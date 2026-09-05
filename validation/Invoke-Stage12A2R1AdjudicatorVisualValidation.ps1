param([string]$Root = (Join-Path $PSScriptRoot '..'))
$ErrorActionPreference = 'Stop'
$model = Get-Content (Join-Path $Root 'src/client/java/party/lemons/biomemakeover/client/model/AdjudicatorModel.java') -Raw
$released = Get-Content (Join-Path $Root 'reference/Biome-Makeover-1.20/common/src/main/java/party/lemons/biomemakeover/entity/render/AdjudicatorModel.java') -Raw
$lang = Get-Content (Join-Path $Root 'src/main/resources/assets/biomemakeover/lang/en_us.json') -Raw | ConvertFrom-Json
foreach ($needle in @('texOffs(0, 0)', 'addBox(-4, -12, -4, 8, 12, 8)', 'PartPose.offset(0, 11.25F, 0)', 'LayerDefinition.create(mesh, 64, 64)', 'setupAnim(AdjudicatorRenderState state)', 'state.xRot * Mth.DEG_TO_RAD')) {
    if (-not $model.Contains($needle)) { throw "Adjudicator model contract missing: $needle" }
}
foreach ($needle in @('texOffs(0, 0)', 'addBox(-4.0F, -12.0F, -4.0F, 8.0F, 12.0F, 8.0F', 'LayerDefinition.create(meshdefinition, 64, 64)')) {
    if (-not $released.Contains($needle)) { throw "Released model reference missing: $needle" }
}
if ($lang.'entity.biomemakeover.adjudicator' -ne 'Adjudicator') { throw 'Released Adjudicator translation missing or incorrect' }
Write-Output 'STAGE 12A.2-R1 ADJUDICATOR MODEL/NAME VALIDATION PASSED'
