# Biome Makeover Porting Engineering Rules

This is the mandatory engineering contract for restoration Stages 4–13 and future Biome Makeover maintenance.
Historical released Biome Makeover remains the behavioral specification; lessons from other ports inform process
and validation only. Every stage document and completion report must state that the work was reviewed against this
contract.

## 1. Validation states are independent

Track these states separately: **COMPILES**, **STATICALLY VALIDATED**, **PACKAGED VALIDATED**, **CLIENT RUNTIME
VALIDATED**, **DEDICATED SERVER VALIDATED**, **MULTIPLAYER VALIDATED**, **SAVE/RELOAD VALIDATED**, and
**EXISTING-WORLD VALIDATED**. Never infer a later state from an earlier one. `BUILD SUCCESSFUL` does not prove
gameplay correctness, and an unperformed manual test is never reported as passed.

## 2. Source first, patch second

Before implementing or correcting historical behavior: locate the original implementation; trace registration,
resources, and execution; establish observable behavior; identify 1.20.1-to-1.21.10 API differences; then implement
the smallest faithful translation. Do not repeatedly patch symptoms. Uncertain behavior is marked for verification,
not guessed.

## 3. Presence is not reachability

Classify content as **REGISTERED**, **REFERENCED**, **REACHABLE**, **NATURALLY OBTAINABLE**, **COMMAND-ONLY**,
**HIDDEN**, **UNFINISHED**, **DEAD**, or **DEV-ONLY**. A class, texture, JSON, ID, or translation alone proves
nothing about released reachability. Preserve historical reachability and never activate dead content accidentally.

## 4. Dedicated-server safety is first-class

Audit sided classloading in every Java stage. Common/server code must not eagerly load renderers, models, layers,
screens, keybindings, client networking/registries, client singletons, or client-only helpers. Single-player success
does not establish dedicated-server safety.

## 5. Networking is explicitly sided

For every payload record sender, receiver, registration side, codec, lifecycle, and required state. Test or document
dedicated server, initial join, rejoin, synchronization, stale/missing state, and payload registration. Unknown
payloads or side-mismatched registration are release blockers.

## 6. Audit the production JAR

Minecraft consumes the built JAR, not the source tree. Inspect applicable packaged metadata, mixins, recipes, loot,
tags, advancements, blockstates, models, item definitions, textures, sounds, worldgen, structures, templates, and
processors. Source presence is insufficient.

## 7. Use current pack metadata

Validate Minecraft 1.21.10 resource/data-pack metadata and formats. Never allow historical metadata to overwrite
known-good current metadata.

## 8. Validate transparent rendering explicitly

Transparent PNGs do not imply correct rendering. Plants, leaves, ladders, fungi, doors/trapdoors, overlays, and
unusual geometry require current render-layer review and visual runtime tests.

## 9. Tags are gameplay contracts

Trace each historical tag dependency and map its semantics to 1.21.10. Tags may govern mining, climbing, drops,
recipes, AI, spawning, replacement, worldgen, connections, tools, and compatibility; migration is not clerical.

## 10. Complete every registration chain

- Worldgen: configured feature → placed feature → bootstrap → biome injection → placement → observed generation.
- Structures: template → processor → piece/pool/structure → registration → placement → observed generation.
- Entities: type → attributes → restriction → reachability/injection → AI → renderer/model → loot → sounds → persistence.

One component never proves a system complete.

## 11. Entity parity is multidimensional

Independently validate ID, attributes, predicate, weight/group, biome, navigation, goals, targets, interactions,
food/taming/breeding where relevant, persistence, drops, sounds, model, renderer, animation, variants, reachability,
spawn egg, and synchronization. Summonability alone is not parity.

## 12. Historical textures may not fit modern UVs

For vanilla-derived or changed models compare UV layout, dimensions, faces, eyes, overlays, variants, and babies;
then verify visually at runtime. Successful loading is insufficient.

## 13. Persistence requires reload tests

State-bearing systems must be created/changed, saved, quit, reloaded, verified, restarted, and verified again.
Prioritize entities, ownership/taming, block entities, inventories, timers, references/positions, custom state,
structures, and components.

## 14. Existing-world data is sacred

Prefer additive restoration. Do not rename or repurpose persistent IDs or fields without migration review and
explicit approval. Consider blocks, items, entities, block entities, chunks, structures, worldgen, NBT, and
components. Never silently invalidate Mythas world data.

## 15. Preserve correct modern implementations

Observable historical behavior—not old source shape—is the target. Reuse current 1.21.10 compatibility work when
it is behaviorally correct. Keep Mythas enhancements separately classified.

## 16. Do not fix unrelated defects mid-stage

Document unrelated findings and assign an owning stage. Intervene immediately only for a build blocker, data/world
corruption, registry corruption, dedicated-server crash, or critical dependency blocker. Avoid scope expansion.

## 17. Use small testable checkpoints

Complex systems use logical checkpoints. After each meaningful checkpoint: build, validate, compare registries,
audit packaged resources, record runtime requirements, and commit when appropriate. Avoid giant unvalidated patches.

## 18. Runtime failures require root-cause analysis

Capture reproduction, expected and actual results, `latest.log`, crash report, and client/server context. Trace the
owning system before fixing; do not issue repeated speculative builds.

## 19. Audit recipe and conditional semantics

Presence is insufficient when availability depends on configuration, progression, feature state, loader condition,
or another mechanic. Preserve historical conditions and invent none.

## 20. Maintain evidence-backed parity status

At each stage update current implementation status against `docs/BIOME_MAKEOVER_1_21_10_PARITY_MATRIX.md` without
altering historical findings. Static parity and runtime-validated parity remain distinct.

## 21. Immediate application and Stage 3 debt

All Stage 4 planning and implementation is reviewed against this document. Stage 3 is not rewritten merely to
retrofit these rules. Its carried validation debt is explicit:

- client, dedicated-server, multiplayer, save/reload, and existing-world tests are not executed;
- development-server bootstrap is blocked offline by uncached `net.fabricmc:fabric-log4j-util:1.0.2`;
- Mushroom Fields data-pack decoding, distribution, transparent layers, sign atlas, historical texture/modern UV,
  Glowfish carried-block rendering, advancement trigger, persistence, and bucket synchronization need runtime checks;
- Blighted Balsa boats await faithful historical shared boat infrastructure;
- Mushroom House, possible Mushroom Trader reachability, and its disc remain Stage 10A.

No dependency is changed solely to eliminate this runtime-environment limitation.

## 22. Target every restored worldgen family in fresh chunks

A successful client bootstrap, resource reload, dynamic-registry load, world creation, and initial spawn do not
establish worldgen runtime safety. Feature placement code can remain dormant until an affected biome and feature are
actually generated. Every biome restoration checkpoint must therefore test targeted fresh chunks in every affected
biome and exercise each custom feature family.

Runtime reports must keep these gates distinct: client bootstrap; resource loading; dynamic registry loading; world
creation; initial-spawn generation; recipe/data decoding; advancement decoding; model/texture resolution; targeted
Mushroom Fields generation; targeted Badlands generation; feature-specific generation; entity behavior; save/reload;
and dedicated-server/multiplayer behavior. Passing one gate never implies a later gate passed.

## 23. Audit modern base attribute contracts with AI migrations

Adding a modern vanilla goal can add an attribute precondition that did not exist in the historical version. When a
goal is introduced or translated, inspect its current implementation and the current vanilla base attribute builder;
do not assume `Mob.createMobAttributes()` remains sufficient because it compiled historically. Audit sibling restored
entities for the same goal/attribute pattern. Missing attributes must be repaired through the correct modern base
contract or an evidence-supported explicit value, never exception suppression or removal of historical AI.

## 24. Preserve historical random-bound semantics

Utility names such as `randomRange` do not establish whether the upper bound is inclusive. Inspect the historical
helper and carry its exact bound semantics into modern RNG calls. Audit every call in recursive algorithms, where a
one-block error can become a large visual difference.

## 25. Render-state migrations must carry custom animation state

The extracted render-state pipeline does not implicitly expose custom entity fields. Custom renderers must define,
extract, interpolate, and submit every historical rotation, variant, attachment, visibility change, and lighting state.
A static model submission is not an animation port.

## 26. Audit new vanilla variants inherited by historical subclasses

When a historical entity subclasses vanilla, inspect systems added to that vanilla class after the source release.
New randomized variants, scales, dimensions, components, or renderer model selections must not silently alter the
custom entity. Pin the historical default only when source evidence proves it never used the newer system.

## 27. Treat render submission arguments as versioned API contracts

Do not carry numeric sentinels into a changed render API by position. In 1.21.10 the final `submitBlock` argument is
an outline color; `-1` is opaque white, not a neutral tint. Resolve every argument from current mappings/source and
use the current no-effect value. Audit sibling renderers whenever two unrelated models show the same flat-white
failure.

## 28. Preserve RNG stream ownership, not only numeric ranges

Matching inclusive/exclusive bounds is insufficient when historical code used a separate shared RNG. In recursive
worldgen, moving shape-range draws onto the feature RNG changes the later recursion rolls and can materially alter a
seed's total structure height. Trace which RNG instance owns every draw as well as its bounds and order.

## 29. Natural-spawn contracts include density suppression

Spawn weight and group bounds do not completely describe effective abundance. Preserve custom predicates, nearby-
entity exclusion radii, brightness/height/substrate rules, and `getMaxSpawnClusterSize`; validate direct summons and
natural/finalized spawns separately because setup paths can differ.
