# Biome Makeover 1.21.10 Released-Parity Audit

Audit date: 2026-09-06  
Authoritative reference: final released Biome Makeover 1.20.1 source in `reference/Biome-Makeover-1.20/`  
Port baseline: Stage 12A.11 frozen baseline `1456da7bea694fd9616d2b0ce085b3ce73bf0094`  
Preservation tag: `biome-makeover-1.21.10-pre-parity-reconstruction` → `d664cccf13ab65bddc7a3d30aa04254bb810e4f1`

## Executive summary

Stage 13A makes the localization surface complete for the registered current
port and records the remaining released-parity backlog. The released English
file contained 629 keys; the port contained 230 before this stage. The merged
current file contains the released keys plus five current-only keys, with the
requested current user-facing value `item.biomemakeover.enchanted_totem` set to
`Enchanted Totem`.

This document is an audit and work-order record. It does not implement any
feature listed as partial or missing. Mansion and Adjudicator parity remains
complete/frozen, and the independent Stone Golem and Mimic systems remain
complete/frozen.

## Localization closure

The durable validator is `validation/Invoke-Stage13ALocalizationValidation.ps1`.
It extracts current BM item, block, entity, advancement, and custom component
translation references and requires English coverage. Explicit exceptions are
limited to vanilla-derived display names and internal non-display registry
keys.

The corrected Totem key is:

```text
item.biomemakeover.enchanted_totem = Enchanted Totem
```

Released advancement, subtitle, item, block, entity, effect, enchantment,
tooltip, creative-group, and item-component English keys were restored without
changing IDs, criteria, recipes, components, or gameplay.

## Current frozen systems

These are not remaining work for released parity unless a regression is found:

- Mansion generation and template inventory: 168 total, 165 active, and the
  three released orphans `wall/outer/base/wall_outer_base_1.nbt`,
  `wall/outer/wall_outer_1.nbt`, and `wall/outer/wall_window_3.nbt`.
- Adjudicator controller, all released phases, Mimic phase, mounted Stone
  Golem phase, encounter alliance, death lifecycle, Enchanted Totem, and
  Tapestry reward.
- Independent Stone Golem construction, AI, crossbow, rendering, persistence,
  recipes, and recipe-book unlocks.
- Owl, Rootling/Moth, Swamp ecosystem, Badlands core restoration, Dark Forest
  physical pipeline, and the previously runtime-accepted stage slices recorded
  in the existing parity matrix.

## Current-Minecraft compatibility adaptations

The port contains narrow adaptations required by 1.21.10 APIs, including
modern item-definition resources, singular data-resource conventions,
data-component based item behavior, current death-protection hooks, current
crossbow/projectile APIs, and mounted Stone Golem goal/control compatibility.
These preserve released results and are not counted as missing features.

## Released feature-group classification

At the coarse family level used by this document, 35 audit rows are classified:
8 complete/frozen, 3 complete but runtime-open, 5 partial, 7 missing, 5
current-Minecraft compatibility adaptations, and 7 intentionally deferred or
out-of-scope rows. These are family rows, not a replacement for the older
125-unit capability denominator.

The prior detailed matrix remains the source of the 125-unit capability
allocation; this current audit reconciles it against the present tree and
frozen stages rather than repeating historical filename counts.

| Group | Current status | Evidence / boundary |
|---|---|---|
| Mansion + Adjudicator | COMPLETE / FROZEN | Stages 11B and 12A.4–12A.11 runtime accepted and pushed before this audit. |
| Independent Stone Golem + Mimic | COMPLETE / FROZEN | Stages 12A.8–12A.9 runtime accepted and frozen. |
| Swamp ecology and accepted Sunken Ruins slice | COMPLETE / FROZEN | Existing stage records and current resources/code. |
| Owl / Rootling / Moth | COMPLETE / FROZEN for owned scope | Existing runtime-accepted stage records; disabled final-release content remains excluded. |
| Badlands core blocks/ecology/Cowboy | COMPLETE / NOT RUNTIME VERIFIED in every sub-slice | Ghost Town remains separate. |
| Dark Forest physical/worldgen slice | COMPLETE / NOT RUNTIME VERIFIED in every sub-slice | Dormant/current-only content is not silently promoted to released parity. |
| Localization | CURRENT-MC COMPATIBILITY ADAPTATION | Closed by this stage; validator is durable. |
| Historical broad parity script | INTENTIONALLY NON-GATING | Its 82 historical mismatches predate the completed later stages and include stale scope assumptions. |

## Remaining PARTIAL released features

These are meaningful released families with some current implementation but an
unclosed released behavior boundary:

1. Mushroom Fields makeover: generic wild-mushroom coverage exists, but the
   complete released vegetation, Blighted Balsa, glowshroom masonry, and biome
   integration are not complete.
2. Badlands final closure: Ghost Town archaeology/structure/loot remains
   separate from the restored Badlands core; some spawn and fresh-chunk checks
   remain runtime-open.
3. Dark Forest final closure: some released biome modifier differences,
   dormant registrations, and remaining client/ecology checks are not a claim
   of full parity.
4. Shared data surface: recipes, loot, tags, advancements, sounds, particles,
   and client resources outside the accepted stage scopes remain only partial
   where the current tree does not provide the released execution path.
5. Current registry/content breadth: the current tree has registered families
   that are not all equivalent to the complete released surface; direct source
   mapping is required before calling each family complete.

## Remaining MISSING released features

The following released families are not implemented as complete released
systems in the current port and must not be inferred from similarly named
resources:

- Complete Mushroom Fields wood, glowshroom, masonry, and associated worldgen
  families.
- Complete Beach/Helmit Crab released ecology, food/drop chain, spawning,
  renderer, sounds, and loot where not covered by a frozen stage.
- Ghost Town archaeology structure, processors, suspicious-block behavior,
  templates, and structure loot.
- Released final-release Toad/Tadpole content where the authoritative source
  leaves it disabled or otherwise outside the required reachable surface;
  this is source-audit scope, not permission to invent natural spawning.
- Any released block entity, menu, witch quest, Poltergeist/soul, particle,
  network, or special interaction whose current equivalent is absent from the
  registered execution path.
- Any remaining released recipe/loot/tag/advancement/resource entry that is
  absent after the current source-level comparison; these require feature-
  specific stages and must not be restored wholesale from raw file counts.

The exact source evidence for these boundaries is in the existing
`docs/BIOME_MAKEOVER_1_21_10_PARITY_MATRIX.md`,
`docs/ORIGINAL_BIOME_MAKEOVER_MASTER_FEATURE_AUDIT.md`, and
`docs/CURRENT_PORT_PRE_PARITY_MANIFEST.md`; those documents are preserved as
historical evidence rather than rewritten.

## Runtime-validation backlog

- Re-test every “complete / not runtime verified” biome/worldgen sub-slice in
  fresh chunks.
- Verify remaining Badlands Scuttler/Cowboy/Ghost Town boundaries.
- Verify Dark Forest feature density and dormant resource reachability.
- Verify non-frozen resource families after any future feature restoration.

## Historical audit reconciliation

The old matrix’s headline 82 historical non-gating mismatches are not a current
missing-feature count. They were produced against earlier baselines and broad
filename/registry assumptions. They include systems subsequently accepted in
Stages 11B and 12A, modern singular-resource/API adaptations, and deferred or
unreachable final-release content. This audit keeps that result documented as
historical and non-gating; no historical document was destructively rewritten.

## Ordered remaining released-parity work

1. **P0 — authoritative feature inventory and dependency lock:** use this
   document plus direct source comparison to select one family at a time;
   preserve frozen Mansion/Adjudicator/Stone Golem systems.
2. **P1 — Mushroom Fields completion:** Blighted Balsa and glowshroom families,
   recipes/loot/tags, configured/placed features, and biome integration. Fresh
   world testing is required.
3. **P1 — Beach/Helmit Crab completion:** entity behavior, spawning, food/drop,
   sounds, rendering, and reachable ecology. Fresh-world testing is required.
4. **P1 — Ghost Town archaeology closure:** suspicious blocks, processors,
   templates, structure placement, loot, and brush interactions. This changes
   only new chunks but needs dedicated structure/archaeology validation.
5. **P2 — remaining released shared systems:** Poltergeist/soul, witch quest
   systems, block entities/menus, particles/networking, and any source-proven
   missing interaction/data families.
6. **P3 — final client/resource and fresh-world verification:** close dormant
   assets only when a released registration/execution path is proven.

## Recommended next stage

**STAGE 13B — MUSHROOM FIELDS RELEASED PARITY RESTORATION**

This is the next foundational released family: its wood and glowshroom
registrations feed blocks, recipes, loot/tags, configured/placed features, and
biome injection. It is independent of the frozen Mansion and Adjudicator
systems and can be validated in fresh Mushroom Fields chunks. Ghost Town,
Helmit Crab, witch/Poltergeist systems, and all Mythas enhancements remain out
of scope.

## Explicitly deferred Mythas enhancements

Mansion Trial Wing, Manor Vault, keys/fragments, economy/relic rewards,
difficulty/rebalance work, Owl-nest experiments, and other current-only Mythas
content are not released parity and do not count against the released backlog.

## Safety invariants

- Mansion inventory remains 168 total / 165 active / 3 released orphans.
- Structure NBT changes: 0.
- Preservation tag remains unchanged.
