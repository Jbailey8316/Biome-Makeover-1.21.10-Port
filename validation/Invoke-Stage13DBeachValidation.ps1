[CmdletBinding()]
param([string]$Root = (Get-Location).Path)

$ErrorActionPreference = 'Stop'
$reference = Join-Path $Root 'reference/Biome-Makeover-1.20/common/src/main/resources'
$jarPath = Join-Path $Root 'build/libs/biomemakeover-fabric-1.21.10-0.8.5.jar'
if (!(Test-Path $jarPath)) { throw 'Build the Stage 13D artifact before running this validator.' }
$entries = @(jar tf $jarPath)
function Require-Entry([string]$entry) { if ($entry -notin $entries) { throw "Missing packaged Beach resource: $entry" } }
function Require-Reference([string]$relative) { if (!(Test-Path (Join-Path $reference $relative))) { throw "Missing released reference resource: $relative" } }

$java = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/init/BMEntities.java') -Raw
$crab = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/entity/HelmitCrabEntity.java') -Raw
$worldgen = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/init/BMWorldgen.java') -Raw
$items = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/init/BMItems.java') -Raw
$client = Get-Content (Join-Path $Root 'src/client/java/party/lemons/biomemakeover/client/BiomeMakeoverClient.java') -Raw
if ($java -notmatch 'EntityType<HelmitCrabEntity> HELMIT_CRAB') { throw 'Helmit Crab entity registration missing.' }
if ($java -notmatch 'HelmitCrabEntity.createAttributes') { throw 'Helmit Crab attributes missing.' }
if ($java -notmatch [regex]::Escape('SpawnPlacements.register(HELMIT_CRAB')) { throw 'Helmit Crab spawn restriction missing.' }
if ($worldgen -notmatch 'BiomeMakeover.id\("beaches"\)' -or $worldgen -notmatch 'BMEntities.HELMIT_CRAB') { throw 'Beach biome spawn integration missing.' }
if ($items -notmatch 'RAW_CRAB' -or $items -notmatch 'COOKED_CRAB' -or $items -notmatch 'CRAB_CHOWDER') { throw 'Crab food registrations missing.' }
if ($client -notmatch 'BMEntities.HELMIT_CRAB.*HelmitCrabRenderer|HelmitCrabRenderer.*BMEntities.HELMIT_CRAB') { throw 'Helmit Crab renderer registration missing.' }
if ($crab -notmatch 'out\.store\("Shell",\s*ItemStack\.OPTIONAL_CODEC') { throw 'Helmit Crab shell persistence does not use the empty-safe ItemStack codec.' }
if ($crab -notmatch 'in\.read\("Shell",\s*ItemStack\.OPTIONAL_CODEC') { throw 'Helmit Crab shell persistence read path does not use the empty-safe ItemStack codec.' }
if ($crab -match 'Shell",\s*ItemStack\.CODEC') { throw 'Helmit Crab shell persistence still encodes shell state with the non-empty ItemStack codec.' }

$required = @(
  'data/biomemakeover/tags/worldgen/biome/beaches.json',
  'data/biomemakeover/tags/block/crab_spawnable_on.json',
  'data/biomemakeover/loot_table/entities/helmit_crab.json',
  'data/biomemakeover/recipe/crab_chowder.json',
  'data/biomemakeover/recipe/cooking/cooked_crab.json',
  'data/biomemakeover/recipe/cooking/cooked_crab_smoking.json',
  'data/biomemakeover/recipe/cooking/cooked_crab_campfire.json',
  'assets/biomemakeover/textures/entity/helmit_crab/helmit_crab.png',
  'assets/biomemakeover/textures/entity/helmit_crab/shulker.png',
  'assets/biomemakeover/textures/item/raw_crab.png',
  'assets/biomemakeover/textures/item/cooked_crab.png',
  'assets/biomemakeover/textures/item/crab_chowder.png',
  'assets/biomemakeover/textures/item/helmit_crab_spawn_egg.png',
  'assets/biomemakeover/models/item/raw_crab.json',
  'assets/biomemakeover/models/item/cooked_crab.json',
  'assets/biomemakeover/models/item/crab_chowder.json',
  'assets/biomemakeover/models/item/helmit_crab_spawn_egg.json'
)
foreach ($entry in $required) { Require-Entry $entry }
foreach ($relative in @(
  'data/biomemakeover/tags/worldgen/biome/beaches.json',
  'data/biomemakeover/tags/blocks/crab_spawnable_on.json',
  'data/biomemakeover/loot_tables/entities/helmit_crab.json',
  'data/biomemakeover/recipes/crab_chowder.json',
  'data/biomemakeover/recipes/cooking/cooked_crab.json',
  'data/biomemakeover/recipes/cooking/cooked_crab_smoking.json',
  'data/biomemakeover/recipes/cooking/cooked_crab_campfire.json')) { Require-Reference $relative }

if ($java -match 'ghosttown|GhostTown|beach_boat|BeachBoat') { throw 'Stage 13D contains out-of-scope Beach/Ghost Town leakage.' }
if ($items -match 'blighted_balsa_(boat|chest_boat)') { throw 'Blighted Balsa boats remain deferred.' }
$mansion = & powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $Root 'validation/Invoke-Stage11AMansionInventory.ps1') -Root $Root
if (($mansion -join "`n") -notmatch 'templates=168.*active_unique=165.*orphan=3') { throw 'Mansion inventory drift detected.' }
Write-Output 'STAGE 13D BEACH VALIDATION PASSED: Helmit Crab registration/spawn/client path, packaged crab food/loot/resources, and empty-safe shell persistence verified.'
Write-Output 'Beach tag and crab-spawnable block tag are reference-backed; Mushroom Fields, Ghost Town, Dark Forest, Badlands, boats, and Mansion remain out of scope.'
