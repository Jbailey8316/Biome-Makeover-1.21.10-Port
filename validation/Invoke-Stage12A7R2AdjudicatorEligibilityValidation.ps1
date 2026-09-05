param([string]$Root = (Join-Path $PSScriptRoot '..'))
$ErrorActionPreference = 'Stop'
$source = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/entity/AdjudicatorEntity.java') -Raw
foreach ($required in @(
    'private List<ControllerPhase> selectablePhases()',
    'candidate.selectable() && (!isSummonPhase(candidate) || summonPhaseEligible())',
    'List<ControllerPhase> actualPool = selectablePhases().stream()',
    '.filter(AdjudicatorEntity::isImplementedPhase).toList()',
    'actualSelectablePhases=', 'actualSelectablePhaseCount=', 'selectedPhase=',
    'BM_ADJUDICATOR_CADENCE_PROOF', 'BM_ADJUDICATOR_SUMMON_ELIGIBILITY_PROOF',
    'restrictedSummonsPresentInActualPool=', 'size() < 4'
)) {
    if ($source -notlike "*$required*") { throw "Missing Stage 12A.7-R2 eligibility contract: $required" }
}
foreach ($phase in @('spawn_evoker','spawn_vindicator','spawn_vex','spawn_mix')) {
    if ($source -notlike "*isSummonPhase(candidate)*") { throw 'Restricted summon family is not filtered at pool construction' }
}
if ($source -like '*BM_STAGE12A7_PHASE_TEST_GATE*' -or $source -like '*BM_STAGE12A7_PHASE_TEST_SUBSET*') {
    throw 'Temporary forced phase test gate remains'
}
Write-Output 'STAGE 12A.7-R2 ADJUDICATOR ELIGIBILITY VALIDATION PASSED (actual pool enforces released Monster limit)'
