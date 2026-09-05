# Stage 12A.4 — Adjudicator Controller / Arena Lifecycle Substrate

Stage 12A.4 restores the released controller substrate without activating
combat execution. The entity now carries the released `STATE`, `CHARGING`,
and `INVULNERABLE` synced fields, phase inventory/timing constants, arena
initialization, player-scoped boss-bar membership, summon eligibility count,
and controller save/load state.

The arena is initialized from the boss position using the released smooth
quartz arena markers. Players inside the released arena bounds are added to
the blue health boss bar and removed when they leave; the bar updates from
entity health and is hidden while the controller is idle. Damage activates
the controller lifecycle, while the explicit `COMBAT_PHASES_ENABLED = false`
gate prevents transition into any unimplemented attack or summon phase.

The released phase definitions remain represented for later implementation,
with uniform eligibility data preserved and summon eligibility restricted to
fewer than four living arena Monsters. No placeholder attacks are installed.
Teleport, bow, melee, fangs, fang barrage, Ravager, summon, Mimic, Stone
Golem, rewards, and Mythas trial/key systems remain deferred.
