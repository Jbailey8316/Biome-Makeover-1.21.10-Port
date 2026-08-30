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
Require-Path 'src/main/resources/data/biomemakeover/loot_table/archaeology/ghost_town_junk.json'
Require-Path 'src/main/resources/data/biomemakeover/loot_table/archaeology/ghost_town_horse_armor.json'
Require-Path 'src/main/resources/data/biomemakeover/loot_table/blocks/suspicious_red_sand.json'
Require-Text 'src/main/resources/assets/biomemakeover/lang/en_us.json' 'block.biomemakeover.suspicious_red_sand'
Require-Text 'src/main/java/party/lemons/biomemakeover/init/BMBlocks.java' 'entries.accept(WILD_MUSHROOMS); entries.accept(BLACK_THISTLE); entries.accept(FOXGLOVE); entries.accept(IVY); entries.accept(ITCHING_IVY); entries.accept(MOTH_BLOSSOM); entries.accept(SUSPICIOUS_RED_SAND)'
# Stage 10C.4 intentionally activates the complete Ghost Town archaeology
# table. The 10C.2 checks below continue to protect its nested-table and item
# codec migrations without requiring the table to remain absent.
$junk = Get-Content (Join-Path $Root 'src/main/resources/data/biomemakeover/loot_table/archaeology/ghost_town_junk.json') -Raw | ConvertFrom-Json
$armor = Get-Content (Join-Path $Root 'src/main/resources/data/biomemakeover/loot_table/archaeology/ghost_town_horse_armor.json') -Raw | ConvertFrom-Json
if ($junk.type -ne 'minecraft:archaeology' -or $armor.type -ne 'minecraft:archaeology') { throw 'Nested archaeology table type mismatch' }
$junkNames = @($junk.pools[0].entries | ForEach-Object { $_.name })
if ($junkNames -contains 'minecraft:chain') { throw 'Obsolete minecraft:chain item reference remains' }
if ($junkNames -notcontains 'minecraft:iron_chain') { throw 'Modern iron_chain migration missing' }
if (Test-Path (Join-Path $Root 'src/main/resources/data/biomemakeover/structures')) { throw 'Obsolete plural structure directory leaked into Stage 10C.2' }
# Ghost Town is the next active bounded stage, so its resources are expected
# here; later-stage validators provide the scope guard for 10C.4+.
Write-Output 'STAGE 10C.2 VALIDATION PASSED'
