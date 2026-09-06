param(
    [string]$Root = (Split-Path -Parent $PSScriptRoot),
    [string]$Jar = (Join-Path (Split-Path -Parent $PSScriptRoot) 'build/libs/biomemakeover-fabric-1.21.10-0.8.5.jar')
)
$ErrorActionPreference = 'Stop'
$ids = @('ancient_oak_boat','ancient_oak_chest_boat','willow_boat','willow_chest_boat',
    'swamp_cypress_boat','swamp_cypress_chest_boat','blighted_balsa_boat','blighted_balsa_chest_boat')
$java = (Get-ChildItem (Join-Path $Root 'src/main/java') -Recurse -Filter *.java | ForEach-Object { Get-Content $_.FullName -Raw }) -join "`n"
$client = (Get-ChildItem (Join-Path $Root 'src/client/java') -Recurse -Filter *.java | ForEach-Object { Get-Content $_.FullName -Raw }) -join "`n"
foreach ($id in $ids) { if ($java -notmatch [regex]::Escape($id)) { throw "Missing boat registration: $id" } }
foreach ($id in $ids) { if ($client -notmatch [regex]::Escape("$id")) { throw "Missing boat renderer/model layer: $id" } }
foreach ($id in $ids) {
    $def = "assets/biomemakeover/items/$id.json"
    $model = "assets/biomemakeover/models/item/$id.json"
    if (-not (jar tf $Jar | Select-String -SimpleMatch $def)) { throw "Missing packaged item definition: $def" }
    if (-not (jar tf $Jar | Select-String -SimpleMatch $model)) { throw "Missing packaged model: $model" }
}
$lang = Get-Content (Join-Path $Root 'src/main/resources/assets/biomemakeover/lang/en_us.json') -Raw | ConvertFrom-Json
foreach ($id in $ids) {
    $key = "item.biomemakeover.$id"
    if (-not $lang.PSObject.Properties.Name.Contains($key)) { throw "Missing boat translation: $key" }
}
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [System.IO.Compression.ZipFile]::OpenRead((Resolve-Path $Jar))
try {
    $langEntry = $archive.GetEntry('assets/biomemakeover/lang/en_us.json')
    if ($null -eq $langEntry) { throw 'Missing packaged BM English language file' }
    $reader = New-Object System.IO.StreamReader($langEntry.Open())
    try { $packagedLang = $reader.ReadToEnd() | ConvertFrom-Json } finally { $reader.Dispose() }
} finally { $archive.Dispose() }
foreach ($id in $ids) {
    $key = "entity.biomemakeover.$id"
    if (-not $packagedLang.PSObject.Properties.Name.Contains($key)) { throw "Missing packaged boat entity translation: $key" }
}
$woods = @('ancient_oak','willow','swamp_cypress','blighted_balsa')
foreach ($wood in $woods) {
    foreach ($path in @("assets/biomemakeover/textures/entity/boat/$wood.png", "assets/biomemakeover/textures/entity/boat/${wood}_chest.png")) {
        if (-not (jar tf $Jar | Select-String -SimpleMatch $path)) { throw "Missing packaged entity texture: $path" }
    }
}
foreach ($wood in $woods) {
    foreach ($layer in @("boat/$wood", "boat/${wood}_chest")) {
        $layerExpression = [regex]::Escape("BiomeMakeover.id(`"$layer`")")
        if ($client -notmatch $layerExpression) { throw "Renderer layer does not map to released boat texture path: $layer" }
    }
}
foreach ($id in $ids) {
    $recipe = "data/biomemakeover/recipe/wood/$($id -replace '_chest_boat$','' -replace '_boat$','')/$id.json"
    if (-not (jar tf $Jar | Select-String -SimpleMatch $recipe)) { throw "Missing packaged recipe: $recipe" }
}
foreach ($tag in @('data/minecraft/tags/item/boats.json','data/minecraft/tags/item/chest_boats.json')) {
    if (-not (jar tf $Jar | Select-String -SimpleMatch $tag)) { throw "Missing packaged tag: $tag" }
}
Write-Output "Stage 13H boat validation PASS: $($ids.Count) released items, native entity types, models, recipes, and tags packaged."
