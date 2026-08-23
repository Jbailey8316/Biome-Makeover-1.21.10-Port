# Stage 3 — Mushroom Fields Restoration

## A. Historical Stage 3 inventory

The authoritative behavioral source is `Lemonszz/Biome-Makeover`, branch `1.20`, commit
`2f314c0596af095a4890995a465f308f69476b4a` (release `1.20.1-1.11.4`). Family membership is
cross-checked against `Lemonszz/taniwha` tag `1.20.0-5.4.4`, commit
`ee029d785850d8b0ad8ba69bee4e069b03253afe`.

Reachable released Mushroom Fields content consists of:

- Purple, green, and waterlogged orange glowshrooms; three luminous huge-cap blocks and a luminous stem.
- Mycelium sprouts and roots; tall red and brown mushrooms; the existing wild-mushroom colony.
- Blighted Balsa wood, leaves, sapling, tree, stripping, fire behavior, signs, and recipes.
- Seven fungal masonry families and two blighted-stone families. Each decoration family is exactly base,
  slab, stairs, and wall.
- Glowshroom stew, raw/cooked Glowfish, Glowfish bucket, Glowfish spawn egg, and Glowfish.
- Fifteen configured features, twelve placed features, eleven biome injections, five feature-type IDs, and
  the Blighted Balsa trunk-placer ID.
- Mycelium bonemeal ecology, glowshroom growth, recipes, loot, tags, translations, six theme advancements,
  Glowfish full-bright rendering and carried orange glowshroom, and the fall-save trigger.

The deterministic inventory is `validation/foundations/stage_3_mushroom_fields_contract.json`.

## B. Reachability decisions

| Historical content | Decision | Evidence / owner |
|---|---|---|
| Glowfish | RESTORE-STAGE-3 | Natural spawn hook is active at weight 7, group 2–7 in Mushroom Fields. |
| Blight Bat | UNREACHABLE-HISTORICAL | Entity exists, but natural spawn and placement calls are commented out; hidden spawn egg. |
| Mushroom Trader | DEFERRED-BY-APPROVED-PLAN | Natural spawn is commented out. Possible Mushroom House marker acquisition is Stage 10A and needs runtime verification. |
| Mushroom House | DEFERRED-BY-APPROVED-PLAN | Templates, processor, structure registration, structure loot, and marker behavior are Stage 10A. |
| Button Mushrooms disc | DEFERRED-BY-APPROVED-PLAN | Structure/loot-owned acquisition is completed with Mushroom House in Stage 10A. |
| `deep_mycelium` assets | UNREACHABLE-HISTORICAL | No released block registration or execution path. |
| Blighted Balsa boat/chest boat | DEFERRED-BY-APPROVED-PLAN | Released IDs require the historical boat type/entity plumbing; no vanilla-boat approximation or placeholder was registered. |

Boat infrastructure must be approved as a small cross-theme checkpoint no later than the next stage that owns a
historical wood boat family (recommended before or within Stage 5). It must restore the common BM boat entity/type
contract and then register Blighted Balsa, Willow, Swamp Cypress, and later Ancient Oak members only in their owning
stages.

## C. Family contracts

The nine decoration families are:

1. `red_mushroom_brick`
2. `brown_mushroom_brick`
3. `purple_glowshroom_brick`
4. `green_glowshroom_brick`
5. `orange_glowshroom_brick`
6. `glowshroom_stem_brick`
7. `mushroom_stem_brick`
8. `blighted_cobblestone`
9. `blighted_stone_bricks`

Each has only the verified base, slab, stairs, and wall. Blighted Balsa restores all 17 verified wood blocks and
15 safe items (13 ordinary block items plus sign and hanging sign). Leaves and sapling remain separate. Boat and
chest-boat items are the only deferred wood-factory members. Properties preserve historical colors, strength,
sound, note instruments, light, fire, piston, stripping, pressure/button timing, and cutout behavior.

## D. Restored blocks and items

- **72 block IDs added:** 62 ordinary block-item blocks plus four sign blocks and six potted blocks.
- **69 item IDs added:** 62 ordinary block items, two special sign items, four standalone food/bucket items,
  and the Glowfish spawn egg.
- Exact IDs, including potted/no-item distinctions, are in the Stage 3 contract.
- Checked-in Java registrations are paired with deterministic generated 1.21.10 item definitions. Pinned released
  models, blockstates, textures, recipes, loot, and advancement inputs are ownership-filtered at build time.

No modern wood-family member was invented. No boat placeholder exists.

## E. Restored vegetation

- Three small glowshrooms grow on historical mushroom substrates; orange is waterloggable.
- Huge purple and orange glowshrooms use the broad crooked-stem cap; green uses its released layered cap.
- Mycelium sprouts/roots accept mycelium in addition to their vanilla-like substrate rules.
- Tall mushrooms retain double-block survival behavior.
- Wild Mushrooms now use the released mushroom survival/spread base rather than the port's generic sturdy-floor bush
  rule. Historical map color, random ticks, fire behavior, and potted form were restored.
- Bonemealing mycelium reproduces the released 128-attempt distribution, water-only orange selection, small/tall
  vanilla mushroom selection, and roots/sprouts weighting.

## F. Restored entity

`biomemakeover:glowfish` is a Salmon-derived water-ambient entity with the released dimensions, tracking range,
attributes, bucket persistence, spawn predicate, weight 7 and group size 2–7. It uses the released texture, full
block light, carried orange-glowshroom render, raw/cooked drops, bucket item, and spawn egg. Salmon supplies the
released movement, schooling, flop, ambient/hurt/death sounds, and fish bucket NBT behavior.

## G. World generation

The complete path is restored:

`Feature/TrunkPlacer registry -> configured JSON -> placed JSON -> Fabric biome modification -> Mushroom Fields`.

Generation steps:

- `UNDERGROUND_DECORATION`: underground mycelium/vegetation patch.
- `FLUID_SPRINGS`: Blighted Balsa trees (the unusual historical step is retained).
- `VEGETAL_DECORATION`: green, purple, and orange glowshrooms; sprouts; roots; underground huge glowshrooms;
  tall brown/red mushrooms; and wild mushrooms.

The configured and placed data remain at their historical IDs. The released configured green huge glowshroom's
reference to the purple feature type is preserved instead of silently correcting released behavior.

## H. Mechanics

- Mycelium bonemeal ecology is supplied by `MyceliumBlockMixin`.
- Glowfish food applies Night Vision and Glowing for 200 ticks independently at 50% chance; raw nutrition/saturation
  is 1/0.1 and cooked is 5/0.6; both remain always edible.
- Glowshroom stew is stack size 1, returns a bowl, supplies 5/0.6, is always edible, and guarantees both effects for
  1200 ticks.
- Using a Glowfish bucket while falling at least 23 blocks triggers `glowfish_bucket_save`, matching the released
  trigger point.

## I. Recipes, loot, advancements, tags, and sounds

- **85 recipes** were added, including shaped family recipes, cooking, smoking, campfire cooking, and stonecutting.
- **72 block loot tables** and **one entity loot table** were added.
- **Six theme advancements** were added. The released resource tree contains no separate checked-in Stage 3
  per-recipe unlock advancement files; none were invented.
- Historical plural 1.20 resource directories are deterministically translated to singular 1.21.10 directories.
  Ingredient/result and advancement item-stack syntax is likewise translated during `processResources`.
- Block/item/log/wood/sign/fish/axolotl-target/mineable/replaceable tags were restored using 1.21.10 paths. Existing
  Ancient Oak and Mesmerite tag members are retained.
- Glowfish uses vanilla Salmon sounds. No separate released Stage 3 particle or entity-sound registry ID exists.

## J. Current Mushroom Colony comparison

The pre-stage Wild Mushrooms implementation was **PRESENT-BUT-DIFFERENT**: geometry and assets matched, but it used
generic sturdy-floor survival and omitted released mushroom random-tick/fire/map-color/pot semantics. Stage 3 changes
it to **COMPATIBLE**, retaining the working 1.21.10 registration and model while restoring released observable rules.
No separate registered `mushroom_colony` ID exists in the released source.

## K. Deferred Mushroom House dependency

All general Mushroom Fields blocks and items needed by Mushroom House are now registered. Stage 10A still owns:

- structure, set, pool, templates and processor list;
- structure-only loot and marker processing;
- Mushroom Trader reachability determination;
- Button Mushrooms disc registration/acquisition if confirmed structure-owned.

Stage 10A must consume these IDs, not redefine them.

## L. Registry changes

| Registry/resource | Before | After | Added |
|---|---:|---:|---:|
| Blocks | 28 | 100 | 72 |
| Items | 31 | 100 | 69 |
| Entity types | 1 | 2 | 1 |
| Sound events | 8 | 8 | 0 |
| Configured-feature resources | 13 | 28 | 15 |
| Placed-feature resources | 13 | 25 | 12 |
| Injected placed features | 3 | 14 | 11 |

Additionally added are five feature-type IDs, one trunk-placer-type ID, and one criterion trigger. No prior ID was
removed, renamed, or reused.

## M. Existing-world safety

- New configured/placed features and Glowfish spawn injection affect newly generated Mushroom Fields terrain.
- They do not retro-generate into already generated chunks and do not require a world reset.
- Runtime mycelium bonemeal behavior applies in existing chunks when explicitly invoked.
- Existing BM IDs, items, blocks, Owls, Owl NBT, and Dark Forest injections remain registered unchanged.
- Adding historical IDs is registry-safe in existing worlds. The production world must still be tested only via a
  copy using the Stage 0 protocol.
- Risk: crossing newly generated chunk borders may show normal generation seams. Removing these added IDs after a
  world has saved them would create missing-registry risk.

## N. Validation

- `git diff --check`: required at final checkpoint.
- Clean offline build: passed during implementation; final hash recorded after the final clean build.
- Java and client compilation: passed.
- Parity validator: passed with 100 blocks, 100 items, two entities, 28 configured and 25 placed resources.
- Historical family validator: passed for all nine decoration families and the 17-block Blighted Balsa contract.
- Recipe, loot, tag, item-definition, model/blockstate, dependency, duplicate-ID and packaged-resource checks: passed
  statically through build and validator.
- Tests: Gradle `test` is `NO-SOURCE`.
- Dedicated-server runtime launch was attempted offline but did not reach Minecraft bootstrap because the development
  runtime lacks cached `net.fabricmc:fabric-log4j-util:1.0.2`. Dependencies were not changed or downloaded.

The dedicated-server launch failure is a **manual/runtime environment blocker**, not a clean production build failure.
Consequently data-pack codec decoding and renderer execution remain runtime verification items.

## O. Manual runtime checklist

### Client / single player

1. Launch with the Stage 3 JAR and create a new test world.
2. `/locate biome minecraft:mushroom_fields`; teleport to the result and generate a broad new-chunk sample.
3. Confirm Blighted Balsa trees, underground mycelium, three glowshroom colors, huge glowshrooms, sprouts, roots,
   tall mushrooms, and wild mushrooms at plausible released density.
4. Use `/place feature biomemakeover:mushroom_fields/blighted_balsa_checked` and each placed-feature ID from the
   Stage 3 contract in disposable terrain.
5. `/give @s biomemakeover:<id>` for every ordinary Stage 3 item; place, break, rotate, waterlog, strip, burn, and
   test stairs/slabs/wall/fence/sign connections and potted forms.
6. Bonemeal dry and source-water-covered mycelium; verify sprouts/roots, small/tall mushrooms, and orange-only water
   placement. Bonemeal each glowshroom and verify huge variants.
7. Craft a representative recipe from every family and every cooking/stonecutting conversion.
8. Eat raw/cooked Glowfish and stew while hungry and while full; verify probabilities, durations, bowl return, and
   always-edible behavior.
9. `/summon biomemakeover:glowfish`; verify school/swim/flop, bucket capture/release/NBT, drops, texture, full light,
   carried orange glowshroom, and sounds.
10. Fall at least 23 blocks and successfully use the Glowfish bucket; verify the advancement.
11. Save/reload and restart; verify blocks, signs, potted plants, Glowfish, and inventory stacks persist.
12. Regress an Owl and current Dark Forest worldgen/blocks.

### Dedicated server

1. Boot with the production JAR, join, disconnect/rejoin, and verify registry synchronization.
2. Generate Mushroom Fields, run the feature/entity/recipe checks, and verify no client-only class loads server-side.
3. Save, stop cleanly, restart, and recheck placed blocks, Glowfish bucket data, entities, tags, and advancements.

### Existing Mythas world copy

1. Confirm the exact JAR hash and load only a disposable copy.
2. Capture missing/unknown registry, datafix, serialization, chunk and data-pack messages.
3. Inspect existing BM blocks/items and an existing Owl, then save/reload and restart.
4. Generate new Mushroom Fields chunks if practical; do not trim or modify existing chunks for this test.

No manual gameplay test is claimed as passed in Stage 3.

## P. Known deviations and unresolved items

- Blighted Balsa boat and chest-boat IDs are deliberately absent pending faithful shared boat infrastructure.
- Mushroom House, possible Mushroom Trader structure reachability, and the structure-owned disc remain Stage 10A.
- Dedicated-server/client/runtime, data-pack decode, exact distribution sampling, advancement trigger, sign atlas, and
  Glowfish render-layer verification are **MANUAL TEST REQUIRED**.
- The validator retains pre-existing warnings for several current-port missing loot resources and grandfathered plural
  sapling tags; Stage 3 introduced no new plural tag paths.

## Q. Stage 4 entry criteria

Stage 4 may begin only after:

1. clean build, validator, family validation, registry diff, packaged JAR audit and `git diff --check` pass;
2. the Stage 3 checkpoint is committed locally and the preservation tag remains unchanged;
3. the reviewer accepts the explicit boat/Stage 10A deferrals and manual-runtime backlog;
4. no Stage 4 Badlands implementation is included in this checkpoint.
