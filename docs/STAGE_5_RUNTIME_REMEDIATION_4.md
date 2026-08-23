# Stage 5 Runtime Remediation 4: Released Swamp Gameplay Audit

## Evidence and scope

This checkpoint starts at `21502d550a5173d8e91125c1b1f976bdfb3f606a`. The behavioral authority remains
released BM 1.20.1-1.11.4 at `2f314c0596af095a4890995a465f308f69476b4a`. Showcase footage identified
relationships for renewed inspection, but was not used to combine older behavior with the final release.

## A-H dispositions

| Area | Disposition | Released-source evidence and implementation |
|---|---|---|
| A. Water Lily/Small Lily Pad item tint | FIXED STATICALLY; RUNTIME VERIFICATION REQUIRED | Both historical models use tint index zero and the provider applies a -20/+40/-20 shift to default foliage without world context. Their item definitions now supply exact ARGB `-13312764`. World rendering is unchanged. Magenta Bud is standalone untinted artwork and was untouched. |
| B. moving Lightning Bug illumination | SOURCE-CONFIRMED; NO MOVING WORLD LIGHT ADDED | Final source contains no light manager, light blocks, block-update loop, or dynamic-light integration. `aiStep` only sends Lightning Spark particles at a 1/200 chance and the renderer is full-bright. The placed Lightning Bug Bottle is explicitly light level 15 and is restored below. |
| C. Toad/Tadpole ecosystem | FINAL-RELEASE UNREACHABLE; NOT ACTIVATED | The only natural Toad spawn call is commented out. Toad/Tadpole eggs, Tadpole Bucket, and Raw/Cooked Toad are hidden/disabled; the egg tooltip says spawning was disabled in 1.19. Existing code establishes Slime Ball or Spider Eye—not Dragonfly Wings—as Toad food. Reproduction creates water Tadpoles which mature to Toads and can be accelerated with Slime Balls, but final 1.20.1 has no initial survival acquisition. |
| D. Dragonfly Wings | FINAL-RELEASE UNREACHABLE; NOT ACTIVATED | Canonical ID is `biomemakeover:dragonfly_wings`, but the item is hidden and final Dragonfly loot has no pools. No final recipe or Toad predicate references it. Dragonfly code was untouched. |
| E. Lightning Bug Bottle | RESTORED STATICALLY; RUNTIME VERIFICATION REQUIRED | A Glass Bottle becomes `biomemakeover:lightning_bug_bottle`, the bug is discarded, and bottle-fill sound plays. The waterloggable/up-or-down placed block emits level 15 and has released shapes, assets, loot, advancement, block entity, and client-only contained-bug renderer. Historical state stores no variant/NBT. Breaking drops the filled bottle; it does not release a bug. The separate Experience Bottle/throwable combat system remains deferred as a complete independent system. |
| F. underwater saplings | RESTORED STATICALLY; RUNTIME VERIFICATION REQUIRED | Released Willow/Cypress use `WaterSaplingBlock`, preserve source water, and have depth gates 1 and 3 respectively. Placement, random ticks, bonemeal and existing tree growers share the contract. |
| G. complete Swamp cross-audit | COMPLETE FOR STATIC FINAL-RELEASE REACHABILITY | See below. No Stage 6, dev-only, or older-showcase-only content was activated. |
| H. Mushroom Fields follow-up | DOCUMENTED ONLY | See `STAGE_3_MUSHROOM_FIELDS_FOLLOW_UP_AUDIT.md`. No Stage 3 production file changed. |

## Released Swamp cross-audit

| Original released feature | Current status | Disposition | Evidence |
|---|---|---|---|
| Willow/Cypress families, waterlogged saplings, leaves | Restored | COMPLETE STATIC / RUNTIME PARTIAL | Registrations, resources, features, leaf support/loot and source water-depth behavior traced. |
| Peat, reeds, flowers, pads/lilies, branches | Restored | COMPLETE STATIC / RUNTIME PARTIAL | Worldgen, interactions, recipes, loot and item/world tint traced. |
| Decayed | Restored | RUNTIME PARTIAL PASS | Natural spawn, wet behavior, baby layer, loot and progression restored. |
| Dragonfly | Restored | RUNTIME VERIFICATION REQUIRED | Natural weight 20/groups 3-8; final loot is empty. Wings are hidden/unobtainable. |
| Lightning Bug/alternate | Restored | RUNTIME PARTIAL PASS | Natural spawn and accepted geometry/pulse/color/sparks restored; no entity world light exists. |
| Glass-bottle capture and placed Lightning Bug Bottle | Restored now | RUNTIME VERIFICATION REQUIRED | Direct entity interaction plus block/block entity/renderer/level-15 light/loot/advancement is one reachable loop. |
| Experience Bottle capture / throwable Lightning Bottle | Not restored | DEFERRED INDEPENDENT COMBAT ITEM | Separate projectile/network/splash system, not the glass jar block loop; no placeholder added. |
| Toad/Tadpole, foods/bucket/eggs | Absent | FINAL-RELEASE UNREACHABLE | Spawn commented and all acquisition items hidden/disabled. |
| Dragonfly Wings | Absent | FINAL-RELEASE UNREACHABLE | Hidden item, empty final loot, no final food/recipe use. |
| Peat Composter, Witch progression, Sunken Ruins | Absent | REMAIN DEFERRED | Stage 9/10B/later ownership remains. |

## Registry and world safety

The restoration additively registers `biomemakeover:lightning_bug_bottle` as block, item and block entity. No ID is
removed, renamed or repurposed. Existing Willow/Cypress sapling IDs gain their released waterlogged property; old
states load with false. No terrain or retro-generation behavior changes, and Taniwha is not introduced.

Stage 5 remains runtime-open pending comprehensive Prism, soak, persistence, dedicated-server and world-copy tests.

## Comprehensive Stage 5 Prism acceptance checklist

1. Generate fresh Swamp and Mangrove Swamp terrain; inspect Willow/Cypress, peat/mossy peat, reeds/cattails/flowers,
   pads/lilies, branch ecology, bonemeal behavior, bee nests and terrain continuity.
2. Verify Willow/Cypress canopies persist. Remove trunks and test natural decay, sapling/stick drops, ordinary break,
   Shears, Silk Touch and Fortune without leaf-item storms.
3. Inspect Willow/Cypress leaves plus Water Lily and Small Lily Pad in world, inventory, hotbar, hand and dropped form.
   Place both water-surface plants manually and exercise released Small Lily Pad stacking.
4. Place Willow and Cypress saplings on land and underwater. Test survival, water preservation, depth limits, random
   growth and bonemeal at valid/invalid water depths, then inspect generated trees.
5. Exercise every restored wood/decorative family, signs, doors/trapdoors, fences/walls, peat tilling, Willowing Branch
   growth/shearing, recipes, loot and advancements.
6. Observe Decayed adults/babies, water seeking/breathing, combat, loot and persistence. Verify Faster Farmland from
   peat tilling and no spawn-egg false trigger.
7. Observe Dragonflies for extended survival/movement, variants, sound and save/reload. Final released Dragonflies
   should not drop Wings.
8. Confirm accepted Lightning Bug geometry, pulse/color and sparks. Check that moving bugs themselves do not mutate
   world light. Capture one with a Glass Bottle, verify removal/item/advancement, place it floor/ceiling/underwater,
   inspect contained bug rendering and level-15 illumination, then break/re-place and save/reload. There is no released
   bug-release interaction for this block.
9. Confirm Toad/Tadpole and Dragonfly Wings do not naturally appear in final-release parity; do not treat their absence
   as a failed Stage 5 test.
10. Remain in Willow-heavy terrain for several minutes while monitoring ItemEntities and server tick stability. Save,
    reload, leave/re-enter, and restart the client/world.
11. Boot a dedicated server, join/rejoin, repeat capture/placement and generate fresh Swamp chunks; check registry sync,
    block-entity persistence, lighting and client-only classloading.
12. On a COPY only, load the Mythas world, inspect existing BM/Owl/Cowboy content, generate fresh Swamp/Mangrove
    chunks, exercise new additive IDs, save/restart, and inspect registry/data diagnostics.
