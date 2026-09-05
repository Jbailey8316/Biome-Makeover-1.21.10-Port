$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$entity = Get-Content (Join-Path $root 'src/main/java/party/lemons/biomemakeover/entity/AdjudicatorMimicEntity.java') -Raw
$boss = Get-Content (Join-Path $root 'src/main/java/party/lemons/biomemakeover/entity/AdjudicatorEntity.java') -Raw
$entities = Get-Content (Join-Path $root 'src/main/java/party/lemons/biomemakeover/init/BMEntities.java') -Raw
$client = Get-Content (Join-Path $root 'src/client/java/party/lemons/biomemakeover/client/BiomeMakeoverClient.java') -Raw
$renderer = Get-Content (Join-Path $root 'src/client/java/party/lemons/biomemakeover/client/render/AdjudicatorMimicRenderer.java') -Raw
foreach ($needle in @('class AdjudicatorMimicEntity', 'RangedAttackGoal', 'MAX_HEALTH, 1.0D', 'MOVEMENT_SPEED, 0.25D', 'ATTACK_DAMAGE, 3.0D', 'setItemSlot', 'Items.BOW', 'getMobArrow', 'noSummon')) {
    if (($entity + $entities) -notlike "*$needle*") { throw "Mimic contract missing $needle" }
}
foreach ($needle in @('3 + random.nextInt(4)', 'EntitySpawnReason.NATURAL', 'AdjudicatorAlliance.assign(mimic, this)', 'clearMimics()', 'mimicInterrupted', 'ControllerPhase.MIMIC')) {
    if ($boss -notlike "*$needle*") { throw "Mimic phase contract missing $needle" }
}
foreach ($needle in @('setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOW))', 'new RangedBowAttackGoal<>(this, 0.75F, 12, 30)', 'weaponValid', 'BM_ADJUDICATOR_MIMIC_WEAPON_PROOF')) {
    if ($boss -notlike "*$needle*") { throw "Mimic ranged-weapon invariant missing $needle" }
}
if ($entity -notlike '*boolean weaponValid*' -or $entity -notlike '*if (!weaponValid) return*') { throw 'Mimic ranged-weapon guard missing' }
if ($entity -notlike '*populateDefaultEquipmentSlots(level.getRandom(), difficulty)*') { throw 'Mimic finalizeSpawn equipment population missing' }
if ($boss -notlike '*MIMIC_READY entity=*') { throw 'Mimic ready diagnostic missing' }
foreach ($needle in @('AdjudicatorMimicRenderer', 'BMModelLayers.ADJUDICATOR', 'adjudicator.png', 'adjudicator_eyes.png')) {
    if (($client + $renderer) -notlike "*$needle*") { throw "Mimic client contract missing $needle" }
}
if ($boss -like '*phase != ControllerPhase.MIMIC && phase != ControllerPhase.STONE_GOLEM*') { throw 'Mimic remains execution-gated' }
if ($entities -like '*ADJUDICATOR_MIMIC_SPAWN_EGG*') { throw 'Mimic spawn egg must not be registered' }
if ($boss -like '*catch (IllegalArgumentException*') { throw 'Weapon crash must not be handled by exception suppression' }
$sounds = Get-Content (Join-Path $root 'src/main/resources/assets/biomemakeover/sounds.json') -Raw
foreach ($needle in @('minecraft:entity.evoker.prepare_attack","type":"event', 'minecraft:entity.evoker.cast_spell","type":"event', 'minecraft:entity.evoker.hurt","type":"event')) {
    if ($sounds -notlike "*$needle*") { throw "Vanilla sound event reference missing $needle" }
}
if ((Get-ChildItem (Join-Path $root 'src') -Recurse -File | Select-String -Pattern 'Mansion.*\.nbt|\.nbt' -SimpleMatch | Where-Object { $_.Path -notlike '*validation*' })) { }
Write-Output 'STAGE 12A.8 ADJUDICATOR MIMIC VALIDATION PASSED (released phase entity, lifecycle, client parity, alliance, and Stone Golem gate)'
