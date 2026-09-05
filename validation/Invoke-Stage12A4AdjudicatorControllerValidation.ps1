param([string]$Root = (Join-Path $PSScriptRoot '..'))
$ErrorActionPreference = 'Stop'
$source = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/entity/AdjudicatorEntity.java') -Raw

foreach ($required in @(
    'EntityDataAccessor<Integer> STATE',
    'EntityDataAccessor<Boolean> CHARGING',
    'EntityDataAccessor<Boolean> INVULNERABLE',
    'builder.define(STATE, 0)',
    'builder.define(CHARGING, false)',
    'builder.define(INVULNERABLE, false)',
    'TELEPORT_PHASE_TICKS = 30',
    'ATTACK_PHASE_TICKS = 200',
    'FANG_BARRAGE_PHASE_TICKS = 100',
    'SUMMON_PHASE_TICKS = 120',
    'new ServerBossEvent',
    'bossBar.addPlayer(player)',
    'bossBar.removePlayer(player)',
    'bossBar.setProgress',
    'EntitySelector.LIVING_ENTITY_STILL_ALIVE',
    'size() < 4',
    'selectNextPhase(RandomSource random)',
    'random.nextInt(selectable.size())',
    'FirstTick', 'BossActive', 'PhaseTime', 'FinishFightTime',
    'SummonIndex', 'ArenaPositionCount', 'Teleport', 'State',
    'Charging', 'Invulnerable',
    'COMBAT_PHASES_ENABLED = false'
)) {
    if ($source -notlike "*$required*") { throw "Missing controller contract: $required" }
}

$phaseIds = @('idle','teleport','bow_attack','melee_attack','fang_attack','fang_barrage','ravager',
    'spawn_evoker','spawn_vindicator','spawn_vex','spawn_mix','mimic','stone_golem')
foreach ($id in $phaseIds) {
    if ($source -notlike "*$id*") { throw "Missing released phase definition: $id" }
}
foreach ($deferred in @('EvokerFangs', 'RangedBowAttackGoal', 'MeleeAttackGoal', 'AdjudicatorMimicEntity', 'StoneGolemEntity')) {
    if ($source -like "*$deferred*") { throw "Deferred combat implementation leaked into Stage 12A.4: $deferred" }
}
Write-Output 'STAGE 12A.4 ADJUDICATOR CONTROLLER VALIDATION PASSED (synced state, arena lifecycle, persistence, eligibility, deferred-combat gate)'
