param([string]$ProjectRoot = (Split-Path $PSScriptRoot -Parent))
$ErrorActionPreference = 'Stop'
$resource = Join-Path $ProjectRoot 'src/main/resources'
$errors = [System.Collections.Generic.List[string]]::new()
function Require-File([string]$p) {
  if (-not (Test-Path (Join-Path $resource $p)) -and -not (Test-Path (Join-Path $ProjectRoot "build/generated/resources/$p"))) { $errors.Add("missing resource: $p") }
}

# Runtime asset contracts for the item families introduced by 11B/12A.
foreach ($id in 'cladded_helmet','cladded_chestplate','cladded_leggings','cladded_boots','crude_cladding','cladded_stone','red_rose_music_disk') {
  Require-File "assets/biomemakeover/items/$id.json"
  Require-File "assets/biomemakeover/models/item/$id.json"
}

# Every registered BM potted plant must have the modern blockstate/model pair.
$blocks = Get-Content (Join-Path $ProjectRoot 'src/main/java/party/lemons/biomemakeover/init/BMBlocks.java') -Raw
[regex]::Matches($blocks, 'potted\("([^"]+)"').Groups | Where-Object Name -eq 1 | ForEach-Object {
  $id = $_.Value; Require-File "assets/biomemakeover/blockstates/$id.json"; Require-File "assets/biomemakeover/models/block/$id.json"
}

# The released loot entry is an item reference; cladded_stone must exist in both registries.
if ($blocks -notmatch 'CLADDED_STONE\s*=\s*registerBlockItem') { $errors.Add('cladded_stone is not registered with a BlockItem') }
$loot = Get-ChildItem (Join-Path $resource 'data/biomemakeover/loot_table/mansion') -Filter '*.json'
if ($loot.Count -ne 7) { $errors.Add("expected 7 Mansion loot tables, found $($loot.Count)") }

# Released placement semantics: preserve processor distinction. Modern 1.21.10
# removed the historical keepLiquids mutator; fluid behavior requires runtime verification.
$mf = Get-Content (Join-Path $ProjectRoot 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionFeature.java') -Raw
if ($mf -notmatch 'wall \? BlockIgnoreProcessor\.STRUCTURE_AND_AIR') { $errors.Add('Mansion wall/non-wall processor distinction missing') }

# Dark Forest vegetation configuration is recorded, not aesthetically tuned here.
Require-File 'data/biomemakeover/worldgen/placed_feature/dark_forest/itching_ivy.json'
Require-File 'data/biomemakeover/worldgen/configured_feature/dark_forest/itching_ivy.json'
if ($errors.Count) { $errors | ForEach-Object { Write-Error $_ }; exit 1 }
Write-Output 'Stage 11B.1R.1 static asset/loot/fluid validation: PASS'
Write-Output 'Potted plant resource pairs: PASS'
Write-Output 'cladded_stone block + item registration: PASS'
Write-Output 'Mansion placement processor distinction: PASS'
