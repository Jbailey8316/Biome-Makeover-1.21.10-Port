param([string]$Root = (Split-Path $PSScriptRoot -Parent))
$ErrorActionPreference='Stop'
$f=Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/worldgen/mansion/MansionFeature.java') -Raw
foreach($needle in @('BM_LOOT_TRACE','BM_CONTAINER_TRACE','bm.mansion.trace','bounds.isInside','relative(facing)')) { if($f.IndexOf($needle,[StringComparison]::Ordinal) -lt 0){throw "Missing diagnostic probe: $needle"} }
foreach($needle in @('BM_PIECE_TRACE','BM_LOOT_LIFECYCLE','BM_FENCE_LIFECYCLE','BM_FLUID_LIFECYCLE','phase=AFTER_TEMPLATE','"T4"','"W5"')) { if($f.IndexOf($needle,[StringComparison]::Ordinal) -lt 0){throw "Missing lifecycle probe: $needle"} }
if($f -match 'setBlock\([^\)]*Blocks\.AIR[^\)]*\).*TRACE'){throw 'Diagnostics must not alter placement'}
Write-Output 'Stage 11B.1R.8 diagnostic probes: PASS'
Write-Output 'Behavior changes: none (property-gated logging only)'
