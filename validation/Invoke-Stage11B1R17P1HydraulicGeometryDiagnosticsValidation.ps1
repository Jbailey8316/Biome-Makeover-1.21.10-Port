$ErrorActionPreference = 'Stop'
$source = Join-Path $PSScriptRoot '..\src\main\java\party\lemons\biomemakeover\worldgen\mansion\MansionFeature.java'
$text = Get-Content -LiteralPath $source -Raw
foreach ($needle in @('analyzeHydraulicGeometry', '[BM_HYDRAULIC_LEAK_FACE]', '[BM_HYDRAULIC_SUMMARY]', '[BM_BOSS_HYDRAULIC_SUMMARY]', '[BM_HYDRAULIC_WATER_VOLUME]', 'isHydraulicPassable', 'volume > 600_000L')) {
  if ($text.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing hydraulic diagnostic requirement: $needle" }
}
if ($text -match 'analyzeHydraulicGeometry[\s\S]{0,7000}level\.setBlock') { throw 'Hydraulic analysis must be read-only' }
if ($text -match 'analyzeHydraulicGeometry[\s\S]{0,7000}getChunk') { throw 'Hydraulic analysis must not force-load chunks' }
Write-Output 'PASS: R17P.1 bounded read-only hydraulic geometry diagnostics are present.'
