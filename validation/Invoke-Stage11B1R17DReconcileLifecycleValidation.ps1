param([string]$Root = (Split-Path $PSScriptRoot -Parent))
$ErrorActionPreference = 'Stop'
$source = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionFeature.java') -Raw
foreach ($needle in @(
    'ServerTickEvents.END_WORLD_TICK.register',
    'reconcileCompletedDungeon',
    'event=EXECUTE_BEGIN', 'event=EXECUTE_END', 'event=REMOVE',
    '[BM_DUNGEON_RECONCILE]',
    'DELAYED_FLUID_TRACES.add(new DelayedFluidTrace')) {
    if ($source.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing reconciliation lifecycle contract: $needle" }
}
if ($source -match 'Future\.(get|join)|CountDownLatch|\.join\(\)') { throw 'Blocking wait found in reconciliation lifecycle' }
Write-Output 'Stage 11B.1R.17D reconciliation lifecycle validation: PASS'
