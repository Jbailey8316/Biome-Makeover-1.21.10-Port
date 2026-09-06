[CmdletBinding()]
param([string]$Root = (Get-Location).Path)
$ErrorActionPreference = 'Stop'
$entity = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/entity/StoneGolemEntity.java') -Raw
$creation = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/entity/StoneGolemCreation.java') -Raw
$entities = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/init/BMEntities.java') -Raw
$client = Get-Content (Join-Path $Root 'src/client/java/party/lemons/biomemakeover/client/BiomeMakeoverClient.java') -Raw
$model = Get-Content (Join-Path $Root 'src/client/java/party/lemons/biomemakeover/client/model/StoneGolemModel.java') -Raw
$layers = Get-Content (Join-Path $Root 'src/client/java/party/lemons/biomemakeover/client/model/BMModelLayers.java') -Raw
foreach ($needle in @('STONE_GOLEM', 'MobCategory.MISC', 'sized(1.6F, 2.5F)', 'clientTrackingRange(12)', 'STONE_GOLEM_SPAWN_EGG')) { if ($entities -notlike "*$needle*") { throw "Stone Golem registration missing $needle" } }
foreach ($needle in @('class StoneGolemEntity', 'AbstractGolem', 'CrossbowAttackMob', 'MAX_HEALTH, 60.0D', 'FOLLOW_RANGE, 24.0D', 'PLAYER_CREATED', 'CHARGING', 'BetterCrossbowAttackGoal', 'populateDefaultEquipmentSlots', 'EntitySpawnReason.MOB_SUMMONED')) { if ($entity -notlike "*$needle*") { throw "Stone Golem entity contract missing $needle" } }
foreach ($needle in @('~^~', '###', '~#~', 'Blocks.CARVED_PUMPKIN', 'BMBlocks.CLADDED_STONE', 'STONE_GOLEM', 'addFreshEntityWithPassengers')) { if ($creation -notlike "*$needle*") { throw "Stone Golem creation contract missing $needle" } }
if ($client -notlike '*StoneGolemRenderer*' -or $client -notlike '*BMEntities.STONE_GOLEM*') { throw 'Stone Golem renderer registration missing' }
if ($layers -notlike '*STONE_GOLEM*') { throw 'Stone Golem model layer missing' }
if ($model -notmatch 'translateToHand') { throw 'ArmedModel hand-transform hook missing' }
$renderer = Get-Content (Join-Path $Root 'src/client/java/party/lemons/biomemakeover/client/render/StoneGolemRenderer.java') -Raw
foreach ($needle in @('StoneGolemItemLayer', 'Axis.XP.rotationDegrees(-90.0F)', 'Axis.YP.rotationDegrees(180.0F)', 'pose.translate(0.0F, 0.5F, 0.0F)', 'ItemStackRenderState', 'item.submit')) { if ($renderer -notlike "*$needle*") { throw "Released crossbow render chain missing $needle" } }
if ($entity -notmatch 'isPlayerCreated\(\) != other\.isPlayerCreated\(\)') { throw 'Released Stone Golem same-type predicate missing' }
$blocks = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/init/BMBlocks.java') -Raw
$items = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/init/BMItems.java') -Raw
if ($blocks -notmatch 'entries\.accept\(CLADDED_STONE\)') { throw 'cladded_stone is not in a Creative building-block group' }
if ($items -notmatch 'entries\.accept\(CRUDE_CLADDING\)') { throw 'crude_cladding is not in a Creative item group' }
$loot = Join-Path $Root 'src/main/resources/data/biomemakeover/loot_table/entities/stone_golem.json'
if (-not (Test-Path $loot)) { throw 'Stone Golem loot table missing' }
$recipe = Join-Path $Root 'src/main/resources/data/biomemakeover/recipe/cladded_stone.json'
if (-not (Test-Path $recipe)) { throw 'Cladded Stone recipe missing' }
$crudeRecipe = Join-Path $Root 'src/main/resources/data/biomemakeover/recipe/crude_cladding.json'
if (-not (Test-Path $crudeRecipe)) { throw 'Crude Cladding recipe missing' }
$crudeAdv = Join-Path $Root 'src/main/resources/data/biomemakeover/advancement/recipes/building_blocks/crude_cladding.json'
$claddedAdv = Join-Path $Root 'src/main/resources/data/biomemakeover/advancement/recipes/building_blocks/cladded_stone.json'
foreach ($path in @($crudeAdv,$claddedAdv)) { if (-not (Test-Path $path)) { throw "Recipe unlock advancement missing: $path" } }
$eggItem = Join-Path $Root 'src/main/resources/assets/biomemakeover/items/stone_golem_spawn_egg.json'
$eggModel = Join-Path $Root 'src/main/resources/assets/biomemakeover/models/item/stone_golem_spawn_egg.json'
$eggTexture = Join-Path $Root 'src/main/resources/assets/biomemakeover/textures/item/stone_golem_spawn_egg.png'
foreach ($path in @($eggItem,$eggModel,$eggTexture)) { if (-not (Test-Path $path)) { throw "Spawn egg resource missing: $path" } }
$recipeData = Get-Content $recipe -Raw | ConvertFrom-Json
if ($recipeData.type -ne 'minecraft:crafting_shapeless' -or $recipeData.result.id -ne 'biomemakeover:cladded_stone' -or $recipeData.result.count -ne 4) { throw 'Cladded Stone recipe differs from released contract' }
$crudeData = Get-Content $crudeRecipe -Raw | ConvertFrom-Json
if ($crudeData.type -ne 'minecraft:crafting_shaped' -or $crudeData.result.id -ne 'biomemakeover:crude_cladding' -or $crudeData.result.count -ne 1 -or (($crudeData.pattern -join ',') -ne '##,##') -or $crudeData.key.'#' -ne 'biomemakeover:crude_fragment') { throw 'Crude Cladding recipe differs from released contract' }
$crudeAdvData = Get-Content $crudeAdv -Raw | ConvertFrom-Json
$claddedAdvData = Get-Content $claddedAdv -Raw | ConvertFrom-Json
if ($crudeAdvData.rewards.recipes -notcontains 'biomemakeover:crude_cladding' -or $claddedAdvData.rewards.recipes -notcontains 'biomemakeover:cladded_stone') { throw 'Recipe unlock rewards missing or incorrect' }
if (($crudeAdvData.criteria.has_ingredient.conditions.items.items -ne 'biomemakeover:crude_fragment') -or ($claddedAdvData.criteria.has_ingredient.conditions.items.items -ne 'biomemakeover:crude_cladding')) { throw 'Recipe unlock criteria missing or incorrect' }
if ($creation -notmatch 'match\.getBlock\(1, 2, 0\)' -or $creation -notmatch 'spawn\.getY\(\) \+ \.05D') { throw 'Source-equivalent bottom-center spawn anchor missing' }
if ($entity -notmatch 'public ItemStack getProjectile\(ItemStack weapon\)' -or $entity -notmatch 'new ItemStack\(Items\.ARROW\)') { throw 'Released crossbow projectile supply missing' }
$allBoss = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/entity/AdjudicatorEntity.java') -Raw
if ($allBoss -match 'isImplementedPhase\([^)]*STONE_GOLEM') { throw 'Stone Golem phase gate was removed' }
$templates = @(Get-ChildItem (Join-Path $Root 'src/main/resources/data/biomemakeover/structure/mansion') -Recurse -Filter *.nbt)
if ($templates.Count -ne 168) { throw "Expected 168 Mansion templates, found $($templates.Count)" }
Write-Output 'STAGE 12A.9-R3 STONE GOLEM VALIDATION PASSED'
