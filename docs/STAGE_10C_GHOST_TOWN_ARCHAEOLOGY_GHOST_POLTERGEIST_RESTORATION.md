# Stage 10C.1 — Paranormal Foundations

This section records the bounded 10C.1 implementation. The authoritative
target is the final released Biome Makeover 1.20.1 source and resources.

## Restored contracts

* `biomemakeover:ectoplasm` is registered as the canonical stackable item,
  with its original item asset. Its Ghost-drop relationship is restored;
  Composter conversion remains deferred to 10C.3 and structure/archaeology
  acquisition remains deferred to 10C.2/10C.4.
* `biomemakeover:possessed` is a harmful effect with source color `0x20c09e`
  and the final ten-tick window (`duration % 10 < min(amplifier + 1, 8)`).
  Poltergeist world interaction is intentionally deferred to 10C.3; no
  generic application path is introduced here.
* `biomemakeover:ghost` is a manually testable flying Monster foundation with
  the final dimensions/category, no natural biome spawn registration, flying
  navigation, no-gravity/no-fall movement, hostile targeting, and original
  ambient/hurt/death sound events. Ghost Town marker spawning is deferred to
  10C.4. The renderer is client-only and uses the modern render-state API with
  the original Ghost texture.
* `biomemakeover:recipe/phantom_membrane` is the native 1.21.10 shapeless
  recipe: one Ectoplasm plus three Moth Scales produces one vanilla Phantom
  Membrane. No new advancement or alternate recipe is added.

Ghost audio assets are copied byte-for-byte from the final source resource
tree and referenced by the registered Ghost sound events. The modern item
definition/model layer is used for Ectoplasm and the existing spawn-egg
pipeline is used for controlled Ghost testing.

## Explicitly deferred

10C.1 does not restore Suspicious Red Sand/archaeology, Ectoplasm Composter,
Poltergeist, Ghost Town processors/templates/pools/worldgen, Ghost Town loot,
Badlands disc, or later Stage 10C/Stage 11+ systems. In particular, the Ghost
has no natural Badlands spawn and no structure integration in this substage.

## Validation and runtime gate

The offline Gradle build and the existing Stage 10A/10B packaged-template
checks must remain green. A bounded 10C.1 validator checks the canonical
registrations, modern recipe path/schema, singular loot-table path, original
Ghost assets, client/common separation, and absence of deferred resources.
Runtime acceptance is still pending: startup, controlled Ghost spawn, flying
AI/combat/audio, Ectoplasm drop, save/reload, Phantom Membrane crafting, and
any fully independent 10C.1 advancement are the required Prism checks.

## 10C.1 first runtime remediation

The first Prism candidate booted, but exposed two deterministic migration
defects. The Ghost spawn egg item definition referenced the obsolete
`minecraft:item/template_spawn_egg` model, which is not resolved by the
1.21.10 item-model path. It now points to a packaged `item/generated` model;
the existing build-time tinted vanilla spawn-egg pipeline emits the matching
Ghost texture using the final source colours.

The first `/summon biomemakeover:ghost` also crashed on its first tick because
1.21.10's `FlyingMoveControl` reads `Attributes.FLYING_SPEED` while the old
Monster attribute set no longer supplies it implicitly. The modern Ghost
attribute builder now adds the source-effective flying speed `0.6`; no global
movement change or exception suppression was introduced.

The remediation is committed as `750fd6b` and remains awaiting Prism runtime
validation. The shortest retest is: inspect the Ghost egg sprite, summon a
Ghost, wait 20–30 seconds, and verify stable ticking, rendering, and movement.

## 10C.1 second runtime remediation — Ghost rendering

The first remediation passed spawn-egg loading and Ghost ticking/movement, but
Prism showed a green translucent villager-like model. The source audit found
that final 1.20.1 uses a dedicated `GhostModel` (64×64 texture atlas), custom
body/head/arms/tapered lower-body hierarchy, source walking-arm/tail animation,
and an `entityTranslucent` render type. Villager geometry was therefore only a
placeholder artifact; translucency and airborne presentation are source-correct.

The port now carries the original model geometry in a modern
`GhostRenderState`/`EntityModel` pair, registers a dedicated `ghost` model
layer, copies the source head/pose and walk animation state into the render
state, uses the original `ghost.png`, and selects the translucent render type.
No extra glow, particles, bobbing, or gameplay behavior was added. The model
and renderer validator now rejects a missing layer or a vanilla placeholder
model. Runtime visual acceptance remains pending Prism retest.

## 10C.1 gameplay remediation — neutral anger and damage immunity

The previously accepted runtime state includes the Ghost spawn egg, summon,
stable ticking, flight/movement, dedicated renderer/model, released
texture/translucency, and Ectoplasm loot drop. Aggression and environmental
immunity remain runtime gates until the targeted Prism retest.

The final 1.20.1 Ghost is neutral when created. Its target priorities are
`HurtByTargetGoal` (priority 1, alerting nearby Ghosts), an
`NearestAttackableTargetGoal<Player>` (priority 2) gated by `isAngryAt`, and
`ResetUniversalAngerTargetGoal` (priority 3); movement/look goals do not make
a fresh Ghost hostile. Anger uses the vanilla `NeutralMob` contract with a
random 20–39 second timer, persisted UUID target and timer, expiry updates,
and alert bounds equal to follow range horizontally and 10 blocks vertically.
Manual and structure-created Ghosts share this behavior.

The port previously used an unconditional nearest-player target and had no
NeutralMob state. It now implements the modern 1.21.10 `NeutralMob` methods,
anger save/load helpers, source priorities, and bounded alert propagation;
the only API adaptation is using current `ServerLevel`/`ValueInput`/
`ValueOutput` signatures.

Final damage behavior is a blacklist only. The
`biomemakeover:ghost_immune_to` damage-type tag contains exactly lava,
in_wall, cactus, drown, sweet_berry_bush, hot_floor, fly_into_wall, and fall.
`GhostEntity.isInvulnerableTo(ServerLevel, DamageSource)` checks that tag and
otherwise delegates to vanilla behavior. The tag had previously been empty
and unchecked; no item, projectile, explosion, magic, or generic-fire
immunity was added.

The Stage 10C.1 validator now checks the NeutralMob contract, anger goal
gating/persistence, randomized anger duration, invulnerability hook, and the
exact eight-entry tag. These checks are semantic source-contract guards, not
a substitute for runtime AI/damage testing.

## Stage 10C.1 final runtime acceptance

Stage 10C.1 is complete and runtime accepted after the final Prism pass.
Ghost acceptance covers the spawn egg, summon, stable ticking, dedicated
source model and texture/translucency, flight, Ectoplasm loot, fresh
neutrality, retaliation, persistent 20–39 second NeutralMob anger, alert
propagation, save/load anger state, and the exact environmental damage
blacklist. Phantom Membrane crafting is accepted: one Ectoplasm plus three
Moth Scales shapelessly produces one membrane, and the recipe is learned after
manual crafting as observed.

Possessed applies to a player with the restored icon and particles visible;
its harmful color/cadence contract remains source-correct. Full effect-world
interaction is intentionally deferred to the Poltergeist implementation in
Stage 10C.3 and is not a missing 10C.1 feature.

Ghost flight can occasionally place a Ghost partly in terrain or temporarily
stick it there; observed recovery is possible. This is non-blocking and
accepted for parity. No pathfinding polish was performed; optional terrain or
flight polish is future work only.

The final accepted candidate was built and tested as
`build/libs/biomemakeover-fabric-1.21.10-0.8.5.jar` (SHA-256 recorded in the
stage handoff). Stage 10C.1 is now closed; Stage 10C.2 (archaeology) remains
the next substage, while Poltergeist integration remains Stage 10C.3.

## Possessed effect audit and icon remediation

The final effect is `biomemakeover:possessed`, a harmful MobEffect with color
`0x20c09e`. Its intrinsic tick cadence is every ten ticks, with active windows
`duration % 10 < min(amplifier + 1, 8)`. The released effect's tick invokes
`PoltergeistHandler` repeatedly (up to `min(amplifier + 1, 20)` actions), so
that world interaction is Poltergeist-owned and remains deferred to 10C.3;
there is no independent Ghost targeting, damage, movement, or generic player
application behavior in 10C.1. The already-restored particles and effect
application remain accepted, while full gameplay interaction awaits 10C.3.

The Prism failure was a missing status-effect icon. The final asset is the
unchanged `assets/biomemakeover/textures/mob_effect/possessed.png` (418 bytes,
SHA-256 `EDFD60E81985F0FD1BE8E234CAAEB1AA6201E6C252254FBCE902646CEBCD75B1`).
It is now packaged at the native 1.21.10 mob-effect texture path; no item
model indirection or artwork changes are involved. The Stage 10C.1 validator
requires the source icon path and non-empty packaged asset. Runtime icon
acceptance remains pending Prism retest.

## Stage 10C.2 — Ghost Town archaeology foundation

The final 1.20.1 archaeology block is `biomemakeover:suspicious_red_sand`, a
vanilla `BrushableBlock` which turns into `minecraft:red_sand` after brushing
and uses the vanilla suspicious-sand brush sounds, sand map colour, snare
instrument, 0.25 strength, suspicious sound, and destroy push reaction. It
uses the native 1.21.10 `BrushableBlockEntity` and
`BlockEntityType.BRUSHABLE_BLOCK` path; no custom block entity, Charmony
dependency, recipe, or standalone survival acquisition was added. Vanilla
brush progress, loot-table seed, item payload, save/reload, and falling-block
handling are retained by the current engine.

The released blockstate, four brushed-stage models/textures, item model, and
English translation are restored at modern paths. The block item is included
in the shovel-mineable tag. The final archaeology resources are
`biomemakeover:archaeology/ghost_town` plus its nested horse-armor and junk
tables. The top-level table preserves the released single-roll entries (BM
pottery sherds, iron, gold, Crude Fragment, nested tables, Ghost Town disc,
and damaged/optionally enchanted leather boots), but its active registration
is intentionally deferred until Stage 10C.4 because those later-owned BM item
IDs are not yet registered. The two nested tables remain active and valid;
their released `minecraft:chain` entry is translated to the current
`minecraft:iron_chain` item ID. Assignment to placed blocks is owned by Ghost
Town processors and also remains deferred to Stage 10C.4. The complete source
table is retained in the audit/reference contract for exact restoration then.

No archaeology advancement is independent of Ghost Town in the final
contract, so none is restored here. `Invoke-Stage10C2Validation.ps1` checks
registration, BrushableBlock construction, modern assets, translation,
archaeology table shape/entries, nested tables, and absence of later-stage
production leakage. These static checks do not replace runtime brushing,
falling, and persistence tests.

### 10C.2 Prism test procedure

Use `/give @s biomemakeover:suspicious_red_sand`, place it on a temporary
platform, brush through the dust stages, and confirm conversion to red sand.
Save/reload between brush attempts to verify persistence. The dependency-
invalid top-level archaeology table is not active in this substage; its
complete source form is restored with Ghost Town in Stage 10C.4. The nested
tables can be codec-tested independently, without adding debug production
content.
