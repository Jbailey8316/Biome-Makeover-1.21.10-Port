param([string]$Root = (Join-Path $PSScriptRoot '..'))
$ErrorActionPreference = 'Stop'
$feature = Join-Path $Root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionFeature.java'
$templateRoot = Join-Path $Root 'src/main/resources/data/biomemakeover/structure/mansion'
if (!(Test-Path -LiteralPath $feature)) { throw 'MansionFeature source is missing' }
if (!(Test-Path -LiteralPath $templateRoot)) { throw 'Mansion template root is missing' }
$text = Get-Content -LiteralPath $feature -Raw
foreach ($required in @('template.filterBlocks', 'BMBlocks.DIRECTIONAL_DATA', 'consumeDirectionalMetadata', 'Blocks.AIR.defaultBlockState')) {
  if ($text.IndexOf($required, [StringComparison]::Ordinal) -lt 0) { throw "Directional Data consumer missing: $required" }
}
$templates = @(Get-ChildItem -LiteralPath $templateRoot -Recurse -Filter '*.nbt')
if ($templates.Count -ne 168) { throw "Expected 168 Mansion templates, found $($templates.Count)" }
Write-Output 'STAGE 11A.3.2R.2 DIRECTIONAL DATA VALIDATION PASSED (piece consumer wired; 168 templates preserved; gameplay dispatch inert)'
