# Stage 12A.8 — Adjudicator Mimic phase

The released 1.20.1 Mimic is a phase-only registered entity,
`biomemakeover:adjudicator_mimic`, with no normal summon path or spawn egg.
It uses the Adjudicator appearance and eyes, 0.6 × 1.95 dimensions, 1 health,
0.25 movement speed, 3 attack damage, bow equipment, ranged AI, and released
arrow behavior.

The phase creates a random 3–6 Mimics at unique established arena positions
using the released natural spawn/finalization path. The phase ends when the
Adjudicator is hurt by a player; same-encounter Mimics are discarded on phase
exit. Their encounter tag is assigned through the existing Mythas alliance
helper, so they neither damage nor target allied encounter members while
players remain valid targets. Mimic interruption state is serialized with the
Adjudicator controller.

Stone Golem and all rewards remain deferred. Mansion templates and structure
NBT are unchanged.
