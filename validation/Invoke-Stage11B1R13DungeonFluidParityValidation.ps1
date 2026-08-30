param([string]$Root = (Split-Path $PSScriptRoot -Parent))
$ErrorActionPreference = 'Stop'

$mansion = Join-Path $Root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionFeature.java'
$source = Get-Content -LiteralPath $mansion -Raw
foreach ($needle in @('BM_FLUID_INTERIOR','BM_FLUID_POSITION','authoredDryPositions','waterInAuthoredDry','BlockStateProperties.WATERLOGGED')) {
    if ($source.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing authored-interior fluid diagnostic: $needle" }
}

$templates = Join-Path $Root 'src/main/resources/data/biomemakeover/structure/mansion'
$dungeon = @(Get-ChildItem -LiteralPath $templates -Recurse -Filter '*.nbt' | Where-Object { $_.FullName -match '\\dungeon\\' })
if ($dungeon.Count -eq 0) { throw 'No packaged dungeon Mansion templates found' }

$all = @(Get-ChildItem -LiteralPath $templates -Recurse -Filter '*.nbt')
if ($all.Count -ne 168) { throw "Expected 168 Mansion templates, found $($all.Count)" }
Write-Output "Stage 11B.1R.13 authored-interior fluid diagnostics: PASS (templates=$($all.Count) dungeonTemplates=$($dungeon.Count); diagnostic-only, no placement mutation)"
