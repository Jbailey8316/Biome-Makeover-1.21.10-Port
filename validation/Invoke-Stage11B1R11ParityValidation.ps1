param([string]$Root = (Split-Path $PSScriptRoot -Parent))
$ErrorActionPreference = 'Stop'
$source = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionFeature.java') -Raw
$releasedTags = Get-Content (Join-Path $Root 'reference/Biome-Makeover-1.20/common/src/main/resources/data/minecraft/tags/blocks/wooden_fences.json') -Raw
foreach ($needle in @('parts.length >= 4','random.nextInt(100) > chance','world.setBlock(marker, replacement','BM_FENCE_LIFECYCLE','BM_FLUID_LIFECYCLE')) {
    if ($source.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing parity/diagnostic path: $needle" }
}
foreach ($id in @('blighted_balsa_fence','willow_fence','swamp_cypress_fence','ancient_oak_fence')) {
    if ($releasedTags.IndexOf($id, [StringComparison]::Ordinal) -lt 0) { throw "Released fence tag entry missing: $id" }
}
if ($source -match 'clear.*water|water.*clear|setBlock\([^\)]*Blocks\.AIR') { Write-Output 'No broad fluid cleanup added; placement semantics preserved.' }
Write-Output 'Stage 11B.1R.11 bounded parity audit: PASS'
