[CmdletBinding()]
param([string]$Root = (Get-Location).Path)
$ErrorActionPreference = 'Stop'
$recipeRoot = Join-Path $Root 'src/main/resources/data/biomemakeover/recipe'
$advRoot = Join-Path $Root 'src/main/resources/data/biomemakeover/advancement/recipes/building_blocks'
$crude = Get-Content (Join-Path $recipeRoot 'crude_cladding.json') -Raw | ConvertFrom-Json
$cladded = Get-Content (Join-Path $recipeRoot 'cladded_stone.json') -Raw | ConvertFrom-Json
if ($crude.type -ne 'minecraft:crafting_shaped' -or (($crude.pattern -join ',') -ne '##,##') -or $crude.key.'#' -ne 'biomemakeover:crude_fragment' -or $crude.result.id -ne 'biomemakeover:crude_cladding' -or $crude.result.count -ne 1) { throw 'Released crude_cladding recipe mismatch' }
if ($cladded.type -ne 'minecraft:crafting_shapeless' -or $cladded.result.id -ne 'biomemakeover:cladded_stone' -or $cladded.result.count -ne 4) { throw 'Released cladded_stone recipe mismatch' }
$ingredients = @($cladded.ingredients)
if (($ingredients | Where-Object { $_ -eq 'biomemakeover:crude_cladding' }).Count -ne 1 -or ($ingredients | Where-Object { $_ -eq 'minecraft:smooth_stone' }).Count -ne 4) { throw 'cladded_stone ingredients mismatch' }
foreach ($name in @('crude_cladding','cladded_stone')) {
    $path = Join-Path $advRoot "$name.json"
    if (-not (Test-Path $path)) { throw "Missing recipe unlock advancement: $name" }
    $adv = Get-Content $path -Raw | ConvertFrom-Json
    if ($adv.parent -ne 'minecraft:recipes/root' -or $adv.rewards.recipes -notcontains "biomemakeover:$name" -or $adv.criteria.has_ingredient.trigger -ne 'minecraft:inventory_changed') { throw "Invalid recipe unlock advancement: $name" }
}
if ((Get-Content (Join-Path $Root 'src/main/resources/data/biomemakeover/recipe/crude_cladding.json') -Raw) -match 'terracotta') { throw 'Non-released terracotta recipe remains' }
$templates = @(Get-ChildItem (Join-Path $Root 'src/main/resources/data/biomemakeover/structure/mansion') -Recurse -Filter *.nbt)
if ($templates.Count -ne 168) { throw "Expected 168 Mansion templates, found $($templates.Count)" }
Write-Output 'STAGE 12A.9-R4 RECIPE VALIDATION PASSED'
