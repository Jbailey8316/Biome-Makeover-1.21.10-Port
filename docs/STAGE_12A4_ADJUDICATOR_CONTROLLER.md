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
the active encounter lifecycle. Stage 12A.5 opens only the source-backed
teleport, bow, and melee execution paths behind the temporary
`BM_STAGE12A5_IMPLEMENTED_PHASE_GATE`; the remaining phases remain gated.

R1 corrected the activation boundary after runtime testing: arena setup marks
the encounter active on the first server tick, independently of the disabled
combat-execution gate. This preserves released boss-bar enrollment and
visibility without enabling any attack phase.

The released phase definitions remain represented for later implementation,
with uniform eligibility data preserved and summon eligibility restricted to
fewer than four living arena Monsters. No placeholder attacks are installed.
Fangs, fang barrage, Ravager, summon, Mimic, Stone Golem, rewards, and Mythas
trial/key systems remain deferred. Teleport uses the released arena-position
selection, timing, relocation, portal effects, and anti-trapping behavior.
Bow and melee use the released goal parameters, equipment, timing, and
projectile/melee paths. Mid-phase controller state, pending teleport targets,
and arena data remain serializable across reload.

Stage 12A.5 teleport, bow, and melee execution was runtime accepted. The
temporary combat proof logging was removed during the R1 cleanup; the staged
implementation gate remains active until the remaining released phases are
ported. No Mythas combat tuning was added.

Stage 12A.4-R2 cleanup removed the temporary controller proof logging. The
production controller has no trace-only state or callbacks.
