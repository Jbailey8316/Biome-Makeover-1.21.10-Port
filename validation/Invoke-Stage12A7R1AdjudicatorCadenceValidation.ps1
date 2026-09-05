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
    'BM_ADJUDICATOR_CADENCE_PROOF',
    'PHASE_EXIT=',
    'selectablePhaseCount=',
    'summonPhasesEligible='
)) {
    if ($source -notlike "*$required*") { throw "Missing Stage 12A.7-R1 cadence contract: $required" }
}
if ($source -like '*BM_STAGE12A7_PHASE_TEST_GATE*' -or $source -like '*BM_STAGE12A7_PHASE_TEST_SUBSET*') {
    throw 'Temporary Stage 12A.7 test selector remains'
}
foreach ($deferred in @('AdjudicatorMimicEntity', 'StoneGolemEntity', 'EnchantedTotem', 'adjudicator_tapestry')) {
    if ($source -like "*$deferred*") { throw "Deferred system leaked into Stage 12A.7-R1: $deferred" }
}
Write-Output 'STAGE 12A.7-R1 ADJUDICATOR CADENCE VALIDATION PASSED (released timing, Monster limit, and normal staged gate)'
