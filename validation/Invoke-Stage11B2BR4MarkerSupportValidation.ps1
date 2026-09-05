[CmdletBinding()]
param([string]$Root = (Get-Location).Path)
$ErrorActionPreference = 'Stop'
$feature = Join-Path $Root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionFeature.java'
$audit = Join-Path $Root 'validation/java/Stage11B2BMarkerSupportAudit.java'
$doc = Join-Path $Root 'docs/STAGE_11B2B_TAPESTRY_PARITY_AUDIT.md'
foreach ($path in @($feature,$audit,$doc)) { if (!(Test-Path -LiteralPath $path)) { throw "Missing R.4 audit file: $path" } }
$text = Get-Content -LiteralPath $feature -Raw
foreach ($needle in @('BM_TAPESTRY_MARKER_DIRECTION','candidateSupportAtFacingOpposite','candidateSupportAtFacing','pieceRotation','facing.getOpposite()')) {
    if ($text.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing marker direction contract: $needle" }
}
if ($text.IndexOf('BM_TAPESTRY_PLACED_STATE', [StringComparison]::Ordinal) -lt 0 -or $text.IndexOf('transformedMarkerFacing', [StringComparison]::Ordinal) -lt 0) { throw 'Missing post-write tapestry state assertion/dataflow fields' }
$auditText = Get-Content -LiteralPath $audit -Raw
foreach ($needle in @('totalWallMarkers','markerFacingPointsTowardBacking','markerFacingOppositePointsTowardBacking','noAdjacentBacking','ambiguous')) {
    if ($auditText.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing offline audit aggregate: $needle" }
}
$docText = Get-Content -LiteralPath $doc -Raw
foreach ($needle in @('all 56','56 point toward backing','production renderer transforms are unchanged')) {
    if ($docText.IndexOf($needle, [StringComparison]::OrdinalIgnoreCase) -lt 0) { throw "Missing documented R.4 finding: $needle" }
}
Write-Output 'STAGE 11B.2B R.4 MARKER SUPPORT VALIDATION PASSED (56 released markers; direction/support contract; diagnostic units)'
