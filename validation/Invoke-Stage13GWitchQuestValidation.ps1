[CmdletBinding()]
param([string]$Root = (Get-Location).Path)
$ErrorActionPreference = 'Stop'
$jarPath = Join-Path $Root 'build/libs/biomemakeover-fabric-1.21.10-0.8.5.jar'
if (!(Test-Path $jarPath)) { throw 'Build the Stage 13G artifact first.' }
$entries = @(jar tf $jarPath)
function Need([string]$entry) { if ($entry -notin $entries) { throw "Missing packaged Stage 13G entry: $entry" } }
function Source([string]$path) { $p=Join-Path $Root $path; if (!(Test-Path $p)) { throw "Missing Stage 13G source: $path" }; Get-Content $p -Raw }
function JarText([string]$entry) {
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $archive = [System.IO.Compression.ZipFile]::OpenRead($jarPath)
    try {
        $item = $archive.GetEntry($entry)
        if ($null -eq $item) { throw "Missing packaged Stage 13G entry: $entry" }
        $reader = [System.IO.StreamReader]::new($item.Open())
        try { return $reader.ReadToEnd() } finally { $reader.Dispose() }
    } finally { $archive.Dispose() }
}
$witch = Source 'src/main/java/party/lemons/biomemakeover/mixin/WitchMixin_Quests.java'
if ($witch -notmatch 'bmQuests\.populate\(getRandom\(\)\)') { throw 'Released Witch constructor-time quest population is missing.' }
$quest = Source 'src/main/java/party/lemons/biomemakeover/crafting/witch/WitchQuest.java'
$handler = Source 'src/main/java/party/lemons/biomemakeover/crafting/witch/WitchQuestHandler.java'
$antidote = Source 'src/main/java/party/lemons/biomemakeover/mixin/WitchMixin_Antidote.java'
$interaction = Source 'src/main/java/party/lemons/biomemakeover/mixin/WitchMixin_Interaction.java'
$mobHooks = Source 'src/main/java/party/lemons/biomemakeover/mixin/WitchMixin_MobHooks.java'
$livingHooks = Source 'src/main/java/party/lemons/biomemakeover/mixin/WitchMixin_LivingHooks.java'
$menus = Source 'src/main/java/party/lemons/biomemakeover/init/BMMenus.java'
$main = Source 'src/main/java/party/lemons/biomemakeover/BiomeMakeover.java'
if ($witch -notmatch 'WitchQuestList|QuestCategories|bmInit|bmGoals|bmInteract|bmServerAiStep|bmSave|bmLoad') { throw 'Released Witch quest lifecycle hooks are incomplete.' }
if ($witch -notmatch 'WITCH_HATS|canInteract') { throw 'Witch Hat interaction gating is missing.' }
if ($quest -notmatch 'Points|Items|toTag|CompoundTag') { throw 'Quest persistence fields are missing.' }
if ($handler -notmatch 'weightedCount|RewardTables|getRewardFor') { throw 'Quest count/reward selection is missing.' }
if ($antidote -notmatch '@Mixin\(LivingEntity\.class\)|method = "aiStep"|WitchQuestEntity') { throw 'Current LivingEntity antidote hook is missing or not Witch-scoped.' }
if ($witch -notmatch 'offerAntidote|ANTIDOTE|setUsingItem') { throw 'Released Witch antidote behavior is missing.' }
if ($interaction -notmatch '@Mixin\(Mob\.class\)|method = "mobInteract"|instanceof Witch|WitchQuestEntity') { throw 'Current Mob interaction hook is missing or not Witch-scoped.' }
if ($mobHooks -notmatch 'registerGoals|customServerAiStep|addAdditionalSaveData|readAdditionalSaveData|WitchQuestEntity') { throw 'Mob-declared Witch quest hooks are incomplete.' }
if ($livingHooks -notmatch 'method = "die"|dropFromLootTable|instanceof Witch|WitchQuestEntity') { throw 'LivingEntity-declared Witch quest hooks are incomplete.' }
if ($witch -match 'method = "registerGoals"|method = "customServerAiStep"|method = "addAdditionalSaveData"|method = "readAdditionalSaveData"|method = "die"|method = "dropFromLootTable"') { throw 'Superclass-declared Witch hooks remain incorrectly targeted from WitchMixin_Quests.' }
if ($menus -notmatch 'WITCH|WitchMenu') { throw 'Witch menu registration is missing.' }
if ($main -notmatch 'QuestCategoryReloadListener|RewardTables|PayloadTypeRegistry|CompleteWitchQuestPayload') { throw 'Quest reload/network registration is missing.' }
foreach($id in @('common','dark_forest','flower','jungle','mesa','mushroom','nether','ocean','rare','swamp')) { Need "data/biomemakeover/quest_category/$id.json" }
foreach($id in @('items','multi_potions','potion','potion_ingredients')) { Need "data/biomemakeover/quest_reward/$id.json" }
Need 'data/biomemakeover/advancement/biomemakeover/witch_quest.json'
Need 'data/biomemakeover/loot_table/entities/witch_hat.json'
Need 'assets/biomemakeover/textures/gui/witch.png'
Need 'party/lemons/biomemakeover/mixin/WitchMixin_Quests.class'
Need 'party/lemons/biomemakeover/crafting/witch/menu/WitchMenu.class'
Need 'party/lemons/biomemakeover/client/screen/WitchScreen.class'
Need 'party/lemons/biomemakeover/network/WitchQuestsPayload.class'
Need 'party/lemons/biomemakeover/network/CompleteWitchQuestPayload.class'
Need 'party/lemons/biomemakeover/mixin/WitchMixin_MobHooks.class'
Need 'party/lemons/biomemakeover/mixin/WitchMixin_LivingHooks.class'
if ($witch -match 'setTarget\(') { throw 'Quest hook must not refresh Witch target each tick.' }
if ($witch -match 'performRangedAttack|spawn.*Projectile') { throw 'Witch quest scope contains unauthorized combat fallback.' }
$loot = Get-Content (Join-Path $Root 'src/main/resources/data/biomemakeover/loot_table/entities/witch_hat.json') -Raw
if ($loot -match 'random_chance_with_looting' -or $loot -notmatch 'random_chance_with_enchanted_bonus|unenchanted_chance|per_level_above_first') { throw 'Witch Hat loot table does not use the current Looting-compatible condition.' }
if ($loot -notmatch 'unenchanted_chance[^0-9]*0\.05|base[^0-9]*0\.10|per_level_above_first[^0-9]*0\.05') { throw 'Witch Hat 5% base plus 5% per Looting level semantics are not preserved.' }
$screen = Source 'src/client/java/party/lemons/biomemakeover/client/screen/WitchScreen.java'
if ($screen -notmatch 'updateQuests|SCREEN_INIT|SCREEN_RENDER|QuestButton|renderLabels|renderBg') { throw 'Released Witch quest screen rendering/data-refresh path is incomplete.' }
$client = Source 'src/client/java/party/lemons/biomemakeover/client/BiomeMakeoverClient.java'
if ($client -notmatch 'WitchQuestsPayload\.TYPE|setQuests\(new WitchQuestList|updateQuests\(\)') { throw 'Client Witch quest payload refresh path is missing.' }
$packagedLoot = JarText 'data/biomemakeover/loot_table/entities/witch_hat.json'
try { $null = $packagedLoot | ConvertFrom-Json } catch { throw 'Packaged Witch Hat loot table is not valid JSON.' }
if ($packagedLoot -match 'random_chance_with_looting' -or $packagedLoot -notmatch 'random_chance_with_enchanted_bonus|unenchanted_chance|per_level_above_first') { throw 'Packaged Witch Hat loot table does not use the current Looting-compatible condition.' }
if ($packagedLoot -notmatch 'unenchanted_chance[^0-9]*0\.05|base[^0-9]*0\.10|per_level_above_first[^0-9]*0\.05') { throw 'Packaged Witch Hat loot table does not preserve 5% base plus 5% per Looting level semantics.' }
$mixins = Source 'src/main/resources/biomemakeover.mixins.json'
if ($mixins -notmatch 'WitchMixin_Quests|WitchMixin_Interaction') { throw 'Witch quest mixins are not declared.' }
Write-Output 'STAGE 13G WITCH QUEST VALIDATION PASSED: 10 categories, 4 reward tables, Witch Hat gating, persistence, antidote hook, menu, payloads, reload listeners, advancement, and packaged resources verified.'
