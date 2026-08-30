param([string]$Root = (Split-Path $PSScriptRoot -Parent))
$ErrorActionPreference = 'Stop'
$source = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionFeature.java') -Raw
foreach ($needle in @(
    'diagnosticTemplate.contains("/boss_room")',
    'isDungeonStructuralTemplate()',
    'ReconcileResult(boolean executed',
    'result.executed()',
    'candidate.authoredStates')) {
    if ($source.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing boss-room reconciliation contract: $needle" }
}
Write-Output 'Stage 11B.1R.17E boss-room/lifecycle validation: PASS'
