param([string]$Root = (Join-Path $PSScriptRoot '..'))
$ErrorActionPreference = 'Stop'
$source = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/entity/AdjudicatorEntity.java') -Raw
foreach ($required in @(
    'IMPLEMENTED_PHASE_EXECUTION_GATE = true',
    'SUMMON_PHASE_TICKS = 120',
    'phaseTime % (SUMMON_PHASE_TICKS / count)',
    'case SPAWN_EVOKER, SPAWN_VEX -> 2',
    'case SPAWN_VINDICATOR -> 6',
    'case SPAWN_MIX -> 3',
    'EntitySelector.LIVING_ENTITY_STILL_ALIVE',
    'size() < 4',
    'ControllerPhase.RAVAGER',
    'selectNextPhaseForStage(RandomSource random)',
    'selectablePhases()',
    'actualPool'
)) {
    if ($source -notlike "*$required*") { throw "Missing Stage 12A.7-R1 cadence contract: $required" }
}
foreach ($temporary in @('BM_ADJUDICATOR_CADENCE_PROOF','BM_ADJUDICATOR_SUMMON_ELIGIBILITY_PROOF','BM_STAGE12A7_PHASE_TEST_GATE','BM_STAGE12A7_PHASE_TEST_SUBSET')) {
    if ($source -like "*$temporary*") { throw "Temporary Stage 12A.7 marker remains: $temporary" }
}
foreach ($deferred in @('AdjudicatorMimicEntity', 'StoneGolemEntity', 'EnchantedTotem', 'adjudicator_tapestry')) {
    if ($source -like "*$deferred*") { throw "Deferred system leaked into Stage 12A.7-R1: $deferred" }
}
Write-Output 'STAGE 12A.7-R1 ADJUDICATOR CADENCE VALIDATION PASSED (released timing, Monster limit, and normal staged gate)'
