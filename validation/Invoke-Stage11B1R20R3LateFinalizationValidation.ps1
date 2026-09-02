$ErrorActionPreference = 'Stop'
$jar = Join-Path $PSScriptRoot '..\build\libs\biomemakeover-fabric-1.21.10-0.8.5.jar'
$tmp = Join-Path $env:TEMP ('r20r3_validator_' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $tmp | Out-Null
Push-Location $tmp; jar xf $jar; Pop-Location
$class = Join-Path $tmp 'party/lemons/biomemakeover/worldgen/mansion/MansionFeature.class'
foreach ($needle in @('BM_BOSS_ROOM_LATE_AUDIT', 'BM_CROP_LATE_SUMMARY', 'phase=C5', 'phase=C6', 'phase=C7', 'phase=C8')) {
  if (-not (rg -a -F -- $needle $class)) { Remove-Item -LiteralPath $tmp -Recurse -Force; throw "Compiled JAR missing runtime marker: $needle" }
}
Remove-Item -LiteralPath $tmp -Recurse -Force
Write-Output 'PASS: compiled JAR contains all required R20R.3 late-finalization markers.'
