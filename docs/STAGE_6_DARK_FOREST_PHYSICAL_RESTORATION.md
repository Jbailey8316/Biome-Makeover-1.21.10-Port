# Stage 6 — Dark Forest Physical Restoration

Date: 2026-08-25  
Authority: released BM 1.20.1 source at `2f314c0596af095a4890995a465f308f69476b4a`  
Status: **STATIC/PACKAGE PASS — RUNTIME OPEN**

## Scope and boundary

Stage 6 restores only the released Dark Forest physical ecosystem and exact feature pipeline. Owl reconciliation, Rootlings, Moths, Bulbus Root/Stunt Powder, Nocturnal, Altar/curses, Ectoplasm crossover, Mansion systems, music, boats and all Mythas additions remain deferred. No protected earlier-stage production system was intentionally changed.

## Released manifest

- Ancient Oak complete physical wood family, including ordinary/hanging signs; boats remain deferred.
- Ancient Oak leaves/sapling and both 1x1 and 2x2 growth paths.
- Large Ancient Oak, Small Ancient Oak and Small Dark Oak tree configurations.
- Ivy, Itching Ivy and Moth Blossom physical blocks.
- Foxglove, Black Thistle and Wild Mushrooms physical/worldgen integration.
- Mesmerite and Polished Mesmerite base/slab/stairs/wall families.
- Illunite Block, Budding Illunite, Small/Medium/Large buds, full cluster and Illunite Shard.
- The released Mesmerite/Illunite fissure algorithm.
- Eleven configured-feature definitions, twelve placed-feature definitions and seven direct Dark Forest injections.

The earlier audit shorthand said “11 placed.” The pinned resource tree actually contains twelve: three checked tree wrappers plus `grass`, `tall_grass`, `flowers`, `itching_ivy`, `trees`, `wild_mushrooms`, `mesmerite_boulder`, `mesmerite_underground` and `mesmerite_fissure`. Only seven are injected; the boulder/underground definitions are shipped dormant components. This correction changes inventory wording, not gameplay.

Two pre-Stage-6 `owl_nest` feature JSON files are retained in the source tree as audit artifacts so this checkpoint does not edit Owl-owned files. They never had a production injection and do not exist in final BM 1.20.1; resource processing excludes them from the packaged worldgen inventory. Owl code, spawning, nests and eggs are otherwise untouched.

## Modern translations

- Taniwha registration/family helpers were replaced by local vanilla/Fabric registration while retaining canonical BM IDs.
- The six-face ivy state uses modern `MultifaceBlock`; it is deliberately absent from the climbable tag. Itching Ivy and Moth Blossom retain block-owned slowdown, growth and spread. The released `blossom` particle is registered client-safely with its six historical translucent sprites; Moth attraction remains Stage 8.
- The Taniwha fissure helper is replaced locally with the released BM codec fields, offset construction, column scan, material/depth replacement and crystal placement algorithm.
- Ancient Oak uses modern `TreeGrower` mega/ordinary slots: a 2x2 formation selects the large tree and a single sapling selects Small Ancient Oak.
- The Ancient Oak custom trunk placer and Ivy tree decorator are locally registered under their historical codec IDs.
- Historical nested uniform integer providers are structurally flattened during resource processing for the 1.21.10 codec.
- Historical tool/enchantment loot predicates are structurally migrated across packaged loot tables to the current item-component predicate form.
- Illunite retains day/night/unknown visual state, scheduled state refresh, historical stage light values (5/7/13/15 at night; 2 by day/fixed-time fallback), custom sound set, projectile-hit sounds, four-stage growth and waterlogged facing state.
- Ancient Oak logs are merged into modern `logs` and `logs_that_burn` block/item tags so generated leaves receive correct distance propagation.
- Ancient Oak Leaves and Ivy receive the historical foliage tint in both world and item render paths.

## Exact biome pipeline

| Generation step | Injected placed feature | Released placement |
|---|---|---|
| `VEGETAL_DECORATION` | `dark_forest/grass` | square, world-surface-WG, biome |
| `VEGETAL_DECORATION` | `dark_forest/tall_grass` | rarity 1/3 |
| `VEGETAL_DECORATION` | `dark_forest/flowers` | noise threshold count, rarity 1/3 |
| `TOP_LAYER_MODIFICATION` | `dark_forest/itching_ivy` | rarity 1/4 |
| `UNDERGROUND_ORES` | `dark_forest/trees` | count 3, selector 10% Small Ancient / 20% Small Dark / 5% Large Ancient / default checked Dark Oak |
| `UNDERGROUND_ORES` | `dark_forest/wild_mushrooms` | count 2 |
| `LOCAL_MODIFICATIONS` | `dark_forest/mesmerite_fissure` | rarity 1/22, world-surface-WG |

The unusual tree and mushroom generation step is intentional released behavior. No terrain noise, density function or carver was added.

## Advancement ownership

Packaged now: Enter Dark Forest, Ancient Oak Sapling, Illunite Shard. Advancements requiring Rootling, Moth, Altar/curses, Mansion, loot progression or music remain deferred with their owning stages.

## Validation additions

`stage_6_dark_forest_contract.json` fixes the physical IDs and resource inventory. The validator now verifies exact Stage 6 injection calls/steps. Resource processing now migrates nested uniform providers and old loot tool predicates structurally rather than by filename.

Registry inventory moved from 247 blocks / 248 items / 10 entities / 1 block entity / 27 sounds / 1 particle to 259 / 259 / 10 / 1 / 31 / 2. The exact deltas are +12 blocks, +11 items, +4 sounds and +1 particle; Stage 6 adds no entity or block-entity registry entry.

The packaged JAR contains 320 recipes, 267 loot tables, 27 advancements, 277 blockstates, 595 block models, 261 item models, 259 item definitions, 260 textures, 75 tags, 38 configured features and 37 placed features. Compared with the starting checkpoint this is 0 recipes, +15 loot tables, +3 advancements, +8 blockstates, +17 block models, +9 item models, +11 item definitions, +28 textures and +2 tags. The global configured/placed totals decrease by 2/1 because noncanonical placeholder Dark Forest definitions were removed; the authoritative Dark Forest inventory is 11 configured and 12 placed resources.

## Runtime-open risks

- Natural ratios and geometry for all four selector outcomes.
- Ancient Oak leaf retention/decay and sapling/Fortune/Silk Touch behavior.
- Six-face ivy support, non-climbability and spread.
- Itching Ivy/Moth Blossom slowdown, particles, conversion and spread.
- Fissure geometry/material noise, crystal orientation/light/growth and loot.
- Client cutout/emissive presentation, persistence and dedicated-server classloading.

Static/package validation is not runtime acceptance.

## Prism checklist

Use fresh `minecraft:dark_forest` chunks. Inspect floor density and multiple selector results; confirm terrain/noise remains vanilla. Exercise 1x1 and 2x2 Ancient Oak growth, leaf retention and decay, sapling/stick/Shears/Silk Touch/Fortune drops, then save/reload. Place Ivy on every supported face and verify it cannot be climbed; test support loss and spread. Test Itching Ivy slowdown, Moth Blossom particles, downward-face bonemeal conversion and spread without expecting Moths. Verify Foxglove, Black Thistle, Wild Mushroom variants and potting. Locate multiple fissures above and below ground; inspect all materials/stages, lighting, growth, support, waterlogging and shard/Silk Touch/Fortune drops. Soak a feature-heavy biome, revisit after save/restart, then repeat generation/join/rejoin on a dedicated server.
