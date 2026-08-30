[CmdletBinding()]
param(
    [string]$Root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path,
    [string]$Jar = (Join-Path (Join-Path (Join-Path $PSScriptRoot '..') 'build/libs') 'biomemakeover-fabric-1.21.10-0.8.5.jar')
)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem

if (!(Test-Path $Jar)) { throw "Stage 10C freeze candidate JAR is missing: $Jar" }
$zip = [IO.Compression.ZipFile]::OpenRead((Resolve-Path $Jar))
try {
    $entries = @{}
    foreach ($entry in $zip.Entries) { $entries[$entry.FullName] = $entry }
    function Require-Jar([string]$path) {
        if (!$entries.ContainsKey($path)) { throw "Stage 10C freeze JAR is missing $path" }
    }
    function Read-JarJson([string]$path) {
        Require-Jar $path
        $reader = [IO.StreamReader]::new($entries[$path].Open())
        try { return $reader.ReadToEnd() | ConvertFrom-Json } finally { $reader.Dispose() }
    }
    function Require-Source([string]$path) {
        if (!(Test-Path (Join-Path $Root $path))) { throw "Stage 10C freeze source is missing $path" }
    }

    # Cross-system graph anchors. Per-stage validators retain the detailed
    # contracts; this validator ensures the active edges remain connected in
    # the packaged candidate.
    foreach ($path in @(
        'data/biomemakeover/worldgen/structure/ghost_town.json',
        'data/biomemakeover/worldgen/structure_set/ghost_towns.json',
        'data/biomemakeover/worldgen/template_pool/ghosttown/centers.json',
        'data/biomemakeover/worldgen/template_pool/ghosttown/roads.json',
        'data/biomemakeover/worldgen/template_pool/ghosttown/buildings.json',
        'data/biomemakeover/worldgen/template_pool/ghosttown/decoration.json',
        'data/biomemakeover/worldgen/processor_list/ghosttown_building.json',
        'data/biomemakeover/worldgen/processor_list/ghosttown_roads.json',
        'data/biomemakeover/loot_table/archaeology/ghost_town.json',
        'data/biomemakeover/loot_table/archaeology/ghost_town_junk.json',
        'data/biomemakeover/loot_table/archaeology/ghost_town_horse_armor.json',
        'data/biomemakeover/jukebox_song/ghost_town.json',
        'assets/biomemakeover/sounds/ghost_town.ogg',
        'assets/biomemakeover/sounds.json',
        'data/biomemakeover/advancement/biomemakeover/ghost_town.json',
        'data/biomemakeover/advancement/biomemakeover/badlands_disc.json',
        'data/biomemakeover/advancement/biomemakeover/going_ghost.json',
        'data/biomemakeover/advancement/biomemakeover/compost_soul_soil.json',
        'data/biomemakeover/advancement/biomemakeover/poltergeist.json',
        'data/biomemakeover/loot_table/blocks/suspicious_red_sand.json'
    )) { Require-Jar $path }

    $structure = Read-JarJson 'data/biomemakeover/worldgen/structure/ghost_town.json'
    if ($structure.start_pool -ne 'biomemakeover:ghosttown/centers' -or
        $structure.size -ne 3 -or $structure.project_start_to_heightmap -ne 'WORLD_SURFACE_WG' -or
        $structure.terrain_adaptation -ne 'beard_thin' -or $structure.step -ne 'surface_structures') {
        throw 'Stage 10C freeze structure-to-pool/generation edge is invalid'
    }
    $set = Read-JarJson 'data/biomemakeover/worldgen/structure_set/ghost_towns.json'
    if ($set.placement.spacing -ne 32 -or $set.placement.separation -ne 12 -or $set.placement.salt -ne 6969) {
        throw 'Stage 10C freeze structure placement edge is invalid'
    }

    # The source and packaged graph must contain exactly the authoritative 50
    # templates, all under the singular modern structure/ path.
    $templates = @($entries.Keys | Where-Object { $_ -match '^data/biomemakeover/structure/ghosttown/.+\.nbt$' })
    if ($templates.Count -ne 50) { throw "Stage 10C freeze expected 50 Ghost Town templates, found $($templates.Count)" }
    if (@($entries.Keys | Where-Object { $_ -match '^data/biomemakeover/structures/ghosttown/' }).Count -ne 0) {
        throw 'Stage 10C freeze found obsolete plural Ghost Town template paths'
    }

    $arch = Read-JarJson 'data/biomemakeover/loot_table/archaeology/ghost_town.json'
    $archText = ($arch | ConvertTo-Json -Depth 100)
    foreach ($id in @('biomemakeover:ghost_town_music_disk','biomemakeover:crude_fragment',
                      'biomemakeover:whinny_pottery_sherd','biomemakeover:worker_pottery_sherd',
                      'biomemakeover:refined_pottery_sherd')) {
        if ($archText -notmatch [regex]::Escape($id)) { throw "Stage 10C freeze archaeology edge is missing $id" }
    }
    $junkText = (Read-JarJson 'data/biomemakeover/loot_table/archaeology/ghost_town_junk.json' | ConvertTo-Json -Depth 100)
    if ($junkText -match 'minecraft:chain' -and $junkText -notmatch 'minecraft:iron_chain') { throw 'Stage 10C freeze found obsolete minecraft:chain' }
    if ($junkText -notmatch 'minecraft:iron_chain') { throw 'Stage 10C freeze iron_chain migration is missing' }

    $song = Read-JarJson 'data/biomemakeover/jukebox_song/ghost_town.json'
    if ($song.comparator_output -ne 15) { throw 'Stage 10C freeze Ghost Town disc comparator contract is not 15' }

    # Freeze boundaries: Ghost remains structure-owned, Taniwha is not a
    # runtime dependency, and later-stage systems are not activated.
    Require-Source 'src/main/java/party/lemons/biomemakeover/init/BMWorldgen.java'
    $worldgen = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/init/BMWorldgen.java') -Raw
    if ($worldgen -match '(?i)GHOST.*addSpawn|addSpawn.*GHOST') { throw 'Stage 10C freeze detected a free Badlands Ghost spawn hook' }
    $mixin = Get-Content (Join-Path $Root 'src/main/resources/biomemakeover.mixins.json') -Raw
    if ($mixin -match '(?i)taniwha') { throw 'Stage 10C freeze detected Taniwha runtime mixin leakage' }
    $java = (Get-ChildItem (Join-Path $Root 'src/main/java') -Recurse -File -Filter '*.java' | ForEach-Object {
        (Get-Content $_.FullName -Raw) -replace '(?m)^\s*(//|/\*|\*).*$', ''
    }) -join "`n"
    if ($java -match '(?i)taniwha') { throw 'Stage 10C freeze detected Taniwha runtime source leakage' }
    foreach ($forbidden in @('witch_quest','stone_golem','adjudicator','mimic','enchanted_totem')) {
        if ($java -match "(?i)register.*$forbidden") { throw "Stage 10C freeze detected later-stage registration: $forbidden" }
    }

    Write-Output 'STAGE 10C INTEGRATED PARITY FREEZE VALIDATION PASSED (50 templates, connected graph, bounded scope)'
}
finally { $zip.Dispose() }
