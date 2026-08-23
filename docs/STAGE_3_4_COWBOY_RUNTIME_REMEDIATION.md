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
- The first compatibility pass restored historical direct Bad Omen duration/stacking. The later 1.21.10 captain
  checkpoint below supersedes that interim translation with the current vanilla Ominous Bottle mechanism.
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

## Player-hat and deterministic patrol-test checkpoint

Historical Fabric registered `HatArmorRenderer` for `cowboy_hat`. That renderer selected the same dedicated 64x64
Cowboy Hat geometry and `textures/misc/cowboy_hat.png`, copied the humanoid model's head pose into it, and rendered it
instead of vanilla helmet geometry. The item remained a head-slot wearable with 500 durability, two armor points,
zero enchantability, leather repair, and the leather equip sound. Cowboy and leader-horse layers deliberately used
the same geometry through their own transforms.

The 1.21.10 item had been created with `humanoidArmor(LEATHER, HELMET)`. Besides equipment behavior, that method
assigns the leather equipment asset, so Minecraft correctly—but historically incorrectly—rendered leather helmet
geometry. The first translation supplied the historical durability, armor, repair and equip contracts plus a
head-slot `Equippable` without a vanilla render asset, but its generic player feature layer failed visual runtime
acceptance. The dedicated armor-renderer correction is recorded below. Inventory and dropped rendering remain the
existing item-model path; Cowboy and horse layers are unchanged.

The released 1.20.1 source itself exposed a command that invoked private `PatrolSpawner.spawnPatrolMember` through a
mixin invoker. The temporary 1.21.10 command `/bmtest cowboy_patrol` retains that evidence-backed design: while an
operator stands in Badlands, it asks the real spawner method for one leader and three ordinary nearby members. The
existing production `PatrolSpawnerMixin` consequently performs all Cowboy replacement, finalization, horse creation,
mounting, banner, leader target and persistence-marker work. The command contains none of that logic.

The hook is permission-level 2, never runs automatically, adds no registry entry, and rejects non-Badlands context.
It is packaged only so Prism can exercise the production path and should be removed after Cowboy patrol acceptance.
Flat, open Badlands terrain gives the spawn predicate the best chance of creating all four members; the command reports
the actual successful count rather than fabricating failures.

## Player render-path correction and modern captain semantics

Prism showed that registering a player layer and removing the leather asset was necessary but insufficient. The first
layer reused `CowboyHatLayer`, including the Cowboy entity's historical `-0.2` translation, and operated as an extra
living-entity feature instead of replacing the equipment render submission. Released Fabric player rendering used
`HatArmorRenderer`: it copied the animated humanoid head pose into the hat model and replaced the default armor render.
Fabric 1.21.10 retains that extension point. `CowboyHatArmorRenderer` is now registered for the item, follows the
provided animated armor head, cancels exactly the model's baked two-pixel pivot (`-0.125` block units), and causes
`HumanoidArmorLayer` to cancel its default submission. This applies to wide and slim players without relying on avatar
renderer registration. The item also remains asset-free, so no vanilla leather geometry exists underneath it.

Vanilla 1.21.10 does not apply Bad Omen when a patrol captain dies. Its Pillager entity loot table has a captain-only
pool using the raider `is_captain` type-specific predicate. The pool drops exactly one Ominous Bottle and assigns its
amplifier uniformly from 0 through 4. It contains no killed-by-player condition; ordinary entity-loot/game-rule rules
govern whether loot is produced. Drinking the bottle is the later action that grants Bad Omen, which vanilla converts
to Raid Omen at the appropriate current gameplay boundary.

Inherited `Raider.isCaptain()` compares the head stack to vanilla's exact ominous banner, so BM's intentionally custom
banner made its leader fail the vanilla loot predicate. Cowboy now overrides that identity narrowly: it is a captain
only when patrol-leader state and BM's exact custom banner both match. Its loot table mirrors vanilla Pillager's exact
captain pool, including `minecraft:entities/pillager` random-sequence behavior, under BM's existing loot-table ID. The interim direct Bad Omen application
was removed, so the two progression paths cannot fire together.

The 1.21.10 Voluntary Exile advancement independently requires vanilla's exact banner component list. BM's custom
seven-pattern banner cannot satisfy it, so focused manual awarding remains for a responsible player or tame-wolf owner
who kills a matching BM captain. That award does not create Bad Omen or a bottle. Mounted patrol behavior, the custom
leader banner, and the deliberately hatted leader horse are unchanged.

## Shared rear item-artifact correction

The pinned equipped texture is 64x64 and its packaged SHA-256 is identical to the released
`textures/misc/cowboy_hat.png`; the separate inventory texture is 16x16 and remains bound only by the generated item
model. All five current hat cuboids reproduce the released UV origins (`32,32`, `32,46`, `32,0`, `0,46`, `0,12`),
mirror flags and 64x64 layer dimensions. Player, Cowboy and horse 3D paths all instantiate the same baked
`CowboyHatModel` and bind the same equipped texture. The artifact therefore was not a changed texture or UV conversion.

The modern hat is a component-based plain `Item`, while released `HatItem` extended `ArmorItem`. After Fabric's custom
armor renderer submitted the correct 3D model, Minecraft's default head-item path also treated the modern stack as a
head item and submitted its normal flat 16x16 inventory model. This duplicate was most obvious behind the wide brim
and also affected equipped Cowboys. `ArmorRenderer.shouldRenderDefaultHeadItem` is the current Fabric contract for
this exact collision; the Cowboy renderer now returns false there. Inventory and dropped-item rendering are unchanged,
and player/Cowboy/horse continue to share the historical 3D model and equipped texture.

## Mounted rider attachment correction

Released BM registered Cowboy at exactly `0.6 x 1.95`, matching the contemporary Pillager dimensions, and added no
Cowboy-specific riding-position override or horse-position mixin. Its patrol path simply called `startRiding(horse)`,
so the released observable contract was the inherited vanilla illager-on-horse position.

Minecraft 1.21.10 expresses attachment geometry on `EntityType.Builder`. Vanilla Pillager now declares
`passengerAttachments(2.0F)` and `ridingOffset(-0.6F)` in addition to the same dimensions. Cowboy's ported registration
had retained only the dimensions; consequently `Entity.positionRider` subtracted Cowboy's default vehicle attachment
instead of Pillager's `-0.6` riding attachment, leaving the Cowboy visibly too high. The Cowboy registration now copies
those two modern Pillager attachment values. No runtime offset override was introduced, and patrol creation, mounting,
horse state, persistence, hats, banner and captain loot paths are unchanged.

## Leader-horse hat seating correction

The released horse layer attached to the first `HorseModel.headParts()` entry, then used a `-0.4` lift and `-25` degree
X leveling rotation. Both released 1.20.1 and current 1.21.10 horse meshes identify that same part as `head_parts`,
with the same neutral `+30` degree X pitch and `(0, 4, -12)` pivot; selecting an inner muzzle/head cuboid would lose
the complete animated neck/head transform and is not equivalent.

The initial 1.21.10 translation mechanically retained the immediate-render call order. In the render-state layer, that
put the `-0.4` seating lift into the still-pitched head-local axes, adding a visible forward component before the hat
was leveled. The layer now keeps the exact animated `head_parts` attachment and historical scale, lift magnitude and
leveling angle, but applies the leveling rotation before the lift so the lift is expressed in the seated hat frame.
This is a horse-only transform-order adaptation: the shared model, texture, UVs, Cowboy/player paths, synchronized hat
state and all patrol/captain behavior are unchanged.
