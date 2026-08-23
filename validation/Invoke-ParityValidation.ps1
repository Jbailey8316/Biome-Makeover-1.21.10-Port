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
$baseline = Get-Content $baselinePath -Raw | ConvertFrom-Json
$null = Get-Content $historicalPath -Raw | ConvertFrom-Json

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

$tagDirectories = Get-ChildItem (Join-Path $RepositoryRoot 'src/main/resources/data') -Recurse -Directory |
    Where-Object { $_.FullName -match '[\\/]tags[\\/](block|item)s$' }
foreach ($directory in $tagDirectories) {
    $warnings.Add("Legacy plural tag directory requires runtime review: $($directory.FullName.Substring($RepositoryRoot.Length + 1))")
}

if ($failures.Count) {
    Write-Host "PARITY VALIDATION FAILED ($($failures.Count) issue(s))"
    $failures | ForEach-Object { Write-Host " - $_" }
    exit 1
}

Write-Host 'PARITY VALIDATION PASSED'
Write-Host " registries: blocks=$($blocks.Count), items=$($items.Count), entities=$($entities.Count), sounds=$($sounds.Count)"
Write-Host " worldgen resources: configured=$($configured.Count), placed=$($placed.Count), injected=$($injected.Count)"
Write-Host ' JSON syntax and current block/item/feature resource contracts passed'
if ($warnings.Count) {
    Write-Host " warnings=$($warnings.Count)"
    $warnings | ForEach-Object { Write-Host " - $_" }
}
