param(
    [string]$Root = (Join-Path $PSScriptRoot '..'),
    [string]$Jar = (Join-Path (Join-Path (Join-Path $PSScriptRoot '..') 'build/libs') 'biomemakeover-fabric-1.21.10-0.8.5.jar'),
    [switch]$SkipJar
)

$ErrorActionPreference = 'Stop'
$sourcePath = Join-Path $Root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionFeature.java'
$source = Get-Content -LiteralPath $sourcePath -Raw
$markers = @(
    'BM_BOSS_BOUNDARY_IDENTITY', 'BM_BOSS_BOUNDARY_SUMMARY', 'BM_BOSS_BOUNDARY_WATER',
    'BM_BOSS_WATER_PROXIMITY', 'BM_BOSS_OPENING_SUMMARY', 'BM_BOSS_BOUNDARY_CHANGE'
)
foreach ($marker in $markers) {
    if ($source.IndexOf($marker, [StringComparison]::Ordinal) -lt 0) { throw "Missing source marker: $marker" }
}
foreach ($needle in @('Boolean.getBoolean("bm.mansion.trace")', 'filterBlocks(piece.templatePosition, piece.placeSettings, block)',
    'snapshotBossBoundary(level, entry.mansionId(), "READY")', 'entry.age == 2', 'entry.age == 20', 'BOSS_BOUNDARY_TRACES.remove(trace)')) {
    if ($source.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing source architecture: $needle" }
}
$diagnosticStart = $source.IndexOf('private static final class BossBoundaryTrace', [StringComparison]::Ordinal)
if ($diagnosticStart -lt 0) { throw 'Missing BossBoundaryTrace implementation' }
$diagnosticEnd = $source.IndexOf('private void traceFences', $diagnosticStart, [StringComparison]::Ordinal)
if ($diagnosticEnd -lt 0) { throw 'Could not delimit BossBoundaryTrace implementation' }
$diagnostic = $source.Substring($diagnosticStart, $diagnosticEnd - $diagnosticStart)
foreach ($forbidden in @('setBlock(', 'placeInWorld(', 'clearBossRoomAuthoredAir(')) {
    if ($diagnostic.IndexOf($forbidden, [StringComparison]::Ordinal) -ge 0) { throw "Diagnostic contains mutation: $forbidden" }
}
if ($diagnostic.IndexOf('relative(d)', [StringComparison]::Ordinal) -lt 0) { throw 'Missing bounded directional proximity scan' }
if ($diagnostic.IndexOf('p2 = p1.relative(d)', [StringComparison]::Ordinal) -lt 0) { throw 'Missing radius-2 proximity scan' }
if ($source.IndexOf('phase=C7', [StringComparison]::Ordinal) -lt 0) { throw 'Existing C7 reconciliation marker missing' }

if (!$SkipJar) {
    if (!(Test-Path -LiteralPath $Jar)) { throw "Missing compiled artifact: $Jar" }
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $archive = [IO.Compression.ZipFile]::OpenRead((Resolve-Path -LiteralPath $Jar))
    try {
        $classBytes = [Collections.Generic.List[byte]]::new()
        foreach ($entry in $archive.Entries | Where-Object { $_.FullName -like '*.class' }) {
            $stream = $entry.Open(); $memory = [IO.MemoryStream]::new()
            try { $stream.CopyTo($memory); foreach ($b in $memory.ToArray()) { $classBytes.Add($b) } }
            finally { $stream.Dispose(); $memory.Dispose() }
        }
        $compiledText = [Text.Encoding]::UTF8.GetString($classBytes.ToArray())
        foreach ($marker in $markers) {
            if ($compiledText.IndexOf($marker, [StringComparison]::Ordinal) -lt 0) { throw "Missing compiled JAR marker: $marker" }
        }
    }
    finally { $archive.Dispose() }
}

foreach ($path in @('src/main/resources/data/biomemakeover/worldgen/structure/mansion.json', 'src/main/resources/data/biomemakeover/structure/mansion/boss_room.nbt')) {
    if (!(Test-Path -LiteralPath (Join-Path $Root $path))) { throw "Missing unchanged template/layout input: $path" }
}
Write-Output 'Stage 11B.1R.20R.5 boss-boundary trace validation passed.'
