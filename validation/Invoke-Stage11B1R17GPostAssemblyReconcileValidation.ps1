param([string]$Root = (Split-Path $PSScriptRoot -Parent))
$ErrorActionPreference = 'Stop'
$source = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionFeature.java') -Raw
foreach ($needle in @('EXECUTED_MANSIONS','entry.age >= 20','EXECUTED_MANSIONS.add(entry.mansionId())',
    'event=READY','countMansionPieces','reconcileCompletedDungeon(level, entry.order, entry.mansionOrigin)')) {
    if ($source.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing post-assembly lifecycle contract: $needle" }
}
if ($source -match 'if \(!tracing\)\s*\{\s*DELAYED_FLUID_TRACES\.remove') { throw 'Trace flag prematurely removes production jobs' }
Write-Output 'Stage 11B.1R.17G post-assembly reconciliation validation: PASS'
