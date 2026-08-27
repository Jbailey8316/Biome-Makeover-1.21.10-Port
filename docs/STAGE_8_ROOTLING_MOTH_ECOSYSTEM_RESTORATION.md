# Stage 8 — Rootling and Moth Ecosystem Restoration

## Status

Static/package checkpoint. Runtime acceptance remains open. Stage 7 was closed and pushed at `50ce8856872217cb32574ce60dd100505238ad0a` before this work began.

## Authority and scope

The authority is released Biome Makeover 1.11.4 for Minecraft 1.20.1 at commit `2f314c0596af095a4890995a465f308f69476b4a`. The showcase audits were used to locate relationships, not to override final source.

Stage 8 owns the two reachable Dark Forest ecology systems:

- Rootling entity, six synchronized flower variants, personality goals, shearing, flower regrowth, rain/water acceleration, Bone Meal interaction, loot, seeds/crop reproduction, Bulbus Root foods, bud dyes, rendering, sounds, spawn egg and advancements.
- Moth entity, canopy spawn predicate, hostile flight/combat, Owl avoidance, released attraction behavior, Moth Scales, rendering, sounds, spawn egg, Nocturnal effect/potions/brewing and advancements.

It does not own Stunt Powder, Altar/curses, Ectoplasm/Ghosts, the dependency-gated Phantom Membrane recipe, Mansion content or any Stage 9+ system.

## Final Rootling contract

- Creature spawn in Dark Forest: weight 40, groups 2–6; ground placement, Grass Block substrate and raw brightness above 2.
- 10 health and 0.25 movement speed.
- Six variants in final order: blue, brown, cyan, gray, light blue, purple. Flower state/type and regrowth/action timers persist.
- Goal order: float 0, panic 1, seek rain 2, flee shears 4, Bone Meal temptation 5, dance 6, social follow 7, flower inspection 8, random stroll 9, player look 10, random look 11.
- Shearing drops 2–4 color-matched buds, consumes one shears durability, makes the Rootling bald and starts a uniformly selected 600–1200 tick regrowth timer.
- Water/rain gives the released extra one-in-five regrowth decrement. Bone Meal on a bald Rootling has a one-in-three immediate-regrowth chance.
- Rootlings do not conventionally breed. Rootling Seeds grow through five crop ages on farmland; maturity consumes the crop and produces a randomized Rootling.
- Death loot is 1–2 seeds plus Looting 0–1, and one Bulbus Root plus Looting 0–1; fire applies the historical furnace-smelt behavior.
- Bulbus Root: 2 nutrition/0.6 saturation. Roasted Bulbus Root: 5/0.8. Furnace, smoker and campfire recipes are restored.

## Final Moth contract

- Monster spawn in Dark Forest: weight 90, groups 2–3; unrestricted placement with motion-blocking heightmap, non-Peaceful darkness and leaves immediately below except spawners.
- 10 health, 0.6 flying speed and 0.25 movement speed; flying navigation, no fall damage and Owl avoidance.
- Goal order: float 0, attraction 1, melee 3, avoid Owl 4, flying wander 5; player target 1 and retaliation 2.
- Attraction candidates emit more than 10 light or belong to `biomemakeover:moth_attractive` (Moth Blossom). Final 1.20.1 source also gates the goal on a recorded attacker. That unusual reachable source behavior is preserved rather than silently “fixed” from showcase narration.
- Loot is 0–2 Moth Scales plus Looting 0–2.
- Awkward Potion + Moth Scales produces `nocturnal_pot` (72,000 ticks); Redstone produces `long_nocturnal_pot` (144,000 ticks). Nocturnal resets the affected player's time-since-rest every 20 ticks, preventing phantom eligibility through vanilla insomnia state.

## Modern translations

- Historical tracked data uses 1.21.10 `SynchedEntityData.Builder`; persistent state uses `ValueInput`/`ValueOutput`.
- The crop uses the modern crop growth/bonemeal callbacks and the production Rootling type.
- Shearing uses the 1.21.10 server-authoritative `Shearable` contract.
- Entity renderers explicitly extract Rootling flower state into a render state; the released base/flower overlay textures and model geometry are retained.
- Brewing uses Fabric's 1.21.10 brewing-registry build callback and registered potion holders.
- Looting uses `minecraft:enchanted_count_increase` with `minecraft:looting`.
- Spawn eggs receive generated 1.21.10 inventory textures while preserving released primary/secondary colors.

## Dependency classification

| Content | Classification | Disposition |
|---|---|---|
| Rootling, six buds, seeds/crop, Bulbus foods | Direct reachable Stage 8 | Restored |
| Moth, scales, Nocturnal effect/potions | Direct reachable Stage 8 | Restored |
| Illunite | Earlier Stage 6 dependency | Existing, unchanged |
| Stunt Powder | Later progression | Deferred |
| Moth Scales + Ectoplasm → Phantom Membrane | Final recipe, missing later Ghost dependency | Deferred with owning dependency |
| Altar/curses/Mansion | Later stages | Excluded |

## Registry delta

- Blocks: +1 (`rootling_crop`, intentionally no direct block item).
- Items: +12 (six buds, Rootling Seeds, two Bulbus foods, Moth Scales, two spawn eggs).
- Entity types: +2 (`rootling`, `moth`).
- Sound events: +9.
- Mob effects: +1 (`nocturnal`).
- Potions: +2 (`nocturnal_pot`, `long_nocturnal_pot`).
- No existing ID was removed, renamed or repurposed.

## Runtime-open risks

- Natural spawn sampling and the final leaves/darkness predicates.
- Rootling social goal timing, shears avoidance, exact server/client flower synchronization, crop emergence and save/reload.
- Moth flight/attack/Owl avoidance, released attacker-gated attraction, scale loot and audiovisual presentation.
- Potion brewing, insomnia suppression and persistence.

No Java test suite exists; static/package validation cannot establish runtime AI or rendering acceptance.

## Static validation

- Parity validator: PASS (`blocks=260`, `items=271`, `entities=12`, `sounds=40`; worldgen remains `38/37/31`).
- Clean offline build: PASS; Gradle reports `test NO-SOURCE`.
- Packaged reference/deferred-content/Taniwha audits: PASS.
- Package totals: 873 models, 285 PNG textures, 271 item definitions, 328 recipes, 271 loot tables, 32 advancements, 78 tags and 75 OGG assets.
- A dedicated-server bootstrap was attempted, but the offline run configuration lacks cached `net.fabricmc:fabric-log4j-util:1.0.2`; no Minecraft process started. Prism remains the required registry/datapack/runtime bootstrap.
