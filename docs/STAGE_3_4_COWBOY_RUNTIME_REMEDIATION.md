# Stage 3/4 Cowboy runtime-parity remediation

Historical authority: `Lemonszz/Biome-Makeover` 1.20 branch at
`2f314c0596af095a4890995a465f308f69476b4a` (released 1.20.1-1.11.4).

## Released lifecycle

- `PatrolSpawnerMixin_Cowboy` intercepted each patrol member whose position was in `#minecraft:is_badlands`.
- It validated the BM Cowboy space but deliberately used the vanilla Pillager patrol spawn predicate.
- Each successful member created and finalized one Horse and one Cowboy, mounted the Cowboy, marked the horse
  `CowboySpawned`, and added the horse with passengers. No standalone natural Cowboy spawn entry existed.
- Leaders were marked before Cowboy finalization, received `findPatrolTarget`, and marked their horse `Hat=true`.
- `CowboyEntity` inherited Pillager crossbow, patrol, targeting, raid, sound, and interaction behavior. While riding,
  it copied its yaw to the vehicle.
- The Cowboy renderer always added the custom Cowboy Hat model. Thus direct `/summon` visibly had a hat even though
  command creation did not run patrol finalization or create a horse.
- Finalized ordinary Cowboys equipped `cowboy_hat`; the constructor assigned head drop chance 0.25.
- Leader finalization replaced head equipment with BM's seven-layer custom ominous banner and set head drop chance
  2.0 (guaranteed equipment drop semantics). The visual Cowboy Hat render layer remained unconditional.
- Every patrol horse serialized `CowboySpawned`; leader horses additionally synchronized and serialized `Hat`.
  Cowboy-spawned horses followed their controlling patrol member's far-away removal. An unmounted, unmodified patrol
  horse despawned; saddling, armor, or a leash cleared the patrol marker and preserved it.
- Killing an out-of-raid leader carrying BM's exact custom banner credited a direct player or a tame wolf's player
  owner. It applied/stacked Bad Omen for 120000 ticks up to amplifier 4 unless raids were disabled and granted the
  Voluntary Exile criterion. Cowboys had an otherwise empty entity loot table; hat/banner drops came from equipment.
- No Cowboy-specific sounds, foods, breeding, interactions, attributes, spawn egg mechanics, or advancement resources
  beyond inherited Pillager behavior were implemented.

## Pre-remediation gaps and root causes

The modern patrol interceptor already created mounted finalized Cowboys and used the corrected Pillager predicate,
but it did not mark horses, synchronize/render leader horse hats, serialize/despawn patrol horses, replace leader
equipment with BM's banner, set exact head drop chances, render the unconditional Cowboy hat, or translate the old
Bad Omen/advancement result. Direct summon therefore looked like an ordinary Pillager despite using the Cowboy texture.

These gaps came from omitted historical mixin/render-layer infrastructure and the 1.20-to-1.21 transitions from NBT
item tags to data components, CompoundTag entity IO to ValueInput/ValueOutput, immediate entity rendering to extracted
render states, and the Bad Omen/Raid Omen progression split.

## 1.21.10 translation

- Restored ordinary 0.25 and leader 2.0 head equipment drop chances.
- Restored ordinary hat equipment and leader finalization with the exact seven historical pattern/color layers using
  `BANNER_PATTERNS`, plus the historical gold ominous-banner name.
- Restored the leader's historical Bad Omen duration/stacking. Modern vanilla converts that effect to Raid Omen on
  village entry; the historical direct Voluntary Exile award remains necessary because BM's banner intentionally does
  not match vanilla's current banner predicate. No vanilla Pillager or raid method is mixed into globally.
- Restored `Hat` as synchronized horse data and both `Hat`/`CowboySpawned` under their historical serialized names.
- Restored patrol-horse removal rules using current SADDLE/BODY equipment and leash state.
- Restored patrol marking for every horse and leader-hat marking only for leaders.
- Restored the exact 64x64 historical hat geometry/texture. Cowboy rendering is unconditional as released. Horse
  rendering reads synchronized hat state through a client-only Horse render-state mixin, then uses the historical
  head transform, translations, -25-degree pitch, and scales through Fabric's feature-layer registration callback.
- Direct summon gets the released visual hat and inherited Pillager AI, but intentionally gets no horse, patrol target,
  leader flag, banner, or finalized ordinary equipment merely from command construction.

## Compatibility and runtime status

No IDs, loot tables, spawn weights, biome tags, or global Pillager/Raid behavior changed. Existing ordinary horses
load absent fields as false. Existing Cowboys remain loadable; finalized future patrol Cowboys gain the restored
equipment and state. Static/package validation cannot prove mixin application, visuals, natural patrol scheduling,
despawn timing, drops, save/reload, or Raid Omen behavior; all remain targeted Prism tests.

Stage 3/4 is not declared closed until this Cowboy system passes runtime verification. Stage 5 was not started.
