param([switch]$SkipJar)
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$sourcePath = Join-Path $root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionFeature.java'
$source = Get-Content -Raw $sourcePath
$released = Get-Content -Raw (Join-Path $root 'reference/Biome-Makeover-1.20/common/src/main/java/party/lemons/biomemakeover/level/feature/mansion/MansionFeature.java')
$failed = $false
function Check($name, $ok) { if ($ok) { Write-Host "PASS $name" } else { Write-Host "FAIL $name"; $script:failed = $true } }

Check 'released marker handler documented' ($released.Contains('metadata.equals("boss")') -and $released.Contains('metadata.equals("arena_pos")') -and $released.Contains('Blocks.SMOOTH_QUARTZ') -and $released.Contains('level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2)'))
Check 'current marker semantics' ($source.Contains('case "boss" -> Blocks.AIR.defaultBlockState()') -and $source.Contains('case "arena_pos" -> Blocks.SMOOTH_QUARTZ.defaultBlockState()'))
Check 'marker diagnostics' ($source.Contains('BM_DATA_MARKER_FLUID') -and $source.Contains('BM_DATA_MARKER_FLUID_SUMMARY'))
Check 'marker mutation bounded' ($source.Contains('handleDataMarker') -and -not $source.Contains('clearBossRoomMarkers') -and -not $source.Contains('clearBossRoomWater'))
Check 'external fluid not cleared' (-not $source.Contains('for (BlockPos pos : pieceBounds') -and $source.Contains('level.setBlock(position, expected, 2)'))
Check 'C8 one-set handoff' ($source.Contains('BOSS_C8_WATER_CELLS') -and $source.Contains('BOSS_C8_WATER_STATES') -and $source.Contains('emitTraceSeedCheck(c8WaterCells)') -and $source.Contains('emitWaterCellCompare(c8WaterCells'))
Check 'C8 trace uses captured seeds' ($source.Contains('waterComponentTrace(phase,') -and $source.Contains('Set<BlockPos> seeds = capturedSeeds == null ? currentWaterSeeds() : capturedSeeds'))
Check 'crop and scheduler unchanged' ($source.Contains('ageTicks >= 400') -and $source.Contains('ageTicks >= 900') -and $source.Contains('restoreSerializedCrops'))
Check 'native liquid setting unchanged' ($source.Contains('LiquidSettings.IGNORE_WATERLOGGING'))

if (-not $SkipJar) {
  Add-Type -AssemblyName System.IO.Compression.FileSystem
  $jar = Get-ChildItem (Join-Path $root 'build/libs') -Filter '*.jar' | Sort-Object LastWriteTime -Descending | Select-Object -First 1
  Check 'compiled artifact exists' ($null -ne $jar)
  if ($jar) {
    $zip = [System.IO.Compression.ZipFile]::OpenRead($jar.FullName)
    try { $parts = foreach ($entry in $zip.Entries) { $reader = New-Object IO.StreamReader($entry.Open()); try { $reader.ReadToEnd() } finally { $reader.Dispose() } }; $all = $parts -join "`n" } finally { $zip.Dispose() }
    foreach ($marker in 'BM_DATA_MARKER_FLUID','BM_DATA_MARKER_FLUID_SUMMARY','BM_BOSS_WATER_CELL_COMPARE','BM_BOSS_TRACE_SEED_CHECK','BM_BOSS_WATER_COMPONENT_SUMMARY') { Check "JAR $marker" $all.Contains($marker) }
  }
}
if ($failed) { exit 1 }
Write-Host 'R20R.10 data-marker fluid validation PASS'
