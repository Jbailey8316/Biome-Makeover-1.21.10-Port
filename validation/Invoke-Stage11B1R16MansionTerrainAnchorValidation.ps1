param([string]$Root = (Split-Path $PSScriptRoot -Parent))
$ErrorActionPreference = 'Stop'
$source = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionFeature.java') -Raw
foreach ($needle in @('BM_MANSION_HEIGHT_TRACE','WORLD_SURFACE_WG','minSurfaceY','medianSurfaceY','anchorMinusMedian')) {
    if ($source.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing terrain anchor diagnostic: $needle" }
}
Write-Output 'Stage 11B.1R.16 Mansion terrain anchor diagnostic: PASS (audit-only)'
