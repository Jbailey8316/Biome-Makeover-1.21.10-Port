# Stage 10B - Sunken Ruins, Swamp Jives, and Witch Hat Restoration

Status: **COMPLETE / RUNTIME ACCEPTED**

Implementation date: 2026-08-29

Starting checkpoint: `4ea3009c3100a6e209d117304f5e7f076ee96d74`

## Authority and scope

The final released Biome Makeover 1.20.1-1.11.4 source and packaged resources are authoritative. Stage 10B restores the active Sunken Ruin structure, saved template piece, nine templates, marker behavior, loot and advancement; Swamp Jives and its native 1.21.10 jukebox data; and the complete shared Witch Hat wearable foundation required by the ruin's secondary Hat acquisition path.

The primary Witch-death Hat drop, Witch quests, quest rewards/UI/networking/state, Ghost Town and archaeology, Ghost/Ectoplasm/Poltergeist, Mansion, and all Stage 10C+ work remain absent. This implementation adds no Mythas enhancement.

## Modern structure architecture

`BMStructures` performs bootstrap-time built-in registrations for both the `biomemakeover:sunken_ruin` `StructureType` and `StructurePieceType`. `SunkenRuinStructure` extends vanilla `Structure` and exposes a strict `MapCodec` containing standard `StructureSettings`, `large_probability`, and `cluster_probability`. The data-driven structure and random-spread structure set retain the canonical IDs.

The final JSON says `surface_structures`, but the released custom class overrides the effective step to `LOCAL_MODIFICATIONS`. The 1.21.10 class preserves that runtime-authoritative discrepancy. Eligible biomes resolve through:

```text
#c:is_swamp
  -> #biomemakeover:swamps
  -> #biomemakeover:has_structure/sunken_ruin
  -> biomemakeover:sunken_ruin
```

The structure set uses linear spread, spacing 24, separation 9, salt 420, full/default frequency, and no exclusion zone. No Fabric biome injection, jigsaw pool, processor list, spawn override, or configuration gate was added.

### Root selection, fitting, and clustering

Generation validates the chunk center through `OCEAN_FLOOR_WG`, starts the piece at the chunk minimum X/Z and temporary Y 90, and independently chooses one of four vanilla rotations with no mirror. A `nextFloat() <= 0.6` roll selects a uniformly random one of three large templates; otherwise it selects a uniformly random one of six small templates.

Immediately before placement each piece moves to `OCEAN_FLOOR_WG - random(1..3)`. The source support scan then descends through air, water, and ice for every footprint column. When a sufficiently broad drop exceeds two blocks, the entire piece lowers to one block above the minimum support. No new water-depth, full-submersion, fluid, dimension, or terrain predicate was introduced.

A large root independently clusters when `nextFloat() <= 0.8`. The final eight perimeter candidate formulas, 4-8 candidate count, random-without-replacement selection, small-template selection, rotations, and piece order are preserved. Auxiliary candidates are checked only against the large root; they are deliberately not mutually collision-checked and may overlap. The released code calculated unused 0.9/0.8 values but constructed every piece with integrity 1. Stage 10B therefore retains complete templates rather than adding artificial erosion.

### Saved pieces and markers

`SunkenRuinPiece` is a modern `TemplateStructurePiece`. Vanilla base serialization stores the template ID and position; the BM extension persists `Rot`, `Integrity`, and `IsLarge`, reconstructs placement settings through the registered piece factory, and preserves rotation, no-mirror placement, and marker handling across save/reload.

The exact final marker behavior is retained:

- `chest` becomes a vanilla chest, waterlogged when water occupies the marker position, and receives `biomemakeover:sunken_ruin` with a random loot seed.
- `witch` independently has a 50% chance, requires air above, creates a real vanilla Witch with `STRUCTURE` initialization, makes it persistent, and adds it server-side. On successful spawning the marker becomes air at/above sea level or water below sea level. A failed roll/check is not replaced by another mob.

Lightning Bug Bottle blocks and block-entity NBT in the original templates are handled by vanilla template placement and the already accepted Stage 5 registration.

## Templates

All original compressed NBT bytes are unchanged and are packaged only below the Minecraft 1.21.10 singular directory `data/biomemakeover/structure/sunken_ruins/`. There is no obsolete plural duplicate.

| Template | Size | SHA-256 | Placed blocks | Markers / block entities |
|---|---:|---|---:|---|
| `sunken_1` | 15x9x17 | `CDD49475FF88926A28D6AC35C2066F9E71299F819482406C4AF304B989B6976C` | 458 | chest 1, witch 3 |
| `sunken_2` | 15x8x15 | `E7F21B1CE6F9DD579B2EB9384A21768DCF1F5CC27D19B27DFA9C1BD036946EB4` | 376 | chest 1, witch 3 |
| `sunken_3` | 15x10x14 | `C81FCBE58E52E9B8A3377104E00CB801D8883B9E64182269524F5A7B609C8777` | 786 | chest 1, witch 2, Lightning Bug Bottle 6 |
| `sunken_small_1` | 7x4x5 | `884938C5EDA8CF976A54D47D45BDA3B0D243EEBAC73C8CB2FA19088CF1E96133` | 57 | witch 1 |
| `sunken_small_2` | 7x5x5 | `23550DE08B5F753BC68087A6900D3C2D0253FF52F6F7B87D7C926F8C1460B3C2` | 97 | chest 1, witch 1 |
| `sunken_small_3` | 7x6x5 | `51E73246225271D8BADDB7B2430F4809830FD5B7E3013062AB9B8877FB707AD5` | 94 | chest 1, Lightning Bug Bottle 1 |
| `sunken_small_4` | 7x6x5 | `0140844ADD46E93A5D3F64BA57759BACC89C7D740EE04E6257FAD0752F0FD3B1` | 70 | chest 1 |
| `sunken_small_5` | 7x9x5 | `43BB2F513638D8553F323EAB3A415F0BE862639A56AAF8028B2BB7BDD9F36539` | 113 | chest 1, witch 1 |
| `sunken_small_6` | 7x8x5 | `2FF392E30034306DCB75DD6159A3F15FD39B536F33A72E34F9A74A1D98094F48` | 137 | chest 1 |

Every template remains DataVersion 2584 with one palette, zero embedded entities, zero jigsaw connectors, and zero direct chest block entities. The packaged-template validator applies Minecraft's current structure DataFix, resolves every palette state, invokes `StructureTemplate.load`, and checks the exact dimensions, bytes, hashes, blocks, markers, and Lightning Bug Bottle NBT.

## Loot and advancements

`biomemakeover:sunken_ruin` is one chest pool with uniform 2-8 rolls, no bonus rolls or conditions, and total weight 58. Weight-6 entries are Charcoal 1-3, Glass Bottle 1-3, Water Potion, Awkward Potion, Water Splash Potion, Red Mushroom 1-4, Brown Mushroom 1-4, and Sugar 1-6. Weight-3 entries are Poison Potion, Witch Hat, and Bottle o' Lightning 1-2. Swamp Jives has weight 1. Obsolete potion NBT is translated to native `minecraft:set_potion` functions; weights and outcomes are unchanged. Witch Hat remains 3/58 and Swamp Jives 1/58 per roll, with neither guaranteed.

The visible `biomemakeover:biomemakeover/sunken_ruin` advancement is parented to `enter_swamp` and uses a vanilla location criterion for the canonical structure. The 1.20.1 JSON used `location.structure`; 1.21.10's `LocationPredicate` codec exposes the equivalent registry-entry list as plural `location.structures`, so the port uses `["biomemakeover:sunken_ruin"]` and validates that the singular key is absent. It retains the Cauldron icon, `Sinking Feeling`, `Find a sunken ruin`, task frame, toast, chat announcement, and visible state.

## Swamp Jives

The historical ID `biomemakeover:swamp_jives_music_disk` is preserved. The rare, single-stack item uses the native `JUKEBOX_PLAYABLE` component and `biomemakeover:swamp_jives` jukebox song. Song data specifies 277 seconds, comparator output 1, and the description `Isaac Chambers - Swamp Jives`. The original streamed, non-looping mono 44.1 kHz Vorbis asset remains exactly 1,100,948 bytes with SHA-256 `4C22E43123601CFDAE6FFE69408E348B082556128EE4E54F3157C4A0808E7124`.

The original item art, generated model, and modern item-definition layer are present. `Swamp Shuffle` is a visible goal advancement, parented to `enter_swamp`, triggered by inventory acquisition of the disc. There is no recipe, trader offer, creeper-drop tag, mob drop, or Stage 12 quest-reward path in this stage.

## Witch Hat foundation

`biomemakeover:witch_hat` is a real head-slot wearable: durability 500, armor 2, enchantability 0, toughness 0, knockback resistance 0, Leather repair, and the Leather equip sound. Native item attributes and `EQUIPPABLE` data implement the common contract without an obsolete ArmorItem subclass.

The final item texture and dedicated 64x128 wearable texture are unchanged. A client-only Fabric armor renderer uses a registered model layer and the exact released four-tier hierarchy, pivots, rotations, UVs, scale, and head transform. The item is the sole member of `#biomemakeover:witch_hats`.

The former `taniwha:wear_armor` criterion is translated into the narrow local `biomemakeover:wear_witch_hat` `PlayerTrigger`. A server-end-tick check triggers it only while the player's head-slot item belongs to `witch_hats`; merely holding or storing the Hat does not qualify. This provides the source-observable `Which Witch?` progression without Taniwha or a general advancement framework.

The only Stage 10B survival acquisition is Sunken Ruin chest loot. The Stage 12A `entities/witch_hat` table and global Witch mixin are absent, so ordinary Witch deaths do not acquire the supplemental 5% + Looting Hat path. Quest state, rewards, UI, and networking are also absent.

## Validation contract

The integrated parity validator covers registration source, canonical IDs, codec fields, effective step, tag path, structure-set values, root/cluster probabilities and sets, source quirks, marker behavior, exact loot and modern potion functions, item/song/sound/model/texture/advancement resources, Witch Hat components/model/tag/trigger, client/common sidedness, no obsolete structure path, no jigsaw resources, no Taniwha runtime reference, and no Stage 12/10C leakage.

`validateStage10BTemplateRuntime` validates the actual processed resource tree through Minecraft 1.21.10 bootstrap, DataFixers, registered block lookups, `StructureTemplate.load`, exact template hashes and contracts, and built-in StructureType/StructurePieceType presence. It does not claim that a static JVM audit can replace natural generation, structure-piece chunk save/reload, marker side effects, or renderer/gameplay testing.

The normal clean offline build, Stage 10A runtime-template validator, Stage 9B contracts, JSON/resource/package/reference audits, audio hash, client/common sidedness scan, leakage scans, and `git diff --check` form the closure suite. Java tests remain `NO-SOURCE`; these validators are dedicated Gradle audit tasks rather than fake unit architecture.

The offline dedicated-server task remains subject to the established uncached `net.fabricmc:fabric-log4j-util:1.0.2` limitation. Dependency versions are not changed to mask it.

The implementation candidate passes the clean offline build, integrated parity validator, both Stage 10A and Stage 10B packaged template-load tasks, final-source NBT audit, Stage 9B regression contracts, exact loot/advancement/tag/model/texture/audio checks, packaged-JAR reference audit, client/common sidedness scan, Taniwha/Sliding/Stage 10C+ leakage scans, and `git diff --check`. The accepted candidate registries contain 262 blocks, 277 items, 13 entities, 2 block entities, 43 sounds, and 2 particles. The JAR contains 279 blockstates, 601 block models, 277 item definitions, 279 item models, 299 PNG textures, 78 OGG files, 330 recipes, 275 loot tables, 38 advancements, 88 tags, 2 structures, 2 structure sets, 10 templates, and 2 jukebox songs.

## Prism runtime matrix

1. In a fresh disposable world, verify bootstrap completes without registry or worldgen decode errors.
2. Run `/locate structure biomemakeover:sunken_ruin`, teleport above the result, and verify a physical ruin exists; `/locate` alone is not acceptance.
3. Check plausible swamp-seafloor fitting. Inspect several results for rotations and both large/small forms; observe a clustered large ruin if practical without requiring exhaustive RNG.
4. Open ruin chests and confirm coherent source loot. Swamp Jives and Witch Hat are rare, so commands may accelerate their item tests without changing production probability.
5. Confirm Witch markers can create persistent vanilla Witches and that generated Witches survive save/reload. Confirm applicable templates retain Lightning Bug Bottles after reload.
6. Play Swamp Jives in a jukebox: original track, non-looping normal completion near 277 seconds, comparator signal 1, and `Swamp Shuffle` on acquisition.
7. Equip a Witch Hat: correct four-tier head model standing/walking/crouching/turning, armor value 2, durability behavior, Leather repair, `Which Witch?` on actual head-slot wear, and persistence after reload.
8. Smoke-test that ordinary Witches do not have an active supplemental Hat drop and no Witch quest UI/system appears.
9. In existing worlds, test only newly generated eligible chunks; already-generated chunks remain unchanged and no retro-generation occurs.

Stage 10B is complete and runtime accepted. Stage 10C is not started.

## Runtime remediation - Sinking Feeling

The first Prism candidate awarded `Sinking Feeling` immediately when a player logged into a desert, before locating or entering a ruin. The final 1.20.1 advancement uses the vanilla `minecraft:location` trigger with an `entity_properties` player predicate whose location is restricted to the `biomemakeover:sunken_ruin` structure. It is awarded when the player enters/intersects a generated ruin; it is not a login, proximity, chest-opening, or biome-only event.

The defect was a schema translation error. The 1.20.1 JSON key is singular `location.structure`, but the actual 1.21.10 mapped `LocationPredicate` codec has an optional `structures` registry-entry list. The obsolete singular key was ignored during advancement decoding, leaving an empty location predicate that matched the desert login. The remediation changes only this field to `"structures": ["biomemakeover:sunken_ruin"]`. The semantic validator now requires the plural list, canonical structure ID, vanilla location trigger, and absence of the singular key; it also rejects any custom login/player-tick path or Taniwha dependency.

No structure generation, loot, Witch, Hat, disc, renderer, or other Stage 10B behavior changed. To retest, revoke `biomemakeover:biomemakeover/sunken_ruin`, log in away from a ruin and confirm no toast, then teleport into a generated ruin and confirm `Sinking Feeling` awards. The exact command is:

```text
/advancement revoke @s only biomemakeover:biomemakeover/sunken_ruin
```

This remediation was runtime accepted in Prism: away from a ruin, Sinking Feeling did not award; entering/intersecting the generated Sunken Ruin awarded it. The accepted candidate hash was `E82406AE2077696792AEA807005574CD8FC5840A4DFC27E9C19DC67DF3B43725`.

## Final Prism acceptance

The final disposable-world pass confirmed successful bootstrap, `/locate structure biomemakeover:sunken_ruin`, physical ruin generation at the located position, acceptable swamp terrain fitting, chest markers and coherent observed loot (Charcoal, Awkward Potion, and Red Mushrooms), Witch markers and real Witches, and clean save/reload. The Witch Hat equipped and rendered with the accepted four-tier model; `Which Witch?` fired only on actual head-slot equip. Swamp Jives acquisition, `Swamp Shuffle`, jukebox playback, and comparator signal 1 all passed. The earlier Sinking Feeling login failure was absent after the plural `structures` remediation, and entry into the generated ruin awarded the advancement.

The full weighted loot table, exact rare-entry probabilities, non-looping configured song duration, and other deterministic contracts remain covered by static/package validation; they are not overstated as individually observed natural rolls or manually timed playback. Optional terrain-blending/overgrowth polish is recorded only as a future **MYTHAS ENHANCEMENT CANDIDATE** and was not implemented.

Stage 10B is **COMPLETE / RUNTIME ACCEPTED**. Stage 12A primary Witch-death Hat loot and Witch quests remain deferred, and Stage 10C remains not started.
