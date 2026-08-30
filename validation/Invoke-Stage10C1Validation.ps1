[CmdletBinding()]
param([string]$Root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path)
$ErrorActionPreference = 'Stop'
function Require-Path([string]$p) { if (!(Test-Path (Join-Path $Root $p))) { throw "Missing Stage 10C.1 path: $p" } }
function Require-Text([string]$p,[string]$pattern) { $f=Join-Path $Root $p; if (!(Select-String -LiteralPath $f -Pattern $pattern -SimpleMatch -Quiet)) { throw "Missing '$pattern' in $p" } }
Require-Text 'src/main/java/party/lemons/biomemakeover/init/BMItems.java' 'ECTOPLASM'
Require-Text 'src/main/java/party/lemons/biomemakeover/init/BMEntities.java' 'EntityType<GhostEntity>'
Require-Text 'src/main/java/party/lemons/biomemakeover/init/BMEntities.java' 'GHOST_SPAWN_EGG'
Require-Text 'src/main/java/party/lemons/biomemakeover/entity/GhostEntity.java' 'Attributes.FLYING_SPEED'
Require-Text 'src/main/java/party/lemons/biomemakeover/entity/GhostEntity.java' 'implements NeutralMob'
Require-Text 'src/main/java/party/lemons/biomemakeover/entity/GhostEntity.java' 'this::isAngryAt'
Require-Text 'src/main/java/party/lemons/biomemakeover/entity/GhostEntity.java' 'new HurtByTargetGoal(this).setAlertOthers()'
Require-Text 'src/main/java/party/lemons/biomemakeover/entity/GhostEntity.java' 'ResetUniversalAngerTargetGoal'
Require-Text 'src/main/java/party/lemons/biomemakeover/entity/GhostEntity.java' 'TimeUtil.rangeOfSeconds(20, 39)'
Require-Text 'src/main/java/party/lemons/biomemakeover/entity/GhostEntity.java' 'addPersistentAngerSaveData'
Require-Text 'src/main/java/party/lemons/biomemakeover/entity/GhostEntity.java' 'readPersistentAngerSaveData'
Require-Text 'src/main/java/party/lemons/biomemakeover/entity/GhostEntity.java' 'isInvulnerableTo(ServerLevel level, DamageSource source)'
Require-Text 'src/client/java/party/lemons/biomemakeover/client/BiomeMakeoverClient.java' 'EntityRenderers.register(BMEntities.GHOST'
Require-Text 'src/client/java/party/lemons/biomemakeover/client/render/GhostRenderer.java' 'GhostModel'
Require-Text 'src/client/java/party/lemons/biomemakeover/client/model/BMModelLayers.java' 'ModelLayerLocation GHOST'
Require-Text 'src/client/java/party/lemons/biomemakeover/client/model/BMModelLayers.java' 'registerModelLayer(GHOST, GhostModel::createBodyLayer)'
Require-Text 'src/main/java/party/lemons/biomemakeover/init/BMEffects.java' 'POSSESSED'
Require-Path 'src/main/resources/data/biomemakeover/recipe/phantom_membrane.json'
Require-Path 'src/main/resources/data/biomemakeover/loot_table/entities/ghost.json'
Require-Path 'src/main/resources/data/biomemakeover/tags/damage_type/ghost_immune_to.json'
if (Test-Path (Join-Path $Root 'src/main/resources/data/biomemakeover/recipes')) { throw 'Obsolete plural recipe directory present' }
if (Test-Path (Join-Path $Root 'src/main/resources/data/biomemakeover/loot_tables/entities/ghost.json')) { throw 'Obsolete plural Ghost loot path present' }
$recipe = Get-Content (Join-Path $Root 'src/main/resources/data/biomemakeover/recipe/phantom_membrane.json') -Raw | ConvertFrom-Json
if ($recipe.type -ne 'minecraft:crafting_shapeless' -or $recipe.result.id -ne 'minecraft:phantom_membrane' -or $recipe.result.count -ne 1) { throw 'Phantom Membrane recipe header/result mismatch' }
$ids = @($recipe.ingredients | ForEach-Object { $_.item })
if (($ids | Where-Object { $_ -eq 'biomemakeover:ectoplasm' }).Count -ne 1 -or ($ids | Where-Object { $_ -eq 'biomemakeover:moth_scales' }).Count -ne 3) { throw 'Phantom Membrane recipe ingredients mismatch' }
$immune = Get-Content (Join-Path $Root 'src/main/resources/data/biomemakeover/tags/damage_type/ghost_immune_to.json') -Raw | ConvertFrom-Json
$expectedImmune = @('minecraft:lava','minecraft:in_wall','minecraft:cactus','minecraft:drown','minecraft:sweet_berry_bush','minecraft:hot_floor','minecraft:fly_into_wall','minecraft:fall')
if (((@($immune.values) | Sort-Object) -join ',') -ne (($expectedImmune | Sort-Object) -join ',')) { throw 'Ghost immunity tag differs from the exact released eight-entry blacklist' }
@('ghost_angry.ogg','ghost_charge_1.ogg','ghost_charge_2.ogg','ghost_death_1.ogg','ghost_death_2.ogg','ghost_hurt_1.ogg','ghost_hurt_2.ogg','ghost_hurt_3.ogg','ghost_idle_1.ogg','ghost_idle_2.ogg','ghost_idle_3.ogg') | ForEach-Object { Require-Path "src/main/resources/assets/biomemakeover/sounds/$_" }
Require-Path 'src/main/resources/assets/biomemakeover/textures/item/ectoplasm.png'
Require-Path 'src/main/resources/assets/biomemakeover/textures/mob_effect/possessed.png'
Require-Path 'src/main/resources/assets/biomemakeover/textures/entity/ghost.png'
if ((Get-Item (Join-Path $Root 'src/main/resources/assets/biomemakeover/textures/entity/ghost.png')).Length -le 0) { throw 'Ghost texture is empty' }
if ((Get-Item (Join-Path $Root 'src/main/resources/assets/biomemakeover/textures/mob_effect/possessed.png')).Length -le 0) { throw 'Possessed effect icon is empty' }
$packagedPossessedIcon = Join-Path $Root 'build/resources/main/assets/biomemakeover/textures/mob_effect/possessed.png'
if (!(Test-Path $packagedPossessedIcon) -or (Get-Item $packagedPossessedIcon).Length -le 0) { throw 'Packaged Possessed effect icon is missing or empty' }
Require-Text 'src/main/resources/assets/biomemakeover/items/ghost_spawn_egg.json' 'biomemakeover:item/ghost_spawn_egg'
$generatedGhostModel = Join-Path $Root 'build/resources/main/assets/biomemakeover/models/item/ghost_spawn_egg.json'
if (!(Test-Path $generatedGhostModel) -or !(Select-String -LiteralPath $generatedGhostModel -Pattern 'minecraft:item/generated' -Quiet)) { throw 'Packaged Ghost spawn-egg model is missing or not native item/generated' }
# Stage 10C.4 activates Ghost Town resources; this regression validator now
# checks the 10C.1 contract without treating the later bounded stage as leak.
$taniwha = Get-ChildItem (Join-Path $Root 'src/main/java') -Recurse -File | Select-String -Pattern 'taniwha' -SimpleMatch | Where-Object { $_.Path -match 'Ghost|Ectoplasm|Possessed' }; if ($taniwha) { throw 'Taniwha runtime dependency leaked into 10C.1 foundation' }
Write-Output 'STAGE 10C.1 VALIDATION PASSED'
