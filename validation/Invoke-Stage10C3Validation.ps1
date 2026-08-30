[CmdletBinding()]
param([string]$Root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path)
$ErrorActionPreference = 'Stop'
$src = Join-Path $Root 'src/main'
$resources = Join-Path $src 'resources'
function Require-Path([string]$p) { if (!(Test-Path (Join-Path $Root $p))) { throw "Missing Stage 10C.3 path: $p" } }
function Require-Text([string]$p,[string]$pattern) { if (!(Select-String -LiteralPath (Join-Path $Root $p) -Pattern $pattern -SimpleMatch -Quiet)) { throw "Missing '$pattern' in $p" } }
function Require-Packaged([string]$p) { Require-Path "build/resources/main/$p" }

# Registrations and server-side behavior.
Require-Text 'src/main/java/party/lemons/biomemakeover/init/BMBlocks.java' 'ECTOPLASM_COMPOSTER'
Require-Text 'src/main/java/party/lemons/biomemakeover/init/BMBlocks.java' 'POLTERGEIST = register'
Require-Text 'src/main/java/party/lemons/biomemakeover/init/BMBlockEntities.java' 'BlockEntityType<PoltergeistBlockEntity>'
Require-Text 'src/main/java/party/lemons/biomemakeover/level/PoltergeistHandler.java' 'registerBehaviour(BlockTags.DOORS'
Require-Text 'src/main/java/party/lemons/biomemakeover/level/PoltergeistHandler.java' 'registerBehaviour(BlockTags.BUTTONS'
Require-Text 'src/main/java/party/lemons/biomemakeover/level/PoltergeistHandler.java' 'registerBehaviour(BlockTags.TRAPDOORS'
Require-Text 'src/main/java/party/lemons/biomemakeover/level/PoltergeistHandler.java' 'registerBehaviour(Blocks.LEVER'
Require-Text 'src/main/java/party/lemons/biomemakeover/level/PoltergeistHandler.java' 'registerBehaviour(Blocks.NOTE_BLOCK'
Require-Text 'src/main/java/party/lemons/biomemakeover/level/PoltergeistHandler.java' 'registerBehaviour(BlockTags.FENCE_GATES'
Require-Text 'src/main/java/party/lemons/biomemakeover/level/PoltergeistHandler.java' 'registerBehaviour(Blocks.DAYLIGHT_DETECTOR'
Require-Text 'src/main/java/party/lemons/biomemakeover/level/PoltergeistHandler.java' 'registerBehaviour(Blocks.BELL'
Require-Text 'src/main/java/party/lemons/biomemakeover/mobeffect/PossessedEffect.java' 'Math.min(amplifier + 1, 20)'
Require-Text 'src/main/java/party/lemons/biomemakeover/mobeffect/PossessedEffect.java' 'entity.blockPosition(), 4'
Require-Text 'src/main/java/party/lemons/biomemakeover/block/entity/PoltergeistBlockEntity.java' 'pos, 5'
Require-Text 'src/main/java/party/lemons/biomemakeover/block/PoltergeistBlock.java' 'ENABLED'
Require-Text 'src/main/java/party/lemons/biomemakeover/block/PoltergeistBlock.java' 'POLTERGEIST_YOURSELF'
Require-Text 'src/main/java/party/lemons/biomemakeover/block/EctoplasmComposterBlock.java' 'Blocks.SOUL_SOIL'
Require-Text 'src/main/java/party/lemons/biomemakeover/item/EctoplasmItem.java' 'BMBlocks.ECTOPLASM_COMPOSTER'

# Exact modern resources and advancement/recipe contracts.
@('assets/biomemakeover/blockstates/ectoplasm_composter.json','assets/biomemakeover/blockstates/poltergeist.json',
  'assets/biomemakeover/items/poltergeist.json','assets/biomemakeover/particles/poltergeist.json',
  'data/biomemakeover/recipe/poltergeist.json','data/biomemakeover/loot_table/blocks/ectoplasm_composter.json',
  'data/biomemakeover/loot_table/blocks/poltergeist.json') | ForEach-Object { Require-Path "src/main/resources/$_"; Require-Packaged $_ }
@('assets/biomemakeover/models/block/poltergeist_on.json','assets/biomemakeover/models/block/poltergeist_off.json',
  'assets/biomemakeover/models/item/poltergeist.json') | ForEach-Object { Require-Path "src/main/resources/$_" }
$recipe = Get-Content (Join-Path $resources 'data/biomemakeover/recipe/poltergeist.json') -Raw | ConvertFrom-Json
if ($recipe.type -ne 'minecraft:crafting_shaped' -or $recipe.result.id -ne 'biomemakeover:poltergeist' -or $recipe.result.count -ne 1) { throw 'Poltergeist recipe header/result mismatch' }
if (($recipe.pattern -join '') -ne 'EEEP SPP C P'.Replace(' ','')) { throw 'Poltergeist recipe pattern mismatch' }
foreach ($key in @('E','P','S','C')) { if (!$recipe.key.$key) { throw "Poltergeist recipe key missing: $key" } }
$advSource = Join-Path $Root 'src/main/java/party/lemons/biomemakeover/init/BMAdvancements.java'
Require-Text 'src/main/java/party/lemons/biomemakeover/init/BMAdvancements.java' 'POLTERGEIST_YOURSELF'
$particle = Get-Content (Join-Path $resources 'assets/biomemakeover/particles/poltergeist.json') -Raw | ConvertFrom-Json
if (@($particle.textures).Count -ne 11) { throw 'Poltergeist particle must contain 11 released frames' }
$sounds = Get-Content (Join-Path $resources 'assets/biomemakeover/sounds.json') -Raw | ConvertFrom-Json
if (!$sounds.poltergeist_action -or !$sounds.poltergeist_toggle) { throw 'Poltergeist sound events missing' }

# Scope guard: later Ghost Town/archaeology systems must not be activated here.
$forbidden = @('ghost_town/','ghosttown','ghost_town_music_disk')
foreach ($needle in $forbidden) {
    $hits = Get-ChildItem $resources -Recurse -File -ErrorAction SilentlyContinue | Select-String -Pattern $needle -SimpleMatch
    if ($hits) { throw "Deferred Stage 10C.4 resource leaked into 10C.3: $needle" }
}
if (Get-ChildItem (Join-Path $resources 'data/biomemakeover/structure') -Recurse -File -ErrorAction SilentlyContinue | Where-Object { $_.FullName -match 'ghost[_-]?town' }) { throw 'Ghost Town structure resources leaked into 10C.3' }
if (Get-ChildItem (Join-Path $resources 'data/biomemakeover/worldgen/structure') -Recurse -File -ErrorAction SilentlyContinue | Where-Object { $_.Name -match 'ghost[_-]?town' }) { throw 'Ghost Town worldgen leaked into 10C.3' }
Write-Output 'STAGE 10C.3 VALIDATION PASSED'
