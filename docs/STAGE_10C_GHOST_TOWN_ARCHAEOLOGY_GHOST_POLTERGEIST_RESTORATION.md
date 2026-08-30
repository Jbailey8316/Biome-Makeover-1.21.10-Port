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
  navigation, no-gravity/no-fall movement, neutral-until-provoked targeting, and original
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

## Stage 10C.3 final runtime acceptance

Prism accepted the interactive paranormal systems. Ectoplasm is obtainable
with `/give` and the released source intentionally exposes it in the BM item
group; the modern port maps that placement to Natural Blocks immediately
after Scuttler Tail. Ectoplasm Composter conversion and functional composting
passed without errors.

The Poltergeist placed correctly with its released cauldron-like appearance,
enabled light, and state changes. All eight released action categories were
observed working: lever, trapdoor, door, bell, note block, button, fence gate,
and daylight detector. Possessed applied successfully, produced its released
paranormal interactions and particles, and remained stable. Save/quit/reload
left the Poltergeist functional; no Stage 10C.3 runtime errors were observed.

Poltergeist advancement JSONs remain deliberately deferred to Stage 10C.4:
the released parent chain begins at the Ghost Town advancement, so temporary
parents are not introduced. The trigger registrations remain available for
that later restoration.

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

## Stage 10C.2 final runtime acceptance

Stage 10C.2 is complete and runtime accepted. Prism confirmed that
`biomemakeover:suspicious_red_sand` is visible through the expected Creative
inventory exposure, is obtainable with `/give`, places without error, and
uses a compatible vanilla `BrushableBlockEntity`. Brushing, all dust stages,
and final conversion to red sand passed. Unsupported/falling behavior matched
vanilla Suspicious Sand and is accepted without custom persistence logic; no
runtime errors were observed.

No archaeology loot appears from a manually placed block in this substage,
which is expected: Ghost Town processor assignment and the dependency-bearing
top-level `biomemakeover:archaeology/ghost_town` table remain deferred to
Stage 10C.4. That table must later restore the exact released entries after
auditing and registering its Ghost Town disc, three pottery sherds, and Crude
Fragment dependencies; early item registration does not authorize later Crude
gameplay. The valid nested tables remain active, with `minecraft:chain`
translated to modern `minecraft:iron_chain`.

The original placement crash was caused by the custom block not being a member
of vanilla `BRUSHABLE_BLOCK`'s valid-block set. Final 1.20.1 used
Taniwha's `BlockEntityHooks.addAdditionalBlock`; the narrow 1.21.10
translation adds only Suspicious Red Sand to that set and leaves all vanilla
brush, seed, save/reload, falling, and conversion behavior intact. The
Stage 10C.2 validator now checks this compatibility, Creative exposure,
packaged assets, active nested loot references, and explicit top-level loot
deferral.

## Stage 10C.3 — Interactive paranormal systems

This implementation restores the final released Ectoplasm Composter and
Poltergeist loop without activating Ghost Town (10C.4). `ectoplasm_composter`
is a no-item `ComposterBlock` subclass. A partially filled vanilla composter
accepts one Ectoplasm through the source item's use action or the narrow
`ComposterBlock.InputContainer` mixin; it preserves the current level while
changing to the BM block. At level 8, player use emits one Soul Soil and
resets to a vanilla composter, while a downward hopper may extract one Soul
Soil through the source-compatible output container. The Ectoplasm Composter
has the released wood/six-tenths-strength properties and no independent block
entity. The complete released BM compostable table is registered into the
modern `ComposterBlock.COMPOSTABLES` map (including the three reed-thatch
entries); Peat Composter behavior remains separate.

`poltergeist` is a functional-block item with the released cauldron-like shape,
`enabled` state, light level 7, and a registered stateless
`poltergeist` BlockEntity. Redstone toggles the state after the source's
four-tick delay and emits the source sound/particle event. While enabled, its
server ticker performs one random action attempt in a 5-block cube. The
source action set is preserved: doors, buttons, trapdoors, levers, note blocks,
fence gates, daylight detectors, and bells. Air and stone are ignored; at most
one selected block is changed per invocation. Actions use native 1.21.10
block APIs and game events, with the final random candidate calculation and
action-success sound behavior retained. The old Taniwha block/wood sound
holders are not needed by the modern vanilla block classes; native block-set
door sounds and vanilla event sounds are used where available.

Entering an enabled Poltergeist applies/extends `biomemakeover:possessed` to
any LivingEntity in the released vertical intersection. Possessed's existing
10C.1 cadence is unchanged (`duration % 10 < min(amplifier + 1, 8)`); each
server tick invokes `PoltergeistHandler.doPoltergeist` up to
`min(amplifier + 1, 20)` times around the affected entity with range 4.
The block and effect share this server-only helper, but no generalized effect
application path was added. The `ectoplasm_compost`,
`poltergeist_yourself`, and native inventory advancement triggers are
registered now. Their released advancement JSONs are intentionally deferred
until 10C.4 because the parent chain starts at the Ghost Town advancement;
the parent is not activated by this substage and no invalid child resources
are packaged.

The 1.21.10 translation registers `poltergeist` through
`FabricBlockEntityTypeBuilder`, uses the current `BlockEntityTicker` and
`ValueInput`/`ValueOutput`-compatible stateless BE contract, and dispatches
particles with `ServerLevel.sendParticles`. The private daylight-detector
signal helper is reached only through a narrow mixin invoker. The particle is
the released 11-frame translucent sheet, and the two sound events retain the
released Soulspeed sound paths/subtitles. No Taniwha runtime dependency,
Ghost Town resource, archaeology activation, or later-stage system is
introduced.

### Stage 10C.3 validation and runtime gate

`validation/Invoke-Stage10C3Validation.ps1` checks registrations, block/entity
compatibility, source action set and ranges, Possessed invocation formula,
modern recipe/loot/advancement resources, packaged resources, particle frames,
sound events, and absence of 10C.4 structure/worldgen leakage. Static checks
do not replace Prism testing. The bounded runtime sequence is: obtain and
place Poltergeist (or use `/give @s biomemakeover:poltergeist`), verify its
enabled model; power it and observe the toggle; stand inside it and use
`/effect give @s biomemakeover:possessed 30 0`; observe particles, sounds,
and random nearby interactions; test a nearby door/button/lever and a note
block; repeat on a passive mob; then save/reload and confirm the block state
and effect behavior remain stable. Ectoplasm Composter should be tested by
filling a vanilla composter, applying Ectoplasm, completing it, and extracting
Soul Soil. Stage 10C.3 is implemented but awaits this runtime validation;
Ghost Town, archaeology assignment, and other 10C.4 systems remain deferred.

## Stage 10C.4 — Ghost Town integration

This implementation restores the final released Ghost Town integration as a
data-driven vanilla `minecraft:jigsaw` structure. The canonical IDs are
`biomemakeover:ghost_town`, structure set
`biomemakeover:ghost_towns`, biome tag
`#biomemakeover:has_structure/ghost_town`, and start pool
`biomemakeover:ghosttown/centers`. The structure uses size 3,
`WORLD_SURFACE_WG`, `beard_thin`, `surface_structures`, absolute start height
0, max distance 80, and the released expansion hack. Its monster spawn
override is Ghost, weight 150, groups 2–4, with piece bounding boxes. The
linear random-spread set is spacing 32, separation 12, salt 6969 and has no
exclusion zone or free Badlands spawn registration.
The active biome chain is explicit: `has_structure/ghost_town` replaces to
`#biomemakeover:badlands`, whose tag includes `#minecraft:is_badlands` and the
optional `#c:badlands` convention tag.

The transitive graph has four pools: `ghosttown/centers`, `roads`,
`buildings`, and `decoration`. The center pool retains seven equally weighted
rigid road roots (`street_01` through `street_07`). The graph contains one
center, seven roads, fifteen decorations, and twenty-seven houses, plus the
three water-tower elements in the building pool: 50 final NBT templates in
total. The audit prose called this 40 (and described 17 decorations), but its
own inventory and the released resource tree contain 50; source bytes and the
complete graph are preserved. All templates are packaged at the required
singular 1.21.10 paths under
`data/biomemakeover/structure/ghosttown/`; no plural duplicate is packaged.
The source and packaged SHA-256 bytes are compared by the Stage 10C.4
validator, and the Java validator DataFixes and loads all 50 through the
current `StructureTemplate` implementation (mixed source DataVersions 2580,
2584, 3098, and 3454).

The two released processor lists are active. `ghosttown_building` assigns one
of `ghost_town/loot_0`, `_1`, or `_2` to barrels, fills/replaces chiseled
bookshelves using the released weighted ranges, randomizes brick variants,
and performs suspicious replacement. `ghosttown_roads` changes water to oak
planks, changes dirt paths to red sand at the released 30% rule, and performs
suspicious replacement. The two Taniwha processor behaviors are local
`GhostTownLootProcessor`, `FillBookshelvesProcessor`, and
`SuspiciousBlockReplacementProcessor` registrations; Taniwha is not a runtime
dependency. During graph audit two released pool filenames were dangling:
`bell_decoration` and `tree_decoration`; the actual released files are
`bell_decoration_1` and `tree_decoration_1`, so the modern pool references are
narrowly corrected to those existing templates rather than shipping a
dangling graph.

The three ordinary chest tables retain the final source pools, rolls,
damaged/enchantable tools, books, resources, Cowboy Hat, and Ghost Town disc.
The complete one-roll archaeology table is now active and resolves its final
dependencies: the three pottery sherds, iron, gold, Crude Fragment, nested
horse armour and junk tables, Ghost Town disc, and optionally enchanted
damaged leather boots. The nested table codec uses modern `value` fields and
the historical `minecraft:chain` entry is translated to
`minecraft:iron_chain`; no dummy or vanilla substitute BM items were added.
Crude Fragment is registered only as the archaeology item dependency; Crude
cladding/progression and all later Crude systems remain deferred. The existing
Cracked Brick item is also mapped to the released cracked pottery pattern;
the three new sherds use native 1.21.10 `DecoratedPotPattern` registrations
and the item-to-pattern mixin.

`ghost_town_music_disk` is a rare native 1.21.10 jukebox item using the
original `ghost_town.ogg`, the `biomemakeover:ghost_town` sound event, and
song metadata with duration 270 seconds and comparator output 15 (the final
record constructor's comparator value). It is in the
music-disc tag, ordinary Ghost Town/archaeology loot, and the `badlands_disc`
inventory advancement. The advancement chain is restored with modern icon
and structure predicate forms: `ghost_town` (inside the generated structure),
`badlands_disc`, `compost_soul_soil`, `poltergeist`, and `going_ghost`, with
the released parent relationships. The former Poltergeist deferral is now
resolved because its Ghost Town parent exists; no temporary parent was added.

### Stage 10C.4 validation and runtime gate

`Invoke-Stage10C4Validation.ps1` validates the packaged JAR graph, exact
template count and byte identity, singular paths, all pool/fallback/template
and processor references, structure/set placement, active loot item IDs and
nested tables, `iron_chain`, sherd/pattern registrations, disc/song assets,
advancement parent and modern `structures` predicates, and Taniwha absence.
`Stage10C4TemplateRuntimeValidator` additionally DataFixes and loads every
packaged template through the 1.21.10 structure-template code. Existing
Stage 10A, 10B, 10C.1–10C.3, and integrated parity checks remain regression
gates.

The shortest Prism gate is: create a fresh disposable world, confirm clean
bootstrap, run `/locate structure biomemakeover:ghost_town`, teleport above
the result and verify a complete town (roads, buildings, decorations) is
physically present; inspect barrels and released loot, Ghosts, suspicious red
sand and brush it for archaeology loot (RNG-dependent), obtain sherds/Crude
Fragment/disc, play the disc and check comparator output 1, inspect each
relevant advancement, then save/reload and confirm no errors. Commands
`/place structure biomemakeover:ghost_town` (when supported),
`/give @s biomemakeover:refined_pottery_sherd`,
`/give @s biomemakeover:worker_pottery_sherd`,
`/give @s biomemakeover:whinny_pottery_sherd`,
`/give @s biomemakeover:crude_fragment`, and
`/give @s biomemakeover:ghost_town_music_disk` isolate deterministic checks.
Before the final Prism pass, runtime acceptance was pending; the final Prism
acceptance is recorded below.

### Stage 10C.4 runtime blocker remediation: processor number providers

The first Prism world-creation attempt exposed a 1.21.10 dynamic-registry
codec failure in `biomemakeover:ghosttown_building`. The released processor
JSON used the older nested UniformInt form (`"value": {"min_inclusive":
..., "max_inclusive": ...}`). In the modern `UniformInt` MapCodec those two
fields are direct members of the `minecraft:uniform` provider; the enclosing
`minecraft:weighted_list` still uses its `distribution` entries with `data`
and `weight`.

`ghosttown_building.json` now uses the direct modern fields while preserving
the released weighted ranges and weights exactly: 20 × 1–5, 10 × 3–10, 4 ×
7–15, and 1 × 20–35 (all inclusive). A real 1.21.10 codec validator boots
the vanilla registries and decodes `FillBookshelvesProcessor.CODEC`, then
decodes and checks each UniformInt range from the packaged resource. The
Stage 10C.4 PowerShell validator performs the same semantic checks on source
and packaged JSON. The other Stage 10C.4 provider resources were audited; no
additional occurrence of this obsolete nested UniformInt shape was found.

This remediation changes only the processor provider serialization and its
audit validation. At that point Stage 10C.4 remained implemented and awaited
the next Prism runtime validation; no structure, template, pool, loot, or
gameplay behavior was changed by the codec remediation.

### Stage 10C.4 final Prism runtime acceptance

The processor-codec-remediated candidate passed the final Prism run on
Minecraft 1.21.10 with Fabric Loader 0.19.3, Fabric API 0.138.4+1.21.10,
and no dynamic-registry errors. A fresh disposable world created normally;
recipes, advancements, and the integrated server loaded without an unbound
`ghosttown_building` processor-list entry.

`/locate structure biomemakeover:ghost_town` returned a real structure (the
observed location was approximately `[4192, ~, 2352]`). Teleporting there
showed a complete, coherent town: roads, multiple buildings, decorations,
and correctly assembled jigsaw layout. No missing-template or broken-jigsaw
failure was observed. Structure-associated Ghosts spawned, with no evidence
of free Badlands Ghost spawning.

Naturally generated Suspicious Red Sand was present in the town. Brushing
worked and produced cursed boots, confirming the live processor → suspicious
block → Ghost Town archaeology table → nested loot chain. Whinny, Worker,
and Refined Pottery Sherds, Crude Fragment, and the Ghost Town disc were
present and visually valid; the three sherds remained Creative-visible.
Archaeology remains RNG-dependent, so this observation does not claim every
weighted entry was rolled. Crude Fragment remains bounded to its active loot
dependency; broader Crude/cladding/armor gameplay is deferred to Stage 12B.

Ghost Town disc playback was accepted; static validation retains the released
jukebox comparator value 15. Runtime advancement activity included “Yee
hawnted” and “Ragtime Gal”. The world, all dimensions, player state, and
advancement state saved and reloaded cleanly at the town, and the integrated
server stopped normally with no structure, processor, or block-entity errors.

Stage 10C.4 is therefore **COMPLETE / RUNTIME ACCEPTED**. The 50-template
authoritative count correction (rather than the earlier prose count of 40),
the `bell_decoration_1` and `tree_decoration_1` pool filename corrections,
and the four exact weighted UniformInt ranges remain part of the accepted
implementation. The known offline `fabric-log4j-util:1.0.2` limitation is
unchanged and unrelated. Occasional clumsy Ghost terrain pathing remains a
non-blocking Stage 10C.1 observation only; no polish was added. Stage 10C.5
is the next closure/integration step and Stage 10C overall remains in
progress.

## Scope boundary and deferred work

No Mansion, Witch quest/primary Witch Hat drop, Crude gameplay, Stone Golem,
Adjudicator, Mimic, Beach/Stage 13, historical Badlands revival, free-roaming
Badlands Ghost spawning, terrain-blending polish, or Mythas enhancement is
included. Existing-world compatibility relies on normal new-chunk structure
generation; no retro-generation is introduced.

### Stage 10C.5 — integrated parity freeze

The final source-ownership walk and packaged-graph audit found no unexplained
missing active Stage 10C feature and no active parity defect. Stages 10C.1
through 10C.4 remain bounded to the released paranormal family: Ectoplasm,
Possessed, Ghost, Suspicious Red Sand/archaeology, Ectoplasm Composter,
Poltergeist, Ghost Town worldgen and its 50 templates, processors, loot,
pottery/Crude Fragment dependencies, disc/audio, structure-only Ghost spawn,
and the Stage 10C advancement chain. Each cross-system edge resolves in the
packaged JAR, including processor → suspicious sand → archaeology loot,
structure → Ghost spawn override, Ectoplasm → Composter/recipe, and
Possessed → Poltergeist actions.

The authoritative Ghost Town inventory is 50 templates (one center, seven
roads, 27 houses, and 15 decorations including three water towers), all under
the singular `data/biomemakeover/structure/ghosttown/` path. Source bytes,
hashes, DataFix/StructureTemplate loading, pool references, and the narrow
`bell_decoration_1`/`tree_decoration_1` filename corrections are frozen. The
`ghosttown_building` codec remediation retains the direct 1.21.10 UniformInt
fields and exact weighted ranges 20×1–5, 10×3–10, 4×7–15, and 1×20–35.

Crude Fragment is intentionally registered only as an active Ghost Town
archaeology dependency; broader Crude/cladding/armor/Pillager behavior remains
Stage 12B. Witch quests and primary Witch Hat drops remain Stage 12A. Mansion,
Stone Golem, Adjudicator, Mimics, Enchanted Totem, Beach/Stage 13, and other
later or historical/showcase content remain excluded. No Mythas enhancement
was introduced, and Taniwha remains absent as a runtime dependency.

`Invoke-Stage10CFreezeValidation.ps1` is the integrated freeze gate. It checks
the packaged 50-template graph, structure/set anchors, archaeology and disc
edges, comparator value 15, structure-only Ghost ownership, Taniwha absence,
and later-stage registration boundaries; the detailed 10C.1–10C.4 validators
remain in place. The full offline build, real processor codec, template
DataFix/load, per-stage validators, integrated parity checks, leakage scans,
and `git diff --check` all pass. The known uncached
`net.fabricmc:fabric-log4j-util:1.0.2` dedicated-server limitation is an
environmental gate only and was not worked around.

Stage 10C.5 is **COMPLETE / PARITY FROZEN** and Stage 10C is **COMPLETE**.
The occasional clumsy Ghost terrain pathing remains the previously accepted
non-blocking observation; no pathfinding polish was added. Stage 11A is next.
