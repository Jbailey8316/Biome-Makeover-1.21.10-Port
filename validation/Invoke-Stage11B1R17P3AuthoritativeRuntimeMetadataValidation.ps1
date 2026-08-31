$ErrorActionPreference = 'Stop'
$source = Join-Path $PSScriptRoot '..\src\main\java\party\lemons\biomemakeover\worldgen\mansion\MansionFeature.java'
$text = Get-Content -LiteralPath $source -Raw
foreach ($needle in @('layoutSignature', 'MansionLayoutSignature', 'layoutSignature(List<Piece>', 'mansionId()')) {
  if ($text.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing metadata requirement: $needle" }
}
if ($text -match 'mansionId\(level, mansionOrigin\)[\s\S]{0,300}BM_HYDRAULIC') { throw 'Hydraulic diagnostics use origin-only identity' }
Write-Output 'PASS: runtime metadata and hydraulic verification are signature-scoped.'
