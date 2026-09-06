[CmdletBinding()]
param([string]$Root = (Get-Location).Path)
$ErrorActionPreference = 'Stop'
$goal = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/entity/ai/BetterCrossbowAttackGoal.java') -Raw
$boss = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/entity/AdjudicatorEntity.java') -Raw
$golem = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/entity/StoneGolemEntity.java') -Raw
foreach ($needle in @('boolean targetPresent', 'boolean targetAlive', 'boolean weaponValid', 'return result', 'mob.performRangedAttack(target, 1.0F)')) {
    if ($goal -notlike "*$needle*") { throw "Continuation lifecycle anchor missing: $needle" }
}
if ($golem -notlike '*goalSelector.addGoal(1, new BetterCrossbowAttackGoal<>(this, 1.0D, 24.0F))*') {
    throw 'Stone Golem combat registration regression'
}
foreach ($needle in @('new MountedCrossbowAttackGoal<>(this, 25.0F)', 'implements RangedAttackMob, CrossbowAttackMob')) {
    if ($boss -notlike "*$needle*") { throw "Mounted combat regression: $needle" }
}
if ($goal -match 'every\s+N\s+ticks|performRangedAttack\(getTarget\(') { throw 'Manual projectile fallback detected' }
if ($boss -match 'setMovementSpeed|MOVEMENT_SPEED[\s\S]{0,80}StoneGolem') { throw 'Movement speed tuning detected' }
$templates = @(Get-ChildItem (Join-Path $Root 'src/main/resources/data/biomemakeover/structure/mansion') -Recurse -Filter *.nbt)
if ($templates.Count -ne 168) { throw "Expected 168 Mansion templates, found $($templates.Count)" }
Write-Output 'STAGE 12A.10-R4 CONTINUATION VALIDATION PASSED'
