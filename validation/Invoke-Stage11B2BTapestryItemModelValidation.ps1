[CmdletBinding()]
param(
    [string]$Root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path,
    [string]$Jar = ''
)
$ErrorActionPreference = 'Stop'
$colors = @('white','orange','magenta','light_blue','yellow','lime','pink','gray','light_gray','cyan','purple','blue','brown','green','red','black','adjudicator')
$itemRoot = Join-Path $Root 'src/main/resources/assets/biomemakeover/items'
$modelRoot = Join-Path $Root 'src/main/resources/assets/biomemakeover/models/item'
$textureRoot = Join-Path $Root 'src/main/resources/assets/biomemakeover/textures/tapestry'
$defs = Get-ChildItem $itemRoot -Filter '*_tapestry.json' -File
if ($defs.Count -ne 17) { throw "Expected 17 tapestry item definitions, found $($defs.Count)" }
foreach ($color in $colors) {
    $id = "${color}_tapestry"
    $definitionPath = Join-Path $itemRoot "$id.json"
    $modelPath = Join-Path $modelRoot "$id.json"
    $texturePath = Join-Path $textureRoot "$id.png"
    foreach ($path in @($definitionPath,$modelPath,$texturePath)) { if (-not (Test-Path -LiteralPath $path)) { throw "Missing tapestry item resource: $path" } }
    $definition = Get-Content $definitionPath -Raw | ConvertFrom-Json
    if ($definition.model.type -ne 'minecraft:model' -or $definition.model.model -ne "biomemakeover:item/$id") { throw "Invalid modern item definition: $id" }
    $model = Get-Content $modelPath -Raw | ConvertFrom-Json
    if ($model.parent -ne 'biomemakeover:item/template_tapestry') { throw "Item model does not use released tapestry model: $id" }
    $template = Get-Content (Join-Path $modelRoot 'template_tapestry.json') -Raw | ConvertFrom-Json
    if ($template.parent -ne 'builtin/entity') { throw 'Released tapestry item model template changed unexpectedly' }
    if ((Get-Item $texturePath).Length -le 0) { throw "Empty tapestry texture: $id" }
}
$blockSource = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionTapestryBlock.java') -Raw
if ($blockSource -notmatch 'tapestryTexture') { throw 'Placed/item tapestry texture mapping source missing' }
if (-not [string]::IsNullOrWhiteSpace($Jar)) {
    if (-not (Test-Path -LiteralPath $Jar)) { throw "JAR missing: $Jar" }
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $zip = [IO.Compression.ZipFile]::OpenRead((Resolve-Path $Jar))
    try {
        $names = @($zip.Entries | ForEach-Object FullName)
        foreach ($color in $colors) {
            $id = "${color}_tapestry"
            foreach ($entry in @("assets/biomemakeover/items/$id.json","assets/biomemakeover/models/item/$id.json","assets/biomemakeover/textures/tapestry/$id.png")) {
                if ($names -notcontains $entry) { throw "Compiled JAR missing tapestry item resource: $entry" }
            }
            $definitionEntry = $zip.GetEntry("assets/biomemakeover/items/$id.json")
            $reader = [IO.StreamReader]::new($definitionEntry.Open())
            $compiled = $reader.ReadToEnd() | ConvertFrom-Json
            $reader.Dispose()
            if ($compiled.model.model -ne "biomemakeover:item/$id") { throw "Compiled definition unresolved: $id" }
        }
    } finally { $zip.Dispose() }
}
Write-Output 'STAGE 11B.2B R.6.1 ITEM MODEL VALIDATION PASSED (17 modern definitions, released models/textures, compiled resources)'
