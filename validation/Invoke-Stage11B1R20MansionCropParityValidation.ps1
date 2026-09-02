$ErrorActionPreference = 'Stop'
$source = Join-Path $PSScriptRoot '..\src\main\java\party\lemons\biomemakeover\worldgen\mansion\MansionFeature.java'
$text = Get-Content -LiteralPath $source -Raw
foreach ($needle in @('case "bonemeal", "tapestry"', 'BM_CROP_TRACE', 'Blocks.WHEAT', 'Blocks.CARROTS', 'Blocks.POTATOES', 'Blocks.BEETROOTS')) {
  if ($text.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Missing crop parity evidence: $needle" }
}
if ($text -match '(?s)case "bonemeal", "tapestry"\s*->\s*\{\s*[^}]*(applyBonemeal|growCrop|randomTick)') { throw 'Bonemeal marker must remain a no-op.' }
$count = (Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot '..\src\main\resources\data\biomemakeover\structure\mansion') -Recurse -Filter *.nbt).Count
if ($count -ne 168) { throw "Expected 168 Mansion templates, found $count" }
Write-Output "PASS: Mansion bonemeal marker is consumed as a no-op; serialized crop state remains authoritative (templates=$count)."
