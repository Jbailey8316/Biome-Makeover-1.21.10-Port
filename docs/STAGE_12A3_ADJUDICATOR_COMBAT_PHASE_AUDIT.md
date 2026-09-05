# Stage 12A.3 — Adjudicator Combat / Phase System Audit

Status: `COMPLETE / IMPLEMENTATION PLAN READY`  
Scope: final released Biome Makeover 1.20.1 source audit only. No gameplay,
resource, or NBT changes were made.

## 1. Released controller and phase inventory

The released Adjudicator is a custom phase controller. It constructs and
registers these phases in `AdjudicatorEntity`:

| ID | Class | Duration / exit | Selection and principal behavior |
|---|---|---|---|
| `idle` | `IdleAdjudicatorPhase` | Until `active`; heals 1 every 4 ticks | Not selectable; boss bar hidden. |
| `teleport` | `TeleportingPhase` | 30 ticks | Not selectable; uniformly chooses a selectable phase, emits teleport effects, then moves to that phase's arena start position. |
| `bow_attack` | `BowAttackingPhase` | 200 ticks | `RangedBowAttackGoal`, speed 0.75, interval 12, minimum range 30; equips a bow and removes it on exit. |
| `melee_attack` | `MeleeAttackingPhase` | 200 ticks | `MeleeAttackGoal`, speed 1, long-memory enabled; equips an iron axe and removes it on exit. |
| `fang_attack` | `FangAttackingPhase` | 200-tick attacking shell; goal ends with target/lifecycle | Evoker fangs; sets summoning render state. Five close fangs plus eight at 2.5 blocks when target distance is under 24, otherwise 16 fangs in a 1.25-block-spaced line. Warmups are 0/3 or line index. |
| `fang_barrage` | `FangBarragePhase` | 100 ticks | At ticks 50 and 100, ten fangs in each horizontal cardinal direction; 1–10 block spacing, random yaw, warmup 0–9; start position is home. |
| `ravager` | `RavagerChargePhase` | Until no longer mounted | Creates one loot-blocked Ravager, mounts it, equips the boss a Multishot III crossbow, and makes the phase invulnerable. |
| `spawn_evoker` | `SummonPhase` | 120 ticks / two spawned / player hit | Two Evokers, one every 60 ticks; removes their summon-spell goal; remaining entities are spawned on clean exit. |
| `spawn_vindicator` | `SummonPhase` | 120 ticks / six spawned / player hit | Six Vindicators, one every 20 ticks; same interruption and cleanup contract. |
| `spawn_vex` | `SummonPhase` | 120 ticks / two spawned / player hit | Two Vexes, one every 60 ticks; same contract. |
| `spawn_mix` | `SummonPhase` | 120 ticks / three spawned / player hit | Three random Vex/Vindicator/Evoker/Pillager entities, one every 40 ticks. |
| `mimic` | `MimicPhase` | Until boss is hurt by a player | Extends bow phase; creates 3–6 unique-position phase-only Mimics and discards them on exit. |
| `stone_golem` | `StoneGolemPhase` | Until no longer passenger | Creates one loot-blocked Stone Golem, mounts it, equips the boss a Punch I bow, and makes the boss invulnerable. |

`AttackingPhase` supplies Float, RandomStroll, the phase attack goal,
LookAtPlayer, and RandomLookAround. Its targets are HurtByTarget, nearest
Player, and nearest AbstractGolem. The Ravager and Golem phases replace this
with mounted crossbow/non-moving-bow behavior and retain player/golem target
selection.

## 2. Selection and transitions

The entity starts in `idle`. Damage activates the encounter; idle ends when
`active` is true. The normal phase transition is `phase.getNextPhase()`,
whose default is `teleport`. Teleport builds a list from
`PHASES.values().filter(AdjudicatorPhase::isSelectable)` and selects uniformly
with `level.random.nextInt`; it excludes itself by retrying. Thus there is no
health threshold, weighted table, cooldown history, target-distance weighting,
or recently-used exclusion in the released selector.

The four summon phases are selectable only when fewer than four living
`Monster` entities are already in the arena bounds. Idle and teleport are not
selectable. Fang barrage and the mounted phases start at home. A phase enters
by stopping current goals, copying the phase goal/target selectors, setting
boss-bar visibility and the synced invulnerability flag, and exits through
teleport unless its own `getNextPhase`/completion rule says otherwise.

Player damage activates the fight and is forwarded to the phase. Summon and
Mimic phases use a player hit as their early-exit signal. Mounted phases end
when the boss is no longer a passenger. Idle is the only healing phase.

## 3. Ability contracts

Teleport selects an arena position, broadcasts Ender/teleport events every
tick, plays the released spell sound on entry, emits block-Ender particles,
and moves on exit. The destination is selected from authored smooth-quartz
arena positions (with the home position included); the entity helper places
cobblestone below an air destination and clears mob-griefable obstruction.

Bow uses a vanilla bow and `RangedBowAttackGoal(adjudicator, 0.75F, 12, 30)`.
The entity's ranged implementation uses a mob arrow, velocity 1.6, and
difficulty-dependent inaccuracy `14 - difficultyId * 4`, with the released
0.2 arc term. Melee uses the vanilla melee goal and iron axe; no custom reach
or damage multiplier is added beyond attributes/vanilla goal behavior.

Fang attacks use `EvokerFangs`. For close range, the five inner fangs use
radius 1.5 and angular increment `PI * .4`; the eight outer fangs use radius
2.5 and increment `PI / 4` with a `1.2566371` offset. Long range uses 16
fangs at `1.25 * (index + 1)` along the target yaw. Each placement searches
downward for a sturdy floor and places the fang at the collision-shape top.
The barrage repeats four cardinal ten-fang lines every 50 ticks.

## 4. Ravager and summon relationships

The Ravager phase creates a temporary loot-blocked vanilla Ravager at the
boss position, equips the boss with a Multishot III crossbow, mounts the boss,
and removes the Ravager when the phase exits. It is not a normal Mansion
marker and does not alter the frozen tree-intrusion protection. The Golem
phase follows the same temporary-mounted pattern with a loot-blocked
Biome Makeover Stone Golem and Punch I bow; it ends when mounting ends.

Each summon uses an authored arena position, centers X/Z by 0.5, uses
`MobSpawnType.EVENT`, blocks loot, inserts the entity, clears obstruction, and
plays the Evoker summon sound. Unused positions show particles and a clean
phase exit completes remaining summons. The released Evoker summon goal is
removed from summoned Evokers. No summoned entity is persisted as a reward.

## 5. Mimic phase

`biomemakeover:adjudicator_mimic` is phase-only. `MimicPhase` creates a random
3–6 unique-position group, uses the arena positions, centers each entity,
finalizes with the released natural-spawn reason, blocks loot, clears
obstruction, and broadcasts Ender particles. It extends bow behavior, so
Mimics use the Adjudicator appearance and ranged combat. A player hit ends the
phase; all Mimics in the arena are discarded on exit. The current port has no
Mimic entity, renderer, or phase implementation, so this dependency is not
available to a controller port yet.

## 6. Stone Golem separation

The released `biomemakeover:stone_golem` is an independent player-creatable
entity with 60 health, custom multipart model/renderer, damage/crack state,
crossbow AI, repair/healing tags, construction rules, sounds, spawn egg, and
entity loot. The Adjudicator phase is a separate temporary, loot-blocked
mounting use and does not replace ordinary Golem parity. Current Stage 12A.0
contains only cladding/crude substrate; both the independent entity and boss
phase are missing.

## 7. Synced state, boss bar, and persistence

The released SynchedEntityData fields are:

| Field | Type | Purpose |
|---|---|---|
| `STATE` | Integer | Four client presentation states: `WAITING`, `TELEPORT`, `FIGHTING`, `SUMMONING`. |
| `CHARGING` | Boolean | Crossbow charging presentation/state. |
| `INVULNERABLE` | Boolean | Mirrors the active phase's damage immunity and power presentation. |

The blue progress `ServerBossEvent` is named from `getDisplayName`, hidden in
idle, and tracks players inside the arena bounds. It updates progress every
tick, adds/removes players by arena membership, removes all players on death,
and removes players leaving visibility. Custom-name changes update the bar.

The entity saves `FirstTick`, `BossActive`, home position, room bounds, arena
positions, phase ID, phase-specific `PhaseData`, and the presentation `State`.
Timed phases save their timer; summon phases save `SpawnIndex`; Mimic saves
its hit flag; teleport saves its destination. On reload the phase is looked
up, phase data restored, selectors reinstalled, and state reapplied. Temporary
Mimics, Ravagers, and Golems are not serialized as phase rewards; their
mid-fight cleanup behavior must be verified during implementation because the
released source does not provide a general cross-entity recovery transaction.

## 8. Death and reward boundary

Released death evaluates `data/biomemakeover/loot_tables/entities/adjudicator.json`
with two unconditional one-item pools: one Enchanted Totem and one
Adjudicator Tapestry, plus XP 50. Drops use the released extended item
lifetime behavior. Death removes the boss bar and exits the active phase.
The Enchanted Totem advancement is an inventory-change advancement. These
rewards and the Totem behavior remain deferred and are not part of this
combat audit implementation.

## 9. Current gap and client/resource map

| Feature | Current 1.21.10 port | Classification |
|---|---|---|
| Phase controller/state map | Substrate only | MISSING |
| Goal/target phase selectors | None | MISSING |
| ServerBossEvent arena controller | None in substrate | MISSING |
| Teleport/fang/bow/melee | None | MISSING |
| Vanilla summon phases | None | MISSING |
| Temporary Ravager phase | None | MISSING; frozen spawn/tree safety remains separate |
| Mimic entity/renderer/assets | None | DEFERRED / MISSING |
| Independent Stone Golem | Substrate not active | PARTIAL / DEFERRED |
| Boss Golem phase | None | DEFERRED / MISSING |
| Phase sounds/particles | Base lifecycle sounds only | PARTIAL |
| Phase model poses/layers | Neutral model only | PARTIAL |
| Fang/projectile resources | Vanilla EvokerFangs dependency absent from custom controller | MAPPED API |
| Boss loot/Totem/reward advancement | Not active | DEFERRED |

Released client dependencies include phase pose/state presentation, held-item
and eyes layers, teleport/summon/fang effects, Adjudicator cast/grunt/laugh/no
sound families, Mimic presentation, Stone Golem model and damaged textures,
and the vanilla projectile/entity render paths. The current accepted base
model/eyes texture and lifecycle sounds are present; phase-specific sounds,
Mimic, Golem, and combat presentation are not.

## 10. 1.21.10 port risks

Base phase classes are a direct conceptual port, but entity construction and
spawn calls require mapped 1.21.10 `EntitySpawnReason`/`ServerLevel` APIs.
Goal selector copying is a custom extension and must be ported or replaced
with an equivalent selector installation. `SynchedEntityData`, NBT compound
access, `ServerBossEvent`, damage-source tags, item enchantment APIs,
crossbow/bow interfaces, `EvokerFangs`, navigation, and model render-state
APIs all require mapped 1.21.10 compilation checks. Worldgen entity access
must not be reused for combat-time `ServerLevel` operations; the prior
Mansion deadlock repairs remain a hard boundary.

## 11. Recommended decomposition

1. **12A.4 — controller substrate:** synced fields, phase interfaces,
   boss-bar arena tracking, save/load, activation, and inert idle/teleport
   test. Depends on the accepted entity substrate; acceptance is one boss,
   stable save/reload, and no combat abilities yet.
2. **12A.5 — vanilla combat:** bow and melee phases plus target/navigation
   selectors. Test controlled player combat and 200-tick exits.
3. **12A.6 — fangs:** close/long Fang phase and 100-tick barrage. Test all
   floor/arena positions and no worldgen-path access.
4. **12A.7 — vanilla summons/Ravager:** Evoker/Vindicator/Vex/mix restrictions
   and temporary Ravager mount. Test count limit, interruption, cleanup, and
   existing tree-safety behavior.
5. **12A.8 — Mimic entity and phase:** register phase-only entity, renderer,
   bow behavior, group cleanup, and player-hit exit.
6. **12A.9 — independent Stone Golem:** entity, model, sounds, loot,
   construction/healing, and save/reload.
7. **12A.10 — mounted Golem phase:** temporary blocked Golem, Punch bow,
   mount/vulnerability/cleanup contract.
8. **12A.11 — rewards:** Adjudicator loot, Enchanted Totem behavior and
   advancement, Adjudicator Tapestry reward, and released cleanup.

The separate Mythas trial/fragment/key/cache/vault system is not in this
sequence and remains a future enhancement after released behavior is stable.

## 12. Validation and repository state

- No gameplay Java or resource implementation changed.
- No Mansion NBT changed.
- Mansion closure and marker validation passed.
- Mansion inventory remains 168 total / 165 active / 3 released orphans.
- `git diff --check` passed.
