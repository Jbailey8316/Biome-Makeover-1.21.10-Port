# Stage 7 — Original Owl Parity Restoration

## Authority and scope

This checkpoint restores the Owl from final Biome Makeover 1.20.1 (`1.20`
source commit `2f314c0596af095a4890995a465f308f69476b4a`). It does not restore
Rootlings, Moths, Altar/curses, Mansion content, or Mythas enhancements.

## Final released contract

- Entity: creature, standing dimensions `0.7 × 0.8`, tracking range 12. The
  flying pose uses historical `0.7 × 1.4` dimensions.
- Spawn: Dark Forest, weight 20, group 1–4, `ON_GROUND` with the
  motion-blocking heightmap. Support is Grass Block or Leaves and raw
  brightness is greater than 2. There is no time gate or daytime despawn rule.
- Attributes: flying speed 0.8, health 6, movement speed 0.4, attack damage 2.
  Taming changes health/damage to 20/4 and fills health.
- Navigation: flying controls, no floating or door opening, fire path malus -1,
  slow fall, and no fall damage.
- Goals: Float (0); sit (2); melee (3); owner follow 1.2/10/2 (4); meat
  temptation 1.2 (5); breed 1.0 (6); panic 1.25 (7); look at Player (8);
  fly onto tree 1.0/0.5 (9); random stroll (10); random look (11). Targets are
  owner defense (1/2), then untamed `#biomemakeover:owl_targets` prey (3).
- Prey: Rabbit, Chicken, Silverfish, Endermite, Bat, Toad, Blightbat,
  Dragonfly and both Lightning Bug types. Toad/Blightbat are optional tag
  entries because those disabled entities are not activated by this stage.
- Food: every edible meat drives temptation, taming, healing and breeding.
  Taming is 1-in-3 when the wild Owl has no attack target. Healing equals the
  food's nutrition. Since 1.21.10 removed the old meat boolean, the maintained
  vanilla `minecraft:wolf_food` tag preserves this semantic family.
- Babies use inherited age state. Offspring inherit owner and tame state.
- Tree landing is opportunistic tree-targeting random stroll. The released
  class inherits shoulder serialization/storage from `ShoulderRidingEntity`,
  but it does not register `LandOnOwnersShoulderGoal`; normal AI therefore has
  no path that transfers an Owl to a player shoulder. There is no custom nest
  lifecycle.
- Rendering uses the released 64×64 model, 0.75 scale, historical flight lean
  and movement poses, and unconditional full-bright `owl_eyes.png`. A
  case-insensitive name `Hedwig` selects `owl_2.png`. There is no blink.
- Sounds are idle, hurt and death only, with released 4/3/2 variants.
- Loot is 1–2 Feathers plus the released Looting increase. No Owl head exists.
- Spawn egg remains reachable. No final Owl-owned advancement was found.
- Owl-owned persisted fields are `OwlState` and `StandingState`; tame, owner,
  age and sit state remain superclass data.

## Experimental behavior removed from the Owl

The old port used night-only canopy spawning, daytime wild culling, a tall
standing hitbox, rabbit-only food, special Chicken healing, night Chicken-only
hunting, player fleeing, remembered nests/day sleeping, extra vocal events,
custom lift/hover movement, blinking and custom sleeping/resting poses. None is
final-release behavior and none now participates in Owl gameplay or rendering.

The already-registered `owl_nest`, `owl_egg`, and extra sound IDs remain inert
for existing-world registry safety. Nonreleased Owl nest worldgen remains
excluded from the jar. No registry ID was removed, renamed or repurposed.

## Modern translations and runtime risks

- `TEMPT_RANGE=10` is explicit because modern temptation goals require the
  attribute; it preserves the old fixed 10-block targeting range.
- `minecraft:wolf_food` replaces the removed food `isMeat` property.
- Render-state extraction transfers flight, sitting, lean and Hedwig state;
  the emissive eye layer is unconditional.
- Dynamic living dimensions moved to `getDefaultDimensions` in 1.21.10.
- Modern `FollowOwnerGoal` replaces BM's old compatibility wrapper. The old
  `leavesAllowed=true` argument maps to `canFlyToOwner()` in 1.21.10. The
  released first-match tree scan and 15-by-7 water escape search are retained.

## Prism acceptance checklist

1. In fresh Dark Forest chunks, observe natural Owls by day and night. Check
   grass/leaves support, brightness >2 and groups up to four; verify no day
   despawn.
2. Observe wild movement, flight, landing, tree use, player response, released
   sounds, hunting and persistence.
3. Test several raw and cooked `minecraft:wolf_food` meats plus invalid foods.
   Verify temptation, 1-in-3 taming, nutrition healing, sit/follow and defense.
4. Breed two tame Owls; inspect baby size/model/movement, growth, ownership and
   save/reload.
5. Test Rabbit, Chicken, Silverfish, Endermite, Bat, Dragonfly and both bug
   types as prey; tame Owls must not use the wild prey target goal.
6. Test opportunistic tree landing. Do not expect shoulder mounting: final
   1.20.1 supplied shoulder storage but omitted the goal that triggers it.
7. Inspect standing, walking, flying, landing, sitting and baby poses from all
   sides. Eyes must glow by day and night with no blinking. Test `Hedwig`.
8. Test ordinary and Looting kills: 1–2 Feathers plus Looting, no Owl head.
9. Save/reload and restart with wild, tame and baby Owls.
10. Dedicated-server boot/join, natural spawn, tame/breed/save/rejoin.

Stage 7 remains runtime-open until this checklist passes.
