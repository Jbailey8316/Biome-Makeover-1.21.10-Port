$ErrorActionPreference = 'Stop'
$source = Join-Path $PSScriptRoot '..\src\main\java\party\lemons\biomemakeover\worldgen\mansion\MansionFeature.java'
$text = Get-Content -LiteralPath $source -Raw
foreach ($needle in @('LiquidSettings.IGNORE_WATERLOGGING', 'setLiquidSettings', 'trial_liquid_parity', '[BM_TRIAL_LIQUID_PARITY]')) {
  if ($text.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing R18 requirement: $needle" }
}
if ($text -match 'trial_liquid_parity[\s\S]{0,500}setBlock') { throw 'R18 switch must not add broad fluid mutation' }
Write-Output 'PASS: Trial Chamber ignore-waterlogging semantics are wired with an explicit experimental bypass.'
