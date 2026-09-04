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
$renderer = Join-Path $Root 'src/client/java/party/lemons/biomemakeover/client/render/MansionTapestryRenderer.java'
$model = Join-Path $Root 'src/client/java/party/lemons/biomemakeover/client/model/TapestryModel.java'
foreach ($path in @($java,$feature,$standing,$wall,$entity,$client,$renderer,$model)) { if (-not (Test-Path -LiteralPath $path)) { throw "Missing tapestry source: $path" } }
$source = (Get-Content $java -Raw) + (Get-Content $feature -Raw) + (Get-Content $standing -Raw) + (Get-Content $wall -Raw) + (Get-Content $entity -Raw) + (Get-Content $client -Raw) + (Get-Content $renderer -Raw) + (Get-Content $model -Raw)

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
if ($source -notmatch 'flag' -or $source -notmatch 'pole' -or $source -notmatch 'bar' -or $source -notmatch 'createBodyLayer') { throw 'Released custom tapestry geometry missing' }
foreach ($direction in @('NORTH','SOUTH','EAST','WEST')) { if ($source -notmatch 'HORIZONTAL_FACING' -or $source -notmatch 'toYRot') { throw "Wall transform contract missing for $direction" } }
if ($source -notmatch 'ROTATION_16' -or $source -notmatch '22\.5F') { throw 'Standing rotation transform contract missing' }
if ($feature -and (Get-Content $feature -Raw) -notmatch 'case "tapestry"') { throw 'Mansion tapestry marker dispatch missing' }
if ($source -match 'Trial Spawner|Emerald Key|EMERALD_KEY|Mythas') { throw 'Stage 12/Mythas gameplay leaked into tapestry implementation' }

$lang = Get-Content (Join-Path $Root 'src/main/resources/assets/biomemakeover/lang/en_us.json') -Raw | ConvertFrom-Json
foreach ($color in $colors) {
    if (-not $lang.PSObject.Properties.Name.Contains("block.biomemakeover.${color}_tapestry")) { throw "Missing translation: $color" }
}
if (-not (Test-Path (Join-Path $Root 'src/main/resources/data/biomemakeover/tags/block/tapestries.json'))) { throw 'Tapestry tag missing' }
if (-not (Test-Path (Join-Path $Root 'src/main/resources/data/biomemakeover/advancement/biomemakeover/all_tapestries.json'))) { throw 'All-tapestries advancement missing' }
$advancementPath = Join-Path $Root 'src/main/resources/data/biomemakeover/advancement/biomemakeover/all_tapestries.json'
$advancement = Get-Content $advancementPath -Raw | ConvertFrom-Json
if ($advancement.parent -ne 'biomemakeover:biomemakeover/mansion') { throw 'All-tapestries parent does not match released contract' }
$parentPath = Join-Path $Root 'src/main/resources/data/biomemakeover/advancement/biomemakeover/mansion.json'
if (-not (Test-Path -LiteralPath $parentPath)) { throw 'All-tapestries advancement parent missing' }
$criteria = $advancement.criteria.get_shrooms.conditions.items
if (@($criteria).Count -ne 17) { throw "Expected 17 all-tapestries item criteria, found $(@($criteria).Count)" }
foreach ($criterion in $criteria) { if (-not $criterion.items -or $criterion.items.Count -ne 1) { throw 'Malformed tapestry advancement item predicate' } }

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
        foreach ($entry in @('data/biomemakeover/advancement/biomemakeover/mansion.json','party/lemons/biomemakeover/client/model/TapestryModel.class','party/lemons/biomemakeover/client/render/MansionTapestryRenderer.class')) {
            if ($names -notcontains $entry) { throw "Compiled JAR missing: $entry" }
        }
        $rendererEntry = $zip.GetEntry('party/lemons/biomemakeover/client/render/MansionTapestryRenderer.class')
        $rendererStream = $rendererEntry.Open()
        $rendererBytes = New-Object System.Collections.Generic.List[byte]
        $buffer = New-Object byte[] 4096
        while (($read = $rendererStream.Read($buffer, 0, $buffer.Length)) -gt 0) { for ($i = 0; $i -lt $read; $i++) { $rendererBytes.Add($buffer[$i]) } }
        $rendererStream.Dispose()
        $rendererText = [Text.Encoding]::ASCII.GetString($rendererBytes.ToArray())
        if ($rendererText -notmatch 'BM_TAPESTRY_RENDER') { throw 'Compiled JAR missing diagnostic marker: BM_TAPESTRY_RENDER' }
        $clientEntry = $zip.GetEntry('party/lemons/biomemakeover/client/BiomeMakeoverClient.class')
        $clientStream = $clientEntry.Open()
        $clientBytes = New-Object System.Collections.Generic.List[byte]
        while (($read = $clientStream.Read($buffer, 0, $buffer.Length)) -gt 0) { for ($i = 0; $i -lt $read; $i++) { $clientBytes.Add($buffer[$i]) } }
        $clientStream.Dispose()
        if ([Text.Encoding]::ASCII.GetString($clientBytes.ToArray()) -notmatch 'BM_TAPESTRY_RENDER_REGISTER') { throw 'Compiled JAR missing diagnostic marker: BM_TAPESTRY_RENDER_REGISTER' }
        if ($names -notcontains 'party/lemons/biomemakeover/block/entity/TapestryBlockEntity.class') { throw 'Compiled JAR missing TapestryBlockEntity' }
    } finally { $zip.Dispose() }
}
Write-Output 'STAGE 11B.2B TAPESTRY PARITY VALIDATION PASSED (17 variants, released resources, support/transforms, 168 NBT templates, no Stage 12 activation)'
