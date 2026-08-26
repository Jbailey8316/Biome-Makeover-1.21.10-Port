# Stage 7 Runtime Remediation 2 — Owl Baby Rendering

## Runtime evidence

After Remediation 1, two tame adult Owls entered love mode, completed breeding,
awarded `The Parrots and the Bats`, and caused no exception. The offspring
looked adult-sized rather than like the released baby Owl.

## Server breeding and age audit

Final BM 1.20.1 returns a newly constructed Owl from `getBreedOffspring` and
copies owner/tame state. Vanilla's breeding method assigns the newborn baby
age. Minecraft 1.21.10 retains that semantic contract:

1. `Animal.spawnChildFromBreeding` calls `getBreedOffspring`.
2. It calls `offspring.setBaby(true)` before adding the entity.
3. `AgeableMob.setBaby(true)` calls `setAge(-24000)`.
4. `AgeableMob` saves `Age` and `ForcedAge` and restores them through
   `setAge`, which also synchronizes baby state and refreshes dimensions.

The current Owl offspring method does not reset age. Tame/owner inheritance
does not change age. The newborn therefore had the genuine vanilla negative
age and baby state; this was a client presentation defect, not adult creation.

## Root cause and 1.21.10 translation

The released renderer used `AgeableListModel(true, 14, 0, 2, 2, 24)`, which
automatically transformed head and body parts for babies. That ageable model
class no longer owns rendering in Minecraft 1.21.10. Modern renderers bake
separate adult and baby layers and select one from render state.

Stage 7 had only one adult Owl layer/model. Although
`LivingEntityRenderer.extractRenderState` transferred `isBaby`, nothing used
that value, so a logically correct baby rendered with the adult model.

The port now:

- explicitly transfers `entity.isBaby()` into Owl render state;
- registers a dedicated `owl#baby` model layer;
- applies `BabyModelTransform(true, 14, 0, 2, 2, 24,
  Set.of("head_connection"))`, the modern equivalent of the released model;
- stores an `AdultAndBabyModelPair<OwlModel>`;
- selects the baby model during renderer submission when `state.isBaby` is
  true.

No arbitrary scale was added. The negative age, logical dimensions, natural
aging and save/load persistence remain vanilla `AgeableMob` behavior. The
Remediation 1 `super.getDefaultDimensions(pose)` recursion fix is unchanged.

## Static coverage and runtime-open checks

The validator now requires the offspring hook without an adult-age reset,
explicit baby-state transfer, a registered baby layer, the historical baby
transform, and render-time adult/baby model selection. It continues checking
the dimension recursion fix.

Prism must still confirm visible baby geometry, baby hitbox, owner inheritance,
save/reload age persistence and the eventual transition to the adult model and
dimensions.
