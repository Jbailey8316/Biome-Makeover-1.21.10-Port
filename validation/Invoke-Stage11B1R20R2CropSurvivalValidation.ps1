$ErrorActionPreference = 'Stop'
$source = Get-Content (Join-Path $PSScriptRoot '..\src\main\java\party\lemons\biomemakeover\worldgen\mansion\MansionFeature.java') -Raw
foreach ($needle in @('C4', 'C5', 'C6', '6 + entry.age', 'BM_CROP_RUNTIME', 'BM_CROP_DISAPPEAR', 'CROP_DISAPPEAR_LOGGED', 'restoreSerializedCrops')) { if ($source.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing late crop lifecycle trace: $needle" } }
$method = [regex]::Match($source, '(?s)reconcileCropsFinal.*?private static void traceCropPhase')
if ($method.Success -and $method.Value -match 'applyBonemeal|randomTick|nextInt') { throw 'Late crop handling must not add growth or RNG.' }
Write-Output 'PASS: bounded C4-C8 crop lifecycle tracing and one-shot disappearance capture are present; no growth logic added.'
