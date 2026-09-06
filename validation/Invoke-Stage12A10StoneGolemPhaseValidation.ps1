[CmdletBinding()]
param([string]$Root = (Get-Location).Path)
$ErrorActionPreference = 'Stop'
$boss = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/entity/AdjudicatorEntity.java') -Raw
$golem = Get-Content (Join-Path $Root 'src/main/java/party/lemons/biomemakeover/entity/StoneGolemEntity.java') -Raw
foreach ($needle in @('STONE_GOLEM("stone_golem", true)', 'ensureStoneGolemMount()', 'BMEntities.STONE_GOLEM.create(serverLevel, EntitySpawnReason.EVENT)', 'AdjudicatorAlliance.assign(golem, this)', 'EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW)', 'startRiding(golem, true, true)', 'phase == ControllerPhase.STONE_GOLEM', 'golem.isAlive()', 'golem.discard()', 'Enchantments.PUNCH')) { if ($boss -notlike "*$needle*") { throw "Mounted Stone Golem phase anchor missing: $needle" } }
if ($boss -match 'selected == ControllerPhase.STONE_GOLEM[\s\S]{0,500}setPlayerCreated\(true\)') { throw 'Boss Stone Golem must not be player-created' }
foreach ($needle in @('CrossbowAttackMob', 'BetterCrossbowAttackGoal', 'getProjectile(ItemStack weapon)', 'FOLLOW_RANGE, 24.0D')) { if ($golem -notlike "*$needle*") { throw "Independent Stone Golem contract regressed: $needle" } }
$templates = @(Get-ChildItem (Join-Path $Root 'src/main/resources/data/biomemakeover/structure/mansion') -Recurse -Filter *.nbt)
if ($templates.Count -ne 168) { throw "Expected 168 Mansion templates, found $($templates.Count)" }
Write-Output 'STAGE 12A.10 MOUNTED STONE GOLEM VALIDATION PASSED'
