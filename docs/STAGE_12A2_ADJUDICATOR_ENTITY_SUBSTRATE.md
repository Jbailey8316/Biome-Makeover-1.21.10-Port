# Stage 12A.2 — Adjudicator Entity Substrate

Stage 12A.2 restores the released Adjudicator entity substrate and Mansion
`boss` marker spawn path. The registered entity is fire-immune, persistent,
non-despawning, and uses the released 0.6 × 1.95 dimensions, 255 health,
0.25 movement speed, 3 attack damage, and 50 XP reward. It spawns with the
released `STRUCTURE` reason and finalization lifecycle. Combat phases and
boss rewards remain intentionally deferred.

The released 1.20.1 model mesh, base texture, glowing-eye layer, and
neutral/head-look/basic-walk animation are retained. Localization restores
the English name `Adjudicator`.

R2A adds a small **MYTHAS ENHANCEMENT**: two dark pupil pixels at `(10,16)`
and `(13,16)` in the eye-layer texture. These pupils were not present as
distinct pixels in the released asset; the released light eye pixels at
`(9,16)` and `(14,16)` remain unchanged.

Stage 12A.2 is complete for the entity substrate, visual/name behavior, and
Mansion spawn parity. Full phase AI, Mimics, Stone Golem phases, arena
gameplay, rewards, Enchanted Totem behavior, and Mythas trial/key systems are
deferred to later stages.
