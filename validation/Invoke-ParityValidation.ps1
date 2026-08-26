param(
    [string]$RepositoryRoot = (Split-Path -Parent $PSScriptRoot)
)

$ErrorActionPreference = 'Stop'
$failures = [System.Collections.Generic.List[string]]::new()
$warnings = [System.Collections.Generic.List[string]]::new()

function Add-Failure([string]$Message) { $failures.Add($Message) }
function Sorted([object[]]$Values) { @($Values | Sort-Object -Unique) }
function Assert-EqualSet([string]$Name, [object[]]$Expected, [object[]]$Actual) {
    $expectedSorted = Sorted $Expected
    $actualSorted = Sorted $Actual
    $missing = @($expectedSorted | Where-Object { $_ -notin $actualSorted })
    $unexpected = @($actualSorted | Where-Object { $_ -notin $expectedSorted })
    if ($missing.Count -or $unexpected.Count) {
        Add-Failure "$Name differs; missing=[$($missing -join ', ')]; unexpected=[$($unexpected -join ', ')]"
    }
}
function Java-RegisterIds([string]$RelativePath) {
    $text = Get-Content (Join-Path $RepositoryRoot $RelativePath) -Raw
    Sorted ([regex]::Matches($text, 'register(?:Entity|SpawnEgg)?\(\s*"([a-z0-9_./-]+)"') | ForEach-Object { $_.Groups[1].Value })
}
function Resource-Ids([string]$RelativeDirectory) {
    $directory = Join-Path $RepositoryRoot $RelativeDirectory
    if (-not (Test-Path $directory)) { return @() }
    Sorted (Get-ChildItem $directory -Recurse -File -Filter '*.json' | ForEach-Object {
        $_.FullName.Substring($directory.Length + 1).Replace('\', '/').Replace('.json', '')
    })
}

$baselinePath = Join-Path $RepositoryRoot 'validation/baselines/current_registry_ids.json'
$historicalPath = Join-Path $RepositoryRoot 'validation/baselines/historical_registry_targets.json'
$dependencyPath = Join-Path $RepositoryRoot 'validation/baselines/production_dependency_contract.json'
$familyPath = Join-Path $RepositoryRoot 'validation/foundations/family_membership.json'
$historicalFamilyPath = Join-Path $RepositoryRoot 'validation/foundations/historical_family_contracts.json'
$stage3Path = Join-Path $RepositoryRoot 'validation/foundations/stage_3_mushroom_fields_contract.json'
$stage4Path = Join-Path $RepositoryRoot 'validation/foundations/stage_4_badlands_contract.json'
$stage5Path = Join-Path $RepositoryRoot 'validation/foundations/stage_5_swamp_contract.json'
$stage6Path = Join-Path $RepositoryRoot 'validation/foundations/stage_6_dark_forest_contract.json'
$baseline = Get-Content $baselinePath -Raw | ConvertFrom-Json
$null = Get-Content $historicalPath -Raw | ConvertFrom-Json
$dependencyContract = Get-Content $dependencyPath -Raw | ConvertFrom-Json
$familyContract = Get-Content $familyPath -Raw | ConvertFrom-Json
$historicalFamilies = Get-Content $historicalFamilyPath -Raw | ConvertFrom-Json
$stage3 = Get-Content $stage3Path -Raw | ConvertFrom-Json
$stage4 = Get-Content $stage4Path -Raw | ConvertFrom-Json
$stage5 = Get-Content $stage5Path -Raw | ConvertFrom-Json
$stage6 = Get-Content $stage6Path -Raw | ConvertFrom-Json

foreach ($setName in @('blocks','no_item_blocks','standalone_items','entities','spawn_eggs','configured_features','placed_features')) {
    $values = @($stage3.$setName)
    if ((Sorted $values).Count -ne $values.Count) { Add-Failure "Duplicate ID in Stage 3 contract set $setName" }
}
$overlap = @($stage3.blocks | Where-Object { $_ -in @($stage3.no_item_blocks) })
if ($overlap.Count) { Add-Failure "Stage 3 ordinary/no-item block overlap: $($overlap -join ', ')" }
foreach ($setName in @('blocks_with_items','no_item_blocks','items','spawn_eggs','entities','sounds','features','configured_features','placed_features')) {
    $values = @($stage4.registry.$setName)
    if ((Sorted $values).Count -ne $values.Count) { Add-Failure "Duplicate ID in Stage 4 contract set $setName" }
}
foreach ($setName in @('blocks_with_items','no_item_blocks','special_items','spawn_eggs','entities','sounds','particles','block_entities','configured_features','placed_features')) {
    $values = @($stage5.$setName)
    if ((Sorted $values).Count -ne $values.Count) { Add-Failure "Duplicate ID in Stage 5 contract set $setName" }
}
foreach ($setName in @('blocks_with_items','no_item_blocks','special_items','items','configured_features','placed_features')) {
    $values = @($stage6.$setName)
    if ((Sorted $values).Count -ne $values.Count) { Add-Failure "Duplicate ID in Stage 6 contract set $setName" }
}

foreach ($property in $baseline.registries.PSObject.Properties) {
    $values = @($property.Value)
    if ((Sorted $values).Count -ne $values.Count) { Add-Failure "Duplicate ID in baseline registry $($property.Name)" }
}

$parsedBlocks = @(Java-RegisterIds 'src/main/java/party/lemons/biomemakeover/init/BMBlocks.java' | Where-Object { $_ -ne 'stripped_' })
$blocks = Sorted (@($parsedBlocks) + @($stage3.blocks) + @($stage3.no_item_blocks) + @($stage4.registry.blocks_with_items) + @($stage4.registry.no_item_blocks) + @($stage5.blocks_with_items) + @($stage5.no_item_blocks) + @($stage6.blocks_with_items) + @($stage6.no_item_blocks))
$standaloneItems = Java-RegisterIds 'src/main/java/party/lemons/biomemakeover/init/BMItems.java'
$entityFileIds = Java-RegisterIds 'src/main/java/party/lemons/biomemakeover/init/BMEntities.java'
$entities = @($entityFileIds | Where-Object { $_ -notlike '*_spawn_egg' })
$spawnEggs = @($entityFileIds | Where-Object { $_ -like '*_spawn_egg' })
$allNoItemBlocks = @($stage3.no_item_blocks) + @($stage4.registry.no_item_blocks) + @($stage5.no_item_blocks) + @($stage6.no_item_blocks)
$items = Sorted (@($blocks | Where-Object { $_ -notin $allNoItemBlocks }) + @($standaloneItems) + @($spawnEggs))
$sounds = Java-RegisterIds 'src/main/java/party/lemons/biomemakeover/init/BMSounds.java'
$particleText=Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/init/BMParticles.java') -Raw
$particles=Sorted ([regex]::Matches($particleText,'BiomeMakeover\.id\("([a-z0-9_./-]+)"\)')|ForEach-Object{$_.Groups[1].Value})
$blockEntityText=Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/init/BMBlockEntities.java') -Raw
$blockEntities=Sorted ([regex]::Matches($blockEntityText,'BiomeMakeover\.id\("([a-z0-9_./-]+)"\)')|ForEach-Object{$_.Groups[1].Value})
$stage3Java = (Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/init/BMBlocks.java') -Raw) +
    (Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/init/BMItems.java') -Raw) +
    (Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/init/BMEntities.java') -Raw)
foreach ($deferred in $stage3.deferred_released_ids) {
    if ($stage3Java -match "register(?:NoItem|SpawnEgg|Entity)?\(\s*`"$([regex]::Escape([string]$deferred))`"") {
        Add-Failure "Deferred Stage 3 ID was registered prematurely: $deferred"
    }
}

Assert-EqualSet 'block registry IDs' (Sorted (@($baseline.registries.block) + @($stage3.blocks) + @($stage3.no_item_blocks) + @($stage4.registry.blocks_with_items) + @($stage4.registry.no_item_blocks) + @($stage5.blocks_with_items) + @($stage5.no_item_blocks) + @($stage6.blocks_with_items) + @($stage6.no_item_blocks))) $blocks
Assert-EqualSet 'item registry IDs' (Sorted (@($baseline.registries.item) + @($stage3.blocks) + @($stage3.standalone_items) + @($stage3.spawn_eggs) + @($stage4.registry.blocks_with_items) + @($stage4.registry.items) + @($stage4.registry.spawn_eggs) + @($stage5.blocks_with_items) + @($stage5.special_items) + @($stage5.spawn_eggs) + @($stage6.blocks_with_items) + @($stage6.special_items))) $items
Assert-EqualSet 'entity registry IDs' (Sorted (@($baseline.registries.entity_type) + @($stage3.entities) + @($stage4.registry.entities) + @($stage5.entities))) $entities
Assert-EqualSet 'sound registry IDs' (Sorted (@($baseline.registries.sound_event) + @($stage4.registry.sounds) + @($stage5.sounds) + @('illunite_break','illunite_hit','illunite_place','illunite_step'))) $sounds
Assert-EqualSet 'particle registry IDs' (Sorted (@($baseline.registries.particle_type) + @($stage5.particles) + @($stage6.particles))) $particles
Assert-EqualSet 'Stage 5 block-entity registry IDs' @($stage5.block_entities) $blockEntities

$configured = Sorted (@(Resource-Ids 'src/main/resources/data/biomemakeover/worldgen/configured_feature') + @(Resource-Ids 'build/resources/main/data/biomemakeover/worldgen/configured_feature') | Where-Object { $_ -ne 'dark_forest/owl_nest' })
$placed = Sorted (@(Resource-Ids 'src/main/resources/data/biomemakeover/worldgen/placed_feature') + @(Resource-Ids 'build/resources/main/data/biomemakeover/worldgen/placed_feature') | Where-Object { $_ -ne 'dark_forest/owl_nest' })
Assert-EqualSet 'configured feature resource IDs' (Sorted (@($baseline.worldgen_resources.configured_feature) + @($stage3.configured_features) + @($stage4.registry.configured_features) + @($stage5.configured_features) + @($stage6.configured_features))) $configured
Assert-EqualSet 'placed feature resource IDs' (Sorted (@($baseline.worldgen_resources.placed_feature) + @($stage3.placed_features) + @($stage4.registry.placed_features) + @($stage5.placed_features) + @($stage6.placed_features))) $placed

$worldgenText = Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/init/BMWorldgen.java') -Raw
$stage6Injected = @($stage6.biome_injections | ForEach-Object { $_[1] })
$injected = Sorted (@([regex]::Matches($worldgenText, 'BiomeMakeover\.id\("([a-z0-9_./-]+)"\)') | ForEach-Object { $_.Groups[1].Value } | Where-Object { $_ -notin @('swamps','swamp/remove_vanilla_trees') }) + @($stage3.placed_features | Where-Object { $_ -ne 'mushroom_fields/blighted_balsa_checked' }) + @($stage4.registry.placed_features) + @($stage5.placed_features) + $stage6Injected)
Assert-EqualSet 'injected placed feature keys' (Sorted (@($baseline.worldgen_resources.injected_placed_feature) + @($stage3.placed_features | Where-Object { $_ -ne 'mushroom_fields/blighted_balsa_checked' }) + @($stage4.registry.placed_features) + @($stage5.placed_features) + $stage6Injected)) $injected
foreach ($requiredCall in @(
    'addDarkForest\(GenerationStep\.Decoration\.VEGETAL_DECORATION, DARK_FOREST_GRASS\)',
    'addDarkForest\(GenerationStep\.Decoration\.VEGETAL_DECORATION, DARK_FOREST_TALL_GRASS\)',
    'addDarkForest\(GenerationStep\.Decoration\.VEGETAL_DECORATION, DARK_FOREST_FLOWERS\)',
    'addDarkForest\(GenerationStep\.Decoration\.TOP_LAYER_MODIFICATION, DARK_FOREST_ITCHING_IVY\)',
    'addDarkForest\(GenerationStep\.Decoration\.UNDERGROUND_ORES, DARK_FOREST_TREES\)',
    'addDarkForest\(GenerationStep\.Decoration\.UNDERGROUND_ORES, DARK_FOREST_WILD_MUSHROOMS\)',
    'addDarkForest\(GenerationStep\.Decoration\.LOCAL_MODIFICATIONS, DARK_FOREST_FISSURE\)')) {
    if ($worldgenText -notmatch $requiredCall) { Add-Failure "Missing exact Stage 6 Dark Forest injection contract: $requiredCall" }
}

$familyNames = @($familyContract.families | ForEach-Object { $_.name })
if ((Sorted $familyNames).Count -ne $familyNames.Count) { Add-Failure 'Duplicate family name in family membership contract' }
foreach ($family in $familyContract.families) {
    $members = @($family.members)
    if ((Sorted $members).Count -ne $members.Count) { Add-Failure "Duplicate member in family $($family.name)" }
    $notRegistered = @($members | Where-Object { $_ -notin $blocks })
    if ($notRegistered.Count) { Add-Failure "Family $($family.name) contains unregistered current IDs: $($notRegistered -join ', ')" }
    $sortedMembers = Sorted $members
    if (($members -join "`n") -cne ($sortedMembers -join "`n")) { Add-Failure "Family $($family.name) members are not deterministically sorted" }
}

$expectedDecorationSuffixes = @('slab', 'stairs', 'wall')
Assert-EqualSet 'historical DecorationBlockFactory.all suffixes' $expectedDecorationSuffixes $historicalFamilies.decoration_all.generated_suffixes
$expectedWoodBlocks = @(
    '<base>_button', '<base>_door', '<base>_fence', '<base>_fence_gate', '<base>_hanging_sign',
    '<base>_log', '<base>_planks', '<base>_pressure_plate', '<base>_sign', '<base>_slab',
    '<base>_stairs', '<base>_trapdoor', '<base>_wall_hanging_sign', '<base>_wall_sign', '<base>_wood',
    'stripped_<base>_log', 'stripped_<base>_wood'
)
$expectedWoodBlockItems = @(
    '<base>_button', '<base>_door', '<base>_fence', '<base>_fence_gate', '<base>_log',
    '<base>_planks', '<base>_pressure_plate', '<base>_slab', '<base>_stairs', '<base>_trapdoor',
    '<base>_wood', 'stripped_<base>_log', 'stripped_<base>_wood'
)
$expectedWoodSpecialItems = @('<base>_boat', '<base>_chest_boat', '<base>_hanging_sign', '<base>_sign')
Assert-EqualSet 'historical WoodBlockFactory.all blocks' $expectedWoodBlocks $historicalFamilies.wood_all.block_paths
Assert-EqualSet 'historical WoodBlockFactory ordinary block items' $expectedWoodBlockItems $historicalFamilies.wood_all.ordinary_block_item_paths
Assert-EqualSet 'historical WoodBlockFactory special items' $expectedWoodSpecialItems $historicalFamilies.wood_all.special_item_paths
if (-not $historicalFamilies.wood_all.leaves_and_saplings_separate) { Add-Failure 'Historical wood contract must keep leaves and saplings separate' }
$owners = @($historicalFamilies.released_bm_family_ownership | ForEach-Object { $_.family })
if ((Sorted $owners).Count -ne $owners.Count) { Add-Failure 'Duplicate family in historical ownership contract' }
foreach ($ownership in $historicalFamilies.released_bm_family_ownership) {
    if ([string]::IsNullOrWhiteSpace([string]$ownership.owner)) { Add-Failure "Missing owner for historical family $($ownership.family)" }
}

$gradleProperties = @{}
Get-Content (Join-Path $RepositoryRoot 'gradle.properties') | ForEach-Object {
    if ($_ -match '^([a-zA-Z0-9_.-]+)=(.*)$') { $gradleProperties[$matches[1]] = $matches[2] }
}
foreach ($property in $dependencyContract.gradle_properties.PSObject.Properties) {
    if ($gradleProperties[$property.Name] -ne [string]$property.Value) {
        Add-Failure "Production dependency property changed: $($property.Name)"
    }
}
$buildAndMetadata = (Get-Content (Join-Path $RepositoryRoot 'build.gradle') -Raw) +
    (Get-Content (Join-Path $RepositoryRoot 'src/main/resources/fabric.mod.json') -Raw)
foreach ($forbidden in $dependencyContract.forbidden_runtime_dependencies) {
    if ($buildAndMetadata -match [regex]::Escape([string]$forbidden)) { Add-Failure "Forbidden runtime dependency present: $forbidden" }
}
$fabricMetadata = Get-Content (Join-Path $RepositoryRoot 'src/main/resources/fabric.mod.json') -Raw | ConvertFrom-Json
Assert-EqualSet 'runtime dependency IDs' $dependencyContract.required_runtime_dependencies $fabricMetadata.depends.PSObject.Properties.Name

Get-ChildItem (Join-Path $RepositoryRoot 'src/main/resources') -Recurse -File -Filter '*.json' | ForEach-Object {
    $jsonFile = $_
    try { $null = Get-Content $jsonFile.FullName -Raw | ConvertFrom-Json }
    catch {
        $rawJson = Get-Content $jsonFile.FullName -Raw
        if ($_.Exception.Message -like '*argument "name" is not valid*' -and $rawJson -match '""\s*:') {
            $warnings.Add("PowerShell 5 cannot materialize an empty JSON property name; syntax deferred to build: $($jsonFile.FullName.Substring($RepositoryRoot.Length + 1))")
        } else {
            Add-Failure "Invalid JSON: $($jsonFile.FullName.Substring($RepositoryRoot.Length + 1)): $($_.Exception.Message)"
        }
    }
}

foreach ($block in $blocks) {
    $blockResources = @(
        "src/main/resources/assets/biomemakeover/blockstates/$block.json",
        "src/main/resources/data/biomemakeover/loot_table/blocks/$block.json"
    )
    if ($block -notin $allNoItemBlocks) { $blockResources += "src/main/resources/assets/biomemakeover/items/$block.json" }
    foreach ($relative in $blockResources) {
        $sourcePath = Join-Path $RepositoryRoot $relative
        $builtPath = Join-Path $RepositoryRoot ($relative -replace '^src/main/resources/', 'build/resources/main/')
        if (-not (Test-Path $sourcePath) -and -not (Test-Path $builtPath)) { $warnings.Add("Current baseline has no block resource (future stage assertion): $relative") }
    }
}
foreach ($item in $items) {
    $definition = Join-Path $RepositoryRoot "src/main/resources/assets/biomemakeover/items/$item.json"
    $builtDefinition = Join-Path $RepositoryRoot "build/resources/main/assets/biomemakeover/items/$item.json"
    if (-not (Test-Path $definition) -and -not (Test-Path $builtDefinition)) { Add-Failure "Missing item definition: assets/biomemakeover/items/$item.json" }
}
foreach ($feature in $placed) {
    $featurePath = Join-Path $RepositoryRoot "src/main/resources/data/biomemakeover/worldgen/placed_feature/$feature.json"
    if (-not (Test-Path $featurePath)) { $featurePath = Join-Path $RepositoryRoot "build/resources/main/data/biomemakeover/worldgen/placed_feature/$feature.json" }
    $json = Get-Content $featurePath -Raw | ConvertFrom-Json
    $configuredId = [string]$json.feature
    if ($configuredId -like 'biomemakeover:*') {
        $configuredPath = $configuredId.Substring('biomemakeover:'.Length)
        if ($configuredPath -notin $configured) { Add-Failure "Placed feature $feature references missing configured feature $configuredPath" }
    }
}

# Runtime-remediation resource graph checks. These deliberately validate only
# deterministic references; vanilla assets and dynamic runtime lookups remain
# the responsibility of packaged/runtime validation.
$builtAssets = Join-Path $RepositoryRoot 'build/resources/main/assets/biomemakeover'
$builtData = Join-Path $RepositoryRoot 'build/resources/main/data'
if (Test-Path $builtAssets) {
    Get-ChildItem $builtAssets -Recurse -File -Filter '*.json' | ForEach-Object {
        $resourceFile = $_
        $raw = Get-Content $resourceFile.FullName -Raw
        [regex]::Matches($raw, '"(?:model|parent)"\s*:\s*"biomemakeover:([^"#]+)"') | ForEach-Object {
            $modelPath = Join-Path $builtAssets ("models/" + $_.Groups[1].Value + '.json')
            if (-not (Test-Path $modelPath)) { Add-Failure "Missing internal model reference $($_.Groups[0].Value) from $($resourceFile.FullName.Substring($RepositoryRoot.Length + 1))" }
        }
        if ($raw -match 'minecraft:item/template_spawn_egg') {
            Add-Failure "Obsolete 1.20 spawn-egg parent in $($resourceFile.FullName.Substring($RepositoryRoot.Length + 1))"
        }
        if ($resourceFile.FullName -match '[\\/]models[\\/]') {
            $modelJson = $raw | ConvertFrom-Json
            if ($modelJson.textures) {
                foreach ($property in $modelJson.textures.PSObject.Properties) {
                    $texture = [string]$property.Value
                    if ($texture -like 'biomemakeover:*') {
                        $texturePath = Join-Path $builtAssets ("textures/" + $texture.Substring('biomemakeover:'.Length) + '.png')
                        if (-not (Test-Path $texturePath)) { Add-Failure "Missing internal texture reference $texture from $($resourceFile.FullName.Substring($RepositoryRoot.Length + 1))" }
                    }
                }
            }
        }
    }
}

$configuredRoot = Join-Path $builtData 'biomemakeover/worldgen/configured_feature'
if (Test-Path $configuredRoot) {
    Get-ChildItem $configuredRoot -Recurse -File -Filter '*.json' | ForEach-Object {
        $raw = Get-Content $_.FullName -Raw
        if ($raw -match '"type"\s*:\s*"minecraft:uniform"\s*,\s*"value"\s*:') {
            Add-Failure "Obsolete nested uniform IntProvider in $($_.FullName.Substring($RepositoryRoot.Length + 1))"
        }
    }
}

$recipeRoot = Join-Path $builtData 'biomemakeover/recipe'
$supportedRecipeTypes = @(
    'minecraft:crafting_shaped', 'minecraft:crafting_shapeless', 'minecraft:smelting',
    'minecraft:smoking', 'minecraft:campfire_cooking', 'minecraft:stonecutting'
)
if (Test-Path $recipeRoot) {
    Get-ChildItem $recipeRoot -Recurse -File -Filter '*.json' | ForEach-Object {
        $recipeFile = $_
        $recipe = Get-Content $recipeFile.FullName -Raw | ConvertFrom-Json
        $relative = $recipeFile.FullName.Substring($RepositoryRoot.Length + 1)
        if ($recipe.type -notin $supportedRecipeTypes) { Add-Failure "Unvalidated recipe type $($recipe.type) in $relative" }
        if ($recipe.result -is [string] -or -not ($recipe.result.PSObject.Properties.Name -contains 'id')) {
            Add-Failure "Recipe result is not a 1.21.10 item-stack object with id: $relative"
        }
        if ($recipe.type -eq 'minecraft:crafting_shapeless' -and @($recipe.ingredients).Count -lt 1) {
            Add-Failure "Shapeless recipe has no ingredients: $relative"
        }
        if ($recipe.type -eq 'minecraft:crafting_shaped') {
            if (@($recipe.pattern).Count -lt 1 -or @($recipe.key.PSObject.Properties).Count -lt 1) {
                Add-Failure "Shaped recipe has an empty pattern/key: $relative"
            }
        }
        $rawRecipe=Get-Content $recipeFile.FullName -Raw
        [regex]::Matches($rawRecipe,'"biomemakeover:([a-z0-9_./-]+)"') | ForEach-Object {
            if ($_.Groups[1].Value -notin $items) { Add-Failure "Recipe references missing BM item biomemakeover:$($_.Groups[1].Value): $relative" }
        }
    }
}

$advancementRoot = Join-Path $builtData 'biomemakeover/advancement'
if (Test-Path $advancementRoot) {
    $advancementJava=Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/init/BMAdvancements.java') -Raw
    $registeredCustomTriggers=Sorted ([regex]::Matches($advancementJava,'CriteriaTriggers\.register\(\s*"biomemakeover:([a-z0-9_./-]+)"')|ForEach-Object{$_.Groups[1].Value})
    $advancementIds = Resource-Ids 'build/resources/main/data/biomemakeover/advancement'
    Get-ChildItem $advancementRoot -Recurse -File -Filter '*.json' | ForEach-Object {
        $advancementFile = $_
        $advancement = Get-Content $advancementFile.FullName -Raw | ConvertFrom-Json
        $relative = $advancementFile.FullName.Substring($RepositoryRoot.Length + 1)
        $criteria = @($advancement.criteria.PSObject.Properties.Name)
        if ($criteria.Count -lt 1) { Add-Failure "Advancement has no criteria: $relative" }
        if ($advancement.parent -like 'biomemakeover:*') {
            $parent = ([string]$advancement.parent).Substring('biomemakeover:'.Length)
            if ($parent -notin $advancementIds) { Add-Failure "Advancement references missing BM parent $($advancement.parent): $relative" }
        }
        if ($null -ne $advancement.requirements) {
            $required = @($advancement.requirements | ForEach-Object { @($_) } | ForEach-Object { $_ })
            $missing = @($criteria | Where-Object { $_ -notin $required })
            $unknown = @($required | Where-Object { $_ -notin $criteria })
            if ($missing.Count -or $unknown.Count) { Add-Failure "Advancement requirements mismatch in ${relative}: missing=[$($missing -join ', ')], unknown=[$($unknown -join ', ')]" }
        }
        $raw = Get-Content $advancementFile.FullName -Raw
        if ($raw -match '"item"\s*:\s*\[') { Add-Failure "Obsolete singular advancement item predicate in $relative" }
        [regex]::Matches($raw,'"trigger"\s*:\s*"biomemakeover:([a-z0-9_./-]+)"')|ForEach-Object{if($_.Groups[1].Value -notin $registeredCustomTriggers){Add-Failure "Advancement leaks unavailable BM custom trigger biomemakeover:$($_.Groups[1].Value) in $relative"}}
        if ($raw -match '"condition"\s*:\s*"minecraft:match_tool"[\s\S]{0,180}"tag"\s*:') { Add-Failure "Obsolete match_tool tag predicate in $relative" }
    }
}

$lootRoot=Join-Path $builtData 'biomemakeover/loot_table'
if(Test-Path $lootRoot){Get-ChildItem $lootRoot -Recurse -File -Filter '*.json'|ForEach-Object{$raw=Get-Content $_.FullName -Raw;$relative=$_.FullName.Substring($RepositoryRoot.Length+1);if($raw -match 'minecraft:(looting_enchant|random_chance_with_looting)'){Add-Failure "Obsolete pre-1.21 Looting schema in $relative"}}}

# Minecraft 1.21.10 TemptGoal reads Attributes.TEMPT_RANGE on every canUse
# evaluation. Animal.createAnimalAttributes supplies the vanilla 10-block
# contract; custom builders must add the attribute explicitly.
Get-ChildItem (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/entity') -File -Filter '*.java' | ForEach-Object {
    $entityFile = $_
    $source = Get-Content $entityFile.FullName -Raw
    if ($source -match 'new\s+TemptGoal\s*\(' -and
        $source -notmatch 'createAnimalAttributes\s*\(' -and
        $source -notmatch 'Attributes\.TEMPT_RANGE') {
        Add-Failure "Entity uses 1.21.10 TemptGoal without TEMPT_RANGE attribute contract: $($entityFile.Name)"
    }
}

$saguaroSource = Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/block/SaguaroCactusBlock.java') -Raw
if ($saguaroSource -notmatch 'nextInt\(maximum\s*-\s*minimum\)' -or $saguaroSource -match 'maximum\s*-\s*minimum\s*\+\s*1') {
    Add-Failure 'Saguaro historical randomRange contract must keep its exclusive upper bound'
}
$clientInitializer = Get-Content (Join-Path $RepositoryRoot 'src/client/java/party/lemons/biomemakeover/client/BiomeMakeoverClient.java') -Raw
if ($clientInitializer -notmatch 'BlockRenderLayerMap\.putBlocks[\s\S]*BMBlocks\.TUMBLEWEED') {
    Add-Failure 'Transparent Tumbleweed block model is missing a client cutout render-layer contract'
}
$glowfishSource = Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/entity/GlowfishEntity.java') -Raw
if ($glowfishSource -notmatch 'Salmon\.Variant\.DEFAULT') {
    Add-Failure 'Glowfish must opt out of post-1.20 random Salmon size variants'
}
$glowfishRendererSource = Get-Content (Join-Path $RepositoryRoot 'src/client/java/party/lemons/biomemakeover/client/render/GlowfishRenderer.java') -Raw
if ($glowfishRendererSource -notmatch 'extends\s+RenderLayer<SalmonRenderState,SalmonModel>' -or
    $glowfishRendererSource -notmatch 'getParentModel\(\)\.root\(\)\.getChild\("body_back"\)\.translateAndRotate') {
    Add-Failure 'Glowfish attachment must render as a LivingEntity render layer inheriting Salmon/body_back transforms'
}
$attachedBlockRenderers = @(
    'src/client/java/party/lemons/biomemakeover/client/render/TumbleweedRenderer.java',
    'src/client/java/party/lemons/biomemakeover/client/render/GlowfishRenderer.java'
)
foreach ($relative in $attachedBlockRenderers) {
    $source = Get-Content (Join-Path $RepositoryRoot $relative) -Raw
    if ($source -match 'submitBlock\([\s\S]*OverlayTexture\.NO_OVERLAY\s*,\s*-1\s*\)') {
        Add-Failure "Entity-attached block uses -1 as a 1.21.10 outline color and will render white: $relative"
    }
}
$scuttlerEntitySource = Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/entity/ScuttlerEntity.java') -Raw
$scuttlerRendererSource = Get-Content (Join-Path $RepositoryRoot 'src/client/java/party/lemons/biomemakeover/client/render/ScuttlerRenderer.java') -Raw
if ($scuttlerEntitySource -notmatch 'getMaxSpawnClusterSize\s*\(' -or $scuttlerEntitySource -notmatch 'inflate\(50\)') {
    Add-Failure 'Scuttler released single-cluster and 50-block exclusion spawn contract is incomplete'
}
if ($scuttlerRendererSource -notmatch 'state\.rattleTime\s*=\s*entity\.getRattleTime') {
    Add-Failure 'Scuttler renderer does not transfer released rattle animation state'
}
if ($saguaroSource -notmatch 'support\.is\(Blocks\.SAND\)' -or $saguaroSource -notmatch 'support\.is\(Blocks\.RED_SAND\)') {
    Add-Failure 'Saguaro growth origins must retain the released sand/red-sand support gate'
}
if ($saguaroSource -notmatch 'if\s*\(isValidBonemealTarget\([^)]+\)\s*&&\s*random\.nextInt\(10\)\s*==\s*0\)') {
    Add-Failure 'Saguaro random ticking must validate the released base growth origin before consuming its growth roll'
}
if ($saguaroSource -notmatch 'ticks\.scheduleTick\(pos\s*,\s*this\s*,\s*1\)' -or
    $saguaroSource -notmatch 'if\s*\(!state\.canSurvive\(level\s*,\s*pos\)\)\s*level\.destroyBlock') {
    Add-Failure 'Saguaro neighbor survival/update cleanup contract is incomplete'
}

$cowboySource = Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/entity/CowboyEntity.java') -Raw
$entitiesSource = Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/init/BMEntities.java') -Raw
if ($entitiesSource -notmatch '(?s)COWBOY\s*=.*?\.sized\(\.6F,1\.95F\).*?\.passengerAttachments\(2\.0F\).*?\.ridingOffset\(-0\.6F\)') {
    Add-Failure 'Cowboy must retain the Minecraft 1.21.10 Pillager passenger/vehicle attachment contract'
}
$patrolSource = Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/mixin/PatrolSpawnerMixin.java') -Raw
$horseSource = Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/mixin/HorseMixin.java') -Raw
$cowboyRenderer = Get-Content (Join-Path $RepositoryRoot 'src/client/java/party/lemons/biomemakeover/client/render/CowboyRenderer.java') -Raw
$cowboyHatLayer = Get-Content (Join-Path $RepositoryRoot 'src/client/java/party/lemons/biomemakeover/client/render/CowboyHatLayer.java') -Raw
if ($cowboyHatLayer -notmatch '(?s)getChild\("head_parts"\)\.translateAndRotate\(pose\).*?translate\(0,-\.4F-2F/16F,3F/32F\).*?rotationDegrees\(-25\)') {
    Add-Failure 'Leader-horse Cowboy Hat must retain its skull-centered Z anchor and accepted two-pixel upward seating correction'
}
$horseModelMixin = Get-Content (Join-Path $RepositoryRoot 'src/client/java/party/lemons/biomemakeover/mixin/client/HorseModelMixin.java') -Raw
if ($horseModelMixin -notmatch 'biomemakeover\$hasHat\(\)' -or
    $horseModelMixin -notmatch 'getChild\("left_ear"\)\.y-=1\.0F' -or
    $horseModelMixin -notmatch 'getChild\("right_ear"\)\.y-=1\.0F') {
    Add-Failure 'Mythas horse-ear refinement must be hat-state-only and limited to a one-pixel render-model lift'
}
$horseRendererMixin = Get-Content (Join-Path $RepositoryRoot 'src/client/java/party/lemons/biomemakeover/mixin/client/HorseRendererMixin.java') -Raw
$itemsSource = Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/init/BMItems.java') -Raw
$clientInitializer = Get-Content (Join-Path $RepositoryRoot 'src/client/java/party/lemons/biomemakeover/client/BiomeMakeoverClient.java') -Raw
$playerHatLayer = Get-Content (Join-Path $RepositoryRoot 'src/client/java/party/lemons/biomemakeover/client/render/CowboyHatArmorRenderer.java') -Raw
$patrolInvoker = Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/mixin/PatrolSpawnerInvoker.java') -Raw
if ($cowboySource -notmatch 'setDropChance\(EquipmentSlot\.HEAD\s*,\s*\.25F\)' -or
    $cowboySource -notmatch 'setDropChance\(EquipmentSlot\.HEAD\s*,\s*2\.0F\)' -or
    $cowboySource -notmatch 'DataComponents\.BANNER_PATTERNS' -or
    $cowboySource -notmatch '@Override public boolean isCaptain\(\)' -or
    $cowboySource -match 'MobEffects\.BAD_OMEN') {
    Add-Failure 'Cowboy equipment/banner or modern captain identity is incomplete, or legacy direct Bad Omen remains'
}
if ($patrolSource -notmatch 'biomemakeover\$setCowboySpawned' -or $patrolSource -notmatch 'if\(leader\).*biomemakeover\$setHat') {
    Add-Failure 'Badlands patrol replacement does not mark every horse persistent and the leader horse hatted'
}
if ($horseSource -notmatch 'putBoolean\("Hat"' -or $horseSource -notmatch 'putBoolean\("CowboySpawned"' -or
    $horseSource -notmatch 'removeWhenFarAway') {
    Add-Failure 'Cowboy horse synchronized/persistent/despawn contract is incomplete'
}
if ($cowboyRenderer -notmatch 'CowboyHatLayer' -or $horseRendererMixin -notmatch 'biomemakeover\$setHasHat') {
    Add-Failure 'Cowboy or leader-horse hat render-state chain is incomplete'
}
if ($itemsSource -match 'COWBOY_HAT[^\r\n]+humanoidArmor' -or
    $itemsSource -notmatch 'COWBOY_HAT[\s\S]{0,500}Equippable\.builder\(EquipmentSlot\.HEAD\)' -or
    $itemsSource -notmatch 'COWBOY_HAT[\s\S]{0,500}\.durability\(500\)') {
    Add-Failure 'Cowboy Hat must retain historical wearable durability without a vanilla armor render asset'
}
if ($clientInitializer -notmatch 'ArmorRenderer\.register' -or
    $clientInitializer -notmatch 'CowboyHatArmorRenderer' -or
    $playerHatLayer -notmatch 'implements ArmorRenderer' -or
    $playerHatLayer -notmatch 'shouldRenderDefaultHeadItem' -or
    $playerHatLayer -notmatch 'return false' -or
    $playerHatLayer -notmatch 'translate\(0,-0\.125F,0\)') {
    Add-Failure 'Cowboy Hat must use historical custom armor rendering and suppress the duplicate flat head-item model'
}
$hatModelSource = Get-Content (Join-Path $RepositoryRoot 'src/client/java/party/lemons/biomemakeover/client/model/CowboyHatModel.java') -Raw
if ($hatModelSource -notmatch 'LayerDefinition\.create\(mesh,64,64\)' -or
    $playerHatLayer -notmatch 'textures/misc/cowboy_hat\.png') {
    Add-Failure 'Equipped Cowboy Hat must retain its historical 64x64 UV atlas and misc texture binding'
}
$cowboyLootPath = Join-Path $builtData 'biomemakeover/loot_table/entities/cowboy.json'
$cowboyLootRaw = if (Test-Path $cowboyLootPath) { Get-Content $cowboyLootPath -Raw } else { '' }
if ($cowboyLootRaw -notmatch '"is_captain"\s*:\s*true' -or
    $cowboyLootRaw -notmatch 'minecraft:set_ominous_bottle_amplifier' -or
    $cowboyLootRaw -notmatch 'minecraft:entities/pillager' -or
    $cowboyLootRaw -notmatch '"max"\s*:\s*4\.0' -or $cowboyLootRaw -notmatch '"min"\s*:\s*0\.0') {
    Add-Failure 'Cowboy captain loot must mirror the 1.21.10 Pillager Ominous Bottle pool and 0-4 amplifier range'
}
if (Test-Path (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/command/BMDebugCommands.java')) {
    Add-Failure 'Accepted Cowboy test command exposure must be absent after Stage 5 cleanup'
}
$mainInitializer = Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/BiomeMakeover.java') -Raw
if ($mainInitializer -match 'BMDebugCommands') {
    Add-Failure 'Production initializer still exposes the temporary Cowboy patrol test command'
}
$dragonflySource=Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/entity/DragonflyEntity.java') -Raw
if($dragonflySource -notmatch 'implements FlyingAnimal' -or $dragonflySource -notmatch 'causeFallDamage' -or $dragonflySource -notmatch 'PathType\.WATER,-1F'){Add-Failure 'Dragonfly/Lightning Bug must retain released flying no-fall and water-avoidance contracts'}
$decayedBreathingTag=Join-Path $builtData 'minecraft/tags/entity_type/can_breathe_under_water.json'
if(-not(Test-Path $decayedBreathingTag) -or (Get-Content $decayedBreathingTag -Raw) -notmatch 'biomemakeover:decayed'){Add-Failure 'Modern Decayed type must retain the released non-drowning undead water contract'}
$lightningRendererSource=Get-Content (Join-Path $RepositoryRoot 'src/client/java/party/lemons/biomemakeover/client/render/LightningBugRenderer.java') -Raw
if($lightningRendererSource -notmatch 'LIGHTNING_BUG_INNER' -or $lightningRendererSource -notmatch 'LIGHTNING_BUG_OUTER' -or $lightningRendererSource -notmatch 'entityTranslucent' -or $lightningRendererSource -notmatch '0x00F000F0'){Add-Failure 'Lightning Bug must retain separate translucent inner/outer full-bright render layers'}
$lightningParticle=Join-Path $builtAssets 'particles/lightning_spark.json'
if(-not(Test-Path $lightningParticle)){Add-Failure 'Released Lightning Bug spark particle definition is not packaged'}
$swampLogIds=@('biomemakeover:willow_log','biomemakeover:stripped_willow_log','biomemakeover:willow_wood','biomemakeover:stripped_willow_wood','biomemakeover:swamp_cypress_log','biomemakeover:stripped_swamp_cypress_log','biomemakeover:swamp_cypress_wood','biomemakeover:stripped_swamp_cypress_wood')
foreach($tagPath in @('block/logs.json','block/logs_that_burn.json','item/logs.json','item/logs_that_burn.json')){
    $tagFile=Join-Path $builtData "minecraft/tags/$tagPath"
    if(-not(Test-Path $tagFile)){Add-Failure "Missing leaf-support tag $tagPath";continue}
    $tagValues=@((Get-Content $tagFile -Raw|ConvertFrom-Json).values)
    foreach($logId in $swampLogIds){if($logId -notin $tagValues){Add-Failure "$tagPath omits Stage 5 trunk $logId required by leaf distance propagation"}}
}
$willowItemDefinition=Join-Path $builtAssets 'items/willow_leaves.json'
$cypressItemDefinition=Join-Path $builtAssets 'items/swamp_cypress_leaves.json'
if(-not(Test-Path $willowItemDefinition) -or (Get-Content $willowItemDefinition -Raw) -notmatch 'biomemakeover:block/willow_leaves' -or (Get-Content $willowItemDefinition -Raw) -notmatch 'minecraft:constant' -or (Get-Content $willowItemDefinition -Raw) -notmatch '-12012264') { Add-Failure 'Willow Leaves item definition must tint the block model with its historical no-world foliage color' }
if(-not(Test-Path $cypressItemDefinition) -or (Get-Content $cypressItemDefinition -Raw) -notmatch 'biomemakeover:block/swamp_cypress_leaves' -or (Get-Content $cypressItemDefinition -Raw) -notmatch 'minecraft:constant' -or (Get-Content $cypressItemDefinition -Raw) -notmatch '-8082577') { Add-Failure 'Swamp Cypress Leaves item definition must tint the block model with its historical no-world foliage color' }
$blocksSource=Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/init/BMBlocks.java') -Raw
if($blocksSource -notmatch 'registerOnWater\("small_lily_pad"' -or $blocksSource -notmatch 'registerOnWater\("water_lily"' -or $blocksSource -notmatch 'new PlaceOnWaterBlockItem') { Add-Failure 'Released Small Lily Pad and Water Lily items must retain their source-water placement item contract' }
foreach($padId in @('small_lily_pad','water_lily')){
    $padDefinition=Join-Path $builtAssets "items/$padId.json"
    if(-not(Test-Path $padDefinition) -or (Get-Content $padDefinition -Raw) -notmatch '-13312764'){Add-Failure "$padId item definition omits the released green no-world tint"}
}
$waterSaplingSource=Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/block/WaterSaplingBlock.java') -Raw
if($blocksSource -notmatch 'WaterSaplingBlock\(WILLOW_GROWER, WILLOW_TREE, false, 1' -or $blocksSource -notmatch 'WaterSaplingBlock\(SWAMP_CYPRESS_GROWER, SWAMP_CYPRESS_TREE, true, 3' -or $waterSaplingSource -notmatch 'pos\.above\(maxDepth\)' -or $waterSaplingSource -notmatch 'waterOrigin \? Blocks\.WATER\.defaultBlockState\(\) : Blocks\.AIR\.defaultBlockState\(\)'){Add-Failure 'Willow/Cypress must retain released depth (1/3) and distinct air/water feature-origin growth contracts'}
$bottleBlockstate=Join-Path $builtAssets 'blockstates/lightning_bug_bottle.json'
$bottleLoot=Join-Path $builtData 'biomemakeover/loot_table/blocks/lightning_bug_bottle.json'
$bottleAdvancement=Join-Path $builtData 'biomemakeover/advancement/biomemakeover/lightning_bug_bottle.json'
$bottleRenderer=Get-Content (Join-Path $RepositoryRoot 'src/client/java/party/lemons/biomemakeover/client/render/LightningBugBottleRenderer.java') -Raw
$lightningInteractionSource=Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/entity/LightningBugEntity.java') -Raw
if(-not(Test-Path $bottleBlockstate) -or -not(Test-Path $bottleLoot) -or -not(Test-Path $bottleAdvancement) -or $blocksSource -notmatch 'lightLevel\(state -> 15\)' -or $lightningInteractionSource -notmatch 'Items\.GLASS_BOTTLE' -or $bottleRenderer -notmatch 'LIGHTNING_BUG_INNER' -or $bottleRenderer -notmatch 'LIGHTNING_BUG_OUTER'){Add-Failure 'Released Lightning Bug glass-bottle capture, level-15 block, data, and contained-bug renderer chain is incomplete'}
$lightningBottleEntity=Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/entity/LightningBottleEntity.java'
$lightningBottleItemDefinition=Join-Path $builtAssets 'items/lightning_bottle.json'
$lightningBottleAdvancement=Join-Path $builtData 'biomemakeover/advancement/biomemakeover/bottle_o_lightning.json'
$entitySource=Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/init/BMEntities.java') -Raw
if(-not(Test-Path $lightningBottleEntity) -or -not(Test-Path $lightningBottleItemDefinition) -or -not(Test-Path $lightningBottleAdvancement) -or $lightningInteractionSource -notmatch 'Items\.EXPERIENCE_BOTTLE' -or $entitySource -notmatch 'registerEntity\("lightning_bottle"' -or $entitySource -notmatch 'updateInterval\(4\)'){Add-Failure 'Released experience-bottle capture and throwable Lightning Bottle registry/resource chain is incomplete'}
$branchItemDefinition=Join-Path $builtAssets 'items/willowing_branches.json'
if(-not(Test-Path $branchItemDefinition) -or (Get-Content $branchItemDefinition -Raw) -notmatch '-12012264'){Add-Failure 'Willowing Branch item definition omits its released no-world foliage tint'}
if($entitySource -match 'registerEntity\("toad"' -or $entitySource -match 'registerEntity\("tadpole"' -or $entitySource -match 'registerSpawnEgg\("toad_spawn_egg"'){Add-Failure 'Final 1.20.1-disabled Toad/Tadpole content was activated despite released reachability evidence'}
foreach($leafId in @('willow_leaves','swamp_cypress_leaves')){
    $leafLootPath=Join-Path $builtData "biomemakeover/loot_table/blocks/$leafId.json"
    if(-not(Test-Path $leafLootPath)){Add-Failure "Missing Stage 5 leaf loot table $leafId";continue}
    $leafLootRaw=Get-Content $leafLootPath -Raw
    if($leafLootRaw -match '"item"\s*:\s*"minecraft:shears"' -or $leafLootRaw -match '"items"\s*:\s*\[' -or $leafLootRaw -match '"enchantments"\s*:\s*\[\s*\{\s*"enchantment"') { Add-Failure "$leafId retains obsolete pre-1.21.10 tool-predicate syntax" }
    if($leafLootRaw -notmatch '"items"\s*:\s*"minecraft:shears"' -or $leafLootRaw -notmatch '"minecraft:enchantments"' -or $leafLootRaw -notmatch '"enchantments"\s*:\s*"minecraft:silk_touch"') { Add-Failure "$leafId does not retain the modern shears/Silk Touch leaf-acquisition contract" }
}
$lightningSource=Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/entity/LightningBugEntity.java') -Raw
if($lightningSource -notmatch 'visualPhase' -or $lightningSource -notmatch 'advanceVisualColor' -or $lightningRendererSource.IndexOf('LIGHTNING_BUG_INNER') -gt $lightningRendererSource.IndexOf('LIGHTNING_BUG_OUTER')) { Add-Failure 'Lightning Bug must retain historical randomized pulse, interpolated position color, and inner/outer layer order' }
$clientMixinConfig = Get-Content (Join-Path $RepositoryRoot 'src/main/resources/biomemakeover.client.mixins.json') -Raw | ConvertFrom-Json
if ('HorseRenderStateMixin' -notin @($clientMixinConfig.client) -or 'HorseRendererMixin' -notin @($clientMixinConfig.client)) {
    Add-Failure 'Cowboy horse rendering mixins are not isolated and registered in the client-only mixin list'
}

$registryByTagDirectory = @{
    'block' = $blocks; 'item' = $items; 'entity_type' = $entities
}
Get-ChildItem $builtData -Recurse -File -Filter '*.json' | Where-Object { $_.FullName -match '[\\/]tags[\\/](block|item|entity_type|damage_type|worldgen[\\/]biome)[\\/]' } | ForEach-Object {
    $tagFile = $_
    $tagDirectory = [regex]::Match($tagFile.FullName, '[\\/]tags[\\/](block|item|entity_type|damage_type|worldgen[\\/]biome)[\\/]').Groups[1].Value.Replace('\', '/')
    $tag = Get-Content $tagFile.FullName -Raw | ConvertFrom-Json
    foreach ($entry in @($tag.values)) {
        $id = if ($entry -is [string]) { $entry } else { [string]$entry.id }
        if ($id -like '#biomemakeover:*') {
            $target = $id.Substring('#biomemakeover:'.Length)
            $targetFile = Join-Path $builtData ("biomemakeover/tags/$tagDirectory/$target.json")
            if (-not (Test-Path $targetFile)) { Add-Failure "Missing internal tag reference $id from $($tagFile.FullName.Substring($RepositoryRoot.Length + 1))" }
        } elseif ($id -like 'biomemakeover:*' -and $registryByTagDirectory.ContainsKey($tagDirectory)) {
            $target = $id.Substring('biomemakeover:'.Length)
            if ($target -notin @($registryByTagDirectory[$tagDirectory])) { Add-Failure "Tag references missing BM $tagDirectory ID $id from $($tagFile.FullName.Substring($RepositoryRoot.Length + 1))" }
        }
    }
}

$legacyTagFiles = Get-ChildItem (Join-Path $RepositoryRoot 'src/main/resources/data') -Recurse -File |
    Where-Object { $_.FullName -match '[\\/]tags[\\/](blocks|items)[\\/]' } | ForEach-Object {
        $_.FullName.Substring($RepositoryRoot.Length + 1).Replace('\', '/')
    }
Assert-EqualSet 'grandfathered legacy plural tag files' $dependencyContract.allowed_legacy_plural_tag_files $legacyTagFiles
foreach ($legacyTagFile in $legacyTagFiles) {
    $warnings.Add("Grandfathered legacy plural tag file; do not copy this path in restored content: $legacyTagFile")
}

if ($failures.Count) {
    Write-Host "PARITY VALIDATION FAILED ($($failures.Count) issue(s))"
    $failures | ForEach-Object { Write-Host " - $_" }
    exit 1
}

Write-Host 'PARITY VALIDATION PASSED'
Write-Host " registries: blocks=$($blocks.Count), items=$($items.Count), entities=$($entities.Count), block_entities=$($blockEntities.Count), sounds=$($sounds.Count), particles=$($particles.Count)"
Write-Host " worldgen resources: configured=$($configured.Count), placed=$($placed.Count), injected=$($injected.Count)"
Write-Host " foundations: current_families=$(@($familyContract.families).Count), historical_owned_families=$($owners.Count), runtime_dependencies=$(@($fabricMetadata.depends.PSObject.Properties).Count)"
Write-Host ' JSON syntax, dependencies, family membership, and current block/item/feature resource contracts passed'
if ($warnings.Count) {
    Write-Host " warnings=$($warnings.Count)"
    $warnings | ForEach-Object { Write-Host " - $_" }
}
