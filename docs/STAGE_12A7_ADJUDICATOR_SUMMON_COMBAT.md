# Stage 12A.7 — Adjudicator Ravager / Vanilla Summon Combat

Stage 12A.7 opens the released Ravager and four vanilla-mob summon phases in
the existing Adjudicator controller. The summon phases are `spawn_evoker`
(two Evokers), `spawn_vindicator` (six Vindicators), `spawn_vex` (two Vexes),
and `spawn_mix` (three random Vexes, Vindicators, Evokers, or Pillagers).
They use the released 120-tick lifecycle and interval scheduling, summon at
the controller's established arena positions, finalize vanilla mobs with the
event spawn reason, and assign the current arena target.

The released Ravager phase creates one event-spawned Ravager at the
Adjudicator, mounts the boss, equips the released Multishot III crossbow, and
makes the boss invulnerable while mounted. Existing arena bounds, target
validation, summoning presentation, navigation ownership, and the broad
fewer-than-four living `Monster` eligibility rule remain authoritative.

The implementation gate now enables ten released phases: teleport, bow,
melee, fangs, fang barrage, Ravager, and the four summon phases. Mimic and
Stone Golem remain explicitly execution-gated. Rewards and all Mythas trial,
key, cache, and vault systems remain deferred. The temporary
`BM_ADJUDICATOR_SUMMON_PROOF` trace is gated by `bm.mansion.trace` and is
intended for Prism acceptance cleanup.
