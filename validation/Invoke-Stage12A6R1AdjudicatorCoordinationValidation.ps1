param([string]$Root = (Join-Path $PSScriptRoot '..'))
$ErrorActionPreference = 'Stop'
$source = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/entity/AdjudicatorEntity.java') -Raw
$renderState = Get-Content (Join-Path $Root 'src/client/java/party/lemons/biomemakeover/client/render/AdjudicatorRenderState.java') -Raw
$renderer = Get-Content (Join-Path $Root 'src/client/java/party/lemons/biomemakeover/client/render/AdjudicatorRenderer.java') -Raw
$model = Get-Content (Join-Path $Root 'src/client/java/party/lemons/biomemakeover/client/model/AdjudicatorModel.java') -Raw
foreach ($required in @(
    'STATE_SUMMONING = 3',
    'getNavigation().stop()',
    'getLookControl().setLookAt',
    'private static final boolean IMPLEMENTED_PHASE_EXECUTION_GATE = true',
    'ControllerPhase.BOW_ATTACK, ControllerPhase.MELEE_ATTACK',
    'ControllerPhase.FANG_ATTACK, ControllerPhase.FANG_BARRAGE'
)) {
    if ($source -notlike "*$required*") { throw "Missing accepted coordination contract: $required" }
}
foreach ($required in @('public int controllerState', 'public float attackAnimation')) {
    if ($renderState -notlike "*$required*") { throw "Missing render-state contract: $required" }
}
foreach ($required in @('state.controllerState = entity.getControllerState()', 'state.attackAnimation = entity.getAttackAnim(tickDelta)')) {
    if ($renderer -notlike "*$required*") { throw "Missing renderer coordination contract: $required" }
}
foreach ($required in @('state.controllerState == 1 || state.controllerState == 3', 'AnimationUtils.swingWeaponDown')) {
    if ($model -notlike "*$required*") { throw "Missing model coordination contract: $required" }
}
$bossBar = $source.IndexOf('updateBossBarPlayers();')
$controller = $source.IndexOf('tickController();')
if ($bossBar -lt 0 -or $controller -lt 0 -or $bossBar -gt $controller) {
    throw 'Boss-bar enrollment must precede controller ticking.'
}
foreach ($temporary in @('BM_ADJUDICATOR_FANG_PROOF', 'BM_ADJUDICATOR_COMBAT_COORDINATION_PROOF')) {
    if ($source.Contains($temporary)) { throw "Temporary diagnostic remains: $temporary" }
}
Write-Output 'STAGE 12A.6-R1 ADJUDICATOR COORDINATION VALIDATION PASSED (accepted pose, facing, navigation, and boss-bar ordering retained)'
