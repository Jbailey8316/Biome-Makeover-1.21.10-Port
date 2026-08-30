param([string]$Root = (Split-Path $PSScriptRoot -Parent))
$ErrorActionPreference = 'Stop'
$source = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionFeature.java') -Raw
foreach ($needle in @(
    'restoreAuthoredDryWaterloggedStates',
    'BlockStateProperties.WATERLOGGED',
    'diagnosticTemplate.contains("/dungeon/")',
    'level.setBlock(entry.getKey(), authored, 3)',
    'context.chunkPos(), terrainSamples.stream().sorted()',
    'thresholdSpread=40', 'thresholdAbove=20', 'thresholdGap=20')) {
    if ($source.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing waterlogged parity contract: $needle" }
}
Write-Output 'Stage 11B.1R.17B waterlogged parity validation: PASS'
