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
$baseline = Get-Content $baselinePath -Raw | ConvertFrom-Json
$null = Get-Content $historicalPath -Raw | ConvertFrom-Json
$dependencyContract = Get-Content $dependencyPath -Raw | ConvertFrom-Json
$familyContract = Get-Content $familyPath -Raw | ConvertFrom-Json
$historicalFamilies = Get-Content $historicalFamilyPath -Raw | ConvertFrom-Json
$stage3 = Get-Content $stage3Path -Raw | ConvertFrom-Json

foreach ($setName in @('blocks','no_item_blocks','standalone_items','entities','spawn_eggs','configured_features','placed_features')) {
    $values = @($stage3.$setName)
    if ((Sorted $values).Count -ne $values.Count) { Add-Failure "Duplicate ID in Stage 3 contract set $setName" }
}
$overlap = @($stage3.blocks | Where-Object { $_ -in @($stage3.no_item_blocks) })
if ($overlap.Count) { Add-Failure "Stage 3 ordinary/no-item block overlap: $($overlap -join ', ')" }

foreach ($property in $baseline.registries.PSObject.Properties) {
    $values = @($property.Value)
    if ((Sorted $values).Count -ne $values.Count) { Add-Failure "Duplicate ID in baseline registry $($property.Name)" }
}

$parsedBlocks = Java-RegisterIds 'src/main/java/party/lemons/biomemakeover/init/BMBlocks.java'
$blocks = Sorted (@($parsedBlocks) + @($stage3.blocks) + @($stage3.no_item_blocks))
$standaloneItems = Java-RegisterIds 'src/main/java/party/lemons/biomemakeover/init/BMItems.java'
$entityFileIds = Java-RegisterIds 'src/main/java/party/lemons/biomemakeover/init/BMEntities.java'
$entities = @($entityFileIds | Where-Object { $_ -notlike '*_spawn_egg' })
$spawnEggs = @($entityFileIds | Where-Object { $_ -like '*_spawn_egg' })
$items = Sorted (@($blocks | Where-Object { $_ -notin @($stage3.no_item_blocks) }) + @($standaloneItems) + @($spawnEggs))
$sounds = Java-RegisterIds 'src/main/java/party/lemons/biomemakeover/init/BMSounds.java'
$stage3Java = (Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/init/BMBlocks.java') -Raw) +
    (Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/init/BMItems.java') -Raw) +
    (Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/init/BMEntities.java') -Raw)
foreach ($deferred in $stage3.deferred_released_ids) {
    if ($stage3Java -match "register(?:NoItem|SpawnEgg|Entity)?\(\s*`"$([regex]::Escape([string]$deferred))`"") {
        Add-Failure "Deferred Stage 3 ID was registered prematurely: $deferred"
    }
}

Assert-EqualSet 'block registry IDs' (Sorted (@($baseline.registries.block) + @($stage3.blocks) + @($stage3.no_item_blocks))) $blocks
Assert-EqualSet 'item registry IDs' (Sorted (@($baseline.registries.item) + @($stage3.blocks) + @($stage3.standalone_items) + @($stage3.spawn_eggs))) $items
Assert-EqualSet 'entity registry IDs' (Sorted (@($baseline.registries.entity_type) + @($stage3.entities))) $entities
Assert-EqualSet 'sound registry IDs' $baseline.registries.sound_event $sounds

$configured = Sorted (@(Resource-Ids 'src/main/resources/data/biomemakeover/worldgen/configured_feature') + @(Resource-Ids 'build/resources/main/data/biomemakeover/worldgen/configured_feature'))
$placed = Sorted (@(Resource-Ids 'src/main/resources/data/biomemakeover/worldgen/placed_feature') + @(Resource-Ids 'build/resources/main/data/biomemakeover/worldgen/placed_feature'))
Assert-EqualSet 'configured feature resource IDs' (Sorted (@($baseline.worldgen_resources.configured_feature) + @($stage3.configured_features))) $configured
Assert-EqualSet 'placed feature resource IDs' (Sorted (@($baseline.worldgen_resources.placed_feature) + @($stage3.placed_features))) $placed

$worldgenText = Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/init/BMWorldgen.java') -Raw
$injected = Sorted (@([regex]::Matches($worldgenText, 'BiomeMakeover\.id\("([a-z0-9_./-]+)"\)') | ForEach-Object { $_.Groups[1].Value }) + @($stage3.placed_features | Where-Object { $_ -ne 'mushroom_fields/blighted_balsa_checked' }))
Assert-EqualSet 'injected placed feature keys' (Sorted (@($baseline.worldgen_resources.injected_placed_feature) + @($stage3.placed_features | Where-Object { $_ -ne 'mushroom_fields/blighted_balsa_checked' }))) $injected

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
    if ($block -notin @($stage3.no_item_blocks)) { $blockResources += "src/main/resources/assets/biomemakeover/items/$block.json" }
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
Write-Host " registries: blocks=$($blocks.Count), items=$($items.Count), entities=$($entities.Count), sounds=$($sounds.Count)"
Write-Host " worldgen resources: configured=$($configured.Count), placed=$($placed.Count), injected=$($injected.Count)"
Write-Host " foundations: current_families=$(@($familyContract.families).Count), historical_owned_families=$($owners.Count), runtime_dependencies=$(@($fabricMetadata.depends.PSObject.Properties).Count)"
Write-Host ' JSON syntax, dependencies, family membership, and current block/item/feature resource contracts passed'
if ($warnings.Count) {
    Write-Host " warnings=$($warnings.Count)"
    $warnings | ForEach-Object { Write-Host " - $_" }
}
