# Stage 12A.9 — Independent Stone Golem

The released `biomemakeover:stone_golem` is an independent `AbstractGolem`
with the released 1.6 × 2.5 dimensions, 60 health, crossbow attack behavior,
player-created flag, neutral anger state, released cladded-stone creation
pattern, model, renderer, spawn egg, healing tag, and 2–3 cladded-stone loot.

The creation pattern is the released form `~^~ / ### / ~#~`, where `^` is a
carved pumpkin and `#` is `cladded_stone`; the matched blocks are consumed and
one player-created golem is spawned with `MOB_SUMMONED` semantics. The
original external GolemHandler is not present in the port, so the equivalent
hook is scoped to `CarvedPumpkinBlock.trySpawnGolem`. No natural spawn rule is
registered.

The 1.21.10 port uses the released crossbow goal with the modern
`ChargedProjectiles` component API. The four released target families,
player-created behavior, persistent anger fields, crossbow equipment, and
standalone loot are retained. The released Stone Golem OGG files and event
families (`stone_golem_turn`, `stone_golem_stop`, `stone_golem_hurt`, and
`stone_golem_death`) are included unchanged.

The Adjudicator `STONE_GOLEM` phase remains execution-gated. Ordinary
player-created golems receive no Adjudicator encounter tag. Boss-created
membership and mounting remain Stage 12A.10 work.

`cladded_stone` is obtained through the released shapeless recipe: one
`crude_cladding` plus four `smooth_stone` produces four blocks. Loose stone or
cladded-stone item entities have no released conversion or creation mechanic.

The modern port declares `FOLLOW_RANGE=24` as a compatibility requirement of
the 1.21.10 target-goal implementation; it is not a balance change. Creation
constructs the entity before consuming the matched blocks.

## Scope boundary

No Mansion templates, structure NBT, arena data, Adjudicator controller,
combat phases, rewards, or Mythas trial systems were changed. The temporary
`BM_STONE_GOLEM_PARITY_PROOF` marker is retained for Prism entity/creation
testing and is intended for removal after acceptance.
