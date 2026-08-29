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
