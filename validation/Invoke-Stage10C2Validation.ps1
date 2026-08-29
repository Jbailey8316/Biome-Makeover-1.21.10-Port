[CmdletBinding()]
param([string]$Root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path)
$ErrorActionPreference = 'Stop'
function Require-Path([string]$p) { if (!(Test-Path (Join-Path $Root $p))) { throw "Missing Stage 10C.2 path: $p" } }
function Require-Text([string]$p,[string]$pattern) { if (!(Select-String -LiteralPath (Join-Path $Root $p) -Pattern $pattern -SimpleMatch -Quiet)) { throw "Missing '$pattern' in $p" } }
Require-Text 'src/main/java/party/lemons/biomemakeover/init/BMBlocks.java' 'SUSPICIOUS_RED_SAND'
Require-Text 'src/main/java/party/lemons/biomemakeover/init/BMBlocks.java' 'new BrushableBlock'
Require-Path 'src/main/resources/assets/biomemakeover/blockstates/suspicious_red_sand.json'
Require-Path 'src/main/resources/assets/biomemakeover/models/item/suspicious_red_sand.json'
Require-Path 'src/main/resources/assets/biomemakeover/textures/block/suspicious_red_sand_0.png'
Require-Path 'src/main/resources/data/biomemakeover/loot_table/archaeology/ghost_town.json'
Require-Path 'src/main/resources/data/biomemakeover/loot_table/archaeology/ghost_town_junk.json'
Require-Path 'src/main/resources/data/biomemakeover/loot_table/archaeology/ghost_town_horse_armor.json'
Require-Path 'src/main/resources/data/biomemakeover/loot_table/blocks/suspicious_red_sand.json'
Require-Text 'src/main/resources/assets/biomemakeover/lang/en_us.json' 'block.biomemakeover.suspicious_red_sand'
$table = Get-Content (Join-Path $Root 'src/main/resources/data/biomemakeover/loot_table/archaeology/ghost_town.json') -Raw | ConvertFrom-Json
if ($table.type -ne 'minecraft:archaeology' -or @($table.pools).Count -ne 1) { throw 'Ghost Town archaeology table header mismatch' }
$names = @($table.pools[0].entries | ForEach-Object { $_.name })
foreach ($id in @('biomemakeover:refined_pottery_sherd','biomemakeover:worker_pottery_sherd','biomemakeover:whinny_pottery_sherd','biomemakeover:crude_fragment','biomemakeover:ghost_town_music_disk')) { if ($names -notcontains $id) { throw "Missing archaeology entry: $id" } }
if (Test-Path (Join-Path $Root 'src/main/resources/data/biomemakeover/structures')) { throw 'Obsolete plural structure directory leaked into Stage 10C.2' }
foreach ($needle in @('ectoplasm_composter','poltergeist','ghost_town/','ghosttown')) { $hits = Get-ChildItem (Join-Path $Root 'src/main/resources') -Recurse -File | Select-String -Pattern $needle -SimpleMatch; if ($hits -and $needle -ne 'ghost_town/') { throw "Deferred Stage 10C resource leaked: $needle" } }
Write-Output 'STAGE 10C.2 VALIDATION PASSED'
