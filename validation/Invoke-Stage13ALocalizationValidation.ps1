[CmdletBinding()]
param([string]$Root = (Get-Location).Path)

$ErrorActionPreference = 'Stop'
$langPath = Join-Path $Root 'src/main/resources/assets/biomemakeover/lang/en_us.json'
$lang = Get-Content $langPath -Raw | ConvertFrom-Json
$keys = @($lang.psobject.Properties.Name)

function Get-Matches([string]$Text, [string]$Pattern) {
    return @([regex]::Matches($Text, $Pattern) | ForEach-Object { $_.Groups[1].Value })
}
function Assert-Keys([string[]]$Expected, [string]$Kind) {
    $missing = @($Expected | Sort-Object -Unique | Where-Object { $_ -notin $keys })
    if ($missing.Count -gt 0) {
        throw "$Kind missing English translations: $($missing -join ', ')"
    }
}

$itemsSource = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/init/BMItems.java') -Raw
$blocksSource = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/init/BMBlocks.java') -Raw
$entitiesSource = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/init/BMEntities.java') -Raw

$itemIds = Get-Matches $itemsSource 'register\("([a-z0-9_]+)"'
$itemIds += Get-Matches $entitiesSource 'registerSpawnEgg\("([a-z0-9_]+)"'
$blockDescriptionItems = @('ancient_oak_sign', 'ancient_oak_hanging_sign', 'swamp_cypress_sign', 'swamp_cypress_hanging_sign', 'willow_sign', 'willow_hanging_sign')
$itemIds = @($itemIds | Where-Object { $_ -notin $blockDescriptionItems })
$blockIds = Get-Matches $blocksSource 'register\("([a-z0-9_]+)"'
$blockIds = @($blockIds | Where-Object { $_ -notmatch '_$' })
$entityIds = Get-Matches $entitiesSource 'registerEntity\(\s*"([a-z0-9_]+)"'

Assert-Keys ($itemIds | ForEach-Object { "item.biomemakeover.$_" }) 'Registered items'
Assert-Keys ($blockIds | ForEach-Object { "block.biomemakeover.$_" }) 'Registered blocks'
Assert-Keys ($entityIds | ForEach-Object { "entity.biomemakeover.$_" }) 'Registered entities'

$advancementKeys = @()
Get-ChildItem (Join-Path $Root 'src/main/resources/data/biomemakeover/advancement') -Recurse -Filter *.json | ForEach-Object {
    $advancementKeys += Get-Matches (Get-Content $_.FullName -Raw) '"translate"\s*:\s*"([^"]+)"'
}
Assert-Keys $advancementKeys 'Advancement translations'

$tooltipKeys = @()
Get-ChildItem @((Join-Path $Root 'src/main/java'), (Join-Path $Root 'src/client/java')) -Recurse -Filter *.java -ErrorAction SilentlyContinue | ForEach-Object {
    $tooltipKeys += Get-Matches (Get-Content $_.FullName -Raw) 'Component\.translatable(?:WithFallback)?\(\s*"([^"]+)"'
}
$tooltipKeys = @($tooltipKeys | Where-Object { $_ -like 'biomemakeover.*' -or $_ -like 'item.biomemakeover.*' -or $_ -like 'block.biomemakeover.*' })
Assert-Keys $tooltipKeys 'Custom component translations'

if ($lang.'item.biomemakeover.enchanted_totem' -ne 'Enchanted Totem') {
    throw 'Enchanted Totem must resolve to exactly "Enchanted Totem"'
}

$exceptionList = @(
    'Vanilla-derived item/block/entity names are intentionally supplied by Minecraft and are not BM keys.',
    'Sign and hanging-sign BlockItems use Minecraft 1.21.10 block-description prefixes and intentionally inherit the corresponding block translation.',
    'No-item implementation blocks and dynamically generated family members are validated through their reachable item/block family resources rather than synthetic helper-prefix matches.'
    'Internal registry keys, tags, loot tables, predicates, and recipe IDs are not display names unless referenced by a user-facing Component.'
)
Write-Output ('STAGE 13A LOCALIZATION VALIDATION PASSED: items={0} blocks={1} entities={2} advancementKeys={3} tooltipKeys={4} langKeys={5}' -f @($itemIds | Sort-Object -Unique).Count, @($blockIds | Sort-Object -Unique).Count, @($entityIds | Sort-Object -Unique).Count, @($advancementKeys | Sort-Object -Unique).Count, @($tooltipKeys | Sort-Object -Unique).Count, $keys.Count)
Write-Output ('Explicit exceptions: ' + ($exceptionList -join ' | '))
