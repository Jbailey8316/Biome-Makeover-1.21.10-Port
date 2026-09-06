[CmdletBinding()]
param([string]$Root = (Get-Location).Path)
$ErrorActionPreference = 'Stop'
$golem = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/entity/StoneGolemEntity.java') -Raw
$goal = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/entity/ai/BetterCrossbowAttackGoal.java') -Raw
$boss = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/entity/AdjudicatorEntity.java') -Raw
foreach ($needle in @('updateControlFlags()', 'getControllingPassenger()', 'AdjudicatorAlliance.allied(this, controller)', 'goalSelector.enableControlFlag(Goal.Flag.MOVE)', 'goalSelector.enableControlFlag(Goal.Flag.LOOK)', 'goalSelector.enableControlFlag(Goal.Flag.JUMP)')) {
    if ($golem -notlike "*$needle*") { throw "Stone Golem control-flag compatibility anchor missing: $needle" }
}
if ($golem -notlike '*goalSelector.addGoal(1, new BetterCrossbowAttackGoal<>(this, 1.0D, 24.0F)*') {
    throw 'Stone Golem crossbow goal registration changed'
}
foreach ($needle in @('setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK))', 'return result')) {
    if ($goal -notlike "*$needle*") { throw "Crossbow goal contract missing: $needle" }
}
if ($boss -match 'goalSelector\.enableControlFlag|disableControlFlag') { throw 'Global/controller control-flag patch detected' }
if ($goal -match 'performRangedAttack\(getTarget\(|every\s+N\s+ticks') { throw 'Manual forced firing detected' }
if ($boss -notlike '*new MountedCrossbowAttackGoal<>(this, 25.0F)*' -or $boss -notlike '*CrossbowAttackMob*') { throw 'Ravager mounted combat regressed' }
$templates = @(Get-ChildItem (Join-Path $Root 'src/main/resources/data/biomemakeover/structure/mansion') -Recurse -Filter *.nbt)
if ($templates.Count -ne 168) { throw "Expected 168 Mansion templates, found $($templates.Count)" }
Write-Output 'STAGE 12A.10-R6 CONTROL-FLAG VALIDATION PASSED'
