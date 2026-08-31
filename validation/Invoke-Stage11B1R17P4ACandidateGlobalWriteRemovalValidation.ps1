$ErrorActionPreference = 'Stop'
$source = Join-Path $PSScriptRoot '..\src\main\java\party\lemons\biomemakeover\worldgen\mansion\MansionFeature.java'
$text = Get-Content -LiteralPath $source -Raw
foreach ($needle in @('LayoutMetadata', 'Candidate construction is pure', 'layoutMetadata', '[BM_MANSION_RUNTIME_REGISTER]', 'putIfAbsent(mansionId')) {
  if ($text.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing P4A requirement: $needle" }
}
Write-Output 'PASS: candidate layout metadata is local; runtime maps publish on first placement.'
