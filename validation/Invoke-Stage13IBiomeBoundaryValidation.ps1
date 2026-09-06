param(
    [string]$Root = (Split-Path -Parent $PSScriptRoot),
    [string]$Jar = (Join-Path (Split-Path -Parent $PSScriptRoot) 'build/libs/biomemakeover-fabric-1.21.10-0.8.5.jar')
)
$ErrorActionPreference = 'Stop'
$jarEntries = @(jar tf $Jar)
$java = (Get-ChildItem (Join-Path $Root 'src/main/java') -Recurse -Filter *.java | ForEach-Object { Get-Content $_.FullName -Raw }) -join "`n"

function Require-JarEntry([string]$path) {
    if (-not ($jarEntries -contains $path)) { throw "Missing packaged boundary resource: $path" }
}
function Require-Source([string]$token, [string]$description) {
    if ($java -notmatch [regex]::Escape($token)) { throw "Missing current boundary hook: $description ($token)" }
}

$badlandsPlaced = @('barrel_cactus','saguaro_cactus','paydirt','surface_fossil')
foreach ($id in $badlandsPlaced) {
    Require-JarEntry "data/biomemakeover/worldgen/configured_feature/badlands/$id.json"
    Require-JarEntry "data/biomemakeover/worldgen/placed_feature/badlands/$id.json"
}
foreach ($id in @('BADLANDS_BARREL_CACTUS','BADLANDS_SAGUARO_CACTUS','BADLANDS_PAYDIRT','BADLANDS_SURFACE_FOSSIL')) { Require-Source $id 'Badlands placed-feature registration' }
Require-Source 'BiomeTags.IS_BADLANDS' 'Badlands biome integration'
Require-Source 'BMEntities.SCUTTLER' 'Badlands Scuttler spawn integration'
Require-Source 'BMEntities.COWBOY' 'Badlands Cowboy/patrol integration'
Require-Source 'TUMBLEWEED_SPAWNING' 'Badlands Tumbleweed world event'
Require-Source 'BARREL_CACTUS_PLANTABLE' 'Badlands Barrel Cactus tag'
Require-Source 'SAGUARO_CACTUS_PLANTABLE' 'Badlands Saguaro tag'
Require-JarEntry 'data/biomemakeover/tags/worldgen/biome/spawns_tumbleweed.json'

$darkConfigured = @('ancient_oak','ancient_oak_small','dark_oak_small','flowers','itching_ivy','mesmerite_boulder','mesmerite_fissure','mesmerite_underground','tall_grass','trees','wild_mushrooms')
$darkPlaced = @('ancient_oak_checked','ancient_oak_small_checked','dark_oak_small_checked','flowers','grass','itching_ivy','mesmerite_boulder','mesmerite_fissure','mesmerite_underground','tall_grass','trees','wild_mushrooms')
foreach ($id in $darkConfigured) { Require-JarEntry "data/biomemakeover/worldgen/configured_feature/dark_forest/$id.json" }
foreach ($id in $darkPlaced) { Require-JarEntry "data/biomemakeover/worldgen/placed_feature/dark_forest/$id.json" }
foreach ($token in @('DARK_FOREST_GRASS','DARK_FOREST_TALL_GRASS','DARK_FOREST_FLOWERS','DARK_FOREST_ITCHING_IVY','DARK_FOREST_TREES','DARK_FOREST_WILD_MUSHROOMS','DARK_FOREST_FISSURE')) { Require-Source $token 'Dark Forest placed-feature registration' }
Require-Source 'Biomes.DARK_FOREST' 'Dark Forest biome integration'
Require-Source 'BMEntities.OWL' 'Dark Forest Owl integration'
Require-Source 'BMEntities.MOTH' 'Dark Forest Moth integration'
Require-Source 'BLACK_THISTLE' 'Dark Forest Black Thistle integration'
Require-Source 'MOTH_BLOSSOM' 'Dark Forest Moth Blossom integration'
Require-Source 'PoltergeistHandler' 'Shared Poltergeist/soul system'

foreach ($path in @(
    'assets/biomemakeover/textures/entity/owl.png',
    'assets/biomemakeover/textures/entity/moth.png',
    'assets/biomemakeover/textures/entity/scuttler.png',
    'assets/biomemakeover/textures/entity/cowboy.png',
    'assets/biomemakeover/textures/block/black_thistle.png',
    'assets/biomemakeover/textures/block/moth_blossom.png')) { Require-JarEntry $path }

# Final-release boundary decisions: these are not active natural-spawn gaps.
if ($java -match 'registerEntity\("toad"|registerEntity\("blightbat"') { throw 'Disabled/orphan Toad or Blightbat was reopened in the active port' }

Write-Output 'STAGE 13I BIOME BOUNDARY VALIDATION PASSED: Badlands and Dark Forest released systems are wired, packaged, and classified; disabled/orphan boundaries remain closed.'
