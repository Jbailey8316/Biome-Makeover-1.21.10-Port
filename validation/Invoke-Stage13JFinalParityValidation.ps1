param(
    [string]$Root = (Split-Path -Parent $PSScriptRoot),
    [string]$Jar = (Join-Path (Split-Path -Parent $PSScriptRoot) 'build/libs/biomemakeover-fabric-1.21.10-0.8.5.jar')
)
$ErrorActionPreference = 'Stop'
$doc = Join-Path $Root 'docs/parity/stage-13j-final-released-parity-closure.md'
$matrix = Join-Path $Root 'docs/parity/stage-13i-badlands-dark-forest-boundary.md'
foreach ($path in @($doc,$matrix,(Join-Path $Root 'validation/Invoke-Stage13IBiomeBoundaryValidation.ps1'))) {
    if (-not (Test-Path -LiteralPath $path)) { throw "Missing final parity audit resource: $path" }
}
$docText = Get-Content $doc -Raw
if ($docText -match '\bUNKNOWN\b') { throw 'Final parity matrix contains an UNKNOWN classification' }
foreach ($token in @('COMPLETE','CURRENT-MINECRAFT ADAPTED','SHARED / FROZEN','RELEASED ORPHAN','RELEASED DISABLED / UNREACHABLE')) {
    if ($docText -notmatch [regex]::Escape($token)) { throw "Final parity matrix lacks classification: $token" }
}
$jarEntries = @(jar tf $Jar)
function Require-Jar([string]$path) { if (-not ($jarEntries -contains $path)) { throw "Missing packaged final-parity resource: $path" } }
foreach ($path in @(
    'data/biomemakeover/worldgen/configured_feature/badlands/barrel_cactus.json',
    'data/biomemakeover/worldgen/configured_feature/dark_forest/ancient_oak.json',
    'data/biomemakeover/worldgen/placed_feature/badlands/saguaro_cactus.json',
    'data/biomemakeover/worldgen/placed_feature/dark_forest/mesmerite_fissure.json',
    'assets/biomemakeover/textures/entity/owl.png',
    'assets/biomemakeover/textures/entity/scuttler.png',
    'assets/biomemakeover/textures/block/black_thistle.png',
    'assets/biomemakeover/lang/en_us.json')) { Require-Jar $path }
$java = (Get-ChildItem (Join-Path $Root 'src/main/java') -Recurse -Filter *.java | ForEach-Object { Get-Content $_.FullName -Raw }) -join "`n"
foreach ($token in @('class BMBlocks','class BMItems','class BMEntities','class BMWorldgen','class PoltergeistHandler','class WitchQuestHandler','class MansionFeature')) {
    if ($java -notmatch [regex]::Escape($token)) { throw "Missing current released-system anchor: $token" }
}
foreach ($token in @('Ghost Town','Mushroom Fields','Beach','Witch Quest','boats','Badlands','Dark Forest','Toad/Tadpole','Blightbat')) {
    if ($docText -notmatch [regex]::Escape($token)) { throw "Final matrix lacks boundary entry: $token" }
}
if ($docText -notmatch 'd664cccf13ab65bddc7a3d30aa04254bb810e4f1') { throw 'Final matrix lacks the preservation tag target' }
Write-Output 'STAGE 13J FINAL PARITY VALIDATION PASSED: released registrations/resources are accounted for, frozen ownership is referenced, and orphan/disabled boundaries are explicit.'
