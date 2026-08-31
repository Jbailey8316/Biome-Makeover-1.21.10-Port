$ErrorActionPreference = 'Stop'
$source = Join-Path $PSScriptRoot '..\src\main\java\party\lemons\biomemakeover\worldgen\mansion\MansionFeature.java'
$text = Get-Content -LiteralPath $source -Raw
foreach ($needle in @('[BM_FLUID_SOURCE_CLOSURE]', 'authoredStates.containsKey(sourcePos)', 'fluid.isSource()', 'naturalClosureState', 'correctReleasedFluidStateForCurrentClip')) {
  if ($text.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "R17P requirement missing: $needle" }
}
if ($text -match 'Map<BlockPos, BlockState> union[\s\S]{0,2500}naturalClosureState') { throw 'R17O union-omitted source closure remains production-active' }
if ($text -match 'BM_UNION_FLUID_CLOSURE.*externalOmittedSourcesClosed') { throw 'R17O union boundary mutation remains active' }
if ($text -match 'naturalClosureState[\s\S]{0,2500}(while\s*\(|flood|fill)') { throw 'Hydraulic closure must remain one-face and non-recursive' }
Write-Output 'PASS: R17O production mutation rolled back; R17N bounded source closure remains active.'
