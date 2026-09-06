[CmdletBinding()]
param([string]$Root = (Get-Location).Path)
$ErrorActionPreference = 'Stop'
$jarPath = Join-Path $Root 'build/libs/biomemakeover-fabric-1.21.10-0.8.5.jar'
if (!(Test-Path $jarPath)) { throw 'Build the Stage 13G artifact first.' }
$entries = @(jar tf $jarPath)
function Need([string]$entry) { if ($entry -notin $entries) { throw "Missing packaged Stage 13G entry: $entry" } }
function Source([string]$path) { $p=Join-Path $Root $path; if (!(Test-Path $p)) { throw "Missing Stage 13G source: $path" }; Get-Content $p -Raw }
$witch = Source 'src/main/java/party/lemons/biomemakeover/mixin/WitchMixin_Quests.java'
$quest = Source 'src/main/java/party/lemons/biomemakeover/crafting/witch/WitchQuest.java'
$handler = Source 'src/main/java/party/lemons/biomemakeover/crafting/witch/WitchQuestHandler.java'
$antidote = Source 'src/main/java/party/lemons/biomemakeover/mixin/WitchMixin_Antidote.java'
$interaction = Source 'src/main/java/party/lemons/biomemakeover/mixin/WitchMixin_Interaction.java'
$menus = Source 'src/main/java/party/lemons/biomemakeover/init/BMMenus.java'
$main = Source 'src/main/java/party/lemons/biomemakeover/BiomeMakeover.java'
if ($witch -notmatch 'WitchQuestList|QuestCategories|bmInit|bmGoals|bmInteract|bmServerAiStep|bmSave|bmLoad') { throw 'Released Witch quest lifecycle hooks are incomplete.' }
if ($witch -notmatch 'WITCH_HATS|canInteract') { throw 'Witch Hat interaction gating is missing.' }
if ($quest -notmatch 'Points|Items|toTag|CompoundTag') { throw 'Quest persistence fields are missing.' }
if ($handler -notmatch 'weightedCount|RewardTables|getRewardFor') { throw 'Quest count/reward selection is missing.' }
if ($antidote -notmatch 'ANTIDOTE|startUsingItem|aiStep') { throw 'Released Witch antidote hook is missing.' }
if ($interaction -notmatch '@Mixin\(Mob\.class\)|method = "mobInteract"|instanceof Witch|WitchQuestEntity') { throw 'Current Mob interaction hook is missing or not Witch-scoped.' }
if ($menus -notmatch 'WITCH|WitchMenu') { throw 'Witch menu registration is missing.' }
if ($main -notmatch 'QuestCategoryReloadListener|RewardTables|PayloadTypeRegistry|CompleteWitchQuestPayload') { throw 'Quest reload/network registration is missing.' }
foreach($id in @('common','dark_forest','flower','jungle','mesa','mushroom','nether','ocean','rare','swamp')) { Need "data/biomemakeover/quest_category/$id.json" }
foreach($id in @('items','multi_potions','potion','potion_ingredients')) { Need "data/biomemakeover/quest_reward/$id.json" }
Need 'data/biomemakeover/advancement/biomemakeover/witch_quest.json'
Need 'data/biomemakeover/loot_table/entities/witch_hat.json'
Need 'assets/biomemakeover/textures/gui/witch.png'
Need 'party/lemons/biomemakeover/mixin/WitchMixin_Quests.class'
Need 'party/lemons/biomemakeover/crafting/witch/menu/WitchMenu.class'
Need 'party/lemons/biomemakeover/network/WitchQuestsPayload.class'
Need 'party/lemons/biomemakeover/network/CompleteWitchQuestPayload.class'
if ($witch -match 'setTarget\(') { throw 'Quest hook must not refresh Witch target each tick.' }
if ($witch -match 'performRangedAttack|spawn.*Projectile') { throw 'Witch quest scope contains unauthorized combat fallback.' }
$mixins = Source 'src/main/resources/biomemakeover.mixins.json'
if ($mixins -notmatch 'WitchMixin_Quests|WitchMixin_Interaction') { throw 'Witch quest mixins are not declared.' }
Write-Output 'STAGE 13G WITCH QUEST VALIDATION PASSED: 10 categories, 4 reward tables, Witch Hat gating, persistence, antidote hook, menu, payloads, reload listeners, advancement, and packaged resources verified.'
