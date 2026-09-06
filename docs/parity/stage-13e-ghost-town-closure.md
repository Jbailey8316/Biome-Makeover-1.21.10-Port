# Stage 13E — Ghost Town archaeology / structure closure

## Adjudication

The final released 1.20.1 Ghost Town package is already implemented in the
current 1.21.10 port. The packaged Stage 10C.4 contract validates the complete
graph: the jigsaw structure and structure set, Badlands biome tag, four pools,
50 released templates, both local processor lists, suspicious red sand,
archaeology and container loot, Ghost structure spawn override, advancement
predicates, the Ghost Town disc, and the packaged sound.

The old Stage 10C freeze validator is historical and non-gating here because it
rejects later legitimate registrations such as Stone Golem. Stage 13E uses a
scoped validator that invokes the complete packaged Ghost Town contract with
explicit artifact paths and checks the final singular template path.

## Current status

**COMPLETE / PRISM RUNTIME REQUIRED**

Static/package validation is complete. Natural Ghost Town generation,
archaeology brushing, container loot, save/reload, and log cleanliness still
require Prism verification in a fresh disposable world.

## Released inventory

- `biomemakeover:ghost_town`, vanilla jigsaw structure, Badlands tag
- `biomemakeover:ghost_towns`, spacing 32 / separation 12 / salt 6969
- Pools: `ghosttown/centers`, `roads`, `buildings`, `decoration`
- Templates: 1 center, 7 roads, 27 houses, 15 decorations (50 total)
- Processors: `ghosttown_building`, `ghosttown_roads`, local suspicious-block
  replacement, Ghost Town barrel loot, and bookshelf filling
- Suspicious red sand with `archaeology/ghost_town` loot
- Archaeology tables: Ghost Town, junk, and horse armor
- Container tables: `ghost_town/loot_0`, `_1`, `_2`
- Ghost Town advancement, Badlands disc advancement, disc/jukebox data, and
  released sound resources

## Compatibility representation

Released plural structure-template resources are packaged under the current
singular 1.21.10 `data/biomemakeover/structure/ghosttown/` path. The released
Taniwha suspicious-block processor is represented by the registered local
1.21.10 processor. The released old singular advancement predicate is carried
through the current plural `structures` form. No Mansion data is involved.

## Prism checklist

Use a fresh world and locate a naturally generated Ghost Town. Inspect roads,
houses, decorations, processors, suspicious red sand, archaeology loot,
barrels, structure spawns, save/reload behavior, and logs for structure,
processor, jigsaw, loot, registry, and resource errors. `/place` is only a
supplemental diagnostic; natural generation is required.
