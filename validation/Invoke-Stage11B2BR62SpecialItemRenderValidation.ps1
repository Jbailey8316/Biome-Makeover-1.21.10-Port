[CmdletBinding()]
param(
    [string]$Root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path,
    [string]$Jar = ''
)
$ErrorActionPreference = 'Stop'
$colors = @('white','orange','magenta','light_blue','yellow','lime','pink','gray','light_gray','cyan','purple','blue','brown','green','red','black','adjudicator')
$itemRoot = Join-Path $Root 'src/main/resources/assets/biomemakeover/items'
$template = Join-Path $Root 'src/main/resources/assets/biomemakeover/models/item/template_tapestry.json'
$renderer = Join-Path $Root 'src/client/java/party/lemons/biomemakeover/client/render/MansionTapestryItemSpecialRenderer.java'
$mixin = Join-Path $Root 'src/client/java/party/lemons/biomemakeover/mixin/client/SpecialModelRenderersMixin.java'
$mixinConfig = Join-Path $Root 'src/main/resources/biomemakeover.client.mixins.json'
foreach ($path in @($template,$renderer,$mixin,$mixinConfig)) { if (-not (Test-Path -LiteralPath $path)) { throw "Missing special-item resource/source: $path" } }
$templateJson = Get-Content $template -Raw | ConvertFrom-Json
if ($templateJson.parent) { throw 'Tapestry special base still references a normal-model parent' }
$mixinText = Get-Content $mixin -Raw
if ($mixinText -notmatch 'SpecialModelRenderers' -or $mixinText -notmatch 'ID_MAPPER\.put' -or $mixinText -notmatch 'biomemakeover.*tapestry') { throw 'Special model codec is not registered in the vanilla mapper' }
$rendererText = Get-Content $renderer -Raw
foreach ($needle in @('NoDataSpecialModelRenderer','RecordCodecBuilder','variant','RenderType.entitySolid','submitModelPart','TAPESTRY')) { if ($rendererText -notmatch [regex]::Escape($needle)) { throw "Special renderer contract missing: $needle" } }
$config = Get-Content $mixinConfig -Raw | ConvertFrom-Json
if (-not @($config.client) -contains 'SpecialModelRenderersMixin') { throw 'Special model registration mixin is not enabled on the client' }
foreach ($color in $colors) {
    $id = "${color}_tapestry"
    $path = Join-Path $itemRoot "$id.json"
    if (-not (Test-Path $path)) { throw "Missing item definition: $id" }
    $json = Get-Content $path -Raw | ConvertFrom-Json
    if ($json.model.type -ne 'minecraft:special' -or $json.model.base -ne 'biomemakeover:item/template_tapestry' -or $json.model.model.type -ne 'biomemakeover:tapestry' -or $json.model.model.variant -ne $color) { throw "Invalid special item schema: $id" }
    if ((Get-Content (Join-Path $Root "src/main/resources/assets/biomemakeover/models/item/$id.json") -Raw) -match 'builtin/entity') { throw "Unresolved builtin/entity dependency: $id" }
}
if (-not [string]::IsNullOrWhiteSpace($Jar)) {
    if (-not (Test-Path $Jar)) { throw "JAR missing: $Jar" }
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $zip = [IO.Compression.ZipFile]::OpenRead((Resolve-Path $Jar))
    try {
        $names = @($zip.Entries | ForEach-Object FullName)
        foreach ($color in $colors) {
            $id = "${color}_tapestry"
            $entry = "assets/biomemakeover/items/$id.json"
            if ($names -notcontains $entry) { throw "Compiled special item definition missing: $id" }
            $stream = $zip.GetEntry($entry).Open(); $reader = [IO.StreamReader]::new($stream); $json = $reader.ReadToEnd(); $reader.Dispose(); $stream.Dispose()
            if ($json -match 'builtin/entity' -or $json -notmatch 'minecraft:special' -or $json -notmatch 'biomemakeover:tapestry') { throw "Compiled item does not use the special path: $id" }
        }
        foreach ($entry in @('party/lemons/biomemakeover/client/render/MansionTapestryItemSpecialRenderer.class','party/lemons/biomemakeover/mixin/client/SpecialModelRenderersMixin.class')) { if ($names -notcontains $entry) { throw "Compiled special renderer class missing: $entry" } }
        foreach ($entry in @('assets/biomemakeover/models/item/template_tapestry.json')) { if ($names -notcontains $entry) { throw "Compiled special base missing: $entry" } }
    } finally { $zip.Dispose() }
}
Write-Output 'STAGE 11B.2B R.6.2 SPECIAL ITEM RENDER VALIDATION PASSED (17 variants, native special codec, no builtin/entity dependency)'
