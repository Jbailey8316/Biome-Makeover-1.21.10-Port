# Stage 14B.3 — Mythas Manor Caches and Fragment Progression

Status: implemented standalone native Vault cache contracts, guaranteed
fragment rewards, and the inert Mansion Emerald Key recipe. Mansion
integration, cache placement, boss gating, and final rewards remain deferred.

This document is an enhancement design, not released Biome Makeover parity.
The released-parity baseline remains frozen at 168 Mansion templates, 165
active templates, and three released orphans. With Mythas disabled, the
Mansion must retain that behavior and inventory exactly.

## 1. Vanilla 1.21.10 Trial Spawner audit

The current game uses `TrialSpawnerBlock` with a
`TrialSpawnerBlockEntity`. The block entity owns a `TrialSpawner` controller;
the controller owns state, player detection, configuration, and ominous
state. The relevant mapped classes are:

- `net.minecraft.world.level.block.TrialSpawnerBlock`
- `net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity`
- `net.minecraft.world.level.block.entity.trialspawner.TrialSpawner`
- `net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerConfig`
- `net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState`
- `net.minecraft.world.level.block.entity.trialspawner.PlayerDetector`

The block entity delegates server ticking to `TrialSpawner.tickServer` and
client ticking to `tickClient`. The controller's state machine includes
`INACTIVE`, `WAITING_FOR_PLAYERS`, `ACTIVE`, `WAITING_FOR_REWARD_EJECTION`,
`EJECTING_REWARD`, and `COOLDOWN`. It persists controller state/config data
through the block entity's load/store path, so unload/reload is not inherently
volatile.

The current `TrialSpawnerConfig` record is codec-backed and contains:

| Field | Meaning |
| --- | --- |
| `spawnRange` | spawn search/range value |
| `totalMobs` | base total target |
| `simultaneousMobs` | base simultaneous target |
| `totalMobsAddedPerPlayer` | scaling per detected player |
| `simultaneousMobsAddedPerPlayer` | simultaneous scaling per player |
| `ticksBetweenSpawn` | delay between spawn attempts |
| `spawnPotentialsDefinition` | weighted entity spawn potentials |
| `lootTablesToEject` | completion ejection loot tables |
| `itemsToDropWhenOminous` | ominous-mode item behavior |

The config exposes `calculateTargetTotalMobs(playerCount)` and
`calculateTargetSimultaneousMobs(playerCount)`. Player detection has explicit
selectors for normal operation and creative-inclusive operation. The
controller also exposes player-range, cooldown, spawn, ominous, and reward
ejection operations. These are the correct reuse points for scaling, player
count changes, completion, cooldown, and serialization.

The vanilla configuration examples are data resources under
`data/minecraft/trial_spawner/...`, with weighted `spawn_potentials` entries
and entity data. The Mythas configurations use the same current registry
directory under `data/biomemakeover/trial_spawner/...`; their registry IDs are
`biomemakeover:mythas/mansion/patrol`,
`biomemakeover:mythas/mansion/enforcer`, and
`biomemakeover:mythas/mansion/captain`. The direct config JSON is the current
1.21.10 format; there is no separate nested `normal_config` object in the
registry resource. A placed Trial Spawner can select a normal or ominous
registry configuration through its block-entity data, but these Mythas
resources intentionally provide no ominous configuration.

### Recommendation

Reuse the native Trial Spawner block, block entity, controller, state machine,
player detector, codec, scaling, cooldown, and reward-ejection machinery.
Use three data-driven configs and a thin Mansion-local completion/eligibility
controller only where vanilla has no per-participant distribution concept.
Do not subclass `TrialSpawnerBlockEntity` unless a later implementation
proves that config/bootstrap injection cannot express the required behavior.

The vanilla spawner's reward ejection is a physical completion event; it is
not a reliable per-player key distributor. Stage 14B.2 deliberately uses one
guaranteed corresponding key loot-table opportunity per completed standalone
spawner. It adds no custom distribution bookkeeping. Per-participant key
distribution, if still required by the locked progression contract, remains
an explicit later implementation decision and is not silently approximated
here.

## 1.1 Implemented standalone configurations

| Trial | Config ID | Spawn potentials (weight) | Base / per-player total | Base / per-player simultaneous | Interval | Reward |
| --- | --- | --- | --- | --- | ---: | --- |
| Patrol | `biomemakeover:mythas/mansion/patrol` | Pillager (4), Vindicator (1) | 6 / 2 | 2 / 1 | 20 ticks | `patrol_trial_key` |
| Enforcer | `biomemakeover:mythas/mansion/enforcer` | Vindicator (5), Pillager (2) | 8 / 2 | 3 / 0.5 | 20 ticks | `enforcer_trial_key` |
| Captain | `biomemakeover:mythas/mansion/captain` | Evoker (2), Vindicator (5), Pillager (3) | 10 / 1.5 | 3 / 0.5 | 20 ticks | `captain_trial_key` |

All three use `spawn_range: 4`. Their reward lists contain exactly one
weight-1 loot table and each loot table contains exactly one guaranteed key.
They have no Fragment, Mansion Emerald Key, Manor Vault Key, or secondary
loot reward. The current codec has no per-entity-type spawn cap; therefore
Captain contains no Ravager potential, giving Ravagers a hard bound of zero
rather than risking an unbounded repeated selection. Evoker presence supplies
the elite Captain escalation without custom mob logic.

The resources are unconditional data registrations. The Mythas config toggle
does not remove them; later Mansion generation and activation will consult
`mythas.enabled && mythas.mansion_trial_wing.enabled`.

## 2. Vanilla 1.21.10 Vault audit

The current game uses `VaultBlock` and `VaultBlockEntity`, with the following
mapped classes:

- `net.minecraft.world.level.block.VaultBlock`
- `net.minecraft.world.level.block.entity.VaultBlockEntity`
- `net.minecraft.world.level.block.entity.vault.VaultConfig`
- `net.minecraft.world.level.block.entity.vault.VaultServerData`
- `net.minecraft.world.level.block.entity.vault.VaultSharedData`
- `net.minecraft.world.level.block.entity.vault.VaultState`

`VaultConfig` is codec-backed and contains a loot table, activation and
deactivation ranges, a key item, an optional display-loot override, a player
detector, and its entity selector. `VaultBlockEntity` separates server,
shared, client, and config state. The server data persists `rewardedPlayers`
as UUIDs, ejection state, queued items, and timing fields. The shared data
persists the display item and connected players.

The server path validates the inserted key against the configured key item,
rejects invalid insertion, resolves loot, ejects the result, and records the
player UUID. The native rewarded-player history therefore supports one
physical Vault rewarding multiple distinct players while preventing a repeat
reward to the same player. It survives block-entity serialization and chunk
unload/reload. The implementation must still respect vanilla block-breaking,
creative, piston, hopper, and explosion behavior unless a later exploit audit
proves a Mythas-specific restriction necessary.

### Recommendation

Reuse native Vaults directly for all four caches. Give each trial cache its
own configured key item and loot table; give the final Manor Vault its own
key item and loot table. Native UUID reward history is the preferred once-per-
player mechanism. A custom Vault block entity is not justified by the locked
design.

Vanilla history alone cannot establish Adjudicator participation. A small,
Mansion-local participant ledger is still required to decide who receives a
Manor Vault Key. The Vault remains responsible for final per-player repeat
prevention.

## 2.1 Implemented Manor Cache contracts

Unlike Trial Spawner configurations, current 1.21.10 `VaultConfig` is not a
datapack registry. It is embedded in each `VaultBlockEntity` under the
`config` field and decoded with the native `VaultConfig.CODEC`. The three
versioned JSON files under `data/biomemakeover/vault/mythas/mansion/cache/`
are canonical, packaged config payloads for future Mansion template/overlay
placement and admin testing; they are not a second custom Vault
implementation or an automatically discovered registry. Each uses the
current fields `loot_table`, `activation_range`, `deactivation_range`, and
`key_item`.

| Cache | Canonical ID | Key | Loot table | Activation / deactivation |
| --- | --- | --- | --- | --- |
| Patrol Manor Cache | `biomemakeover:mythas/mansion/cache/patrol` | `biomemakeover:patrol_trial_key` | `biomemakeover:mansion/cache/patrol` | 4.0 / 4.5 |
| Enforcer Manor Cache | `biomemakeover:mythas/mansion/cache/enforcer` | `biomemakeover:enforcer_trial_key` | `biomemakeover:mansion/cache/enforcer` | 4.0 / 4.5 |
| Captain Manor Cache | `biomemakeover:mythas/mansion/cache/captain` | `biomemakeover:captain_trial_key` | `biomemakeover:mansion/cache/captain` | 4.0 / 4.5 |

Each loot table has one roll and one matching Fragment entry, so a successful
native Vault claim guarantees exactly one corresponding Fragment. The
canonical cache contracts contain no ominous variant and no secondary loot.

Vanilla `VaultServerData` stores rewarded player UUIDs in an ordered set and
persists it through the block entity codec. The set has a hard maximum of 128
players; when a 129th player is added, the oldest entry is evicted. Therefore
the native guarantee is once per player while that physical cache retains the
player in its 128-entry history, not permanent unlimited once-per-player
protection. Separate physical Vault blocks have separate block entities and
separate histories, even when their configs are identical. Successful
matching-key insertion consumes one key; rejected or already-rewarded
attempts do not consume a key.

Standalone admin setup uses the actual embedded current-version contract:

```text
/setblock ~ ~ ~ minecraft:vault
/data merge block ~ ~ ~ {config:{loot_table:"biomemakeover:mansion/cache/patrol",activation_range:4.0d,deactivation_range:4.5d,key_item:{id:"biomemakeover:patrol_trial_key",count:1}}}
```

Replace `patrol` in the loot table and key item with `enforcer` or `captain`
for the other caches. The `/data merge` command is required because current
vanilla does not provide a datapack registry selector for VaultConfig.

## 3. Locked progression graph

```text
Patrol Trial   -> Patrol Trial Key   -> Patrol Manor Cache   -> Patrol Fragment
Enforcer Trial -> Enforcer Trial Key -> Enforcer Manor Cache -> Enforcer Fragment
Captain Trial  -> Captain Trial Key  -> Captain Manor Cache  -> Captain Fragment

Patrol Fragment + Enforcer Fragment + Captain Fragment
    -> Mansion Emerald Key
    -> Mythas-only Adjudicator gate/activation
    -> unchanged released Adjudicator encounter
    -> Manor Vault Key for eligible participants
    -> Manor Vault (native per-player Vault history)
```

The three trials are independent and may be completed in any order. Fragments
are physical, tradeable items. The proposed initial item IDs, all in the
existing `biomemakeover` namespace, are:

| Count | Proposed ID | Role |
| ---: | --- | --- |
| 1 | `patrol_trial_key` | opens Patrol Manor Cache |
| 1 | `enforcer_trial_key` | opens Enforcer Manor Cache |
| 1 | `captain_trial_key` | opens Captain Manor Cache |
| 1 | `patrol_fragment` | first combination ingredient |
| 1 | `enforcer_fragment` | second combination ingredient |
| 1 | `captain_fragment` | third combination ingredient |
| 1 | `mansion_emerald_key` | Mythas-only Adjudicator gate |
| 1 | `manor_vault_key` | final Manor Vault key |

This is eight restrained item IDs, not eight entity types. Keys and fragments
should stack only if the implementation can preserve identity and eligibility
safely; the default recommendation is stackable ordinary trial keys/fragments
and a Mansion-local participant/eligibility check where identity matters.
If a key must be tied to one Mansion, use a documented current item component
or a server-side issuance record rather than silently combining keys from
different Mansions.

Stage 14B.1 permanently establishes these eight IDs unconditionally:
`biomemakeover:patrol_trial_key`, `biomemakeover:enforcer_trial_key`,
`biomemakeover:captain_trial_key`, `biomemakeover:patrol_fragment`,
`biomemakeover:enforcer_fragment`, `biomemakeover:captain_fragment`,
`biomemakeover:mansion_emerald_key`, and `biomemakeover:manor_vault_key`.
They intentionally remain outside the released creative tab and have no
survival acquisition, recipes, loot, or interactions until later stages.

The three fragments may be crafted in a shapeless recipe into one Mansion
Emerald Key only when the implementation can prove Mansion identity is not
lost. If fragments remain ordinary transferable items, the key is a physical
group gate and can be used by one player to activate the local encounter.
The Emerald Key is consumed on successful activation; failed or wrong-gate
uses do not consume it.

Stage 14B.3 implements the exact shapeless recipe
`patrol_fragment + enforcer_fragment + captain_fragment -> mansion_emerald_key`.
The recipe is data-present while Mythas is disabled, but it introduces no
natural Fragment source and the resulting Emerald Key is inert until the
later Stage 14B.5 gate implementation.

## 4. Locked multiplayer and item contract

The following decisions supersede earlier deferred wording and are
authoritative for implementation:

- Patrol, Enforcer, and Captain are shared encounters and may be completed in
  any order.
- A successful trial targets one corresponding physical Trial Key per
  participating player. Native Trial Spawner reward-ejection mechanics are
  the first implementation choice. No custom progression database may be
  introduced merely to distribute these keys.
- Trial Keys and Fragments are physical, tradeable, droppable, storable, and
  neither player-bound nor Mansion-instance-bound. They must not carry hidden
  Mansion UUID components.
- Each trial has one physical native Vault cache with personal vanilla
  rewarded-player UUID history. Each player may claim its reward once; other
  players may claim their own reward from the same cache.
- Each cache guarantees its matching Fragment; the progression fragment is
  not an RNG outcome.
- The three exact Fragment types craft one generic physical Mansion Emerald
  Key. It is consumed only after successful activation of one Mansion gate.
- Each actual qualifying Adjudicator participant receives one physical Manor
  Vault Key. The final native Vault's UUID history prevents repeat rewards.
  This reward is additional to released Adjudicator loot.

The implementation must stop for review if native ejection cannot meet the
per-participant Trial Key target without prohibited bookkeeping or an
unapproved fourth template. It must not silently reduce the target to one
shared key.

The recommended semantics are:

1. A completed physical Trial Spawner run produces one trial-key reward per
   qualifying participant, delivered once by the Mansion-local completion
   controller. This makes four participants yield four keys and matches the
   per-player Vault reward model. If a later data-only design proves that the
   selected vanilla ejection table can express this exactly, use it; otherwise
   keep the completion hook thin and server-authoritative.
2. Each Manor Cache is a native Vault, so each unique player may redeem its
   configured key once. The key is consumed per successful unlock.
3. Trial keys are physical and tradeable. A player who missed a trial may use
   a legitimately obtained key. No invisible global account gate or
   Mansion-instance binding is required for trial caches.
4. Any one player may gather all three fragments and craft the Mansion Emerald
   Key for the group. The physical key is consumed to activate that Mansion's
   gate.
5. Only one Adjudicator run may be active for a Mansion-local gate/controller.
   The existing Adjudicator AI, phases, alliance, and released loot are
   untouched.
6. A small Mansion-local participant ledger records players who materially
   participated in the activated run, using a bounded proximity/engagement
   window defined by the later implementation. This ledger exists only for
   boss-participant eligibility, not trial-key distribution. A late player
   who did not participate does not receive a Manor Vault Key merely because
   they can reach the room.
7. Each eligible participant receives one Manor Vault Key for that Mansion
   and boss run. The final Vault's native UUID history prevents repeat reward.
8. A player who joins after the fight and was not recorded as a participant
   cannot claim the final reward. A participant may claim it after restart,
   provided the Mansion-local ledger and key issuance persisted.
9. Restart, unload, or a temporary disconnect preserves the spawner/Vault
   state and the Mansion-local participant/fragment state. An active encounter
   resumes through native spawner persistence; if a later implementation must
   abort an unrecoverable run, it must define and test that transition rather
   than silently deleting progress.

## 5. Recommended trial rosters

No new mob entity types are needed. The recommended first roster is:

| Trial | Existing roster | Purpose |
| --- | --- | --- |
| Patrol | Pillagers, with occasional Vindicators | entry raider patrol |
| Enforcer | Vindicators and Evokers; Evoker Vexes remain vanilla support behavior | Mansion escalation |
| Captain | Evokers, Vindicators, and limited Ravager potential | highest existing raider threat |

These are recommendations, not final balance values. The final configs must
preserve vanilla Trial Spawner scaling and avoid modifying global entity AI,
spawn rules, or the frozen Adjudicator encounter. Trial entities should be
confined to the encounter's native lifecycle and must not be permanently
registered as Mansion population after completion.

## 6. Mansion integration without released-template changes

The current port registers `biomemakeover:mansion` as a custom `StructureType`
and assembles rooms/pieces algorithmically in `MansionFeature`. Dungeon and
boss rooms are represented by `DungeonRoom` and `BossRoom`; marker handling
and late reconciliation are already tied to a Mansion origin/layout
signature. Existing code records per-instance origin, signature, piece
ordinals, placements, bounding boxes, and boss-room metadata.

The clean insertion point is a Mythas-only post-assembly hook after the
released Mansion layout is complete and its bounding/marker metadata is known.
The hook should be guarded by `mythas.mansion_trial_wing.enabled` and should
derive a stable Mansion identity from dimension plus structure origin and
layout signature. It must execute once per generated instance, persist an
installed/disabled state, and never run when the toggle is false.

Initial structure budget: three new Mythas templates, subject to a later
template audit:

- `trial/patrol`
- `trial/enforcer`
- `trial/captain`

Each template should contain its own entry/corridor connection and sealed
chamber boundary, allowing deterministic placement beneath the dungeon while
avoiding a fourth root template. The hook should choose one orientation from
the Mansion's established orientation, place the three chambers at a fixed
subterranean Y offset relative to the dungeon/boss metadata, and validate
collisions against terrain before committing the wing. The exact Y offset,
dimensions, connector geometry, and collision policy require a later
template/terrain prototype; they are deliberately not invented here.

The generator must guarantee at most one wing per generated Mansion. It must
not retro-generate into an already-generated Mansion. Enabling the feature
affects newly generated Mansions only. If disabled after an existing wing is
present, no destructive block removal should occur; the already-generated
wing becomes inert/non-accessible according to the later gate policy. This is
the only safe interpretation for persistent worlds without retroactive world
editing.

The released 168 templates and all three released orphan templates remain
unchanged. If exactly three Mythas templates are eventually added, the
inventory expectation becomes 171 total / 168 active / 3 released orphans,
but that count is a future implementation invariant, not a change made here.

## 7. Locked Adjudicator gate boundary

Gate encounter activation rather than merely placing an ordinary breakable
locked door. A door-only solution is rejected because it can be bypassed by
mining through Mansion walls. Use the least invasive Mythas-only external
hook at the existing Adjudicator activation point: a physical key-compatible
gate/controller consumes `mansion_emerald_key`, stores its open/activated
state per Mansion, and controls whether the unchanged encounter is
instantiated. Do not modify `AdjudicatorEntity` combat, phases, loot, or
alliance code.

The released boss room and its marker results remain intact. When Mythas is
disabled before generation, no gate is installed and the frozen released
behavior remains available. If a wing/gate already exists and the toggle is
later disabled, the gate must become non-blocking/inactive without deleting
world blocks; this preserves world safety at the cost of treating that
already-generated enhancement as an inert historical overlay.

Only one activation may be in flight for a Mansion identity. The gate must
reject the wrong key, reject a second activation while active, and consume a
valid key only after successful server-side validation.

## 8. Manor Vault architecture

After the unchanged Adjudicator dies, the Mansion-local participant ledger
issues one `manor_vault_key` to each eligible participant. The final Manor
Vault is a native Vault configured with that key and a later Mythas reward
table. Its `rewardedPlayers` UUID set is the repeat-prevention authority.

Eligibility must be recorded at the boss encounter, not inferred later from
distance or possession of a key. The ledger and issued-key state must persist
by Mansion identity; it must be independent for two Mansions in the same
dimension. A traded key must not create eligibility for a nonparticipant.
The final reward is additional to, and never replaces, released Adjudicator
loot.

Reward contents, probabilities, and cosmetic/utility balance are deferred to
a later design stage.

## 9. Failure and exploit analysis

| Case | Design treatment |
| --- | --- |
| Trial spawner broken | use native block behavior unless the future structure policy requires protection; do not silently duplicate rewards |
| Cache/Vault broken | audit native Vault drop/BE behavior before choosing protection; preserve vanilla semantics where safe |
| Silk Touch/explosions/pistons | test native behavior; add only a narrowly justified Mythas protection rule |
| Hopper extraction | keys are physical; ensure successful unlock is server-side and not triggered by inventory automation |
| Key duplication | no custom client authority; consume only after validated use; audit component/container paths |
| Shared storage/trading | allowed for trial keys/fragments; final eligibility remains Mansion-local |
| Players enter/leave | use native detection for spawner; snapshot/record participant eligibility at the defined boss window |
| Death | physical keys/fragments drop under ordinary item rules; encounter state persists unless vanilla spawner completes/aborts |
| Restart/chunk unload | rely on native spawner/Vault serialization plus persistent Mansion-local state; test mid-encounter and mid-ejection |
| Enhancement disabled later | do not delete generated blocks; disable activation/access behavior safely |
| Two Mansions | identity includes dimension and origin/signature; no global progression flag |
| Creative/admin | retain vanilla administrative behavior; do not make creative a hidden reward path |

Native Trial Spawner ejection remains the first proof target for the locked
per-participant Trial Key result. It must not be replaced with hidden player
progression bookkeeping merely because the native path is inconvenient.

## 10. Stage 14A configuration dependency

Stage 14B requires the Stage 14A enhancement framework to expose:

`mythas.mansion_trial_wing.enabled`, default `false`.

The option controls generation and activation/accessibility, never registry
existence. All future item/block/configured-feature/template IDs must remain
registered regardless of the toggle so save data and network registries stay
stable. The option is read when a new Mansion is generated and by the gate's
activation/access checks. It must not retroactively rewrite existing worlds.

## 11. Locked stage order and implementation sequence

Stage 14A must precede 14B.1. Do not begin 14B.1 until the Stage 14A
framework exposes the toggle and registration-safety contract.

1. **14B.1 — Registry/data foundation:** register the eight restrained items,
   define translations/models/tags only as source/design requires, and add
   Mansion-local persistent identity/state substrate. No wing generation yet.
2. **14B.2 — Trial configs:** add three data-driven Trial Spawner configs,
   exact rosters/settings, completion loot, and a focused test harness. Prove
   scaling, cooldown, save/reload, and no duplicate completion.
3. **14B.3 — Caches/fragments:** place/configure three native Vault caches,
   implement key/fragment delivery, and prove native rewarded-player history
   plus multiplayer semantics.
4. **14B.4 — Wing integration:** add only the three audited Mythas templates,
   deterministic orientation/placement/connectors, collision checks, and the
   one-wing-per-Mansion installation record. Fresh-world testing required.
5. **14B.5 — Emerald gate:** add the physical Mansion-local key gate without
   changing Adjudicator code or released boss loot. Test wrong key, consumption,
   restart, and one-active-run behavior.
6. **14B.6 — Manor Vault:** record boss participants, issue final keys, and
   configure the native per-player Vault. Test late players, restart, and
   repeat prevention.
7. **14B.7 — Hardening:** test unload/reload, death, chunk boundaries,
   multiplayer concurrency, creative/admin intervention, toggle transitions,
   two Mansions, and exploit cases.
8. **14B.8 — Polish/freeze:** finalize visual assets, localized names,
   reward balance, focused validators, documentation, and Prism acceptance.

No step above is implemented by Stage 14B.0. A later stage may split the
foundation if the Stage 14A framework dictates a different registration
boundary.

## 12. Design-lock decisions and implementation gates

Locked now:

- enhancement is disabled by default and separate from released parity;
- three independent trials, any order;
- trials are shared multiplayer encounters;
- physical keys/caches/fragments;
- trial keys and fragments are generic, tradeable, and not Mansion-bound;
- each cache guarantees its matching Fragment;
- each trial targets one Trial Key per participant without custom bookkeeping
  solely for key distribution;
- the Emerald Key is generic and consumed on successful gate activation;
- the Adjudicator is gated at encounter activation, not by an ordinary door;
- native Trial Spawner and Vault reuse is preferred;
- released Adjudicator behavior and loot remain unchanged;
- progression is Mansion-local and restart-safe;
- no new mob entity types are currently required;
- exactly three Mythas templates is the initial budget, pending geometry
  feasibility;
- no retro-generation into already-generated Mansions.

Must be proven before implementation lock:

- proof that native Trial Spawner ejection meets the per-participant key
  target without prohibited bookkeeping;
- final trial counts, cooldowns, and reward tables;
- stable Mansion identity persistence format;
- exact underground anchor/Y/connector geometry and collision policy;
- gate block versus Vault-like implementation;
- participant radius/window and disconnect/death rules;
- native break/explosion/piston/hopper behavior for the chosen blocks;
- final Manor reward contents and balance.

The exact native ejection implementation, final trial settings, participant
window, and gate block/controller are implementation gates. They may refine
the locked contract but may not weaken its physical, generic, shared, or
server-authoritative semantics without a new design review.
