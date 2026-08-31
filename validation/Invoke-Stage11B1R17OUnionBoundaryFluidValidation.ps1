$ErrorActionPreference = 'Stop'
$source = Join-Path $PSScriptRoot '..\src\main\java\party\lemons\biomemakeover\worldgen\mansion\MansionFeature.java'
$text = Get-Content -LiteralPath $source -Raw
$required = @('[BM_UNION_FLUID_CLOSURE]', 'naturalClosureState', 'union.putAll',
  'Blocks.STRUCTURE_VOID', 'BlockStateProperties.WATERLOGGED', 'fluid.isSource()',
  'Direction.values()', 'clip.isInside(dryPos)', 'correctReleasedFluidStateForCurrentClip')
foreach ($needle in $required) { if ($text.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing union-boundary requirement: $needle" } }
if ($text -match 'union[\s\S]{0,3000}(flood|fill|while\s*\()') { throw 'Union closure must not flood-fill or recurse' }
if ($text -match 'naturalClosureState[\s\S]{0,2500}(Blocks\.BEDROCK|Blocks\.BARRIER)') { throw 'Union closure must not use barrier/bedrock' }
Write-Output 'PASS: R17O union-boundary classification is bounded and source-only.'
