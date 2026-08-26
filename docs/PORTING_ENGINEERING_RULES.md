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

## 30. Entity attachments belong inside the transformed layer pipeline

Historical living-entity render layers inherited the renderer's entity orientation, scale, animation setup, and model
pose before applying a parent-part transform. A 1.21.10 attachment submitted after `super.submit` starts again from the
outer entity pose and will visibly detach even if its local offsets are identical. Port such attachments as modern
`RenderLayer` submissions and retain the historical parent-part transform order.

## 31. Growth predicates are worldgen termination contracts

When a generated plant also random-ticks or accepts bonemeal, compare its historical growth-origin predicate as part
of the generation algorithm. Dropping a substrate check can turn every exposed segment into a new recursive origin,
producing unbounded-looking structures without changing the nominal feature height or recursion probabilities.

## 32. Do not infer missing visual detail when the released texture omits it

Before adding an eye, overlay, tint, or extra model layer, compare the exact released texture pixels, historical model
UVs, historical renderer layers, and the modern vanilla equivalents. A visually surprising omission may be authored
released content rather than a migration loss. Record it explicitly and do not manufacture detail without evidence.

## 33. Test constructor, command, and finalized spawn paths independently

Entity construction, `/summon`, natural spawn, patrol/event spawn, and structure spawn need not call the same
finalization hooks. Rendering can also expose unconditional historical state not represented by equipment. Trace and
test each reachable creation path; do not force patrol-only mounts, leaders, or equipment onto direct summons.

## 34. Translate progression effects at their modern semantic boundary

When vanilla progression changes (for example Bad Omen becoming the precursor to Raid Omen), preserve the historical
player-visible result through current effects and criteria rather than globally reverting vanilla raid code. Confine
the adaptation to the custom feature's trigger and retain current vanilla behavior outside that scope.

## 35. Wearability and armor geometry are separate modern contracts

Historical custom armor renderers could replace vanilla armor geometry while an item still supplied equipment stats.
In the component-based equipment pipeline, assigning a vanilla equipment asset silently restores that vanilla model.
For custom wearable geometry, preserve equip slot, attributes, durability, repair, and equip sound independently, omit
the unwanted vanilla asset, and render the historical model from an equipped-item-aware client layer.

## 36. Runtime hooks must enter production paths

A deterministic test command must invoke the same production method or service used by normal gameplay. It must not
reconstruct look-alike state in parallel. Keep such hooks operator-only, inert unless invoked, clearly documented and
scheduled for removal after their runtime gate is accepted.

## 37. Port modern vanilla progression at its data-driven trigger

Do not assume a changed vanilla reward remains entity death code. Trace predicates, loot tables, functions, item
components, advancement criteria and later effect transitions. When a custom visual identity prevents a vanilla
predicate from recognizing the entity, restore that identity narrowly and mirror the authoritative vanilla data path;
do not combine the modern reward with the superseded legacy effect.

## 38. Custom wearable rendering must suppress duplicate head-item rendering

A component-based wearable can enter both custom armor rendering and the generic head-item path, especially when the
historical item subclass no longer exists. If correct 3D geometry is overlaid by flat inventory art, audit both paths
before changing UVs or textures. Use the rendering API's explicit default-head-item suppression contract and validate
that equipped and inventory textures remain independently bound.

## 39. Subclass entity types must inherit modern attachment metadata explicitly

Entity behavior formerly inherited from a vanilla superclass may move into `EntityType` dimensions and attachment
metadata. When registering a custom type whose entity subclasses a vanilla mob, compare current vanilla `eyeHeight`,
passenger attachments and vehicle/riding attachment—not only width and height. Missing metadata can compile and mount
successfully while positioning passengers incorrectly. Prefer the authoritative vanilla type contract over a runtime
Y-offset override.

## 40. Derive attachment corrections from model geometry

Before changing attachment transform order, trace the parent renderer's complete pose and verify whether the modern
layer actually begins in a different coordinate space. If the pipeline is equivalent but attachment centers differ,
derive the correction from named model-part cuboid bounds and pixel-to-model-unit conversion. Record failed runtime
theories, avoid visual tuning, and keep corrections isolated from shared geometry and textures.

## 41. Custom entity types do not inherit vanilla type tags

Modern behavior may move from a Java superclass into an `EntityType` tag. A custom entity class extending a vanilla
mob does not inherit membership held by the vanilla registry ID. Audit current type tags controlling breathing,
immunities, freezing, projectiles, or AI semantics and add the custom historical ID only when the released outcome
requires it.

## 42. Tint-indexed historical assets require explicit modern providers

Copying a model with `tintindex` does not copy its historical color-provider registration. Audit block and item tint
paths independently. A white model can be a missing provider rather than a bad texture; restore the released biome or
temperature color contract rather than recoloring artwork.

## 43. Validate resource semantics against registered runtime contracts

Schema-valid JSON can still reference an absent item or custom trigger. Validators must resolve BM recipe item IDs,
advancement parents/triggers, and versioned loot functions/conditions against the packaged registry/runtime contract.
When a functional system is deferred, defer its dependent data instead of creating a placeholder trigger or item.

## 44. Leaf support is a registry-tag contract

Modern leaf distance does not infer support from a log-shaped block or wood family. It assigns distance zero through
the block's membership in `minecraft:logs`. Every restored tree family must validate its log, stripped log, wood, and
stripped wood in the current block log tags before runtime generation. Missing membership can trigger canopy-wide
scheduled updates, decay loot, ItemEntities, and severe tick lag while the tree feature itself appears to generate.
Item log tags remain a separate crafting/fuel contract and must also be audited.

## 45. Block and item tint paths are independent data contracts

In 1.21.10 a block color provider does not tint the inventory model. Historical helpers that registered a single
provider for block and item must be translated into a block provider plus item-definition tint source. Preserve the
historical world/biome condition for placed blocks and the historical no-world/default color for inventory rendering.

## 46. Water-surface blocks may require a specialized item contract

A block inheriting vanilla lily-pad survival rules can generate correctly yet remain impossible for players to place.
Historical `PlaceOnWaterBlockItem` registration performs a fluid-aware raycast that a generic `BlockItem` does not.
Audit block construction and item construction separately when porting water-surface flora; preserve valid-fluid and
survival rules rather than broadening placement.

## 47. Convert nested item predicates, not only loot function names

Loot JSON may parse while retaining obsolete item and enchantment predicate shapes that alter tool-gated behavior.
Compare migrated Shears, Silk Touch, and Fortune tables with the current vanilla canonical table. In 1.21.10 this
includes scalar `items` and component-based `minecraft:enchantments` predicates. Validate the packaged result because
source-copy tasks may perform the translation after the source tree is inspected.

## 48. Showcase evidence must be version-resolved against final reachability

Showcase footage can reveal relationships missed by static categorization, but may represent an older release. Trace
the final branch's spawn calls, hidden/disabled acquisition items, loot, recipes and direct interactions before
activating it. Preserve older designed behavior as version-evolution evidence when final released gameplay explicitly
disconnects every survival entry path.

## 49. Separate entity full-bright, placed light, and dynamic world light

Full-bright models and particles do not update the world's light engine. A capturable entity may instead produce a
placed light-emitting block. Trace light-level properties, world mutations, cleanup and integrations before adding
temporary lighting; footage alone does not authorize a per-tick light-block system.

## 50. Tree-grower origin replacement is part of the feature contract

Modern `TreeGrower` may replace a sapling with its current fluid legacy block before invoking the configured feature.
Historical custom growers may instead have forced air or water independently of the sapling's placed state. Audit the
pre-placement origin state as well as survival and depth checks; a waterloggable sapling can place successfully while
both land and submerged growth silently fail under the wrong origin contract.

## 51. Prefer production feature commands over synthetic debug placement

When a data-driven feature is probabilistic, first use `/place feature` on its registered configured and placed keys at
a source-valid origin. This distinguishes implementation failure from sparse placement without adding a debug registry
or duplicating production logic. Natural fresh-chunk sampling remains necessary for density and injection validation.

## 52. Normalize provider codecs structurally across custom feature configs

Custom features often embed `IntProvider` values below fields that vanilla data generators never exercise. The 1.20
shape `{type: uniform, value: {min_inclusive, max_inclusive}}` no longer matches the 1.21.10 flattened provider
contract. Walk complete configured-feature documents by structure, not by a known field name, and validate the
packaged form. A feature codec can compile while every configured instance fails dynamic-registry loading.

## 53. Distinguish configured, checked and injected feature inventories

A final-release worldgen family may ship configured definitions, checked placed wrappers, selector placements and
dormant component placements while injecting only a smaller top-level set. Count and validate each layer separately.
Do not inject every shipped placed feature, and do not replace a selector chain with direct leaf features merely to
make registered resources visible.

## 54. Resolve configured-feature types, not only resource filenames

A packaged configured feature can exist and still make every world unloadable when its `type` names an unregistered
custom `Feature<?>`. Validate injection → placed feature → configured feature → Feature registry type, including
dormant packaged configurations that are not biome-injected.
