$ErrorActionPreference = 'Stop'
$source = Join-Path $PSScriptRoot '..\src\main\java\party\lemons\biomemakeover\worldgen\mansion\MansionFeature.java'
$text = Get-Content -LiteralPath $source -Raw
$required = @(
  'correctReleasedFluidStateForCurrentClip',
  'clip.isInside(pos)',
  'authored.isAir()',
  'BlockStateProperties.WATERLOGGED',
  'isDungeonStructuralTemplate()',
  '[BM_PLACEMENT_FLUID_FIX]'
)
foreach ($needle in $required) {
  if ($text.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing placement-fluid requirement: $needle" }
}
if ($text -match 'correctReleasedFluidStateForCurrentClip[\s\S]{0,500}level\.getChunk') { throw 'Placement correction must not force-load chunks' }
if ($text -match 'correctReleasedFluidStateForCurrentClip[\s\S]{0,1200}(\.join\(|\.get\()') { throw 'Placement correction must not block on futures' }
Write-Output 'PASS: R17M per-placement fluid correction is clip-local, structural, and nonblocking.'
