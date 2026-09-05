param([string]$Root = (Join-Path $PSScriptRoot '..'))
$ErrorActionPreference = 'Stop'
$source = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/entity/AdjudicatorEntity.java') -Raw
foreach ($required in @(
    'RAVAGER("ravager", true)',
    'SPAWN_EVOKER("spawn_evoker", true)',
    'SPAWN_VINDICATOR("spawn_vindicator", true)',
    'SPAWN_VEX("spawn_vex", true)',
    'SPAWN_MIX("spawn_mix", true)',
    'SUMMON_PHASE_TICKS = 120',
    'case SPAWN_EVOKER, SPAWN_VEX -> 2',
    'case SPAWN_VINDICATOR -> 6',
    'case SPAWN_MIX -> 3',
    'EntityType.EVOKER', 'EntityType.VINDICATOR', 'EntityType.VEX', 'EntityType.PILLAGER',
    'EntityType.RAVAGER.create(serverLevel, EntitySpawnReason.EVENT)',
    'startRiding(ravager, true, true)',
    'getOrThrow(Enchantments.MULTISHOT), 3',
    'isSummonPhase(phase)',
    'EntitySelector.LIVING_ENTITY_STILL_ALIVE',
    'size() < 4',
    'ControllerPhase.RAVAGER',
    'ControllerPhase.SPAWN_EVOKER', 'ControllerPhase.SPAWN_VINDICATOR',
    'ControllerPhase.SPAWN_VEX', 'ControllerPhase.SPAWN_MIX'
)) {
    if ($source -notlike "*$required*") { throw "Missing Stage 12A.7 contract: $required" }
}
foreach ($deferred in @('AdjudicatorMimicEntity', 'StoneGolemEntity', 'EnchantedTotem', 'adjudicator_tapestry')) {
    if ($source -like "*$deferred*") { throw "Deferred Stage 12A.7 system leaked into implementation: $deferred" }
}
Write-Output 'STAGE 12A.7 ADJUDICATOR SUMMON VALIDATION PASSED (Ravager and four vanilla summon phases)'
