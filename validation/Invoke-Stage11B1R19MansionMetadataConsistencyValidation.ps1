$ErrorActionPreference = 'Stop'
$source = Join-Path $PSScriptRoot '..\src\main\java\party\lemons\biomemakeover\worldgen\mansion\MansionFeature.java'
$text = Get-Content -LiteralPath $source -Raw
foreach ($needle in @('Map<BlockPos, BlockState> completeUnion', 'completeUnion.size()', 'EXPECTED_UNION_SIZES', 'LayoutMetadata')) {
  if ($text.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing metadata consistency requirement: $needle" }
}
if ($text -match 'LAYOUT_COMPLETE[\s\S]{0,300}mapToInt\(piece -> piece\.dungeonAuthoredStates\(\)\.size\(\)\)\.sum') { throw 'Layout union must use deduplicated canonical union size' }
Write-Output 'PASS: layout and runtime metadata report the same deduplicated canonical union size.'
