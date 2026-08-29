# Stage 10A - Mushroom House, Mushroom Trader, and Button Mushrooms Restoration

Status: **COMPLETE / RUNTIME ACCEPTED**

Implementation date: 2026-08-29

Authority: final Biome Makeover 1.20.1-1.11.4 source at `2f314c0596af095a4890995a465f308f69476b4a`

Starting port checkpoint: `c732d87d62d3311313e7480a1f6c3a8856e53e92`

Stage 10B and later content remain unstarted. No Mythas gameplay enhancement was added.

## Restored final-release chain

```text
#c:is_mushroom
  -> #biomemakeover:mushroom_fields
  -> #biomemakeover:has_structure/mushroom_house
  -> biomemakeover:mushroom_houses structure set
  -> biomemakeover:mushroom_house jigsaw structure
  -> biomemakeover:mushroom_house/house pool
  -> biomemakeover:mushroom_house/house/house_1 template
     -> one biomemakeover:mushroom_house loot chest
     -> one persistent biomemakeover:mushroom_trader
```

The port uses native 1.21.10 jigsaw/structure-set data. No custom structure type and no Fabric biome-injection hook are required. The random-spread placement is linear, spacing 12, separation 6, salt 6942069. The jigsaw uses `WORLD_SURFACE_WG`, `surface_structures`, `beard_thin`, size 3, and maximum center distance 80.

The exact final binary `house_1` template is packaged unchanged under the modern `data/biomemakeover/structure/` directory: 11 x 10 x 11, four flower pots, one loot chest, and one embedded Mushroom Trader. It contains no jigsaw block or data marker. Newly generated eligible chunks can receive the structure; already-generated chunks are not rewritten.

## Local weighted flower-pot processor

The final resource referenced `taniwha:replace_selection`. Stage 10A replaces that external dependency with the narrow local codec `biomemakeover:replace_selection`. It matches only `minecraft:flower_pot`, keeps the current block position/NBT, and independently samples one weighted state through the structure placement RNG:

| Output | Weight |
|---|---:|
| `biomemakeover:potted_mycelium_roots` | 2 |
| `minecraft:potted_red_mushroom` | 3 |
| `minecraft:potted_brown_mushroom` | 3 |
| `biomemakeover:potted_wild_mushrooms` | 2 |
| `biomemakeover:potted_green_glowshroom` | 2 |
| `biomemakeover:potted_purple_glowshroom` | 2 |
| `biomemakeover:potted_orange_glowshroom` | 2 |

Total weight is 16. No Taniwha class, registry type, resource ID, or runtime dependency is packaged.

## House loot

`biomemakeover:mushroom_house` restores three final pools:

- Eight equal-weight rolls of red or brown mushroom.
- One guaranteed `biomemakeover:button_mushrooms_music_disk`.
- Five to nine equal-weight rolls among Glowshroom Stew, Glowfish (1-3), Cooked Glowfish (1-3), the three Glowshrooms, both tall mushrooms, Blighted Balsa Sapling, and vanilla Mushroom Stew.

The single chest therefore provides exactly one guaranteed disc opportunity per house.

## Mushroom Trader

`biomemakeover:mushroom_trader` is a persistent `AbstractVillager` merchant. It uses the final 0.6 x 1.95 dimensions, ambient classification, vanilla wandering-trader sounds, door-aware navigation, trade/look/wander goals, and avoidance of the final hostile set. It never despawns by distance. Its empty loot table is restored. Its commented-out natural Mushroom Fields spawn is not registered.

The two original textures are layers, not variants: `mushrooming_trader_inner.png` is the base skin and `mushrooming_trader_outer.png` is the outer robe. The modern renderer uses the native Villager model, source 0.9375 scale, custom-head layer, and crossed-arms held-item layer.

The final visible spawn egg is restored. Creative/spawn-egg traders initialize five randomly selected common offers without replacement, one randomly selected suspicious-stew listing, and the rare disc offer.

### Common offer pool

All common offers have 12 maximum uses, 1 merchant XP, and price multiplier 0.05:

| Emeralds | Result |
|---:|---|
| 1 | 3 Brown Mushrooms |
| 1 | 3 Red Mushrooms |
| 2 | 3 Purple Glowshrooms |
| 2 | 3 Orange Glowshrooms |
| 2 | 3 Green Glowshrooms |
| 2 | 2 Green Glowshroom Blocks |
| 2 | 2 Purple Glowshroom Blocks |
| 2 | 2 Orange Glowshroom Blocks |
| 1 | 2 Tall Brown Mushrooms |
| 1 | 2 Tall Red Mushrooms |
| 2 | 2 Glowshroom Stems |
| 1 | 2 vanilla Mushroom Stems |
| 1 | 2 Red Mushroom Blocks |
| 1 | 2 Brown Mushroom Blocks |
| 1 | 3 Glow Lichen |
| 1 | 4 Glowfish |
| 2 | 4 Cooked Glowfish |
| 3 | 1 Glowfish Bucket |
| 1 | 3 Blighted Balsa Saplings |
| 1 | 5 Mycelium Sprouts |
| 1 | 5 Mycelium Roots |
| 1 | 3 Wild Mushrooms |
| 2 | 2 Glowshroom Stems (source-confirmed duplicate weighting entry) |

The stew listing is selected between the exact final 2-emerald and 4-emerald effect pools, then selects one effect. Each has four maximum uses, 1 merchant XP, and price multiplier 0.05. The rare listing is 8 Emeralds for one Button Mushrooms disc, four maximum uses, 1 merchant XP, and price multiplier 0.05.

The original template's embedded entity and persisted offers remain in its unmodified NBT. Those six offers are loaded as saved merchant state and are not supplemented because trade generation runs only for an uninitialized offer list. The embedded disc offer is 8 Emeralds for one disc with four maximum uses. Template placement supplies transformed position/orientation and normal structure entity UUID handling; persistence prevents removal and save/reload preserves the offers.

## Button Mushrooms

The historical registry spelling is preserved: `biomemakeover:button_mushrooms_music_disk`. It is a rare, single-stack item linked through the native 1.21.10 `JUKEBOX_PLAYABLE` component to `biomemakeover:button_mushrooms` jukebox-song data.

The song uses `biomemakeover:button_mushrooms`, 115 seconds, comparator signal 14, and `Podington Bear - Button Mushrooms`. `sounds.json` streams the non-looping original `sounds/button_mushrooms.ogg`. The original 244,688-byte mono 44.1 kHz Vorbis asset is unchanged; SHA-256 is `B4A3770107E7163F7A57A71F6D92E259C3E96BFA248447E49E0B3A66290D8D03`.

No custom jukebox mixin and no creeper-drop music-disc tag are used.

The visible `biomemakeover:biomemakeover/mushroom_disc` advancement restores the final parent, icon, title `Badger Badger Badger`, description, task frame, toast/chat visibility, and `minecraft:inventory_changed` criterion for obtaining the disc.

## Static validation contract

The Stage 10A foundation validator checks the registrations, exact structure parameters and tag chain, exact binary template hash/dimensions/entity/chest contract, modern singular template path, seven processor outputs and weight 16, no Taniwha runtime reference, all three loot pools, merchant class/trades/persistence/no natural spawn, client-only renderer/assets, native jukebox song/component, original audio hash, advancement, and packaged-JAR reachability. The existing Stage 9B.1 and Stage 9B.2 contracts remain enforced by the same parity validator.

The dedicated `validateStage10ATemplateRuntime` task additionally uses the actual 1.21.10 `FileToIdConverter`, server-data resource manager, compressed-NBT reader, Structure data fixer, registered block lookup, and `StructureTemplate.load`. It proves the template resolves from `biomemakeover:structure/mushroom_house/house/house_1.nbt`, data-fixes from DataVersion 3337, decodes to 11 x 10 x 11 with 1,098 blocks, one chest, four pot targets, zero jigsaw connectors, one Mushroom Trader, six persisted offers, and the persisted disc offer. This improves resource/runtime-codec coverage without claiming to replace natural-worldgen testing.

## Runtime remediation 1 - missing generated house

The first Prism candidate successfully booted and `/locate structure biomemakeover:mushroom_house` found a Mushroom Fields candidate, but the destination contained no house. The registry, structure set, biome tag, and start pool were therefore reachable.

The exact failure was the Minecraft 1.21 data-directory rename. Final 1.20.1 stores binary templates below `data/<namespace>/structures/` (plural), while the actual 1.21.10 `StructureTemplateManager` initializes `FileToIdConverter("structure", ".nbt")` (singular). The initial port copied the binary unchanged into the legacy plural directory. Consequently `SinglePoolElement` asked the manager for `biomemakeover:mushroom_house/house/house_1`, the manager found no resource, and `getOrCreate` supplied a new empty `StructureTemplate`.

Actual 1.21.10 `JigsawPlacement` constructs the selected `PoolElementStructurePiece` and adds that root piece before connector traversal. A root template with no jigsaw blocks is therefore still supported. The failed candidate had a structure start/root-piece path, but that piece referenced the empty-template fallback and placed no blocks or entities. Its processor, embedded trader NBT, chest NBT, and block palette were never reached. Start-height projection remained the source-correct `absolute: 0` plus `WORLD_SURFACE_WG` calculation and was not implicated.

The remediation moves only the unchanged 5,277-byte binary from legacy `data/biomemakeover/structures/mushroom_house/house/house_1.nbt` to modern `data/biomemakeover/structure/mushroom_house/house/house_1.nbt`. The SHA-256 remains `8CDD1C997FCE691FE7D57FA1EAC863E4A00042EEAAC688B21F2CB49D5A639567`; no NBT field, palette state, block entity, embedded entity, position, offer, or layout changed.

## Prism runtime acceptance

The remediated candidate (`B39E1A5E19A632E36D16DE74ECAB718299A158E3166868ABB76D4FA1F58C51A4`) was accepted in Prism Launcher on Minecraft 1.21.10. `/locate structure biomemakeover:mushroom_house` resolved a real generated surface house rather than the previous empty logical start. The complete house, exactly one embedded Mushroom Trader, trader interaction, mushroom and mushroom-block trades, the Button Mushrooms trade, guaranteed chest disc, coherent secondary chest loot, and visibly processed potted-mushroom outcomes all passed. The pot result proves the local `biomemakeover:replace_selection` processor executes during natural structure placement.

A separately spawned Mushroom Trader also passed rendering, merchant behavior, generated-offer, and general-functionality checks. The Button Mushrooms disc played successfully in a jukebox, stopped normally, and did not loop.

Comparator signal 14 and the `Badger Badger Badger` advancement toast were not separately observed during the manual pass. Their native jukebox-song and advancement definitions remain covered by the Stage 10A static/package contract; inspection found no discrepancy, so neither is held open as a runtime blocker.

Final implementation and runtime-acceptance closure validation passed the clean offline build, integrated parity/Stage 10A/Stage 9B contracts, JSON/resource/reference/package checks, loot/advancement/tag/model/texture/sound audits, sidedness checks, changed-path Stage 10B+ leakage scan, Sliding Curse scan, Taniwha runtime-reference scan, and `git diff --check`. Gradle reports `compileTestJava`, `processTestResources`, `testClasses`, and `test` as `NO-SOURCE`/up-to-date because this repository has no Java test source set.

Final registry totals are 262 blocks, 275 items, 13 entities, 2 block entities, 42 sounds, 2 particles, and 1 local structure processor. Relative to the accepted Stage 9B.2 checkpoint, Stage 10A adds two items (disc and spawn egg), one entity, one sound, and one structure processor; it removes or renames no registry entry.

The candidate JAR packages 279 blockstates, 601 block models, 275 item definitions, 277 item models, 296 PNG textures, 77 OGG assets, 330 recipes, 274 loot tables, 35 advancements, and 86 tag files. Stage 10A's direct resource deltas are two item definitions, two item models, four PNG textures, one OGG, two loot tables, one advancement, two biome tags, one structure, one structure set, one template pool, one processor list, one NBT template, and one jukebox-song definition. Recipe count is unchanged.

The offline dedicated-server launch was attempted. Loom reached `runServer` but could not resolve the already-known uncached `net.fabricmc:fabric-log4j-util:1.0.2` artifact in offline mode, before Minecraft bootstrap. Dependencies were not changed to mask that environment limitation.

Stage 10A is complete and runtime accepted. Stage 10B and later stages remain unstarted.
