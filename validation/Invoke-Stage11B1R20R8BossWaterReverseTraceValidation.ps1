param([switch]$SkipJar)
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$source = Get-Content -Raw (Join-Path $root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionFeature.java')
$bossStart = $source.IndexOf('private static final class BossBoundaryTrace')
$bossEnd = $source.IndexOf('private record BoundaryFace', $bossStart)
$boss = if($bossStart -ge 0 -and $bossEnd -gt $bossStart){$source.Substring($bossStart,$bossEnd-$bossStart)}else{''}
$checks = [ordered]@{
 'single retained late state' = $source.Contains('Map<String, LateFinalization> LATE_FINALIZATIONS') -and $source.Contains('for (LateFinalization late : LATE_FINALIZATIONS.values())')
 'retention through D45S' = $source.Contains('BM_LATE_FINALIZATION_RETENTION') -and $source.Contains('ageTicks >= 900') -and $source.Contains('retention(level, "D45S")')
 'true delayed ticks' = $source.Contains('currentLevel.getGameTime() - readyTick') -and $source.Contains('age >= 400') -and $source.Contains('age >= 900')
 'no tracker lifetime coupling' = $source.Contains('if (entry.age >= 100') -and $source.Contains('LATE_FINALIZATIONS.remove(late.id, late)')
 'reverse seeded from boss water' = $boss.Contains('for (BlockPos seed : explicitAir)') -and $boss.Contains('traceWaterComponent(seed, expanded, visited)')
 'fluid-only traversal' = $boss.Contains('!level.getFluidState(seed).is(Fluids.WATER)') -and $boss.Contains('!level.getFluidState(next).is(Fluids.WATER)')
 'expansion four' = $boss.Contains('pieceBounds.minX() - 4') -and $boss.Contains('pieceBounds.maxX() + 4')
 'single visit set' = $boss.Contains('Set<BlockPos> visited') -and $boss.Contains('visited.add(next)')
 'no source-centric BFS' = -not $boss.Contains('findSourcePath') -and -not $boss.Contains('candidateSources')
 'bounded representative output' = $boss.Contains('emitted++ < 8')
 'diagnostic no mutation' = -not $boss.Contains('setBlock(')
 'crop semantics retained' = $source.Contains('level.setBlock(e.getKey(), e.getValue(), 2)') -and $source.Contains('isCropState')
}
foreach($c in $checks.GetEnumerator()){if($c.Value){Write-Host "PASS $($c.Key)"}else{Write-Host "FAIL $($c.Key)";$global:failed=$true}}
if(-not $SkipJar){
 Add-Type -AssemblyName System.IO.Compression.FileSystem
 $jar=Get-ChildItem (Join-Path $root 'build/libs') -Filter '*.jar'|Sort-Object LastWriteTime -Descending|Select-Object -First 1
 $zip=[System.IO.Compression.ZipFile]::OpenRead($jar.FullName)
 try { $parts = @(); foreach($e in $zip.Entries) { $stream = $e.Open(); $reader = New-Object IO.StreamReader($stream); $parts += $reader.ReadToEnd(); $reader.Dispose(); $stream.Dispose() }; $all = $parts -join "`n" } finally { $zip.Dispose() }
 foreach($m in 'BM_LATE_FINALIZATION_RETENTION','BM_BOSS_WATER_COMPONENT_SUMMARY','BM_BOSS_WATER_COMPONENT','BM_BOSS_TRACE_PERF','phase=D20S','phase=D45S'){if($all.Contains($m)){Write-Host "PASS JAR $m"}else{Write-Host "FAIL JAR $m";$global:failed=$true}}
}
if($failed){exit 1};Write-Host 'R20R.8 validation PASS'
