param(
    [string]$Root = (Join-Path $PSScriptRoot '..'),
    [string]$Jar = (Join-Path (Join-Path (Join-Path $PSScriptRoot '..') 'build/libs') 'biomemakeover-fabric-1.21.10-0.8.5.jar'),
    [switch]$SkipJar
)

$ErrorActionPreference = 'Stop'
$source = Get-Content -LiteralPath (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionFeature.java') -Raw
$markers = @('BM_CROP_FINALIZATION_READY','BM_BOSS_SOURCE_PATH','phase=D20S','phase=D45S')
foreach ($needle in @(
    'private static boolean isFinalPlacementComplete',
    'placedPlacements.size() == expectedPlacements.size()',
    'placedPieces.size() == expectedOrdinals.size()',
    'if (!late.ready) continue;',
    'if (late.age < 0) { late.age++; continue; }',
    'level.getBlockState(e.getKey()).equals(e.getValue())',
    'sourcePath(phase);',
    'pieceBounds.minX() - 3',
    'pieceBounds.maxX() + 3',
    'if (trace.age == 20) trace.snapshot("D20S")',
    'if (trace.age == 45) { trace.snapshot("D45S"); BOSS_BOUNDARY_TRACES.remove(trace); }')) {
    if ($source.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing R20R.6 source requirement: $needle" }
}
$diagStart = $source.IndexOf('private static final class BossBoundaryTrace', [StringComparison]::Ordinal)
$diagEnd = $source.IndexOf('private void traceFences', $diagStart, [StringComparison]::Ordinal)
if ($diagStart -lt 0 -or $diagEnd -lt 0) { throw 'Could not delimit boss diagnostic implementation' }
$diag = $source.Substring($diagStart, $diagEnd - $diagStart)
foreach ($forbidden in @('setBlock(', 'placeInWorld(', 'clearBossRoomAuthoredAir(')) {
    if ($diag.IndexOf($forbidden, [StringComparison]::Ordinal) -ge 0) { throw "Boss diagnostic contains mutation: $forbidden" }
}
if ($diag.IndexOf('new java.util.ArrayDeque', [StringComparison]::Ordinal) -lt 0) { throw 'Missing bounded boss source BFS' }
if ($diag.IndexOf('expanded.isInside(next)', [StringComparison]::Ordinal) -lt 0) { throw 'Boss source BFS is not expanded-region bounded' }
if ($source.IndexOf('phase=C7', [StringComparison]::Ordinal) -lt 0) { throw 'Existing C7 behavior marker missing' }

if (!$SkipJar) {
    if (!(Test-Path -LiteralPath $Jar)) { throw "Missing JAR: $Jar" }
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $zip=[IO.Compression.ZipFile]::OpenRead((Resolve-Path -LiteralPath $Jar))
    try {
        $text=[Text.StringBuilder]::new()
        foreach($entry in $zip.Entries | Where-Object FullName -like '*.class') {
            $stream=$entry.Open();$memory=[IO.MemoryStream]::new()
            try {$stream.CopyTo($memory);[void]$text.Append([Text.Encoding]::UTF8.GetString($memory.ToArray()))}
            finally {$stream.Dispose();$memory.Dispose()}
        }
        foreach($marker in $markers) { if(!$text.ToString().Contains($marker)) { throw "Missing compiled marker: $marker" } }
    } finally {$zip.Dispose()}
}
Write-Output 'Stage 11B.1R.20R.6 lifecycle and boss source validation passed.'
