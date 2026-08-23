# Stage 4 — Badlands Restoration

## A. Historical inventory and reachability

The behavioral source is `Lemonszz/Biome-Makeover` branch `1.20`, commit
`2f314c0596af095a4890995a465f308f69476b4a` (release `1.20.1-1.11.4`). Generated masonry
membership is checked against Taniwha `1.20.0-5.4.4`, commit
`ee029d785850d8b0ad8ba69bee4e069b03253afe`. Stage 4 was reviewed against
`docs/PORTING_ENGINEERING_RULES.md` before and after implementation.

Reachable released Badlands gameplay comprises Paydirt, Saguaro Cactus, both Barrel Cactus states,
terracotta/cracked-brick masonry, four worldgen chains, Scuttlers, wind-driven Tumbleweeds, and Cowboy
patrol replacement. The deterministic contract is
`validation/foundations/stage_4_badlands_contract.json`.

| Content | Classification | Evidence |
|---|---|---|
| Scuttler | RESTORE-STAGE-4 | Natural Badlands spawn registration, weight 4, group 1–2. |
| Tumbleweed | RESTORE-STAGE-4 | Server tick spawner and default-true `BMdoTumbleweedSpawning` gamerule. |
| Cowboy | RESTORE-STAGE-4 | Badlands branch of released patrol-member injection. |
| Terracotta and cracked bricks | RESTORE-STAGE-4 | Registered and normally craftable released building families. |
| Suspicious Red Sand and archaeology | DEFERRED-BY-APPROVED-PLAN | Ghost Town acquisition and replacement processor are Stage 10C-owned. |
| Pottery sherds/patterns and Ghost Town disc | DEFERRED-BY-APPROVED-PLAN | Stage 10C progression rewards. |
| Dust Devil and later development experiments | DEV-ONLY | Not part of released parity. |

## B. Family contract

There are seventeen terracotta families: undyed plus all sixteen dye colors. Each contains exactly the
plural base (`terracotta_bricks`) and singular decoration suffixes (`terracotta_brick_slab`,
`terracotta_brick_stairs`, `terracotta_brick_wall`). Cracked Bricks use the released irregular naming:
`cracked_bricks` plus `cracked_brick_{slab,stairs,wall}`. No modern members were invented.

This restores 72 masonry blocks. With Paydirt, three cactus blocks, the no-item Tumbleweed render block,
and three no-item flowerpots, the Stage 4 block delta is 80. Ordinary block items number 76; three
standalone items and two spawn eggs bring the item delta to 81.

## C. Blocks, items, vegetation, and mechanics

- Paydirt retains gravel sound, terracotta-gray map color, correct-tool mining, and released loot data.
- Saguaro retains cactus contact damage, attachment state, arm geometry, random/bonemeal growth, and the
  released plantable-on tag contract.
- Barrel Cactus retains its compact collision shape, careful-walking exemption, young/tagged-item
  immunity, random flowering, flower particles, and cactus damage.
- Scuttler Tail, Cowboy Hat, and Cracked Brick use historical IDs. The tail's Antidote brewing/progression
  dependency remains with its cross-theme Witch/progression owner rather than introducing a partial potion.
- Transparent cacti are explicitly registered on the current cutout layer.

## D. Entities

### Scuttler

Registered at historical dimensions/category with 10 health and 0.25 movement speed; natural Badlands
spawning is weight 4, group 1–2, with a brightness/random spawn predicate. Goals cover swimming,
food-tag temptation, warning rattle, panic, breeding, non-passive player avoidance, flowered Barrel Cactus
seeking/eating, parenting, wandering, and looking. Food has the released one-in-three passive conversion,
then healing/breeding semantics. Passive state is persisted. Custom sounds, loot, spawn egg, texture,
geometry, and animation plumbing are packaged.

### Tumbleweed

The released MISC entity ID, no-item render block, default-true gamerule, Badlands player-proximity spawner,
bounded evolving wind system, gravity/bounce/water motion, lifetime, collision, block particles, and break/
tumble sounds are restored. The original entity had no custom persistent payload.

### Cowboy

The Pillager-derived entity, attributes, equipment, spawn egg, custom texture, mounted-horse patrol
replacement, leader state, and patrol targeting are restored. The historical horse-only hat render layer
and its horse `Hat`/`CowboySpawned` persistence fields still require runtime-safe 1.21.10 reconciliation;
they are not claimed as complete. Cowboy-Hat head rendering likewise requires visual runtime verification.

## E. World generation

| Configured/placed ID | Feature type | Injection step |
|---|---|---|
| `badlands/barrel_cactus` | vanilla random patch | `VEGETAL_DECORATION` |
| `badlands/saguaro_cactus` | `biomemakeover:saguaro_cactus` | `VEGETAL_DECORATION` |
| `badlands/paydirt` | `biomemakeover:paydirt` | `UNDERGROUND_DECORATION` |
| `badlands/surface_fossil` | `biomemakeover:surface_fossil` | `SURFACE_STRUCTURES` |

All four configured resources, all four placed resources, three custom feature registrations, and all four
Badlands-tag injections are present. Surface fossils use vanilla fossil templates and a local modern
structure-placement translation. Distribution/codec behavior needs runtime verification.

## F. Supporting data and packaged resources

Historical ownership-filtered blockstates, models, textures, ordinary recipes, stonecutting recipes, loot,
tags, three advancements (`enter_badlands`, `cacti`, `scuttler_tail`), sounds, and translations are copied
from the pinned release and translated to current singular data directories and item-definition format.
Generated item definitions are deterministic. Ghost Town/archaeology data is excluded.

## G. Registry and existing-world safety

Before: 100 blocks, 100 items, 2 entities, 8 sounds, 28 configured features, 25 placed features, 14
injected feature keys. After: 180 blocks, 181 items, 5 entities, 14 sounds, 32 configured features, 29
placed features, 18 injected keys. This is additive: no existing ID was removed, renamed, or repurposed.

Placed features affect newly generated Badlands terrain only. No retro-generation or existing-chunk rewrite
exists. The gamerule and runtime spawner can create Tumbleweeds around players in already-generated
Badlands, matching released runtime behavior. Existing worlds should be tested only on a copy.

## H. Engineering-rules compliance and validation

- COMPILES: PASS (`clean build --offline`).
- STATICALLY VALIDATED: PASS; registry/family/dependency/resource validation reports 16 pre-existing or
  explicitly grandfathered warnings.
- PACKAGED VALIDATED: PASS for critical Stage 4 classes, mixin configuration, worldgen, recipes, loot,
  tags, models, item definitions, textures, sounds, and advancements.
- CLIENT RUNTIME VALIDATED: NOT EXECUTED.
- DEDICATED SERVER VALIDATED: NOT EXECUTED; common source has no client imports and client registrations
  remain in the client entrypoint.
- MULTIPLAYER, SAVE/RELOAD, and EXISTING-WORLD VALIDATED: NOT EXECUTED.
- Tests: `NO-SOURCE`.

Stage 3 runtime debt remains open: data-pack decode/distribution, transparent/sign/UV visuals, Glowfish
render/bucket synchronization, advancement triggers, entity persistence, boat infrastructure, dedicated
server, save/reload, multiplayer, and existing-world-copy testing.

## I. Manual runtime checklist

Client/single-player:

1. Launch a new test world and run `/locate biome minecraft:badlands`.
2. Generate fresh terrain and inspect all four features at representative distances; use
   `/place feature biomemakeover:badlands/saguaro_cactus`, `/place feature biomemakeover:badlands/barrel_cactus`,
   `/place feature biomemakeover:badlands/paydirt`, and `/place feature biomemakeover:badlands/surface_fossil`.
3. Use `/give` for each masonry base/slab/stair/wall, Paydirt, cacti, Scuttler Tail, Cowboy Hat, and Cracked
   Brick. Check placement, wall connections, mining/loot, recipes, stonecutting, transparency, and UVs.
4. `/summon biomemakeover:scuttler`; test avoidance, rattling, food-tag temptation, passive conversion,
   healing, breeding, flower eating, drops, sounds, animation, save/reload, and restart persistence.
5. `/summon biomemakeover:tumbleweed`; test wind, bounce, water, damage, particles, sounds, lifetime, and
   `/gamerule BMdoTumbleweedSpawning false|true`.
6. `/summon biomemakeover:cowboy`; verify texture, hat, equipment, horse riding, patrol generation, leader
   behavior, loot, despawn, and save/reload.
7. Regress Mushroom Fields/Glowfish/bucket, current Dark Forest, and current Owl behavior.

Dedicated server: boot, join/rejoin, generate Badlands, test entity and interaction synchronization,
save/restart, patrol mixin, payload/registry synchronization, and absence of client classloading. Existing
world copy: load without registry warnings, inspect existing BM blocks/items/Owl, generate fresh Badlands,
save/reload/restart, and inspect logs for registry/datafix/chunk errors.

The offline development server remains NOT EXECUTED if the cached `fabric-log4j-util:1.0.2` dependency is
still unavailable; dependencies must not be changed solely to run it.

## J. Known deviations and Stage 5 entry criteria

Runtime validation is mandatory before declaring visual/behavioral parity. Horse leader hats and associated
horse persistence are a known static gap. Scuttler flower-eating loot-table output and exact legacy animation
timing need runtime/source follow-up; registered support data is packaged. Antidote is deferred to its Witch/
progression owner, and all archaeology remains Stage 10C.

Stage 5 entry requires a clean tree after the focused Stage 4 commit, passing clean offline build, parity and
family validators, unchanged preservation tag, reviewed runtime debt, and no unapproved Ghost Town content.
