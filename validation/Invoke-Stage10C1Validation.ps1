[CmdletBinding()]
param([string]$Root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path)
$ErrorActionPreference = 'Stop'
function Require-Path([string]$p) { if (!(Test-Path (Join-Path $Root $p))) { throw "Missing Stage 10C.1 path: $p" } }
function Require-Text([string]$p,[string]$pattern) { $f=Join-Path $Root $p; if (!(Select-String -LiteralPath $f -Pattern $pattern -Quiet)) { throw "Missing '$pattern' in $p" } }
Require-Text 'src/main/java/party/lemons/biomemakeover/init/BMItems.java' 'ECTOPLASM'
Require-Text 'src/main/java/party/lemons/biomemakeover/init/BMEntities.java' 'EntityType<GhostEntity>'
Require-Text 'src/main/java/party/lemons/biomemakeover/init/BMEntities.java' 'GHOST_SPAWN_EGG'
Require-Text 'src/main/java/party/lemons/biomemakeover/init/BMEffects.java' 'POSSESSED'
Require-Path 'src/main/resources/data/biomemakeover/recipe/phantom_membrane.json'
Require-Path 'src/main/resources/data/biomemakeover/loot_table/entities/ghost.json'
if (Test-Path (Join-Path $Root 'src/main/resources/data/biomemakeover/recipes')) { throw 'Obsolete plural recipe directory present' }
if (Test-Path (Join-Path $Root 'src/main/resources/data/biomemakeover/loot_tables/entities/ghost.json')) { throw 'Obsolete plural Ghost loot path present' }
$recipe = Get-Content (Join-Path $Root 'src/main/resources/data/biomemakeover/recipe/phantom_membrane.json') -Raw | ConvertFrom-Json
if ($recipe.type -ne 'minecraft:crafting_shapeless' -or $recipe.result.id -ne 'minecraft:phantom_membrane' -or $recipe.result.count -ne 1) { throw 'Phantom Membrane recipe header/result mismatch' }
$ids = @($recipe.ingredients | ForEach-Object { $_.item })
if (($ids | Where-Object { $_ -eq 'biomemakeover:ectoplasm' }).Count -ne 1 -or ($ids | Where-Object { $_ -eq 'biomemakeover:moth_scales' }).Count -ne 3) { throw 'Phantom Membrane recipe ingredients mismatch' }
@('ghost_angry.ogg','ghost_charge_1.ogg','ghost_charge_2.ogg','ghost_death_1.ogg','ghost_death_2.ogg','ghost_hurt_1.ogg','ghost_hurt_2.ogg','ghost_hurt_3.ogg','ghost_idle_1.ogg','ghost_idle_2.ogg','ghost_idle_3.ogg') | ForEach-Object { Require-Path "src/main/resources/assets/biomemakeover/sounds/$_" }
Require-Path 'src/main/resources/assets/biomemakeover/textures/item/ectoplasm.png'
Require-Path 'src/main/resources/assets/biomemakeover/textures/entity/ghost.png'
$forbidden = @('suspicious_red_sand','ectoplasm_composter','poltergeist','ghost_town','ghosttown','badlands_disc')
foreach ($needle in $forbidden) { $hits = Get-ChildItem (Join-Path $Root 'src/main/resources') -Recurse -File -ErrorAction SilentlyContinue | Select-String -Pattern $needle -SimpleMatch; if ($hits) { throw "Deferred Stage 10C resource leaked: $needle" } }
$taniwha = Get-ChildItem (Join-Path $Root 'src/main/java') -Recurse -File | Select-String -Pattern 'taniwha' -SimpleMatch | Where-Object { $_.Path -match 'Ghost|Ectoplasm|Possessed' }; if ($taniwha) { throw 'Taniwha runtime dependency leaked into 10C.1 foundation' }
Write-Output 'STAGE 10C.1 VALIDATION PASSED'
