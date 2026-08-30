param([string]$Root = (Split-Path $PSScriptRoot -Parent))
$ErrorActionPreference = 'Stop'
$source = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionFeature.java') -Raw
foreach ($needle in @('BM_WATERLOG_TRANSITION','BM_UNTRACKED_STAIR_WATER','BlockStateProperties.WATERLOGGED','dungeonAuthoredStates','architecturalInterior')) {
    if ($source.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing waterlogged seam diagnostic: $needle" }
}
foreach ($forbidden in @('setBlock(', 'setAir(', 'getChunk(', '.join(', 'CountDownLatch')) {
    if ($forbidden -ne 'setBlock(' -and $source.IndexOf($forbidden, [StringComparison]::Ordinal) -ge 0) { throw "Forbidden operation in diagnostic path: $forbidden" }
}
$all = @(Get-ChildItem (Join-Path $Root 'src/main/resources/data/biomemakeover/structure/mansion') -Recurse -Filter '*.nbt')
if ($all.Count -ne 168) { throw "Expected 168 Mansion templates, found $($all.Count)" }
Write-Output "Stage 11B.1R.15B waterlogged seam diagnostics: PASS (templates=$($all.Count); diagnostic-only)"
