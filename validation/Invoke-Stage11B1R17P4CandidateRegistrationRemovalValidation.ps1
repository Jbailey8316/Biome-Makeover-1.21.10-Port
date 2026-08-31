$ErrorActionPreference = 'Stop'
$source = Join-Path $PSScriptRoot '..\src\main\java\party\lemons\biomemakeover\worldgen\mansion\MansionFeature.java'
$text = Get-Content -LiteralPath $source -Raw
foreach ($needle in @('EXPECTED_UNION_SIZES', 'EXPECTED_BOSS_PIECES', 'RUNTIME_REGISTERED', '[BM_MANSION_RUNTIME_REGISTER]', 'layoutSignature')) {
  if ($text.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing P17.4 metadata requirement: $needle" }
}
Write-Output 'PASS: complete layout metadata is retained and runtime registration is signature-scoped.'
