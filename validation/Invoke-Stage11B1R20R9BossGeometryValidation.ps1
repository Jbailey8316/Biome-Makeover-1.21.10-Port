param([switch]$SkipJar)
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$source = Get-Content -Raw (Join-Path $root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionFeature.java')
$bossStart=$source.IndexOf('private static final class BossBoundaryTrace');$bossEnd=$source.IndexOf('private record BoundaryFace',$bossStart)
$boss=if($bossStart -ge 0 -and $bossEnd -gt $bossStart){$source.Substring($bossStart,$bossEnd-$bossStart)}else{''}
$checks=[ordered]@{
 'immutable BossGeometry'=$source.Contains('private record BossGeometry') -and $source.Contains('BOSS_GEOMETRIES.putIfAbsent')
 'actual Piece transform'=$boss.Contains('piece.placeSettings.getRotation()') -and $boss.Contains('piece.placeSettings.getMirror()') -and $boss.Contains('piece.templatePosition')
 'geometry compare'=$boss.Contains('BM_BOSS_GEOMETRY_COMPARE') -and $boss.Contains('canonicalOnly') -and $boss.Contains('legacyOnly')
 'water cell compare'=$boss.Contains('BM_BOSS_WATER_CELL_COMPARE') -and $boss.Contains('canonicalClassification')
 'source classifier'=$boss.Contains('BM_BOSS_SOURCE_CELL_CLASSIFY') -and $boss.Contains('OMITTED_OR_UNSERIALIZED') -and $boss.Contains('STRUCTURE_VOID')
 'seed invariant'=$boss.Contains('BM_BOSS_TRACE_SEED_CHECK') -and $boss.Contains('missingFromTrace') -and $boss.Contains('extraInTrace')
 'reverse trace uses canonical seeds'=$boss.Contains('for (BlockPos seed : explicitAir)') -and $boss.Contains('traceWaterComponent(seed, expanded, visited)')
 'bounds expansion four'=$boss.Contains('pieceBounds.minX() - 4') -and $boss.Contains('pieceBounds.maxX() + 4')
 'no source BFS'=-not $boss.Contains('findSourcePath') -and -not $boss.Contains('candidateSources')
 'scheduler unchanged'=$source.Contains('age >= 400') -and $source.Contains('age >= 900') -and $source.Contains('readyTick')
 'no diagnostic mutation'=-not $boss.Contains('setBlock(')
 'crop behavior retained'=$source.Contains('level.setBlock(e.getKey(), e.getValue(), 2)') -and $source.Contains('isCropState')
}
foreach($c in $checks.GetEnumerator()){if($c.Value){Write-Host "PASS $($c.Key)"}else{Write-Host "FAIL $($c.Key)";$global:failed=$true}}
if(-not $SkipJar){
 Add-Type -AssemblyName System.IO.Compression.FileSystem
 $jar=Get-ChildItem (Join-Path $root 'build/libs') -Filter '*.jar'|Sort-Object LastWriteTime -Descending|Select-Object -First 1
 $zip=[System.IO.Compression.ZipFile]::OpenRead($jar.FullName)
 try{$parts=@();foreach($e in $zip.Entries){$s=$e.Open();$r=New-Object IO.StreamReader($s);$parts+=$r.ReadToEnd();$r.Dispose();$s.Dispose()};$all=$parts -join "`n"}finally{$zip.Dispose()}
 foreach($m in 'BM_BOSS_GEOMETRY_COMPARE','BM_BOSS_WATER_CELL_COMPARE','BM_BOSS_SOURCE_CELL_CLASSIFY','BM_BOSS_TRACE_SEED_CHECK','BM_BOSS_WATER_COMPONENT_SUMMARY'){if($all.Contains($m)){Write-Host "PASS JAR $m"}else{Write-Host "FAIL JAR $m";$global:failed=$true}}
}
if($failed){exit 1};Write-Host 'R20R.9 validation PASS'
