param([string]$Root = (Join-Path $PSScriptRoot '..'))
$ErrorActionPreference = 'Stop'
$source = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/entity/AdjudicatorEntity.java') -Raw
foreach ($required in @(
    'ControllerPhase.FANG_ATTACK',
    'ControllerPhase.FANG_BARRAGE',
    'FANG_BARRAGE_PHASE_TICKS = 100',
    'new EvokerFangs(level(), x, pos.getY() + height, z, yaw, warmup, this)',
    'for (int i = 0; i < 5; i++)',
    'for (int i = 0; i < 8; i++)',
    'for (int i = 0; i < 16; i++)',
    'direction.getStepX() * (i + 1)',
    'direction.getStepZ() * (i + 1)',
    'ControllerPhase.FANG_ATTACK, ControllerPhase.FANG_BARRAGE'
)) {
    if ($source -notlike "*$required*") { throw "Missing Stage 12A.6 contract: $required" }
}
foreach ($deferred in @('AdjudicatorMimicEntity', 'StoneGolemEntity', 'new Ravager')) {
    if ($source -like "*$deferred*") { throw "Deferred phase leaked into Stage 12A.6: $deferred" }
}
if ($source -match 'EnchantedTotem|adjudicator_tapestry') { throw 'Reward code leaked into Stage 12A.6' }
Write-Output 'STAGE 12A.6 ADJUDICATOR FANG VALIDATION PASSED (EvokerFangs geometry/timing and gated controller integration)'
