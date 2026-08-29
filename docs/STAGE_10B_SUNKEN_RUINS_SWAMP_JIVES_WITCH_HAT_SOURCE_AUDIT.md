# Stage 10B - Sunken Ruins, Swamp Jives, and Secondary Witch Hat Source Audit

Status: **AUDITED / AWAITING IMPLEMENTATION AUTHORIZATION**

Audit date: 2026-08-29

Repository checkpoint: `d352e425b3a4713b84bb79fda03fc94e8c78bcb1`

## Authority and classification

The authoritative parity target is the final released Biome Makeover 1.20.1-1.11.4 source and resources at `2f314c0596af095a4890995a465f308f69476b4a`. Final Java, data, binary templates, loot, advancements, assets, and tags control this audit. Older/showcase material is supporting evidence only and cannot expand the parity scope.

The systems below are **ACTIVE FINAL 1.20.1 CONTENT**. Nothing in the audited Stage 10B chain is merely registered/dormant:

1. the custom Sunken Ruin structure, custom saved piece, nine templates, marker behavior, loot, and location advancement;
2. the Swamp Jives music disc, original streamed audio, ruin-loot acquisition, and inventory advancement;
3. the Sunken Ruin's secondary Witch Hat acquisition, which requires the shared wearable Hat item, rendering, tag, and wear advancement to be functional.

The global Witch-death Hat drop and Witch quest system are also active final content, but they remain Stage 12A ownership. Swamp Jives appears in final Witch quest reward data as an additional later acquisition path; that is a **DEFERRED INTEGRATION**, not a reason to pull quests into Stage 10B.

## Scope decision

Stage 10B can proceed independently of Stage 10C, Mansion, and Beach work, with one explicit scope correction:

- **Stage 10B parity:** Sunken Ruin structure/type/piece/data/templates/markers/loot/advancement; Swamp Jives item/sound/jukebox song/assets/advancement; complete minimum Witch Hat item foundation, secondary ruin-loot acquisition, `witch_hats` tag, renderer/model/assets, and `witch_hat` wear advancement.
- **Stage 12A remains:** supplemental Witch-death Hat loot, the global Witch mixin/state/AI changes, quest categories/rewards/menu/networking, the `witch_trade` trigger and advancement, and the quest-owned Swamp Jives reward entries.

Registering only a non-wearable placeholder Hat would not close Stage 10B: final ruin loot yields a real 500-durability helmet, and its child progression requires wearing it. Conversely, the Hat item has no intrinsic quest code and can be restored without activating Witch quests.

## Canonical IDs and resources

| Role | Final ID/path | Classification |
|---|---|---|
| Structure type | `biomemakeover:sunken_ruin` | Active final |
| Structure piece type | `biomemakeover:sunken_ruin` | Active final |
| Data-driven structure | `biomemakeover:sunken_ruin` | Active final |
| Structure set | `biomemakeover:sunken_ruins` | Active final |
| Structure biome tag | `biomemakeover:has_structure/sunken_ruin` | Active final |
| Shared biome tag | `biomemakeover:swamps` | Active final/shared |
| Loot table | `biomemakeover:sunken_ruin` | Active final |
| Structure advancement | `biomemakeover:biomemakeover/sunken_ruin` | Active final |
| Swamp Jives item | `biomemakeover:swamp_jives_music_disk` | Active final; historical `disk` spelling |
| Swamp Jives sound | `biomemakeover:swamp_jives` | Active final |
| Future 1.21 jukebox song | `biomemakeover:swamp_jives` | Modern translation |
| Disc advancement | `biomemakeover:biomemakeover/swamp_disc` | Active final |
| Witch Hat item | `biomemakeover:witch_hat` | Active final/shared Stage 10B/12A foundation |
| Witch Hat item tag | `biomemakeover:witch_hats` | Active final/shared |
| Witch Hat advancement | `biomemakeover:biomemakeover/witch_hat` | Active final, parented to Sunken Ruin |
| Primary Witch Hat loot | `biomemakeover:entities/witch_hat` | Active final, Stage 12A |
| Quest advancement | `biomemakeover:biomemakeover/witch_quest` | Active final, Stage 12A |

There is no Sunken Ruin template pool, processor list, configured feature, placed feature, structure tag, sound, particle, custom spawn override, or configuration gate. The structure is not jigsaw-based.

## Final reachability chain

```text
#c:swamp / #forge:is_swamp
  -> #biomemakeover:swamps
  -> #biomemakeover:has_structure/sunken_ruin
  -> biomemakeover:sunken_ruins random-spread set
  -> biomemakeover:sunken_ruin custom structure
  -> one large or small custom TemplateStructurePiece
     -> optional small-piece cluster when the root is large
     -> chest markers -> biomemakeover:sunken_ruin loot
        -> Swamp Jives
        -> secondary Witch Hat
     -> witch markers -> optional persistent vanilla Witch
```

The structure set and biome tags make this chain naturally reachable in final gameplay. The location advancement separately detects entry into `biomemakeover:sunken_ruin`.

## Structure architecture and generation

`BMStructures` registers both `StructureType<SunkenRuinFeature>` and `StructurePieceType` as `biomemakeover:sunken_ruin`. `SunkenRuinFeature` extends vanilla `Structure`; `SunkenRuinPiece` extends `TemplateStructurePiece` and persists `Rot`, `Integrity`, and `IsLarge`.

### Structure settings

| Setting | Final value |
|---|---|
| Biomes | `#biomemakeover:has_structure/sunken_ruin` |
| Large probability | `0.6` |
| Cluster probability | `0.8`, evaluated only for a large root |
| Structure-set spacing | 24 chunks |
| Separation | 9 chunks |
| Spread | linear random spread |
| Salt | 420 |
| Frequency/reduction | default/full; none specified |
| Exclusion zone | none |
| Spawn overrides | empty |
| Terrain adaptation | omitted/default `none` |
| Jigsaw size/max center distance/start pool | not applicable |
| Mirror | always `NONE` |
| Rotation | uniformly random among four rotations for each piece |
| Config gate | none |

The final structure JSON serializes `step: surface_structures`, but the final class explicitly overrides `step()` to return `GenerationStep.Decoration.LOCAL_MODIFICATIONS`. The subclass accessor is the runtime-effective generation step. This source quirk must be preserved/tested deliberately rather than silently following the misleading JSON field.

### Root and cluster selection

- The generation point uses `onTopOfChunkCenter(..., OCEAN_FLOOR_WG, ...)` for biome/height validation.
- The root template position begins at the structure chunk's minimum X/Z and temporary Y 90.
- A `nextFloat() <= 0.6` roll selects large versus small.
- Large variants are selected uniformly from three templates; small variants uniformly from six.
- A large root receives a cluster when a second `nextFloat() <= 0.8` roll succeeds.
- Cluster generation chooses 4-8 entries without replacement from eight hardcoded perimeter candidate zones. A candidate is skipped only when its hardcoded rotated 5x6 check box intersects the large root. The final code does not test small-piece boxes against one another, so auxiliary pieces can overlap.
- Cluster pieces reuse the same six small templates with independent uniformly random template and rotation choices.

The code calculates an intended root integrity of `0.9` for large and `0.8` for small, but `addPiece` ignores that parameter and always constructs every final piece with integrity `1`. `postProcess` installs `BlockRotProcessor(1)` followed by `BlockIgnoreProcessor.STRUCTURE_AND_AIR`; consequently final gameplay does **not** randomly erode template blocks. This is an active final quirk and must not be "fixed" during parity restoration.

### Seafloor fitting and liquid behavior

Immediately before placement, every piece sets its Y to `OCEAN_FLOOR_WG - random(1..3)`. It then examines the footprint, scans downward through air, water, and ice to solid support, and may lower the piece to the minimum support when the footprint would otherwise hang over a drop greater than two blocks.

There is no water-depth, water-surface, fluid, neighboring-biome, or explicit dimension predicate. Eligibility comes from the swamp biome tag; the height code tolerates air, water, and ice. The result can be buried into the swamp floor, partly submerged, or partly emergent depending on local terrain. Full submersion is not guaranteed by final source.

### Markers

Templates contain only `chest` and `witch` data markers:

- `chest`: replaces the marker with a vanilla Chest, waterlogged exactly when water exists at the position, and assigns `biomemakeover:sunken_ruin` with a random loot seed.
- `witch`: independently passes a 50% roll and requires the block above to be air. Success creates a vanilla Witch, marks it persistent, calls `finalizeSpawn` with `MobSpawnType.STRUCTURE`, and adds it server-side. The marker position is restored to air at/above sea level or water below sea level.

The Stage 12 Witch mixin will eventually give these vanilla Witches the same global final quest/drop behavior as every other Witch. The ruin does not hard-depend on that later integration to generate or spawn the base Witch.

## Template inventory

All final files originate below `data/biomemakeover/structures/sunken_ruins/` in the 1.20.1 source. A 1.21.10 implementation must package the unchanged/translated files below singular `data/biomemakeover/structure/sunken_ruins/`.

All nine are DataVersion 2584, contain one palette, zero embedded entities, zero jigsaw connectors, and zero direct chests. Loot containers are created from data markers. `Stored blocks` includes explicit air/marker states; `placed blocks` excludes air, structure void, and structure blocks.

| Template ID | Size | Bytes | SHA-256 | Palette | Stored / placed blocks | Markers | Block entities |
|---|---:|---:|---|---:|---:|---|---|
| `sunken_1` | 15x9x17 | 7,465 | `CDD49475FF88926A28D6AC35C2066F9E71299F819482406C4AF304B989B6976C` | 28 | 2,295 / 458 | 1 chest, 3 witch | none |
| `sunken_2` | 15x8x15 | 6,296 | `E7F21B1CE6F9DD579B2EB9384A21768DCF1F5CC27D19B27DFA9C1BD036946EB4` | 51 | 1,800 / 376 | 1 chest, 3 witch | none |
| `sunken_3` | 15x10x14 | 7,307 | `C81FCBE58E52E9B8A3377104E00CB801D8883B9E64182269524F5A7B609C8777` | 48 | 2,100 / 786 | 1 chest, 2 witch | 6 Lightning Bug Bottles |
| `sunken_small_1` | 7x4x5 | 923 | `884938C5EDA8CF976A54D47D45BDA3B0D243EEBAC73C8CB2FA19088CF1E96133` | 12 | 140 / 57 | 1 witch | none |
| `sunken_small_2` | 7x5x5 | 1,142 | `23550DE08B5F753BC68087A6900D3C2D0253FF52F6F7B87D7C926F8C1460B3C2` | 15 | 175 / 97 | 1 chest, 1 witch | none |
| `sunken_small_3` | 7x6x5 | 1,252 | `51E73246225271D8BADDB7B2430F4809830FD5B7E3013062AB9B8877FB707AD5` | 18 | 210 / 94 | 1 chest | 1 Lightning Bug Bottle |
| `sunken_small_4` | 7x6x5 | 1,192 | `0140844ADD46E93A5D3F64BA57759BACC89C7D740EE04E6257FAD0752F0FD3B1` | 13 | 210 / 70 | 1 chest | none |
| `sunken_small_5` | 7x9x5 | 1,536 | `43BB2F513638D8553F323EAB3A415F0BE862639A56AAF8028B2BB7BDD9F36539` | 17 | 315 / 113 | 1 chest, 1 witch | none |
| `sunken_small_6` | 7x8x5 | 1,455 | `2FF392E30034306DCB75DD6159A3F15FD39B536F33A72E34F9A74A1D98094F48` | 18 | 280 / 137 | 1 chest | none |

The only embedded block-entity tag is `{id:"biomemakeover:lightning_bug_bottle"}`. There are no embedded item stacks, UUIDs, mobs, archaeology blocks, or loot-table NBT. The actual 1.21.10 Structure data fixer and `StructureTemplate.load` successfully decode all nine templates; every palette block resolves against the current port.

Biome Makeover palette dependencies are limited to already-restored Stage 5 content: Willow log/planks/fence/stairs/slab, Stripped Willow Log, Willow Leaves, Willowing Branches, and Lightning Bug Bottle. Rotation is handled by vanilla template state transformation; there are no connectors or fixed-facing entity assumptions.

## Loot contract

Every `chest` marker uses the single `biomemakeover:sunken_ruin` chest table. It has one pool, uniform 2-8 rolls, zero bonus rolls, no pool conditions, and total entry weight 58.

| Entry | Weight | Output/functions |
|---|---:|---|
| Charcoal | 6 | 1-3 |
| Glass Bottle | 6 | 1-3 |
| Water Potion | 6 | one potion with `Potion:"minecraft:water"` |
| Awkward Potion | 6 | one potion with `Potion:"minecraft:awkward"` |
| Water Splash Potion | 6 | one splash potion with `Potion:"minecraft:water"` |
| Red Mushroom | 6 | 1-4 |
| Brown Mushroom | 6 | 1-4 |
| Sugar | 6 | 1-6 |
| Poison Potion | 3 | one potion with `Potion:"minecraft:poison"` |
| Witch Hat | 3 | exactly one |
| Bottle o' Lightning | 3 | 1-2 `biomemakeover:lightning_bottle` |
| Swamp Jives | 1 | exactly one `biomemakeover:swamp_jives_music_disk` |

The table uses random sequence `biomemakeover:sunken_ruin`. No enchantment, archaeology, reward function, guaranteed item, or nested table is present. At one chest, Witch Hat is 3/58 per roll and Swamp Jives is 1/58 per roll. Averaged over the uniform 2-8 roll count, the exact probability of at least one Hat is approximately 22.888244%, and the probability of at least one disc is approximately 8.273040%. Repeated rolls can produce more than one.

All three large root templates have one chest marker. Five of six standalone small roots have one; `sunken_small_1` has none. Thus a root alone has a 14/15 (93.333...%) chest probability. A clustered large ruin can add several more small-piece chest opportunities; 4-8 candidates are attempted, but intersection skips mean that is not a guaranteed piece count.

## Advancements

| ID | Parent | Display | Criterion |
|---|---|---|---|
| `biomemakeover:biomemakeover/sunken_ruin` | `biomemakeover:biomemakeover/enter_swamp` | Cauldron icon; `Sinking Feeling`; `Find a sunken ruin`; task; visible/toast/chat | `minecraft:location`, player location inside structure `biomemakeover:sunken_ruin` |
| `biomemakeover:biomemakeover/swamp_disc` | `biomemakeover:biomemakeover/enter_swamp` | Disc icon; `Swamp Shuffle`; `Collect the Music Disc from the Swamp biome`; goal; visible/toast/chat | `minecraft:inventory_changed` containing `biomemakeover:swamp_jives_music_disk` |
| `biomemakeover:biomemakeover/witch_hat` | `biomemakeover:biomemakeover/sunken_ruin` | Hat icon; `Which Witch?`; `Wear a witch hat`; task; visible/toast/chat | final custom `taniwha:wear_armor` trigger for `biomemakeover:witch_hat` |

All three have no reward. `witch_quest`, parented to `witch_hat` and triggered by `biomemakeover:witch_trade`, remains Stage 12A.

## Swamp Jives contract

| Property | Final value |
|---|---|
| Item ID | `biomemakeover:swamp_jives_music_disk` |
| Display | `Music Disc` |
| Credit/description | `Isaac Chambers - Swamp Jives` |
| Rarity / stack | Rare / 1 |
| Comparator output | 1 |
| Configured duration | 277 seconds |
| Actual OGG duration | approximately 277.760 seconds |
| Sound event | `biomemakeover:swamp_jives` |
| Sound asset | `assets/biomemakeover/sounds/swamp_jives.ogg` |
| Sound behavior | streamed, non-looping, normal record playback; no custom subtitle/attenuation |
| OGG metadata | Vorbis, mono, 44,100 Hz, 1,100,948 bytes |
| OGG SHA-256 | `4C22E43123601CFDAE6FFE69408E348B082556128EE4E54F3157C4A0808E7124` |
| Item model | `biomemakeover:item/swamp_jives_music_disk` -> `biomemakeover:item/music_disc_swamp_jives` |
| Texture | 16x16 `music_disc_swamp_jives.png`, 301 bytes |
| Texture SHA-256 | `3B5B30ABA6FA411E96B1B0B978D2296F9207475F3C53DE746699515D481E1777` |
| Creative placement | visible in the Biome Makeover tab |

Final direct acquisition is the Sunken Ruin chest. Final Witch quest reward tables `quest_reward/items` and `quest_reward/potion` also each list one Swamp Jives disc, but those paths depend on the Stage 12 quest engine. There is no crafting recipe, trader offer, advancement reward, or dedicated mob drop. The final resource adds the disc only to the old general `minecraft:music_discs` tag; it does not add a creeper-drop-specific path. Stage 10B must not add it to modern `minecraft:creeper_drop_music_discs`.

The 1.21.10 port should mirror the accepted Button Mushrooms architecture: register `ResourceKey<JukeboxSong>`, register the SoundEvent, create a rare stack-one item with `jukeboxPlayable`, package `jukebox_song/swamp_jives.json` with comparator 1 and 277 seconds, reuse the original streamed OGG unchanged, and add the modern item-definition layer. No jukebox mixin is justified.

## Witch Hat: secondary versus primary acquisition

### Secondary path - Stage 10B

The secondary path is exactly the weight-3 `biomemakeover:witch_hat` entry in `biomemakeover:sunken_ruin` chest loot. It is not archaeology, a trader, a reward, or a structure mob drop. Because the item is currently absent, Stage 10B must restore the shared Hat foundation:

- `biomemakeover:witch_hat`, common-rarity helmet, stack one through equipment semantics;
- durability 500, armor 2, enchantment value 0, no toughness or knockback resistance;
- leather equip sound and Leather repair ingredient;
- 16x16 inventory texture/model and modern item definition;
- dedicated 64x128 wearable texture and the final four-tier Witch Hat model;
- client-only modern armor renderer/model layer, following the accepted Cowboy Hat render-state pattern;
- `biomemakeover:witch_hats` item tag;
- the final `Which Witch?` wear advancement, with a narrow local replacement for Taniwha's equip trigger.

The original assets are unchanged candidates:

- item texture: 357 bytes, SHA-256 `54755351E1A12572A85B5BF0637BA028D0BFC5B75227971076D26ED03CBD157E`;
- wearable texture: 491 bytes, SHA-256 `1E98647EC2007049EE03A0201725D9FA7E8C7E757159E3022C55005AA6769D43`.

The Hat has no intrinsic active ability. Its later gameplay role is that a worn item in `biomemakeover:witch_hats` permits Witch quest interaction.

### Primary path - Stage 12A

Final `WitchMixin_Quests.dropFromLootTable` supplements every non-blocked Witch death with `biomemakeover:entities/witch_hat`. That entity loot table rolls once and drops one Hat only when killed by a player and `random_chance_with_looting` succeeds: 5% base plus 5 percentage points per Looting level.

That global mixin also replaces player targeting for qualifying Hat wearers, adds interaction/customer goals, creates and persists quest lists/timers/customer state, grants a 12,000-tick despawn shield, and opens the quest network/menu flow. These are the primary Hat/quest progression and remain Stage 12A. Stage 10B must not register the primary Witch loot table as an active hook or port the quest system early.

## Current 1.21.10 port gap analysis

| Component | Current status at `d352e42` | Finding |
|---|---|---|
| Custom structure type/piece and Java placement | MISSING | No registry or implementation |
| Structure and structure-set JSON | MISSING | No Stage 10B worldgen data |
| Nine templates | MISSING | Reference-only; none packaged |
| Biome tag chain | MISSING for structure | Current Swamp restoration uses Fabric biome changes, not these structure tags |
| Ruin loot | MISSING | No table or marker consumer |
| Ruin advancement | MISSING | No location progression |
| Swamp Jives item/sound/song/assets/advancement | MISSING | No registry or packaged resources |
| Witch Hat item/tag/model/renderer/advancement | MISSING | No registry or packaged resources |
| Primary Witch Hat death path | MISSING BY DESIGN | Stage 12A |
| Witch quests and quest disc reward | MISSING BY DESIGN | Stage 12A |
| Willow palette | ALREADY RESTORED | Stage 5, all referenced states resolve |
| Lightning Bug Bottle block entity | ALREADY RESTORED | Stage 5; template NBT is only its canonical ID |
| Bottle o' Lightning loot item | ALREADY RESTORED | Stage 5 |
| Vanilla Witch and swamp biomes | AVAILABLE | No new entity registration required |
| Button Mushrooms jukebox pattern | ALREADY RESTORED | Stage 10A provides a safe modern reference architecture |
| Cowboy Hat equipment/render pattern | ALREADY RESTORED | Stage 4 provides the closest modern Hat foundation |

There are no partial, dormant, obsolete, or colliding Stage 10B production remnants. Current references occur only in roadmap/audit validators. No ID duplication is present.

Current accepted totals remain 262 blocks, 275 items, 13 entities, 2 block entities, 42 sounds, and 2 particles. The accepted JAR has 279 blockstates, 601 block models, 275 item definitions, 277 item models, 296 PNG textures, 77 OGG assets, 330 recipes, 274 loot tables, 35 advancements, and 86 tag files.

## Cross-stage boundaries

- **Stage 10C archaeology/Ghost Town:** no dependency. The nine templates contain no suspicious blocks, archaeology loot, brushable entities, data markers beyond chest/witch, Ghost/Ectoplasm/Poltergeist references, or Ghost Town processors.
- **Stage 11 Mansion:** no dependency or shared template/processor path.
- **Stage 12 Witch systems:** deferred integration only. The shared Hat item must move into Stage 10B; primary Witch death loot, quests, networking, and quest-owned disc rewards remain Stage 12A.
- **Stage 13 Beach/boats:** no dependency. Willow boats are not referenced by structure code, templates, or loot.

## Modern 1.21.10 migration and risk assessment

| Migration | Risk | Required translation |
|---|---|---|
| Custom `StructureType` codec | HIGH | Use a 1.21.10 `MapCodec` with `settingsCodec`, `large_probability`, and `cluster_probability`; register before dynamic structure decode |
| Custom saved piece type | HIGH | Port current `StructurePieceType.load(context, tag)`, `TemplateStructurePiece` constructors/save fields, and reload-safe template manager access |
| Effective generation step conflict | HIGH | Preserve/test runtime `LOCAL_MODIFICATIONS` despite source JSON saying `surface_structures` |
| Placement/height algorithm | HIGH | Translate `GenerationContext`, OCEAN_FLOOR_WG, footprint scan, marker handling, and cluster boxes without redesign |
| Template directory | HIGH | Package only singular `data/biomemakeover/structure/sunken_ruins/*.nbt`; reject obsolete plural duplicates |
| Template NBT/DataFix | MEDIUM, evidence lowers it | All nine DataVersion-2584 files data-fix and load through actual 1.21.10 classes; retain exact hashes unless a proven runtime block-entity migration requires a deterministic rewrite |
| Lightning Bug Bottle NBT | LOW/MEDIUM | Minimal canonical `id` only; validate actual placed block entity/runtime bottle behavior |
| Structure-set JSON | LOW | Random spread 24/9/linear/salt 420 remains native data |
| Biome conventions | LOW | Translate optional `#c:swamp`/Forge union to current Fabric `#c:is_swamp`, which contains both `minecraft:swamp` and `minecraft:mangrove_swamp`; retain BM tag chain |
| Loot schema | MEDIUM | Move `loot_tables` to `loot_table`; translate `set_nbt` potion tags to modern potion-content components/providers while preserving weights/counts |
| Advancements | MEDIUM | Move to singular `advancement`; update icon/item-predicate schema; replace Taniwha wear trigger narrowly |
| Swamp Jives | LOW | Reuse accepted native jukebox-song/item-component architecture and original OGG |
| Witch Hat equipment | MEDIUM | Express 500 durability, armor 2, leather repair/equip, head equippable component, no toughness/KB |
| Witch Hat rendering | MEDIUM/HIGH | Port final model to 1.21.10 render states/client-only ArmorRenderer, patterned after accepted Cowboy Hat |
| Registry bootstrap/sidedness | HIGH | Structure type/piece must exist before datapack decode; Hat renderer must never leak into common/server paths |

No Fabric biome-modification injection is needed for the structure itself: native structure/set/tag data is the final architecture once the two custom code registries exist.

## Validator contract for implementation

The audit added `auditStage10BSourceTemplates`, an audit-only task that measures all nine final binaries and runs them through the actual 1.21.10 Structure data fixer, registered block lookup, and `StructureTemplate.load`. It is intentionally not a production registration or implementation.

The future Stage 10B validator should enforce:

1. exact structure type and piece type IDs, registered before dynamic data decode;
2. exact structure/set codecs and 0.6/0.8, 24/9/linear/420 parameters;
3. effective `LOCAL_MODIFICATIONS` step and final placement/cluster/integrity-1 quirks;
4. `#biomemakeover:has_structure/sunken_ruin -> #biomemakeover:swamps -> #c:is_swamp`, including vanilla Swamp and Mangrove Swamp;
5. all nine templates at the singular path, no plural duplicates, exact hashes/dimensions/palette/block/entity/marker/block-entity counts;
6. actual resource-manager lookup, NBT DataFix, `StructureTemplate.load`, non-empty bounds, and custom piece construction/save round trip;
7. exact chest/witch marker behavior, waterlogged chest, persistent real Witch, and loot references;
8. exact loot rolls, weights, counts, potion components, random sequence, Hat/disc references;
9. Swamp Jives item/sound/jukebox song/model/texture/OGG hash, duration 277, comparator 1, and no creeper-drop tag;
10. Witch Hat equipment components, attributes, repair, tag, model/textures/client renderer, and wear advancement;
11. no primary Witch loot/quest activation and no Stage 10C+ content leakage;
12. packaged-JAR rather than source-tree-only assertions.

Static/resource validation cannot prove natural placement, seafloor fit, cluster geometry, marker execution, or sound playback; those remain explicit runtime gates.

## Proposed Prism runtime matrix

1. **Bootstrap:** load a fresh world and confirm structure/loot/advancement registries decode without errors.
2. **Locate/generate:** run `/locate structure biomemakeover:sunken_ruin`, visit a newly generated Swamp or Mangrove Swamp candidate, and confirm a physical ruin exists.
3. **Placement:** inspect its relationship to swamp floor/water, rotation, intact final-template appearance, and absence of extreme/invalid Y placement.
4. **Variants/clusters:** locate several candidates; confirm both large/small forms and at least one clustered large ruin when practical. Do not require statistical proof of 60%/80% manually.
5. **Markers:** confirm real waterlogged chests; when a witch marker roll succeeds, confirm a persistent real Witch and no structure-block residue.
6. **Template block entities:** inspect a `sunken_3` or `sunken_small_3` containing Lightning Bug Bottles and confirm their accepted Stage 5 rendering/interaction survives placement and reload.
7. **Loot:** confirm the potion/mushroom/material pool and eventual secondary Witch Hat/Swamp Jives outcomes. Static validation carries exact rare probabilities; manual exhaustive sampling is unnecessary.
8. **Progression:** enter a ruin and confirm `Sinking Feeling`; obtain Swamp Jives and confirm `Swamp Shuffle`.
9. **Swamp Jives:** verify item/credit, jukebox playback, non-looping normal stop near 277 seconds, and comparator output 1.
10. **Witch Hat:** equip ruin-looted Hat; confirm inventory and wearable rendering, armor +2, durability/repair basics, and `Which Witch?`. Confirm ordinary ownership alone does not award a wear-only criterion.
11. **Stage boundary:** confirm wearing the Hat does not expose an incomplete quest UI before Stage 12A.
12. **World compatibility:** structures appear in fresh worlds and new eligible chunks of existing worlds; already-generated chunks remain untouched.

## Historical/showcase exclusions

The final release contains exactly the nine audited custom-piece templates. No alternate jigsaw ruin, archaeology edition, auxiliary pool, boss, custom ruin mob, guaranteed Hat/disc, or extra final template was found. General older/showcase swamp ideas such as revived Toad/Tadpole ecology, purple water, enhanced pad clusters, or other ambient expansion are **HISTORICAL / NOT STAGE 10B PARITY** or future **MYTHAS ENHANCEMENT CANDIDATES** unless separately source-proven later.

The primary Witch Hat drop and Witch quests are not historical exclusions: they are active final content deliberately retained for Stage 12A. No Mythas quest/Living World integration is authorized by this audit.

## Explicit implementation exclusions

Stage 10B must not implement Ghost Town/archaeology, Ghost, Ectoplasm, Poltergeist, Phantom crossover recipes, Mansion systems, Witch quests/menus/packets/global targeting, primary Witch Hat death loot, Living World integration, Beach ecology, boats, revived Toad/Tadpole content, or any custom loot/balance/structure variants.

**Stage 10B = AUDITED / AWAITING IMPLEMENTATION AUTHORIZATION**

**NOT PUSHED**
