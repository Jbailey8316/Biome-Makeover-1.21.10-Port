# Stage 12A.1 — Adjudicator / Mansion Boss Parity Audit

Status: `COMPLETE / IMPLEMENTATION PLAN READY`  
Scope: final released Biome Makeover 1.20.1 source/resource audit only. No
gameplay or NBT changes were made in this stage.

## 1. Released boss identity and registration

The released Mansion boss is `biomemakeover:adjudicator`, implemented by
`entity/adjudicator/AdjudicatorEntity.java`. It is a fire-immune,
no-summon `MONSTER` entity with dimensions `0.6 x 1.95`, tracking range 12.
The registration also includes `biomemakeover:adjudicator_mimic` with the same
dimensions and `biomemakeover:stone_golem` at `1.6 x 2.5`.

Released Adjudicator attributes are:

| Attribute | Released value |
|---|---:|
| Max health | 255 |
| Movement speed | 0.25 |
| Attack damage | 3 |
| XP reward | 50 |

No custom armor, knockback-resistance, or fire-damage attribute is declared;
those remain the inherited/default Monster values. Fire immunity is supplied
by the entity registration. The boss sets persistence, does not despawn
(`checkDespawn` resets `noActionTime`), cannot change dimensions, has no fall
damage, and is not pushable. A blue progress `ServerBossEvent` is maintained
for players in the arena.

Relevant released registration and attribute sites:

- `reference/Biome-Makeover-1.20/common/src/main/java/party/lemons/biomemakeover/init/BMEntities.java`
- `reference/Biome-Makeover-1.20/common/src/main/java/party/lemons/biomemakeover/entity/adjudicator/AdjudicatorEntity.java`

The current 1.21.10 port has no Adjudicator, Adjudicator Mimic, or Stone Golem
entity class/registration/renderer. Its only current Adjudicator matches are
the existing `adjudicator_tapestry` block/item resources.

## 2. Released Mansion marker and spawn path

The released `MansionFeature.Piece.handleDataMarker` dispatches exact metadata
`boss` to `spawnBoss(level, pos)`. The released implementation is:

```java
AdjudicatorEntity boss = BMEntities.ADJUDICATOR.get().create(level.getLevel());
boss.setPersistenceRequired();
boss.moveTo(pos, 0.0F, 0.0F);
boss.finalizeSpawn(level, level.getCurrentDifficultyAt(pos),
    MobSpawnType.STRUCTURE, null, null);
level.addFreshEntityWithPassengers(boss);
level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
```

Thus the marker is consumed after insertion, the spawn reason is
`STRUCTURE`, finalization is called, and there is no clearance test,
relocation, block clearing, retry, or explicit arena setup in the marker
callback. Position uses the released 1.20.1 `moveTo(BlockPos, yaw, pitch)`
semantics; the boss later derives `homePos` from its first server tick and
builds the arena state from the authored `SMOOTH_QUARTZ` `arena_pos` markers.
The released handler does not set hostile state directly: the boss begins in
the idle phase and activates on player damage or an arena room edit.

Current port behavior at `MansionFeature.java` is only:

```java
case "boss" -> level.setBlock(position, Blocks.AIR.defaultBlockState(), 2);
```

This is a missing boss spawn path, not a changed released spawn contract.

## 3. Released Adjudicator combat system

The boss uses a registered phase map and starts in `idle`. The default phase
transition is to `teleport`; teleport lasts 30 ticks and selects a random
selectable phase, teleports to that phase's start position, and then enters
combat. Timed attacking phases last 200 ticks unless their special phase
ends earlier.

| Phase | Trigger/exit | Released behavior |
|---|---|---|
| `idle` | Ends when `active` becomes true | Heals 1 health every 4 ticks; boss bar hidden; not selectable. |
| `teleport` | 30 ticks | Chooses another selectable phase, emits teleport/ender effects, then teleports home/start and enters fighting state. |
| `bow_attack` | 200-tick attacking phase | Ranged bow goal, speed 0.75, attack interval 12, minimum range 30; equips bow and clears it on exit. |
| `melee_attack` | 200-tick attacking phase | `MeleeAttackGoal`, speed 1, equipped iron axe; clears hand on exit. |
| `fang_attack` | Timed attacking phase / goal completion | Evoker-fang attack: five close fangs plus eight more at range, or a 16-fang directed line; floor/collision-aware placement. |
| `fang_barrage` | 100 ticks | Every 50 ticks emits four directions of ten fangs, with random yaw and floor search. |
| `ravager` | Ends when mounted Ravager is gone | Spawns one loot-blocked Ravager at the boss, mounts it, equips Multishot III crossbow, and is invulnerable while mounted. |
| `spawn_evoker` | 120 ticks, two entities | Summons two Evokers at arena positions, one per interval; interruption causes remaining summons on exit. |
| `spawn_vindicator` | 120 ticks, six entities | Same summon lifecycle with six Vindicators. |
| `spawn_vex` | 120 ticks, two entities | Same lifecycle with two Vexes. |
| `spawn_mix` | 120 ticks, three entities | Three random entities from Vex, Vindicator, Evoker, and Pillager. |
| `mimic` | Ends when boss is hurt | Spawns 3–6 phase-only Adjudicator Mimics at unique arena positions; discards them on exit. |
| `stone_golem` | Ends when boss is no longer a passenger | Spawns one loot-blocked Stone Golem, equips the boss with a Punch I bow, mounts the golem, and is invulnerable while mounted. |

Attacking phases target players and relevant golems, use ordinary movement/look
goals, and transition through teleport rather than directly chaining combat
phases. The boss is room-bound: it maintains arena bounds, returns home when
outside, and clears suitable obstruction around teleports/summons when mob
griefing permits. Damage is blocked while idle or during an invulnerable
phase; player damage activates the encounter.

## 4. Stone Golem relationship

`biomemakeover:stone_golem` is a separate, registered multipart neutral golem,
not an Adjudicator-only entity. The released class is
`entity/StoneGolemEntity.java`; it is player-buildable from the Cladded Stone
pattern, can be player-created or naturally/event spawned, uses Crossbow AI,
has `MAX_HEALTH=60`, and has multipart body/base collision and custom
crackiness/turning/repair behavior. It targets according to player-created
state and anger rules and can be healed with the `heals_stone_golem` item tag
(Cladded Stone). Normal death loot is 2–3 Cladded Stone from
`data/biomemakeover/loot_tables/entities/stone_golem.json`.

The boss uses a distinct temporary Stone Golem phase instance: it is
loot-blocked, mounted by the boss, armed as part of the phase, and discarded
when the phase ends. Boss progression does not require killing ordinary Stone
Golems; the phase ends when the boss is no longer riding its temporary golem.

## 5. Mimic relationship

`biomemakeover:adjudicator_mimic` is a registered no-normal-spawn monster,
implemented by `AdjudicatorMimicEntity`, and is only created by
`MimicPhase`. Each encounter phase creates a random 3–6 unique-position group
in the arena, equips each with a bow, finalizes it as a natural spawn, and
removes the group when the phase ends. The phase ends when the Adjudicator is
hurt. Mimics use the Adjudicator appearance/sounds and ranged bow behavior;
they are not a general Mansion marker, block, or ordinary loot source.

## 6. Rewards and progression

Released `data/biomemakeover/loot_tables/entities/adjudicator.json` has two
one-roll item pools, with no conditions:

- exactly one `biomemakeover:enchanted_totem`;
- exactly one `biomemakeover:adjudicator_tapestry`.

The Adjudicator custom death loot path evaluates this entity table and spawns
the results with extended item lifetime. This is a guaranteed entity reward,
not Mansion chest loot. The released Mansion good/dungeon-good tables contain
other progression items such as the Red Rose disc; the Red Rose disc is not in
the boss entity table.

`data/biomemakeover/advancements/biomemakeover/enchanted_totem.json` is an
inventory-change advancement parented to the Mansion advancement and triggers
on obtaining the Totem. The Adjudicator tapestry is the boss collectible and
also participates in the all-tapestries advancement.

## 7. Enchanted Totem

The released item is `biomemakeover:enchanted_totem`, an EPIC, stack-size-one
`EnchantedTotemItem`. It is foil and implements the released Taniwha Totem
activation contract. Activation is valid whenever the item is selected by the
totem hook (the item itself does not impose a separate health threshold); it:

1. awards the vanilla used-totem statistic and trigger for a ServerPlayer;
2. sets health to half maximum;
3. clears all effects;
4. grants Regeneration II for 500 ticks;
5. grants Absorption IV for 1200 ticks;
6. grants Fire Resistance for 2000 ticks;
7. grants Resistance for 2000 ticks;
8. broadcasts the vanilla totem entity event.

It is single-use through the normal TotemItem consumption hook. Released
assets include `models/item/enchanted_totem.json` and
`textures/item/enchanted_totem_of_undying.png`. No custom boss-only component
or special NBT payload is used by this item in the released source.

## 8. Crude/cladding dependency map

| Dependency | Released role | Current 1.21.10 state |
|---|---|---|
| `crude_fragment` | Pillager/Mansion progression material; four craft one `crude_cladding` | Registered/resource substrate present; acquisition/progression deferred. |
| `crude_cladding` | Upgrade material; reversibility/storage recipes | Registered/resource substrate present; recipes/activation deferred. |
| `crude_cladding_block` | Nine cladding storage block and reverse recipe | Missing gameplay activation in current Stage 12A.0 scope. |
| Cladding Upgrade Smithing Template | Smithing input for armor | Released asset/recipe is in reference; current activation not restored. |
| Cladded armor | Leather armor transformed by template + crude cladding; dedicated armor set | Registration/equipment substrate present; recipes and progression deferred. |
| `cladded_stone` | Four Smooth Stone + cladding; Stone Golem construction/healing material | Registration/resource substrate present; recipe and golem dependency deferred. |

The released boss does not require crude/cladding items to spawn or fight. The
dependency is downstream progression and the separate player-created Stone
Golem chain. Stage 12A.0 therefore supplies substrate but intentionally does
not activate this graph.

## 9. Released client/resource inventory

Released boss-related resource families are present in the reference under
`common/src/main/resources`:

- Adjudicator entity texture and emissive eye texture;
- Adjudicator model/renderer registration and phase-state presentation;
- Stone Golem base and three damaged textures, model, renderer, and spawn egg;
- Adjudicator and Stone Golem sound event families and audio files;
- Adjudicator entity loot table and Enchanted Totem advancement;
- Enchanted Totem item model/texture;
- Stone Golem entity loot table, healing tag, and related cladding recipes;
- released English translations for Adjudicator, Mimic, Stone Golem, Totem,
  and boss subtitles.

The current port has no Adjudicator/Mimic/Stone Golem Java registration,
entity renderer/model, entity textures, boss sounds, boss loot table, Totem
item/behavior, or Totem advancement. It does have the Adjudicator tapestry
content and the Stage 12A.0 cladded/crude item substrate. The current port's
Mansion advancement and arena marker conversion remain present, but they do
not create the boss.

## 10. Current gap map and implementation order

| Released feature | Current port | Classification |
|---|---|---|
| `boss` marker dispatch to Adjudicator | Marker is cleared only | MISSING |
| Adjudicator registration/attributes/state persistence | No entity | MISSING |
| Arena initialization and room listener | Arena marker conversion exists; no boss consumer | PARTIAL |
| Adjudicator phase engine and AI | No classes | MISSING |
| Adjudicator boss bar/client renderer | No classes/resources | MISSING |
| Mimic entity and phase-only lifecycle | No classes/resources | MISSING / intentionally deferred |
| Stone Golem player entity | Stage 12A.0 substrate only | PARTIAL / deferred |
| Stone Golem boss phase | No boss | MISSING / deferred |
| Adjudicator loot | No entity loot path | MISSING |
| Enchanted Totem behavior/assets/advancement | No current implementation | MISSING |
| Crude/cladding recipes and progression | Registration substrate only | PARTIAL / deferred |
| Adjudicator tapestry reward | Tapestry item/block is present | PRESENT |

Recommended released-parity implementation order, derived from source
coupling:

1. Port/register Adjudicator base entity, attributes, persistence, boss bar,
   room bounds/listener, arena initialization, and the `boss` marker spawn.
2. Port shared AI/phase contracts and core teleport/idle/attacking phases.
3. Port fangs, bow/melee, barrage, summon, and Ravager phases.
4. Port/register Stone Golem and its client model/renderer, then add the
   temporary boss Stone Golem phase.
5. Port/register phase-only Adjudicator Mimic and its client presentation.
6. Port Adjudicator death loot and the Enchanted Totem item/modern
   activation bridge, followed by its advancement.
7. Restore released cladding recipes, Stone Golem construction/healing, and
   the remaining progression/advancement chain.

The boss must precede its phase-only entities and rewards; the ordinary Stone
Golem/cladding chain can be implemented separately but is required before
player-created Golem parity is complete.

## 11. Released parity versus Mythas boundary

This audit covers released behavior only. It does not authorize the separate
Mythas enhancement design: Patrol/Enforcer/Captain trial spawners, three
fragments, per-player Mansion Emerald Key, keyed activation, Manor Cache, or
once-per-player Mansion identity. Those belong to a later enhancement layer
after the released Adjudicator substrate is restored.

## 12. Audit validation and repository state

- No gameplay Java or resource implementation was changed.
- No Mansion NBT was changed.
- Existing source-reference, Mansion closure, template inventory,
  marker-support, and resource-inventory checks are the applicable
  non-mutating validation scope.
- The repository baseline remains the requested clean/pushed HEAD.

This document is the only Stage 12A.1 artifact; implementation is intentionally
left for the next stage.
