param([string]$Root = (Split-Path $PSScriptRoot -Parent))
$ErrorActionPreference = 'Stop'
$source = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionFeature.java') -Raw
foreach ($needle in @('NEXT_PIECE_ORDINAL','mansionPieceOrdinal','MansionPieceOrdinal','EXPECTED_ORDINALS',
    'placedCount={} expectedCount={}','Set.copyOf(ids)','equals(EXPECTED_ORDINALS.get(mansionId))')) {
    if ($source.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing structural identity contract: $needle" }
}
if ($source -match 'pieceId\(\)') { throw 'Coordinate-derived pieceId method remains' }
Write-Output 'Stage 11B.1R.17I structural piece identity validation: PASS'
