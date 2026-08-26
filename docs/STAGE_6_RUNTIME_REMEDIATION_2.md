# Stage 6 Runtime Remediation #2 — Dark Forest Visual and Worldgen Audit

Date: 2026-08-25  
Authority: released BM 1.20.1 source at `2f314c0596af095a4890995a465f308f69476b4a`  
Status: **STATIC/PACKAGE PASS — VISUAL RUNTIME RETEST REQUIRED**

## Accepted runtime evidence

Fresh worlds now load, Dark Forest can be located and generated, Ancient Oak forms and Ivy generate, Itching Ivy slows movement, fissures generate, and Illunite emits visible nighttime light. The Stage 6 registry blocker is runtime PASS.

## A. Itching Ivy and Moth Blossom

The released client registers both blocks and their items with `FoliageShiftBlockColorProvider(35, -10, -5)`. Their historical models deliberately use grayscale `itching_ivy` and `itching_ivy_top` layers with tint indices; the Moth Blossom fan is a separate texture. Stage 6 copied those models but registered no block tint and supplied no modern item-definition tints, leaving the grayscale base white/gray.

The port now applies biome foliage color shifted by +35 red, -10 green and -5 blue to both blocks. Their no-world item forms use the equivalently shifted historical default foliage color `0x6BAB13`: two tint layers for Itching Ivy and three for Moth Blossom, matching the released item provider contract. Cutout rendering, slowdown, spread, blossom geometry and the dedicated six-frame Blossom particle are unchanged.

## B. Flowers and Wild Mushrooms

No defect was demonstrated. The current flower configuration is the final source document: an equal `simple_random_selector` over Lilac, Rose Bush, Peony, Foxglove and Black Thistle, each a seven-try patch. Its placed feature uses noise-threshold count (4 above / 15 below at -0.8), rarity 1/3, square placement, `MOTION_BLOCKING`, and biome filtering. The injection is `VEGETAL_DECORATION`.

Wild Mushrooms use the released eight-try patch, count two, square placement, `WORLD_SURFACE_WG`, biome filter and the intentionally unusual `UNDERGROUND_ORES` injection. One initial exploration sample is insufficient evidence to alter either density. Fresh-chunk sampling and `/place feature biomemakeover:dark_forest/flowers` / `wild_mushrooms` are the appropriate runtime checks.

## C. Fissure geometry

The current feature preserves the final algorithm and configuration. It starts at world surface, selects height 8–15, extends 8–16 positions in both horizontal directions, changes height by -8–3 per segment, turns with 1/5 probability, and performs 1–3 lateral spread passes whose branches lose 2–4 height. Each accepted segment scans only five blocks for its surface floor, fills downward with air (or preserves source water), skins all non-up sides with noise-selected Mesmerite/Illunite and a second Mesmerite depth layer, then places crystals at budding positions.

Consequently narrow open shafts, exposed depressions and shorter isolated lateral branches are source-confirmed possible outputs. The current direction selection, coordinate use, provider scales, noise seed, material weights, budding chance and crystal placement match final source. The dormant boulder/underground definitions remain uninjected, so observed surface patches come from short fissure branches rather than accidental activation. No geometry or frequency change was made. The line comparison found one unrelated omission: the port did not reschedule the fluid state after filling each fissure cell. The released call was restored so water-filled fissures update correctly; it does not alter dry fissure dimensions.

## Validation

The validator now fixes the Itching Ivy/Moth Blossom block-provider and item-tint contracts in addition to the existing injection → placed → configured → Feature-type audit. Minecraft registry bootstrap remains a Prism runtime concern; visual parity likewise requires the focused retest.
