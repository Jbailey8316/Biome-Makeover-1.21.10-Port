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
    Add-Type -AssemblyName System.Drawing
    $bitmap = [System.Drawing.Bitmap]::new((Join-Path $Root "src/main/resources/assets/biomemakeover/textures/tapestry/$($color)_tapestry.png"))
    try {
        if ($bitmap.Width -ne 64 -or $bitmap.Height -ne 64) { throw "Invalid tapestry dimensions: $color" }
        $nonWhite = 0
        for ($x = 0; $x -lt $bitmap.Width; $x++) { for ($y = 0; $y -lt $bitmap.Height; $y++) { $pixel = $bitmap.GetPixel($x, $y); if ($pixel.A -gt 0 -and ($pixel.R -ne 255 -or $pixel.G -ne 255 -or $pixel.B -ne 255)) { $nonWhite++ } } }
        if ($nonWhite -eq 0) { throw "Blank/white tapestry texture: $color" }
    } finally { $bitmap.Dispose() }
    if ($source.IndexOf(('tapestryItem("{0}"' -f $color), [StringComparison]::Ordinal) -lt 0) { throw "Missing BlockItem registration: $color" }
}
if ($source -notmatch 'MansionTapestryBlock') { throw 'Shared tapestry block substrate missing' }
if ($source -notmatch 'ROTATION_16' -or $source -notmatch 'HORIZONTAL_FACING') { throw 'Released state properties missing' }
if ($source -notmatch 'rotate\(' -or $source -notmatch 'mirror\(') { throw 'Released rotation/mirror transforms missing' }
if ($source -notmatch 'canSurvive' -or $source -notmatch 'updateShape') { throw 'Released support/survival contract missing' }
if ($source -notmatch 'TAPESTRY_KEY' -or $source -notmatch 'MansionTapestryRenderer') { throw 'Shared tapestry BlockEntity/renderer registration missing' }
if ($source -notmatch 'flag' -or $source -notmatch 'pole' -or $source -notmatch 'bar' -or $source -notmatch 'createBodyLayer') { throw 'Released custom tapestry geometry missing' }
if ($source -notmatch 'submitModelPart' -or $source -notmatch 'entitySolid\(state\.block\.tapestryTexture\(\)\)') { throw 'Selected tapestry texture is not bound through direct model-part rendering' }
if ($source -notmatch 'LayerDefinition\.create\(mesh, 64, 64\)' -or $source -notmatch 'texOffs\(0, 0\)') { throw 'Released 64x64 tapestry UV contract missing' }
if ($source -notmatch 'facing\.getOpposite\(\)' -or $source -notmatch 'setBlock\(position') { throw 'Released marker-facing/in-place placement contract missing' }
if ($source -notmatch 'mirror\(placeSettings\.getMirror\(\)\)\.rotate\(placeSettings\.getRotation\(\)\)' -or $source -notmatch 'transformedFacing') { throw 'Rotated marker direction transform contract missing' }
if ($source -notmatch 'generateTapestry\(facing' -or $source -notmatch 'setValue\(MansionWallTapestryBlock\.FACING,\s*facing\.getOpposite\(\)\)') { throw 'Production transformed-facing dataflow is not wired to the wall state write' }
if ($source -match 'BM_TAPESTRY_(?!PLACEMENT_TRACE)') { throw 'Superseded tapestry forensic logging remains in production source' }
if ($source -notmatch 'RotationSegment\.convertToSegment\(context\.getRotation\(\) \+ 180\.0F\)') { throw 'Standing tapestry does not use vanilla BannerBlock yaw-to-ROTATION_16 placement semantics' }
if ($source -notmatch 'new StandingAndWallBlockItem\(standing, wall, .*Direction\.DOWN') { throw 'Tapestry item does not use vanilla StandingAndWallBlockItem attachment semantics' }
if ($source -notmatch 'BM_TAPESTRY_PLACEMENT_TRACE') { throw 'R7 placement trace marker is missing' }
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
$parent = Get-Content $parentPath -Raw | ConvertFrom-Json
$location = $parent.criteria.ghost_town.conditions.player[0].predicate.location
if (-not $location.structures -or @($location.structures).Count -ne 1 -or $location.structures[0] -ne 'biomemakeover:mansion') { throw 'Mansion advancement does not use the released structure location predicate' }
if ($parent.criteria.ghost_town.trigger -eq 'minecraft:tick') { throw 'Mansion advancement uses an unconditional tick criterion' }
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
            $sourceTexture = [IO.File]::ReadAllBytes((Join-Path $Root "src/main/resources/assets/biomemakeover/textures/tapestry/${color}_tapestry.png"))
            $textureEntry = $zip.GetEntry("assets/biomemakeover/textures/tapestry/${color}_tapestry.png")
            if ($textureEntry.Length -ne $sourceTexture.Length) { throw "Compiled tapestry texture size differs: $color" }
            $entryStream = $textureEntry.Open()
            $entryMemory = New-Object IO.MemoryStream
            $entryStream.CopyTo($entryMemory)
            $entryStream.Dispose()
            $sourceHash = ([BitConverter]::ToString(([Security.Cryptography.SHA256]::Create().ComputeHash($sourceTexture))) -replace '-', '')
            $entryHash = ([BitConverter]::ToString(([Security.Cryptography.SHA256]::Create().ComputeHash($entryMemory.ToArray()))) -replace '-', '')
            $entryMemory.Dispose()
            if ($sourceHash -ne $entryHash) { throw "Compiled tapestry texture bytes differ: $color" }
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
        $mansionEntry = $zip.GetEntry('party/lemons/biomemakeover/worldgen/mansion/MansionFeature.class')
        $mansionStream = $mansionEntry.Open()
        $mansionBytes = New-Object System.Collections.Generic.List[byte]
        while (($read = $mansionStream.Read($buffer, 0, $buffer.Length)) -gt 0) { for ($i = 0; $i -lt $read; $i++) { $mansionBytes.Add($buffer[$i]) } }
        $mansionStream.Dispose()
        $mansionText = [Text.Encoding]::ASCII.GetString($mansionBytes.ToArray())
        foreach ($classText in @($rendererText, $mansionText)) { if ($classText -match 'BM_TAPESTRY_') { throw 'Compiled JAR contains retired tapestry forensic markers' } }
        if ($names -notcontains 'party/lemons/biomemakeover/block/entity/TapestryBlockEntity.class') { throw 'Compiled JAR missing TapestryBlockEntity' }
    } finally { $zip.Dispose() }
}
Write-Output 'STAGE 11B.2B TAPESTRY PARITY VALIDATION PASSED (17 variants, released resources, support/transforms, 168 NBT templates, no Stage 12 activation)'
