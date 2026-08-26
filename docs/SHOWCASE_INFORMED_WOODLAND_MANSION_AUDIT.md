# Showcase-Informed Woodland Mansion Preservation Audit

Audit date: 2026-08-25

This document is the structure/progression companion to `SHOWCASE_INFORMED_DARK_FOREST_AUDIT.md`. It is audit-only.
Final Biome Makeover 1.20.1-1.11.4 source is authoritative; no Mansion production content was restored here.

## 1. Architecture verdict

The released system is a custom `biomemakeover:mansion` structure and piece type. It does not merely add loot to a
vanilla Woodland Mansion. It uses an algorithmic three-dimensional room grid, custom codec-supplied template lists,
rotation-aware rooms/walls/roofs, structure markers, terrain integration, an underground dungeon staircase/rooms,
and a dedicated boss room. The structure set is spacing 32, separation 9, salt 420, restricted by
`#biomemakeover:has_structure/reworked_mansion`, generated at `surface_structures` with `beard_box` adaptation.

The pinned source tree contains **168 Mansion NBT files**: corridor 18, dungeon 30, entrance 1, garden 7, roof 23,
room 45, stairs 4, tower 3, wall 34, plus three root/filler templates. This concrete count supersedes the older broad
“228 templates” roadmap shorthand for this pinned resource tree and must be reconciled before Stage 11 acceptance.

## 2. Feature disposition matrix

| Feature | Showcase observation | Final source status | Final reachability | Current port | Dependencies | Classification | Action required | Notes |
|---|---|---|---|---|---|---|---|---|
| Reworked Mansion | Larger modular Mansion | Custom structure/type/piece/layout/codecs and 168 NBT files | Natural structure set | Absent | Stage 6 blocks; Stage 9 markers | DEFERRED BY EXISTING STAGE OWNERSHIP | Stage 11 | Must not be reduced to vanilla Mansion + loot |
| Mansion terrain integration | Structure seated into terrain | `beard_box`, per-piece ground flag, Beardifier mixin | Natural generation | Absent | 1.21.10 structure internals | DEFERRED BY EXISTING STAGE OWNERSHIP | Stage 11 | Very-high existing-world/runtime risk |
| Mansion markers | Mobs, loot and decoration | Directional Data markers place Ivy, Tapestries, Owls, mushrooms, spawners, mob pools and seven loot tiers | Structure generation | Absent | Stage 9 Directional Data | DEFERRED BY EXISTING STAGE OWNERSHIP | Stage 9 before 11 | Bonemeal marker is commented/broken and must remain inactive unless final behavior proves otherwise |
| Dungeon | Stair into underground encounter | Dedicated top/mid/bottom stairs, 18 rooms, seven doors, wall and boss room | Generated as Mansion layout layer | Absent | Mansion layout | DEFERRED BY EXISTING STAGE OWNERSHIP | Stage 11 | Not independent cave worldgen |
| Adjudicator | Illusioner-like boss | Persistent custom boss placed by `boss` marker; 255 health; phase engine and room bounds | One boss marker per boss-room template path | Absent | Mansion/dungeon | DEFERRED BY EXISTING STAGE OWNERSHIP | Stage 12 | Do not substitute vanilla Illusioner |
| Adjudicator Mimic | Illusions/clones | Separate no-normal-spawn entity created only by Mimic phase | Phase-only | Absent | Adjudicator | DEFERRED BY EXISTING STAGE OWNERSHIP | Stage 12 | Registered does not imply normal reachability |
| Adjudicator Stone Golem phase | Boss mounts construct | Temporary loot-blocked Crossbow Golem spawned/mounted during phase and discarded on exit | Boss phase only | Absent | Adjudicator/Stone Golem | DEFERRED BY EXISTING STAGE OWNERSHIP | Stage 12 | Separate from player-created Golem |
| Adjudicator rewards | Tapestry/Totem | Entity loot guarantees one Enchanted Totem and one Adjudicator Tapestry | Boss death | Absent | Boss | DEFERRED BY EXISTING STAGE OWNERSHIP | Stage 12 | Music disc is not in boss loot |
| Enchanted Totem | Stronger Totem | Foil custom totem; activates at half max health, clears effects, grants Regen II 500, Absorption IV 1200, Fire Resistance 2000, Resistance 2000 | Guaranteed boss drop | Absent | Adjudicator | DEFERRED BY EXISTING STAGE OWNERSHIP | Stage 12 semantic port | Uses Taniwha Totem hook historically; local modern equivalent required |
| Tapestries | Mansion collectibles | 16 dye variants plus Adjudicator; standing/wall blocks with shared BE/item renderer | Random Mansion markers and boss drop | Absent | Stage 9 BE/renderer, Stage 11 | DEFERRED BY EXISTING STAGE OWNERSHIP | Stage 9 then 11/12 | No crafting recipes found; collectible loot/decoration |
| Crude Cladding | “Crud/crude” Pillager drop | Canonical IDs are `crude_fragment`, `crude_cladding`, `crude_cladding_block` | Pillager/leader loot, Mansion loot | Absent | Pillager loot hooks | DEFERRED BY EXISTING STAGE OWNERSHIP | Stage 12 | Translation is Crude Cladding, not Crud |
| Cladding template | Armor upgrade | `cladding_upgrade_smithing_template`; guaranteed Pillager leader and outpost-additional loot; duplicable with Mesmerite and Iron | Survival reachable | Absent | Pillager hooks/Mesmerite | DEFERRED BY EXISTING STAGE OWNERSHIP | Stage 12 | Current smithing API migration required |
| Cladded armor | Reinforced leather presentation | Four dedicated armor items made by smithing Leather Armor + Crude Cladding + template | Survival reachable | Absent | Template/cladding | DEFERRED BY EXISTING STAGE OWNERSHIP | Stage 12 | Not an armor trim; dedicated material/models/items |
| Cladded Stone | Construct material | Four Smooth Stone + Crude Cladding -> four `cladded_stone` | Crafting | Absent | Crude Cladding | DEFERRED BY EXISTING STAGE OWNERSHIP | Stage 12 | Strength 1.5, resistance 6; heals Stone Golem via tag |
| Stone Golem | Player sentry and boss construct | Player-buildable neutral multipart golem; player-created state, anger/targets, Crossbow arming by player/dispenser, ranged/melee AI | Player construction; boss phase temporary variant | Absent | Cladded Stone | DEFERRED BY EXISTING STAGE OWNERSHIP | Stage 12 | Source—not showcase analogy—decides allegiance |
| Red Rose disc | Mansion-associated record | `red_rose_music_disk`, sound `red_rose`, comparator 2, duration 135 | Mansion good and dungeon-good chest loot | Absent | Mansion loot | DEFERRED BY EXISTING STAGE OWNERSHIP | Stage 11/12 | Not Adjudicator loot |
| Mansion advancement chain | Progression | Mansion discovery, all tapestries, Enchanted Totem, Crude Cladding, template, full Cladded Armor, Arm Golem and Dark Forest disc | Released reachable | Absent | All above | PARITY MISSING | Restore in owning stages | Criteria establish intended acquisition paths |

## 3. Mansion structure and dungeon details

`MansionLayout` generates an algorithmic room grid with entrance, corridors, normal/big rooms, gardens, stair-up,
stair-down, towers, roof layers, dungeon stairs, dungeon rooms and boss room. Each grid cell is spaced 12 blocks in X/Z
and seven blocks vertically. Roofs are sorted/placed first. Rotation-specific offsets, wall pieces and corner fillers
complete the building. Ground pieces participate in terrain shaping; wall/top pieces use different ignore processors.

Directional metadata can:

- place multi-face Ivy;
- place one of 16 standing/wall Tapestries;
- configure Spider/Cave Spider spawners;
- spawn an Owl, with a 25% stunted/baby flag;
- place mushroom decoration;
- select standard/good/junk/dungeon/dungeon-good/dungeon-junk/arrows loot;
- spawn entities from encoded melee/ranged/golem/ravager/cow/allay pools with marker chance halved by the code.

The `bonemeal` directional marker contains deliberately commented/broken behavior. It is final dead marker behavior and
must not be “completed” merely because templates contain the marker.

The dungeon is structurally integrated: top/middle/bottom stair templates descend from the Mansion grid to an
underground room layer, with dedicated doors/walls/18 room variants, dungeon loot tiers, and a boss room. The boss
marker creates a persistent Adjudicator and clears itself; `arena_pos` markers become Smooth Quartz and define room
positions used by the encounter.

## 4. Cladding contract

Pillager supplemental loot is final reachable:

- Ordinary Pillager: 5% chance for one damaged Cladded Boots/Leggings/Chestplate; independently 25% chance for a
  weighted result of Crude Fragment (weight 9, count 1-4) or Crude Cladding (weight 1).
- Patrol leader: guaranteed Cladding Upgrade Smithing Template; 50% chance for damaged Cladded Boots/Leggings/
  Chestplate; and a guaranteed weighted Crude result (Fragment weight 8, 1-4; Cladding weight 3).
- Pillager Outpost supplemental chest loot guarantees the template.

Four Crude Fragments craft one Crude Cladding; one Cladding yields four Fragments; nine Cladding form a storage block
and the reverse recipe exists. Cladded Stone consumes one Cladding and four Smooth Stone. The template duplication
recipe uses the existing template, Mesmerite and seven Iron Ingots.

Final cladded armor is a dedicated armor material/item set, not a visual armor trim. It uses smithing-transform recipes
with Leather Armor as base, Crude Cladding as addition, and the template. Implementation must source the exact final
material attributes and mixin behavior during Stage 12, including projectile reduction/knockback semantics, dye/trim
models, durability and component migration. No audit-time approximation is authorized.

## 5. Stone Golem contract

The Stone Golem is a registered creature with custom multipart renderer/model, damage cracks, turn/stop/hurt/death/
step/repair/arm sounds and Crossbow item layer. It is player-buildable from a Cladded Stone pattern through the final
block-pattern/mixin path. It persists `PlayerCreated`, anger and Crossbow/turn state. A player or dispenser can arm it
with a Crossbow, triggering the custom Arm Golem advancement. It has ranged Crossbow AI when armed, melee/neutral
anger behavior otherwise, target rules that distinguish player-created and hostile/structure contexts, and can be
healed with `#biomemakeover:heals_stone_golem` (Cladded Stone). Normal loot is 2-3 Cladded Stone.

The Adjudicator phase creates a separate temporary instance, blocks its loot, equips a Crossbow, makes the boss ride
it, targets Players, and discards it when that phase exits. Do not conflate this with the player-created sentry.

## 6. Adjudicator encounter

The final boss is a distinct entity with health 255, armor/movement/attack attributes, a blue server boss bar, arena
bounds/positions and persistent phase data. It activates through room/player encounter logic and only shows/tracks the
boss bar for Players in the boss room. Damage is filtered by phase; idle non-player damage is rejected, and invulnerable
phases honor bypass tags.

Registered phases are:

- Idle activation.
- Teleport.
- Bow attack.
- Melee attack.
- Evoker Fang attack.
- Ravager charge behavior.
- Summon two Evokers.
- Summon six Vindicators.
- Summon two Vexes.
- Mixed summons from Vex/Vindicator/Evoker/Pillager.
- Mimic phase using phase-only Adjudicator Mimics.
- 100-tick Fang Barrage.
- Stone Golem mounted/Crossbow phase.

Phase state, phase-specific NBT, boss active flag, home/room bounds and arena positions survive save/reload. The entity
has custom states/model/eyes/held-item rendering, teleport particles, spell/combat/death sounds and multiplayer target
selection. Replacing it with an Illusioner would discard the released encounter.

Boss loot is exactly one `enchanted_totem` and one `adjudicator_tapestry`. The Enchanted Totem is not an enchanted
vanilla Totem stack: it is a dedicated item with custom activation semantics. It revives at half max health, removes
all effects, and applies Regeneration II for 500 ticks, Absorption IV for 1200, Fire Resistance for 2000 and Resistance
for 2000.

## 7. Tapestry and music-disc reward ownership

Sixteen ordinary dye Tapestries are placed by randomized Mansion markers. The Adjudicator Tapestry is the guaranteed
boss collectible. The `all_tapestries` advancement explicitly requires all 16 colors plus the Adjudicator variant.
No authored Tapestry crafting recipes were found, supporting the showcase collectible/loot contract.

The Dark Forest record is `red_rose_music_disk` (“Damiano Baldoni - Red Rose”), sound event `red_rose`, comparator
value 2, duration metadata 135. It appears in Mansion `good` and `dungeon_good` loot, not the Adjudicator entity table.
The authored `dark_forest_disc` advancement triggers on inventory acquisition. This supports the broader BM pattern of
theme/structure-specific exploration records, but each disc retains its own structure loot owner.

## 8. Advancement inventory

Mansion/Dark Forest progression advancements found in final data:

- `enter_dark_forest`
- `mansion` (location predicate for `biomemakeover:mansion`)
- `illunite_shard`
- `altar`
- `crude_cladding`
- `cladding_template`
- `cladded_armor` (all four armor items)
- `arm_golem` (custom trigger)
- `all_tapestries` (all 17 variants)
- `enchanted_totem`
- `dark_forest_disc`
- `cursed_hat` and the larger Altar/Witch progression branch where applicable

Rootling/Moth advancements are in the Dark Forest companion audit. The current port lacks this released progression
tree. Modern implementation must translate location/item/enchantment predicates and the custom Arm Golem trigger;
missing dependencies must not be silenced by deleting advancements.

## 9. Showcase vs final-release evolution/dead content

- The custom Mansion, dungeon, boss, Tapestries, cladding, Golem and record are all final reachable; none is merely
  showcase-only.
- The exact final resource tree has 168 Mansion NBT files, not the roadmap shorthand of 228. Stage 11 must reconcile
  why the older audit counted 228 before declaring a deterministic template contract.
- The Mansion `bonemeal` marker is explicitly disabled in final code.
- Adjudicator Mimics are phase-only and must not receive normal spawn/acquisition paths.
- Directional Data is a hidden structure-development/runtime marker, not ordinary survival content.
- The record is Mansion chest loot; showcase proximity to the boss does not make it a boss drop.
- “Crud” is not a canonical registry name. Final IDs and translations use **Crude**.

## 10. Dependency graph

```text
Stage 6 Ancient Oak/Ivy/Mesmerite/Illunite
        |              |              |
        v              v              v
Stage 9 Tapestry + Directional Data + Altar
        |                             |
        v                             v
Stage 11 Mansion structure       Stage 12 curses
        |
        +--> loot/mob markers --> Pillagers/Owls/Tapestries
        |
        +--> dungeon --> Adjudicator
                           |--> Mimics
                           |--> temporary Stone Golem phase
                           |--> Enchanted Totem
                           `--> Adjudicator Tapestry

Pillager loot --> Crude Fragment/Cladding --> Cladded Stone --> player Stone Golem
                         |
                         `--> template + Leather Armor --> Cladded Armor

Mansion good/dungeon-good loot --> Red Rose music disc
```

## 11. Priority queue

### Woodland Mansion P0 — structure/layout/dungeon

1. Stage 9 Directional Data/Tapestry foundations and exact serialized contracts.
2. Stage 11 structure type/piece/codec and structure set.
3. Deterministic 168-file template/reference manifest and discrepancy resolution.
4. Layout/room/wall/roof/tower assembly.
5. Dungeon stairs, room layer, boss room and terrain integration.
6. Marker processors, mob pools and seven loot tiers.

### Woodland Mansion P1 — boss/construct/progression

1. Stone Golem player construction, allegiance, arming and persistence.
2. Adjudicator room activation, bounds and boss bar.
3. All selectable phases and phase-only Mimic/Golem lifecycles.
4. Multiplayer/save-reload encounter behavior.
5. Stage 9 Altar and Stage 12 curses.

### Woodland Mansion P2 — loot/cladding/rewards/advancements

1. Pillager/leader/outpost supplemental loot.
2. Crude/Cladding storage, template, dedicated armor and Cladded Stone.
3. Tapestry acquisition/collection and Adjudicator reward.
4. Enchanted Totem semantics.
5. Red Rose disc/audio/loot.
6. Full advancement chain and custom triggers.

### Woodland Mansion P3 — cosmetic/minor parity

1. Tapestry renderer/item renderer and all textures.
2. Boss/Golem models, cracks, eyes, held items and particles.
3. Exact sound families and loop/static safety.
4. Mansion decoration/Ivy/mushroom marker presentation.

## 12. Recommended implementation order

1. Finish and runtime-accept Stage 6 physical dependencies.
2. Finish Stage 7/8 entity dependencies without coupling Mansion implementation to Mythas behavior.
3. Stage 9 Directional Data, Tapestry and Altar functional infrastructure.
4. Stage 11A structure registry/codecs/serialization and structure-set placement.
5. Stage 11B exact templates, marker interpretation, processors and loot references.
6. Stage 11C layout, terrain adaptation, dungeon and fresh-region generation tests.
7. Stage 12 cladding and player Stone Golem vertical chain.
8. Stage 12 Adjudicator/Mimic encounter and rewards.
9. Stage 12 Altar/curses and connected progression.
10. Stage 13 packaged-resource, multiplayer, existing-world and side-by-side freeze.

## 13. Mythas candidates (not parity)

- Living World events involving the Mansion or Dark Forest.
- External hooks into the restored Witch progression rather than changing BM ownership.
- Reuse of modular structure concepts for future Mythas caravans/travel structures.
- Optional BM mob heads after the parity freeze.

No candidate is implemented or approved by this audit.
