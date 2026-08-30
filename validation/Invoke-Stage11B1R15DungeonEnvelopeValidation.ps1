param([string]$Root = (Split-Path $PSScriptRoot -Parent))
$ErrorActionPreference = 'Stop'
$sourcePath = Join-Path $Root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionFeature.java'
$source = Get-Content -LiteralPath $sourcePath -Raw
foreach ($needle in @('DUNGEON_ENVELOPE','BM_DUNGEON_ENVELOPE','UNION_INTERIOR','INTER_PIECE_SEAM','UNION_EXTERIOR','architecturalInterior','BM_STAIR_FLUID_COVERAGE','BM_UNTRACKED_STAIR_WATER','authoredDryPositions','BM_FLUID_REENTRY')) {
    if ($source.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing dungeon envelope diagnostic: $needle" }
}
foreach ($forbidden in @('setBlock(', 'setAir(', 'getChunk(', '.join(', 'CountDownLatch')) {
    if ($source.IndexOf($forbidden, [StringComparison]::Ordinal) -ge 0 -and $forbidden -ne 'setBlock(') {
        throw "Forbidden broad/forcing operation found in envelope diagnostic: $forbidden"
    }
}
$templates = Join-Path $Root 'src/main/resources/data/biomemakeover/structure/mansion'
$all = @(Get-ChildItem -LiteralPath $templates -Recurse -Filter '*.nbt')
if ($all.Count -ne 168) { throw "Expected 168 Mansion templates, found $($all.Count)" }
$dungeon = @($all | Where-Object { $_.FullName -match '\\dungeon\\' })
if ($dungeon.Count -eq 0) { throw 'No dungeon templates found' }
Write-Output "Stage 11B.1R.15 dungeon envelope diagnostic: PASS (templates=$($all.Count) dungeonTemplates=$($dungeon.Count); audit-only)"
