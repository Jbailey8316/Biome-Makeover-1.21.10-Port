# Stage 13B — Mushroom Fields released-parity verification

Audit date: 2026-09-06  
Authoritative source: `reference/Biome-Makeover-1.20/common/` (final released
Biome Makeover 1.20.1 source)  
Port baseline: `de6f735759d68e28228d73282b061573e1e253e7`

## Decision

Mushroom Fields is **COMPLETE / PRISM RUNTIME REQUIRED** for this restoration
stage. The 15 released configured features and 12 placed features are supplied
by the existing `build.gradle` reference-resource pipeline, which remaps the
released 1.20.1 paths to the current singular 1.21.10 data paths. The current
Java registrations and `BMWorldgen` injection therefore resolve in the built
artifact.

Stage 13C made no gameplay implementation change. It added a focused validator
and corrected the Stage 13B source-tree-only finding; the fresh artifact now
proves the released Mushroom Fields data is packaged.

## Released Mushroom Fields inventory

The released biome hook is `BMFeatures`' `MUSHROOM_FIELD_BIOMES` tag and its
Mushroom Fields biome modifier. It injects:

- 15 configured features: Blighted Balsa, green/purple/orange glowshrooms,
  huge glowshrooms, mycelium roots/sprouts, tall mushrooms, underground
  vegetation/mycelium, and underground huge glowshrooms.
- 12 placed features: Blighted Balsa (checked and trees), the three
  glowshroom colors, mycelium roots/sprouts, tall mushrooms, underground
  features, and Wild Mushroom Colony.
- Mushroom block families: glowshrooms, huge mushroom blocks, stems, mycelium,
  tall mushrooms, Wild Mushroom Colony, Blighted Balsa, and mushroom/blighted
  masonry families.
- Glowfish ecology: the Glowfish entity, Mushroom Fields spawn rule, bucket,
  food, cooked food, stew, spawn egg, loot, and related advancement.
- Released client/data surface: blockstates, block/item models, textures,
  entity rendering/textures, recipes, loot, tags, sounds, and translations.

The released `BlackThistleBlock` is not a Mushroom Fields feature. Its released
worldgen hook is Dark Forest flowers, so its restored behavior is recorded
under Dark Forest rather than counted here. Mushroom House, Mushroom Trader,
and Button Mushrooms disk are structure-owned/Stage 10A content, not biome
injection gaps. Blighted Balsa boat and chest-boat IDs remain the previously
documented deferred released IDs.

## Current status by feature group

| Feature group | Status | Evidence |
|---|---|---|
| Mushroom Fields biome tag/injection | COMPLETE / PRISM RUNTIME REQUIRED | `BMWorldgen` injects the released key set; the build pipeline supplies the referenced data. |
| Configured features | COMPLETE / PRISM RUNTIME REQUIRED | Released contract contains 15; all 15 are present in the built artifact via `build.gradle`. |
| Placed features | COMPLETE / PRISM RUNTIME REQUIRED | Released contract contains 12; all 12 are present in the built artifact via `build.gradle`. |
| Custom feature/trunk-placer Java types | CURRENT-MC COMPATIBILITY ADAPTATION | `BMFeatures`, custom feature classes, and `BalsaTrunkPlacer` are connected through the current registry/API path. |
| Glowshroom/mycelium/tall-mushroom blocks | PARTIAL | Current registrations/classes exist; released Mushroom Fields generation and the complete client/data surface are not present. |
| Blighted Balsa family | PARTIAL | Current block/item family and tree-grower hooks exist; released Mushroom Fields feature data and full resource closure are absent. |
| Mushroom masonry families | PARTIAL | Current dynamic registrations exist; released Mushroom Fields data/resource closure is not complete. |
| Wild Mushroom Colony block | COMPLETE / RUNTIME OPEN | Current block, loot, models, and Dark Forest generation exist; Mushroom Fields placed-feature entry is absent. |
| Glowfish entity/ecology | CURRENT-MC COMPATIBILITY ADAPTATION / RUNTIME OPEN | Entity, bucket, food, spawn hook, and current renderer path exist; the released dedicated Mushroom Fields resource surface is not fully present. |
| Mushroom recipes/loot/tags/advancement data | PARTIAL | Some current entries exist, but the released Mushroom Fields package is not complete as a data set. |
| Mushroom Fields client resources | PARTIAL | Wild Mushroom/structure-owned assets exist; most released glowshroom, Blighted Balsa, Glowfish, and masonry resources are absent. |
| Mushroom House/Trader/Button Mushrooms disk | COMPLETE / FROZEN (separate scope) | Existing Stage 10A implementation and resources; not a Mushroom Fields biome injection requirement. |
| Black Thistle | NOT ACTUALLY MUSHROOM FIELDS SCOPE | Released placement is Dark Forest. |
| Blighted Balsa boats | INTENTIONALLY DEFERRED RELEASED IDs | Existing approved scope boundary; not implemented in this audit. |

## Runtime evidence

Prior records establish runtime work for Mushroom Colony generation/visuals,
Black Thistle, and other mushroom-related slices, but they do not prove the
complete released Mushroom Fields resource chain in the current tree. The
minimum future Prism test is a fresh Mushroom Fields chunk showing each
released feature family, plus Glowfish spawning and the complete recipe/loot
surface after the missing data resources are restored.

## Stage 13A reconciliation

Stage 13A recommended Mushroom Fields because the current parity matrix
classified the family as remaining work. The recommendation was based on
source-tree inspection and was incomplete: the existing `build.gradle` already
included the released Mushroom Fields resource set from the pinned reference.
The old implication that Mushroom Fields was represented only by a small
generic fragment is also stale; current registrations and classes are
substantial.

The historical Stage 3 restoration record was correct about the packaged
chain, although its implementation record predates the current singular
resource layout. It is retained as historical evidence and is clarified here
by the current artifact inspection.

## Beach inventory

Beach is a separate, genuine missing released package. The final source
contains:

- `BEACH_BIOMES` tag (`biomemakeover:beaches`) and the released beach tag
  including vanilla beach membership.
- `HelmitCrabEntity`, a `CREATURE` entity sized `0.825 x 0.5`, tracking range
  12, with released AI, shell/player interaction, breeding/food behavior,
  sounds, renderer, animations, and `checkSpawnRules`.
- Natural Beach spawning: category `CREATURE`, weight 6, groups 2–5, on ground,
  `MOTION_BLOCKING`, with the released spawn predicate and
  `crab_spawnable_on` block tag.
- Raw Crab, Cooked Crab, Crab Chowder, food properties, recipes, entity loot,
  spawn egg, translations, textures, models, render layers, and sounds.

The current port has no Helmit Crab registration/class, crab items, beach tag,
spawn hook, crab block tag, recipes, loot, renderer, or crab resource surface.
Beach is therefore **MISSING**, with fresh-world testing required after
implementation. It is not the next stage because Mushroom Fields still has a
proven unresolved released worldgen/data chain.

## Overall remaining released backlog

### Partial

- Badlands final Ghost Town/archaeology and remaining fresh-chunk checks.
- Dark Forest final biome/ecology/resource checks outside accepted slices.
- Remaining shared released data surfaces where direct comparison still finds
  an absent execution path.

### Missing

- Complete Beach/Helmit Crab package.
- Ghost Town archaeology closure where not covered by an accepted stage.
- Any separately confirmed released interaction/data family absent from the
  current execution path; these require feature-specific audits, not bulk
  restoration.

### Runtime-validation-only

- Fresh-chunk Mushroom Fields distribution and Glowfish validation after the
  package is complete.
- Accepted Badlands/Dark Forest sub-slices whose implementation is static or
  runtime-open in the existing matrix.
- Mushroom House/Trader/Disk checks where the existing Stage 10A record calls
  for manual confirmation.

### Stale or non-gating audit noise

- The historical broad mismatch count (82) and old filename-based matrix
  rows, where later accepted stages or current-Minecraft adaptations resolved
  the behavior.
- The historical Stage 3 “complete chain restored” statement for the current
  tree, contradicted by the absent Mushroom Fields JSON resources.
- Black Thistle as a Mushroom Fields item; its released scope is Dark Forest.

### Deferred/out of scope

- Blighted Balsa boat/chest-boat IDs previously marked deferred.
- Mythas Trial Wing, Manor Vault, keys/fragments, economy/relic rewards,
  difficulty changes, and all other Mythas enhancements.

## Ordered work

1. **STAGE 13C — MUSHROOM FIELDS FRESH-WORLD RUNTIME VERIFICATION**:
   validate natural generation and the released dependent data/client surface
   in fresh Mushroom Fields chunks.
2. **STAGE 13D — BEACH / HELMIT CRAB RELEASED PARITY RESTORATION**:
   restore the crab ecology, food/drop/data/client surface, then test fresh
   beach chunks.
3. Ghost Town archaeology closure.
4. Remaining source-proven shared systems and final fresh-world/resource
   verification.

Mythas enhancements remain deferred until released parity is complete.

## Invariants

- Mansion: 168 total, 165 active, 3 released orphans.
- Orphans: `wall/outer/base/wall_outer_base_1.nbt`,
  `wall/outer/wall_outer_1.nbt`, `wall/outer/wall_window_3.nbt`.
- Mansion structure NBT changes: 0.
- Preservation tag remains `biome-makeover-1.21.10-pre-parity-reconstruction`
  at `d664cccf13ab65bddc7a3d30aa04254bb810e4f1`.
