param([string]$Root = (Join-Path $PSScriptRoot '..'))
$ErrorActionPreference = 'Stop'
$class = Join-Path $Root 'src/main/java/party/lemons/biomemakeover/util/loot/BetterLootTableReference.java'
$items = Join-Path $Root 'src/main/java/party/lemons/biomemakeover/init/BMItems.java'
$lootRoot = Join-Path $Root 'src/main/resources/data/biomemakeover/loot_table/mansion'
$allLootRoot = Join-Path $Root 'src/main/resources/data/biomemakeover/loot_table'
if (!(Test-Path -LiteralPath $class) -or !(Test-Path -LiteralPath $items)) { throw 'Released loot infrastructure source is missing' }
$classText = Get-Content -LiteralPath $class -Raw
$itemText = Get-Content -LiteralPath $items -Raw
foreach ($needle in @('class BetterLootTableReference', 'MapCodec<BetterLootTableReference>', 'LootTable.KEY_CODEC.fieldOf("name")', 'getRandomItems(context, consumer)')) {
  if ($classText.IndexOf($needle, [StringComparison]::Ordinal) -lt 0) { throw "Loot infrastructure contract missing: $needle" }
}
if ($itemText.IndexOf('BiomeMakeover.id("loot_table")', [StringComparison]::Ordinal) -lt 0) { throw 'Canonical loot_table registry ID is missing' }
$expected = @('arrows.json','dungeon.json','dungeon_good.json','dungeon_junk.json','good.json','junk.json','standard.json')
foreach ($name in $expected) { $path = Join-Path $lootRoot $name; if (!(Test-Path -LiteralPath $path)) { throw "Mansion loot table missing: $name" }; Get-Content -LiteralPath $path -Raw | ConvertFrom-Json | Out-Null }
$allTables = @(Get-ChildItem -LiteralPath $allLootRoot -Recurse -Filter '*.json')
$customTables = @($allTables | Where-Object { (Get-Content -LiteralPath $_.FullName -Raw).IndexOf('biomemakeover:loot_table', [StringComparison]::Ordinal) -ge 0 })
if ($allTables.Count -ne 50) { throw "Unexpected packaged BM loot table count: $($allTables.Count) (expected 50)" }
if ($customTables.Count -ne 5) { throw "Unexpected custom loot-table reference usage: $($customTables.Count) files (expected 5)" }
$deferred = @('biomemakeover:cladded_boots','biomemakeover:cladded_chestplate','biomemakeover:cladded_leggings','biomemakeover:crude_cladding','biomemakeover:red_rose_music_disk')
Write-Output "STAGE 11B.0 LOOT INFRASTRUCTURE PASSED (custom entry registered; BM loot tables=$($allTables.Count); custom-reference files=$($customTables.Count); Mansion tables=$($expected.Count); deferred item dependencies=$($deferred.Count))"
Write-Output ('DEFERRED_DEPENDENCIES: ' + ($deferred -join ', '))
