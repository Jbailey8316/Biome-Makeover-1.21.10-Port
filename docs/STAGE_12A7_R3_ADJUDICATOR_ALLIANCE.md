# Stage 12A.7-R3 — Adjudicator encounter alliance

The released 1.20.1 Adjudicator sources do not define an encounter-wide
ownership or friendly-fire system; summon phases use ordinary mob goals. This
change is therefore classified as the Mythas enhancement **Adjudicator
Encounter Alliance**, scoped to one boss encounter.

Each Adjudicator and its direct Ravager/summons receive a persistent entity
scoreboard tag containing that encounter's UUID. Entity tags survive entity
save/reload without changing Mansion structure NBT. Same-tag entities are
allied for vanilla targeting and same-encounter damage is denied, including
projectile and EvokerFangs ownership resolution. The vanilla
`Evoker$EvokerSummonSpellGoal.performSpellCasting` insertion call is redirected
after `Vex.setOwner`, so tagged Evokers copy the tag to exactly the Vex being
inserted. Player damage, unrelated mobs, and different encounter IDs remain
unaffected.

Summon counts, timing, the four-monster eligibility threshold, phase cadence,
and R2 selection behavior are unchanged. Mimic, Stone Golem, and rewards
remain deferred.
