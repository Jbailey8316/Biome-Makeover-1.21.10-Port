param([string]$Root = (Split-Path $PSScriptRoot -Parent))
$ErrorActionPreference = 'Stop'
$source = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionFeature.java') -Raw
foreach ($needle in @('footprintHeights','median','median + 8','BM_MANSION_SITE_REJECT','thresholdSpread=40','thresholdAbove=20','thresholdGap=20','return Optional.empty')) {
    if ($source.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing site suitability contract: $needle" }
}
Write-Output 'Stage 11B.1R.17A Mansion site suitability validation: PASS'
