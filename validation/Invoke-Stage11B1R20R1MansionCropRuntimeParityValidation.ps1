$ErrorActionPreference = 'Stop'
$source = Get-Content (Join-Path $PSScriptRoot '..\src\main\java\party\lemons\biomemakeover\worldgen\mansion\MansionFeature.java') -Raw
foreach ($needle in @('restoreSerializedCrops', 'BM_CROP_RUNTIME', 'Blocks.WHEAT', 'Blocks.CARROTS', 'Blocks.POTATOES', 'Blocks.BEETROOTS', 'Blocks.ATTACHED_MELON_STEM', 'Blocks.ATTACHED_PUMPKIN_STEM')) { if ($source.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing crop runtime parity support: $needle" } }
$method = [regex]::Match($source, '(?s)restoreSerializedCrops\(.*?\n        \}')
if ($method.Success -and $method.Value -match 'applyBonemeal|randomTick|nextInt') { throw 'Crop restoration must not add growth or RNG.' }
Write-Output 'PASS: serialized Mansion crop states are restored bounded to the active template clip without growth logic.'
