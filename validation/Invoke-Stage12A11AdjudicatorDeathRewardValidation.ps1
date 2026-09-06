[CmdletBinding()]
param([string]$Root = (Get-Location).Path)
$ErrorActionPreference = 'Stop'
$entity = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/entity/AdjudicatorEntity.java') -Raw
$items = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/init/BMItems.java') -Raw
$totem = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/item/EnchantedTotemItem.java') -Raw
$mixin = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/mixin/EnchantedTotemMixin.java') -Raw
$mixins = Get-Content (Join-Path $Root 'src/main/resources/biomemakeover.mixins.json') -Raw
$lootPath = Join-Path $Root 'src/main/resources/data/biomemakeover/loot_table/entities/adjudicator.json'
$loot = Get-Content $lootPath -Raw | ConvertFrom-Json
foreach ($needle in @('xpReward = 50', 'public void die(DamageSource source)', 'exitPhase()', 'active = false', 'bossBar.removeAllPlayers()')) {
    if ($entity -notlike "*$needle*") { throw "Adjudicator death anchor missing: $needle" }
}
foreach ($needle in @('ENCHANTED_TOTEM', 'new EnchantedTotemItem', 'p.stacksTo(1).rarity(Rarity.EPIC)')) {
    if ($items -notlike "*$needle*") { throw "Enchanted Totem registration anchor missing: $needle" }
}
foreach ($needle in @('setHealth(entity.getMaxHealth() / 2.0F)', 'entity.removeAllEffects()', 'MobEffects.REGENERATION, 500, 1', 'MobEffects.ABSORPTION, 1200, 3', 'MobEffects.FIRE_RESISTANCE, 2000, 0', 'MobEffects.RESISTANCE, 2000, 0', 'stack.shrink(1)')) {
    if ($totem -notlike "*$needle*") { throw "Released Totem effect anchor missing: $needle" }
}
foreach ($needle in @('checkTotemDeathProtection', 'BMItems.ENCHANTED_TOTEM', 'EnchantedTotemItem.activate', 'cir.setReturnValue(true)')) {
    if ($mixin -notlike "*$needle*") { throw "1.21.10 Totem hook anchor missing: $needle" }
}
if ($mixins -notlike '*EnchantedTotemMixin*') { throw 'Enchanted Totem mixin is not configured' }
if ($loot.pools.Count -ne 2) { throw 'Adjudicator loot must have exactly two guaranteed pools' }
if (@($loot.pools[0].entries.name) -notcontains 'biomemakeover:enchanted_totem' -or @($loot.pools[1].entries.name) -notcontains 'biomemakeover:adjudicator_tapestry') { throw 'Adjudicator guaranteed reward set differs from released source' }
if ($loot.pools[0].rolls -ne 1 -or $loot.pools[1].rolls -ne 1) { throw 'Adjudicator reward pools must roll exactly once' }
if ($entity -match 'spawnAtLocation\(.*enchanted_totem|spawnAtLocation\(.*adjudicator_tapestry') { throw 'Duplicate custom reward path detected' }
if (-not (Test-Path (Join-Path $Root 'src/main/resources/data/biomemakeover/advancement/biomemakeover/enchanted_totem.json'))) { throw 'Enchanted Totem advancement missing' }
if (-not (Test-Path (Join-Path $Root 'src/main/resources/data/biomemakeover/loot_table/blocks/adjudicator_tapestry.json'))) { throw 'Adjudicator Tapestry loot missing' }
$templates = @(Get-ChildItem (Join-Path $Root 'src/main/resources/data/biomemakeover/structure/mansion') -Recurse -Filter *.nbt)
if ($templates.Count -ne 168) { throw "Expected 168 Mansion templates, found $($templates.Count)" }
Write-Output 'STAGE 12A.11 DEATH / REWARD VALIDATION PASSED'
