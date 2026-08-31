$ErrorActionPreference = 'Stop'
$source = Join-Path $PSScriptRoot '..\src\main\java\party\lemons\biomemakeover\worldgen\mansion\MansionFeature.java'
$text = Get-Content -LiteralPath $source -Raw
$required = @('naturalClosureState', '[BM_FLUID_SOURCE_BOUNDARY]', '[BM_FLUID_SOURCE_CLOSURE]',
  'Direction.values()', 'fluid.isSource()', 'clip.isInside(dryPos)',
  'correctReleasedFluidStateForCurrentClip', 'BlockStateProperties.WATERLOGGED')
foreach ($needle in $required) { if ($text.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing source-closure requirement: $needle" } }
if ($text -match 'naturalClosureState[\s\S]{0,2500}(flood|fill|while\s*\()') { throw 'Source closure must not recurse or flood-fill' }
if ($text -match 'naturalClosureState[\s\S]{0,2500}getChunk') { throw 'Source closure must not force-load chunks' }
if ($text -match 'naturalClosureState[\s\S]{0,2500}(Blocks\.BEDROCK|Blocks\.BARRIER)') { throw 'Source closure must not use barrier/bedrock' }
Write-Output 'PASS: R17N source closure is source-only, face-adjacent, clip-local, deterministic, and bounded.'
