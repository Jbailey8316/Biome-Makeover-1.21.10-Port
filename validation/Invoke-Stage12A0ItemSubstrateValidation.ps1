param([string]$Root = (Join-Path $PSScriptRoot '..'))
$ErrorActionPreference = 'Stop'
$itemsPath = Join-Path $Root 'src/main/java/party/lemons/biomemakeover/init/BMItems.java'
$items = Get-Content $itemsPath -Raw
foreach ($id in @('CLADDED_HELMET','CLADDED_CHESTPLATE','CLADDED_LEGGINGS','CLADDED_BOOTS','CRUDE_CLADDING')) {
  if ($items.IndexOf("$id =", [StringComparison]::Ordinal) -lt 0) { throw "Missing substrate registration: $id" }
}
if ($items -notmatch 'humanoidArmor\(CLADDED_MATERIAL, ArmorType\.HELMET\)') { throw 'Modern armor component substrate missing' }
if ($items -notmatch 'ArmorMaterials\.IRON\.durability\(\)') { throw 'Released iron durability basis missing' }
if ($items -notmatch 'REPAIRS_LEATHER_ARMOR') { throw 'Released leather repair tag missing' }
foreach ($id in @('cladded_helmet','cladded_chestplate','cladded_leggings','cladded_boots','crude_cladding')) {
  foreach ($path in @("src/main/resources/assets/biomemakeover/models/item/$id.json", "src/main/resources/assets/biomemakeover/textures/item/$id.png")) {
    if (!(Test-Path (Join-Path $Root $path))) { throw "Missing item resource: $path" }
  }
}
if (!(Test-Path (Join-Path $Root 'src/main/resources/assets/biomemakeover/equipment/cladded.json'))) { throw 'Cladded equipment asset missing' }
if (Test-Path (Join-Path $Root 'src/main/resources/data/biomemakeover/recipe')) { if (Get-ChildItem (Join-Path $Root 'src/main/resources/data/biomemakeover/recipe') -Recurse -Filter '*cladded*' -ErrorAction SilentlyContinue) { throw 'Unexpected cladded recipe activation' } }
Write-Output 'STAGE 12A.0 ITEM SUBSTRATE PASSED (cladded armor set + inert crude cladding registered)'
