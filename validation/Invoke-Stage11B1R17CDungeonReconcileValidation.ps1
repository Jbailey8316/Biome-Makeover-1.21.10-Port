param([string]$Root = (Split-Path $PSScriptRoot -Parent))
$ErrorActionPreference = 'Stop'
$source = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionFeature.java') -Raw
foreach ($needle in @(
    'reconcileCompletedDungeon',
    'authored.isAir()',
    'candidate.authoredStates',
    'BlockStateProperties.WATERLOGGED',
    'diagnosticTemplate.contains("/dungeon/")')) {
    if ($source.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing dungeon reconciliation contract: $needle" }
}
Write-Output 'Stage 11B.1R.17C dungeon reconciliation validation: PASS'
