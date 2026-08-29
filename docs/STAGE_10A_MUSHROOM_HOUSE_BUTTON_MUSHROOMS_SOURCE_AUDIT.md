# Stage 10A - Mushroom House and Button Mushrooms Source Audit

Audit status: **COMPLETE**

Implementation status: **IMPLEMENTED / AWAITING RUNTIME VALIDATION**

Audit date: 2026-08-29

Authority: final Biome Makeover 1.20.1-1.11.4 source at `2f314c0596af095a4890995a465f308f69476b4a`

Port checkpoint: `f13cce0086a9af220e3e3a346608ff2daac7fea1`

This document remains the authoritative Stage 10A source contract. The corresponding 1.21.10 implementation is recorded in `STAGE_10A_MUSHROOM_HOUSE_BUTTON_MUSHROOMS_RESTORATION.md`; final released 1.20.1 source remains authoritative over showcase material and earlier revisions.

## Executive disposition

Stage 10A is one coherent final-release chain:

```text
#c mushroom biome
  -> biomemakeover:mushroom_house structure set
  -> biomemakeover:mushroom_house jigsaw structure
  -> house_1 template
     -> one Mushroom House chest
        -> guaranteed Button Mushrooms disc
     -> one embedded biomemakeover:mushroom_trader
        -> persisted disc trade and merchant behavior
```

The Mushroom House and Button Mushrooms disc are **ACTIVE FINAL**. The binary template establishes an additional hard dependency that earlier text-only audits did not prove: `house_1.nbt` directly embeds a `biomemakeover:mushroom_trader`. Its natural biome spawn line is commented out, but its house-based survival reachability is active. Stage 10A implementation must therefore restore the house-owned Mushroom Trader rather than omit it as dormant content.

## Canonical IDs and architecture

| Role | Final ID | Final architecture |
|---|---|---|
| Structure | `biomemakeover:mushroom_house` | Vanilla `minecraft:jigsaw`; no custom BM StructureType or structure class |
| Structure set | `biomemakeover:mushroom_houses` | Vanilla random-spread placement |
| Start pool | `biomemakeover:mushroom_house/house` | One rigid single-pool element |
| Template | `biomemakeover:mushroom_house/house/house_1` | Compressed structure NBT |
| Processor list | `biomemakeover:mushroom_house` | Taniwha weighted flower-pot replacement in final 1.20.1 |
| Eligibility tag | `biomemakeover:has_structure/mushroom_house` | Contains `#biomemakeover:mushroom_fields` |
| BM mushroom-biome tag | `biomemakeover:mushroom_fields` | Optional `#c:mushroom` and `#forge:is_mushroom` in final data |
| House loot | `biomemakeover:mushroom_house` | One direct chest loot table |
| Embedded entity | `biomemakeover:mushroom_trader` | Merchant entity saved directly in the template |
| Disc item | `biomemakeover:button_mushrooms_music_disk` | Rare, stack size 1, final RecordItem/ArchitecturyRecordItem |
| Disc sound | `biomemakeover:button_mushrooms` | Streamed OGG sound event |
| Disc advancement | `biomemakeover:biomemakeover/mushroom_disc` | Inventory-changed goal advancement |

There is no Mushroom House block, block item, recipe, translation, menu, custom structure packet, or configuration toggle. `/locate structure biomemakeover:mushroom_house` is the intended registry-level locate path.

## Complete generation contract

Final `worldgen/structure/mushroom_house.json` is a vanilla jigsaw definition with these exact properties:

- `type`: `minecraft:jigsaw`
- `step`: `surface_structures`
- `biomes`: `#biomemakeover:has_structure/mushroom_house`
- `start_pool`: `biomemakeover:mushroom_house/house`
- `size`: 3
- `start_height`: absolute 0
- `project_start_to_heightmap`: `WORLD_SURFACE_WG`
- `max_distance_from_center`: 80
- `terrain_adaptation`: `beard_thin`
- `use_expansion_hack`: false
- empty `spawn_overrides`

The structure adds no custom terrain predicate, height calculation, water test, spawn override, post-generation callback, or biome-modification hook. Vanilla jigsaw placement projects the start onto `WORLD_SURFACE_WG`; `beard_thin` supplies terrain adaptation. The rigid template contains air and ordinary block states, and there is no BM-specific water/air processor.

Final `worldgen/structure_set/mushroom_houses.json` contains only this structure at weight 1:

- placement type: `minecraft:random_spread`
- spacing: 12 chunks
- separation: 6 chunks
- spread type: `linear`
- salt: `6942069`
- no exclusion zone
- no explicit frequency or reduction method; vanilla defaults apply (full frequency/default reduction)

There is no source config entry that disables the house or changes its spacing.

### Biome eligibility

The complete final path is:

```text
structure biomes
  #biomemakeover:has_structure/mushroom_house
    -> #biomemakeover:mushroom_fields
       -> optional #c:mushroom
       -> optional #forge:is_mushroom
```

On the final Fabric release the convention mushroom tag is the intended route to vanilla Mushroom Fields. Final BM does not directly name `minecraft:mushroom_fields` in the structure tag. It targets convention-tagged mushroom biomes, allowing other compatible mushroom biomes as well. There is no separate Mushroom Field Shore biome or shore tag in the final 1.20.1 chain.

For 1.21.10 Fabric, Fabric Convention Tags v2 supplies `#c:is_mushroom`, whose installed definition contains `minecraft:mushroom_fields`. The modern translation should retain BM's two-level tag architecture but change the obsolete final `#c:mushroom` reference to optional `#c:is_mushroom`. A direct vanilla fallback is unnecessary while the required Fabric API supplies that tag.

## Pool and template inventory

There is exactly one Mushroom House template and no foundation, path, exterior, basement, cellar, interior, connector, or decoration-piece variants.

| Template | Dimensions | Pool weight | Purpose |
|---|---:|---:|---|
| `biomemakeover:mushroom_house/house/house_1` | 11 x 10 x 11 | 15 | Complete standalone house, interior, chest, campfire, bed, flower pot, and trader |

The pool fallback is `minecraft:village/plains/terminators`, the element is a rigid `minecraft:single_pool_element`, and its processor list is `biomemakeover:mushroom_house`. Because it is the only pool element, weight 15 still gives it 100% selection. Binary inspection found no jigsaw connector blocks, so the configured jigsaw depth of 3 does not add house pieces and the terminator fallback has no observable connector work. Vanilla jigsaw placement applies rotation; there is no authored mirror mode or custom bounding-box code.

The checked-in NBT is 5,277 bytes, DataVersion 3337, with SHA-256 `8CDD1C997FCE691FE7D57FA1EAC863E4A00042EEAAC688B21F2CB49D5A639567`.

### Template block and content contract

The template palette has 52 states. Significant content is:

- BM structure palette: `blighted_cobblestone`, brown mushroom brick/stairs/slabs, and Blighted Balsa door/stairs/slab/trapdoor.
- Vanilla palette: cobblestone, brown mushroom blocks, bookshelves, iron bars, composter, red/white carpet, flower pot, campfire, brown bed, chest, and air.
- One east-facing chest at relative position `[3,2,2]`, directly carrying loot table `biomemakeover:mushroom_house` and an authored loot seed.
- One empty lit campfire at `[8,1,2]`.
- One brown bed (head/foot at `[2,2,3]` and `[3,2,3]`).
- Four ordinary flower-pot states, each independently selected by the processor below.
- One embedded `biomemakeover:mushroom_trader` centered at relative entity position `[5.5,2,5.5]`.

There are no structure `DATA` markers, loot markers, entity markers, jigsaw connectors, or custom marker callbacks. The chest and trader are stored directly in the NBT. No additional entities are present.

### Flower-pot processor

Final 1.20.1 uses `taniwha:replace_selection` to replace `minecraft:flower_pot` from a weighted state provider. Duplicate entries for Mycelium Roots aggregate as follows:

| Output state | Effective weight | Probability |
|---|---:|---:|
| `biomemakeover:potted_mycelium_roots` | 2 | 12.5% |
| `minecraft:potted_red_mushroom` | 3 | 18.75% |
| `minecraft:potted_brown_mushroom` | 3 | 18.75% |
| `biomemakeover:potted_wild_mushrooms` | 2 | 12.5% |
| `biomemakeover:potted_green_glowshroom` | 2 | 12.5% |
| `biomemakeover:potted_purple_glowshroom` | 2 | 12.5% |
| `biomemakeover:potted_orange_glowshroom` | 2 | 12.5% |

Every output block is already restored. Vanilla rule processors cannot directly emit a weighted state provider. Stage 10A should register one small local BM processor codec matching the final `target + BlockStateProvider` behavior and migrate the JSON away from the foreign Taniwha processor ID. It must not add Taniwha as a runtime dependency. An approximate chain of independent random rule processors is not preferred because it changes the released weighted-selection contract and seeded random consumption.

## Mushroom Trader: house-owned dependency

The trader is not an altered Wandering Trader. Final source registers `biomemakeover:mushroom_trader` as an ambient-category `AbstractVillager` with dimensions 0.6 x 1.95 and tracking range 12, using vanilla mob attributes. It is persistent (`removeWhenFarAway` returns false), does not breed, rewards 3-6 experience for XP-granting trades, uses Wandering Trader sounds, and has these goals:

- float and trade/look-at-trading-player;
- open doors;
- avoid Zombies, Evokers, Vindicators, Vexes, Pillagers, Illusioners, and Zoglins;
- panic;
- water-avoiding random stroll;
- move toward target and look at mobs.

The renderer uses the vanilla Villager model/layer at 0.9375 scale, an inner texture plus an outer overlay, custom-head and crossed-arms held-item layers. Final assets are `mushrooming_trader_inner.png` and `mushrooming_trader_outer.png`. The entity loot table is intentionally empty. The visible spawn egg `biomemakeover:mushroom_trader_spawn_egg` uses colors `0xb3a48b`/`0xb3a48b` and appears in the BM creative tab.

The final biome spawn line is commented out. That disables free natural population spawning, not the entity itself: direct NBT inspection proves every generated house embeds one trader. Structure-template entity loading does not use the commented biome spawn registration.

The saved template entity already contains merchant Offers, including an 8-Emerald -> one Button Mushrooms disc trade with max uses 4. Other persisted offers in this template sell red mushroom, purple glowshroom, tall red mushroom, red mushroom block, and brown mushroom block goods. New/offerless traders use `trade_lists/mushroom_trader.json`: five random common offers, one random suspicious-stew offer, and the single rare Button Mushrooms offer. The common list contains vanilla/final Stage 3 mushroom, glowshroom, Glowfish, Blighted Balsa, roots, sprouts, and Wild Mushroom goods; the stew tier includes vanilla effects and restored Nocturnal; the rare tier is the disc at 8 Emeralds, max uses 4.

Implementation should restore the complete entity, spawn egg, renderer, empty loot, and trade behavior locally. Final Taniwha `TradeLists` and `taniwha:standard` serializers must not become a runtime dependency. The exact final tables can be represented by a narrow BM data loader or source-equivalent server-side offer factory. The embedded persisted offers remain authoritative for the shipped house template.

## Mushroom House loot contract

One house has exactly one loot chest and therefore exactly one chest opportunity.

`biomemakeover:mushroom_house` has three unconditional pools and random sequence `biomemakeover:mushroom_house`:

1. Eight rolls, equal-weight one-count entries: red mushroom or brown mushroom. The chest gets exactly eight mushrooms total, with a random color mix.
2. One roll, one entry: exactly one `biomemakeover:button_mushrooms_music_disk`. This is a **100% guaranteed** disc, not a weighted rare chance.
3. Uniformly 5-9 rolls over ten equal-weight entries:
   - Glowshroom Stew x1;
   - Glowfish x1-3;
   - Cooked Glowfish x1-3;
   - Green, Purple, or Orange Glowshroom x1;
   - Tall Brown or Tall Red Mushroom x1;
   - Blighted Balsa Sapling x1;
   - vanilla Mushroom Stew x1.

There is no luck, Looting, player-kill, biome, tool, or random-chance condition. All nondisc entries are already registered and functional from accepted Stage 3.

Button Mushrooms therefore has two active final acquisition paths:

- guaranteed one per generated Mushroom House chest; and
- the house's embedded Mushroom Trader, 8 Emeralds for one disc, up to four uses.

## Button Mushrooms disc contract

| Property | Final value |
|---|---|
| Item ID | `biomemakeover:button_mushrooms_music_disk` (`disk` is the canonical spelling) |
| Item translation | `item.biomemakeover.button_mushrooms_music_disk` = `Music Disc` |
| Description/credit | `item.biomemakeover.button_mushrooms_music_disk.desc` = `Podington Bear - Button Mushrooms` |
| Item class | `ArchitecturyRecordItem` / vanilla RecordItem semantics |
| Rarity | Rare |
| Stack size | 1 |
| Comparator output | 14 |
| Configured playback length | 115 seconds |
| Sound event | `biomemakeover:button_mushrooms` |
| Audio | `assets/biomemakeover/sounds/button_mushrooms.ogg` |
| Sound definition | one `stream: true` sound, with no loop, volume, pitch, attenuation, or subtitle override |
| Creative placement | visible in the BM creative tab through ordinary `registerItem` |
| Final tag | `#minecraft:music_discs` in 1.20.1 |
| Advancement | `biomemakeover:biomemakeover/mushroom_disc` |

The original OGG is present: 244,688 bytes, Vorbis mono at 44,100 Hz, actual stream duration approximately 115.636372 seconds, SHA-256 `B4A3770107E7163F7A57A71F6D92E259C3E96BFA248447E49E0B3A66290D8D03`. Final gameplay metadata is still 115 seconds and should remain the modern jukebox-song length. It is streamed and does not loop; ordinary jukebox playback stops at the configured song end. The jukebox supplies the records sound category. Final source defines no subtitle or special playback callback.

The item model is a generated model using `biomemakeover:item/music_disk_button_mushrooms`. The original texture is present at `textures/item/music_disk_button_mushrooms.png` (SHA-256 `8BCE360CE026A2481501911CB99BB22CB86AE3EFA6855D8FB08A3417A12EFDAE`). No replacement, remix, or redrawn asset is necessary.

The final advancement is a visible goal titled `Badger Badger Badger`, parented to `biomemakeover:biomemakeover/enter_mushroom_fields`. Criterion `get_disc` uses `minecraft:inventory_changed` for the disc. Toast and chat announcement are enabled; hidden is false; there is no reward.

### Direct final resource/source inventory

The owning final files are:

- Final 1.20.1 worldgen/data: `worldgen/structure/mushroom_house.json`, `worldgen/structure_set/mushroom_houses.json`, `worldgen/template_pool/mushroom_house/house.json`, `worldgen/processor_list/mushroom_house.json`, legacy `structures/mushroom_house/house/house_1.nbt`, `tags/worldgen/biome/has_structure/mushroom_house.json`, and `tags/worldgen/biome/mushroom_fields.json`. The audited 1.21.10 translation requirement below changes only the template resource directory to singular `structure/`.
- Loot/progression/trades: `loot_tables/mushroom_house.json`, empty `loot_tables/entities/mushroom_trader.json`, `trade_lists/mushroom_trader.json`, and `advancements/biomemakeover/mushroom_disc.json`.
- Disc assets: `models/item/button_mushrooms_music_disk.json`, `textures/item/music_disk_button_mushrooms.png`, `sounds/button_mushrooms.ogg`, plus shared `sounds.json` and language files.
- Trader assets: `models/item/mushroom_trader_spawn_egg.json`, `textures/entity/mushrooming_trader_inner.png`, and `textures/entity/mushrooming_trader_outer.png`, plus shared language files.
- Shared final tag: `data/minecraft/tags/items/music_discs.json`, whose Stage 10A member is the Button Mushrooms item.
- Java ownership: `MushroomVillagerEntity`, `MushroomTraderRender`, registration/attribute hooks in `BMEntities`, item/spawn-egg/suspicious-stew trade registration in `BMItems`, sound registration in `BMEffects`, and renderer registration in `BiomeMakeoverClient`.

No Mushroom House recipe, structure translation, custom structure class, custom structure piece, dedicated client structure asset, or house-finding advancement exists.

### 1.21.10 jukebox translation

Minecraft 1.21.10 replaces class-held record metadata with the dynamic `jukebox_song` registry and the `JUKEBOX_PLAYABLE` item component. The narrow native plan is:

1. Register sound event `biomemakeover:button_mushrooms` and retain the exact streamed sounds.json entry and OGG.
2. Add `data/biomemakeover/jukebox_song/button_mushrooms.json` with that sound event, description component using the existing `.desc` translation, length `115.0`, and comparator output 14.
3. Register a normal rare, stack-size-one item with `Item.Properties.jukeboxPlayable()` pointing to canonical modern song key `biomemakeover:button_mushrooms`.
4. Restore the legacy generated model, add the required 1.21.10 `assets/biomemakeover/items/button_mushrooms_music_disk.json` item definition, texture, and translations.

There is no general `minecraft:music_discs` ItemTag in the inspected 1.21.10 common data/API; the `JUKEBOX_PLAYABLE` component is the native classification. Do not add the disc to `minecraft:creeper_drop_music_discs`, because final BM provides no Creeper-drop acquisition. The old general tag should be treated as an obsolete schema concept, not redirected to an unrelated modern tag.

## Current 1.21.10 port state

### Present and functional dependencies

- Every BM block used by the template palette and randomized flower pots.
- All nondisc chest-loot items: glowshrooms, Glowfish foods, Glowshroom Stew, tall mushrooms, and Blighted Balsa Sapling.
- The parent Mushroom Fields advancement and accepted Stage 3 biome/worldgen ecosystem.
- Nocturnal, needed by one offerless-trader suspicious-stew definition.
- Fabric Convention Tags v2 `#c:is_mushroom` containing vanilla Mushroom Fields.

### Missing production content

- Structure, structure set, start pool, processor list, biome eligibility tags, and NBT template.
- Local replacement for `taniwha:replace_selection`.
- Mushroom House loot table.
- Mushroom Trader entity, attributes, spawn egg, renderer, two textures, empty entity loot, trade definitions/loader, and translations.
- Button Mushrooms item, modern jukebox-song data/component, sound event, sounds.json entry, OGG, model, item definition, texture, translations, and disc advancement.

### Residue classification

Production Java, source resources, and the accepted packaged JAR contain none of the Stage 10A registrations/resources above: they are **MISSING**, not partial or dormant. Current references occur only in docs and validation inventories. There is no conflicting registry ID, placeholder, experimental implementation, obsolete structure code, or packaged Stage 10A leakage.

The prior Stage 3 validation contract and showcase audit incorrectly classified Mushroom Trader as unreachable because they had not decoded the NBT entity list. This audit corrects it to **deferred, structure-owned ACTIVE FINAL**. Blightbat remains the only excluded unreachable Mushroom Fields entity.

## Modern structure translation plan

Most of the final feature remains natively data driven in 1.21.10:

- Copy/adapt the jigsaw structure, random-spread structure set, pool, processor list, and NBT template under modern singular data directories.
- Retain vanilla `minecraft:jigsaw`; do not register a BM StructureType or create custom generation code.
- Use `#c:is_mushroom` through the BM eligibility tags; no Fabric biome modification is needed.
- Register only the small local weighted replacement processor required to replace the final Taniwha codec exactly.
- Let native template/jigsaw placement handle rotation, terrain adaptation, bounding boxes, entities, and direct chest NBT.
- Restore Mushroom Trader before enabling structure decoding/generation so the embedded entity ID and offers are valid.

The principal migration risks are not structure placement itself; they are the 1.20.1 -> 1.21.10 entity render-state/model API, merchant-offer item-component serialization inside a DataVersion-3337 template, the removed Taniwha trade/processor codecs, and jukebox-song data/components. Implementation validation must decode the entire template and processor list during clean dedicated/common bootstrap rather than merely checking file presence.

## World compatibility

- **Fresh worlds:** structures participate normally in eligible newly generated Mushroom Fields chunks after the data pack is enabled.
- **Existing worlds, unexplored chunks:** structures may generate in newly generated eligible chunks using the restored structure set. No world rewrite is required.
- **Already-generated chunks:** no Mushroom House is inserted retroactively. Stage 10A must not scan, rewrite, or regenerate existing terrain.
- **Previously generated historical houses:** preserving final registry IDs permits existing templates/entities/items to resolve when otherwise compatible, but no special destructive migration or retro-generation is authorized.

## Dependency and leakage boundary

Hard Stage 10A dependencies are limited to the already-restored Mushroom Fields palette/loot items plus the house-owned Mushroom Trader and disc. No Stage 10B-13 system is required.

Explicit exclusions:

- Sunken Ruins, Swamp Jives, and Witch Hat follow-up (Stage 10B).
- Ghost Town, archaeology, Ghost/Ectoplasm/Poltergeist, Phantom crossover content (Stage 10C).
- Mansion structure, templates, markers, tapestries, loot, and Red Rose (Stage 11).
- Witch quests, primary Witch Hat acquisition, Crude/cladding, Stone Golem, Adjudicator, Mimics, Enchanted Totem, and bosses (Stage 12).
- Beach ecology, boats, and unrelated shared cleanup (Stage 13).

The final Mushroom Trader trade list references only already-restored Mushroom Fields items, vanilla items/effects, Nocturnal, and Button Mushrooms. It does not require Witch quests or later merchant infrastructure. Its Taniwha data machinery is an implementation dependency to replace locally, not permission to restore Taniwha or unrelated trades.

## Historical/showcase appendix - not Stage 10A parity

- **Blightbat:** final entity/code/assets remain, but natural spawn and spawn-placement lines are commented and its spawn egg is hidden. It is not embedded in the Mushroom House. HISTORICAL/DISABLED.
- **Purple Mushroom Fields water:** no final biome water-color or fog-color modification exists. SHOWCASE-ONLY/UNCONFIRMED.
- **Green/glowing vanilla Wandering Trader presentation:** final source does not replace or reskin vanilla Wandering Traders. The house uses a distinct Mushroom Trader entity. SHOWCASE-ONLY where it implies a vanilla-trader replacement.
- **Custom giant cave terrain/mycelium cave carving:** final BM registers no Mushroom Fields carver, density function, noise setting, or surface-rule rewrite. Large cave shape from older/showcase material is not parity.

Conversely, underground Mycelium decoration, underground huge Green/Purple Glowshrooms, small Glowshrooms, Glowfish, and Glowshroom Stew are **ACTIVE FINAL**, already restored Stage 3 content. They are not historical additions and Stage 10A must not reimplement or redesign them.

## Proposed Stage 10A implementation scope

One future implementation checkpoint should contain only this coherent chain:

1. Register the local weighted block-replacement processor.
2. Restore the two biome tags, jigsaw structure, structure set, pool, processor list, and exact `house_1.nbt`.
3. Restore Mushroom House chest loot.
4. Restore Mushroom Trader entity/attributes, source goals/persistence/sounds, visible spawn egg, vanilla-villager renderer with both original textures, empty loot, exact source trade selection, and local replacement for its Taniwha trade data path. Keep natural biome spawning disabled.
5. Restore Button Mushrooms sound event/audio, modern jukebox song, item/component, creative entry, model/item definition/texture/translations, legacy acquisition routes, and `mushroom_disc` advancement.
6. Add a Stage 10A validator covering dynamic-registry decoding, template NBT entity/block IDs, processor weights, loot reachability, trader offers, jukebox metadata/audio, and absence of later-stage leakage/Taniwha dependency.

Planned ordinary registry deltas are one entity type, one spawn-egg item, one disc item, one sound event, and one local structure-processor type. Dynamic/data additions are one structure, one structure set, one template pool, one processor list, one jukebox song, one template, two loot tables (house and empty trader), one advancement, and the two-level biome-tag chain. No custom structure type, structure piece, block, block entity, menu, particle, effect, or recipe is needed.

## Proposed focused runtime matrix

1. **Bootstrap/data:** start integrated and dedicated servers; confirm all structure, processor, template, entity, sound, and jukebox registries decode without Taniwha.
2. **Biome/locate:** in a fresh seed, `/locate structure biomemakeover:mushroom_house`; confirm starts only in `#c:is_mushroom` biomes and not ordinary nearby biomes.
3. **Placement:** inspect several fresh houses for surface projection, terrain adaptation, rotation, intact 11 x 10 x 11 structure, and no extra jigsaw pieces.
4. **Pot selection:** sample generated houses; confirm only the seven final potted outputs appear and no empty/invalid pot remains. Statistical exactness is static/source validated; a small runtime sample is a smoke test.
5. **Chest:** open the sole chest; confirm exactly one Button Mushrooms disc, eight red/brown mushrooms total, and 5-9 third-pool rolls with source-correct counts.
6. **Trader:** confirm exactly one embedded Mushroom Trader, correct two-layer appearance, no despawn, working persisted offers, 8-Emerald disc offer/max uses, trade XP, save/reload, and no free natural Mushroom Trader biome spawning.
7. **Creative trader:** spawn one with the final spawn egg and verify source trade population (five common + one stew + one rare) without a Taniwha dependency.
8. **Disc:** verify Rare/stack-one tooltip `Podington Bear - Button Mushrooms`, jukebox playback, Records volume control, no loop, normal stop near 115 seconds, comparator output 14, ejection, save/reload, and multiplayer audibility.
9. **Advancement:** acquiring the disc triggers visible `Badger Badger Badger` under the Mushroom Fields branch; no unrelated advancement fires.
10. **World compatibility:** load an accepted existing world, generate only new eligible chunks, and confirm already-generated chunks are unchanged.
11. **Regression/leakage:** smoke accepted Stage 3 Mushroom Fields content; verify no Sunken Ruin, Ghost Town, Mansion, Witch, Beach, boat, Blightbat, or other Stage 10+ system was activated.

## Audit conclusion

**Stage 10A = AUDITED / AWAITING IMPLEMENTATION AUTHORIZATION.**

No production implementation was performed and nothing was pushed.
