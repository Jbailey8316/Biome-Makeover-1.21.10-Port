[CmdletBinding()]
param(
    [string]$Root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path,
    [string]$Jar = (Join-Path (Join-Path (Join-Path $PSScriptRoot '..') 'build/libs') 'biomemakeover-fabric-1.21.10-0.8.5.jar')
)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem

$contract = Get-Content (Join-Path $Root 'validation/foundations/stage_10c4_ghost_town_contract.json') -Raw | ConvertFrom-Json
if (!(Test-Path $Jar)) { throw "Stage 10C.4 candidate JAR is missing: $Jar" }
$zip = [IO.Compression.ZipFile]::OpenRead((Resolve-Path $Jar))
try {
    $entries = @{}
    foreach ($entry in $zip.Entries) { $entries[$entry.FullName] = $entry }
    function Require-Jar([string]$path) {
        if (!$entries.ContainsKey($path)) { throw "Candidate JAR is missing $path" }
    }
    function Read-Jar([string]$path) {
        Require-Jar $path
        $reader = [IO.StreamReader]::new($entries[$path].Open())
        try { return $reader.ReadToEnd() } finally { $reader.Dispose() }
    }
    function Bytes-Jar([string]$path) {
        Require-Jar $path
        $stream = $entries[$path].Open(); $memory = [IO.MemoryStream]::new()
        try { $stream.CopyTo($memory); return $memory.ToArray() } finally { $stream.Dispose(); $memory.Dispose() }
    }
    function Hash-Bytes([byte[]]$bytes) {
        $sha = [Security.Cryptography.SHA256Managed]::new()
        try { return ([BitConverter]::ToString($sha.ComputeHash($bytes)) -replace '-', '') } finally { $sha.Dispose() }
    }
    function Require-Source([string]$path) {
        if (!(Test-Path (Join-Path $Root $path))) { throw "Source resource is missing $path" }
    }
    function Read-SourceJson([string]$path) {
        Require-Source $path
        return Get-Content (Join-Path $Root $path) -Raw | ConvertFrom-Json
    }
    function Require-JsonPair([string]$path) {
        Require-Source "src/main/resources/$path"
        Require-Jar $path
        # processResources intentionally translates legacy item fields (for
        # example advancement icon.item -> icon.id); parse both sides and let
        # the semantic checks below assert the source-effective contract.
        $null = Get-Content (Join-Path $Root "src/main/resources/$path") -Raw | ConvertFrom-Json
        $null = Read-Jar $path | ConvertFrom-Json
    }

    function Assert-GhostTownBuildingProvider([object]$building, [string]$label) {
        $fill = @($building.processors | Where-Object { $_.processor_type -eq 'biomemakeover:fill_bookshelves' })
        if ($fill.Count -ne 1) { throw "$label must contain exactly one fill_bookshelves processor" }
        $level = $fill[0].enchantment_level
        if ($level.type -ne 'minecraft:weighted_list') { throw "$label enchantment_level must use minecraft:weighted_list" }
        $distribution = @($level.distribution)
        if ($distribution.Count -ne 4) { throw "$label enchantment_level must contain four weighted providers" }
        $expected = @(
            @{ weight = 20; min = 1; max = 5 },
            @{ weight = 10; min = 3; max = 10 },
            @{ weight = 4; min = 7; max = 15 },
            @{ weight = 1; min = 20; max = 35 }
        )
        for ($i = 0; $i -lt $expected.Count; $i++) {
            $entry = $distribution[$i]
            $data = $entry.data
            if ($entry.weight -ne $expected[$i].weight -or $data.type -ne 'minecraft:uniform' -or
                $data.min_inclusive -ne $expected[$i].min -or $data.max_inclusive -ne $expected[$i].max) {
                throw "$label enchantment_level provider $i differs from the released weighted range" }
            if ($data.PSObject.Properties.Name -contains 'value') {
                throw "$label uses the obsolete nested UniformInt value object; 1.21.10 requires direct min_inclusive/max_inclusive fields" }
        }
    }

    function Assert-NoObsoleteUniformProvider([object]$node, [string]$label) {
        if ($null -eq $node) { return }
        if ($node -is [System.Array]) {
            foreach ($child in $node) { Assert-NoObsoleteUniformProvider $child $label }
            return
        }
        if ($node -is [pscustomobject]) {
            if ($node.type -eq 'minecraft:uniform' -and $node.PSObject.Properties.Name -contains 'value') {
                throw "$label contains obsolete nested UniformInt value; 1.21.10 requires direct min_inclusive/max_inclusive fields"
            }
            foreach ($property in $node.PSObject.Properties) {
                Assert-NoObsoleteUniformProvider $property.Value $label
            }
        }
    }

    # The released resource tree contains one center, seven road roots, fifteen
    # decorations, and twenty-seven houses (50 NBT files). The old audit prose
    # called this 40; the source/resource graph is authoritative.
    $templateIds = @($contract.templates)
    if ($templateIds.Count -ne 50) { throw "Stage 10C.4 contract must contain 50 released templates, found $($templateIds.Count)" }
    $templateEntries = @($entries.Keys | Where-Object { $_ -match '^data/biomemakeover/structure/ghosttown/.+\.nbt$' })
    if ($templateEntries.Count -ne 50) { throw "Candidate JAR has $($templateEntries.Count) Ghost Town templates, expected 50" }
    foreach ($id in $templateIds) {
        $path = "data/biomemakeover/structure/$id.nbt"
        Require-Source "src/main/resources/$path"
        Require-Jar $path
        $sourceHash = (Get-FileHash (Join-Path $Root "src/main/resources/$path") -Algorithm SHA256).Hash
        $jarBytes = Bytes-Jar $path
        $jarHash = Hash-Bytes $jarBytes
        if ($sourceHash -ne $jarHash) { throw "Template bytes changed for $id" }
        $bytes = Bytes-Jar $path
        if ($bytes.Length -lt 3 -or $bytes[0] -ne 0x1f -or $bytes[1] -ne 0x8b) { throw "Template $id is not a compressed NBT structure" }
    }
    if (@($entries.Keys | Where-Object { $_ -match '^data/biomemakeover/structures/' }).Count) { throw 'Obsolete plural Ghost Town template path is packaged' }

    # Core worldgen graph and exact placement contract.
    $structure = Read-SourceJson 'src/main/resources/data/biomemakeover/worldgen/structure/ghost_town.json'
    Require-JsonPair 'data/biomemakeover/worldgen/structure/ghost_town.json'
    if ($structure.type -ne 'minecraft:jigsaw' -or $structure.biomes -ne '#biomemakeover:has_structure/ghost_town' -or
        $structure.size -ne 3 -or $structure.project_start_to_heightmap -ne 'WORLD_SURFACE_WG' -or
        $structure.step -ne 'surface_structures' -or $structure.terrain_adaptation -ne 'beard_thin' -or
        $structure.max_distance_from_center -ne 80 -or $structure.start_pool -ne 'biomemakeover:ghosttown/centers') {
        throw 'Ghost Town structure contract differs from final release'
    }
    if (!$structure.use_expansion_hack -or $structure.start_height.absolute -ne 0) { throw 'Ghost Town start-height/expansion contract differs' }
    if (@($structure.spawn_overrides.monster.spawns | Where-Object { $_.type -eq 'biomemakeover:ghost' -and $_.weight -eq 150 -and $_.minCount -eq 2 -and $_.maxCount -eq 4 }).Count -ne 1) { throw 'Ghost Town Ghost spawn override contract is missing' }
    $set = Read-SourceJson 'src/main/resources/data/biomemakeover/worldgen/structure_set/ghost_towns.json'
    Require-JsonPair 'data/biomemakeover/worldgen/structure_set/ghost_towns.json'
    if ($set.placement.spacing -ne 32 -or $set.placement.separation -ne 12 -or $set.placement.salt -ne 6969 -or $set.placement.spread_type -ne 'linear') { throw 'Ghost Town structure-set placement differs' }
    if (@($set.structures | Where-Object { $_.structure -eq 'biomemakeover:ghost_town' -and $_.weight -eq 1 }).Count -ne 1) { throw 'Ghost Town structure-set reference is missing' }
    $biomeTag = Read-SourceJson 'src/main/resources/data/biomemakeover/tags/worldgen/biome/has_structure/ghost_town.json'
    if ($biomeTag.replace -ne $true -or @($biomeTag.values) -notcontains '#biomemakeover:badlands') { throw 'Ghost Town biome tag chain differs' }
    $badlandsTag = Read-SourceJson 'src/main/resources/data/biomemakeover/tags/worldgen/biome/badlands.json'
    Require-JsonPair 'data/biomemakeover/tags/worldgen/biome/badlands.json'
    if ($badlandsTag.replace -ne $false -or @($badlandsTag.values) -notcontains '#minecraft:is_badlands' -or
        @($badlandsTag.values | Where-Object { $_.id -eq '#c:badlands' -and $_.required -eq $false }).Count -ne 1) {
        throw 'Badlands biome tag chain differs from final release'
    }

    # Every pool, fallback, template and processor reference must resolve.
    $poolPaths = @('centers','roads','buildings','decoration') | ForEach-Object { "data/biomemakeover/worldgen/template_pool/ghosttown/$_.json" }
    foreach ($path in $poolPaths) { Require-JsonPair $path }
    $poolIds = @('biomemakeover:ghosttown/centers','biomemakeover:ghosttown/roads','biomemakeover:ghosttown/buildings','biomemakeover:ghosttown/decoration')
    foreach ($path in $poolPaths) {
        $pool = Read-SourceJson "src/main/resources/$path"
        if ($pool.name -notin $poolIds) { throw "Unexpected Ghost Town pool name in $path" }
        if (!$pool.fallback) { throw "Pool fallback missing in $path" }
        foreach ($element in @($pool.elements)) {
            if (!$element.element.location) { throw "Pool element location missing in $path" }
            $templatePath = "data/biomemakeover/structure/$($element.element.location.Substring($element.element.location.IndexOf(':') + 1)).nbt"
            Require-Jar $templatePath
            if ($element.element.processors -and $element.element.processors -notin @('minecraft:empty','biomemakeover:ghosttown_building','biomemakeover:ghosttown_roads')) { throw "Unknown processor reference $($element.element.processors)" }
        }
    }
    $buildingPool = Read-SourceJson 'src/main/resources/data/biomemakeover/worldgen/template_pool/ghosttown/buildings.json'
    if (@($buildingPool.elements).Count -ne 30) { throw 'Ghost Town building pool must contain 30 released elements (27 houses + 3 water towers)' }
    $centerPool = Read-SourceJson 'src/main/resources/data/biomemakeover/worldgen/template_pool/ghosttown/centers.json'
    if (@($centerPool.elements).Count -ne 7 -or @($centerPool.elements | Where-Object { $_.element.location -match '/roads/street_0[1-7]$' }).Count -ne 7) { throw 'Ghost Town must retain seven road roots' }
    foreach ($path in @('data/biomemakeover/worldgen/processor_list/ghosttown_building.json','data/biomemakeover/worldgen/processor_list/ghosttown_roads.json')) { Require-JsonPair $path }
    # Minecraft 1.21.10 UniformInt's MapCodec consumes min_inclusive and
    # max_inclusive directly.  The released 1.20.1 shape nested those fields
    # under `value`, which the modern codec interprets as a missing/invalid
    # provider. Check both source and packaged JSON so processResources cannot
    # reintroduce the runtime registry-load failure.
    Assert-GhostTownBuildingProvider (Read-SourceJson 'src/main/resources/data/biomemakeover/worldgen/processor_list/ghosttown_building.json') 'source ghosttown_building.json'
    Assert-GhostTownBuildingProvider ((Read-Jar 'data/biomemakeover/worldgen/processor_list/ghosttown_building.json') | ConvertFrom-Json) 'packaged ghosttown_building.json'
    # Audit every JSON resource in the Stage 10C.4 structure graph, not just
    # the currently failing processor, for the same old provider shape.
    $stage10c4JsonRoots = @('structure','structure_set','template_pool','processor_list')
    foreach ($rootName in $stage10c4JsonRoots) {
        $sourceRoot = Join-Path $Root "src/main/resources/data/biomemakeover/worldgen/$rootName"
        if (Test-Path $sourceRoot) {
            foreach ($jsonFile in Get-ChildItem $sourceRoot -Recurse -File -Filter '*.json') {
                Assert-NoObsoleteUniformProvider (Get-Content $jsonFile.FullName -Raw | ConvertFrom-Json) $jsonFile.FullName
            }
        }
    }
    foreach ($path in @($entries.Keys | Where-Object { $_ -match '^data/biomemakeover/worldgen/(structure|structure_set|template_pool|processor_list)/.+\.json$' })) {
        Assert-NoObsoleteUniformProvider (Read-Jar $path | ConvertFrom-Json) "packaged $path"
    }
    foreach ($processor in @('biomemakeover:ghost_town_loot','biomemakeover:fill_bookshelves','biomemakeover:suspicious_block_replacement')) {
        if (!(Select-String -LiteralPath (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/init/BMStructureProcessors.java') -Pattern ($processor.Split(':')[1]) -SimpleMatch -Quiet)) { throw "Processor registration missing $processor" }
    }

    # Active loot is dependency-safe and uses modern nested-table `value`.
    $registeredItems = @('crude_fragment','ghost_town_music_disk','refined_pottery_sherd','worker_pottery_sherd','whinny_pottery_sherd')
    $registeredItems += @(Select-String -Path (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/init/BMItems.java') -Pattern 'register\("([a-z0-9_]+)"' -AllMatches | ForEach-Object { $_.Matches | ForEach-Object { $_.Groups[1].Value } })
    $lootPaths = @($contract.loot_tables | ForEach-Object { "data/biomemakeover/loot_table/$_.json" })
    foreach ($path in $lootPaths) {
        Require-JsonPair $path
        $loot = Read-SourceJson "src/main/resources/$path"
        $json = Read-Jar $path
        $objects = @()
        function Visit([object]$node) {
            if ($null -eq $node) { return }
            if ($node -is [System.Array]) { foreach ($item in $node) { Visit $item }; return }
            if ($node -is [pscustomobject]) {
                $script:objects += $node
                foreach ($property in $node.PSObject.Properties) { Visit $property.Value }
            }
        }
        $objects = @(); Visit $loot
        foreach ($obj in $objects) {
            if ($obj.type -eq 'minecraft:item' -and $obj.name -like 'biomemakeover:*') {
                $id = [string]$obj.name.Substring(14)
                if ($id -notin $registeredItems) { throw "$path references an unregistered BM item $($obj.name)" }
            }
            if ($obj.type -eq 'minecraft:loot_table') {
                if (!$obj.value) { throw "$path nested loot entry must use modern value field" }
                $nested = "data/biomemakeover/loot_table/$($obj.value.Substring($obj.value.IndexOf(':') + 1)).json"
                Require-Jar $nested
            }
        }
    }
    $arch = Read-SourceJson 'src/main/resources/data/biomemakeover/loot_table/archaeology/ghost_town.json'
    if ($arch.type -ne 'minecraft:archaeology' -or $arch.pools[0].rolls -ne 1) { throw 'Ghost Town archaeology table header differs' }
    if (@($arch.pools[0].entries | Where-Object { $_.name -eq 'biomemakeover:crude_fragment' }).Count -ne 1) { throw 'Crude Fragment archaeology entry missing' }
    if (@($arch.pools[0].entries | Where-Object { $_.name -eq 'biomemakeover:ghost_town_music_disk' }).Count -ne 1) { throw 'Ghost Town disc archaeology entry missing' }
    if ((Get-Content (Join-Path $Root 'src/main/resources/data/biomemakeover/loot_table/archaeology/ghost_town_junk.json') -Raw) -match 'minecraft:chain') { throw 'Obsolete minecraft:chain reference remains' }
    if ((Get-Content (Join-Path $Root 'src/main/resources/data/biomemakeover/loot_table/archaeology/ghost_town_junk.json') -Raw) -notmatch 'minecraft:iron_chain') { throw 'minecraft:iron_chain migration missing' }

    # Items, disc data, pattern integration and packaged assets.
    foreach ($item in @($contract.items)) {
        if (@($registeredItems | Where-Object { $_ -eq $item }).Count -eq 0) { throw "Stage 10C.4 item registration missing: $item" }
        Require-Jar "assets/biomemakeover/items/$item.json"
        Require-Jar "assets/biomemakeover/models/item/$item.json"
    }
    foreach ($asset in @('assets/biomemakeover/sounds/ghost_town.ogg','assets/biomemakeover/sounds.json','data/biomemakeover/jukebox_song/ghost_town.json')) { Require-Jar $asset }
    $song = Read-SourceJson 'src/main/resources/data/biomemakeover/jukebox_song/ghost_town.json'
    if ($song.comparator_output -ne 15 -or $song.length_in_seconds -ne 270 -or $song.sound_event -ne 'biomemakeover:ghost_town') { throw 'Ghost Town jukebox song contract differs' }
    foreach ($pattern in @('cracked_pottery_pattern','refined_pottery_pattern','worker_pottery_pattern','whinny_pottery_pattern')) {
        if (!(Select-String -LiteralPath (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/init/BMItems.java') -Pattern $pattern -SimpleMatch -Quiet)) { throw "Decorated pot pattern missing $pattern" }
    }

    # Parent graph and modern structure predicate validation.
    $advPaths = @($contract.advancements | ForEach-Object { "data/biomemakeover/advancement/$_.json" })
    $advIds = @{}
    foreach ($path in $advPaths) { Require-JsonPair $path; $advIds["biomemakeover:$([IO.Path]::GetFileNameWithoutExtension($path))"] = $path }
    foreach ($path in $advPaths) {
        $adv = Read-SourceJson "src/main/resources/$path"
        if ($adv.parent -and $adv.parent -match '^biomemakeover:') {
            $parentPath = "data/biomemakeover/advancement/$($adv.parent.Substring(14)).json"
            if (!$entries.ContainsKey($parentPath) -and !(Test-Path (Join-Path $Root "src/main/resources/$parentPath"))) { throw "Advancement parent is unresolved: $($adv.parent)" }
        }
        $raw = Get-Content (Join-Path $Root "src/main/resources/$path") -Raw
        if ($raw -match '"structure"\s*:') { throw "Obsolete singular advancement structure field remains in $path" }
        if ($adv.criteria.PSObject.Properties.Name -contains 'ghost_town' -and $raw -notmatch '"structures"\s*:') { throw 'Ghost Town advancement lost structures predicate' }
    }
    if ((Get-Content (Join-Path $Root 'src/main/resources/data/minecraft/tags/item/music_discs.json') -Raw) -notmatch 'biomemakeover:ghost_town_music_disk') { throw 'Ghost Town disc item tag membership missing' }
    if ((Get-Content (Join-Path $Root 'src/main/resources/biomemakeover.mixins.json') -Raw) -match 'Taniwha|taniwha') { throw 'Taniwha runtime reference leaked' }
    $javaRuntimeText = (Get-ChildItem (Join-Path $Root 'src/main/java') -Recurse -File -Filter '*.java' | ForEach-Object {
        (Get-Content $_.FullName -Raw) -replace '(?m)^\s*(//|/\*|\*).*$', ''
    }) -join "`n"
    if ($javaRuntimeText -match '(?i)taniwha') { throw 'Taniwha source reference leaked' }
    Write-Output "STAGE 10C.4 PACKAGED VALIDATION PASSED (templates=$($templateIds.Count), roads=7, houses=27, decorations=15, centers=1)"
}
finally { $zip.Dispose() }
