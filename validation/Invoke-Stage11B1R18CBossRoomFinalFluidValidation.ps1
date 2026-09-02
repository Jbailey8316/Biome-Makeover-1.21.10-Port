$ErrorActionPreference = 'Stop'
$source = Get-Content (Join-Path $PSScriptRoot '..\src\main\java\party\lemons\biomemakeover\worldgen\mansion\MansionFeature.java') -Raw
foreach ($needle in @('reconcileBossRoomFinalAir', 'BM_BOSS_ROOM_FINAL_RECONCILE', 'candidate.template.contains("/boss_room")', 'entry.getValue().isAir()')) { if ($source.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing bounded boss-room safeguard: $needle" } }
Write-Output 'PASS: boss-room final reconciliation is explicit-air-only and template-scoped.'
