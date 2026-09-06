[CmdletBinding()]
param([string]$Root = (Get-Location).Path)
$ErrorActionPreference = 'Stop'
$model = Join-Path $Root 'src/main/resources/assets/biomemakeover/models/item/enchanted_totem.json'
$definition = Join-Path $Root 'src/main/resources/assets/biomemakeover/items/enchanted_totem.json'
$texture = Join-Path $Root 'src/main/resources/assets/biomemakeover/textures/item/enchanted_totem_of_undying.png'
$entity = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/entity/AdjudicatorEntity.java') -Raw
foreach ($path in @($model, $definition, $texture)) { if (-not (Test-Path $path)) { throw "Required Totem resource missing: $path" } }
if ((Get-Content $model -Raw) -notlike '*minecraft:item/generated*' -or (Get-Content $model -Raw) -notlike '*enchanted_totem_of_undying*') { throw 'Released Totem model/texture linkage missing' }
if ((Get-Content $definition -Raw) -notlike '*"type": "minecraft:model"*' -or (Get-Content $definition -Raw) -notlike '*biomemakeover:item/enchanted_totem*') { throw '1.21.10 Totem item definition is invalid' }
if ($entity -notlike '*ControllerPhase.STONE_GOLEM*' -or $entity -notlike '*setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY)*') { throw 'Adjudicator temporary weapon cleanup anchor missing' }
if ($entity -match 'spawnAtLocation\(.*Items\.BOW') { throw 'Global/custom Bow reward path detected' }
$loot = Get-Content (Join-Path $Root 'src/main/resources/data/biomemakeover/loot_table/entities/adjudicator.json') -Raw | ConvertFrom-Json
if ($loot.pools.Count -ne 2) { throw 'Guaranteed reward pools changed' }
Write-Output 'STAGE 12A.11-R1 TOTEM SPRITE / BOW DROP VALIDATION PASSED'
