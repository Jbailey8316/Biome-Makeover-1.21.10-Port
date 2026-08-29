# Showcase-Informed Mushroom Fields and Badlands Audit

## 1. Scope and evidence rule

This is an audit-only checkpoint. It changes no production source, resource, registry, gameplay, rendering, or
world-generation behavior. Laser Knight showcase material is evidence of historical design, but strict parity is
decided by the final released source: `Lemonszz/Biome-Makeover`, branch `1.20`, commit
`2f314c0596af095a4890995a465f308f69476b4a` (Minecraft 1.20.1, BM 1.20.1-1.11.4). Current-port evidence is the
repository at starting commit `60b90151b79b0c07b6eae3111c39a6aea8e49f69`.

The classifications used below are exactly: **PASS**, **PARITY MISSING**, **PARITY PARTIAL**,
**HISTORICAL/DISABLED**, **MYTHAS CANDIDATE**, **SHOWCASE-ONLY/UNCONFIRMED**, and
**DEFERRED BY EXISTING STAGE OWNERSHIP**. A final-source registration without a released acquisition or execution
path is not promoted to parity merely because a showcase demonstrated an earlier version.

## 2. Executive findings

- The final Mushroom Fields release does have an extensive underground decoration chain. It does **not** register a
  carver, density/noise function, terrain surface rule, or cave-size modifier. Its apparent cave transformation is
  vanilla cave space dressed with an underground mycelium vegetation patch and huge glowshrooms from Y -30 through
  60. The current port already contains those configured/placed features and injections. The earlier concern that the
  underground chain was omitted is invalidated; targeted distribution testing remains open.
- The showcase mushroom bat is `biomemakeover:blightbat`. Its class, renderer, texture and hidden items remain in the
  final tree, but both natural-spawn calls are commented and its spawn egg/wing are hidden. It is
  **HISTORICAL/DISABLED**, not released parity.
- Mushroom food gameplay is simpler than the showcase wording suggests: raw and cooked Glowfish each independently
  roll Night Vision and Glowing at 50% for 200 ticks on every consumption. There is no accumulated-consumption state.
  Glowshroom Stew guarantees both for 1200 ticks. These systems are present in the current port.
- Final-release Ghost Towns are not showcase-only. They are reachable jigsaw structures with structure-set placement,
  Ghost spawn overrides, processors, templates, loot and archaeology. The entire chain remains absent from the current
  port under the already-approved Stage 10C ownership boundary.
- Ghost -> Ectoplasm -> Ectoplasm Composter/Poltergeist is a connected final gameplay chain, not a collection of
  unrelated registrations. Restoring it requires Ghost Town infrastructure, entity behavior, block entities,
  networking/particles, effects and criteria in dependency order.
- The showcase Pay Dirt reward recollection is not the final 1.20.1 loot contract. Final data contains iron nuggets,
  gold nuggets, raw iron, raw gold, Emerald and Dirt; it contains neither Silver Nugget nor Diamond.
- Current Saguaro, Tumbleweed, Scuttler/Pink Bud, fossils, Paydirt, Glowfish and Mushroom underground data are already
  implemented. This audit does not reopen their accepted rendering or movement.

## 3. Mushroom Fields feature matrix

| Feature | Showcase observation | Final 1.20.1 source status | Current 1.21.10 status | Classification | Action required | Dependencies / notes |
|---|---|---|---|---|---|---|
| Underground cave size/shape | Large open mushroom caves | No BM carver, density/noise function or surface rule is registered. BM decorates existing cave floors. | No BM terrain-shape alteration, correctly. | PASS | Do not add cave-noise changes. Runtime compare representative final/current caves only. | `BMFeatures.init`; no carver registry in final source. |
| Underground mycelium patch | Stone-like cave floors covered in mycelium and vegetation | Reachable `mushroom_fields/underground_mycelium`: `grass_patch`, floor surface, depth 1, replaceable `#biomemakeover:ore_replaceable`, count 12, Y -30..60, downward environment scan through air up to 12. | Configured/placed IDs and biome injection exist. Registry decoding and basic Mushroom generation have runtime passed; representative underground density remains unmeasured. | PARITY PARTIAL | Target fresh underground Mushroom Fields and compare distribution/substrate coverage; change only on demonstrated data mismatch. | Configured feature -> nested `underground_vegetation` -> placed feature -> `UNDERGROUND_DECORATION`. |
| Underground vegetation provider | Dense fungal cave dressing | Nested final configured feature supplies the vegetation used by the patch. | Present under historical ID. | PARITY PARTIAL | Validate actual member weighting and observed placement during the focused Stage 3 follow-up. | Owned with underground mycelium, not a separate biome terrain system. |
| Underground huge glowshrooms | Giant bioluminescent fungi | Reachable random-boolean configured selector between huge green and huge purple; placed count 120, Y -30..60, biome filter. | Present and injected. Runtime test has observed underground purple glowshrooms, but not a systematic huge-feature sample. | PARITY PARTIAL | Runtime sample green/purple huge fungi underground. | No orange member in this underground selector. |
| Purple huge glowshroom | Giant glowing purple cap | Registered custom feature and configured feature; purple cap/stem blocks emit released light values and are obtainable. | Blocks, feature type, data, loot, recipes and rendering restored. | PASS | Preserve; retain targeted visual/worldgen regression coverage. | Small purple plant bonemeals to this configured feature. |
| Green huge glowshroom | Giant glowing green layered cap | Registered custom feature and configured feature. The released green configured JSON references the purple feature type; released behavior is intentionally retained by Stage 3. | Restored with documented released quirk. | PASS | No speculative correction. | Small green plant bonemeal route. |
| Orange huge glowshroom | Underwater giant orange form | Registered custom feature/configured data; orange small plant is waterloggable and worldgen is intentionally aquatic. | Restored; underwater orange glowshrooms runtime/source-confirmed. | PASS | Preserve unusual aquatic behavior. | Separate from underground green/purple selector. |
| Small glowshrooms | Purple, green, orange cave/surface plants | Three reachable configured/placed chains, light level 13; orange uses underwater implementation. | Restored and runtime-observed. | PASS | None beyond regression. | Biome vegetation injection and mycelium bonemeal ecology. |
| Mycelium roots and sprouts | Cave groundcover | Reachable registered blocks/configured/placed data; patch vegetation and mycelium bonemeal also place them. | Restored; roots observed at runtime. | PASS | Preserve. | `MyceliumBlockMixin`, placement/survival tags. |
| Tall red/brown mushrooms | Tall decorative fungi | Reachable blocks and placed features; included in bonemeal ecology. | Restored and packaged. | PASS | Runtime distribution remains ordinary regression coverage. | Double-block survival contract. |
| Wild Mushrooms | Multi-piece crossed cluster | Reachable block, potted form, spread/survival behavior and placed feature. | Source/resource-verified historical appearance and runtime accepted. | PASS | Do not redesign. | Three blockstate/model variants. |
| Blighted Balsa | Surface tree family in Mushroom Fields | Reachable tree feature injected at the unusual `FLUID_SPRINGS` step. | Restored; trees runtime-observed. Boats remain shared historical-infrastructure debt. | PARITY PARTIAL | Keep boats/chest boats deferred until faithful common boat support. | Tree/trunk placer, wood family, leaves/sapling; Stage 10A consumes family blocks. |
| Mushroom decorative/brick families | Showcase cave/building palette | Reachable blocks, recipes, loot and tags in final release. | Stage 3 restored ordinary families and resources. | PASS | Preserve. | Includes mushroom/glowshroom stem and cap masonry families. |
| Blightbat (showcase mushroom bat) | Purple/pink glowing mushroom-themed bat | `biomemakeover:blightbat` entity/model/texture exists, full-bright renderer carries a purple glowshroom. Final natural spawn and placement registration are commented; spawn egg and `blightbat_wing` are hidden. | Correctly excluded from current reachable registrations. | HISTORICAL/DISABLED | Do not restore for parity. Archive as a strong future Mythas candidate with explicit acquisition/spawn design review. | `BMEntities` commented calls; `BlightBatEntity`; `BlightBatRender`; hidden item registrations. |
| Glowfish natural spawn | Fish in Mushroom Fields water/caves | Reachable water-ambient spawn, weight 7, group 2-7; Salmon behavior, bucket and loot. | Restored and runtime accepted visually/stably. | PASS | Preserve accepted renderer; complete bucket save/reload acceptance as normal runtime debt. | Entity -> attributes -> spawn -> renderer -> bucket -> loot. |
| Raw Glowfish food | Edible raw catch | 1 nutrition, 0.1 saturation, always edible; independent 50% Night Vision and 50% Glowing, each 200 ticks. | Restored using modern consumable components. | PASS | None beyond runtime probability sanity check. | Raw fish is entity loot and Mushroom House loot/trade content. |
| Cooked Glowfish | Cooked fish grants effects | 5 nutrition, 0.6 saturation, always edible; same two independent 50%/200-tick effects. Smelting, smoking and campfire recipes. No consumption counter exists. | Restored. | PASS | Correct earlier narrative: repeated consumption merely repeats independent rolls. | Recipe/data conversion already runtime-loaded. |
| Glowshroom Stew | Three-color fungal stew | `biomemakeover:glowshroom_stew`; purple + green + orange + bowl; 5/0.6, always edible, bowl remainder, guaranteed Night Vision and Glowing for 1200 ticks. Advancement exists. | Restored and runtime data-loaded. | PASS | Runtime consume/bowl/effect check remains useful. | Also obtainable in Mushroom House loot. |
| Button Mushrooms music disc | Unique Mushroom Fields record | Reachable final item `button_mushrooms_music_disk`, comparator value 14, duration metadata 115; sound event `button_mushrooms`; original `button_mushrooms.ogg` is present. Mushroom House loot gives one guaranteed disc and Mushroom Trader trade data also references it. | Item/sound/audio absent under approved Stage 10A boundary. | DEFERRED BY EXISTING STAGE OWNERSHIP | Restore with Mushroom House/structure reward chain in Stage 10A; retain original packaged audio and licensing review, never substitute audio. | Item + sound + OGG + music-disc tag + loot + advancement. |
| Mushroom House | Showcase/reward context | Reachable jigsaw structure, set, pool, template, processor and loot. | Absent, assigned Stage 10A. | DEFERRED BY EXISTING STAGE OWNERSHIP | Stage 10A implementation precedes disc/structure reward acceptance. | Structure-owned processors/templates remain out of Stage 3. |
| Mushroom Trader | Green mushroom trader imagery | Separate final registered entity `mushroom_trader`, custom renderer and trade list; its free natural biome spawn is commented, but binary inspection proves `mushroom_house/house/house_1.nbt` directly embeds one trader with persisted offers, including the disc trade. It is not a modified vanilla Wandering Trader. | Entity absent under the Stage 10A boundary. | ACTIVE FINAL / STAGE 10A DEFERRED | Restore as a hard Mushroom House dependency while keeping free biome spawning disabled. Do not invent a vanilla Wandering Trader replacement. | Structure-template entity loading provides survival reachability; visible spawn egg also provides creative reachability. |
| Purple Mushroom Fields water | Showcase water appears purple | No final biome water-color, water-fog-color, client tint or biome-effects modification was found. | Vanilla Mushroom Fields water behavior retained. | SHOWCASE-ONLY/UNCONFIRMED | Close as non-parity unless a final runtime/binary source contradicts this audit. | Screenshot/version/resource-pack presentation is insufficient. |
| Green/glowing Wandering Trader thumbnail | Promotional green trader | No final vanilla Wandering Trader modification, texture swap or biome injection exists. The distinct Mushroom Trader is disabled from natural spawning. | No such modification. | SHOWCASE-ONLY/UNCONFIRMED | Do not invent content. | Promotional imagery is not an execution path. |

## 4. Mushroom Fields source evidence

- Registration/injection: `common/src/main/java/party/lemons/biomemakeover/init/BMFeatures.java`, methods and fields in
  `init()` for the Mushroom Fields tag.
- Underground data: `data/biomemakeover/worldgen/configured_feature/mushroom_fields/underground_mycelium.json`,
  `underground_vegetation.json`, `underground_huge_glowshrooms.json`; corresponding placed-feature JSON.
- Giant fungi: `level/feature/Huge*GlowshroomFeature.java`, `init/BMBlocks.java`, configured-feature JSON.
- Ecology: `mixin/mushroom/MyceliumBlockMixin.java`, `block/BMMushroomBlock.java`,
  `BMTallMushroomBlock.java`, `MushroomRootsBlock.java`, `MushroomSproutsBlock.java`, `WildMushroomBlock.java`.
- Blightbat reachability: `init/BMEntities.java` commented spawn/placement lines; `entity/BlightBatEntity.java`,
  `entity/render/BlightBatRender.java`; hidden registrations in `init/BMItems.java`.
- Glowfish/food: `entity/GlowfishEntity.java`, `init/BMItems.java`, `loot_tables/entities/glowfish.json`, cooking
  recipes and `recipes/glowshroom_stew.json`.
- Disc/structure: `init/BMItems.java`, `init/BMEffects.java`, `sounds/button_mushrooms.ogg`,
  `loot_tables/mushroom_house.json`, Mushroom House structure/set/pool/template/processor data.

## 5. Badlands feature matrix

| Feature | Showcase observation | Final 1.20.1 source status | Current 1.21.10 status | Classification | Action required | Dependencies / notes |
|---|---|---|---|---|---|---|
| Ghost Town structure | Jigsaw-generated abandoned town | Reachable vanilla jigsaw structure data `ghost_town`; Badlands biome tag; size 3; `beard_thin`; world-surface projection; structure set spacing 32, separation 12, salt 6969. Pools cover centers, roads, buildings and decoration; many NBT templates and two processor lists. | Entire system absent under Stage 10C. | DEFERRED BY EXISTING STAGE OWNERSHIP | Restore as Stage 10C prerequisite. | Structure/set -> pools/templates -> processors -> placement -> Ghost spawn override -> loot/archaeology. |
| Ghost Town loot | Unique town rewards | Three building loot tables selected by `GhostTownLootProcessor`; templates/pools are reachable. | Absent. | DEFERRED BY EXISTING STAGE OWNERSHIP | Restore with structure processors, not as disconnected loot. | Processor replaces marker container loot table deterministically. |
| Ghost entity | Neutral environmental spirit | Reachable through Ghost Town monster spawn override: weight 150, groups 2-4, piece bounding box. Monster implements persistent neutrality/anger; starts non-angry, retaliates/alerts nearby Ghosts, charges targets, flies/no-clips, keeps a home, performs poltergeist actions, has custom sounds/textures and immunity tag. | Entity, renderer, sounds, loot and spawn egg absent. | DEFERRED BY EXISTING STAGE OWNERSHIP | Restore after Ghost Town registry/data foundation; do not add global Badlands natural spawning. | Spawn predicate requires non-Peaceful and darkness; structure override supplies reachability. |
| Ghost environmental interaction | Opens/changes nearby blocks | Final `PoltergeistGoal` attempts four operations at range 10 whenever scheduled. Handler supports doors, buttons, trapdoors, lever, note block, fence gates, daylight detector and bell, with game events/sounds/particle packet. | Absent. | DEFERRED BY EXISTING STAGE OWNERSHIP | Port handler once, share with Poltergeist block, and audit server cost/sided packets. | `PoltergeistHandler`, `S2C_DoPoltergeistParticle`, particle and sounds. |
| Ectoplasm | Ghost drop and processing resource | Final Ghost loot rolls 0-1 Ectoplasm plus Looting uniform 0-1. Reachable through structure Ghosts. | Item/system absent. | DEFERRED BY EXISTING STAGE OWNERSHIP | Restore with Ghost loot and downstream recipes. | Item has special use on composter; also Poltergeist and Phantom Membrane recipes. |
| Ectoplasm composting | Ectoplasm converts composting to soul material | Using Ectoplasm in a non-empty vanilla composter swaps it to `ectoplasm_composter` at the same level; ordinary compostables finish it, and level 8 yields **Soul Soil**, then returns to vanilla composter. Advancement trigger `ectoplasm_compost`. | Absent. | DEFERRED BY EXISTING STAGE OWNERSHIP | Restore exact mixin/block/advancement semantics with world-safe block entity/state handling. | Result is Soul Soil, not Soul Sand. Functional system may warrant a Stage 10C subcheckpoint. |
| Poltergeist block | Localized one-block Ghost/randomizer | Reachable craft: Ectoplasm, Phantom Membranes, Soul Soil and Cauldron. Block entity repeatedly calls shared handler at range 5 while enabled. Redstone disables it; toggle sounds/particles. Standing inside applies/increases Possessed effect and triggers advancement. | Block, block entity, effect, networking, particles, recipe and criteria absent. | DEFERRED BY EXISTING STAGE OWNERSHIP | Restore as one complete functional chain after Ectoplasm. | Requires block entity, effect, sound, particle payload and `poltergeist_yourself` criterion. |
| Ghost Town music disc | Unique Badlands record | `ghost_town_music_disk`, comparator value 15, duration metadata 270; `ghost_town.ogg` exists. Reachable in Ghost Town container loot and archaeology; dedicated advancement. | Absent under Stage 10C. | DEFERRED BY EXISTING STAGE OWNERSHIP | Restore with town loot/archaeology and preserve original audio asset. | Supports a broader four-theme exploration-disc pattern. |
| Suspicious Red Sand | Archaeology in Ghost Towns | Reachable through `taniwha:suspicious_block_replacement` entries in Ghost Town road/building processors, using `archaeology/ghost_town`. | Absent; replacement utility unresolved. | DEFERRED BY EXISTING STAGE OWNERSHIP | Stage 10C must replace only the required processor semantics locally/vanilla, not port Taniwha. | Structure processor dependency, not independent Badlands terrain generation. |
| Ghost Town archaeology loot | Brushing rewards | One-roll table: three BM pottery sherds, iron/gold ingots, crude fragment, nested horse-armor and junk tables, Ghost Town disc, or damaged/possibly enchanted leather boots. | Absent. | DEFERRED BY EXISTING STAGE OWNERSHIP | Restore with Suspicious Red Sand processor and current archaeology schema. | Includes advancement/reward dependencies. |
| Paydirt generation | Water-edge mineral dirt | Reachable custom feature injected underground decoration; creates up to three small connected ellipsoid attempts adjacent to water/Paydirt. | Restored and generated. | PASS | Preserve; runtime distribution is accepted Stage 4 regression coverage. | `PaydirtFeature`, configured/placed data. |
| Paydirt loot | Random mineral rewards, Silk Touch preserves | Final loot: Silk Touch returns Paydirt. Otherwise the surviving-explosion group produces iron nuggets 1-4, gold nuggets 1-4, raw gold and raw iron with Fortune ore bonus, Emerald at 5/6/7/8% by Fortune level, and Dirt. **No Silver Nugget or Diamond exists in final table.** | Historical loot packaged/restored. | PASS | Do not add showcase-recalled Silver/Diamond rewards. Runtime verify representative drops/Fortune only. | Final `loot_tables/blocks/paydirt.json` is authoritative. |
| Tumbleweed lifetime/cleanup | Eventually breaks so populations do not accumulate | Final entity kills after age >1500 ticks (~75 s), or after 100 ticks with unchanged integer X/Z (~5 s stuck); non-immune damage kills it. Water damps horizontal motion and makes it rise. Break creates particles; no item loot. Runtime spawner groups players and respects gamerule. | Matching age/stuck cleanup exists; movement/rendering runtime accepted. | PASS | Add cleanup/lifetime to future runtime soak checklist; do not alter accepted physics/rendering. | Performance-relevant but no persistent NBT. |
| Saguaro shape probability | Usually one stage; rarer second/third | Final segment height uses historical exclusive `randomRange(4,8)` = 4-7. Arms gate is 80%; without arms the method returns before continuation. Conditional continuation is 10% first stage and 2% later, yielding effective ~8% and ~1.6% per eligible call. Recursion is theoretically unbounded with rapidly diminishing probability. | Current algorithm was corrected and runtime accepted. | PASS | Do not add a cap. Preserve accepted output and regression-test fresh chunks. | Arm count is two 80% / one 20% after arms gate; growth and worldgen share generator. |
| Saguaro interactions | Growth, potting, cactus damage | Final block is pottable, deals 1 cactus damage on contact, random ticks at 1/10 when valid, bonemeal success 45%, survives on tagged substrate and has connected directional arms. | Restored/runtime accepted. | PASS | Preserve. | Current Stage 4 contract/resources. |
| Barrel Cactus | Small damaging cactus, flowering source | Final normal/flowered blocks generate, grow/flower, are pottable, damage non-immune entities like cactus, and expose the flowered state to Scuttlers. | Restored/runtime observed. | PASS | Runtime verify ordinary contact damage and placement/growth without balance changes. | Immunity tag includes young/tagged exceptions; source decides. |
| Scuttler natural spawn | Rare Badlands animal | Final spawn: Badlands tag, creature category, weight 4, group 1-2; predicate has 50% random gate, 50-block absence of any Scuttler, and brightness predicate; max spawn cluster size 1. No Ghost Town dependency. | Restored with density suppression and runtime behavior accepted; natural abundance remains source-faithful even if sparse. | PASS | Do not increase rate. Continue representative natural-spawn observation only. | Entity registration + spawn placement + biome injection + predicate + cluster cap. |
| Scuttler -> flowered Barrel Cactus -> Pink Bud | Showcase acquisition loop | Final Scuttler searches an 8x8 horizontal area (range 4), navigates to flowered Barrel Cactus, spends 40 ticks eating, changes it to unflowered Barrel Cactus, fires event/sound behavior and drops 1-2 Pink Buds from `gameplay/scuttler_eating`; search/eat cooldowns persist. | Current port implements cactus seeking/eating, conversion and 1-2 Pink Bud output; attraction/breeding runtime passed. | PASS | Include acquisition-half test in final Stage 4 regression checklist. | Pink Bud then tempts/breeds/heals/passivates according to Scuttler food semantics. |
| Scuttler Tail | Showcase drop with no meaningful use | Final entity loot always emits exactly one `scuttler_tail`; no player-kill condition, chance, or Looting function. Item and acquisition advancement are reachable. A later Antidote/progression relationship exists outside basic Badlands ownership. | Item, loot and advancement restored. | PASS | Do not invent a use in parity. Keep possible Mythas use separate. | Final loot table is unconditional one-roll item. |
| Surface fossils | Badlands fossils | Reachable custom placed feature chooses among 18 vanilla templates: four Overworld spine fossils and fourteen Nether fossils, random rotation, surface-height placement with ignore-air processor. | Restored and current feature generation present. | PASS | Runtime sample multiple variants/frequency; no redesign. | Historical Taniwha ignore-air behavior already locally translated. |
| Cowboy patrol system | Marauder/cowboy replacement | Reachable Badlands patrol conversion; mounted Cowboys, leader/custom banner, horse hat/persistence and modern captain reward translation. | Runtime accepted after remediation. | PASS | Freeze. No Cowboy Witch exists. | Modern Ominous Bottle translation is intentional compatibility, horse ear extension is Mythas-only visual layer. |
| Cowboy patrol horse cleanup | Patrol horses should not accumulate | Final `CowboySpawned` marker persists. With no controlling passenger, saddled, armored or leashed clears marker and prevents despawn; otherwise marked horse may despawn. With PatrollingMonster controlling passenger, delegates to rider despawn. Other controlling passenger falls through to marker result. Final code does not explicitly test `isTamed()`. | Current mixin mirrors this final contract and has passed patrol save/runtime work. | PASS | Source wording supersedes showcase shorthand: preservation actions are saddling, armor or leash; do not add a tame test without evidence. | Audit current future changes against exact NBT names `Hat` and `CowboySpawned`. |

## 6. Badlands source evidence

- Structure chain: `data/biomemakeover/worldgen/structure/ghost_town.json`, `structure_set/ghost_towns.json`,
  `template_pool/ghosttown/*.json`, `processor_list/ghosttown_*.json`, `structures/ghosttown/**/*.nbt`,
  `level/generate/GhostTownLootProcessor.java`.
- Ghost/poltergeist: `entity/GhostEntity.java`, `entity/render/Ghost*.java`, `level/PoltergeistHandler.java`,
  `block/PoltergeistBlock.java`, `block/blockentity/PoltergeistBlockEntity.java`, particle payload/resources.
- Ectoplasm processing: `item/EctoplasmItem.java`, `mixin/InputContainerMixin_EctoCompost.java`,
  `block/EctoplasmComposterBlock.java`, entity loot, recipes and advancements.
- Archaeology: Ghost Town processor lists and `loot_tables/archaeology/ghost_town*.json`.
- Paydirt/fossils: `level/feature/PaydirtFeature.java`, `SurfaceFossilFeature.java`, corresponding configured/placed
  data and Paydirt block loot.
- Tumbleweed/Saguaro/Scuttler: their entity/block/feature classes, `TumbleweedSpawner.java`, spawn registration in
  `BMEntities.java`, `loot_tables/entities/scuttler.json`, `loot_tables/gameplay/scuttler_eating.json`.
- Horse cleanup: `mixin/badlands/HorseMixin.java`.

## 7. Cross-biome gameplay design patterns

The final source supports these recurring audit patterns; later stages must trace the complete loop rather than count
registrations.

1. **Biome structure -> unique entity/interaction -> unique resource -> functional processing.** Ghost Towns create
   Ghost reachability; Ghosts create Ectoplasm; Ectoplasm enables soul-soil composting and Poltergeist.
2. **Biome mob -> environmental interaction -> player-facing resource loop.** Scuttlers consume a flowered Barrel
   Cactus to produce Pink Buds, which feed back into Scuttler interaction and breeding. Final Swamp disabled the earlier
   Dragonfly-wing/Toad loop, demonstrating why reachability must be checked per release.
3. **Biome exploration -> structure loot -> music disc -> advancement.** Mushroom House/Button Mushrooms, Ghost
   Town/Ghost Town, Sunken Ruins/Swamp Jives and mansion/Dark Forest Red Rose form a four-theme record/reward pattern.
4. **Ecology is distributed across data and code.** Mushroom cave appearance requires configured/placed data, custom
   feature types, biome injection, block survival and bonemeal mixins. A registration-only audit misses the result.
5. **Decorative-looking blocks may be functional systems.** Poltergeist is a block entity, redstone-controlled world
   interaction source and effect applicator; Ectoplasm Composter modifies vanilla automation paths.
6. **Historical showcase loops can be disabled later.** Blightbat and Toad content demonstrate that showcase presence
   is insufficient without a final execution/acquisition path.

Method for remaining stages: trace `biome/structure or spawn source -> registry -> acquisition -> interaction -> unique
drop -> recipe/processor -> advancement/loot -> client/network/persistence`, then separately prove every link reachable.

## 8. Historical/disabled and future Mythas candidates

These are explicitly outside strict released parity and are not implementation-stage work unless separately approved.

| Candidate | Classification | Preservation note |
|---|---|---|
| Blightbat ecosystem | HISTORICAL/DISABLED; MYTHAS CANDIDATE | Restore only through a later approved spawn/acquisition/design decision; preserve final hidden IDs and assets as evidence. |
| Toad/Tadpole/Dragonfly Wing ecosystem | HISTORICAL/DISABLED; MYTHAS CANDIDATE | Strong cohesive future restoration candidate: predation, wings, breeding, tadpole/bucket lifecycle and Toad products. Not Stage 5 parity. |
| Dynamic environmental light for moving Lightning Bugs | MYTHAS CANDIDATE | Final bugs are full-bright/particles only; placed bottle is level-15 light. Any moving light is new behavior. |
| Decayed active shield blocking | MYTHAS CANDIDATE | Preserve carried-shield parity first; design later. |
| Small Lily Pad enhanced multi-piece placement | MYTHAS CANDIDATE | Do not conflate with released placement contract. |
| Witch/Living World integration and dedicated Witch-system audit | MYTHAS CANDIDATE / FUTURE AUDIT | Audit final Witch Hat, Warts, brewing, quests, Sunken Ruins and miniboss as a separate connected system. |
| Scuttler/Hermit Crab Witch Hats and biome mob heads | MYTHAS CANDIDATE | Cosmetic/collection layer, not historical parity. |
| Scuttler Tail future use | MYTHAS CANDIDATE | Final basic loot has no meaningful Badlands use; do not backfill during parity. |
| Ghost Town jigsaw patterns as Mythas caravan/travel reference | MYTHAS CANDIDATE | Reference architecture only; Ghost Town itself remains required final parity. |
| Cowboy leader-horse ear extension | Existing MYTHAS ENHANCEMENT | Render-only accepted current layer. It is not evidence for historical horse-hat geometry and must remain separately documented. |

## 9. Prioritized remediation queues

### Mushroom Fields

**P0 - biome/core worldgen**

1. No statically missing final underground chain was found. Perform a focused fresh-chunk underground runtime survey
   of `underground_mycelium`, nested vegetation and huge green/purple glowshrooms from Y -30..60.
2. If distribution differs, compare codec translation, placement count, environment scan and replaceable tag before
   changing code. Do not add carvers/noise/surface rules.

**P1 - major gameplay/entity systems**

1. Stage 10A Mushroom House structure chain.
2. Reconfirm Mushroom Trader remains non-survival-reachable while inspecting the historical template at runtime; do
   not activate natural spawn absent new final evidence.

**P2 - items/recipes/loot/effects**

1. Restore Button Mushrooms disc/sound/audio, Mushroom House loot and disc advancement in Stage 10A.
2. Complete Glowfish bucket save/reload and food/stew runtime acceptance; no static gameplay gap is identified.
3. Resolve shared Blighted Balsa boat/chest-boat infrastructure in its approved shared boat stage.

**P3 - cosmetic/minor parity**

1. No purple-water parity task.
2. No green Wandering Trader task.
3. Keep Blightbat assets archived as disabled historical content.

### Badlands

**P0 - biome/core worldgen**

1. Stage 10C Ghost Town jigsaw structure/set, pools, templates, placement and processors.
2. Stage 10C Suspicious Red Sand replacement processor and current archaeology data conversion.

**P1 - major gameplay/entity systems**

1. Ghost entity registration, structure spawn override, AI/neutrality/home/persistence, renderer, sounds and immunity.
2. Shared Poltergeist interaction handler, explicitly sided particle networking and supported-block behavior.
3. Ectoplasm Composter and Poltergeist block/block-entity/effect/criterion chain.

**P2 - items/recipes/loot/effects**

1. Ectoplasm item/loot, Soul Soil compost output, Phantom Membrane and Poltergeist recipes.
2. Ghost Town container and archaeology loot, pottery rewards and Ghost Town music disc/audio/advancement.
3. Runtime-test current Paydirt rewards, Scuttler Tail and Pink Bud acquisition without changing final probabilities.

**P3 - cosmetic/minor parity**

1. Ghost translucent/angry rendering, particles and full sound suite after server behavior is stable.
2. Sample fossil variants and Tumbleweed cleanup under soak conditions.
3. Preserve current accepted Cowboy visuals, Saguaro shapes, Scuttler animation and Tumbleweed motion.

## 10. Recommended implementation order

1. Close Stage 5 runtime acceptance; do not mix Swamp remediation into this roadmap.
2. Run the Mushroom Fields underground verification checkpoint. It is primarily a runtime/data audit unless a
   concrete mismatch appears.
3. Complete Stage 10A Mushroom House foundation, then its loot/disc/advancement. Reassess Mushroom Trader reachability
   only from final template/runtime evidence.
4. For Badlands Stage 10C, first add structure registry/data compatibility and deterministic template/processor
   validation; then generate a template-only test town.
5. Add Suspicious Red Sand/archaeology replacement and loot after structure placement works.
6. Add Ghost entity server contract and structure spawn override; then client renderer/sounds and persistence tests.
7. Add Ectoplasm loot/item, followed by the Ectoplasm Composter conversion chain.
8. Port the shared Poltergeist handler/network/particle contract once, then connect Ghost AI and the Poltergeist block
   entity to it.
9. Add Poltergeist effect/recipe/advancements and Ghost Town disc/reward polish.
10. Finish targeted single-player, dedicated-server, save/reload, multiplayer and existing-world-copy acceptance.

This order prevents disconnected placeholder items and avoids restoring Ghosts globally before their released
structure reachability exists.

## 11. Conclusions changed or clarified by this audit

- **Invalidated concern:** Mushroom underground configured/placed features were not omitted; Stage 3 already restored
  them. What remains is runtime distribution verification, not a new cave-terrain implementation.
- **Clarified:** BM creates a mushroom-cave atmosphere inside vanilla caves; it does not enlarge caves in final 1.20.1.
- **Clarified:** the showcase mushroom bat is final-source `blightbat`, but final survival reachability is disabled.
- **Clarified:** cooked Glowfish has no repeated-consumption state machine; both effects are independent static 50%
  rolls per consumption.
- **Confirmed omission with existing ownership:** Button Mushrooms disc is reachable final content via Mushroom House
  and remains Stage 10A work.
- **Strengthened existing conclusion:** Ghost Town is a complete, reachable, interconnected Stage 10C ecosystem—not
  merely archaeology templates or a decorative structure.
- **Corrected showcase reward recollection:** final Paydirt contains neither Silver Nugget nor Diamond.
- **Confirmed:** Tumbleweed cleanup, exact effective Saguaro continuation rates, Scuttler density suppression,
  Scuttler/Pink Bud acquisition, unconditional Scuttler Tail and patrol-horse cleanup are represented in current code.

## 12. Audit-only validation checklist

- Production source/resource changes: expected none.
- Registry changes: expected none.
- Stage 5 changes: expected none.
- Stage 6 work: prohibited.
- Accepted Cowboy Hat/horse-ear, Glowfish and Tumbleweed systems: untouched.
- `git diff --check` and parity validator must pass before committing this document.
- No production JAR is required for a documentation-only checkpoint.
