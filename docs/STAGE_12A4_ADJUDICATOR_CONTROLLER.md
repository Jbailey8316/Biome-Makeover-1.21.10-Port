# Stage 12A.4 — Adjudicator Controller / Arena Lifecycle Substrate

Stage 12A.4 restores the released controller substrate without activating
combat execution. The entity now carries the released `STATE`, `CHARGING`,
and `INVULNERABLE` synced fields, phase inventory/timing constants, arena
initialization, player-scoped boss-bar membership, summon eligibility count,
and controller save/load state.

The arena is initialized from the boss position using the released smooth
quartz arena markers and activates the encounter on its first server tick.
Players inside the released arena bounds are added to the blue health boss bar
and removed when they leave; the bar updates from entity health and follows
the active encounter lifecycle. The explicit `COMBAT_PHASES_ENABLED = false`
gate prevents transition into any unimplemented attack or summon phase while
leaving arena membership and boss-bar behavior live for substrate testing.

R1 corrected the activation boundary after runtime testing: arena setup marks
the encounter active on the first server tick, independently of the disabled
combat-execution gate. This preserves released boss-bar enrollment and
visibility without enabling any attack phase.

The released phase definitions remain represented for later implementation,
with uniform eligibility data preserved and summon eligibility restricted to
fewer than four living arena Monsters. No placeholder attacks are installed.
Teleport, bow, melee, fangs, fang barrage, Ravager, summon, Mimic, Stone
Golem, rewards, and Mythas trial/key systems remain deferred.

Stage 12A.4-R2 cleanup removed the temporary controller proof logging. The
production controller has no trace-only state or callbacks.
