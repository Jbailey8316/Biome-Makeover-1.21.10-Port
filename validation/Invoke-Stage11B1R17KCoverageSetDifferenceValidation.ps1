param([string]$Root = (Split-Path $PSScriptRoot -Parent))
$ErrorActionPreference = 'Stop'
$source = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionFeature.java') -Raw
foreach ($needle in @('BM_RECONCILE_COVERAGE_MISMATCH','missingCount={} unexpectedCount={}',
    'BM_PLACEMENT_COVERAGE','LAYOUT_COMPLETE','getBoundingBox()',
    'placed.equals(expected)','chunkPos.x','chunkPos.z')) {
    if ($source.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing coverage set-difference contract: $needle" }
}
if ($source -match 'placed\.size\(\)\s*>=\s*expected\.size\(\)\s*\)\s*\{\s*READY') { throw 'Count-only READY logic detected' }
Write-Output 'Stage 11B.1R.17K coverage set-difference validation: PASS'
