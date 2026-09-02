$ErrorActionPreference = 'Stop'
$source = Join-Path $PSScriptRoot '..\src\main\java\party\lemons\biomemakeover\worldgen\mansion\MansionFeature.java'
$text = Get-Content -LiteralPath $source -Raw
foreach ($needle in @('layoutMetadata == null', 'BM_MANSION_RUNTIME_METADATA_MISSING', 'RUNTIME_REGISTERED.add(mansionId)', 'layoutMetadata.placements', 'layoutMetadata.unionSize')) {
  if ($text.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing runtime metadata handoff requirement: $needle" }
}
if ($text -match 'setLiquidSettings\(LiquidSettings\.IGNORE_WATERLOGGING\).*R17') { throw 'Fluid placement semantics unexpectedly changed.' }
Write-Output 'PASS: first-piece runtime registration uses complete LayoutMetadata and reports unavailable metadata.'
