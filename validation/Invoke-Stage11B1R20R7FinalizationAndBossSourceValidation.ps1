param([switch]$SkipJar)
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$source = Join-Path $root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionFeature.java'
$text = Get-Content -Raw $source
$bossStart = $text.IndexOf('private static final class BossBoundaryTrace')
$bossEnd = $text.IndexOf('private record BoundaryFace', $bossStart)
$bossDiagnostic = if ($bossStart -ge 0 -and $bossEnd -gt $bossStart) { $text.Substring($bossStart, $bossEnd - $bossStart) } else { '' }
$checks = [ordered]@{
  'final placement gate' = $text.Contains('isFinalPlacementComplete(mansionId)') -and $text.Contains('placedPlacements.equals(expectedPlacements)')
  'one authoritative scheduler' = $text.Contains('Map<String, LateFinalization> LATE_FINALIZATIONS') -and $text.Contains('LATE_FINALIZATIONS.putIfAbsent(mansionId, late)')
  'no early per-piece C7/C8' = -not $text.Contains('entry.age <= 2') -and -not $text.Contains('"C" + (6 + entry.age)')
  'ordered one-shot phases' = $text.Contains('late.phases.add(phase)') -and $text.Contains('duplicate or out-of-order phase') -and $text.Contains('case "C5" -> 1')
  'authoritative crop targets' = $text.Contains('CROP_TARGETS') -and $text.Contains('registerCropTargets')
  'crop registration missing guard' = $text.Contains('BM_CROP_TARGET_REGISTRATION_MISSING') -and $text.Contains('cropReady')
  'C7 requires ready' = $text.Contains('if (!late.ready)') -and $text.Contains('late.age == 1')
  'crop result verification' = $text.Contains('BM_CROP_FINALIZATION_RESULT') -and $text.Contains('missingAfter')
  'no alternate crop finalization call' = -not $text.Contains('reconcileCropsFinal(')
  'game tick delayed timing' = $text.Contains('currentLevel.getGameTime() - readyTick') -and $text.Contains('age >= 400') -and $text.Contains('age >= 900')
  'finite D45 disposal' = $text.Contains('snapshots.contains("D45S")') -and $text.Contains('BOSS_BOUNDARY_TRACES.remove(this)')
  'bounded boss expansion' = $text.Contains('pieceBounds.minX() - 3') -and $text.Contains('Math.abs(dx) + Math.abs(dy) + Math.abs(dz) > 2')
  'bounded source output' = $text.Contains('emitted++ < 16') -and $text.Contains('directEmitted++ < 16')
  'direct source evidence' = $text.Contains('BM_BOSS_DIRECT_SOURCE') -and $text.Contains('bossBoundaryPos=')
  'diagnostic has no fluid mutation' = -not $bossDiagnostic.Contains('setBlock(')
}
foreach ($item in $checks.GetEnumerator()) { if ($item.Value) { Write-Host "PASS $($item.Key)" } else { Write-Host "FAIL $($item.Key)"; $global:failed = $true } }
if (-not $SkipJar) {
  Add-Type -AssemblyName System.IO.Compression.FileSystem
  $jar = Get-ChildItem (Join-Path $root 'build/libs') -Filter '*.jar' | Sort-Object LastWriteTime -Descending | Select-Object -First 1
  if (-not $jar) { Write-Host 'FAIL compiled JAR missing'; exit 1 }
  $markers = 'BM_LATE_FINALIZATION_CONTRACT_VIOLATION','BM_CROP_FINALIZATION_RESULT','BM_CROP_FINALIZATION_READY','BM_BOSS_SOURCE_PATH','BM_BOSS_DIRECT_SOURCE','BM_BOSS_SOURCE_PATH_SUMMARY','phase=C7','phase=C8','phase=D20S','phase=D45S'
  $zip = [System.IO.Compression.ZipFile]::OpenRead($jar.FullName)
  try { $bytes = foreach ($entry in $zip.Entries) { $stream = $entry.Open(); $reader = New-Object IO.StreamReader($stream); $s = $reader.ReadToEnd(); $reader.Dispose(); $stream.Dispose(); $s }; $all = $bytes -join "`n" }
  finally { $zip.Dispose() }
  foreach ($marker in $markers) { if ($all.Contains($marker)) { Write-Host "PASS JAR $marker" } else { Write-Host "FAIL JAR $marker"; $global:failed = $true } }
}
if ($failed) { exit 1 }
Write-Host 'R20R.7 validation PASS'
