[CmdletBinding()]
param([string]$Root = (Get-Location).Path)
$ErrorActionPreference = 'Stop'
$entity = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/entity/StoneGolemEntity.java') -Raw
$creation = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/entity/StoneGolemCreation.java') -Raw
$entities = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/init/BMEntities.java') -Raw
$client = Get-Content (Join-Path $Root 'src/client/java/party/lemons/biomemakeover/client/BiomeMakeoverClient.java') -Raw
$layers = Get-Content (Join-Path $Root 'src/client/java/party/lemons/biomemakeover/client/model/BMModelLayers.java') -Raw
foreach ($needle in @('STONE_GOLEM', 'MobCategory.MISC', 'sized(1.6F, 2.5F)', 'clientTrackingRange(12)', 'STONE_GOLEM_SPAWN_EGG')) { if ($entities -notlike "*$needle*") { throw "Stone Golem registration missing $needle" } }
foreach ($needle in @('class StoneGolemEntity', 'AbstractGolem', 'CrossbowAttackMob', 'MAX_HEALTH, 60.0D', 'FOLLOW_RANGE, 24.0D', 'PLAYER_CREATED', 'CHARGING', 'BetterCrossbowAttackGoal', 'populateDefaultEquipmentSlots', 'EntitySpawnReason.MOB_SUMMONED', 'BM_STONE_GOLEM_PARITY_PROOF')) { if ($entity -notlike "*$needle*") { throw "Stone Golem entity contract missing $needle" } }
foreach ($needle in @('~^~', '###', '~#~', 'Blocks.CARVED_PUMPKIN', 'BMBlocks.CLADDED_STONE', 'STONE_GOLEM', 'addFreshEntityWithPassengers')) { if ($creation -notlike "*$needle*") { throw "Stone Golem creation contract missing $needle" } }
foreach ($needle in @('StoneGolemRenderer', 'BMEntities.STONE_GOLEM')) { if ($client -notlike "*$needle*") { throw "Stone Golem renderer registration missing $needle" } }
if ($layers -notlike '*STONE_GOLEM*') { throw 'Stone Golem model layer missing' }
$loot = Join-Path $Root 'src/main/resources/data/biomemakeover/loot_table/entities/stone_golem.json'
if (-not (Test-Path $loot)) { throw 'Stone Golem loot table missing' }
$recipe = Join-Path $Root 'src/main/resources/data/biomemakeover/recipe/cladded_stone.json'
if (-not (Test-Path $recipe)) { throw 'Cladded Stone recipe missing' }
$eggItem = Join-Path $Root 'src/main/resources/assets/biomemakeover/items/stone_golem_spawn_egg.json'
$eggModel = Join-Path $Root 'src/main/resources/assets/biomemakeover/models/item/stone_golem_spawn_egg.json'
$eggTexture = Join-Path $Root 'src/main/resources/assets/biomemakeover/textures/item/stone_golem_spawn_egg.png'
foreach ($path in @($eggItem,$eggModel,$eggTexture)) { if (-not (Test-Path $path)) { throw "Spawn egg resource missing: $path" } }
$recipeData = Get-Content $recipe -Raw | ConvertFrom-Json
if ($recipeData.type -ne 'minecraft:crafting_shapeless' -or $recipeData.result.id -ne 'biomemakeover:cladded_stone' -or $recipeData.result.count -ne 4) { throw 'Cladded Stone recipe differs from released contract' }
$allBoss = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/entity/AdjudicatorEntity.java') -Raw
if ($allBoss -match 'isImplementedPhase\([^)]*STONE_GOLEM') { throw 'Stone Golem phase gate was removed' }
$templates = @(Get-ChildItem (Join-Path $Root 'src/main/resources/data/biomemakeover/structure/mansion') -Recurse -Filter *.nbt)
if ($templates.Count -ne 168) { throw "Expected 168 Mansion templates, found $($templates.Count)" }
Write-Output 'STAGE 12A.9 STONE GOLEM VALIDATION PASSED (independent entity, player creation, client registration, loot, and boss phase gate)'
