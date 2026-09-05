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
standalone loot are retained. Released custom Stone Golem audio files are not
available in the port asset set; lifecycle sound events use valid vanilla
Iron Golem event references to avoid invalid-resource warnings.

The Adjudicator `STONE_GOLEM` phase remains execution-gated. Ordinary
player-created golems receive no Adjudicator encounter tag. Boss-created
membership and mounting remain Stage 12A.10 work.

## Scope boundary

No Mansion templates, structure NBT, arena data, Adjudicator controller,
combat phases, rewards, or Mythas trial systems were changed. The temporary
`BM_STONE_GOLEM_PARITY_PROOF` marker is retained for Prism entity/creation
testing and is intended for removal after acceptance.
