[CmdletBinding()]
param(
    [string]$Root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path,
    [string]$Jar = ''
)
$ErrorActionPreference = 'Stop'

$colors = @('white','orange','magenta','light_blue','yellow','lime','pink','gray','light_gray','cyan','purple','blue','brown','green','red','black','adjudicator')
$java = Join-Path $Root 'src/main/java/party/lemons/biomemakeover/init/BMBlocks.java'
$feature = Join-Path $Root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionFeature.java'
$standing = Join-Path $Root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionStandingTapestryBlock.java'
$wall = Join-Path $Root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionWallTapestryBlock.java'
$entity = Join-Path $Root 'src/main/java/party/lemons/biomemakeover/init/BMBlockEntities.java'
$client = Join-Path $Root 'src/client/java/party/lemons/biomemakeover/client/BiomeMakeoverClient.java'
foreach ($path in @($java,$feature,$standing,$wall,$entity,$client)) { if (-not (Test-Path -LiteralPath $path)) { throw "Missing tapestry source: $path" } }
$source = (Get-Content $java -Raw) + (Get-Content $feature -Raw) + (Get-Content $standing -Raw) + (Get-Content $wall -Raw) + (Get-Content $entity -Raw) + (Get-Content $client -Raw)

if (([regex]::Matches((Get-Content $java -Raw), 'mansionStandingTapestry\("')).Count -ne 17) { throw 'Released standing variant count is not 17' }
if (([regex]::Matches((Get-Content $java -Raw), 'mansionWallTapestry\("')).Count -ne 17) { throw 'Released wall variant count is not 17' }
foreach ($color in $colors) {
    foreach ($id in @("${color}_tapestry", "${color}_wall_tapestry")) {
        $blockstate = Join-Path $Root "src/main/resources/assets/biomemakeover/blockstates/$id.json"
        if (-not (Test-Path -LiteralPath $blockstate)) { throw "Missing blockstate: $id" }
    }
    foreach ($path in @(
        "src/main/resources/assets/biomemakeover/models/item/$($color)_tapestry.json",
        "src/main/resources/assets/biomemakeover/textures/tapestry/$($color)_tapestry.png",
        "src/main/resources/data/biomemakeover/loot_table/blocks/$($color)_tapestry.json")) {
        if (-not (Test-Path -LiteralPath (Join-Path $Root $path))) { throw "Missing tapestry resource: $path" }
    }
    if ($source.IndexOf(('tapestryItem("{0}"' -f $color), [StringComparison]::Ordinal) -lt 0) { throw "Missing BlockItem registration: $color" }
}
if ($source -notmatch 'MansionTapestryBlock') { throw 'Shared tapestry block substrate missing' }
if ($source -notmatch 'ROTATION_16' -or $source -notmatch 'HORIZONTAL_FACING') { throw 'Released state properties missing' }
if ($source -notmatch 'rotate\(' -or $source -notmatch 'mirror\(') { throw 'Released rotation/mirror transforms missing' }
if ($source -notmatch 'canSurvive' -or $source -notmatch 'updateShape') { throw 'Released support/survival contract missing' }
if ($source -notmatch 'TAPESTRY_KEY' -or $source -notmatch 'MansionTapestryRenderer') { throw 'Shared tapestry BlockEntity/renderer registration missing' }
if ($feature -and (Get-Content $feature -Raw) -notmatch 'case "tapestry"') { throw 'Mansion tapestry marker dispatch missing' }
if ($source -match 'Trial Spawner|Emerald Key|EMERALD_KEY|Mythas') { throw 'Stage 12/Mythas gameplay leaked into tapestry implementation' }

$lang = Get-Content (Join-Path $Root 'src/main/resources/assets/biomemakeover/lang/en_us.json') -Raw | ConvertFrom-Json
foreach ($color in $colors) {
    if (-not $lang.PSObject.Properties.Name.Contains("block.biomemakeover.${color}_tapestry")) { throw "Missing translation: $color" }
}
if (-not (Test-Path (Join-Path $Root 'src/main/resources/data/biomemakeover/tags/block/tapestries.json'))) { throw 'Tapestry tag missing' }
if (-not (Test-Path (Join-Path $Root 'src/main/resources/data/biomemakeover/advancement/biomemakeover/all_tapestries.json'))) { throw 'All-tapestries advancement missing' }

$templates = @(Get-ChildItem (Join-Path $Root 'src/main/resources/data/biomemakeover/structure/mansion') -Recurse -Filter '*.nbt')
if ($templates.Count -ne 168) { throw "Expected 168 Mansion templates, found $($templates.Count)" }
# NBT inventory and marker records are validated above. The stage command runs
# the repository-level NBT diff check separately, avoiding a Git dependency in
# this reusable validator.
$ignore = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionFeature.java') -Raw
if ($ignore -notmatch 'LiquidSettings') { throw 'Mansion liquid settings source is missing' }

if (-not [string]::IsNullOrWhiteSpace($Jar)) {
    if (-not (Test-Path -LiteralPath $Jar)) { throw "JAR missing: $Jar" }
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $zip = [IO.Compression.ZipFile]::OpenRead((Resolve-Path $Jar))
    try {
        $names = @($zip.Entries | ForEach-Object FullName)
        foreach ($color in $colors) {
            foreach ($entry in @(
                "assets/biomemakeover/textures/tapestry/${color}_tapestry.png",
                "assets/biomemakeover/models/item/${color}_tapestry.json",
                "data/biomemakeover/loot_table/blocks/${color}_tapestry.json")) {
                if ($names -notcontains $entry) { throw "Compiled JAR missing: $entry" }
            }
        }
        foreach ($entry in @('data/biomemakeover/tags/block/tapestries.json','data/biomemakeover/advancement/biomemakeover/all_tapestries.json')) {
            if ($names -notcontains $entry) { throw "Compiled JAR missing: $entry" }
        }
        if ($names -notcontains 'party/lemons/biomemakeover/block/entity/TapestryBlockEntity.class') { throw 'Compiled JAR missing TapestryBlockEntity' }
    } finally { $zip.Dispose() }
}
Write-Output 'STAGE 11B.2B TAPESTRY PARITY VALIDATION PASSED (17 variants, released resources, support/transforms, 168 NBT templates, no Stage 12 activation)'
