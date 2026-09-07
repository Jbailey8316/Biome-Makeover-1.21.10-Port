[CmdletBinding()]
param(
    [string]$Root = (Split-Path -Parent $PSScriptRoot),
    [string]$Jar = (Join-Path (Split-Path -Parent $PSScriptRoot) 'build/libs/biomemakeover-fabric-1.21.10-0.8.5.jar')
)

$ErrorActionPreference = 'Stop'
$configRoot = Join-Path $Root 'src/main/resources/data/biomemakeover/trial_spawner/mythas/mansion'
$lootRoot = Join-Path $Root 'src/main/resources/data/biomemakeover/loot_table/spawners/mansion'
$configs = @{
    patrol = @{ mobs = @(@('minecraft:pillager',4), @('minecraft:vindicator',1)); total = 6.0; totalPerPlayer = 2.0; simultaneous = 2.0; simultaneousPerPlayer = 1.0; key = 'patrol_trial_key' }
    enforcer = @{ mobs = @(@('minecraft:vindicator',5), @('minecraft:pillager',2)); total = 8.0; totalPerPlayer = 2.0; simultaneous = 3.0; simultaneousPerPlayer = 0.5; key = 'enforcer_trial_key' }
    captain = @{ mobs = @(@('minecraft:evoker',2), @('minecraft:vindicator',5), @('minecraft:pillager',3)); total = 10.0; totalPerPlayer = 1.5; simultaneous = 3.0; simultaneousPerPlayer = 0.5; key = 'captain_trial_key' }
}

foreach ($path in @($configRoot, $lootRoot, $Jar)) { if (!(Test-Path $path)) { throw "Missing Stage 14B.2 input: $path" } }
$configFiles = @(Get-ChildItem $configRoot -Filter '*.json' -File)
if ($configFiles.Count -ne 3) { throw "Expected exactly three Mythas Trial Spawner configs; found $($configFiles.Count)" }
$jarEntries = @(jar tf $Jar)
foreach ($name in $configs.Keys) {
    $configPath = Join-Path $configRoot "$name.json"
    $lootPath = Join-Path $lootRoot "${name}_key.json"
    if (!(Test-Path $configPath) -or !(Test-Path $lootPath)) { throw "Missing $name config or reward table" }
    $config = Get-Content $configPath -Raw | ConvertFrom-Json
    foreach ($field in @('spawn_range','total_mobs','simultaneous_mobs','total_mobs_added_per_player','simultaneous_mobs_added_per_player','ticks_between_spawn','spawn_potentials','loot_tables_to_eject')) {
        if ($null -eq $config.$field) { throw "Missing current 1.21.10 Trial Spawner field '$field' in $name" }
    }
    $expected = $configs[$name]
    if ($config.spawn_range -ne 4 -or $config.total_mobs -ne $expected.total -or $config.total_mobs_added_per_player -ne $expected.totalPerPlayer -or $config.simultaneous_mobs -ne $expected.simultaneous -or $config.simultaneous_mobs_added_per_player -ne $expected.simultaneousPerPlayer -or $config.ticks_between_spawn -ne 20) { throw "Unexpected Trial Spawner scaling/timing in $name" }
    if (@($config.spawn_potentials).Count -ne $expected.mobs.Count) { throw "Unexpected spawn-potential count in $name" }
    foreach ($mob in $expected.mobs) {
        $matches = @($config.spawn_potentials | Where-Object { $_.data.entity.id -eq $mob[0] -and $_.weight -eq $mob[1] })
        if ($matches.Count -ne 1) { throw "Missing or incorrect $($mob[0]) weight in $name" }
    }
    if ($name -eq 'captain' -and @($config.spawn_potentials | Where-Object { $_.data.entity.id -eq 'minecraft:ravager' }).Count -ne 0) { throw 'Captain Ravager use is not hard-bounded' }
    if (@($config.loot_tables_to_eject).Count -ne 1 -or $config.loot_tables_to_eject[0].weight -ne 1 -or $config.loot_tables_to_eject[0].data -ne "biomemakeover:spawners/mansion/${name}_key") { throw "Incorrect guaranteed reward mapping in $name" }
    $configJarPath = "data/biomemakeover/trial_spawner/mythas/mansion/$name.json"
    $lootJarPath = "data/biomemakeover/loot_table/spawners/mansion/${name}_key.json"
    if ($jarEntries -notcontains $configJarPath -or $jarEntries -notcontains $lootJarPath) { throw "Packaged config/reward missing for $name" }
    $loot = Get-Content $lootPath -Raw | ConvertFrom-Json
    if (@($loot.pools).Count -ne 1 -or @($loot.pools[0].entries).Count -ne 1 -or $loot.pools[0].entries[0].name -ne "biomemakeover:$($expected.key)") { throw "Reward table does not guarantee the matching key in $name" }
}
$changedSource = @(git -c (('safe.directory=' + $Root.Replace('\','/'))) diff --name-only -- 'src/main/java/**')
if ($changedSource.Count -ne 0) { throw "Stage 14B.2 must not change Java gameplay code: $($changedSource -join ', ')" }
$mansionFiles = @(git -c (('safe.directory=' + $Root.Replace('\','/'))) diff --name-only aed9454605739fd52b52c06aac72d31b3f519502 -- 'src/main/resources/data/biomemakeover/structure/mansion/**')
if ($mansionFiles.Count -ne 0) { throw 'Mansion structure resources changed during Stage 14B.2' }
$itemsSource = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/init/BMItems.java') -Raw
foreach ($itemId in @('patrol_trial_key','enforcer_trial_key','captain_trial_key','patrol_fragment','enforcer_fragment','captain_fragment','mansion_emerald_key','manor_vault_key')) {
    if ($itemsSource -notmatch [regex]::Escape(('register("' + $itemId + '")'))) { throw "Stage 14B.1 item foundation missing: $itemId" }
}
if ($itemsSource -match 'MythasConfig\.(isEnabled|isMansionTrialWingEnabled)') { throw 'Stage 14B.1 item registration became conditional' }
$mansion = & powershell.exe -NoProfile -ExecutionPolicy Bypass -File (Join-Path $Root 'validation/Invoke-Stage11AMansionInventory.ps1') -Root $Root 2>&1
if ($LASTEXITCODE -ne 0 -or (($mansion -join "`n") -notmatch 'templates=168 active_unique=165 orphan=3')) { throw 'Mansion inventory regression' }
Write-Output 'STAGE 14B.2 MANSION TRIAL SPAWNER VALIDATION PASSED: three current-schema configs, approved rosters, bounded Ravager policy, guaranteed matching keys, packaged data, Stage 14B.1 foundation, and frozen Mansion boundaries verified.'
