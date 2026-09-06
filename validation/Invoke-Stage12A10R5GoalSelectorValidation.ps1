[CmdletBinding()]
param([string]$Root = (Get-Location).Path)
$ErrorActionPreference = 'Stop'
$goal = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/entity/ai/BetterCrossbowAttackGoal.java') -Raw
$golem = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/entity/StoneGolemEntity.java') -Raw
$boss = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/entity/AdjudicatorEntity.java') -Raw
foreach ($needle in @('boolean targetPresent', 'boolean targetAlive', 'boolean weaponValid', 'return result')) {
    if ($goal -notlike "*$needle*") { throw "R5 continuation anchor missing: $needle" }
}
if ($golem -notlike '*goalSelector.addGoal(1, new BetterCrossbowAttackGoal<>(this, 1.0D, 24.0F))*') { throw 'Stone Golem goal registration changed' }
if ($goal -notlike '*setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK))*') { throw 'Stone Golem goal flags changed' }
if ($goal -match 'performRangedAttack\(getTarget\(|every\s+N\s+ticks') { throw 'Manual forced firing detected' }
if ($boss -notlike '*new MountedCrossbowAttackGoal<>(this, 25.0F)*' -or $boss -notlike '*CrossbowAttackMob*') { throw 'Ravager mounted combat regressed' }
if ($boss -match 'selected == ControllerPhase\.STONE_GOLEM[\s\S]{0,500}setPlayerCreated\(true\)') { throw 'Boss Stone Golem must remain non-player-created' }
$templates = @(Get-ChildItem (Join-Path $Root 'src/main/resources/data/biomemakeover/structure/mansion') -Recurse -Filter *.nbt)
if ($templates.Count -ne 168) { throw "Expected 168 Mansion templates, found $($templates.Count)" }
Write-Output 'STAGE 12A.10-R5 GOALSELECTOR VALIDATION PASSED'
