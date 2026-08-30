param(
    [string]$Root = (Split-Path $PSScriptRoot -Parent),
    [string]$JarPath = ''
)
$ErrorActionPreference = 'Stop'
$ids = @('blighted_balsa_fence','willow_fence','swamp_cypress_fence','ancient_oak_fence') | ForEach-Object { "biomemakeover:$_" }
$tag = Get-Content (Join-Path $Root 'src/main/resources/data/minecraft/tags/block/fences.json') -Raw | ConvertFrom-Json
$source = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/init/BMBlocks.java') -Raw
foreach ($id in $ids) {
    if ($tag.values -notcontains $id) { throw "Fence source tag missing $id" }
    if ($source.IndexOf(($id -replace '^biomemakeover:', ''), [StringComparison]::Ordinal) -lt 0) { throw "Fence registration missing $id" }
}
if ($source -notmatch 'FenceBlock::new') { throw 'Vanilla FenceBlock registration path missing' }

if ([string]::IsNullOrWhiteSpace($JarPath)) {
    $JarPath = Join-Path $Root 'build/libs/biomemakeover-fabric-1.21.10-0.8.5.jar'
}
if (-not (Test-Path -LiteralPath $JarPath)) { throw "Built JAR not found: $JarPath" }
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [IO.Compression.ZipFile]::OpenRead((Resolve-Path -LiteralPath $JarPath))
try {
    foreach ($path in @('data/minecraft/tags/block/fences.json','data/minecraft/tags/block/wooden_fences.json')) {
        $entry = $zip.GetEntry($path)
        if ($null -eq $entry) { throw "Built JAR missing tag: $path" }
        $reader = New-Object IO.StreamReader($entry.Open())
        try { $builtTag = $reader.ReadToEnd() | ConvertFrom-Json } finally { $reader.Dispose() }
        foreach ($id in $ids) {
            if ($builtTag.values -notcontains $id) { throw "Built JAR tag $path missing $id" }
        }
    }
} finally { $zip.Dispose() }
Write-Output 'Stage 11B.1R.12B fence tag packaging audit: PASS'
