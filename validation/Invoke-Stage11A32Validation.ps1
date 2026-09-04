[CmdletBinding()]
param([string]$Root = (Get-Location).Path)
$ErrorActionPreference = 'Stop'

$java = Join-Path $Root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionFeature.java'
$structure = Join-Path $Root 'src/main/resources/data/biomemakeover/worldgen/structure/mansion.json'
$set = Join-Path $Root 'src/main/resources/data/biomemakeover/worldgen/structure_set/mansions.json'
$tag = Join-Path $Root 'src/main/resources/data/biomemakeover/tags/worldgen/biome/has_structure/reworked_mansion.json'
$dark = Join-Path $Root 'src/main/resources/data/biomemakeover/tags/worldgen/biome/dark_forest.json'
foreach ($path in @($java,$structure,$set,$tag,$dark)) { if (-not (Test-Path -LiteralPath $path)) { throw "Missing Mansion worldgen resource: $path" } }

$source = Get-Content -LiteralPath $structure -Raw | ConvertFrom-Json
if ($source.type -ne 'biomemakeover:mansion') { throw 'Mansion structure type is not biomemakeover:mansion' }
if ($source.biomes -ne '#biomemakeover:has_structure/reworked_mansion') { throw 'Mansion biome tag chain is incorrect' }
if ($source.step -ne 'surface_structures') { throw 'Mansion generation step is incorrect' }
if ($source.terrain_adaptation -ne 'beard_box') { throw 'Mansion terrain adaptation is incorrect' }
if (-not $source.templates -or -not $source.details) { throw 'Mansion structure codec payload is incomplete' }

$placement = Get-Content -LiteralPath $set -Raw | ConvertFrom-Json
$entries = @($placement.structures)
if ($entries.Count -ne 1 -or $entries[0].structure -ne 'biomemakeover:mansion') { throw 'Mansion structure-set entry missing or duplicated' }
if ($placement.placement.spacing -ne 32 -or $placement.placement.separation -ne 9 -or $placement.placement.salt -ne 420 -or $placement.placement.spread_type -ne 'linear' -or $placement.placement.type -ne 'minecraft:random_spread') { throw 'Mansion placement parameters differ from released contract' }

$tagData = Get-Content -LiteralPath $tag -Raw | ConvertFrom-Json
if (@($tagData.values) -notcontains '#biomemakeover:dark_forest') { throw 'Mansion reworked_mansion tag does not reference dark_forest' }
$darkData = Get-Content -LiteralPath $dark -Raw | ConvertFrom-Json
if (@($darkData.values) -notcontains 'minecraft:dark_forest') { throw 'Dark forest tag does not resolve to minecraft:dark_forest' }

$sourceText = Get-Content -LiteralPath $java -Raw
if ($sourceText -match 'findGenerationPoint\s*\([^)]*\)\s*\{\s*return\s+Optional\.empty\(\)') { throw 'Mansion generation point remains inert' }
if ($sourceText -match 'party\.lemons\.taniwha') { throw 'Taniwha runtime dependency detected' }

$templates = @(Get-ChildItem (Join-Path $Root 'src/main/resources/data/biomemakeover/structure/mansion') -Recurse -Filter *.nbt)
if ($templates.Count -ne 168) { throw "Expected 168 Mansion templates, found $($templates.Count)" }
if (Test-Path (Join-Path $Root 'src/main/resources/data/biomemakeover/worldgen/structure/woodland_mansion.json')) { throw 'Vanilla Mansion override detected' }

$allJava = (Get-ChildItem (Join-Path $Root 'src/main/java') -Recurse -Filter *.java | Get-Content -Raw) -join "`n"
foreach ($forbidden in @('MIMIC','STONE_GOLEM')) { if ($allJava -match $forbidden) { throw "Stage 11B/12 gameplay marker activation detected: $forbidden" } }
Write-Output 'STAGE 11A.3.2 VALIDATION PASSED (Mansion activation resources, placement contract, biome chain, templates=168)'
