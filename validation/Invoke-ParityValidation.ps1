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
$baseline = Get-Content $baselinePath -Raw | ConvertFrom-Json
$null = Get-Content $historicalPath -Raw | ConvertFrom-Json
$dependencyContract = Get-Content $dependencyPath -Raw | ConvertFrom-Json
$familyContract = Get-Content $familyPath -Raw | ConvertFrom-Json
$historicalFamilies = Get-Content $historicalFamilyPath -Raw | ConvertFrom-Json

foreach ($property in $baseline.registries.PSObject.Properties) {
    $values = @($property.Value)
    if ((Sorted $values).Count -ne $values.Count) { Add-Failure "Duplicate ID in baseline registry $($property.Name)" }
}

$blocks = Java-RegisterIds 'src/main/java/party/lemons/biomemakeover/init/BMBlocks.java'
$standaloneItems = Java-RegisterIds 'src/main/java/party/lemons/biomemakeover/init/BMItems.java'
$entityFileIds = Java-RegisterIds 'src/main/java/party/lemons/biomemakeover/init/BMEntities.java'
$entities = @($entityFileIds | Where-Object { $_ -notlike '*_spawn_egg' })
$spawnEggs = @($entityFileIds | Where-Object { $_ -like '*_spawn_egg' })
$items = Sorted (@($blocks) + @($standaloneItems) + @($spawnEggs))
$sounds = Java-RegisterIds 'src/main/java/party/lemons/biomemakeover/init/BMSounds.java'

Assert-EqualSet 'block registry IDs' $baseline.registries.block $blocks
Assert-EqualSet 'item registry IDs' $baseline.registries.item $items
Assert-EqualSet 'entity registry IDs' $baseline.registries.entity_type $entities
Assert-EqualSet 'sound registry IDs' $baseline.registries.sound_event $sounds

$configured = Resource-Ids 'src/main/resources/data/biomemakeover/worldgen/configured_feature'
$placed = Resource-Ids 'src/main/resources/data/biomemakeover/worldgen/placed_feature'
Assert-EqualSet 'configured feature resource IDs' $baseline.worldgen_resources.configured_feature $configured
Assert-EqualSet 'placed feature resource IDs' $baseline.worldgen_resources.placed_feature $placed

$worldgenText = Get-Content (Join-Path $RepositoryRoot 'src/main/java/party/lemons/biomemakeover/init/BMWorldgen.java') -Raw
$injected = Sorted ([regex]::Matches($worldgenText, 'BiomeMakeover\.id\("([a-z0-9_./-]+)"\)') | ForEach-Object { $_.Groups[1].Value })
Assert-EqualSet 'injected placed feature keys' $baseline.worldgen_resources.injected_placed_feature $injected

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
    foreach ($relative in @(
        "src/main/resources/assets/biomemakeover/blockstates/$block.json",
        "src/main/resources/assets/biomemakeover/items/$block.json",
        "src/main/resources/data/biomemakeover/loot_table/blocks/$block.json"
    )) {
        if (-not (Test-Path (Join-Path $RepositoryRoot $relative))) { $warnings.Add("Current baseline has no block resource (future stage assertion): $relative") }
    }
}
foreach ($item in $items) {
    $definition = Join-Path $RepositoryRoot "src/main/resources/assets/biomemakeover/items/$item.json"
    if (-not (Test-Path $definition)) { Add-Failure "Missing item definition: assets/biomemakeover/items/$item.json" }
}
foreach ($feature in $placed) {
    $json = Get-Content (Join-Path $RepositoryRoot "src/main/resources/data/biomemakeover/worldgen/placed_feature/$feature.json") -Raw | ConvertFrom-Json
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
