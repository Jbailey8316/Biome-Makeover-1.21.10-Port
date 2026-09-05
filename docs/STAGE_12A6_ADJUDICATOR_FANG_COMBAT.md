# Stage 12A.6 — Adjudicator Fangs / Fang Barrage

Stage 12A.6 restores the released `EvokerFangs` attack phases through the
existing Adjudicator controller. Ordinary fangs retain the released 200-tick
phase and cast at the controller's 20-tick start plus 40-tick intervals. At
distances below 24 blocks, each cast uses five fangs at radius 1.5 and eight
at radius 2.5; otherwise it uses sixteen fangs in a target-facing line at
1.25-block increments. Warmups are `0`, `3`, or the line index as in the
released source.

Fang barrage retains the released 100-tick phase. It casts at ticks 50 and
100, sending ten fangs along each horizontal cardinal direction at distances
1 through 10, with per-fang random yaw and warmups from 0 through 9. Each
fang searches downward to the released vertical limit, requires an upward
sturdy support face, and uses the collision-shape top offset.

The temporary implementation gate now permits teleport, bow, melee, ordinary
fangs, and fang barrage. Ravager, summon, Mimic, Stone Golem, and rewards
remain execution-gated. Mansion templates, structure NBT, and frozen arena
systems are unchanged.

The coordination repair carries released controller state into the client
render state. `TELEPORT` and `SUMMONING` use the released Illager-style
casting arm pose; bow uses the aimed weapon pose; melee uses the native
weapon-swing animation. Fang phases stop navigation and direct look control
toward the validated arena target, while first-tick enrollment adds players
to the boss bar before combat transition. The bounded
`BM_ADJUDICATOR_FANG_PROOF` and `BM_ADJUDICATOR_COMBAT_COORDINATION_PROOF`
traces are temporary and gated by `bm.mansion.trace`.
