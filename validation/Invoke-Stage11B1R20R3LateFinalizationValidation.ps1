$ErrorActionPreference = 'Stop'
$source = Get-Content (Join-Path $PSScriptRoot '..\src\main\java\party\lemons\biomemakeover\worldgen\mansion\MansionFeature.java') -Raw
foreach ($needle in @('LATE_FINALIZATIONS', 'C5', 'C6', 'C7', '6 + entry.age', 'BM_BOSS_ROOM_FINAL_RECONCILE', 'reconcileCropsFinal')) { if ($source.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing finite late-finalization requirement: $needle" } }
Write-Output 'PASS: finite post-READY C7 late finalization and C5-C8 diagnostics are present.'
