# Original Biome Makeover Master Feature Audit

Audit date: 2026-08-23  
Scope: historical/original Biome Makeover only  
Status: Step 2 audit; no comparison with the 1.21.10 port

Showcase-informed follow-up audits that compare these final-source findings
with the current port are maintained separately:

- `docs/SHOWCASE_INFORMED_MUSHROOM_BADLANDS_AUDIT.md`
- `docs/SHOWCASE_INFORMED_DARK_FOREST_AUDIT.md`
- `docs/SHOWCASE_INFORMED_WOODLAND_MANSION_AUDIT.md`

The Dark Forest/Mansion follow-up directly inventories the pinned Mansion
resource tree at 168 NBT files and records the older `228` description as a
count discrepancy to resolve before Stage 11, not as permission to omit or
invent templates.

This manifest independently describes the original Lemonszz implementation. It does not use the current port as a feature checklist, and it makes no parity judgment about that port. “Implemented” means a registration and reachable execution/data path was found in the pinned original source. Resource-only, commented-out, developer-only, and divergent-development content is explicitly separated.

## A. Historical source benchmark

Authoritative repository: `https://github.com/Lemonszz/Biome-Makeover` (archived by its owner on 2024-08-27).

Primary audit source:

- Branch: `1.20`
- Commit: `2f314c0596af095a4890995a465f308f69476b4a`
- Commit date/subject: 2024-08-27, `Update README.md`
- Minecraft: 1.20.1
- Mod version: `1.20.1-1.11.4`
- Architecture: common Architectury source plus Fabric and Forge frontends
- Fabric Loader/API: 0.14.22 / 0.89.3+1.20.1
- Forge: 47.2.1
- Taniwha dependency: 1.20.0-5.4.4

This is the last complete released branch, the repository default, and the strongest single definition of shipped original gameplay. Both `fabric/` and `forge/` exist at this commit and share the same `common/` gameplay implementation.

Secondary evolution sources, used only to identify version boundaries and divergent content:

| Ref | Commit | Minecraft/mod version | Audit role |
|---|---|---|---|
| `master` | `b83fb73fcc347f1c45ebec8d03465b2ac76b866f` | 1.16.5 / 1.3.8 | Earlier monolithic-era reference |
| tag `1.3.0` | `fde0d21a16415e71b8aeadeec29e5798b7706dcd` | 1.16.5 era | Last available named early release tag |
| `1.18` | `1c66d9f15648f10557e442a56fc2f38617f5d272` | 1.18.2 / 1.4.32 | Mature 1.18 release line |
| `1.18.2` | `654f624695014c2b9b100593e22a09ba770d8417` | 1.18 RC metadata / 1.4.8 | Earlier 1.18 branch head |
| `1.19` | `9416a1d0b5936447bfd6b9729e8269891c9cb4ab` | 1.19 / 1.5.10 | Initial 1.19 line |
| `1.19.1` | `d3d7f20f3f7dd827ef30b7c1c1b7923ce9a959d7` | 1.19.2 / 1.6.4 | 1.19.2 release line |
| `1.19.3` | `a9a8c4567597cba86e20d640bf89178a6108b775` | 1.19.3 / 1.7.2 | 1.19.3 release line |
| `1.19.4` | `740f8d93ed1460dcafbbd841f5ee8cd464086c0a` | 1.19.4 / 1.9.5 | Immediate predecessor |
| `dev` | `f369f833e1be66366f5d8a8f5f1502ac4f92e390` | 1.20.1 / 1.12.0-DEV1 | Unreleased divergent experiments only |

Available historical release tags are sparse: `1.0.0`, `1.0.1`, `1.2.3`, `1.2.11`, and `1.3.0`; there is no tag for the final 1.20.1 release. Therefore the pinned `1.20` branch commit, not a tag, is authoritative.

Source notation below is relative to the primary commit. Principal roots are `common/src/main/java/party/lemons/biomemakeover/` and `common/src/main/resources/`.

## B. Version and branch analysis

### Released evolution

- The project progressed from 1.16.5 through 1.18.2, 1.19/1.19.2/1.19.3/1.19.4, and 1.20.1.
- By 1.20.1 it was a shared Architectury implementation targeting both Fabric and Forge. The Fabric frontend is not a reduced gameplay branch; loader-specific code is primarily startup and rendering integration.
- The 1.20.1 line includes Minecraft 1.20 archaeology adaptations: suspicious red sand, Ghost Town archaeology loot, pottery sherds/patterns, and a cladding smithing template. These are material additions relative to 1.19.4.
- A 1.19.4-to-1.20 diff changes 1,168 files, so older behavior should not be silently mixed into the primary definition. The 1.20 branch is preferred because it is the final released, integrated, dual-loader implementation.

### Divergent `dev` line

`dev` shares merge base `b55a46874ba0240bb6511a45ed3e470a71b3c49a` with `1.20` but is not a descendant of the final release branch. It contains 1.12.0-DEV1 systems not shipped at the archived default head:

- Dust Devil entity, tornado renderer/sounds, grinding recipes, and REI integration
- succulents, Aloe Vera, large flower pots, new sandstone and gilded-lapis families
- camel/horse/llama caravanning and equipment/chest camel systems
- item reinforcement and Scarab Elytra rendering
- new plant-placement mixins and related worldgen

These are evidence of intended future development, not part of the defensible released 1.11.4 parity target. They are cataloged as unfinished/abandoned in section S.

## C. Complete feature-family catalog

The released implementation contains 24 top-level feature families:

1. Mushroom Fields makeover
2. Badlands makeover
3. Swamp makeover
4. Dark Forest makeover
5. Beach Helmit Crab ecology
6. Ghost Town structure and archaeology
7. Mushroom House structure/trader content
8. Sunken Ruins
9. Reworked Woodland Mansion
10. Adjudicator boss encounter
11. Witch quest/trade system
12. Altar curse system
13. Poltergeist/ectoplasm composting
14. Stone Golem and cladding equipment
15. Illunite crystal system
16. Mesmerite stone/worldgen family
17. peat/reed/swamp construction families
18. glowshroom/mushroom construction families
19. four wood sets and boats
20. terracotta-brick/tapestry decoration systems
21. creature ecosystem (20 mob/entity types plus one projectile)
22. potions/effects/curses
23. music, ambience, particles, and client rendering
24. data-driven loot, advancements, recipes, tags, quests, and trades

The mod adds no new biome registry entries. It substantially modifies tagged vanilla/modded biome groups.

## D. Biome and theme catalog

### Mushroom Fields

Biome selector: `#biomemakeover:mushroom_fields`, populated from optional common/Forge mushroom tags. Evidence: `BMFeatures.MUSHROOM_FIELD_BIOMES` and `data/biomemakeover/tags/worldgen/biome/mushroom_fields.json`.

Worldgen injected by `BMFeatures.init()`:

- underground mycelium and underground vegetation
- Blighted Balsa trees
- green, purple, and underwater orange glowshrooms
- underground huge glowshrooms
- mycelium sprouts and roots
- tall brown/red mushrooms and wild mushrooms

Blocks/mechanics:

- three luminous glowshroom plants and huge-block sets; glow levels 13–15
- glowshroom stem; glowshroom, vanilla mushroom, and stem brick families
- Blighted Balsa full wood set, leaves, sapling, boat/chest boat
- blighted cobblestone and blighted stone-brick construction families
- Mushroom Roots/Sprouts, tall mushrooms, Wild Mushrooms
- Mycelium and Huge Mushroom mixins support makeover behavior

Entities/structures/loot:

- Glowfish naturally spawn, weight 7, groups 2–7, water ambient.
- Mushroom House jigsaw structure, spacing 12/separation 6, with a house template and processor.
- Mushroom Trader code, renderer, trade list, spawn egg, and house integration resources exist, but natural biome spawning is commented out. See section S.
- Blight Bat natural spawning is also commented out.
- Mushroom-themed music disc (`button_mushrooms`) and advancement chain.

Dependencies: biome tag → `BMFeatures` injection → custom feature codecs/JSON → glowshroom/wood blocks; structure set → biome tag `has_structure/mushroom_house` → jigsaw pool/NBT → processor/trader resources.

### Badlands

Biome selector: vanilla `#minecraft:is_badlands` plus optional common badlands tag.

Worldgen:

- Barrel Cactus and flowered growth state
- custom Saguaro Cactus feature
- Paydirt underground decoration
- Surface Fossils
- Tumbleweed server-level spawner governed by `BMdoTumbleweedSpawning`

Entities and mechanics:

- Scuttler spawns weight 4, groups 1–2; rattles, avoids players unless passive, eats flowers, breeds/tempts from the `scuttler_food` tag, and drops tail-related loot.
- Badlands patrols are replaced at the patrol-member mixin path by Cowboys riding Horses; leaders apply banners/hats to horses.
- Ghost Town jigsaw structure: spacing 32/separation 12; Ghost spawn override 150, groups 2–4; roads, buildings, decoration and center pools; archaeology/suspicious-red-sand loot.
- Tumbleweed is a physics entity, not a normal mob; wind/world-event code drives spawning/motion and its own immunity/sound/loot handling.

Blocks/items: Paydirt, Suspicious Red Sand, Saguaro/Barrel Cacti, Terracotta Bricks and all 16 dyed variants with slab/stair/wall families, Cowboy Hat, pottery sherds/patterns, Badlands music disc and advancements.

### Swamp

Biome selector: optional common/Forge swamp tags. Vanilla swamp trees are removed and replaced.

Worldgen:

- Swamp Cypress and Willow trees with custom trunk/foliage/decorators
- big mushrooms, flowers, pads
- Peat top-layer generation
- Reeds top-layer generation
- custom swamp bonemeal behavior produces seagrass, reeds/cattails, small lily pads, Water Lilies, and vanilla lily pads
- Sunken Ruins structure: spacing 24/separation 9, cluster probability 0.8, large probability 0.6, nine templates

Flora/blocks:

- Willow and Swamp Cypress full wood sets, leaves, saplings, boat/chest boat
- Willowing Branches, Cattail, Reed, Small Lily Pad, Water Lily
- Peat, Mossy Peat, Peat Farmland, Dried Peat and dried/mossy/cracked brick families
- Reed Thatch plus slab/stairs
- Buttonbush and Marigold
- Lightning Bug Bottle functional light/display block

Entities:

- Decayed: weight 60, one per group; amphibious zombie with land/water AI, Player/Villager/Golem/Axolotl/baby-Turtle targets and distinct wet/dry sounds/animation.
- Dragonfly: weight 20, groups 3–8; flying Toad target with six data-persisted variants.
- Lightning Bug: weight 20, singletons; flying group behavior, capturable into bottles, alternate visual entity type.
- Toad/Tadpole reproduction code exists, but Toad natural spawn injection is commented out; reachability requires other paths/runtime verification.
- Swamp music/foliage tinting and blossom/lightning ambience are client-integrated.

### Dark Forest

Biome selector: exactly `minecraft:dark_forest`.

Worldgen injected:

- grass, tall grass, flower mixture
- Itching Ivy top-layer feature
- tree selector (Ancient Oak, small Ancient Oak, small Dark Oak)
- Wild Mushrooms
- Mesmerite fissure local modification, which composes boulder and underground Mesmerite features

Flora/blocks:

- full Ancient Oak wood set, leaves/sapling, boat/chest boat
- Black Thistle, Foxglove, Itching Ivy, Ivy, Moth Blossom, Wild Mushrooms
- Mesmerite and Polished Mesmerite slab/stair/wall families

Entities:

- Owl: weight 20, groups 1–4
- Rootling: weight 40, groups 2–6
- Moth: weight 90, groups 2–3
- added Foxes weight 4/groups 2–2 and Rabbits weight 4/groups 2–3

Structure/encounter:

- Reworked Mansion replaces the theme’s mansion experience through a custom structure type, template-driven layout, dungeon and Adjudicator boss arena.
- Dark Forest music disc, advances, Altar/curses, Illunite, cladding and mansion loot form a connected progression.

### Beaches

Beach selector includes optional common beach tags plus vanilla Beach and Stony Shore.

- Helmit Crab spawns weight 6, groups 2–5.
- It seeks/equips shell items (including helmets/heads), hides or escapes into its shell, breeds from fish, follows parents, leaves water, attacks, and drops its equipped shell plus crab loot according to source rules.
- Raw/Cooked Crab and Crab Chowder form its food/recipe family.

### Woodland Mansion / illager theme

This is not a biome registry addition, but it is a major original theme:

- custom Mansion `StructureType`, piece type, no-waterlogging processor, terrain/beard integration and directional-data markers
- 200+ NBT resources spanning entrance, corridors, rooms, large rooms, gardens, walls, towers, roofs, dungeon, boss room, fillers
- Pillager/Vindicator/Evoker/Witch integrations, custom loot, tapestries, Altar, Poltergeist and Adjudicator
- Witch quests are global Witch behavior, gated by wearing a Witch Hat and data-driven quest categories/rewards

## E. Complete block catalog

The English language catalog contains 285 named block entries; `BMBlocks` has 123 direct/static block supplier declarations plus factory-generated families. Generated potted/support blocks and factory families make a raw one-line source count misleading, so 285 named entries is the most practical released-content count. The registration authority is `init/BMBlocks.java`; behaviors are in `block/`; block entities in `block/blockentity/`.

### Full families

- Woods: Ancient Oak, Blighted Balsa, Willow, Swamp Cypress. Each factory supplies logs/wood/stripped variants, planks, slab, stairs, fence/gate, door/trapdoor, button/pressure plate, signs/hanging signs and boats/chest boats; leaves and saplings are separately registered.
- Mesmerite: base and polished blocks, each with slab, stairs, wall.
- Mushroom masonry: Red Mushroom, Brown Mushroom, Purple/Green/Orange Glowshroom, Glowshroom Stem and Mushroom Stem bricks; slab/stair/wall variants.
- Blighted stone: cobblestone and stone bricks with slab/stair/wall variants.
- Peat masonry: dried, mossy dried and cracked dried peat bricks with slab/stair/wall variants.
- Reed Thatch: base/slab/stairs.
- Terracotta Bricks: undyed plus all 16 dye colors, each with slab/stair/wall.
- Cracked Bricks: base/slab/stair/wall.
- Tapestries: all 16 dye colors plus Adjudicator tapestry, with standing/wall implementations backed by one block-entity type.

### Plants/natural blocks

- Purple/Green/Orange Glowshroom, corresponding luminous cap blocks, stem
- Mycelium Sprouts/Roots, Tall Brown/Red Mushroom, Wild Mushrooms
- Saguaro Cactus, Barrel Cactus/Flowered Barrel Cactus
- Buttonbush, Marigold, Black Thistle, Foxglove
- Cattail, Reed, Small Lily Pad, Water Lily, Willowing Branches
- Peat, Mossy Peat, Peat Farmland
- Ancient Oak/Blighted Balsa/Willow/Swamp Cypress leaves and saplings
- Ivy, Itching Ivy, Moth Blossom, Rootling Crop
- Paydirt, Suspicious Red Sand, Surface Fossil placements
- potted variants for saplings, glowshrooms, cactus and Wild Mushrooms

### Functional/special blocks

- Altar + `AltarBlockEntity`: inventory/menu, curses items in exchange for levels/ingredients, active state, renderer and looping cursing sound; strict behavior controlled by config.
- Poltergeist + `PoltergeistBlockEntity`: toggled supernatural block interaction, particle/sound/network effects and Ectoplasm relationship.
- Ectoplasm Composter and Peat Composter: custom compost processors and advancement triggers.
- Lightning Bug Bottle + block entity/renderer: stores/displays a bug and emits light.
- Tapestry + block entity/renderer: colored and Adjudicator wall/standing decoration.
- Directional Data + block entity/menu/screen: hidden structure-development marker block; structure-template mixin consumes its saved orientation/action data. Not ordinary survival content.
- Budding Illunite, small/medium/large buds and full cluster: Amethyst-like growth chain with custom luminous crystal sound; budding block has no loot table.
- Cladded Stone and Crude Cladding Block: golem/progression materials.
- Saguaro/Barrel Cactus, Black Thistle, Itching Ivy, Moth Blossom, Wild Mushrooms and Peat Farmland have custom behavior classes rather than being decorative-only.

## F. Complete item catalog

`BMItems.java` contains 69 direct/static item suppliers including four runtime-created Cladded armor pieces. The English resource contains 85 item translation keys, including generated boat/sign items, description keys and one stale Giant Slime spawn-egg key; practical reachable item count is therefore approximately 80 plus block items.

### Standalone items

- Foods: Glowfish/Cooked Glowfish, Raw/Cooked Toad, Raw/Cooked Crab, Crab Chowder, Bulbus Root/Roasted Bulbus Root, Glowshroom Stew.
- Mob materials: Scuttler Tail, Ectoplasm, Dragonfly Wings, Bat Wing, Blightbat Wing, Wart, Soul Embers, Illunite Shard, Moth Scales.
- Rootling agriculture: Rootling Seeds and nine colored Bud/Petal items (pink, magenta, blue, cyan, brown, gray, light blue, purple, plus resource/loot variants).
- Utility: Lightning Bottle throwable, Stunt Powder, Enchanted Totem, Glowfish Bucket, Tadpole Bucket.
- Equipment/progression: Cowboy Hat, Witch Hat, Crude Cladding, Crude Fragment, Cladding Upgrade Smithing Template, Cladded armor set, Cracked Brick.
- Archaeology: Refined, Worker and Whinny Pottery Sherds; Cracked Brick also registers a pot pattern.
- Four music discs: Button Mushrooms, Ghost Town, Swamp Jives, Red Rose.
- Four wood boat/chest-boat pairs generated through Taniwha boat types.
- Spawn eggs for Glowfish, Mushroom Trader, Blightbat, Ghost, Scuttler, Cowboy, Toad, Tadpole, Dragonfly, Lightning Bug, Decayed, Owl, Rootling, Moth, Helmit Crab and Stone Golem. Toad/Tadpole and Blightbat eggs are deliberately hidden/disabled; Adjudicator/Mimic are no-summon and have no normal eggs.
- `icon_item` is a hidden fake creative-tab icon, not gameplay content.

Special interactions are implemented by `item/EctoplasmItem.java`, `EnchantedTotemItem.java`, `GlowfishBucketItem.java`, `HatItem.java`, `LightningBottleItem.java`, `StuntPowderItem.java`, and `item/Cursing.java`.

## G. Complete entity catalog

The registry contains 21 entity types: 18 distinct mob classes, the non-mob Tumbleweed entity, a second Lightning Bug variant type, and the Lightning Bottle projectile. The following catalog distinguishes reachability.

| Entity | Reachability and original behavior |
|---|---|
| Tumbleweed | Reachable via Badlands `TumbleweedSpawner` and gamerule; wind-driven rolling physics, collision/break sounds and loot; MISC entity, not a breedable mob. |
| Lightning Bottle | Reachable by item use; throwable projectile creates lightning/effects using network packets. |
| Glowfish | Natural Mushroom Fields water-ambient spawn; Salmon-derived movement, bucket capture, custom glowfish renderer/food/loot. |
| Blight Bat | Registered, rendered, attributed, loot/assets present; natural spawn lines commented out. Spawn egg hidden. Implemented but normally unreachable except commands/templates: DEAD/UNUSED IN NORMAL GAMEPLAY. |
| Mushroom Trader | Registered villager-like merchant, renderer/trade list/house assets; natural spawn line commented out. Spawn egg exists. Structure-marker reachability needs runtime verification. |
| Scuttler | Natural Badlands animal; flower finding/eating, rattling warning, player avoidance until passive, food-tag temptation/healing/breeding, offspring, tail loot and antidote brewing ingredient. |
| Ghost | Reachable in Ghost Town spawn overrides and structure markers; neutral anger, flying/charging, Poltergeist interaction, custom immunity tag, sounds and ectoplasm loot. |
| Cowboy | Reachable by Badlands patrol replacement and a permission-2 developer `/pillager` command; Pillager-derived mounted patrol, leader banner/horse hat, custom renderer/loot. |
| Decayed | Natural Swamp monster; Zombie-derived amphibious state/navigation, seeks/leaves water, swims upward, attacks Players/Villagers/Golems/Axolotls/baby Turtles, wet/dry animation and sounds, persistent state. |
| Dragonfly | Natural Swamp ambient flyer; six persisted variants, wander/loop sound, targetable/eaten by Toad. |
| Toad | Registered animal with tongue targeting, Slime Ball/Spider Eye temptation/breeding, tadpole-production goal and sounds. Natural spawn registration is commented out; reachability is incomplete. |
| Tadpole | Registered fish/Stuntable juvenile with bucket interaction and growth/state persistence; tied to Toad reproduction and Tadpole/Frog mixin paths; spawn egg/bucket hidden. |
| Lightning Bug | Natural Swamp ambient flyer, group-leader following, capture into Lightning Bug Bottle, light animation/particles; alternate entity type provides visual variant and is used by spawn egg/tag. |
| Owl | Natural Dark Forest tameable shoulder-riding animal; complete specification in section H. |
| Moth | Natural Dark Forest flying monster; attacks Players, avoids Owls, is attracted to/hugs Moth Blossoms, melee/flap/bite behavior and scales loot. |
| Rootling | Natural Dark Forest animal; fears specified living entities, seeks rain, dances, follows entities, inspects flowers, accepts Bone Meal, can be sheared for color-specific bud/petal loot, has flower/color state and Rootling crop progression. |

Stage 8 restores both reachable final-release entity ecosystems, including Rootling agriculture/foods and Moth Scales/Nocturnal brewing, and is runtime accepted. The Ectoplasm-dependent Phantom Membrane recipe and Stunt Powder remain with their later dependency owners.
| Adjudicator | Reachable boss in the Mansion boss room; multipart, phase-driven encounter using melee, bow, fangs/barrage, teleport, summon, mimic and Stone Golem phases; custom arena/room listener, sounds, model and boss loot. No normal spawn. |
| Adjudicator Mimic | Summoned only by Adjudicator Mimic phase; copies encounter behavior/appearance, removed when phase ends; no normal spawning/summoning. |
| Stone Golem | Player-buildable from Cladded Stone pattern; multipart neutral ranged golem, can be armed with Crossbow by interaction or dispenser, has player-created state, targeting/anger, turning sounds and projectile resistance. |
| Helmit Crab | Natural Beaches animal; seeks/equips shells, hides/escapes, breeding/temptation from fish, parent-following, water/land behavior, combat, shell rendering and custom shell/death loot. |
| Lightning Bug Alternate | Second registered Lightning Bug type with alternate constructor/appearance; used in tag and spawn egg, shares behavior and attributes. |

Entity loot tables exist for all principal mobs plus supplemental Pillager/leader and Witch Hat tables. `giant_slime.json` has no corresponding registered entity and is stale/dead data.

## H. Original Owl behavior specification

This section is derived only from the pinned original `OwlEntity.java`, `BMEntities.java`, `OwlRender.java`, `OwlModel.java`, entity tags, loot and sound resources.

### Implemented original behavior

- Registration: `biomemakeover:owl`, creature category, dimensions 0.7 × 0.8, tracking range 12.
- Biome spawn: `#biomemakeover:dark_forest` (vanilla Dark Forest in shipped tag).
- Spawn weight/group: weight 20, minimum 1, maximum 4.
- Spawn placement: `ON_GROUND`, `MOTION_BLOCKING` heightmap.
- Spawn predicate: block below is Grass Block or any Leaves-tag block, and raw brightness at the spawn position is greater than 2.
- No time-of-day check exists. Original natural spawning is not explicitly night-only.
- No custom despawn override exists; inherited tameable/animal persistence rules apply.
- Base attributes: flying speed 0.8, maximum health 6, movement speed 0.4, attack damage 2.
- Tamed attributes: maximum health becomes 20, attack damage becomes 4, and health is filled to 20. Untaming restores 6/2.
- Navigation: `FlyingMoveControl`; `FlyingPathNavigation` passes doors but cannot open them and cannot float. Fire/danger-fire path malus is -1.
- Goals, in priority order: Float; sit when ordered; melee attack; custom flying follow-owner (speed 1.2, start 10 blocks, stop 2, leaves allowed); food predicate temptation; breed; panic; look at Player; extended fly-onto-tree; random stroll; random look.
- Targets: defend owner from attacker, attack owner’s target, and—only while untamed—target any living entity in `#biomemakeover:owl_targets`.
- Owl target tag: Rabbit, Chicken, Silverfish, Endermite, Bat, Toad, Blight Bat, Dragonfly, both Lightning Bug types. Hunting is general/non-night-specific; no separate Rabbit/Chicken algorithm exists.
- Tree behavior: random-flight goal scans within ±3 X/Z and ±6 Y for empty two-block clearance over Leaves or Logs and selects the first qualifying bottom-center. It is opportunistic wandering/perching, not a remembered home tree.
- Flight state: synchronized `STANDING`/`FLYING`, determined by ground/water/ordered-sit status; flight increases a 0–7 lean value. Falling velocity is multiplied by 0.75 vertically, creating slow fall. Fall damage/checks are disabled. Flying dimensions change to 0.7 × 1.4.
- Food: any edible meat, not a single named item. The same predicate drives temptation, breeding, taming and healing.
- Wild interaction: if holding edible meat and the Owl has no attack target, consume one item (unless creative) and tame with 1-in-3 probability; success assigns owner, sits the Owl and broadcasts vanilla tame particles.
- Tamed healing: edible meat heals by the food item’s nutrition value and is consumed unless creative.
- Owner interaction: inherited `ShoulderRidingEntity` interaction is attempted first. If it does not consume the action, or the Owl is a baby, an owner interaction toggles ordered sitting. The superclass supplies shoulder serialization/storage, but neither it nor the pinned Owl registers `LandOnOwnersShoulderGoal`; unlike Parrot, the final Owl therefore has no reachable normal-AI shoulder transition.
- Breeding: ordinary `BreedGoal`; child is an Owl and inherits owner UUID/tamed state from the parent creating it. No nest or egg mediates reproduction.
- Baby: vanilla ageable child rendering through `AgeableListModel`; name tag is translated upward. No separate baby AI beyond inherited age/breeding rules.
- Sounds: exactly three Owl events—idle, hurt and death—with 4 idle, 3 hurt and 2 death audio variants. No alert, contact, baby, takeoff or hoot-separate event exists.
- Rendering: main texture `owl.png`; exact custom name `Hedwig` selects `owl_2.png`. Renderer always adds an emissive `owl_eyes.png` eyes layer; it is not conditioned on night.
- Animation: model has standing/walking, sitting, flight lean, flapping wing/tip and head yaw/pitch animation driven by movement/state. No explicit blink state, eyelid part or texture swap was found.
- Drops: entity loot table drops 1–2 Feathers plus Looting bonus. No Owl head.
- Persistence/NBT: superclass persists tame/owner/age/sit state; Owl additionally writes integer `OwlState` (`IDLE`/`ATTACKING`) and `StandingState` (`STANDING`/`FLYING`). `OwlState` is defined and persisted but no source path changes it during normal combat in the pinned class.
- Mansion integration: an `owl` structure data marker can instantiate an Owl in a custom Mansion piece.

### Explicitly not implemented in the released original

- no nests or Owl nest block
- no Owl egg, incubation, timer or hatching
- no sleeping/daytime roost behavior
- no day/night spawn, hunting, eye or despawn restriction
- no player-avoidance/fleeing behavior
- no remembered perch/tree/home
- no explicit blinking
- no separate raw Rabbit/raw Chicken rules
- no dedicated Owl breeding courtship or nest flow
- no Owl head or special head drop
- no custom daytime despawn

### Dead/resource-only/uncertain Owl evidence

- `OwlState.ATTACKING` exists but is not set by the pinned Owl class: implemented data shape, effectively unused state.
- Shoulder storage partly resides in vanilla `ShoulderRidingEntity`, but transfer is unreachable because the final Owl omits the explicit landing goal. Runtime failure to shoulder-mount is source-confirmed, not a 1.21.10 regression.
- Stage 7 final status: the released Owl contract, including tree landing, leaf-supported owner follow, taming, breeding, genuine babies/save-load/aging, hunting, emissive eyes and Hedwig, is runtime accepted. Shoulder mounting remains intentionally absent.
- Emissive eyes are unconditional at render-layer level; actual visibility under all render states is **NEEDS HISTORICAL RUNTIME VERIFICATION**.

## I. World-generation catalog

Registrations in `BMFeatures.java`:

- 15 custom Feature types: Mesmerite Boulder, Mesmerite Underground, Itching Ivy, Water Tree, Paydirt, Saguaro Cactus, Surface Fossil, three Huge Glowshrooms, Orange Glowshroom, Grass Patch, Peat, Reeds and Fissure.
- foliage placer: Willow
- decorators: Hanging Leaves, Willowing Branches, Ivy
- trunk placers: Ancient Oak, Swamp Cypress, Willow, Blighted Balsa

Data resources:

- 38 configured-feature JSON files
- 37 placed-feature JSON files
- Mushroom Fields: 15 configured/13 placed resources, with 11 direct injections
- Badlands: 4 configured/4 placed, all injected
- Swamp: 8 configured/9 placed, 7 direct injections after removal of vanilla swamp trees
- Dark Forest: 11 configured/12 placed, 7 direct injections; the three checked tree wrappers account for the placed-resource count, and the shipped but uninjected boulder/underground definitions remain dormant fissure components
- Four structures/four structure sets, three processor lists and five jigsaw template pools

Injection evidence: `init/BMFeatures.java`; JSON: `data/biomemakeover/worldgen/{configured_feature,placed_feature}/`; special algorithms: `level/feature/` and `level/feature/foliage/`.

No new terrain biome/noise settings are registered. Environmental ambience changes are particles/colors/sounds and content injection, not climate or terrain-noise biome creation.

## J. Structures catalog

1. Reworked Mansion
   - custom `biomemakeover:mansion` structure and piece type
   - structure-set spacing 32, separation 9
   - Dark Forest restriction
   - algorithmic `MansionLayout` plus extensive template catalog and directional-data markers
   - entrance/corridors/rooms/big rooms/gardens/outer walls/windows/towers/roofs/dungeon/boss room
   - Adjudicator boss, Mimics, Stone Golems, Owls and illagers can be placed by structure markers
   - mansion loot tiers: standard, good, junk, dungeon, dungeon-good, dungeon-junk, arrows
2. Ghost Town
   - vanilla jigsaw type, Badlands restriction, spacing 32/separation 12
   - centers/roads/buildings/decoration pools, processor lists and NBT templates
   - Ghost spawn override and archaeology/suspicious-red-sand content
3. Mushroom House
   - vanilla jigsaw type, Mushroom Fields restriction, spacing 12/separation 6
   - house pool, processor and NBT template; Mushroom Trader integration requires runtime verification
4. Sunken Ruin
   - custom structure type/piece, Swamp restriction, spacing 24/separation 9
   - cluster probability 0.8, large probability 0.6; nine NBT templates

`BeardifierMixin_Mansion` controls terrain adjustment per Mansion piece. `TemplateStructurePieceMixin_DirectionalData` interprets marker metadata. `BMStructures.java`, `level/feature/mansion/`, `level/generate/`, worldgen JSON and `data/biomemakeover/structures/` are authoritative.

## K. Recipes and processing

There are 374 recipe JSON files:

- 192 shaped crafting
- 38 shapeless crafting
- 123 stonecutting
- 9 smelting
- 4 smoking
- 4 campfire cooking
- 4 smithing-transform

Resource groups: 256 decoration, 72 wood, 12 dye, 12 cooking, 4 smithing, plus 18 root-level recipes. They cover every generated slab/stair/wall/wood/dye family, food cooking, Peat/dried Peat, bricks, glowshroom stew, crab chowder, equipment and cladding.

Non-recipe processors:

- Peat and Ectoplasm custom composters
- vanilla Composter integration and compostable data
- Soul Embers brewing-fuel data
- brewing mixes: Awkward + Wart or Scuttler Tail → Antidote; Awkward + Moth Scales → Nocturnal; Redstone extends Nocturnal
- Altar menu/block entity performs cursing/upgrading interactions
- Witch quest menus exchange data-driven quest items for rewards

No custom furnace block is registered.

## L. Loot and advancements

Loot resources total 329:

- 284 block tables
- 22 entity/supplemental entity tables
- 7 gameplay tables (Rootling colors and Scuttler eating)
- 7 Mansion tables
- 3 Ghost Town tables
- 3 archaeology tables

Runtime loot modification adds Illunite Shards to Evoker loot and an additional table to Pillager Outpost chests. Mixins add Pillager/leader and Witch Hat loot while avoiding duplicate/blocked loot paths.

Forty-eight authored advancements form one Biome Makeover progression tree. They cover entering all four themes, structures, discs, Glowshrooms/Tapestries, Peat compost/farmland, bottles, Glowfish rescue, cacti, Scuttler/Moth/Rootling items, Ancient Oak/Blighted Balsa, Altar/curses, Witch quests, Mansion/Adjudicator progression, Illunite, cladding/armor, archaeology and food. Seven custom simple triggers are registered: Ectoplasm Compost, Peat Compost, Arm Golem, Witch Trade, Glowfish Bucket Save, Poltergeist Yourself and Antidote.

## M. Sounds, particles and visual effects

- `BMEffects.java` registers 75 named sound events (four music records plus entity/block/system events) and `assets/biomemakeover/sounds.json` maps 137 audio resources.
- Major sound families: Ghost, Scuttler, Tumbleweed, Poltergeist, Lightning Bottle, Toad, Dragonfly, Decayed wet/dry states, Rootling, Moth, Illunite, Stone Golem, Adjudicator, Altar, Owl and Helmit Crab.
- Four particles: Lightning Spark, Poltergeist, Blossom and Teleport; Java providers in `level/particle/`, sprite JSON under `assets/biomemakeover/particles/`.
- Network effects drive lightning splash/entity, Poltergeist and general entity particles.
- Swamp color providers alter lily pads, Willow/Willowing Branches and Swamp Cypress; Ivy/Moth Blossom use custom tinting.
- Emissive/luminous visuals include Glowshrooms, Illunite, Lightning Bugs/Bottles and Owl eyes.

## N. Client systems

`BiomeMakeoverClient.java` registers:

- renderers for all 21 entity types
- model layers for all custom modeled entities, Tapestry and hat layers
- block-entity renderers for Tapestry, Altar and Lightning Bug Bottle
- Witch, Altar and Directional Data screens
- Horse Cowboy-Hat render layer
- block/item color providers

Special render systems include multipart Adjudicator/Stone Golem support, Helmit Crab shell/head rendering with loader-specific layer implementations, Decayed overlays, Lightning Bug inner/outer layers, Ghost translucency/effects, Moth/Owl flight animation, tapestries, emissive layers and client loop sounds.

Client mixins add level effects, custom block-entity item rendering and multipart entity render/packet handling.

## O. Configuration

`BMConfig.java` writes `config/biomemakeover.json`.

- `strictAltarCursing` toggle, default false
- per-curse configuration for ten curses: maximum level, minimum/maximum cost, treasure-only, discoverable and tradeable flags
- most worldgen/content tuning is intentionally data-pack driven rather than config-driven
- gamerule `BMdoTumbleweedSpawning`, default true, separately controls Tumbleweed spawning

## P. Networking, mixins and commands

### Networking

Nine Architectury messages:

- S2C: entity particle, lightning splash, lightning entity, Poltergeist particle, Witch quests, BM effect, entity event
- C2S: complete Witch quest, update Directional Data

Evidence: `init/BMNetwork.java`, `network/`.

### Mixins

The common mixin configuration enables 37 mixins spanning:

- Witch quests/antidote; Cowboy patrols and horse hats
- Mansion beard/structure directional data
- composters, Peat/Ectoplasm input and swamp bonemeal
- Dark Oak single-tree behavior; Water Lily behavior
- curse/enchantment behavior on Bow, Entity, ItemStack and LivingEntity
- Player/Camel/Tadpole behavior
- loot blocking/additional Pillager loot
- multipart collision/projectile/server/client packets/rendering
- Mushroom/Mycelium behavior
- Bell/Chiseled Bookshelf bug fixes

Forge adds Lily Pad plant-type and Hat-item hooks. The Fabric mixin file is present but empty because common mixins and Fabric entrypoints carry the behavior.

### Commands

One permission-level-2 command is registered:

```text
/pillager <pos> <leader>
```

It directly invokes patrol-member spawning and is marked by source TODO as developer placement. It can create Cowboy patrol members in Badlands through the same injected method.

## Q. Tags, data generation and data-driven systems

Biome Makeover defines 42 namespace tag files: 14 blocks, 10 items, 3 damage types, 2 enchantments, 2 entity types and 11 worldgen/biome tags. It also contributes 73 files to Minecraft tags (41 block, 27 item, 3 entity, 2 worldgen).

Significant tags:

- biome groups and structure eligibility
- Owl targets and Lightning Bug types
- Ghost/Scuttler/Tumbleweed damage immunities
- Scuttler food, Witch hats, Rootling buds
- Altar curse exclusions/non-upgradeable enchantments
- wood/log/plank/leaves/sapling, walls/slabs/stairs/fences/doors/signs, mineable/tool and compostable integrations

Additional data registries:

- 10 Witch quest categories
- 4 reward tables/types
- Mushroom Trader trade list
- compostables and brewing fuel
- decorated-pot patterns
- custom loot-pool entry type and suspicious-stew trade type

No in-repository data-generator source set/provider was found in the released 1.20 tree. The 2,000+ JSON/model/resource files are checked in. Some were evidently generated by tooling historically, but runtime consumes committed data.

## R. Compatibility and integrations

- Architectury provides shared Fabric/Forge registration and events.
- Taniwha supplies registry factories, boat types, data-driven helpers, item/block modifiers, criteria and other engine code. This external dependency materially affects exact factory behavior.
- Common/Forge optional biome tags allow other mods’ mushroom, swamp, badlands and beach biomes into makeovers.
- Forge-specific hooks cover Lily Pad plant type and Hat armor rendering.
- Fabric-specific multipart rendering exists; common mixins supply most loader-neutral behavior.
- Loot, block/item/entity tags and pottery hooks integrate with vanilla systems.
- No released REI/JEI module is present on `1.20`; REI support exists only on divergent `dev`.

## S. Dead, unused, unfinished and abandoned historical content

### Registered but normally unreachable or partially reachable

- Blight Bat: fully registered/rendered/attributed with loot/assets, but natural spawn and placement registration are commented out; hidden egg.
- Mushroom Trader: natural spawn line commented out. House marker/trade-list reachability is not conclusive without runtime structure inspection.
- Toad: natural Swamp spawn line commented out. Tadpole/Toad lifecycle exists, but initial survival-world acquisition is unclear.
- Tadpole: hidden bucket/egg; depends on Toad or mixin lifecycle.
- Adjudicator Mimic: registered but intentionally only summoned by boss phase.
- Directional Data block/menu: registered hidden development/structure tool, not ordinary obtainable gameplay.
- Owl `ATTACKING` state: persisted but no normal transition in `OwlEntity`.
- Blighted Balsa configured feature `blighted_balsa_trees` and underlying checked feature are reachable; `underground_vegetation` is configured but not directly injected by name and is likely nested data.

### Stale/resource-only

- `loot_tables/entities/giant_slime.json` and `item.biomemakeover.giant_slime_spawn_egg` localization have no registered Giant Slime entity.
- Extra/legacy models, textures and translations may remain from prior generator output; registration/resource cross-check, not asset presence, controls the catalog.
- Some Mansion template files are omitted from the active template arrays (for example numbered base variants visible on disk); these are resource-only unless selected by code/JSON.

### Source TODOs/incomplete paths

- `/pillager` command and several setup registrations carry “find somewhere else” TODO comments but execute.
- Witch `completeQuest` method on the mixin is empty; actual completion is handled through handler/network paths.
- Mushroom Trader/Blight Bat/Toad spawn code is explicitly commented.
- External Taniwha behavior prevents a fully local proof of every generated block-family registration and modifier.

### Abandoned `dev` content

Dust Devils, grinding, reinforcement, caravans, succulents/Aloe, big flower pots, sandstone/gilded-lapis sets, Scarab Elytra and REI integration are substantial but unreleased divergent work. They must not be merged into the 1.11.4 baseline without a later explicit design decision.

## T. Feature dependency map

```text
Biome tag
  -> BMFeatures biome modification
    -> placed-feature key
      -> placed JSON
        -> configured JSON
          -> vanilla/custom Feature codec
            -> registered blocks + placement algorithm

Natural mob
  -> BMEntities entity registration
    -> attributes
    -> biome spawn entry
    -> SpawnPlacements predicate
    -> AI/navigation/entity state
    -> renderer/model/layers
    -> sounds + particles/network
    -> entity loot + tags

Structure
  -> biome has_structure tag
    -> structure JSON
      -> structure set placement
      -> custom StructureType or vanilla jigsaw
      -> piece/pool/processor
      -> NBT templates + markers
      -> mobs/block entities/loot

Functional block
  -> BMBlocks registration
    -> block behavior
    -> BMBlockEntities type/ticker
    -> BMScreens menu + client screen
    -> BMNetwork packets when required
    -> blockstate/model/texture
    -> recipe + loot + tags + advancement

Witch quest
  -> Witch mixin + Witch Hat gate
    -> quest-category reload listener/data
    -> Witch menu/screen
    -> S2C quest sync + C2S completion
    -> reward table/type data
    -> trade advancement/loot

Altar/curses
  -> Altar block/entity/menu/screen
    -> config strictness
    -> curse registry/config
    -> enchantment behavior mixins
    -> sound/network/effects

Mansion boss
  -> Mansion layout/template marker
    -> Adjudicator room listener
    -> multipart entity/mixins
    -> phase selector
      -> melee/bow/fangs/teleport/summon/mimic/golem
    -> boss loot/advancement
```

Suggested later restoration dependency order, without performing restoration: registries/tags → blocks/items/effects → data features → biome injection → entities → structures/block entities → networking/client → loot/recipes/advancements → compatibility/runtime validation.

## U. Areas needing historical runtime verification

1. Exact vanilla inherited Owl shoulder-transfer behavior and shoulder rendering on both loaders.
2. Mushroom Trader survival reachability from Mushroom House markers despite commented natural spawn.
3. Toad/Tadpole initial acquisition and complete metamorphosis flow with `TadpoleMixin`.
4. Blight Bat reachability through any Mansion/template marker not obvious from Java registration.
5. Mansion room-marker selection, omitted template files and actual boss/dungeon frequency.
6. Exact Taniwha-generated membership/count for wood/decorative families, modifiers and boats.
7. Data-pack loading of every optional common/Forge biome tag on Fabric versus Forge.
8. Exact Poltergeist redstone/action set and Ectoplasm/Peat composter probabilities.
9. Helmit Crab shell-selection priority and loader-specific armor/head rendering edge cases.
10. Unconditional Owl eye layer appearance under invisibility, baby and shoulder states.
11. Whether any stale generated resources are accepted or warned about at runtime.
12. Whether the final archived 1.20 branch builds today with its historical external dependencies; no historical build was required or attempted during this audit.

## V. Exact source-reference index and validation

### Registries and initialization inspected

- `BiomeMakeover.java`, `BiomeMakeoverClient.java`, `BMConfig.java`
- `init/BMBlocks.java`, `BMBlockEntities.java`, `BMItems.java`, `BMEntities.java`
- `BMFeatures.java`, `BMStructures.java`, `BMBoats.java`
- `BMEffects.java`, `BMPotions.java`, `BMEnchantments.java`, `BMAdvancements.java`
- `BMNetwork.java`, `BMScreens.java`, `BMTab.java`

### Source families inspected

- all files under `block/`, including five block entities and three renderers
- all registered entity classes, `entity/adjudicator/`, AI and renderer/model registration
- `level/feature/`, foliage/decorator/trunk classes, Mansion and structure generation
- `crafting/`, Witch quest data/menu classes, `item/`, `mobeffect/`, `network/`
- all enabled common mixins plus Fabric/Forge mixin configurations

### Resources inspected/inventoried

- 91 worldgen resources and all biome/structure tags
- four structure definitions/sets, pools/processors and 228 NBT templates
- 374 recipes, 329 loot tables, 48 advancements and 42 mod-namespace tags
- quest/reward/trade/compost/brewing data
- blockstates, models, particles, sounds, textures and language catalog

### Validation result

- Every explicit registry category initialized by `BiomeMakeover.init()` was inspected.
- Java and resources were both audited.
- All four primary biome themes, Beach ecology and Mansion/illager theme were inspected separately.
- All 21 registered entity types and all five block-entity types were traced.
- Worldgen injection paths and all four structure placement paths were traced.
- Commented-out spawns, stale data and divergent development systems are separated from reachable released gameplay.
- Owl conclusions are supported directly by primary source, tags, renderer/model, loot and sound data.

Portions not fully inspectable from this repository alone: implementation details inside the external Taniwha 1.20.0-5.4.4 dependency and runtime-only outcomes listed in section U. No original repository path was intentionally excluded; binary NBT templates were inventoried and traced by filename/selection path but were not decoded block-by-block.

This audit stops before any comparison to the current 1.21.10 port.
