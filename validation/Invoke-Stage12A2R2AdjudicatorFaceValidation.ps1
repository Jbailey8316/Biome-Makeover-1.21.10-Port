param([string]$Root = (Join-Path $PSScriptRoot '..'))
$ErrorActionPreference = 'Stop'
$model = Get-Content (Join-Path $Root 'src/client/java/party/lemons/biomemakeover/client/model/AdjudicatorModel.java') -Raw
$renderer = Get-Content (Join-Path $Root 'src/client/java/party/lemons/biomemakeover/client/render/AdjudicatorRenderer.java') -Raw
$releasedRenderer = Get-Content (Join-Path $Root 'reference/Biome-Makeover-1.20/common/src/main/java/party/lemons/biomemakeover/entity/render/AdjudicatorRender.java') -Raw
$currentTexture = Join-Path $Root 'src/main/resources/assets/biomemakeover/textures/entity/adjudicator.png'
$releasedTexture = Join-Path $Root 'reference/Biome-Makeover-1.20/common/src/main/resources/assets/biomemakeover/textures/entity/adjudicator.png'
$currentEyes = Join-Path $Root 'src/main/resources/assets/biomemakeover/textures/entity/adjudicator_eyes.png'
$releasedEyes = Join-Path $Root 'reference/Biome-Makeover-1.20/common/src/main/resources/assets/biomemakeover/textures/entity/adjudicator_eyes.png'
foreach ($needle in @('addBox(-4, -12, -4, 8, 12, 8)', 'addBox(-1, -.5F, -2, 2, 5, 2)', 'PartPose.offset(0, 11.25F, 0)', 'LayerDefinition.create(mesh, 64, 64)', 'setupAnim(AdjudicatorRenderState state)', 'state.xRot * Mth.DEG_TO_RAD')) {
    if (-not $model.Contains($needle)) { throw "Adjudicator face/model contract missing: $needle" }
}
foreach ($needle in @('EyesLayer<AdjudicatorRenderState, AdjudicatorModel>', 'RenderType.eyes(EYES_TEXTURE)', 'adjudicator_eyes.png')) {
    if (-not $renderer.Contains($needle)) { throw "Adjudicator eye-layer contract missing: $needle" }
}
if (-not $releasedRenderer.Contains('AdjudicatorEyesRenderLayer')) { throw 'Released eye-layer reference missing' }
foreach ($path in @($currentTexture, $releasedTexture, $currentEyes, $releasedEyes)) {
    if (-not (Test-Path -LiteralPath $path)) { throw "Missing texture: $path" }
}
if ((Get-FileHash $currentTexture -Algorithm SHA256).Hash -ne (Get-FileHash $releasedTexture -Algorithm SHA256).Hash) { throw 'Base Adjudicator texture differs from released asset' }
if ((Get-FileHash $currentEyes -Algorithm SHA256).Hash -eq (Get-FileHash $releasedEyes -Algorithm SHA256).Hash) { throw 'Adjudicator eye texture has no R2A pupil enhancement' }
Write-Output 'STAGE 12A.2-R2 ADJUDICATOR FACE VALIDATION PASSED (released mesh/UV and eye layer restored)'
