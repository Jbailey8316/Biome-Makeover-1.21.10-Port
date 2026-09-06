# Stage 13I — Badlands + Dark Forest Source-Boundary Closure

Status: AUDITED / COMPLETE / NO FUNCTIONAL CHANGE / DO NOT PUSH

Authority: final released Biome Makeover 1.20.1 source and the packaged
1.21.10 artifact. Stage 13I changed only this audit record and its validator.

## Verdict

Both biome slices are complete for released parity. Their remaining status is
source-boundary/runtime verification only, not an implementation gap. The
resource pipeline supplies the released data and client resources; current
Java wires the placed features, biome integration, entities, and interactions.

## Badlands ownership matrix

Badlands-owned released content is the terracotta-brick family and its slabs,
stairs, and walls; `paydirt`, `saguaro_cactus`, `barrel_cactus`, and the
flowered Barrel Cactus (including potted forms); `tumbleweed`; `scuttler`,
`cowboy`, and the Tumbleweed entity; `scuttler_tail`, `pink_bud`,
`cowboy_hat`, `cracked_brick`, and the Scuttler/Cowboy spawn eggs; the
Scuttler and Tumbleweed sounds; four configured/placed feature pairs:

| Released feature | Current registration / packaged data | Classification |
| --- | --- | --- |
| `badlands/barrel_cactus` | `BMWorldgen.BADLANDS_BARREL_CACTUS`; reference-backed configured and placed JSON | COMPLETE |
| `badlands/saguaro_cactus` | `BMWorldgen.BADLANDS_SAGUARO_CACTUS`; reference-backed configured and placed JSON | COMPLETE |
| `badlands/paydirt` | `BMWorldgen.BADLANDS_PAYDIRT`; reference-backed configured and placed JSON | COMPLETE |
| `badlands/surface_fossil` | `BMWorldgen.BADLANDS_SURFACE_FOSSIL`; reference-backed configured and placed JSON | COMPLETE |
| Scuttler / spawn rule / food / loot | `ScuttlerEntity`, `BMEntities`, `BMWorldgen`, packaged loot/tags/resources | COMPLETE / SOURCE-BOUNDARY VERIFICATION ONLY |
| Cowboy / Badlands patrol hook | `CowboyEntity`, `PatrolSpawnerMixin`, packaged client resources | COMPLETE / SOURCE-BOUNDARY VERIFICATION ONLY |
| Tumbleweed / gamerule / damage immunity | `TumbleweedEntity`, `BMWorldEvents`, packaged sounds/tags | COMPLETE |

Ghost Town, suspicious red sand, archaeology, processors, templates, and
structure loot are shared/structure-owned and were already frozen by Stage
13E. They are not reopened as Badlands work. The Badlands biome hook uses
Minecraft's `IS_BADLANDS` tag; the Tumbleweed biome tag and plantable/damage
tags are packaged through the established pipeline.

## Dark Forest ownership matrix

Dark-Forest-owned released content is Ancient Oak and its physical wood
family; Ivy and Itching Ivy; Foxglove, Black Thistle, Wild Mushrooms, and Moth
Blossom; Mesmerite and its polished/slab/stair/wall variants; Illunite block,
budding and bud/cluster stages plus Illunite Shard; Owl, Moth, and Rootling
content in their released active scopes; and the Dark Forest worldgen chain:

Configured features: `dark_forest/ancient_oak`, `ancient_oak_small`,
`dark_oak_small`, `flowers`, `itching_ivy`, `mesmerite_boulder`,
`mesmerite_fissure`, `mesmerite_underground`, `tall_grass`, `trees`, and
`wild_mushrooms`.

Placed features: `ancient_oak_checked`, `ancient_oak_small_checked`,
`dark_oak_small_checked`, `flowers`, `grass`, `itching_ivy`,
`mesmerite_boulder`, `mesmerite_fissure`, `mesmerite_underground`,
`owl_nest`, `tall_grass`, `trees`, and `wild_mushrooms`.

Current `BMWorldgen` supplies the Dark Forest feature hooks and spawn entries;
`BMBlocks`, `BMEntities`, the entity classes, client renderers, and the
packaged reference resources supply the corresponding behavior and assets.
Black Thistle is Dark Forest-owned, not Mushroom Fields content.

`owl_nest` data is a released-resource orphan: final release did not wire an
Owl Nest feature into the final natural-generation chain, so the build's
resource treatment does not promote it into a gameplay requirement. The
released Owl/Moth/Rootling, ecology, plant, Mesmerite, and Illunite scopes are
complete; previously accepted runtime scopes remain frozen.

## Shared and frozen systems

The following are intentionally classified `SHARED / FROZEN`, not duplicated
under either biome: Mansion and Adjudicator content, Mimic and Stone Golem,
Witch Quest, Poltergeist/soul handling, Ghost Town, Beach, Mushroom Fields,
boats, localization, common recipe/loot infrastructure, and current-Minecraft
registry/resource adaptations. No source-boundary evidence requires reopening
any of them.

## Disabled/orphan boundaries

Toad/Tadpole remain released-but-disabled/orphan content: final release
registered the package but commented out natural Toad spawning and hid its
egg. Blightbat likewise remains released-but-unused: its final-release spawn
and public egg access were disabled. The current port correctly does not
revive either. These are `RELEASED ORPHAN / UNUSED`, not missing natural
biome gameplay.

There is no source-proven final-release requirement for the development-only
Dust Devil, Reinforcement, Caravanning, Succulents, Large Pots, or additional
Masonry features. They remain out of scope.

## Runtime and pipeline evidence

The established build/reference pipeline packages the released Badlands and
Dark Forest data/client families; source-tree location alone is not used as a
missing-feature test. The durable validator checks representative packaged
worldgen, entity, and block resources plus the current Java biome hooks.
Existing Prism acceptance covers the frozen shared systems and the previously
accepted Owl/Black Thistle/colony scopes. Remaining work is only a focused
fresh-chunk density/source-boundary spot check if a future regression is
suspected; no implementation stage is justified by this audit.

## Closure decision

Badlands: `COMPLETE / SOURCE-BOUNDARY VERIFICATION ONLY`.

Dark Forest: `COMPLETE / SOURCE-BOUNDARY VERIFICATION ONLY`.

No genuine released Badlands or Dark Forest feature is missing. Stage 13I
requires no functional changes and does not require a Prism artifact.
