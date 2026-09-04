# Mythas Mansion Trial and Boss Enhancement

Status: design only. Implementation is deferred to Stage 12.

This document describes a proposed **MYTHAS ENHANCEMENT** for the released
Biome Makeover Mansion. It is not released Biome Makeover parity. It must not
change the accepted Mansion templates, world-generation behavior, or current
R20 runtime behavior until a separate implementation stage is audited and
approved.

## Released Biome Makeover behavior

The accepted parity baseline remains documented in
[the Mansion parity closure](../STAGE_11B1R20R11_MANSION_PARITY_CLOSURE.md) and
[the Mansion marker audit](../STAGE_11B1_MANSION_MARKERS.md).

The following released behavior is preserved by this design:

- the `boss` data marker finishes as AIR;
- each `arena_pos` data marker finishes as smooth quartz;
- the visible quartz arena remains in place for now;
- Mansion templates, placement, terrain, loot, crops, fluids, and the closed
  R20 lifecycle are not redesigned here.

No Trial Spawners, fragments, Emerald Keys, activation rules, or new rewards
are claimed to be part of released parity.

## MYTHAS ENHANCEMENT: progression model

Each Mansion would contain three distinct Trial Spawner encounters:

1. **Patrol Trial** — primarily Pillagers.
2. **Enforcer Trial** — Pillagers and Vindicators.
3. **Captain Trial** — a harder raider composition; the exact mix is deferred.

Each encounter awards a different personal fragment:

- Patrol Fragment
- Enforcer Fragment
- Captain Fragment

A fragment is associated with both the player and the Mansion where it was
earned. A player can obtain each fragment once per Mansion, but not only once
globally. The three fragments combine only when they all belong to the same
Mansion, producing an **EMERALD KEY**. Fragments from different Mansions must
never combine.

This makes progression repeatable by Mansion: a player may earn a key at
Mansion A and another at Mansion B, keeping newly discovered Mansions useful
in a persistent SMP.

## Mansion identity and personal state

Progression is keyed by:

`player UUID + Mansion identity`

The preferred initial Mansion identity is dimension plus the Mansion
structure origin. An implementation may use another stable equivalent only
after proving that it remains stable across reloads, unloaded chunks, server
restarts, and multiple Mansions in the same dimension.

The state model must distinguish at least:

- earned fragment types for each player/Mansion pair;
- consumed Emerald Keys;
- the player who starts an encounter;
- the players qualifying for participation rewards;
- whether the once-per-Mansion helper reward has been delivered.

No global Mansion completion flag may exhaust every player’s progression.

## MYTHAS ENHANCEMENT: boss activation

If technically practical, the existing boss-room Trial Spawner should be the
activation device. The intended interaction is:

`Emerald Key -> use on boss-room Trial Spawner -> consume that player's key -> start one encounter`

Only one encounter may be active for a boss Trial Spawner at a time. The key
holder starts that run, while the nearby group may participate. If four
players each have a valid key, the group may run four encounters, one key at a
time, after each previous encounter completes.

The boss room remains the existing room. This design does not authorize
template edits, arena removal, boss-fluid changes, or new world-generation
behavior.

## Group-friendly rewards

The key holder receives the primary boss reward, provisionally called a
**Manor Cache**. Its contents and loot pool are deferred.

Qualifying helpers must receive a personal participation reward rather than
being left to compete for a shared drop. Candidate names are **Manor Seal**,
**Adjudicator's Mark**, and **Emerald Sigil**; the final name is deferred.

The recommended eligibility rule is once per player per Mansion for the
special helper reward. A player may continue helping with later runs at that
Mansion, but helping must not multiply the special reward indefinitely.
Normal mob drops and experience may continue to follow ordinary game rules.

Important rewards should be attributed per player, using one of these future
delivery models:

- direct delivery to the eligible player;
- a personal Manor Cache;
- a personal reward token redeemed at the boss Trial Spawner.

The preferred theme is: the Emerald Key grants permission to start an
encounter, while the Manor Seal (or final equivalent) proves participation;
the boss Trial Spawner acts as encounter controller and may dispense rewards.

## Existing boss markers

The accepted marker semantics remain:

| Marker | Accepted result |
| --- | --- |
| `boss` | AIR |
| `arena_pos` (15 markers) | smooth quartz |

The quartz arena must not be removed at this design stage. During future
implementation, the existing marker positions may be audited as metadata for
the boss spawn position, movement points, add spawn points, effects, or arena
boundaries. Their final use must follow an audit of released Stage 12 boss
behavior; this document does not assume that use.

## Mansion-exclusive loot direction

Future Mansion rewards should favor horizontal, collectible, cosmetic,
utility, provenance, and sidegrade value. Avoid gear score, stat inflation,
mandatory power progression, and a loot treadmill.

Candidate rewards for later evaluation include:

- the original Biome Makeover Enchanted Totem;
- a Mansion-exclusive armor trim or smithing template;
- Mansion tapestries or collectible variants;
- Red Rose music disc integration;
- a manor trophy, crest, or banner;
- restrained raider-themed equipment sidegrades;
- future Relic-system integration;
- Manor Cache contents;
- rare decorative or building rewards.

Loot probabilities, power levels, and final item selection are intentionally
not locked.

## Design principles

The enhancement should reward exploration, keep multiple Mansions relevant,
work in persistent worlds, support groups naturally, avoid globally consuming
a Mansion, reward helpers fairly, prevent infinite major-loot farming, and
remain understandable as Minecraft gameplay. It should reuse vanilla Trial
Spawner behavior where practical and avoid a new dependency or custom system
unless an audit demonstrates that one is justified.

## Required audits before implementation

The following are gates for a future Stage 12 implementation and are not
resolved by this design document:

1. Audit released Biome Makeover 1.20.1 boss behavior, including the `boss`
   marker, `arena_pos`, Adjudicator, Stone Golem phases, Enchanted Totem, and
   boss rewards.
2. Audit Minecraft 1.21.10 Trial Spawner APIs and data format.
3. Determine whether Pillager/Vindicator encounters can be configured mainly
   through vanilla Trial Spawner data.
4. If source and license permit, inspect an installed one-reward-per-player
   Trial Spawner implementation, including the End Ship Elytra mechanic, as a
   reference. Do not add a dependency without justification.
5. Prove the stable Mansion identity and persistence format before storing
   player progression.
6. Define encounter completion, participant qualification, disconnect,
   death, reload, unload, and concurrent-use behavior.

## Explicitly deferred decisions

The following remain open: exact Trial Spawner wave composition and settings;
Captain Trial mob mix; fragment item implementation and recipe UI; key
stacking and transfer rules; boss entity and phases; activation radius;
encounter timeout and reset rules; participation radius and qualification
window; reward names and delivery; loot probabilities and power; marker use
beyond accepted parity; and any required custom code or dependency.

## Stage 12 guardrails

Any future implementation must remain separate from released parity, preserve
the accepted marker results and templates, scope all progression by player and
Mansion, prevent a second active encounter on one boss spawner, attribute
important rewards personally, and include migration/reload tests. Until those
audits and tests exist, the Mythas Enhancement is design-only.

For the prior item-registration substrate and its Stage 12 boundary, see
[Stage 12A.0](../STAGE_12A0_ITEM_SUBSTRATE.md).
