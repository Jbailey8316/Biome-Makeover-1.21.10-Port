# Stage 6 Runtime Remediation #1 — Dark Forest Feature Registry

Date: 2026-08-25  
Authority: released BM 1.20.1 source at `2f314c0596af095a4890995a465f308f69476b4a`  
Status: **STATIC/PACKAGE PASS — RUNTIME REGISTRY RETEST REQUIRED**

## Runtime blocker and root cause

The first Stage 6 Prism JAR could not create or load a world. Its packaged configured features `dark_forest/mesmerite_boulder` and `dark_forest/mesmerite_underground` named the custom types `biomemakeover:mesmerite_boulder` and `biomemakeover:mesmerite_underground`, but Stage 6 had registered only the main `biomemakeover:fissure` feature. Dynamic-registry decoding therefore encountered two unbound `minecraft:worldgen/feature` keys.

Final BM 1.20.1 explicitly registers all three independently. `MesmeriteBoulderFeature` uses `BlockStateConfiguration.CODEC` to build the released surface boulder and attach full Illunite clusters. `MesmermiteUndergroundFeature` extends the ore algorithm using `OreConfiguration.CODEC`, records its placed Mesmerite positions, and attaches full Illunite clusters with the released probability. Although these two configured/placed components are shipped dormant rather than directly injected, their referenced Feature types must bind while datapacks decode.

## Remediation

- Ported `MesmeriteBoulderFeature` and `MesmermiteUndergroundFeature` to Minecraft 1.21.10.
- Registered them under the canonical IDs `biomemakeover:mesmerite_boulder` and `biomemakeover:mesmerite_underground`.
- Preserved `biomemakeover:fissure`, its 1/22 Dark Forest injection, released geometry/material selection, budding Illunite and oriented crystal placement.
- Kept all configured/placed resources and registry IDs intact.

## Validator contract

The validator now checks the entire packaged static chain:

1. Stage 6 biome-injection keys resolve to packaged placed features.
2. Every packaged BM placed feature resolves to a packaged configured feature.
3. Every packaged configured feature uses a validated Minecraft 1.21.10 Feature type or a production-registered BM Feature ID.
4. The four Stage 6 custom Feature IDs (`fissure`, `itching_ivy`, `mesmerite_boulder`, `mesmerite_underground`) are fixed in the Stage 6 contract.

The project has no automated Minecraft datapack/registry bootstrap test. Static resolution and compilation therefore are not runtime validation; a fresh Prism world must prove dynamic-registry loading.

## Focused retest

Launch Minecraft, create a brand-new world, and stop immediately if datapack/registry loading fails. If it opens, locate `minecraft:dark_forest`, travel into fresh terrain, and confirm no registry/worldgen error occurs. Visual Stage 6 acceptance begins only after this gate passes.
