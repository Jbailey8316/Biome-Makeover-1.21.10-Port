[CmdletBinding()]
param(
    [string]$Root = (Split-Path -Parent $PSScriptRoot),
    [string]$Jar = (Join-Path (Split-Path -Parent $PSScriptRoot) 'build/libs/biomemakeover-fabric-1.21.10-0.8.5.jar')
)

$ErrorActionPreference = 'Stop'
$ids = @('patrol_trial_key','enforcer_trial_key','captain_trial_key','patrol_fragment','enforcer_fragment','captain_fragment','mansion_emerald_key','manor_vault_key')
$items = Join-Path $Root 'src/main/java/party/lemons/biomemakeover/init/BMItems.java'
$lang = Join-Path $Root 'src/main/resources/assets/biomemakeover/lang/en_us.json'
foreach ($path in @($items,$lang,$Jar)) { if (!(Test-Path $path)) { throw "Missing Stage 14B.1 foundation input: $path" } }
$source = Get-Content $items -Raw
$translations = Get-Content $lang -Raw
foreach ($id in $ids) {
    if ($source -notmatch [regex]::Escape("register(`"$id`")")) { throw "Missing unconditional item registration: $id" }
    if ($translations -notmatch [regex]::Escape("item.biomemakeover.$id")) { throw "Missing item localization: $id" }
}
if ($source -match 'MythasConfig\.(isEnabled|isMansionTrialWingEnabled|isDecayedShieldUpgradeEnabled|isDynamicLightningBugsEnabled|isCosmeticPolishEnabled)') { throw 'Item registration is conditional on Mythas configuration' }
$entries = @(jar tf $Jar)
foreach ($id in $ids) {
    $definition = "assets/biomemakeover/items/$id.json"
    $model = "assets/biomemakeover/models/item/$id.json"
    if ($entries -notcontains $definition) { throw "Missing packaged item definition: $definition" }
    if ($entries -notcontains $model) { throw "Missing packaged item model: $model" }
    $itemJson = Get-Content (Join-Path $Root "src/main/resources/assets/biomemakeover/items/$id.json") -Raw | ConvertFrom-Json
    $modelPath = $itemJson.model.model -replace '^biomemakeover:item/', 'assets/biomemakeover/models/item/'
    if ($modelPath -notmatch '\.json$') { $modelPath += '.json' }
    if ($entries -notcontains $modelPath) { throw "Item definition does not resolve: $id -> $modelPath" }
    $modelJson = Get-Content (Join-Path $Root "src/main/resources/assets/biomemakeover/models/item/$id.json") -Raw | ConvertFrom-Json
    $texture = [string]$modelJson.textures.layer0
    if (!$texture) { throw "Item model has no layer0 texture: $id" }
       if ($texture -match ':') {
           $textureNamespace, $textureName = $texture.Split(':', 2)
       } else {
           $textureNamespace = 'minecraft'
           $textureName = $texture
       }
       if ($textureNamespace -eq 'biomemakeover') {
           $texturePath = "assets/biomemakeover/textures/$textureName.png"
           if ($entries -notcontains $texturePath) { throw "Item model texture is not packaged: $id -> $texturePath" }
       } elseif ($textureNamespace -ne 'minecraft') {
           throw "Item model uses an unverified texture namespace: $id -> $texture"
       }
}
$dataFiles = Get-ChildItem (Join-Path $Root 'src/main/resources/data') -Recurse -File -ErrorAction SilentlyContinue
foreach ($file in $dataFiles) {
    $authorizedTrialReward = $file.FullName -match 'data[\\/]biomemakeover[\\/]trial_spawner[\\/]mythas[\\/]mansion[\\/]' -or $file.FullName -match 'data[\\/]biomemakeover[\\/]loot_table[\\/]spawners[\\/]mansion[\\/]' -or $file.FullName -match 'data[\\/]biomemakeover[\\/]loot_table[\\/]mansion[\\/]cache[\\/]' -or $file.FullName -match 'data[\\/]biomemakeover[\\/]recipe[\\/]mansion_emerald_key[.]json' -or $file.FullName -match 'data[\\/]biomemakeover[\\/]vault[\\/]mythas[\\/]mansion[\\/]cache[\\/]'
    if (!$authorizedTrialReward -and (Get-Content $file.FullName -Raw) -match 'patrol_trial_key|enforcer_trial_key|captain_trial_key|patrol_fragment|enforcer_fragment|captain_fragment|mansion_emerald_key|manor_vault_key') { throw "Unauthorized natural/data acquisition path: $($file.FullName)" }
}
foreach ($forbidden in @('TrialWingBlock','ManorCache','MansionEmeraldKey','TrialSpawnerConfig','VaultBlockEntity')) {
    $matches = Get-ChildItem (Join-Path $Root 'src/main/java') -Recurse -Filter '*.java' | Where-Object { $_.FullName -notmatch 'BMItems.java$' } | Select-String -SimpleMatch $forbidden
    if ($matches) { throw "Unauthorized Stage 14B gameplay implementation: $forbidden" }
}
$mansion = & powershell.exe -NoProfile -ExecutionPolicy Bypass -File (Join-Path $Root 'validation/Invoke-Stage11AMansionInventory.ps1') -Root $Root 2>&1
if ($LASTEXITCODE -ne 0 -or (($mansion -join "`n") -notmatch 'STAGE 11A MANSION INVENTORY PASSED')) { throw 'Mansion inventory validation failed' }
Write-Output 'STAGE 14B.1 MANSION ITEM FOUNDATION VALIDATION PASSED: eight unconditional IDs, packaged definitions/models/textures, localization, no acquisition path, and frozen boundaries verified.'
