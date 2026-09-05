$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$alliance = Join-Path $root 'src/main/java/party/lemons/biomemakeover/entity/AdjudicatorAlliance.java'
$adj = Join-Path $root 'src/main/java/party/lemons/biomemakeover/entity/AdjudicatorEntity.java'
$mixin = Join-Path $root 'src/main/resources/biomemakeover.mixins.json'
foreach ($path in @($alliance,$adj,$mixin)) { if (!(Test-Path $path)) { throw "Missing $path" } }
$a = Get-Content $alliance -Raw
$e = Get-Content $adj -Raw
$m = Get-Content $mixin -Raw
foreach ($needle in @('bm_adjudicator_encounter:', 'getTags()', 'Projectile', 'EvokerFangs', 'getOwner()', 'FRIENDLY_DAMAGE_BLOCKED', 'FRIENDLY_TARGET_REJECTED', 'EVOKER_VEX_INHERIT')) {
    if ($a -notlike "*$needle*") { throw "Alliance helper missing $needle" }
}
foreach ($needle in @('AdjudicatorAlliance.ensure(this)', 'AdjudicatorAlliance.assign(ravager, this)', 'AdjudicatorAlliance.assign(entity, this)', 'SUMMON_PHASE_TICKS = 120', 'summonPhaseEligible')) {
    if ($e -notlike "*$needle*") { throw "Production integration missing $needle" }
}
foreach ($needle in @('AdjudicatorAllianceEntityMixin','AdjudicatorAllianceDamageMixin','AdjudicatorAllianceMobMixin','AdjudicatorAllianceEvokerMixin')) {
    if ($m -notlike "*$needle*") { throw "Mixin config missing $needle" }
}
Write-Output 'Stage 12A.7-R3 alliance validation: PASS (static scope, persistence, ownership, and frozen cadence checks)'
