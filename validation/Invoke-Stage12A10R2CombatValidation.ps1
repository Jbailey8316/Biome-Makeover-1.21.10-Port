[CmdletBinding()]
param([string]$Root = (Get-Location).Path)
$ErrorActionPreference = 'Stop'
$boss = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/entity/AdjudicatorEntity.java') -Raw
$golem = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/entity/StoneGolemEntity.java') -Raw
$bow = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/entity/ai/NonMovingBowAttackGoal.java') -Raw
$crossbow = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/entity/ai/BetterCrossbowAttackGoal.java') -Raw
foreach ($needle in @(
    'phase == ControllerPhase.STONE_GOLEM',
    'NonMovingBowAttackGoal<>(this, 12, 30)',
    'getNavigation().stop()',
    'startRiding(golem, true, true)',
    'AdjudicatorAlliance.assign(golem, this)',
    'new ItemStack(Items.BOW)',
    'Enchantments.PUNCH')) {
    if ($boss -notlike "*$needle*") { throw "Mounted Adjudicator combat anchor missing: $needle" }
}
foreach ($needle in @('BetterCrossbowAttackGoal', 'isHolding(Items.CROSSBOW)', 'performRangedAttack(target, 1.0F)', 'getNavigation().moveTo')) {
    if ($golem -notlike "*$needle*" -and $crossbow -notlike "*$needle*") { throw "Boss Golem combat anchor missing: $needle" }
}
foreach ($needle in @('actor.performRangedAttack(target, BowItem.getPowerForTime(useTicks))', 'setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK))')) {
    if ($bow -notlike "*$needle*") { throw "Mounted bow goal anchor missing: $needle" }
}
if ($boss -match 'phase == ControllerPhase\.MIMIC;\s*boolean weaponValid') { throw 'Mounted phase missing from ranged phase guard' }
if ($boss -match 'catch\s*\(IllegalArgumentException') { throw 'Exception-catching workaround is not permitted' }
if ($boss -match 'selected == ControllerPhase\.STONE_GOLEM[\s\S]{0,500}setPlayerCreated\(true\)') { throw 'Boss Stone Golem must remain non-player-created' }
$templates = @(Get-ChildItem (Join-Path $Root 'src/main/resources/data/biomemakeover/structure/mansion') -Recurse -Filter *.nbt)
if ($templates.Count -ne 168) { throw "Expected 168 Mansion templates, found $($templates.Count)" }
Write-Output 'STAGE 12A.10-R2 COMBAT VALIDATION PASSED'
