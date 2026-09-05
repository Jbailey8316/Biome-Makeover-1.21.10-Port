param([string]$Root = (Join-Path $PSScriptRoot '..'))
$ErrorActionPreference = 'Stop'
$feature = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionFeature.java') -Raw
$structure = Join-Path $Root 'src/main/resources/data/biomemakeover/worldgen/structure/mansion.json'
$json = Get-Content $structure -Raw | ConvertFrom-Json
foreach ($needle in @('handleDirectionalMetadata','handleLoot','handleSpawning','generateIvy','spawner_spiders','"tapestry"')) { if ($feature.IndexOf($needle,[StringComparison]::Ordinal) -lt 0) { throw "Marker infrastructure missing: $needle" } }
if ($feature -notmatch 'ADJUDICATOR\.create|EntitySpawnReason\.STRUCTURE|finalizeSpawn') { throw 'Adjudicator substrate marker path missing' }
if ($json.details.mobs.golem_enemies.Count -ne 0) { throw 'Stone Golem marker pool must remain inert' }
foreach ($id in @('minecraft:vindicator','minecraft:evoker','minecraft:pillager','minecraft:ravager','minecraft:cow','minecraft:allay')) { if (-not ($json.details.mobs.enemies -contains $id -or $json.details.mobs.ranged_enemies -contains $id -or $json.details.mobs.ravagers -contains $id -or $json.details.mobs.cow -contains $id -or $json.details.mobs.allays -contains $id)) { throw "Expected safe entity pool missing: $id" } }
foreach ($path in @('src/main/resources/data/biomemakeover/loot_table/mansion/standard.json','src/main/resources/data/biomemakeover/loot_table/mansion/junk.json','src/main/resources/data/biomemakeover/loot_table/mansion/good.json','src/main/resources/data/biomemakeover/loot_table/mansion/dungeon.json','src/main/resources/data/biomemakeover/loot_table/mansion/dungeon_good.json','src/main/resources/data/biomemakeover/loot_table/mansion/dungeon_junk.json','src/main/resources/data/biomemakeover/loot_table/mansion/arrows.json')) { if (!(Test-Path (Join-Path $Root $path))) { throw "Loot table missing: $path" } }
Write-Output 'STAGE 11B.1 MANSION MARKER VALIDATION PASSED (released mob infrastructure and Adjudicator substrate; advanced boss phases deferred)'
