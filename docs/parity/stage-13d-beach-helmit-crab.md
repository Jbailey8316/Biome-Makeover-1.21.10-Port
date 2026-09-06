# Stage 13D — Beach / Helmit Crab

## Status

Implemented / Prism runtime required. This stage restores the released
Helmit Crab package and Beach natural-spawn chain for Minecraft 1.21.10;
runtime acceptance remains open.

## Released inventory

- `biomemakeover:helmit_crab`: passive breedable `Animal` with released shell,
  hiding, seeking, melee, tempt, panic, and water-navigation behavior.
- Beach biome tag (`beach`, `stony_shore`, and optional `c:beach`) and
  `crab_spawnable_on` (`stone` and `#minecraft:sand`).
- Natural spawn: creature category, weight 6, groups 2–5, on-ground motion
  blocking heightmap, bright-light and substrate checks.
- Raw Crab, Cooked Crab, Crab Chowder, and Helmit Crab spawn egg.
- Released crab loot, chowder/furnace/smoker/campfire recipes, client assets,
  renderer/model layer, and six crab sound events.

## Current-Minecraft adaptations

The port uses 1.21.10 entity/data/render-state APIs (`EntitySpawnReason`,
`ValueInput`/`ValueOutput`, synched-data builders, and render states). Released
item models are represented by generated 1.21.10 item definitions/models.
Unchanged released resources remain reference-pipeline-backed and are not
duplicated in source.

## Explicit exclusions

Mushroom Fields, Ghost Town, Dark Forest, Badlands, Blighted Balsa boats,
Mansion data, and Mythas enhancements remain outside this stage.
