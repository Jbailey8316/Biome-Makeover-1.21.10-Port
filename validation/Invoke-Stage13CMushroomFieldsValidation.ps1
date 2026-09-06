[CmdletBinding()]
param([string]$Root = (Get-Location).Path)

$ErrorActionPreference = 'Stop'
$base = Join-Path $Root 'src/main/resources/data/biomemakeover/worldgen'
$referenceBase = Join-Path $Root 'reference/Biome-Makeover-1.20/common/src/main/resources/data/biomemakeover/worldgen'
$buildScript = Get-Content (Join-Path $Root 'build.gradle') -Raw
$jarPath = Join-Path $Root 'build/libs/biomemakeover-fabric-1.21.10-0.8.5.jar'
$jarEntries = if (Test-Path $jarPath) { @(jar tf $jarPath) } else { @() }
$configured = @(
  'blighted_balsa','blighted_balsa_trees','green_glowshrooms','huge_green_glowshroom',
  'huge_orange_glowshroom','huge_purple_glowshroom','mycelium_roots','mycelium_sprouts',
  'orange_glowshrooms','purple_glowshrooms','tall_brown_mushrooms','tall_red_mushrooms',
  'underground_huge_glowshrooms','underground_mycelium','underground_vegetation'
)
$placed = @(
  'blighted_balsa_checked','blighted_balsa_trees','green_glowshrooms','mycelium_roots',
  'mycelium_sprouts','orange_glowshrooms','purple_glowshrooms','tall_brown_mushrooms',
  'tall_red_mushrooms','underground_huge_glowshrooms','underground_mycelium','wild_mushrooms'
)
foreach ($id in $configured) {
  $p = Join-Path $base "configured_feature/mushroom_fields/$id.json"
  $reference = Join-Path $referenceBase "configured_feature/mushroom_fields/$id.json"
  $entry = "data/biomemakeover/worldgen/configured_feature/mushroom_fields/$id.json"
  if (!(Test-Path $reference) -or $entry -notin $jarEntries) { throw "Configured Mushroom Fields feature is not supplied by the released pipeline: $id" }
  if ($buildScript -notmatch [regex]::Escape("data/biomemakeover/worldgen/configured_feature/mushroom_fields/*.json")) { throw "Configured feature pipeline include missing: $id" }
  Get-Content $reference -Raw | ConvertFrom-Json | Out-Null
}
foreach ($id in $placed) {
  $p = Join-Path $base "placed_feature/mushroom_fields/$id.json"
  $reference = Join-Path $referenceBase "placed_feature/mushroom_fields/$id.json"
  $entry = "data/biomemakeover/worldgen/placed_feature/mushroom_fields/$id.json"
  if (!(Test-Path $reference) -or $entry -notin $jarEntries) { throw "Placed Mushroom Fields feature is not supplied by the released pipeline: $id" }
  if ($buildScript -notmatch [regex]::Escape("data/biomemakeover/worldgen/placed_feature/mushroom_fields/*.json")) { throw "Placed feature pipeline include missing: $id" }
  $json = Get-Content $reference -Raw | ConvertFrom-Json
  if (!$json.feature) { throw "Placed feature has no configured-feature reference: $id" }
  if ($id -ne 'wild_mushrooms' -and $json.feature -notmatch '^biomemakeover:mushroom_fields/') { throw "Placed feature escapes Mushroom Fields namespace: $id" }
  $target = ($json.feature -split '/')[-1]
  $targetDir = if ($id -eq 'wild_mushrooms') { 'dark_forest' } else { 'mushroom_fields' }
  $targetPath = Join-Path $referenceBase "configured_feature/$targetDir/$target.json"
  if (!(Test-Path $targetPath)) { throw "Placed feature $id references missing released configured feature $target" }
}
$worldgen = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/init/BMWorldgen.java') -Raw
foreach ($id in $placed) {
  $constantCall = 'mushroom("' + $id + '")'
  if ($id -ne 'blighted_balsa_checked' -and $worldgen -notmatch [regex]::Escape($constantCall) -and $worldgen -notmatch [regex]::Escape("mushroom_fields/$id")) { throw "Biome placement is missing Mushroom Fields feature: $id" }
}
if ($worldgen -match 'BLACK_THISTLE|black_thistle') { throw 'Black Thistle must not be a Mushroom Fields dependency' }
# Beach is a separate package; its registration may coexist in the shared
# worldgen initializer. The Mushroom Fields checks above remain scoped to the
# mushroom feature constants and placement calls.
$items = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/init/BMItems.java') -Raw
if ($worldgen -notmatch 'BMEntities\.GLOWFISH') { throw 'Glowfish Mushroom Fields spawn hook is missing' }
$tag = Join-Path $Root 'src/main/resources/data/biomemakeover/tags/worldgen/biome/mushroom_fields.json'
if (!(Test-Path $tag) -and 'data/biomemakeover/tags/worldgen/biome/mushroom_fields.json' -notin $jarEntries) { throw 'Mushroom Fields biome tag is missing' }
if (Test-Path $tag) { Get-Content $tag -Raw | ConvertFrom-Json | Out-Null }
$missing = @($configured | Where-Object { "data/biomemakeover/worldgen/configured_feature/mushroom_fields/$_.json" -notin $jarEntries }).Count
$missingPlaced = @($placed | Where-Object { "data/biomemakeover/worldgen/placed_feature/mushroom_fields/$_.json" -notin $jarEntries }).Count
Write-Output "STAGE 13C MUSHROOM FIELDS VALIDATION PASSED: configured=$($configured.Count) placed=$($placed.Count) missing=$missing missingPlaced=$missingPlaced"
Write-Output 'Black Thistle and Beach/Helmit Crab remain excluded; Stage 13H boat registrations are validated separately.'
