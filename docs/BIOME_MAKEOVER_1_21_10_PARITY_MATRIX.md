# Biome Makeover 1.21.10 — Current Port vs Original Parity Matrix

Date: 2026-08-23  
Audit scope: static comparison and restoration planning only  
Current-port preservation point: `biome-makeover-1.21.10-pre-parity-reconstruction` (`d664cccf13ab65bddc7a3d30aa04254bb810e4f1`)  
Current baseline inspected: `849b82249277bd98ec030af153f22f6bdeae2afe`  
Historical specification: `Lemonszz/Biome-Makeover`, branch `1.20`, commit `2f314c0596af095a4890995a465f308f69476b4a`, Minecraft 1.20.1, BM 1.20.1-1.11.4

This matrix compares the independently produced manifests in
`CURRENT_PORT_PRE_PARITY_MANIFEST.md` and
`ORIGINAL_BIOME_MAKEOVER_MASTER_FEATURE_AUDIT.md`, then checks current source,
registrations, and resources directly. It does not treat an asset or data file
as implemented unless a released execution/registration path reaches it.

## A. Executive parity summary

The 1.21.10 port is not a broad port of released Biome Makeover. It is a
buildable, modernized Dark Forest prototype containing 28 registered blocks,
two standalone items, and one entity. Badlands, Swamp, Mushroom Fields, beach,
and mansion/illager systems are effectively absent. Dark Forest has useful
foundations, but even that theme differs materially in worldgen and Owl
behavior.

Estimated released parity is **13%**. This is a weighted static estimate over
125 auditable capability units, not a percentage of filenames or registry
counts. `EXACT` and `COMPATIBLE` score 1; `PRESENT-BUT-DIFFERENT` and `PARTIAL`
score 0.5; `BROKEN` scores 0.25; `MISSING` and unverified units score 0. The
score is 16.5/125 = 13.2%. Runtime verification may move individual labels.

| Status | Units | Meaning in this audit |
|---|---:|---|
| EXACT | 1 | Observable behavior matches the released source |
| COMPATIBLE | 4 | Modern implementation preserves the original result |
| PRESENT-BUT-DIFFERENT | 8 | Present, but observable behavior differs |
| PARTIAL | 14 | Only part of the historical capability is reachable |
| BROKEN | 2 | Intended current path is incomplete/nonfunctional |
| MISSING | 91 | No current released-parity implementation |
| NEEDS-RUNTIME-VERIFICATION | 5 | Static evidence cannot establish behavior |
| **Required released total** | **125** | Dead/dev content excluded |

Supplemental, excluded from the parity denominator: eight dead/unreachable
historical families and eighteen current Mythas enhancement units.

| Theme | Estimated parity | Summary |
|---|---:|---|
| Mushroom Fields | 6% | A wild-mushroom fragment exists, in the wrong theme/context |
| Badlands | 88% static | Stage 4 restored core blocks, cactus ecology, four worldgen chains, Scuttler, Tumbleweed and Cowboy patrol plumbing; Ghost Town remains Stage 10C, while horse hats and runtime verification remain open |
| Swamp | 98% static / runtime partial | Fresh Swamp/Mangrove generation, flora, Decayed behavior, peat, canopy/leaf contracts, pads/lilies, accepted Lightning Bug visuals, waterlogged saplings, and the reachable glass-bottle capture/light block are restored. Final Prism, soak/save/server and existing-world checks remain; Peat Composter and Sunken Ruins stay Stage 9/10B. Final-release-disabled Toad/Tadpole/Wings are excluded. |
| Dark Forest | Stage 6 static/package PASS; runtime open | Physical flora, Ancient Oak, Illunite/Mesmerite and the exact seven-feature pipeline restored; Owl/ecology/progression/Mansion remain later stages |
| Beach ecology | 0% | Helmit Crab family absent |
| Mansion / illager progression | 0% | Structure, boss, curses, quests, and functional systems absent |
| Shared/global systems | 6% | Basic modern bootstrap exists; content systems largely absent |

Largest gaps are the complete mansion/illager progression, three biome
makeovers, all historical structures and templates, 20 of 21 entity types,
functional blocks/block entities, and the bulk of recipes/loot/advancements.

## B. Theme-by-theme matrix

### Mushroom Fields

| Original released content | Current 1.21.10 content | Status | Restoration/dependencies |
|---|---|---|---|
| Underground mycelium and mushroom vegetation pipeline | Released mycelium and huge-Glowshroom configured/placed chains are registered, packaged and injected at the correct decoration steps | COMPATIBLE / RUNTIME PASS | Fresh underground Mushroom Fields exploration confirmed smaller vegetation and a naturally generated giant Glowshroom |
| Blighted Balsa wood set | None | MISSING | Foundational wood-family registration before trees/recipes/boats |
| Glowshrooms, huge variants, sprouts, roots, tall mushrooms | One generic Wild Mushrooms block | PARTIAL | Plant behavior, huge-feature codecs, placement, loot and models |
| Glowshroom/blighted masonry | None | MISSING | Block families, recipes, stonecutting, tags, loot |
| Glowfish ecology | Restored entity, water spawning, Salmon AI, renderer/attachment, loot and bucket behavior | COMPATIBLE / NEEDS-RUNTIME-VERIFICATION | Body, attachment, multi-entity rendering and swimming/orientation are runtime PASS; released texture source-confirms its lack of vanilla dark eye pixels; bucket release and save/reload remain runtime-open |
| Mushroom House structure and loot | None | MISSING | Structure registration, pieces/templates/processors and loot |

Mushroom Trader and Blight Bat registrations/resources are historical, but
their released natural spawn hooks were commented out. They are not required
to claim strict released parity; any reachable structure/script use must be
rechecked at runtime before final exclusion.

### Badlands

| Original released content | Current | Status | Restoration/dependencies |
|---|---|---|---|
| Barrel/saguaro vegetation, paydirt, surface fossils, tumbleweed | Stage 4 registrations, mechanics, resources and four injected worldgen chains | COMPATIBLE / NEEDS-RUNTIME-VERIFICATION | Tumbleweed major rendering/movement is runtime PASS; Saguaro sand/red-sand growth-origin parity is statically restored and fresh-chunk height distribution remains retest-open |
| Terracotta bricks and related decoration | All 17 verified families plus Cracked Bricks, recipes, loot, tags and resources | COMPATIBLE | Runtime geometry/connection/recipe checks remain |
| Scuttler | Natural spawn, attributes, core AI/interactions, Pink Bud flower-eating output, persistence, renderer/model, loot/sounds/resources | PARTIAL / NEEDS-RUNTIME-VERIFICATION | TEMPT_RANGE, Pink Bud attraction/breeding, fleeing and contextual threat/rattle are runtime PASS; restored tail animation and exact natural-spawn density remain retest-open |
| Cowboy/horse patrols and hats | Cowboy, exact equipment/drop chances, mounted Badlands patrol injection, custom leader banner, synchronized/persistent horse state, dedicated Cowboy/player/leader-horse hat paths, despawn, and vanilla-equivalent captain Ominous Bottle loot | COMPATIBLE / NEEDS-RUNTIME-VERIFICATION | Direct Cowboy and forced production-path patrol creation are runtime-confirmed; corrected player geometry and Ominous Bottle death progression require the next focused Prism verification |
| Ghost Town archaeology and structure loot | None | MISSING | Suspicious sand/pottery data, processors/templates, structures and loot |

Stage 4 restores an estimated 88% static Badlands parity. Ghost Town remains
the approved Stage 10C scope; client, dedicated-server, save/reload, multiplayer
and existing-world validation have not been executed.

### Swamp

| Original released content | Current | Status | Restoration/dependencies |
|---|---|---|---|
| Replace vanilla swamp trees with Willow/Cypress | Exact wood-family registrations, custom placers/decorators, current log-tag leaf support, checked/tree placements, vanilla-tree removal and Swamp/Mangrove injection | COMPATIBLE / RUNTIME PASS | Fresh generation, canopy retention, item-entity/performance remediation, and land/underwater sapling growth are runtime-accepted |
| Peat, reeds, lily/flower/mushroom ecology and bonemeal behavior | Peat chain, Reed/Cattail, pads/flowers and swamp-water bonemeal behavior restored | COMPATIBLE / RUNTIME PASS | Placement, tint, tilling and restored ecology are runtime-accepted |
| Itching Ivy environmental interaction | Existing implementation preserved; Stage 5 adds the released surrounding ecology without rewriting it | COMPATIBLE / RUNTIME PASS | Accepted with the completed Swamp ecosystem runtime pass |
| Thatch and swamp building families | Reed Thatch and all three exact peat masonry base/slab/stairs/wall families, recipes, loot, tags and assets restored | COMPATIBLE / RUNTIME PASS | Accepted in the Stage 5 comprehensive runtime checkpoint |
| Decayed, Dragonfly, Lightning Bug | Natural reachability and Decayed water/baby behavior/progression are runtime-confirmed; exact layered full-bright Lightning Bug cube/pulse/color/sparks are source/showcase-verified; glass/experience-bottle capture restores both released bottle loops | COMPATIBLE / RUNTIME PASS | Both bottle loops and final no-bolt impact presentation pass; Shocked stacking, expiration, health restoration and save/reload pass |
| Sunken Ruins structure | Deliberately absent | MISSING (STAGE 10B) | Structure/templates/processors/loot retain Stage 10B ownership |

The original Toad entity exists historically but its natural-spawn hook was
commented. Tadpole acquisition/reachability needs runtime confirmation and is
not silently counted as required natural ecology.

Stage 5 Swamp is **COMPLETE**: static parity PASS, package validation PASS, and runtime acceptance PASS. There are no
known mandatory final-release Stage 5 blockers. Historical/disabled content and later-stage structures, progression,
boats, and Mythas candidates remain deferred and do not change this status. Stage 6 is now static/package complete and runtime-open.

### Dark Forest

| Original released content | Current | Status | Restoration/dependencies |
|---|---|---|---|
| Forest floor, flowers, ivy, trees and wild-mushroom makeover | Exact seven-feature pipeline packaged at released steps and modifiers | COMPATIBLE / NEEDS-RUNTIME-VERIFICATION | Fresh-chunk density, support and performance checks |
| Ancient Oak complete wood set, tree and sapling | Physical family including signs plus 1x1/2x2 growth and exact selector configs restored; boats deferred | COMPATIBLE / NEEDS-RUNTIME-VERIFICATION | Tree geometry, ratios, leaf/drop and persistence checks |
| Black Thistle, Foxglove, Itching Ivy, Moth Blossom | Physical blocks and generation runtime-confirmed; Black Thistle upper-half Weakness callback restored; ivy slowdown/particles/spread and bonemeal conversion restored | STATIC-COMPATIBLE / BLACK THISTLE RUNTIME-OPEN | Moth-dependent attraction remains Stage 8 |
| Mesmerite/Illunite fissure, blocks and crystals | Complete physical families, four-stage growth, light/sound/loot and released fissure algorithm restored | COMPATIBLE / NEEDS-RUNTIME-VERIFICATION | Fissure distribution/geometry and crystal presentation checks |
| Owl core entity | Modern Owl exists | PRESENT-BUT-DIFFERENT | See dedicated matrix; preserve enhancements separately |
| Rootling and Moth | None | MISSING | Flora/tags first, then AI/render/loot/sounds/spawn |
| Dark Forest fox/rabbit additions | Both injected with custom weights/groups | PRESENT-BUT-DIFFERENT | Restore released weights/groups and full biome modifier semantics |
| Mansion integration | No custom mansion | MISSING | Depends on complete illager/functional systems and templates |

The four compatible units are the modern registration/bootstrap approach,
the original Owl attribute/taming core, the reusable Mesmerite block-family
plumbing, and the reusable Ancient Oak base-family plumbing. They are not a
claim that their complete feature families have parity. The two broken units
are shoulder visual completion (explicit renderer TODO) and dormant resources
that imply registered content (sign/boat family) without registration.

### Beach ecology

The released Helmit Crab entity, its spawning, behavior, food/drop chain,
renderer/model, sounds, loot and associated recipes are absent. Five capability
units are `MISSING`. Beach generation itself is not to be invented beyond the
historical source.

### Woodland Mansion / illager progression

| Original released system | Current | Status |
|---|---|---|
| Custom mansion structure/layout, processors and template library | None | MISSING; pinned tree currently inventories 168 Mansion NBT files, while the older roadmap shorthand says 228 |
| Adjudicator boss and phase/mimic support | None | MISSING |
| Mimic and Stone Golem | None | MISSING |
| Witch quests/trades/progression | None | MISSING |
| Altar, curses/effects, poltergeist and soul systems | None | MISSING |
| Illunite, cladding and special equipment | None | MISSING |
| Mansion loot, advancements, sounds, particles and client UI | None | MISSING |

All 22 units are missing. This is the largest and highest-risk restoration
family because it crosses structures, block entities, menus, packets, AI,
effects, loot, client UI, and progression persistence.

## C. Complete feature matrix

The allocation column is the reproducible basis for the totals in section A.
Each unit is a meaningful behavior family rather than a raw file.

| Scope | Units | Status allocation | Current evidence / missing boundary |
|---|---:|---|---|
| Mushroom Fields | 18 | PBD 1, PARTIAL 1, MISSING 16 | One generic mushroom fragment; no complete theme |
| Badlands | 17 | MISSING 17 | No registrations, resources, entities or injection |
| Swamp | 19 | STAGE 5 COMPLETE / RUNTIME PASS | Reachable released ecology and both Lightning Bug bottle loops are accepted; Peat Composter, Sunken Ruins, historical boats, disabled Toad content and Mythas ideas remain outside Stage 5 ownership |
| Dark Forest | 28 | STAGE 6 PHYSICAL STATIC/PACKAGE PASS; LATER SYSTEMS OPEN | Eleven configured, twelve placed resources and seven exact injections; Owl/Rootling/Moth/progression/Mansion retain later ownership |
| Beach ecology | 5 | MISSING 5 | No Helmit Crab implementation |
| Mansion/illager | 22 | MISSING 22 | Entire released progression absent |
| Shared/global | 16 | PBD 1, PARTIAL 1, MISSING 10, NRV 4 | Bootstrap/resources exist; systems below mostly absent |
| **Total** | **125** | **EXACT 1, COMPATIBLE 4, PBD 8, PARTIAL 14, BROKEN 2, MISSING 91, NRV 5** | |

### Registry/system coverage

| Historical system | Current inspection result | Primary status |
|---|---|---|
| Biome modifications | Mushroom, Badlands, Swamp and exact seven-feature Dark Forest physical pipeline | PARTIAL GLOBALLY / STAGE 6 STATIC PASS |
| Configured/placed features | Dark Forest final inventory is 11 configured/12 placed with seven injections | COMPATIBLE / NEEDS-RUNTIME-VERIFICATION |
| Structures/pieces/processors/templates | No code; no historical structure pipeline | MISSING |
| Blocks/block entities | 28 blocks; no block entities | PARTIAL / MISSING |
| Items | Two standalone items plus block/spawn items | PARTIAL |
| Entities | Owl only | PARTIAL globally; detailed differences below |
| Recipes/processing | 28 recipes; no original processing systems | PARTIAL |
| Loot | 25 block tables; no entity/structure parity corpus | PARTIAL |
| Advancements/triggers | Seven advancement JSONs; no custom triggers | PARTIAL |
| Sounds | Eight Owl events; other families absent | PARTIAL |
| Particles | No custom particle registration | MISSING |
| Client rendering/UI | Owl model/renderer only; no screens/HUD | PARTIAL |
| Models/blockstates/item definitions | Resources cover current slice; some orphaned/dormant | PARTIAL |
| Tags | Ten current tag files; original behavioral tags largely absent | PARTIAL |
| Networking | No custom networking | MISSING |
| Mixins | No mixins | MISSING |
| Configuration | No configuration system | MISSING |
| Data generation | No current datagen | MISSING |
| Compatibility/integration | No original integration layer | MISSING |

## D. Content counts

Counts are useful for scope only; generated families and historical Taniwha
factories make some original counts approximate.

| Category | Original 1.20.1 | Current 1.21.10 | Practical parity/missing |
|---|---:|---:|---|
| Entity types | 21 | 1 | 20 absent; Owl differs |
| Direct/static block suppliers | 123 plus generated families | 28 | Vast majority absent |
| Named block translations | 285 | 28 registered | At least 257 named entries absent; not a registry-perfect metric |
| Standalone/generated items | about 80 plus block items | 2 standalone | Most absent |
| Block entities | 5 | 0 | 5 absent |
| Configured features | 38 | 13 | 25 files absent; only a subset of current files reachable |
| Placed features | 37 | 13 | 24 files absent; only 3 current placed keys injected |
| Structures | 4 | 0 | 4 absent |
| NBT templates | 228 | 0 historical pipeline | 228 absent from parity path |
| Recipes | 374 | 28 | 346 absent by raw count |
| Loot tables | 329 | 25 | 304 absent by raw count |
| Advancements | 48 | 7 | 41 absent by raw count |
| Sound events | 75 | 8 | 67 absent; current five extra Owl events are Mythas additions |
| Particles | 4 | 0 | 4 absent |
| Network messages | 9 | 0 | 9 absent |
| Tags | 42 BM + 73 Minecraft namespace | 10 files | Historical behavioral membership largely absent |

## E. Owl parity matrix

This comparison uses the released historical Owl, not expectations derived
from the current Owl.

| Capability | Original released behavior | Current behavior | Status |
|---|---|---|---|
| Biome spawn | Dark Forest modifier | Dark Forest modifier | EXACT |
| Weight/group | Weight 20, group 1–4 | Weight 20, group 1–4 | STATIC-EXACT / RUNTIME-OPEN |
| Spawn predicate | Grass/leaves below and brightness >2; no time gate | Restored final predicate | STATIC-EXACT / RUNTIME-OPEN |
| Despawn | Normal persistence behavior | Restored inherited behavior | STATIC-EXACT / RUNTIME-OPEN |
| Attributes | fly .8, health 6, movement .4, attack 2; tame health 20/attack 4 | Same core values; adds tempt range | COMPATIBLE |
| Navigation | FlyingMoveControl/FlyingPathNavigation | Modern equivalents | COMPATIBLE |
| Flight/landing | Original random tree-flight and slow-fall/no fall damage | Extended exposed-canopy selection, lift/descent/hover corrections | PRESENT-BUT-DIFFERENT |
| Perching/tree use | Tree-directed flying goal | Return-to-tree and tree scoring/memory behavior | MYTHAS-ENHANCEMENT / PBD |
| Targeting | Owner defense plus all `owl_targets` tag members | Restored tag-driven target goal | STATIC-EXACT / RUNTIME-OPEN |
| Original prey | Rabbit, chicken, silverfish, endermite, bat, toad, Blight Bat, Dragonfly, both bugs | Chicken at night; rabbit helper says true but no target goal | PARTIAL |
| Taming | Any edible meat; 1-in-3 chance | `minecraft:wolf_food` equivalent; 1-in-3 | STATIC-COMPATIBLE / RUNTIME-OPEN |
| Healing | Any edible meat, nutrition-based | Restored nutrition-based meat healing | STATIC-COMPATIBLE / RUNTIME-OPEN |
| Temptation | Any meat | Raw rabbit only | PRESENT-BUT-DIFFERENT |
| Breeding/food | Any meat and original breeding goal | Rabbit only; child ownership plumbing | PARTIAL |
| Sit/owner behavior | Tameable/shoulder base behavior | Explicit synced sitting toggle and owner behavior | COMPATIBLE plus enhancement |
| Shoulder behavior | Inherited shoulder riding | Goal exists; renderer has explicit shoulder-perch TODO | BROKEN / NEEDS-RUNTIME-VERIFICATION |
| Sounds | Idle, hurt, death | Original three plus five custom events | COMPATIBLE plus enhancement |
| Model/animation | Historical geometry and movement | Geometry adapted to render-state API; custom flying/sitting/sleep/blink | PARTIAL / enhancement |
| Emissive eyes | Always emissive eyes layer | Layer always registered, but `nightEyes` state is not visibly consumed by layer | NEEDS-RUNTIME-VERIFICATION |
| Texture variant | Includes Hedwig variant selection | Restored case-insensitive Hedwig selection | STATIC-EXACT / RUNTIME-OPEN |
| Drops | 1–2 feathers plus looting | Restored modern loot schema | STATIC-EXACT / RUNTIME-OPEN |
| State/NBT | `OwlState` and `StandingState` | Same plus sitting, sleeping and nest position state | COMPATIBLE plus enhancement |
| Persistence | Original tameable-entity behavior | Additional daytime/despawn/nest persistence decisions | PRESENT-BUT-DIFFERENT |
| Baby behavior | Vanilla age/breeding path | Scale/state hooks and baby sound; final geometry noted as unfinished | PARTIAL / enhancement |

To restore released parity while retaining optional work, isolate rules into
two layers. Restore the original spawn weight/predicate, meat ingredient,
nutrition healing, target tag, drops, Hedwig selection, and original goal
priorities in the parity layer. Preserve nest/sleep/daylight/tree-scoring,
blink, extra sounds, and specialized hunting behind a later Mythas layer or
explicit configuration. Modern navigation, attributes, synced data, render
state, and registry plumbing should remain.

Likely conflicts: nighttime-only spawning, daytime despawn, rabbit-only diet,
night-chicken-only targeting, altered goal priorities, exposed-canopy flight,
and conditional-eye intent directly change original behavior. Likely additive
and retainable after parity: blink, cosmetic sounds, nests/eggs once completed,
sleep pose, baby presentation, and tree preference if disabled by default in a
strict-parity configuration.

## F. Current Mythas enhancement inventory

These are not evidence of historical parity and must not be deleted during
restoration.

1. Blink timing and eyelid geometry.
2. Sleeping pose/state and disturbance behavior.
3. Owl Nest block with claimed/egg blockstate.
4. Home-nest search, claim, validation and NBT coordinates.
5. Owl Egg item and resources.
6. Incubation/hatching groundwork; no completed timer/hatch path found.
7. Nighttime spawning rule.
8. Daylight-only wild despawn policy.
9. Wild-player caution/avoidance.
10. Return-to-tree and exposed-perch scoring.
11. Extended tree-flight behavior.
12. Custom lift, descent, glide and anti-hover movement.
13. Night chicken hunting.
14. Rabbit/chicken-specific interaction rules.
15. Hoot/contact/alert/baby/takeoff sound events.
16. Night-eye render-state intent and custom blink/sleep/flying animation.
17. Explicit sit synchronization and child-owner propagation.
18. Partial baby presentation hooks.

No completed Owl heads implementation was found. No natural Owl Nest feature
is injected. `HAS_EGG` has no complete laying/incubation/hatching execution
path. These are preserved experiments, not complete gameplay claims.

## G. Current work worth reusing

| Current work | Decision | Reason |
|---|---|---|
| Fabric 1.21.10 bootstrap and registry conventions | REUSE-AS-IS | Modern API foundation; observable parity independent |
| Owl entity registration, attributes, synced state, navigation adapter | REUSE-WITH-PARITY-CHANGES | Solves modern plumbing; rules/goals differ |
| Owl model geometry/render-state conversion | REUSE-WITH-PARITY-CHANGES | Valuable API conversion; restore Hedwig/eye/shoulder semantics |
| Nest/egg/sleep/blink/day-night systems | PRESERVE-FOR-LATER-MYTHAS-LAYER | Additions conflict with a clean released baseline unless gated |
| Ancient Oak registered base family and data assets | REUSE-WITH-PARITY-CHANGES | Add missing signs/boats and prove original family behavior |
| Mesmerite blocks, stairs/slabs/walls and feature plumbing | REUSE-WITH-PARITY-CHANGES | Useful modern implementation; original system is larger |
| Black Thistle/Foxglove/Itching Ivy/Wild Mushrooms | REUSE-WITH-PARITY-CHANGES | Keep resources and compatibility work; restore exact behavior/generation |
| Modern stairs/slabs/walls recipes/models | REUSE-AS-IS where IDs match | Data format conversion is already solved |
| Current wall tags/connections | REUSE-WITH-PARITY-CHANGES | Static membership is incomplete; runtime connection tests required |
| Dormant configured/placed feature JSONs | REUSE-WITH-PARITY-CHANGES | Review codecs/placement against original before enabling |
| Orphan sign/boat assets | PRESERVE-FOR-LATER / complete during family stage | Assets alone are not registered parity |
| Any conflicting replacement of original IDs | REPLACE only after data migration design | Existing-world registry safety outranks cleanup |

## H. Porting risk analysis

| Missing/restored system | Risk | Principal blockers |
|---|---|---|
| Plain block/item families and recipes | LOW | Renamed data formats, component-based items, tag membership |
| Plants and simple placed features | MEDIUM | 1.21.10 codecs/bootstrap lookup, placement ordering, bonemeal behavior |
| Tree replacement and full biome modification | HIGH | Removal/replacement semantics and Fabric biome API changes |
| Mesmerite fissure/complex terrain feature | HIGH | Worldgen codec/height/context changes and existing-chunk seams |
| Ordinary mobs (Glowfish, Rootling, Moth, etc.) | HIGH | AI/navigation/spawn API and render-state migration |
| Owl parity reconciliation | HIGH | Interleaved Mythas behavior must be preserved but disabled/separated |
| Functional blocks and five block entities | HIGH | data components, menus, ticking, serialization, packets |
| Ghost Town archaeology | VERY HIGH | archaeology APIs, processors/templates and suspicious-block behavior changed |
| Mushroom House/Sunken Ruins | HIGH | structure bootstrap, template pools/pieces/processors |
| Mansion replacement/layout | VERY HIGH | 228 templates, layout generator, processors, vanilla mansion interaction |
| Adjudicator/boss progression | VERY HIGH | multi-phase AI, persistence, packets, UI, structure dependencies |
| Curses/quests/altar/poltergeist | VERY HIGH | effects, capability-like state replacement, menus/networking/client UI |
| Custom criteria/advancements | MEDIUM | trigger API/data changes |
| Client particles/render layers/screens | HIGH | 1.21.10 render-state and GUI API changes |
| Loot corpus and special serializers | HIGH | loot context/data format/API and conditional behavior |
| Integration compatibility | MEDIUM | historical integration targets may be obsolete |

Likely blockers are not registry syntax alone: Taniwha-generated semantics,
removed/changed Fabric biome mutation hooks, new entity render-state APIs,
structure bootstrap codecs, item data components, and block-entity/menu packet
formats all require local behavioral replacements rather than mechanical copy.

## I. Taniwha dependency analysis

Historical BM declared Taniwha 1.20.0-5.4.4. The original audit traces use of
Taniwha for registry/family factories, generated wood/decorative membership,
boat types/rendering, item/block modifiers, data helpers, criteria, and other
engine utilities. Its binary internals were not locally audited, so exact
generated membership and some modifier behavior remain runtime-verification
items.

| Historical use | Preferred 1.21.10 treatment |
|---|---|
| Deferred/family registration helpers | Implement small local typed factories over vanilla registries |
| Standard blocks/items and modifiers | Use vanilla classes/properties and Fabric events/tags where needed |
| Wood/decorative family generation | Local deterministic family helpers plus datagen; preserve IDs |
| Boat/chest-boat types and rendering | Use modern vanilla entity/data-component functionality; local glue if IDs require it |
| Criteria/advancement helpers | Implement local custom triggers only where vanilla triggers cannot express behavior |
| Biome/worldgen hooks | Fabric API plus vanilla bootstrap registries |
| Specialty observable behavior | Port the smallest required utility locally with tests |
| Networking | Fabric networking for the nine historical message behaviors |

Conclusion: **do not require Taniwha by default**. No evidence yet justifies a
new runtime dependency. Prefer vanilla, then Fabric API, then small local
ports. Only reconsider a dependency if a maintained 1.21.10 implementation is
proven compatible and materially reduces risk without changing IDs/behavior.
Before family restoration, obtain/decompile the exact historical Taniwha jar
or run the 1.20.1 build to establish generated memberships and defaults.

## J. Restoration dependency graph

```text
ID/registry contract + parity test harness
  -> shared tags, family factories, data generation
     -> foundational blocks/items/foods/effects
        -> functional blocks + block entities + menus
        -> biome flora/wood/stone families
           -> configured features -> placed features -> biome injection
              -> simple entities -> spawn rules -> renderers/sounds/loot
              -> biome structures -> templates/processors -> structure loot
        -> equipment/curses/quest state
           -> mansion layout -> boss/mimics/golems -> packets/UI
  -> recipes + loot + advancements (validated continuously, finalized late)
  -> client particles/models/layers/screens and integrations
  -> strict parity runtime baseline
     -> separately approved Mythas overlay
```

Specific chains include:

- Blighted Balsa/Glowshroom blocks → huge features → Mushroom Fields injection
  → Glowfish/Mushroom House → loot.
- Saguaro/pay dirt/pottery → Badlands features/archaeology → Ghost Town →
  Cowboys/Scuttler and structure loot.
- Willow/Cypress/peat/flora → swamp replacement → swamp mobs → Sunken Ruins.
- Ancient Oak/plants/Mesmerite → Dark Forest worldgen → Rootling/Moth/Owl
  targets → mansion integration.
- Illunite/cladding/effects → altar/quests → mansion templates/layout →
  Adjudicator phases → progression loot/advancements/UI.

## K. Proposed restoration stages

Fourteen parity stages are proposed. Each ends buildable and does not include
unreleased dev content or activate Mythas additions by default.

| Stage | Scope/prerequisite | Static validation | Runtime validation / parity gained |
|---:|---|---|---|
| 0 | ID contract, registry dump, test-world backup protocol | Baseline ID/count diff; build | Load preserved world copy; no missing-registry warnings |
| 1 | Local Taniwha replacements, tags, family/datagen foundations | Generated membership snapshots | Minimal registry launch; enables all families |
| 2 | Shared block/item/food/effect families | Models, loot, recipes, tags complete | Place/use every family; foundational parity |
| 3 | Mushroom Fields content/worldgen | Feature bootstrap and injection trace | New-chunk generation, bonemeal, Glowfish; theme parity |
| 4 | Badlands blocks/worldgen/entities excluding Ghost Town | Placement/spawn/loot checks | New-chunk ecology and mob AI |
| 5 | Swamp blocks/tree replacement/ecology/entities | Removed/added feature ordering | New-chunk comparison and bonemeal tests |
| 6 | Dark Forest flora/Ancient Oak/Mesmerite exact worldgen | Exact original modifier/placement list | New-chunk distribution and sapling tests |
| 7 | Owl released parity layer, preserving Mythas code separately | Goal/tag/diet/drop/state tests | Spawn/tame/breed/hunt/shoulder/render tests |
| 8 | Remaining simple entities and Beach ecology | Attributes/spawn/loot/render inventory | AI, despawn, animation, sound tests |
| 9 | Functional blocks, five block entities, menus and packets | Codec/NBT/menu/packet round trips | Save/reload, multiplayer interaction tests |
| 10 | Mushroom House, Sunken Ruins, Ghost Town/archaeology | Template/processor/loot references | Locate/generate in new chunks; archaeology tests |
| 11 | Mansion foundation, 228 templates and layout/processors | Template reachability and deterministic layout tests | Locate/generate mansion in disposable new region |
| 12 | Illager progression, Adjudicator, Mimic, Golem, quests/curses | State-machine, loot, packet, advancement tests | Full multiplayer progression playthrough |
| 13 | Complete recipes/loot/advancements/sounds/particles/client/integrations; parity freeze | Registry/resource audit against master manifest | Historical side-by-side acceptance suite; tag parity baseline |

Every `MISSING`, `PARTIAL`, and `BROKEN` required family is covered: stages
1–2 cover shared data, 3–8 themes/entities, 9 functional systems, 10–12 all
structures/progression, and 13 cross-cutting resources. Mythas reconciliation
begins only after the stage-13 parity tag and separate approval.

## L. Existing-world safety analysis

| Change family | Classification | Existing-world treatment |
|---|---|---|
| Add blocks/items/entities under stable historical IDs | SAFE IN EXISTING CHUNKS, subject to ID audit | Do not rename/remove current IDs; test missing-registry recovery on copy |
| Add biome placed features/ores/vegetation | NEW CHUNKS ONLY | Existing generated chunks stay unchanged; travel to new chunks |
| Mesmerite/trees/flora distribution changes | NEW CHUNKS ONLY / chunk-border seams | Chunk trimming only if players want features near explored areas |
| Replace vanilla swamp/dark-forest feature lists | POTENTIAL EXISTING-WORLD RISK | Affects only newly generated chunks, but confirm no retro-generation hook |
| Add Mushroom House/Sunken Ruins/Ghost Town | NEW CHUNKS ONLY | Locate in ungenerated regions; trimming optional to experience nearby |
| Replace/add mansion structure behavior | VERY HIGH EXISTING-WORLD RISK | Never rewrite generated mansions; preserve structure IDs/start compatibility; new chunks first |
| Add entity natural spawns | SAFE IN EXISTING CHUNKS | Spawns can begin in loaded eligible chunks; verify density/despawn |
| Add block entities/menus | SAFE unless replacing an existing ID | DataFix/migration needed before changing serializer or ID |
| Change Owl NBT/state | POTENTIAL EXISTING-WORLD RISK | Preserve current custom keys and tolerate absent historical keys |
| Add recipes/loot/advancements | GENERALLY SAFE | Loot tables affect unopened containers/new drops; advancement behavior may trigger anew |
| Tag changes | POTENTIAL BEHAVIOR RISK | Can alter existing blocks/entities immediately; snapshot memberships |
| Remove/replace current custom IDs | UNACCEPTABLE without migration | Keep aliases or data migration; never strand blocks/entities in long-running world |

No world reset is required. Features already absent from generated terrain are
experienced in new chunks; selective, backed-up trimming is optional, never a
parity prerequisite. Runtime testing must use a copy of the long-running world
and include downgrade/rollback implications before any registry change ships.

## M. Released, dead, and development scope separation

### Required released parity

The 125 units above cover all reachable released 1.20.1-1.11.4 systems:
six theme scopes, registered blocks/items/entities, biome/worldgen, four
structures, functional blocks/entities, recipes, loot, advancements, sounds,
particles, client, tags, networking, mixins/behavior hooks, configuration and
integrations evidenced by the historical audit.

### Unreachable/dead historical content

Documented but excluded from automatic restoration: commented Blight Bat and
Mushroom Trader natural spawns, commented Toad natural spawning, uncertain
Tadpole acquisition, Adjudicator Mimic phase-only reachability, DirectionalData
dev block, unused Owl `ATTACKING` state, stale giant-slime references, and
unused/unselected templates/assets. These require explicit reachability proof,
not assumption from registration or resources.

### Unreleased development appendix

Not parity requirements and not in stages 0–13: Dust Devil, grinding,
reinforcement, caravanning, succulents, large pots, additional masonry, Scarab
Elytra, REI work, and other `dev`-only experiments identified in Step 2. They
remain future design candidates only.

Current Mythas Owl/nest experiments are likewise outside the released-parity
denominator, but unlike historical dev content they must be preserved for a
later approved overlay.

## N. Runtime verification backlog

1. Run the exact 1.20.1-1.11.4 historical build with Taniwha 5.4.4 and dump
   registries/tags to resolve generated family counts and defaults.
2. Verify current Ancient Oak sapling actually selects/generates its configured
   tree; most present feature JSONs have no biome injection path.
3. Measure current Mesmerite and plant distributions and compare seeded worlds.
4. Confirm current wall connections and tag namespace/directory semantics.
5. Test Owl shoulder mounting and rendering; the current renderer explicitly
   leaves a shoulder hook unfinished.
6. Test whether the current eyes layer is always emissive despite unused
   `nightEyes` state, and capture original unconditional behavior.
7. Verify original Hedwig selection probability/persistence from historical
   runtime if static source does not fully determine it.
8. Verify all historical Owl target memberships and reachability, especially
   entities whose natural spawn hooks were disabled.
9. Confirm original Mushroom Trader, Blight Bat, Toad/Tadpole and phase Mimic
   released reachability before final parity sign-off.
10. Decode/sample all 228 NBT templates and validate selector reachability,
    processors, markers, loot and entity placement.
11. Establish original structure spacing/separation/biome predicates from a
    seeded historical server and compare locate results.
12. Exercise block-entity NBT, menu, packet, curse/quest and boss persistence in
    single-player and dedicated-server multiplayer.
13. Compare loot/recipe/advancement corpus through automated ID and semantic
    snapshots rather than count equality.
14. Load a copy of the long-running 1.21.10 world after each registry/worldgen
    stage and inspect logs, chunks, existing Owls and block-entity data.

## Validation statement and source evidence

All historical registry categories and all major current categories described
by the two manifests were assigned a scope. Current conclusions were checked
against `BMBlocks`, `BMItems`, `BMEntities`, `BMWorldgen`, `BMSounds`,
`OwlEntity`, `OwlNestBlock`, the Owl model/renderer/render state, and the
current `data`/`assets` trees. Historical feature details and exact upstream
paths remain traceable through section V of
`ORIGINAL_BIOME_MAKEOVER_MASTER_FEATURE_AUDIT.md` at commit
`2f314c0596af095a4890995a465f308f69476b4a`.

No conclusion treats raw count equality as parity. No unreachable or `dev`
feature is included in the restoration denominator or stages. No gameplay
implementation or resource change is part of this audit.
