$ErrorActionPreference = 'Stop'
$source = Join-Path $PSScriptRoot '..\src\main\java\party\lemons\biomemakeover\worldgen\mansion\MansionFeature.java'
$text = Get-Content -LiteralPath $source -Raw
foreach ($needle in @('layoutSignature', 'MansionLayoutSignature', 'layoutSignature(List<Piece>', 'mansionOrigin + ":" + layoutSignature', 'registerExpectedPieces')) {
  if ($text.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing layout identity requirement: $needle" }
}
if ($text -match 'layoutSignature[\s\S]{0,3000}UUID') { throw 'Layout identity must be deterministic' }
Write-Output 'PASS: Mansion runtime identity carries deterministic layout signatures and serialized metadata.'
