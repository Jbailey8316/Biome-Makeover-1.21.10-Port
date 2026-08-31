param([string]$Root = (Split-Path $PSScriptRoot -Parent))
$ErrorActionPreference = 'Stop'
$source = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionFeature.java') -Raw
foreach ($needle in @('EXPECTED_PLACEMENTS','PLACED_PLACEMENTS','getBoundingBox()',
    'event=CHUNK_PLACED','placementKey','chunkPos.x','chunkPos.z',
    'equals(EXPECTED_PLACEMENTS.get(mansionId))')) {
    if ($source.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing piece-chunk completion contract: $needle" }
}
Write-Output 'Stage 11B.1R.17J piece/chunk completion validation: PASS'
