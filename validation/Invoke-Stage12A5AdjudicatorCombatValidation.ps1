param([string]$Root = (Join-Path $PSScriptRoot '..'))
$ErrorActionPreference = 'Stop'
$source = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/entity/AdjudicatorEntity.java') -Raw
foreach ($required in @(
    'implements RangedAttackMob',
    'TELEPORT_PHASE_TICKS = 30',
    'ATTACK_PHASE_TICKS = 200',
    'RangedBowAttackGoal<>(this, 0.75F, 12, 30)',
    'TracingMeleeAttackGoal(this, 1.0F, true)',
    'setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOW))',
    'setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_AXE))',
    'setPos(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D)',
    'teleportPos = chooseArenaPosition()',
    'BM_STAGE12A5_IMPLEMENTED_PHASE_GATE = true',
    'PHASE_ENTER', 'PHASE_EXECUTE', 'PHASE_EXIT', 'BOW_SHOT', 'MELEE_ATTACK'
)) {
    if ($source -notlike "*$required*") { throw "Missing Stage 12A.5 contract: $required" }
}
foreach ($deferred in @('EvokerFangs', 'AdjudicatorMimicEntity', 'StoneGolemEntity', 'new Ravager')) {
    if ($source -like "*$deferred*") { throw "Deferred phase leaked into Stage 12A.5: $deferred" }
}
if ($source -match 'COMBAT_PHASES_ENABLED') { throw 'Old all-combat gate remains' }
Write-Output 'STAGE 12A.5 ADJUDICATOR COMBAT VALIDATION PASSED (teleport/bow/melee gated execution; deferred phases untouched)'
