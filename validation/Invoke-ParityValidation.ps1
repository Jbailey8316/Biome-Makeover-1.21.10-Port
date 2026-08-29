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
$stage8Path = Join-Path $RepositoryRoot 'validation/foundations/stage_8_rootling_moth_contract.json'
$stage9aPath = Join-Path $RepositoryRoot 'validation/foundations/stage_9a_functional_utilities_contract.json'
$stage9b2Path = Join-Path $RepositoryRoot 'validation/foundations/stage_9b2_altar_contract.json'
$stage10aPath = Join-Path $RepositoryRoot 'validation/foundations/stage_10a_mushroom_house_contract.json'
$baseline = Get-Content $baselinePath -Raw | ConvertFrom-Json
$null = Get-Content $historicalPath -Raw | ConvertFrom-Json
$dependencyContract = Get-Content $dependencyPath -Raw | ConvertFrom-Json
$familyContract = Get-Content $familyPath -Raw | ConvertFrom-Json
$historicalFamilies = Get-Content $historicalFamilyPath -Raw | ConvertFrom-Json
$stage3 = Get-Content $stage3Path -Raw | ConvertFrom-Json
$stage4 = Get-Content $stage4Path -Raw | ConvertFrom-Json
$stage5 = Get-Content $stage5Path -Raw | ConvertFrom-Json
$stage6 = Get-Content $stage6Path -Raw | ConvertFrom-Json
$stage8 = Get-Content $stage8Path -Raw | ConvertFrom-Json
$stage9a = Get-Content $stage9aPath -Raw | ConvertFrom-Json
$stage9b2 = Get-Content $stage9b2Path -Raw | ConvertFrom-Json
$stage10a = Get-Content $stage10aPath -Raw | ConvertFrom-Json

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
foreach ($setName in @('blocks_with_items','no_item_blocks','special_items','items','features','configured_features','placed_features')) {
    $values = @($stage6.$setName)
    if ((Sorted $values).Count -ne $values.Count) { Add-Failure "Duplicate ID in Stage 6 contract set $setName" }
}
foreach ($setName in @('blocks_with_items','no_item_blocks','items','spawn_eggs','entities','sounds','mob_effects','potions','recipes')) { $values=@($stage8.$setName);if((Sorted $values).Count-ne$values.Count){Add-Failure "Duplicate ID in Stage 8 contract set $setName"} }
foreach ($setName in @('blocks_with_items','no_item_blocks','items','recipes','advancements')) { $values=@($stage9a.$setName);if((Sorted $values).Count-ne$values.Count){Add-Failure "Duplicate ID in Stage 9A contract set $setName"} }
foreach ($setName in @('blocks_with_items','block_entities','menus','sounds','recipes','loot_tables','advancements','item_tags','enchantment_tags','textures')) { $values=@($stage9b2.$setName);if((Sorted $values).Count-ne$values.Count){Add-Failure "Duplicate ID in Stage 9B.2 contract set $setName"} }
foreach ($setName in @('items','entities','spawn_eggs','sounds','structure_processors','structures','structure_sets','template_pools','processor_lists','templates','loot_tables','advancements','jukebox_songs','biome_tags')) { $values=@($stage10a.$setName);if((Sorted $values).Count-ne$values.Count){Add-Failure "Duplicate ID in Stage 10A contract set $setName"} }

foreach ($property in $baseline.registries.PSObject.Properties) {
    $values = @($property.Value)
    if ((Sorted $values).Count -ne $values.Count) { Add-Failure "Duplicate ID in baseline registry $($property.Name)" }
}

$parsedBlocks = @(Java-RegisterIds 'src/main/java/party/lemons/biomemakeover/init/BMBlocks.java' | Where-Object { $_ -ne 'stripped_' })
$blocks = Sorted (@($parsedBlocks) + @($stage3.blocks) + @($stage3.no_item_blocks) + @($stage4.registry.blocks_with_items) + @($stage4.registry.no_item_blocks) + @($stage5.blocks_with_items) + @($stage5.no_item_blocks) + @($stage6.blocks_with_items) + @($stage6.no_item_blocks) + @($stage8.blocks_with_items) + @($stage8.no_item_blocks) + @($stage9a.blocks_with_items) + @($stage9a.no_item_blocks))
$standaloneItems = Java-RegisterIds 'src/main/java/party/lemons/biomemakeover/init/BMItems.java'
$entityFileIds = Java-RegisterIds 'src/main/java/party/lemons/biomemakeover/init/BMEntities.java'
$entities = @($entityFileIds | Where-Object { $_ -notlike '*_spawn_egg' })
$spawnEggs = @($entityFileIds | Where-Object { $_ -like '*_spawn_egg' })
$allNoItemBlocks = @($stage3.no_item_blocks) + @($stage4.registry.no_item_blocks) + @($stage5.no_item_blocks) + @($stage6.no_item_blocks) + @($stage8.no_item_blocks) + @($stage9a.no_item_blocks)
$items = Sorted (@($blocks | Where-Object { $_ -notin $allNoItemBlocks }) + @($standaloneItems) + @($spawnEggs))
$sounds = Java-RegisterIds 'src/main/java/party/lemons/biomemakeover/init/BMSounds.java'
$particleText=Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/init/BMParticles.java') -Raw
$particles=Sorted ([regex]::Matches($particleText,'BiomeMakeover\.id\("([a-z0-9_./-]+)"\)')|ForEach-Object{$_.Groups[1].Value})
$blockEntityText=Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/init/BMBlockEntities.java') -Raw
$blockEntities=Sorted ([regex]::Matches($blockEntityText,'BiomeMakeover\.id\("([a-z0-9_./-]+)"\)')|ForEach-Object{$_.Groups[1].Value})
$structureProcessorText=Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/init/BMStructureProcessors.java') -Raw
$structureProcessors=Sorted ([regex]::Matches($structureProcessorText,'BiomeMakeover\.id\("([a-z0-9_./-]+)"\)')|ForEach-Object{$_.Groups[1].Value})
$stage3Java = (Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/init/BMBlocks.java') -Raw) +
    (Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/init/BMItems.java') -Raw) +
    (Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/init/BMEntities.java') -Raw)
foreach ($deferred in $stage3.deferred_released_ids) {
    if ($stage3Java -match "register(?:NoItem|SpawnEgg|Entity)?\(\s*`"$([regex]::Escape([string]$deferred))`"") {
        Add-Failure "Deferred Stage 3 ID was registered prematurely: $deferred"
    }
}

Assert-EqualSet 'block registry IDs' (Sorted (@($baseline.registries.block) + @($stage3.blocks) + @($stage3.no_item_blocks) + @($stage4.registry.blocks_with_items) + @($stage4.registry.no_item_blocks) + @($stage5.blocks_with_items) + @($stage5.no_item_blocks) + @($stage6.blocks_with_items) + @($stage6.no_item_blocks) + @($stage8.no_item_blocks) + @($stage9a.no_item_blocks) + @($stage9b2.blocks_with_items))) $blocks
Assert-EqualSet 'item registry IDs' (Sorted (@($baseline.registries.item) + @($stage3.blocks) + @($stage3.standalone_items) + @($stage3.spawn_eggs) + @($stage4.registry.blocks_with_items) + @($stage4.registry.items) + @($stage4.registry.spawn_eggs) + @($stage5.blocks_with_items) + @($stage5.special_items) + @($stage5.spawn_eggs) + @($stage6.blocks_with_items) + @($stage6.special_items) + @($stage8.items) + @($stage8.spawn_eggs) + @($stage9a.items) + @($stage9b2.blocks_with_items) + @($stage10a.items) + @($stage10a.spawn_eggs))) $items
Assert-EqualSet 'entity registry IDs' (Sorted (@($baseline.registries.entity_type) + @($stage3.entities) + @($stage4.registry.entities) + @($stage5.entities) + @($stage8.entities) + @($stage10a.entities))) $entities
Assert-EqualSet 'sound registry IDs' (Sorted (@($baseline.registries.sound_event) + @($stage4.registry.sounds) + @($stage5.sounds) + @('illunite_break','illunite_hit','illunite_place','illunite_step') + @($stage8.sounds) + @($stage9b2.sounds) + @($stage10a.sounds))) $sounds
Assert-EqualSet 'particle registry IDs' (Sorted (@($baseline.registries.particle_type) + @($stage5.particles) + @($stage6.particles))) $particles
Assert-EqualSet 'block-entity registry IDs' (Sorted (@($stage5.block_entities) + @($stage9b2.block_entities))) $blockEntities
Assert-EqualSet 'structure-processor registry IDs' @($stage10a.structure_processors) $structureProcessors

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
    $featureSource = Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/init/BMFeatures.java') -Raw
    $registeredBmFeatures = Sorted ([regex]::Matches($featureSource,
        'Registry\.register\(\s*BuiltInRegistries\.FEATURE\s*,\s*BiomeMakeover\.id\("([a-z0-9_./-]+)"\)') |
        ForEach-Object { $_.Groups[1].Value })
    Assert-EqualSet 'Stage 6 custom Feature registry IDs' @($stage6.features) @($stage6.features | Where-Object { $_ -in $registeredBmFeatures })
    $validVanillaFeatureTypes = @(
        'random_boolean_selector', 'random_patch', 'random_selector', 'simple_block',
        'simple_random_selector', 'tree'
    )
    Get-ChildItem $configuredRoot -Recurse -File -Filter '*.json' | ForEach-Object {
        $raw = Get-Content $_.FullName -Raw
        $configuredJson = $raw | ConvertFrom-Json
        $relative = $_.FullName.Substring($configuredRoot.Length + 1).Replace('\', '/').Replace('.json', '')
        $type = [string]$configuredJson.type
        if ($type -like 'biomemakeover:*') {
            $featureId = $type.Substring('biomemakeover:'.Length)
            if ($featureId -notin $registeredBmFeatures) {
                Add-Failure "Configured feature $relative references unregistered BM Feature type $type"
            }
        } elseif ($type -like 'minecraft:*') {
            $featureId = $type.Substring('minecraft:'.Length)
            if ($featureId -notin $validVanillaFeatureTypes) {
                Add-Failure "Configured feature $relative uses unvalidated Minecraft 1.21.10 Feature type $type"
            }
        } else {
            Add-Failure "Configured feature $relative has invalid/unqualified Feature type $type"
        }
        if ($raw -match '"type"\s*:\s*"minecraft:uniform"\s*,\s*"value"\s*:') {
            Add-Failure "Obsolete nested uniform IntProvider in $($_.FullName.Substring($RepositoryRoot.Length + 1))"
        }
    }
}

$placedRoot = Join-Path $builtData 'biomemakeover/worldgen/placed_feature'
if (Test-Path $placedRoot) {
    $packagedConfiguredIds = Resource-Ids 'build/resources/main/data/biomemakeover/worldgen/configured_feature'
    Get-ChildItem $placedRoot -Recurse -File -Filter '*.json' | ForEach-Object {
        $placedJson = Get-Content $_.FullName -Raw | ConvertFrom-Json
        $relative = $_.FullName.Substring($placedRoot.Length + 1).Replace('\', '/').Replace('.json', '')
        if ($placedJson.feature -isnot [string]) {
            Add-Failure "Placed feature $relative does not use a resolvable configured-feature registry key"
        } elseif ([string]$placedJson.feature -like 'biomemakeover:*') {
            $configuredId = ([string]$placedJson.feature).Substring('biomemakeover:'.Length)
            if ($configuredId -notin $packagedConfiguredIds) {
                Add-Failure "Placed feature $relative references missing BM configured feature $($placedJson.feature)"
            }
        }
    }
    foreach ($injection in @($stage6.biome_injections)) {
        if ([string]$injection[1] -notin @(Resource-Ids 'build/resources/main/data/biomemakeover/worldgen/placed_feature')) {
            Add-Failure "Stage 6 biome injection references missing packaged placed feature biomemakeover:$($injection[1])"
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
$clientInitializer=Get-Content (Join-Path $RepositoryRoot 'src/client/java/party/lemons/biomemakeover/client/BiomeMakeoverClient.java') -Raw
if($clientInitializer -notmatch 'shiftColor\(color,35,-10,-5\)[\s\S]{0,100}BMBlocks\.ITCHING_IVY,BMBlocks\.MOTH_BLOSSOM') { Add-Failure 'Itching Ivy and Moth Blossom omit the released shifted-foliage world tint provider' }
foreach($ivyId in @('itching_ivy','moth_blossom')) {
    $ivyDefinition=Join-Path $builtAssets "items/$ivyId.json"
    if(-not(Test-Path $ivyDefinition)) { Add-Failure "Missing $ivyId item definition"; continue }
    $ivyTints=@(((Get-Content $ivyDefinition -Raw|ConvertFrom-Json).model.tints))
    $expectedTintCount=if($ivyId -eq 'moth_blossom'){3}else{2}
    if($ivyTints.Count -ne $expectedTintCount -or @($ivyTints|Where-Object{$_.type -ne 'minecraft:constant' -or $_.value -ne -9721069}).Count) {
        Add-Failure "$ivyId item definition omits its released shifted-foliage tint contract"
    }
}
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

# Stage 7 Owl: validate the released behavior/resource contracts rather than
# accepting an entity registration with experimental or incomplete gameplay.
$owlSource=Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/entity/OwlEntity.java') -Raw
$owlRendererSource=Get-Content (Join-Path $RepositoryRoot 'src/client/java/party/lemons/biomemakeover/client/render/OwlRenderer.java') -Raw
$owlModelSource=Get-Content (Join-Path $RepositoryRoot 'src/client/java/party/lemons/biomemakeover/client/model/OwlModel.java') -Raw
$owlWorldgenSource=Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/init/BMWorldgen.java') -Raw
$owlEntitiesSource=Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/init/BMEntities.java') -Raw
if($owlEntitiesSource -notmatch '(?s)OWL\s*=.*?\.sized\(0\.7F,\s*0\.8F\).*?\.clientTrackingRange\(12\)' -or
   $owlWorldgenSource -notmatch 'BMEntities\.OWL,\s*20,\s*1,\s*4') { Add-Failure 'Owl released dimensions/tracking/spawn weight-group contract is incomplete' }
if($owlSource -notmatch 'support\.is\(Blocks\.GRASS_BLOCK\)' -or $owlSource -notmatch 'support\.is\(BlockTags\.LEAVES\)' -or
   $owlSource -notmatch 'getRawBrightness\(pos,\s*0\)\s*>\s*2' -or $owlSource -match 'isNightTime|removeWhenFarAway') { Add-Failure 'Owl must retain released grass/leaves brightness spawn contract without port-only day/night culling' }
foreach($goalContract in @('addGoal\(3, new MeleeAttackGoal\(this, 1\.0D, true\)','addGoal\(4, new FollowOwnerGoal\(this, 1\.2D, 10\.0F, 2\.0F\)','addGoal\(5, new TemptGoal','addGoal\(9, new ExtendedFlyOntoTree\(this, 1\.0D, 0\.5F\)','NonTameRandomTargetGoal')) { if($owlSource -notmatch $goalContract){Add-Failure "Owl released goal contract missing: $goalContract"} }
if($owlSource -match 'LandOnOwnersShoulderGoal') { Add-Failure 'Final 1.20.1 Owl inherited shoulder storage but did not register the vanilla shoulder-landing goal' }
if($owlSource -notmatch '(?s)protected boolean canFlyToOwner\(\).*?return true;' -or
   $owlSource -notmatch 'LandRandomPos\.getPos\(this\.mob,\s*15,\s*7\)' -or
   $owlSource -notmatch 'BlockPos\.betweenClosed\(' -or $owlSource -match 'bestDistance') { Add-Failure 'Owl released leaves-enabled owner following and first-match tree-flight selection contract is incomplete' }
if($owlSource -notmatch 'ItemTags\.WOLF_FOOD' -or $owlSource -notmatch 'Attributes\.TEMPT_RANGE, 10\.0D' -or
   $owlSource -match 'Items\.RABBIT|Items\.CHICKEN|WildPlayerCautionGoal|NightChickenHuntGoal|OwlNestBlock') { Add-Failure 'Owl released broad-meat contract or removal of experimental Owl AI is incomplete' }
if($owlSource -match 'getDefaultDimensions\s*\([^)]*\)[\s\S]{0,250}super\.getDimensions\s*\(' -or
   $owlSource -notmatch 'super\.getDefaultDimensions\s*\(pose\)') { Add-Failure 'Owl dynamic dimensions recurse through final LivingEntity.getDimensions instead of delegating to getDefaultDimensions' }
$owlItemsSource=Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/init/BMItems.java') -Raw
$owlBlocksSource=Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/init/BMBlocks.java') -Raw
if($owlItemsSource -match 'entries\.accept\(OWL_EGG\)' -or $owlBlocksSource -match 'entries\.accept\(OWL_NEST\)' -or
   (Test-Path (Join-Path $builtData 'biomemakeover/recipe/owl_nest.json'))) { Add-Failure 'Nonreleased Owl nest/egg acquisition must remain inactive while preserving registry IDs' }
if($owlRendererSource -notmatch 'RenderType\.eyes\(EYES_TEXTURE\)' -or $owlRendererSource -notmatch 'equalsIgnoreCase\("Hedwig"\)' -or
   $owlRendererSource -match 'nightEyes|blinking|isOwlSleeping' -or $owlModelSource -match 'eyelids|blinking|sleeping') { Add-Failure 'Owl released unconditional eyes/Hedwig/no-blink renderer contract is incomplete' }
$owlLayersSource=Get-Content (Join-Path $RepositoryRoot 'src/client/java/party/lemons/biomemakeover/client/model/BMModelLayers.java') -Raw
if($owlSource -notmatch 'getBreedOffspring\s*\(' -or $owlSource -match 'getBreedOffspring[\s\S]{0,600}setAge\s*\(\s*0\s*\)' -or
   $owlRendererSource -notmatch 'state\.isBaby\s*=\s*entity\.isBaby\(\)' -or
   $owlRendererSource -notmatch 'models\.getModel\(state\.isBaby\)' -or
   $owlLayersSource -notmatch 'OWL_BABY' -or
   $owlModelSource -notmatch 'BabyModelTransform' -or $owlModelSource -notmatch 'Set\.of\("head_connection"\)') { Add-Failure 'Owl offspring must retain vanilla baby age and select the released modern baby model transform' }
$owlTargetTag=Join-Path $builtData 'biomemakeover/tags/entity_type/owl_targets.json'
$owlLoot=Join-Path $builtData 'biomemakeover/loot_table/entities/owl.json'
if(-not(Test-Path $owlTargetTag)){Add-Failure 'Owl released prey entity-type tag is not packaged'}
if(-not(Test-Path $owlLoot)){Add-Failure 'Owl released entity loot table is not packaged'}else{$owlLootText=Get-Content $owlLoot -Raw;if($owlLootText -notmatch 'minecraft:feather' -or $owlLootText -notmatch 'minecraft:enchanted_count_increase' -or $owlLootText -notmatch 'minecraft:looting'){Add-Failure 'Owl feather/Looting loot contract is incomplete'}}
foreach($owlTexture in @('owl.png','owl_2.png','owl_eyes.png')){if(-not(Test-Path (Join-Path $builtAssets "textures/entity/$owlTexture"))){Add-Failure "Owl released texture missing: $owlTexture"}}

# Stage 8 reachable Rootling/Moth ecosystem contracts. These checks intentionally
# follow registrations through acquisition paths instead of equating assets with parity.
$rootlingSource=Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/entity/RootlingEntity.java') -Raw
$mothSource=Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/entity/MothEntity.java') -Raw
foreach($id in @('rootling','moth')){if($id -notin $entities){Add-Failure "Stage 8 entity registration missing: $id"};if(-not(Test-Path (Join-Path $builtData "biomemakeover/loot_table/entities/$id.json"))){Add-Failure "Stage 8 entity loot missing: $id"}}
foreach($id in @('blue_bud','brown_bud','cyan_bud','gray_bud','light_blue_bud','purple_bud','rootling_seeds','bulbus_root','roasted_bulbus_root','moth_scales','rootling_spawn_egg','moth_spawn_egg')){if($id -notin $items){Add-Failure "Stage 8 item registration missing: $id"};if(-not(Test-Path (Join-Path $builtAssets "items/$id.json"))){Add-Failure "Stage 8 item definition missing: $id"}}
if('rootling_crop' -notin $blocks -or $items -contains 'rootling_crop'){Add-Failure 'Rootling crop must be a registered no-item crop reached through rootling_seeds'}
if($rootlingSource -notmatch '600\s*\+\s*random\.nextInt\(601\)' -or $rootlingSource -notmatch 'isInWaterOrRain' -or $rootlingSource -notmatch '2\s*\+\s*random\.nextInt\(3\)'){Add-Failure 'Rootling released 600-1200 regrowth, rain/water acceleration, or 2-4 shearing contract missing'}
if($mothSource -notmatch 'getLastHurtByMob\(\)==null' -or $mothSource -notmatch 'getLightEmission\(\)>10' -or $mothSource -notmatch 'MOTH_ATTRACTIVE'){Add-Failure 'Moth final attacker-gated light/Moth Blossom attraction contract missing'}
$mothTag=Join-Path $builtData 'biomemakeover/tags/block/moth_attractive.json';if(-not(Test-Path $mothTag) -or (Get-Content $mothTag -Raw) -notmatch 'biomemakeover:moth_blossom'){Add-Failure 'Moth-attractive block tag is missing Moth Blossom'}
foreach($recipe in @('roasted_bulbus_root','roasted_bulbus_root_from_smoking','roasted_bulbus_root_from_campfire_cooking','blue_dye_from_blue_bud','brown_dye_from_brown_bud','cyan_dye_from_cyan_bud','gray_dye_from_gray_bud','light_blue_dye_from_light_blue_bud','purple_dye_from_purple_bud')){if(-not(Test-Path (Join-Path $builtData "biomemakeover/recipe/$recipe.json"))){Add-Failure "Stage 8 recipe missing: $recipe"}}
foreach($color in @('blue','brown','cyan','gray','light_blue','purple')){$recipePath=Join-Path $builtData "biomemakeover/recipe/${color}_dye_from_${color}_bud.json";if(Test-Path $recipePath){$recipe=Get-Content -Raw $recipePath|ConvertFrom-Json;if($recipe.type-ne'minecraft:crafting_shaped'-or@($recipe.pattern).Count-ne1-or$recipe.pattern[0]-ne'#'-or$recipe.key.'#'-ne"biomemakeover:${color}_bud"){Add-Failure "Stage 8 bud dye recipe lost released one-slot shaped contract: $color"}}}
if($entities -contains 'ghost' -or (Test-Path (Join-Path $builtData 'biomemakeover/recipe/phantom_membrane.json'))){Add-Failure 'Later Ectoplasm progression leaked into Stage 8'}
$stuntRecipePath=Join-Path $builtData 'biomemakeover/recipe/stunt_powder.json'
if(-not(Test-Path $stuntRecipePath)){Add-Failure 'Stage 9A Stunt Powder recipe missing'}else{$recipe=Get-Content -Raw $stuntRecipePath|ConvertFrom-Json;$ingredients=@($recipe.ingredients|ForEach-Object{if($_-is[string]){$_}else{$_.item}}|Sort-Object);if($recipe.type-ne'minecraft:crafting_shapeless'-or($ingredients -join ',')-ne'biomemakeover:bulbus_root,biomemakeover:illunite_shard'-or$recipe.result.id-ne'biomemakeover:stunt_powder'-or$recipe.result.count-ne2){Add-Failure 'Stage 9A Stunt Powder recipe differs from released contract'}}
$stuntSource=Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/item/StuntPowderItem.java') -Raw
$ageSource=Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/mixin/AgeableMobMixin.java') -Raw
foreach($required in @('instanceof Stuntable','isBaby\(\)','isAlwaysBaby\(\)','isStunted\(\)','WARPED_SPORE','15','0\.2D','consume\(1, player\)')){if($stuntSource-notmatch$required){Add-Failure "Stage 9A Stunt Powder interaction contract missing: $required"}}
foreach($required in @('bm_IsStunted','-6000','@Mixin\(AgeableMob\.class\)','method = "setAge"','ValueOutput','ValueInput','self\.setAge\(BIOMEMAKEOVER_STUNTED_AGE\)')){if($ageSource-notmatch$required){Add-Failure "Stage 9A persistent age contract missing: $required"}}
$peatSource=Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/block/PeatComposterBlock.java') -Raw
foreach($required in @('return 9;','new ItemStack\(BMBlocks\.PEAT\)','Blocks\.COMPOSTER\.defaultBlockState\(\)','side == Direction\.DOWN','BMAdvancements\.PEAT_COMPOST\.trigger')){if($peatSource-notmatch$required){Add-Failure "Stage 9A Peat Composter contract missing: $required"}}
$dripSource=(Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/mixin/PointedDripstoneBlockMixin.java') -Raw)+(Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/mixin/ComposterBlockMixin.java') -Raw)
foreach($required in @('Fluids\.WATER','ComposterBlock\.READY','scheduleTick','BMBlocks\.PEAT_COMPOSTER')){if($dripSource-notmatch$required){Add-Failure "Stage 9A drip conversion contract missing: $required"}}
if($standaloneItems-notcontains'stunt_powder'){Add-Failure 'Stage 9A Stunt Powder item is not registered'}
if($blocks-notcontains'peat_composter'){Add-Failure 'Stage 9A Peat Composter block is not registered'}
if($items-contains'peat_composter'){Add-Failure 'Stage 9A Peat Composter must not have an item registration'}
if(Test-Path(Join-Path $builtData 'biomemakeover/recipe/peat_composter.json')){Add-Failure 'Stage 9A Peat Composter must not have a recipe'}
$peatLoot=Join-Path $builtData 'biomemakeover/loot_table/blocks/peat_composter.json';if(-not(Test-Path $peatLoot)){Add-Failure 'Stage 9A Peat Composter loot missing'}elseif((Get-Content -Raw $peatLoot)-notmatch'minecraft:composter'){Add-Failure 'Stage 9A Peat Composter must break into vanilla Composter'}
$peatAdvancement=Join-Path $builtData 'biomemakeover/advancement/biomemakeover/create_peat.json';if(-not(Test-Path $peatAdvancement)){Add-Failure 'Stage 9A create_peat advancement missing'}else{$adv=Get-Content -Raw $peatAdvancement|ConvertFrom-Json;if($adv.criteria.create_peat.trigger-ne'biomemakeover:peat_compost'){Add-Failure 'Stage 9A create_peat advancement trigger differs from released contract'}}
$mixinConfig=Get-Content (Join-Path $RepositoryRoot 'src/main/resources/biomemakeover.mixins.json') -Raw
foreach($required in @('AgeableMobMixin','PointedDripstoneBlockMixin','ComposterBlockMixin')){if($mixinConfig-notmatch$required){Add-Failure "Stage 9A mixin not wired: $required"}}
foreach($forbidden in @('witch_hat','ectoplasm','poltergeist','tapestry','crude_fragment','cladded_stone','stone_golem','adjudicator','mimic')){if($items-contains$forbidden-or$entities-contains$forbidden-or$blocks-contains$forbidden){Add-Failure "Stage 10+ registry leaked into Stage 9B.2: $forbidden"}}

# Stage 9B.1: dynamic-registry definitions and exact source-level hooks for all
# ten final curses. The removed Sliding curse stays absent.
$curseSpecs=@{
 'decay_curse'=@(5,'#minecraft:enchantable/durability','any',1)
 'insomnia_curse'=@(5,'#minecraft:enchantable/armor','armor',5)
 'conductivity_curse'=@(5,'#minecraft:enchantable/armor','armor',5)
 'enfeeblement_curse'=@(5,'#minecraft:enchantable/vanishing','any',5)
 'depth_curse'=@(3,'#minecraft:enchantable/foot_armor','feet',5)
 'flammability_curse'=@(3,'#minecraft:enchantable/armor','armor',5)
 'suffocation_curse'=@(3,'#minecraft:enchantable/head_armor','head',5)
 'unwieldiness_curse'=@(3,'#biomemakeover:enchantable/unwieldiness','mainhand',5)
 'inaccuracy_curse'=@(3,'#minecraft:enchantable/bow','mainhand',5)
 'buckling_curse'=@(3,'#minecraft:enchantable/leg_armor','legs',5)
}
foreach($entry in $curseSpecs.GetEnumerator()){
 $path=Join-Path $builtData "biomemakeover/enchantment/$($entry.Key).json"
 if(-not(Test-Path $path)){Add-Failure "Stage 9B.1 enchantment definition missing: $($entry.Key)";continue}
 $definition=Get-Content $path -Raw|ConvertFrom-Json;$spec=$entry.Value
 if($definition.max_level-ne$spec[0]-or$definition.supported_items-ne$spec[1]-or$spec[2]-notin@($definition.slots)-or$definition.weight-ne$spec[3]-or
    $definition.min_cost.base-ne25-or$definition.min_cost.per_level_above_first-ne0-or$definition.max_cost.base-ne50-or$definition.max_cost.per_level_above_first-ne0){
   Add-Failure "Stage 9B.1 definition contract differs for $($entry.Key)"
 }
}
$curseTagPath=Join-Path $builtData 'minecraft/tags/enchantment/curse.json';$treasureTagPath=Join-Path $builtData 'minecraft/tags/enchantment/treasure.json'
foreach($tagPath in @($curseTagPath,$treasureTagPath)){if(-not(Test-Path $tagPath)){Add-Failure "Stage 9B.1 enchantment tag missing: $tagPath"}else{$tag=Get-Content $tagPath -Raw|ConvertFrom-Json;foreach($id in $curseSpecs.Keys){$expectedCurseId="biomemakeover:$id";if(@($tag.values)-notcontains$expectedCurseId){Add-Failure "Stage 9B.1 tag omits $expectedCurseId from $tagPath"}}}}
$curseKeys=Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/init/BMEnchantments.java') -Raw
$curseEffects=Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/item/enchantment/BMCurseEffects.java') -Raw
foreach($id in $curseSpecs.Keys){if($curseKeys-notmatch[regex]::Escape("key(`"$id`"") ){Add-Failure "Stage 9B.1 canonical ResourceKey missing: $id"}}
foreach($required in @('11000 - level \* 1000','0\.05D \* level','300\.0F / \(level \* 1\.5F\)','ticks \+ \(int\)\(ticks \* \(level / 2\.0F\)\)','distance >= 3\.0D \? distance \+ level','level \* 1\.3F')){if($curseEffects-notmatch$required){Add-Failure "Stage 9B.1 pure calculation contract missing: $required"}}
foreach($required in @(
 'for \(EquipmentSlot slot : ARMOR\)',
 'BMEnchantments\.equippedLevel\(entity, slot, BMEnchantments\.CONDUCTIVITY_CURSE\)',
 'level\.random\.nextInt\(conductivityDenominator\(conductivity\)\) == 0',
 'level\.isThundering\(\)',
 'level\.isRainingAt\(pos\)',
 'EntityType\.LIGHTNING_BOLT\.create\(level, EntitySpawnReason\.TRIGGERED\)',
 'level\.addFreshEntity\(bolt\)'
)){if($curseEffects-notmatch$required){Add-Failure "Stage 9B.1 Conductivity server path differs from final source: $required"}}
$enfeeblementDefinition=Get-Content (Join-Path $builtData 'biomemakeover/enchantment/enfeeblement_curse.json') -Raw|ConvertFrom-Json
if($enfeeblementDefinition.supported_items-ne'#minecraft:enchantable/vanishing'-or'any'-notin@($enfeeblementDefinition.slots)){Add-Failure 'Enfeeblement must retain final VANISHABLE plus every-equipment-slot activation, including held supported items'}
$suffocationMixin=Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/mixin/curse/EntityCurseMixin.java') -Raw
if($suffocationMixin-notmatch '(?s)method = "getMaxAirSupply".*?getAirSupply\(\) > maximum.*?setAirSupply\(maximum\).*?setReturnValue\(maximum\)' -or $suffocationMixin-match 'setItemSlot|onEquip|equipmentChanged'){Add-Failure 'Suffocation must clamp during the maximum-air query without a nonhistorical equipment-change callback'}
$decayedCurseSource=Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/entity/DecayedEntity.java') -Raw
if($decayedCurseSource-notmatch '(?s)BMEnchantments\.holder\(registryAccess\(\), BMEnchantments\.DECAY_CURSE\).*?shield\.enchant\(decay, 1 \+ random\.nextInt\(4\)\)'){Add-Failure 'Decayed shield must resolve canonical Decay and retain final uniform levels 1-4'}
$curseMixins=Get-Content (Join-Path $RepositoryRoot 'src/main/resources/biomemakeover.mixins.json') -Raw
foreach($required in @('curse.LivingEntityCurseMixin','curse.EntityCurseMixin','curse.BowItemCurseMixin')){if($curseMixins-notmatch[regex]::Escape($required)){Add-Failure "Stage 9B.1 mixin not wired: $required"}}
if(Test-Path(Join-Path $builtData 'biomemakeover/enchantment/sliding_curse.json')){Add-Failure 'Historical/removed Sliding curse was registered'}

# Stage 9B.2: the complete final Altar loop. These checks cover acquisition,
# inventory/process semantics, modern item components, client isolation, and
# every packaged direct resource without pretending to prove random outcomes.
if('altar'-notin$blocks-or'altar'-notin$items-or'altar'-notin$blockEntities-or'altar_cursing'-notin$sounds){Add-Failure 'Stage 9B.2 Altar registry surface is incomplete'}
$menuSource=Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/init/BMMenus.java') -Raw
if($menuSource-notmatch 'Registries\.MENU, BiomeMakeover\.id\("altar"\)' -or $menuSource-notmatch 'new MenuType<>\(AltarMenu::new'){Add-Failure 'Stage 9B.2 Altar menu registration is missing'}
$altarBlockSource=Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/block/AltarBlock.java') -Raw
$altarEntitySource=Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/block/entity/AltarBlockEntity.java') -Raw
$altarCursingSource=Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/item/AltarCursing.java') -Raw
$anvilMixinSource=Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/mixin/AnvilMenuMixin.java') -Raw
$combinerAccessorSource=Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/mixin/ItemCombinerMenuAccessor.java') -Raw
foreach($required in @('ACTIVE','WATERLOGGED','getAnalogOutputSignal','AbstractContainerMenu\.getRedstoneSignalFromBlockEntity','SimpleWaterloggedBlock','Containers\.updateNeighboursAfterDestroy','preRemoveSideEffects spills every Container')){if($altarBlockSource-notmatch$required){Add-Failure "Stage 9B.2 Altar block contract missing: $required"}}
foreach($required in @('useItemOn','Items\.WATER_BUCKET','Items\.BUCKET','InteractionResult\.PASS','triggerEvent\(BlockState state, Level level, BlockPos pos','blockEntity\.triggerEvent\(event, data\)')){if($altarBlockSource-notmatch$required){Add-Failure "Stage 9B.2 Altar water/broadcast interaction contract missing: $required"}}
if($altarBlockSource-match'Containers\.dropContents'){Add-Failure 'Stage 9B.2 Altar must not duplicate the 1.21.10 BlockEntity container spill hook'}
foreach($required in @('MAX_TIME = 300','withSize\(2','Progress','direction\.getAxis\(\) == Direction\.Axis\.Y \? new int\[\]\{0\} : new int\[\]\{1\}','BMItems\.CURSE_FUEL','Block\.popResource','setItem\(0, ItemStack\.EMPTY\)','progress = 0')){if($altarEntitySource-notmatch$required){Add-Failure "Stage 9B.2 Altar inventory/process contract missing: $required"}}
foreach($required in @('MYTHAS ENHANCEMENT','slot == 0\) return !AltarCursing\.isValidTarget\(stack\)','!stack\.is\(BMItems\.CURSE_FUEL\) \|\| !AltarCursing\.isValidTarget\(inventory\.get\(0\)\)')){if($altarEntitySource-notmatch$required){Add-Failure "Mythas Altar automation-lock contract missing: $required"}}
foreach($required in @('START_SOUND_EVENT = 1','!serverWasWorking','level\.blockEvent\(worldPosition, state\.getBlock\(\), START_SOUND_EVENT, 0\)','triggerEvent\(int event, int data\)','clientSoundStarter\.accept\(this\)')){if($altarEntitySource-notmatch$required){Add-Failure "Stage 9B.2 Altar start-sound event contract missing: $required"}}
if($altarEntitySource-match'clientWasActive'){Add-Failure 'Stage 9B.2 sound must start from the authoritative block event, not a passive client state-transition guess'}
foreach($required in @('DataComponents\.STORED_ENCHANTMENTS','DataComponents\.ENCHANTMENTS','DataComponents\.CUSTOM_DATA','BMCursed','DataComponents\.REPAIR_COST, REPAIR_COST','attempts >= RANDOM_ATTEMPTS','minimum == maximum \? minimum : random\.nextInt\(minimum, maximum\)')){if($altarCursingSource-notmatch$required){Add-Failure "Stage 9B.2 Altar cursing contract missing: $required"}}
foreach($required in @('@Mixin\(AnvilMenu\.class\)','@Shadow private boolean onlyRenaming','@Inject\(method = "createResult", at = @At\("RETURN"\)\)','hasInfiniteMaterials\(\)','AltarCursing\.hasMarker\(inputs\.getItem\(0\)\)','AltarCursing\.hasMarker\(inputs\.getItem\(1\)\)','setItem\(0, ItemStack\.EMPTY\)')){if($anvilMixinSource-notmatch$required){Add-Failure "Stage 9B.2 BMCursed anvil translation missing: $required"}}
foreach($required in @('@Mixin\(ItemCombinerMenu\.class\)','@Accessor\("player"\)','@Accessor\("inputSlots"\)','@Accessor\("resultSlots"\)')){if($combinerAccessorSource-notmatch$required){Add-Failure "Stage 9B.2 item-combiner accessor missing: $required"}}
$commonMixinConfig=Get-Content (Join-Path $RepositoryRoot 'src/main/resources/biomemakeover.mixins.json') -Raw
foreach($required in @('ItemCombinerMenuAccessor','AnvilMenuMixin')){if($commonMixinConfig-notmatch[regex]::Escape($required)){Add-Failure "Stage 9B.2 anvil mixin not wired: $required"}}
$altarRecipePath=Join-Path $builtData 'biomemakeover/recipe/altar.json'
if(-not(Test-Path $altarRecipePath)){Add-Failure 'Stage 9B.2 Altar recipe missing'}else{$altarRecipe=Get-Content $altarRecipePath -Raw|ConvertFrom-Json;if(($altarRecipe.pattern-join'/')-ne' B /ICI/MCM'-or$altarRecipe.key.B-ne'minecraft:book'-or$altarRecipe.key.I-ne'biomemakeover:illunite_shard'-or$altarRecipe.key.C-ne'minecraft:crying_obsidian'-or$altarRecipe.key.M-ne'biomemakeover:mesmerite'-or$altarRecipe.result.id-ne'biomemakeover:altar'){Add-Failure 'Stage 9B.2 Altar recipe differs from the final two-Crying-Obsidian contract'}}
$altarRecipeUnlock=Get-ChildItem (Join-Path $builtData 'biomemakeover/advancement') -Recurse -File -Filter '*.json' -ErrorAction SilentlyContinue|Where-Object{$_.FullName-replace'\\','/'-match'/recipes/.*/altar\.json$'}
if($altarRecipeUnlock){Add-Failure 'Final 1.20.1 has no Altar recipe-unlock advancement; do not invent premature recipe-book discovery'}
foreach($resource in @($stage9b2.loot_tables|ForEach-Object{"biomemakeover/loot_table/$_.json"})+@($stage9b2.advancements|ForEach-Object{"biomemakeover/advancement/$_.json"})+@($stage9b2.item_tags|ForEach-Object{"biomemakeover/tags/item/$_.json"})+@($stage9b2.enchantment_tags|ForEach-Object{"biomemakeover/tags/enchantment/$_.json"})){if(-not(Test-Path(Join-Path $builtData $resource))){Add-Failure "Stage 9B.2 packaged data resource missing: $resource"}}
foreach($texture in $stage9b2.textures){if(-not(Test-Path(Join-Path $builtAssets "textures/$texture"))){Add-Failure "Stage 9B.2 packaged texture missing: $texture"}}
foreach($asset in @('blockstates/altar.json','models/block/altar.json','models/item/altar.json','items/altar.json','sounds/altar_cursing.ogg')){if(-not(Test-Path(Join-Path $builtAssets $asset))){Add-Failure "Stage 9B.2 packaged asset missing: $asset"}}
$clientInit=Get-Content (Join-Path $RepositoryRoot 'src/client/java/party/lemons/biomemakeover/client/BiomeMakeoverClient.java') -Raw
foreach($required in @('MenuScreens\.register\(BMMenus\.ALTAR','BlockEntityRenderers\.register\(BMBlockEntities\.ALTAR','AltarBlockEntity\.setClientSoundStarter')){if($clientInit-notmatch$required){Add-Failure "Stage 9B.2 client-only registration missing: $required"}}
if((Get-ChildItem(Join-Path $RepositoryRoot 'src/main/java')-Recurse-File-Filter'*.java'|Get-Content -Raw)-match'client\.screen\.AltarScreen|client\.render\.AltarRenderer|client\.sound\.AltarCursingSound'){Add-Failure 'Stage 9B.2 client classes leaked into common Java sources'}

# Stage 10A: one native jigsaw house, its structure-owned merchant, and the
# guaranteed Button Mushrooms record. The exact template hash locks the binary
# block/entity/offer contract audited from final 1.20.1.
foreach($resource in @(
 'biomemakeover/worldgen/structure/mushroom_house.json',
 'biomemakeover/worldgen/structure_set/mushroom_houses.json',
 'biomemakeover/worldgen/template_pool/mushroom_house/house.json',
 'biomemakeover/worldgen/processor_list/mushroom_house.json',
 'biomemakeover/structure/mushroom_house/house/house_1.nbt',
 'biomemakeover/tags/worldgen/biome/has_structure/mushroom_house.json',
 'biomemakeover/tags/worldgen/biome/mushroom_fields.json',
 'biomemakeover/loot_table/mushroom_house.json',
 'biomemakeover/loot_table/entities/mushroom_trader.json',
 'biomemakeover/jukebox_song/button_mushrooms.json',
 'biomemakeover/advancement/biomemakeover/mushroom_disc.json'
)){if(-not(Test-Path(Join-Path $builtData $resource))){Add-Failure "Stage 10A packaged data resource missing: $resource"}}

$houseStructurePath=Join-Path $builtData 'biomemakeover/worldgen/structure/mushroom_house.json'
if(Test-Path $houseStructurePath){
 $houseStructure=Get-Content $houseStructurePath -Raw|ConvertFrom-Json
 if($houseStructure.type-ne'minecraft:jigsaw'-or$houseStructure.step-ne'surface_structures'-or$houseStructure.biomes-ne'#biomemakeover:has_structure/mushroom_house'-or$houseStructure.start_pool-ne'biomemakeover:mushroom_house/house'-or$houseStructure.size-ne3-or$houseStructure.max_distance_from_center-ne80-or$houseStructure.project_start_to_heightmap-ne'WORLD_SURFACE_WG'-or$houseStructure.terrain_adaptation-ne'beard_thin'-or$houseStructure.use_expansion_hack-ne$false){Add-Failure 'Stage 10A Mushroom House structure differs from the final jigsaw contract'}
}
$houseSetPath=Join-Path $builtData 'biomemakeover/worldgen/structure_set/mushroom_houses.json'
if(Test-Path $houseSetPath){$houseSet=Get-Content $houseSetPath -Raw|ConvertFrom-Json;$placement=$houseSet.placement;if($houseSet.structures.Count-ne1-or$houseSet.structures[0].structure-ne'biomemakeover:mushroom_house'-or$houseSet.structures[0].weight-ne1-or$placement.type-ne'minecraft:random_spread'-or$placement.spacing-ne12-or$placement.separation-ne6-or$placement.salt-ne6942069-or$placement.spread_type-ne'linear'){Add-Failure 'Stage 10A Mushroom House placement differs from spacing 12/separation 6/salt 6942069 linear'}}
$housePoolPath=Join-Path $builtData 'biomemakeover/worldgen/template_pool/mushroom_house/house.json'
if(Test-Path $housePoolPath){$housePool=Get-Content $housePoolPath -Raw|ConvertFrom-Json;$houseElement=$housePool.elements[0];if($housePool.fallback-ne'minecraft:village/plains/terminators'-or$housePool.elements.Count-ne1-or$houseElement.weight-ne15-or$houseElement.element.element_type-ne'minecraft:single_pool_element'-or$houseElement.element.location-ne'biomemakeover:mushroom_house/house/house_1'-or$houseElement.element.processors-ne'biomemakeover:mushroom_house'-or$houseElement.element.projection-ne'rigid'){Add-Failure 'Stage 10A Mushroom House single-template pool differs from final source'}}
$houseProcessorPath=Join-Path $builtData 'biomemakeover/worldgen/processor_list/mushroom_house.json'
if(Test-Path $houseProcessorPath){
 $houseProcessor=Get-Content $houseProcessorPath -Raw|ConvertFrom-Json
 $processor=$houseProcessor.processors[0]
 if($houseProcessor.processors.Count-ne1-or$processor.processor_type-ne'biomemakeover:replace_selection'-or$processor.target-ne$stage10a.processor.target-or$processor.output.type-ne'minecraft:weighted_state_provider'){Add-Failure 'Stage 10A local weighted flower-pot processor header differs from final semantics'}
 $actualWeights=@{};foreach($entry in @($processor.output.entries)){$actualWeights[[string]$entry.data.Name]=[int]$entry.weight}
 foreach($expected in $stage10a.processor.outputs.PSObject.Properties){if(-not$actualWeights.ContainsKey($expected.Name)-or$actualWeights[$expected.Name]-ne[int]$expected.Value){Add-Failure "Stage 10A flower-pot processor weight differs: $($expected.Name)"}}
 if($actualWeights.Count-ne@($stage10a.processor.outputs.PSObject.Properties).Count-or(@($actualWeights.Values)|Measure-Object -Sum).Sum-ne$stage10a.processor.total_weight){Add-Failure 'Stage 10A flower-pot outputs must be exactly seven states totaling weight 16'}
}
$processorJava=Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/worldgen/ReplaceSelectionProcessor.java') -Raw
foreach($required in @('RecordCodecBuilder\.mapCodec','BuiltInRegistries\.BLOCK\.byNameCodec\(\)\.fieldOf\("target"\)','BlockStateProvider\.CODEC\.fieldOf\("output"\)','current\.state\(\)\.is\(target\)','settings\.getRandom\(current\.pos\(\)\)','BMStructureProcessors\.REPLACE_SELECTION')){if($processorJava-notmatch$required){Add-Failure "Stage 10A local processor implementation missing: $required"}}
$productionSource=(Get-ChildItem (Join-Path $RepositoryRoot 'src/main') -Recurse -File|Where-Object{$_.Extension-in@('.java','.json','.mcmeta')}|Get-Content -Raw)-join"`n"
if($productionSource-match'party\.lemons\.taniwha|taniwha:'){Add-Failure 'Stage 10A restored a Taniwha runtime type/resource reference'}

$mushroomTagPath=Join-Path $builtData 'biomemakeover/tags/worldgen/biome/mushroom_fields.json'
if(Test-Path $mushroomTagPath){$mushroomTag=Get-Content $mushroomTagPath -Raw|ConvertFrom-Json;$tagEntry=$mushroomTag.values[0];if($mushroomTag.values.Count-ne1-or$tagEntry.id-ne'#c:is_mushroom'-or$tagEntry.required-ne$false){Add-Failure 'Stage 10A Mushroom Fields tag must translate final convention routing to optional #c:is_mushroom'}}
$houseTagPath=Join-Path $builtData 'biomemakeover/tags/worldgen/biome/has_structure/mushroom_house.json'
if(Test-Path $houseTagPath){$houseTag=Get-Content $houseTagPath -Raw|ConvertFrom-Json;if($houseTag.values.Count-ne1-or$houseTag.values[0]-ne'#biomemakeover:mushroom_fields'){Add-Failure 'Stage 10A structure biome tag chain is incomplete'}}

$houseTemplatePath=Join-Path $builtData 'biomemakeover/structure/mushroom_house/house/house_1.nbt'
if(Test-Path $houseTemplatePath){$templateFile=Get-Item $houseTemplatePath;$templateHash=(Get-FileHash $houseTemplatePath -Algorithm SHA256).Hash;if($templateFile.Length-ne$stage10a.template.bytes-or$templateHash-ne$stage10a.template.sha256){Add-Failure 'Stage 10A packaged house template is not the exact audited 11x10x11 final binary'}}
$legacyHouseTemplatePath=Join-Path $builtData 'biomemakeover/structures/mushroom_house/house/house_1.nbt'
if(Test-Path $legacyHouseTemplatePath){Add-Failure 'Stage 10A template is packaged under obsolete 1.20.1 structures/ instead of the 1.21.10 structure/ resource directory'}
$houseLootPath=Join-Path $builtData 'biomemakeover/loot_table/mushroom_house.json'
if(Test-Path $houseLootPath){
 $houseLoot=Get-Content $houseLootPath -Raw|ConvertFrom-Json
 if($houseLoot.type-ne'minecraft:chest'-or$houseLoot.random_sequence-ne'biomemakeover:mushroom_house'-or$houseLoot.pools.Count-ne3){Add-Failure 'Stage 10A Mushroom House loot header differs from final source'}else{
  $poolA=$houseLoot.pools[0];$poolB=$houseLoot.pools[1];$poolC=$houseLoot.pools[2]
  if($poolA.rolls-ne8-or(@($poolA.entries.name|Sort-Object)-join',')-ne'minecraft:brown_mushroom,minecraft:red_mushroom'){Add-Failure 'Stage 10A Mushroom House primary eight-roll mushroom pool differs'}
  if($poolB.rolls-ne1-or$poolB.entries.Count-ne1-or$poolB.entries[0].name-ne'biomemakeover:button_mushrooms_music_disk'){Add-Failure 'Stage 10A house chest must guarantee exactly one Button Mushrooms disc pool'}
  $expectedSecondary=@('biomemakeover:blighted_balsa_sapling','biomemakeover:cooked_glowfish','biomemakeover:glowfish','biomemakeover:glowshroom_stew','biomemakeover:green_glowshroom','biomemakeover:orange_glowshroom','biomemakeover:purple_glowshroom','biomemakeover:tall_brown_mushroom','biomemakeover:tall_red_mushroom','minecraft:mushroom_stew')|Sort-Object
  if($poolC.rolls.type-ne'minecraft:uniform'-or$poolC.rolls.min-ne5-or$poolC.rolls.max-ne9-or(@($poolC.entries.name|Sort-Object)-join',')-ne($expectedSecondary-join',')){Add-Failure 'Stage 10A Mushroom House secondary 5-9 roll loot pool differs'}
 }
}
$traderLootPath=Join-Path $builtData 'biomemakeover/loot_table/entities/mushroom_trader.json'
if(Test-Path $traderLootPath){$traderLoot=Get-Content $traderLootPath -Raw|ConvertFrom-Json;if($traderLoot.type-ne'minecraft:entity'-or$traderLoot.pools.Count-ne0){Add-Failure 'Stage 10A Mushroom Trader loot must remain empty'}}

$traderSource=Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/entity/MushroomTraderEntity.java') -Raw
$commonTradeBody=[regex]::Match($traderSource,'(?s)private static VillagerTrades\.ItemListing\[\] commonTrades\(\).*?return new VillagerTrades\.ItemListing\[\] \{(.*?)\n\s*\};').Groups[1].Value
if(([regex]::Matches($commonTradeBody,'standardTrade\(')).Count-ne$stage10a.trades.common_entries){Add-Failure 'Stage 10A offerless Mushroom Trader must retain all 23 final common listings including duplicate Glowshroom Stem weighting'}
foreach($required in @('addOffersFromItemListings\(offers, common, 5\)','stewTrades\(\)\.get\(random\.nextInt\(stewTrades\(\)\.size\(\)\)\)','standardTrade\(8, BMItems\.BUTTON_MUSHROOMS_MUSIC_DISK, 1, 4\)','removeWhenFarAway\(double distance\) \{ return false; \}','return Mob\.createMobAttributes\(\)','return false; \}\s*@Override public boolean removeWhenFarAway','BMEffects\.NOCTURNAL, 320','DataComponents\.SUSPICIOUS_STEW_EFFECTS')){if($traderSource-notmatch$required){Add-Failure "Stage 10A Mushroom Trader source contract missing: $required"}}
$worldgenSource=Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/init/BMWorldgen.java') -Raw
if($worldgenSource-match'MUSHROOM_TRADER'){Add-Failure 'Stage 10A must not re-enable free Mushroom Trader biome spawning'}
if($entitiesSource-match'(?s)SpawnPlacements\.register\(\s*MUSHROOM_TRADER'){Add-Failure 'Stage 10A must not register a natural Mushroom Trader spawn placement'}
$traderRendererPath=Join-Path $RepositoryRoot 'src/client/java/party/lemons/biomemakeover/client/render/MushroomTraderRenderer.java'
if(-not(Test-Path $traderRendererPath)){Add-Failure 'Stage 10A Mushroom Trader renderer missing'}else{$traderRenderer=Get-Content $traderRendererPath -Raw;foreach($required in @('ModelLayers\.VILLAGER','mushrooming_trader_inner\.png','mushrooming_trader_outer\.png','poseStack\.scale\(0\.9375F','CustomHeadLayer','CrossedArmsItemLayer','coloredCutoutModelCopyLayerRender')){if($traderRenderer-notmatch$required){Add-Failure "Stage 10A two-texture trader renderer missing: $required"}}}
if($clientInit-notmatch'EntityRenderers\.register\(BMEntities\.MUSHROOM_TRADER, MushroomTraderRenderer::new\)'){Add-Failure 'Stage 10A Mushroom Trader renderer is not registered client-side'}
if((Get-ChildItem(Join-Path $RepositoryRoot 'src/main/java')-Recurse-File-Filter'*.java'|Get-Content -Raw)-match'client\.render\.MushroomTraderRenderer'){Add-Failure 'Stage 10A Mushroom Trader renderer leaked into common Java'}

$discSource=Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/init/BMItems.java') -Raw
foreach($required in @('BUTTON_MUSHROOMS_SONG[\s\S]{0,180}Registries\.JUKEBOX_SONG','BUTTON_MUSHROOMS_MUSIC_DISK = register\("button_mushrooms_music_disk"','stacksTo\(1\)\.rarity\(Rarity\.RARE\)\.jukeboxPlayable\(BUTTON_MUSHROOMS_SONG\)')){if($discSource-notmatch$required){Add-Failure "Stage 10A Button Mushrooms item contract missing: $required"}}
$songPath=Join-Path $builtData 'biomemakeover/jukebox_song/button_mushrooms.json'
if(Test-Path $songPath){$song=Get-Content $songPath -Raw|ConvertFrom-Json;if($song.sound_event-ne$stage10a.disc.song-or$song.length_in_seconds-ne$stage10a.disc.length_seconds-or$song.comparator_output-ne$stage10a.disc.comparator_output-or$song.description.translate-ne'item.biomemakeover.button_mushrooms_music_disk.desc'){Add-Failure 'Stage 10A Button Mushrooms jukebox-song metadata differs from final 115-second/signal-14 contract'}}
$oggPath=Join-Path $builtAssets 'sounds/button_mushrooms.ogg'
if(Test-Path $oggPath){$ogg=Get-Item $oggPath;$oggHash=(Get-FileHash $oggPath -Algorithm SHA256).Hash;if($ogg.Length-ne$stage10a.disc.ogg_bytes-or$oggHash-ne$stage10a.disc.ogg_sha256){Add-Failure 'Stage 10A Button Mushrooms OGG differs from the exact final source asset'}}
$soundsJson=Get-Content (Join-Path $builtAssets 'sounds.json') -Raw|ConvertFrom-Json
$discSound=$soundsJson.button_mushrooms;if($null-eq$discSound-or$discSound.sounds.Count-ne1-or$discSound.sounds[0].name-ne'biomemakeover:button_mushrooms'-or$discSound.sounds[0].stream-ne$true){Add-Failure 'Stage 10A Button Mushrooms sound must be one streamed, non-looping original asset'}
foreach($asset in @('models/item/button_mushrooms_music_disk.json','items/button_mushrooms_music_disk.json','textures/item/music_disk_button_mushrooms.png','models/item/mushroom_trader_spawn_egg.json','items/mushroom_trader_spawn_egg.json','textures/item/mushroom_trader_spawn_egg.png','textures/entity/mushrooming_trader_inner.png','textures/entity/mushrooming_trader_outer.png')){if(-not(Test-Path(Join-Path $builtAssets $asset))){Add-Failure "Stage 10A packaged asset missing: $asset"}}
$discAdvancementPath=Join-Path $builtData 'biomemakeover/advancement/biomemakeover/mushroom_disc.json'
if(Test-Path $discAdvancementPath){$discAdvancement=Get-Content $discAdvancementPath -Raw|ConvertFrom-Json;if($discAdvancement.parent-ne'biomemakeover:biomemakeover/enter_mushroom_fields'-or$discAdvancement.display.icon.id-ne'biomemakeover:button_mushrooms_music_disk'-or$discAdvancement.display.frame-ne'goal'-or$discAdvancement.display.show_toast-ne$true-or$discAdvancement.display.announce_to_chat-ne$true-or$discAdvancement.display.hidden-ne$false-or$discAdvancement.criteria.get_disc.trigger-ne'minecraft:inventory_changed'-or$discAdvancement.criteria.get_disc.conditions.items[0].items-ne'biomemakeover:button_mushrooms_music_disk'){Add-Failure 'Stage 10A Button Mushrooms advancement differs from final inventory-changed goal'}}
$creeperDiscTag=Join-Path $builtData 'minecraft/tags/item/creeper_drop_music_discs.json'
if((Test-Path $creeperDiscTag) -and (Get-Content $creeperDiscTag -Raw)-match'button_mushrooms_music_disk'){Add-Failure 'Stage 10A disc must not enter vanilla Creeper music-disc drops'}

# Resolve every custom curse hook against the actual named Minecraft classes
# used by Loom, then verify the production JAR contains the expected remapped
# intermediary selectors. This catches inherited-method owner mistakes that
# Java compilation alone cannot detect.
$minecraftCommonJar=Get-ChildItem (Join-Path $RepositoryRoot '.gradle/loom-cache/minecraftMaven') -Recurse -File -Filter 'minecraft-common-*.jar'|Select-Object -First 1
if($null-eq$minecraftCommonJar){Add-Failure 'Cannot locate mapped Minecraft common JAR for curse mixin target validation'}else{
 $savedErrorPreference=$ErrorActionPreference;$ErrorActionPreference='Continue'
 try{
  $entityMethods=(& javap -classpath $minecraftCommonJar.FullName -p -s net.minecraft.world.entity.Entity 2>$null|Out-String)
 $livingMethods=(& javap -classpath $minecraftCommonJar.FullName -p -s net.minecraft.world.entity.LivingEntity 2>$null|Out-String)
 $bowMethods=(& javap -classpath $minecraftCommonJar.FullName -p -s net.minecraft.world.item.BowItem 2>$null|Out-String)
  $anvilMethods=(& javap -classpath $minecraftCommonJar.FullName -p -s net.minecraft.world.inventory.AnvilMenu 2>$null|Out-String)
 $combinerMethods=(& javap -classpath $minecraftCommonJar.FullName -p -s net.minecraft.world.inventory.ItemCombinerMenu 2>$null|Out-String)
  $templateManagerCode=(& javap -classpath $minecraftCommonJar.FullName -p -c net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager 2>$null|Out-String)
  $jigsawPlacementCode=(& javap -classpath $minecraftCommonJar.FullName -p -c net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement 2>$null|Out-String)
 }finally{$ErrorActionPreference=$savedErrorPreference}
 foreach($contract in @(
   @('Entity.setRemainingFireTicks','public void setRemainingFireTicks\(int\);[\s\S]*?descriptor: \(I\)V',$entityMethods),
   @('Entity.updateSwimming','public void updateSwimming\(\);[\s\S]*?descriptor: \(\)V',$entityMethods),
   @('Entity.getMaxAirSupply','public int getMaxAirSupply\(\);[\s\S]*?descriptor: \(\)I',$entityMethods),
   @('LivingEntity.tick','public void tick\(\);[\s\S]*?descriptor: \(\)V',$livingMethods),
   @('LivingEntity.causeFallDamage','public boolean causeFallDamage\(double, float, net\.minecraft\.world\.damagesource\.DamageSource\);[\s\S]*?descriptor: \(DFLnet/minecraft/world/damagesource/DamageSource;\)Z',$livingMethods),
   @('LivingEntity.calculateFallDamage','protected int calculateFallDamage\(double, float\);[\s\S]*?descriptor: \(DF\)I',$livingMethods),
   @('BowItem.shootProjectile','protected void shootProjectile\([\s\S]*?descriptor: \(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/projectile/Projectile;IFFFLnet/minecraft/world/entity/LivingEntity;\)V',$bowMethods),
   @('AnvilMenu.createResult','public void createResult\(\);[\s\S]*?descriptor: \(\)V',$anvilMethods),
   @('AnvilMenu.onlyRenaming','private boolean onlyRenaming;[\s\S]*?descriptor: Z',$anvilMethods),
   @('ItemCombinerMenu.player','protected final net\.minecraft\.world\.entity\.player\.Player player;[\s\S]*?descriptor: Lnet/minecraft/world/entity/player/Player;',$combinerMethods),
   @('ItemCombinerMenu.inputSlots','protected final net\.minecraft\.world\.Container inputSlots;[\s\S]*?descriptor: Lnet/minecraft/world/Container;',$combinerMethods),
   @('ItemCombinerMenu.resultSlots','protected final net\.minecraft\.world\.inventory\.ResultContainer resultSlots;[\s\S]*?descriptor: Lnet/minecraft/world/inventory/ResultContainer;',$combinerMethods)
 )){if($contract[2]-notmatch$contract[1]){Add-Failure "Stage 9B.1 named runtime target missing or descriptor changed: $($contract[0])"}}
 if($templateManagerCode-notmatch'(?s)String structure.{0,400}String \.nbt.{0,400}FileToIdConverter\."<init>"'){
  Add-Failure 'Stage 10A cannot verify the actual 1.21.10 StructureTemplateManager structure/.nbt resource converter'
 }
 if($jigsawPlacementCode-notmatch'(?s)new\s+#\d+\s+// class net/minecraft/world/level/levelgen/structure/PoolElementStructurePiece' -or
    $jigsawPlacementCode-notmatch'(?s)method_39824.*?Lists\.newArrayList.*?List\.add.*?ifgt'){
  Add-Failure 'Stage 10A cannot verify 1.21.10 jigsaw root-piece construction before connector traversal'
 }
}
$mappingRoot=Join-Path $env:USERPROFILE '.gradle/caches/fabric-loom/1.21.10'
$mappingFile=Get-ChildItem $mappingRoot -Recurse -File -Filter 'mappings.tiny' -ErrorAction SilentlyContinue|Where-Object{$_.FullName-like'*layered*'}|Select-Object -First 1
if($null-eq$mappingFile){$warnings.Add('Intermediary mapping table unavailable; packaged selector audit remains authoritative')}else{
 $mappingText=Get-Content $mappingFile.FullName -Raw
 foreach($contract in @(
  'm\s+\(\)I\s+\S+\s+method_5748\s+getMaxAirSupply',
  'm\s+\(I\)V\s+\S+\s+method_20803\s+setRemainingFireTicks',
  'm\s+\(\)V\s+\S+\s+method_5790\s+updateSwimming',
  'm\s+\(DFL\S+;\)Z\s+\S+\s+method_5747\s+causeFallDamage',
  'm\s+\(DF\)I\s+\S+\s+method_23329\s+calculateFallDamage',
  'm\s+\(L\S+;L\S+;IFFFL\S+;\)V\s+\S+\s+method_7763\s+shootProjectile',
  'm\s+\(\)V\s+\S+\s+method_24928\s+createResult',
  'f\s+Z\s+\S+\s+field_52566\s+onlyRenaming',
  'f\s+L\S+;\s+\S+\s+field_22482\s+player',
  'f\s+L\S+;\s+\S+\s+field_22480\s+inputSlots',
  'f\s+L\S+;\s+\S+\s+field_22479\s+resultSlots'
 )){if($mappingText-notmatch$contract){Add-Failure "Stage 9B.1 intermediary mapping contract missing: $contract"}}
}
$productionJar=Get-ChildItem (Join-Path $RepositoryRoot 'build/libs') -File -Filter 'biomemakeover-fabric-*.jar'|Where-Object{$_.Name-notlike'*-sources.jar'}|Select-Object -First 1
if($null-ne$productionJar){
 Add-Type -AssemblyName System.IO.Compression.FileSystem
 $zip=[IO.Compression.ZipFile]::OpenRead($productionJar.FullName)
 try{
  $selectorContracts=@{
   'party/lemons/biomemakeover/mixin/curse/EntityCurseMixin.class'=@('method_20803','method_5790','method_5748')
   'party/lemons/biomemakeover/mixin/curse/LivingEntityCurseMixin.class'=@('method_5773','method_5747','method_23329')
   'party/lemons/biomemakeover/mixin/curse/BowItemCurseMixin.class'=@('method_7763')
   'party/lemons/biomemakeover/mixin/AnvilMenuMixin.class'=@('method_24928','field_52566')
   'party/lemons/biomemakeover/mixin/ItemCombinerMenuAccessor.class'=@('field_22482','field_22480','field_22479')
  }
  foreach($classPath in $selectorContracts.Keys){$entry=$zip.GetEntry($classPath);if($null-eq$entry){Add-Failure "Packaged curse mixin class missing: $classPath";continue};$stream=$entry.Open();try{$memory=[IO.MemoryStream]::new();$stream.CopyTo($memory);$classText=[Text.Encoding]::ASCII.GetString($memory.ToArray())}finally{$stream.Dispose()};foreach($selector in $selectorContracts[$classPath]){if(-not$classText.Contains($selector)){Add-Failure "Packaged curse mixin selector missing: $classPath -> $selector"}}}
  foreach($entryPath in @(
   'party/lemons/biomemakeover/worldgen/ReplaceSelectionProcessor.class',
   'party/lemons/biomemakeover/entity/MushroomTraderEntity.class',
   'party/lemons/biomemakeover/client/render/MushroomTraderRenderer.class',
   'data/biomemakeover/worldgen/structure/mushroom_house.json',
   'data/biomemakeover/worldgen/structure_set/mushroom_houses.json',
   'data/biomemakeover/worldgen/template_pool/mushroom_house/house.json',
   'data/biomemakeover/worldgen/processor_list/mushroom_house.json',
   'data/biomemakeover/structure/mushroom_house/house/house_1.nbt',
   'data/biomemakeover/loot_table/mushroom_house.json',
   'data/biomemakeover/jukebox_song/button_mushrooms.json',
   'assets/biomemakeover/sounds/button_mushrooms.ogg',
   'assets/biomemakeover/textures/entity/mushrooming_trader_inner.png',
   'assets/biomemakeover/textures/entity/mushrooming_trader_outer.png'
  )){if($null-eq$zip.GetEntry($entryPath)){Add-Failure "Packaged Stage 10A entry missing: $entryPath"}}
  foreach($hashContract in @(
   @('data/biomemakeover/structure/mushroom_house/house/house_1.nbt',$stage10a.template.sha256),
   @('assets/biomemakeover/sounds/button_mushrooms.ogg',$stage10a.disc.ogg_sha256)
  )){
   $entry=$zip.GetEntry($hashContract[0]);if($null-ne$entry){$stream=$entry.Open();$algorithm=[Security.Cryptography.SHA256]::Create();try{$entryHash=([BitConverter]::ToString($algorithm.ComputeHash($stream))).Replace('-','')}finally{$algorithm.Dispose();$stream.Dispose()};if($entryHash-ne$hashContract[1]){Add-Failure "Packaged Stage 10A binary hash differs: $($hashContract[0])"}}
  }
  if($null-ne$zip.GetEntry('data/biomemakeover/structures/mushroom_house/house/house_1.nbt')){Add-Failure 'Packaged JAR retains the obsolete plural Stage 10A template path'}
 }finally{$zip.Dispose()}
}
$blackThistleSource=Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/block/BlackThistleBlock.java') -Raw
if($blackThistleSource -notmatch '(?s)entityInside\(BlockState\s+state,\s*Level\s+level,\s*BlockPos\s+pos,\s*Entity\s+entity,\s*InsideBlockEffectApplier\s+effects,\s*boolean' -or
   $blackThistleSource -notmatch 'DoubleBlockHalf\.UPPER' -or $blackThistleSource -notmatch 'MobEffects\.WEAKNESS,\s*110,\s*0' -or
   $blackThistleSource -notmatch 'entity\.getType\(\)\s*==\s*BMEntities\.OWL' -or $blackThistleSource -notmatch 'entity\.getType\(\)\s*==\s*EntityType\.BEE') { Add-Failure 'Black Thistle must retain its 1.21.10 inside-block callback and released upper-half Weakness/exclusion contract' }
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
        $required = $entry -is [string] -or $null -eq $entry.required -or [bool]$entry.required
        if ($id -like '#biomemakeover:*') {
            $target = $id.Substring('#biomemakeover:'.Length)
            $targetFile = Join-Path $builtData ("biomemakeover/tags/$tagDirectory/$target.json")
            if (-not (Test-Path $targetFile)) { Add-Failure "Missing internal tag reference $id from $($tagFile.FullName.Substring($RepositoryRoot.Length + 1))" }
        } elseif ($required -and $id -like 'biomemakeover:*' -and $registryByTagDirectory.ContainsKey($tagDirectory)) {
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
