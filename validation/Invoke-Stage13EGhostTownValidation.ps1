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
