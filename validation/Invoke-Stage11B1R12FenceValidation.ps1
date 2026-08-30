param([string]$Root = (Split-Path $PSScriptRoot -Parent))
$ErrorActionPreference = 'Stop'
$tag = Get-Content (Join-Path $Root 'src/main/resources/data/minecraft/tags/block/fences.json') -Raw | ConvertFrom-Json
$source = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/init/BMBlocks.java') -Raw
foreach ($id in @('blighted_balsa_fence','willow_fence','swamp_cypress_fence','ancient_oak_fence')) {
    if ($tag.values -notcontains "biomemakeover:$id") { throw "Fence tag missing $id" }
    if ($source.IndexOf("$id", [StringComparison]::Ordinal) -lt 0) { throw "Fence registration missing $id" }
}
if ($source -notmatch 'FenceBlock::new') { throw 'Vanilla FenceBlock registration path missing' }
Write-Output 'Stage 11B.1R.12 fence registration/tag audit: PASS'
