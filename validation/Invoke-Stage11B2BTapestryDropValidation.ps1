[CmdletBinding()]
param(
    [string]$Root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path,
    [string]$Jar = ''
)
$ErrorActionPreference = 'Stop'

$colors = @('white','orange','magenta','light_blue','yellow','lime','pink','gray','light_gray','cyan','purple','blue','brown','green','red','black','adjudicator')
$blocks = Join-Path $Root 'src/main/java/party/lemons/biomemakeover/init/BMBlocks.java'
$tapestryBlock = Join-Path $Root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionTapestryBlock.java'
$reference = Join-Path $Root 'reference/Biome-Makeover-1.20/common/src/main/java/party/lemons/biomemakeover/init/BMBlocks.java'
$lootRoot = Join-Path $Root 'src/main/resources/data/biomemakeover/loot_table/blocks'
foreach ($path in @($blocks,$tapestryBlock,$reference)) { if (-not (Test-Path -LiteralPath $path)) { throw "Missing drop contract source: $path" } }

$current = Get-Content $blocks -Raw
$released = Get-Content $reference -Raw
if (([regex]::Matches($current, 'mansionStandingTapestry\("')).Count -ne 17) { throw 'Expected 17 standing tapestry registrations' }
if (([regex]::Matches($current, 'mansionWallTapestry\("')).Count -ne 17) { throw 'Expected 17 wall tapestry registrations' }
if (([regex]::Matches($current, 'mansionWallTapestry\("[^\"]+",\s*[A-Z_]+_TAPESTRY\)')).Count -ne 17) { throw 'Every wall tapestry must identify its standing drop source' }
if ($current -notmatch 'overrideLootTable\(Optional\.of\(standingLoot\)\)') { throw 'Wall tapestry blocks do not inherit standing-form drops' }
if ($released -notmatch 'dropsLike\(') { throw 'Released wall dropsLike contract not present' }

foreach ($color in $colors) {
    $path = Join-Path $lootRoot "$color`_tapestry.json"
    if (-not (Test-Path -LiteralPath $path)) { throw "Missing standing loot table: $color" }
    $table = Get-Content $path -Raw | ConvertFrom-Json
    if ($table.type -ne 'minecraft:block' -or @($table.pools).Count -ne 1) { throw "Invalid block loot table: $color" }
    $entry = @($table.pools[0].entries)
    if ($entry.Count -ne 1 -or $entry[0].type -ne 'minecraft:item' -or $entry[0].name -ne "biomemakeover:${color}_tapestry") { throw "Standing drop does not resolve to its released item: $color" }
    if (-not @($table.pools[0].conditions | Where-Object condition -eq 'minecraft:survives_explosion')) { throw "Missing released survives_explosion condition: $color" }
    if (Test-Path (Join-Path $lootRoot "$color`_wall_tapestry.json")) { throw "Unexpected divergent wall loot table: $color" }
}

$blockSource = Get-Content $tapestryBlock -Raw
if ($blockSource -notmatch 'BM_TAPESTRY_DROP' -or $blockSource -notmatch 'playerDestroy' -or $blockSource -notmatch 'Block\.getDrops') { throw 'Trace-gated tapestry drop observation is missing' }
if ($blockSource -notmatch 'bm\.mansion\.trace' -or $blockSource -notmatch 'DROP_TRACE_COUNT' -or $blockSource -notmatch '16') { throw 'Tapestry drop trace is not capped and trace-gated' }

if (-not [string]::IsNullOrWhiteSpace($Jar)) {
    if (-not (Test-Path -LiteralPath $Jar)) { throw "JAR missing: $Jar" }
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $zip = [IO.Compression.ZipFile]::OpenRead((Resolve-Path $Jar))
    try {
        $names = @($zip.Entries | ForEach-Object FullName)
        foreach ($color in $colors) {
            $entry = "data/biomemakeover/loot_table/blocks/${color}_tapestry.json"
            if ($names -notcontains $entry) { throw "Compiled JAR missing standing loot table: $color" }
            if ($names -contains "data/biomemakeover/loot_table/blocks/${color}_wall_tapestry.json") { throw "Compiled JAR contains an unapproved wall loot table: $color" }
        }
        $class = $zip.GetEntry('party/lemons/biomemakeover/worldgen/mansion/MansionTapestryBlock.class')
        if ($null -eq $class) { throw 'Compiled JAR missing MansionTapestryBlock' }
        $stream = $class.Open(); $memory = New-Object IO.MemoryStream; $stream.CopyTo($memory); $stream.Dispose()
        if ([Text.Encoding]::ASCII.GetString($memory.ToArray()) -notmatch 'BM_TAPESTRY_DROP') { throw 'Compiled JAR missing BM_TAPESTRY_DROP' }
        $memory.Dispose()
    } finally { $zip.Dispose() }
}
Write-Output 'STAGE 11B.2B R.6 TAPESTRY DROP VALIDATION PASSED (17 shared standing/wall drop contracts, released loot tables, capped trace)'
