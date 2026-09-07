[CmdletBinding()]
param(
    [string]$Root = (Split-Path -Parent $PSScriptRoot),
    [string]$Jar = (Join-Path (Split-Path -Parent $PSScriptRoot) 'build/libs/biomemakeover-fabric-1.21.10-0.8.5.jar')
)

$ErrorActionPreference = 'Stop'
$cacheRoot = Join-Path $Root 'src/main/resources/data/biomemakeover/vault/mythas/mansion/cache'
$lootRoot = Join-Path $Root 'src/main/resources/data/biomemakeover/loot_table/mansion/cache'
$recipePath = Join-Path $Root 'src/main/resources/data/biomemakeover/recipe/mansion_emerald_key.json'
$expected = @{
    patrol = @{ key = 'patrol_trial_key'; fragment = 'patrol_fragment' }
    enforcer = @{ key = 'enforcer_trial_key'; fragment = 'enforcer_fragment' }
    captain = @{ key = 'captain_trial_key'; fragment = 'captain_fragment' }
}
foreach ($path in @($cacheRoot,$lootRoot,$recipePath,$Jar)) { if (!(Test-Path $path)) { throw "Missing Stage 14B.3 input: $path" } }
$cacheFiles = @(Get-ChildItem $cacheRoot -Filter '*.json' -File)
if ($cacheFiles.Count -ne 3) { throw "Expected exactly three Manor Cache contracts; found $($cacheFiles.Count)" }
$entries = @(jar tf $Jar)
foreach ($name in $expected.Keys) {
    $cachePath = Join-Path $cacheRoot "$name.json"
    $lootPath = Join-Path $lootRoot "$name.json"
    if (!(Test-Path $lootPath)) { throw "Missing $name cache reward table" }
    $cache = Get-Content $cachePath -Raw | ConvertFrom-Json
    foreach ($field in @('loot_table','activation_range','deactivation_range','key_item')) { if ($null -eq $cache.$field) { throw "Missing VaultConfig field '$field' in $name" } }
    $e = $expected[$name]
    if ($cache.loot_table -ne "biomemakeover:mansion/cache/$name" -or $cache.activation_range -ne 4.0 -or $cache.deactivation_range -ne 4.5 -or $cache.key_item.id -ne "biomemakeover:$($e.key)" -or $cache.key_item.count -ne 1) { throw "Incorrect native VaultConfig contract in $name" }
    $loot = Get-Content $lootPath -Raw | ConvertFrom-Json
    if (@($loot.pools).Count -ne 1 -or $loot.pools[0].rolls -ne 1.0 -or @($loot.pools[0].entries).Count -ne 1 -or $loot.pools[0].entries[0].name -ne "biomemakeover:$($e.fragment)") { throw "Cache reward does not guarantee the matching Fragment in $name" }
    if ($loot.pools[0].entries[0].name -match 'trial_key|mansion_emerald_key|manor_vault_key') { throw "Unauthorized progression reward in $name" }
    foreach ($jarPath in @("data/biomemakeover/vault/mythas/mansion/cache/$name.json","data/biomemakeover/loot_table/mansion/cache/$name.json")) { if ($entries -notcontains $jarPath) { throw "Missing packaged Stage 14B.3 resource: $jarPath" } }
}
$recipe = Get-Content $recipePath -Raw | ConvertFrom-Json
if ($recipe.type -ne 'minecraft:crafting_shapeless' -or @($recipe.ingredients).Count -ne 3 -or $recipe.result.id -ne 'biomemakeover:mansion_emerald_key' -or $recipe.result.count -ne 1) { throw 'Emerald Key recipe shape/output is incorrect' }
foreach ($fragment in @('patrol_fragment','enforcer_fragment','captain_fragment')) { if (@($recipe.ingredients | Where-Object { $_ -eq "biomemakeover:$fragment" }).Count -ne 1) { throw "Emerald Key recipe must contain exactly one $fragment" } }
$recipeText = Get-Content $recipePath -Raw
if ($recipeText -match 'trial_key|manor_vault_key') { throw 'Emerald Key recipe contains an unauthorized progression item' }
$java = @(git -c (('safe.directory=' + $Root.Replace('\','/'))) diff --name-only -- 'src/main/java/**')
if ($java.Count -ne 0) { throw "Stage 14B.3 must not change Java gameplay code: $($java -join ', ')" }
$mansionFiles = @(git -c (('safe.directory=' + $Root.Replace('\','/'))) diff --name-only -- 'src/main/resources/data/biomemakeover/structure/mansion/**')
if ($mansionFiles.Count -ne 0) { throw 'Mansion structure resources changed during Stage 14B.3' }
$b2 = & powershell.exe -NoProfile -ExecutionPolicy Bypass -File (Join-Path $Root 'validation/Invoke-Stage14B2MansionTrialSpawnerValidation.ps1') -Root $Root -Jar $Jar 2>&1
if ($LASTEXITCODE -ne 0 -or (($b2 -join "`n") -notmatch 'STAGE 14B.2 MANSION TRIAL SPAWNER VALIDATION PASSED')) { throw 'Stage 14B.2 regression' }
$b1 = & powershell.exe -NoProfile -ExecutionPolicy Bypass -File (Join-Path $Root 'validation/Invoke-Stage14B1MansionItemFoundationValidation.ps1') -Root $Root -Jar $Jar 2>&1
if ($LASTEXITCODE -ne 0 -or (($b1 -join "`n") -notmatch 'STAGE 14B.1 MANSION ITEM FOUNDATION VALIDATION PASSED')) { throw 'Stage 14B.1 regression' }
$mansion = & powershell.exe -NoProfile -ExecutionPolicy Bypass -File (Join-Path $Root 'validation/Invoke-Stage11AMansionInventory.ps1') -Root $Root 2>&1
if ($LASTEXITCODE -ne 0 -or (($mansion -join "`n") -notmatch 'templates=168 active_unique=165 orphan=3')) { throw 'Mansion inventory regression' }
Write-Output 'STAGE 14B.3 MANOR CACHE / FRAGMENT VALIDATION PASSED: three native VaultConfig contracts, matching guaranteed Fragment rewards, exact Emerald Key recipe, packaged resources, and frozen boundaries verified.'
