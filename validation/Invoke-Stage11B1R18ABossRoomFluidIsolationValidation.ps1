$ErrorActionPreference = 'Stop'
$source = Join-Path $PSScriptRoot '..\src\main\java\party\lemons\biomemakeover\worldgen\mansion\MansionFeature.java'
$text = Get-Content -LiteralPath $source -Raw
foreach ($needle in @('LiquidSettings.IGNORE_WATERLOGGING', 'clearBossRoomAuthoredAir', 'diagnosticTemplate.contains("/boss_room")', 'entry.getValue().isAir()', 'clip.isInside(entry.getKey())', '[BM_BOSS_ROOM_FLUID]')) {
  if ($text.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing R18A requirement: $needle" }
}
if ($text -match 'clearBossRoomAuthoredAir[\s\S]{0,1400}(getChunk|flood|while\s*\()') { throw 'Boss-room correction must remain bounded and nonblocking' }
Write-Output 'PASS: boss-room correction is limited to serialized explicit air under native liquid semantics.'
