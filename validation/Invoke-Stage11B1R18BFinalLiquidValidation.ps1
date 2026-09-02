$ErrorActionPreference = 'Stop'
$source = Join-Path $PSScriptRoot '..\src\main\java\party\lemons\biomemakeover\worldgen\mansion\MansionFeature.java'
$text = Get-Content -LiteralPath $source -Raw
foreach ($needle in @('private static final boolean VANILLA_LIQUID_PARITY = true;', 'LiquidSettings.IGNORE_WATERLOGGING', 'clearBossRoomAuthoredAir', 'Legacy R17 authored-fluid mutation is retired')) {
  if ($text.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing R18B requirement: $needle" }
}
if ($text -match 'VANILLA_LIQUID_PARITY\s*=\s*Boolean\.getBoolean') { throw 'Production liquid behavior must not depend on a JVM flag' }
Write-Output 'PASS: native Trial Chamber liquid handling is default and legacy broad fluid mutation is inactive.'
