[CmdletBinding()]
param(
    [string]$Root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path,
    [string]$Jar = (Join-Path (Join-Path (Join-Path $PSScriptRoot '..') 'build/libs') 'biomemakeover-fabric-1.21.10-0.8.5.jar')
)

$ErrorActionPreference = 'Stop'
if (!(Test-Path $Jar)) { throw "Stage 13E artifact is missing: $Jar" }

# Reuse the complete packaged Ghost Town contract, but invoke it with explicit
# paths so later-stage registrations cannot trip the historical Stage 10C
# freeze gate.
$contractResult = & powershell -NoProfile -ExecutionPolicy Bypass -File `
    (Join-Path $Root 'validation/Invoke-Stage10C4Validation.ps1') -Root $Root -Jar $Jar 2>&1
if ($LASTEXITCODE -ne 0) { throw (($contractResult | Out-String).Trim()) }

Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [IO.Compression.ZipFile]::OpenRead((Resolve-Path $Jar))
try {
    $entries = @{}
    foreach ($entry in $zip.Entries) { $entries[$entry.FullName] = $entry }
    function Require-Jar([string]$path) {
        if (!$entries.ContainsKey($path)) { throw "Stage 13E packaged Ghost Town resource is missing: $path" }
    }
    $required = @(
        'data/biomemakeover/worldgen/structure/ghost_town.json',
        'data/biomemakeover/worldgen/structure_set/ghost_towns.json',
        'data/biomemakeover/worldgen/processor_list/ghosttown_building.json',
        'data/biomemakeover/worldgen/processor_list/ghosttown_roads.json',
        'data/biomemakeover/loot_table/archaeology/ghost_town.json',
        'data/biomemakeover/loot_table/archaeology/ghost_town_junk.json',
        'data/biomemakeover/loot_table/archaeology/ghost_town_horse_armor.json',
        'data/biomemakeover/loot_table/ghost_town/loot_0.json',
        'data/biomemakeover/loot_table/ghost_town/loot_1.json',
        'data/biomemakeover/loot_table/ghost_town/loot_2.json',
        'data/biomemakeover/tags/worldgen/biome/has_structure/ghost_town.json',
        'data/biomemakeover/jukebox_song/ghost_town.json',
        'assets/biomemakeover/sounds/ghost_town.ogg',
        'assets/biomemakeover/sounds.json'
    )
    foreach ($path in $required) { Require-Jar $path }

    function Read-JarJson([string]$path) {
        Require-Jar $path
        $reader = [IO.StreamReader]::new($entries[$path].Open())
        try { return $reader.ReadToEnd() | ConvertFrom-Json } finally { $reader.Dispose() }
    }
    function Read-SourceJson([string]$path) {
        return Get-Content (Join-Path $Root $path) -Raw | ConvertFrom-Json
    }
    function Assert-OneRoll([object]$table, [string]$label) {
        if (@($table.pools).Count -ne 1 -or $table.pools[0].rolls -ne 1 -or $table.pools[0].bonus_rolls -ne 0) {
            throw "$label must have exactly one pool with one roll and no bonus rolls."
        }
    }
    $archaeologyPaths = @(
        'ghost_town',
        'ghost_town_junk',
        'ghost_town_horse_armor'
    )
    foreach ($name in $archaeologyPaths) {
        $sourceTable = Read-SourceJson "src/main/resources/data/biomemakeover/loot_table/archaeology/$name.json"
        $jarTable = Read-JarJson "data/biomemakeover/loot_table/archaeology/$name.json"
        Assert-OneRoll $sourceTable "$name source archaeology table"
        Assert-OneRoll $jarTable "$name packaged archaeology table"
        if ($sourceTable.random_sequence -ne "biomemakeover:archaeology/$name" -or
            $jarTable.random_sequence -ne "biomemakeover:archaeology/$name") { throw "$name random sequence changed." }
    }
    $main = Read-SourceJson 'src/main/resources/data/biomemakeover/loot_table/archaeology/ghost_town.json'
    $mainEntries = @($main.pools[0].entries)
    if ($mainEntries.Count -ne 10) { throw 'Ghost Town archaeology must retain ten equally weighted top-level outcomes.' }
    foreach ($entry in $mainEntries) {
        if (($entry.PSObject.Properties.Name -contains 'weight') -and $entry.weight -ne 1) { throw "Ghost Town archaeology item weight changed: $($entry.name)" }
    }
    if (@($mainEntries | Where-Object { $_.type -eq 'minecraft:loot_table' -and $_.value -eq 'biomemakeover:archaeology/ghost_town_horse_armor' }).Count -ne 1 -or
        @($mainEntries | Where-Object { $_.type -eq 'minecraft:loot_table' -and $_.value -eq 'biomemakeover:archaeology/ghost_town_junk' }).Count -ne 1) {
        throw 'Ghost Town archaeology nested-table assignments changed.'
    }
    $boots = @($mainEntries | Where-Object { $_.name -eq 'minecraft:leather_boots' })
    if ($boots.Count -ne 1 -or $boots[0].weight -ne 1 -or $boots[0].functions[0].conditions[0].chance -ne 0.5 -or
        $boots[0].functions[1].damage.min -ne 0.1 -or $boots[0].functions[1].damage.max -ne 1) {
        throw 'Released leather-boots archaeology outcome changed.'
    }
    $junk = Read-SourceJson 'src/main/resources/data/biomemakeover/loot_table/archaeology/ghost_town_junk.json'
    if (@($junk.pools[0].entries | Where-Object { $_.name -eq 'minecraft:iron_chain' }).Count -ne 1 -or
        @($junk.pools[0].entries | Where-Object { $_.name -eq 'minecraft:chain' }).Count -ne 0) { throw 'Modern Ghost Town junk chain adaptation is incorrect.' }
    $horse = Read-SourceJson 'src/main/resources/data/biomemakeover/loot_table/archaeology/ghost_town_horse_armor.json'
    foreach ($expected in @(@('minecraft:leather_horse_armor',30), @('minecraft:iron_horse_armor',15), @('minecraft:golden_horse_armor',9), @('minecraft:diamond_horse_armor',1))) {
        $match = @($horse.pools[0].entries | Where-Object { $_.name -eq $expected[0] })
        if ($match.Count -ne 1 -or $match[0].weight -ne $expected[1]) { throw "Ghost Town horse-armor weight changed: $($expected[0])" }
    }
    foreach ($path in @('ghosttown_building','ghosttown_roads')) {
        $processor = Read-SourceJson "src/main/resources/data/biomemakeover/worldgen/processor_list/$path.json"
        $suspicious = @($processor.processors | Where-Object { $_.processor_type -eq 'biomemakeover:suspicious_block_replacement' })
        if ($suspicious.Count -ne 1 -or $suspicious[0].output_suspicious -ne 'biomemakeover:suspicious_red_sand' -or
            $suspicious[0].loot_table -ne 'biomemakeover:archaeology/ghost_town') { throw "Ghost Town archaeology assignment changed in $path." }
    }
    if (@($entries.Keys | Where-Object { $_ -match '^data/biomemakeover/structures/ghosttown/' }).Count -ne 0) {
        throw 'Obsolete plural Ghost Town template path is packaged.'
    }
    $templates = @($entries.Keys | Where-Object { $_ -match '^data/biomemakeover/structure/ghosttown/.+\.nbt$' })
    if ($templates.Count -ne 50) { throw "Ghost Town template inventory is $($templates.Count), expected 50." }
}
finally { $zip.Dispose() }

$mansion = & powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $Root 'validation/Invoke-Stage11AMansionInventory.ps1') -Root $Root
if (($mansion -join "`n") -notmatch 'templates=168.*active_unique=165.*orphan=3') { throw 'Mansion inventory drift detected.' }

Write-Output 'STAGE 13E GHOST TOWN VALIDATION PASSED: released structure, 50-template graph, processors, archaeology/container loot, and packaged resources verified.'
Write-Output 'Historical Stage 10C freeze checks are intentionally not used because they reject later legitimate parity registrations.'
