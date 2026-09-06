[CmdletBinding()]
param([string]$Root = (Get-Location).Path)
$ErrorActionPreference = 'Stop'
$boss = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/entity/AdjudicatorEntity.java') -Raw
$golemGoal = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/entity/ai/BetterCrossbowAttackGoal.java') -Raw
$ravagerGoal = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/entity/ai/MountedCrossbowAttackGoal.java') -Raw
foreach ($needle in @('implements RangedAttackMob, CrossbowAttackMob', 'new MountedCrossbowAttackGoal<>(this, 25.0F)', 'performCrossbowAttack(this, 2.0F)', 'phase == ControllerPhase.STONE_GOLEM', 'getProjectile(ItemStack weapon)')) {
    if ($boss -notlike "*$needle*") { throw "Mounted combat architecture missing: $needle" }
}
foreach ($needle in @('mob.setTarget(null)', 'mob.performRangedAttack(target, 1.0F)')) {
    if ($golemGoal -notlike "*$needle*") { throw "Stone Golem goal lifecycle anchor missing: $needle" }
}
foreach ($needle in @('setFlags(EnumSet.of(Flag.LOOK))', 'CrossbowItem.getChargeDuration', 'actor.performCrossbowAttack(actor, 1.0F)')) {
    if ($ravagerGoal -notlike "*$needle*") { throw "Mounted Ravager crossbow anchor missing: $needle" }
}
if ($boss -match 'every\s+N\s+ticks|performRangedAttack\(getTarget\(') { throw 'Manual phase-controller firing fallback detected' }
if ($boss -match 'selected == ControllerPhase\.STONE_GOLEM[\s\S]{0,500}setPlayerCreated\(true\)') { throw 'Boss Stone Golem must remain non-player-created' }
$templates = @(Get-ChildItem (Join-Path $Root 'src/main/resources/data/biomemakeover/structure/mansion') -Recurse -Filter *.nbt)
if ($templates.Count -ne 168) { throw "Expected 168 Mansion templates, found $($templates.Count)" }
Write-Output 'STAGE 12A.10-R3 COMBAT VALIDATION PASSED'
