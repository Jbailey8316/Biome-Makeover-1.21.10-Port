# Stage 7 Runtime Remediation 3 — Owl Shoulder and Tree Behavior

## Runtime evidence and authority

The Prism checkpoint after `4945f36f5ba9ef84a5e13aa3fa3cc570a852ed2d`
confirmed breeding, baby rendering and aging, flight, general AI, hunting,
emissive eyes, the `Hedwig` texture, and Black Thistle Weakness. No Owl
exception was reported. This remediation compares the remaining shoulder and
tree questions against final Biome Makeover 1.20.1 commit
`2f314c0596af095a4890995a465f308f69476b4a`.

## Shoulder-riding disposition: source-confirmed unreachable

The final Owl extends `ShoulderRidingEntity`, but its complete goal list does
not contain `LandOnOwnersShoulderGoal`. Vanilla Parrot registers that goal
explicitly; `ShoulderRidingEntity` itself only supplies the cooldown,
serialization into a server player's shoulder slot, and the transfer method.
It does not schedule or initiate the transition.

The 1.21.10 contract has the same split. `LandOnOwnersShoulderGoal` checks for
a tamed owner who is a non-spectating, non-flying, dry server player, requires
the animal not be ordered to sit and its cooldown to exceed 100 ticks, then
calls `setEntityOnShoulder` when bounding boxes intersect. None of those
conditions are evaluated without the goal. The current Owl correctly omits the
goal, so waiting near a tame Owl cannot cause shoulder mounting. No production
shoulder shortcut, teleport, renderer, or goal was added.

This corrects the earlier audit inference that extending the superclass made
the complete Parrot shoulder behavior automatic. Inheritance provides storage
capability only. Absent shoulder riding is not a Stage 7 runtime blocker.

## Tree-flight root cause and remediation

Final source registers `ExtendedFlyOntoTree` at priority 9, speed 1.0 and
probability 0.5. This is not a persistent perch state or timer. When the random
stroll goal obtains a destination, it sometimes scans a box spanning three
blocks horizontally and six vertically. It chooses the **first** candidate in
`BlockPos.betweenClosed` order with two empty blocks above Leaves or a Logs-tag
block. Otherwise it falls back to ordinary water-avoiding random stroll. Its
in-water land search is radius 15, vertical range 7.

The port retained the goal and probability but changed the scan to score every
candidate and choose the nearest, used `blockPosition()` rather than
`getOnPos()`, and widened the water escape search vertically to 15. Those were
port translations rather than released behavior. The remediation restores the
first-match iteration, historical origin, and 15-by-7 fallback without raising
goal frequency or adding attraction, memory, poses, sleeping, or nests.

The sibling audit also found that final `FlyingFollowOwnerGoal` passed
`leavesAllowed=true`. Minecraft 1.21.10 removed that constructor argument and
moved the decision to `TamableAnimal.canFlyToOwner()`. Owl now returns true from
that hook, preserving leaf-supported owner teleport candidates without
changing ordinary follow distances, priority, or speed.

Tree landing remains opportunistic. A runtime sample can legitimately show few
obvious landings when priority-9 stroll is blocked by higher goals or no clear
candidate exists in the small scan volume.

## Focused sibling migration audit

The remaining registered priorities, tame/sit/follow/defense goals, broad meat
temptation/taming/healing/breeding, offspring age/ownership, prey targeting,
flying navigation, slow fall, dimensions, tame attributes, spawn predicate,
persistence, sounds, feather loot, render-state baby transfer, unconditional
eye layer and Hedwig selection were rechecked. No additional demonstrated
semantic defect was found, and those runtime-passing systems were not changed.

The validator now rejects an Owl shoulder-landing goal, nearest-distance tree
scoring, the wrong water-search height, or loss of the modern leaf-enabled
owner-follow hook. These checks preserve specific final-source contracts; they
do not claim to prove runtime pathfinding.

## Static and package validation

- Clean offline Gradle build: PASS; Java test tasks report `NO-SOURCE`.
- Parity validator: PASS (259 blocks, 259 items, 10 entities, 1 block entity,
  31 sounds, 2 particles; 38 configured, 37 placed, 31 injections).
- Packaged Owl classes, three released textures, prey tag and loot: present.
- Owl nest recipe: absent; Taniwha entries and Stage 8 markers: zero.
- Packaged totals remain 856 models, 260 textures, 259 item definitions, 319
  recipes, 268 loot tables, 27 advancements and 76 tags.
- `git diff --check`: PASS. No Stage 6 production file or protected earlier
  system changed.

## Runtime PASS/open matrix

| Area | Status |
|---|---|
| Breeding crash | Runtime PASS |
| Baby state, rendering and aging | Runtime PASS |
| Black Thistle Weakness | Runtime PASS |
| Flight, general AI and hunting | Runtime PASS |
| Eyes and Hedwig texture | Runtime PASS |
| Shoulder mounting | Source-confirmed unreachable; closed |
| First-match tree landing translation | Runtime PASS |
| Leaf-safe owner following/teleport | Runtime PASS |

## Focused Prism retest

1. Use a tame, non-sitting Owl in a Dark Forest with leaves/logs and two-block
   clear spaces nearby. Let it idle away from food, prey and combat; observe
   several random-stroll cycles. Tree landing is opportunistic, not guaranteed
   on a timer.
2. Move more than 12 blocks from a following tame Owl near leaf canopies and
   verify it follows/teleports without becoming stranded by leaf support.
3. Do not wait for shoulder mounting; no released trigger exists.
4. Briefly recheck flight, sit/follow, hunting, breeding and baby appearance,
   aging, `Hedwig`, emissive eyes, and save/reload.

## Final acceptance

Prism runtime acceptance confirmed natural/general behavior, flight, AI,
hunting, taming, breeding without crashes, genuine baby state and rendering,
baby save/reload and adult transition, emissive eyes, `Hedwig`, Black Thistle
generation/Weakness, tree landing and leaf-supported owner following. Shoulder
mounting remains correctly absent for the source-confirmed reason above.

Stage 7 is **COMPLETE / RUNTIME ACCEPTED**. No mandatory final-release Owl
parity blocker remains.
