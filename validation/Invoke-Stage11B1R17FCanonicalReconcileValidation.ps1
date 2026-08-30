param([string]$Root = (Split-Path $PSScriptRoot -Parent))
$ErrorActionPreference = 'Stop'
$source = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionFeature.java') -Raw
foreach ($needle in @('LAYOUT_ORIGIN','mansionOrigin','MansionOriginX','mansionId()',
    'candidate.mansionOrigin.equals(mansionOrigin)','result.executed()','reconcileCompletedDungeon(level, entry.order, entry.mansionOrigin)')) {
    if ($source.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing canonical reconciliation identity: $needle" }
}
if ($source -match 'mansionId\(\).*authoredStates\.keySet') { throw 'Piece-derived mansion identity remains' }
Write-Output 'Stage 11B.1R.17F canonical reconciliation validation: PASS'
