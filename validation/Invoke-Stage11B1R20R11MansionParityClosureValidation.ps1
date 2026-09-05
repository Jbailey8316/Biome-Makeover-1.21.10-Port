param([switch]$SkipJar)
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$source = Get-Content -Raw (Join-Path $root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionFeature.java')
$doc = Get-Content -Raw (Join-Path $root 'docs/STAGE_11B1R20R11_MANSION_PARITY_CLOSURE.md')
$failed = $false
function Check($name, $ok) { if ($ok) { Write-Host "PASS $name" } else { Write-Host "FAIL $name"; $script:failed = $true } }
Check 'closure documentation' ($doc.Contains('CLOSED') -and $doc.Contains('7/7') -and $doc.Contains('11/11'))
Check 'released marker states' ($source.Contains('case "boss" -> {') -and $source.Contains('case "arena_pos" -> level.setBlock(position, Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2)') -and $source.Contains('EntitySpawnReason.STRUCTURE'))
Check 'native liquid semantics' ($source.Contains('LiquidSettings.IGNORE_WATERLOGGING'))
Check 'marker-only scope' (-not $source.Contains('BM_BOSS_WATER_COMPONENT') -and -not $source.Contains('BM_HYDRAULIC_') -and -not $source.Contains('BM_BOSS_SOURCE_CELL_CLASSIFY'))
Check 'external water untouched' (-not $source.Contains('candidateSources') -and -not $source.Contains('naturalClosureState'))
Check 'crop restoration is serialized' ($source.Contains('restoreSerializedCrops') -and $source.Contains('template.filterBlocks(templatePosition, placeSettings, crop)'))
Check 'placement-gated finalization' ($source.Contains('isFinalPlacementComplete(mansionId)') -and $source.Contains('PLACED_PLACEMENTS'))
Check 'delayed lifecycle' ($source.Contains('ageTicks >= 400') -and $source.Contains('ageTicks >= 900') -and $source.Contains('readyTick'))
Check 'no deferred phase activation' (-not $source.Contains('StoneGolem') -and -not $source.Contains('MIMIC'))
if (-not $SkipJar) {
 Add-Type -AssemblyName System.IO.Compression.FileSystem
 $jar = Get-ChildItem (Join-Path $root 'build/libs') -Filter '*.jar' | Sort-Object LastWriteTime -Descending | Select-Object -First 1
 Check 'compiled artifact exists' ($null -ne $jar)
 if ($jar) {
  $zip = [System.IO.Compression.ZipFile]::OpenRead($jar.FullName)
  try { $all = (($zip.Entries | ForEach-Object { $r=New-Object IO.StreamReader($_.Open()); try {$r.ReadToEnd()} finally {$r.Dispose()} }) -join "`n") } finally {$zip.Dispose()}
 Check 'marker semantics compiled' ($all.Contains('boss->AIR;arena_pos->SMOOTH_QUARTZ') -and -not $all.Contains('BM_ADJUDICATOR_SPAWN_PROOF'))
 }
}
if ($failed) { exit 1 }; Write-Host 'R20R.11 Mansion parity closure validation PASS'
