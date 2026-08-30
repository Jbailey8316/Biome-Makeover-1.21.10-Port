param([string]$Root = (Split-Path $PSScriptRoot -Parent))
$ErrorActionPreference = 'Stop'
$source = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionFeature.java') -Raw
foreach ($needle in @('BM_CROP_TRACE','phase=','Blocks.WHEAT','Blocks.CARROTS','Blocks.POTATOES','Blocks.BEETROOTS','supportState')) {
    if ($source.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing crop lifecycle diagnostic: $needle" }
}
Write-Output 'Stage 11B.1R.17A crop lifecycle validation: PASS (diagnostic-only)'
