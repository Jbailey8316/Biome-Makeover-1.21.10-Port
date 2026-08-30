param([string]$Root = (Split-Path $PSScriptRoot -Parent))
$ErrorActionPreference = 'Stop'
$source = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionFeature.java') -Raw
foreach ($needle in @('LAYOUT_PIECES','registerExpectedPieces','EXPECTED_PIECES','PLACED_PIECES',
    'event=PIECE_PLACED','expectedCount','isDungeonStructuralTemplate()',
    'EXECUTED_MANSIONS.add(mansionId)')) {
    if ($source.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing exact placement lifecycle contract: $needle" }
}
if ($source -match 'entry\.age\s*>=\s*20\s*&&\s*EXECUTED_MANSIONS') { throw 'Age-based production readiness remains authoritative' }
Write-Output 'Stage 11B.1R.17H exact placement/reentry validation: PASS'
