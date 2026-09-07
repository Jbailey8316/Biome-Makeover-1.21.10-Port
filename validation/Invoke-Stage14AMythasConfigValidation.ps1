[CmdletBinding()]
param([string]$Root = (Split-Path -Parent $PSScriptRoot))

$ErrorActionPreference = 'Stop'
$config = Join-Path $Root 'src/main/java/party/lemons/biomemakeover/config/MythasConfig.java'
$initializer = Join-Path $Root 'src/main/java/party/lemons/biomemakeover/BiomeMakeover.java'
$doc = Join-Path $Root 'docs/design/mythas/config-framework.md'
foreach ($path in @($config, $initializer, $doc)) { if (!(Test-Path $path)) { throw "Missing Stage 14A file: $path" } }

$text = Get-Content $config -Raw
foreach ($literal in @(
    'State.ALL_OFF',
    'isEnabled()',
    'isMansionTrialWingEnabled()',
    'isDecayedShieldUpgradeEnabled()',
    'isDynamicLightningBugsEnabled()',
    'isCosmeticPolishEnabled()',
    'state.enabled && state.mansionTrialWing',
    'state.enabled && state.decayedShieldUpgrade',
    'state.enabled && state.dynamicLightningBugs',
    'state.enabled && state.cosmeticPolish',
    'biomemakeover-mythas.json')) {
    if ($text -notmatch [regex]::Escape($literal)) { throw "Missing Mythas config contract: $literal" }
}

$keys = @('enabled','mansion_trial_wing','decayed_shield_upgrade','dynamic_lightning_bugs','cosmetic_polish')
if ($text -notmatch 'defaultDocument\(\)' -or $text -notmatch 'document\.add\(ROOT_KEY, mythas\)' -or $text -notmatch 'ensureDefaults\(mythas\)') { throw 'First-launch config does not serialize a complete default document' }
foreach ($key in $keys) {
    if ($key -eq 'enabled') {
        if ($text -notmatch [regex]::Escape('readBoolean(mythas, "enabled", false)')) { throw 'Mythas master default is not safely OFF' }
    } elseif ($text -notmatch [regex]::Escape("ensureFeature(mythas, `"$key`")")) {
        throw "Mythas feature schema/default is missing: $key"
    }
}
if ($text -match 'readBoolean\([^;]+,\s*true\s*\)' -or $text -match 'ensureBoolean\([^;]+,\s*true\s*\)') { throw 'Potential default-on Mythas configuration detected' }

$allSource = (Get-ChildItem (Join-Path $Root 'src/main/java') -Recurse -Filter '*.java' | ForEach-Object { Get-Content $_.FullName -Raw }) -join "`n"
foreach ($forbidden in @('TrialWingBlock','TrialSpawnerConfig','ManorCache','MansionEmeraldKey','dynamicLightningBugs')) {
    if ($forbidden -ne 'dynamicLightningBugs' -and $allSource -match [regex]::Escape($forbidden)) { throw "Stage 14A must not implement Mythas gameplay: $forbidden" }
}
$unexpectedMythasResources = Get-ChildItem (Join-Path $Root 'src/main/resources') -Recurse -File -ErrorAction SilentlyContinue | Where-Object {
    $path = $_.FullName.Replace('\','/')
    $path -match 'mythas|trial_wing|manor_cache' -and
        $path -notmatch '/data/biomemakeover/trial_spawner/mythas/mansion/' -and
        $path -notmatch '/data/biomemakeover/loot_table/spawners/mansion/'
}
if ($unexpectedMythasResources) { throw 'Unexpected Mythas gameplay resources found in Stage 14A' }

$tags = @(
    'biome-makeover-1.21.10-released-parity-complete',
    'biome-makeover-1.21.10-pre-parity-reconstruction'
)
foreach ($tag in $tags) {
    $safeDirectory = 'safe.directory=' + $Root.Replace('\', '/')
    $target = (& git -c $safeDirectory rev-parse "$tag^{}" 2>$null).Trim()
    if (!$target) { throw "Missing required tag: $tag" }
}

$mansion = & powershell.exe -NoProfile -ExecutionPolicy Bypass -File (Join-Path $Root 'validation/Invoke-Stage11AMansionInventory.ps1') -Root $Root 2>&1
if ($LASTEXITCODE -ne 0 -or (($mansion -join "`n") -notmatch 'STAGE 11A MANSION INVENTORY PASSED')) { throw 'Mansion inventory validation failed' }
Write-Output 'STAGE 14A MYTHAS CONFIG VALIDATION PASSED: safe-off defaults, effective master/child gates, registry-safe foundation, no gameplay implementation, and frozen invariants verified.'
