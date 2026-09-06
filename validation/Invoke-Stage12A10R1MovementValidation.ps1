[CmdletBinding()]
param([string]$Root = (Get-Location).Path)
$ErrorActionPreference = 'Stop'
$boss = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/entity/AdjudicatorEntity.java') -Raw
$goal = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/entity/ai/NonMovingBowAttackGoal.java') -Raw
$golem = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/entity/StoneGolemEntity.java') -Raw
foreach ($needle in @('NonMovingBowAttackGoal<>(this, 12, 30)', 'getNavigation().stop()', 'getVehicle() instanceof StoneGolemEntity', 'golem.getTarget() != getTarget()')) { if ($boss -notlike "*$needle*") { throw "Mounted movement boundary missing: $needle" } }
if ($goal -match 'navigation\.moveTo') { throw 'Mounted passenger goal must not navigate' }
foreach ($needle in @('BetterCrossbowAttackGoal', 'CrossbowAttackMob', 'getProjectile(ItemStack weapon)', 'FOLLOW_RANGE, 24.0D')) { if ($golem -notlike "*$needle*") { throw "Independent Stone Golem movement/combat regressed: $needle" } }
if ($boss -match 'selected == ControllerPhase\.STONE_GOLEM[\s\S]{0,500}setPlayerCreated\(true\)') { throw 'Boss Stone Golem must remain non-player-created' }
$templates = @(Get-ChildItem (Join-Path $Root 'src/main/resources/data/biomemakeover/structure/mansion') -Recurse -Filter *.nbt)
if ($templates.Count -ne 168) { throw "Expected 168 Mansion templates, found $($templates.Count)" }
Write-Output 'STAGE 12A.10-R1 MOVEMENT VALIDATION PASSED'
