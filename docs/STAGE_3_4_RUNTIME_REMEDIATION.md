# Stage 3/4 Client Runtime Remediation

## Checkpoint scope

This checkpoint responds to the first real client launch of the Stage 3/4 JAR. It was reviewed against
`docs/PORTING_ENGINEERING_RULES.md`. It does not start Stage 5, change Owl behavior, restore deferred
structures/progression, add Taniwha, or upgrade dependencies.

Tested predecessor: `biomemakeover-fabric-1.21.10-0.8.5-flight-eyes-blink-fix.jar`, SHA-256
`9E9115D3F46F7AA2F8397B53E6B0D7BCFC9E53F30381816AD9F9D7F93E420AF0`.

The reported runtime state is CLIENT RUNTIME VALIDATED only through mod initialization. World creation
failed during dynamic registry loading. No statement in this document claims that the remediated JAR has
passed client runtime, world creation, dedicated-server, multiplayer, save/reload, or existing-world tests.

## Authoritative codec finding

Minecraft 1.21.10's own `data/minecraft/worldgen/configured_feature/moss_patch.json`, read from the
official mapped 1.21.10 client/common artifacts used by this build, encodes a uniform `xz_radius` as:

```json
{
  "type": "minecraft:uniform",
  "min_inclusive": 4,
  "max_inclusive": 7
}
```

The released 1.20.1 `underground_mycelium` resource instead nested those bounds under `value`. The
1.21.10 `IntProvider` codec accepts either a number or the flattened typed provider and rejects that nested
shape. A deterministic resource conversion now flattens exactly this obsolete `xz_radius` form. All built
Biome Makeover configured and placed features were scanned; `underground_mycelium` was the only restored
resource containing the obsolete typed-provider-plus-`value` structure.

## Runtime issue disposition

| Reported issue | Status | Evidence and remediation |
|---|---|---|
| `mushroom_fields/underground_mycelium` decode failure | FIXED (static/package) | Converted nested uniform provider to the exact vanilla 1.21.10 flattened shape; validator rejects recurrence. Runtime retest required. |
| Ten missing Cracked Bricks block models | FIXED | Historical models existed under `models/block/decoration/cracked_bricks/`; Stage 4's non-recursive include omitted them. The complete directory is now packaged and model references validate. |
| `minecraft:item/template_spawn_egg` | FIXED | 1.21.10 vanilla spawn eggs use generated item models backed by individual textures; the old tint-template parent is absent. Glowfish, Scuttler and Cowboy eggs now receive deterministic modern models/textures using released primary/secondary colors and the current vanilla egg silhouette. Owl already had a modern model/texture. |
| Missing `item/mycelium_sprouts` | FIXED | Historical item texture existed but Stage 3 copied only the block texture. It is now packaged. |
| Missing `block/tumbleweed`; model says `tumbleweeb` | FIXED | `tumbleweeb` is a paired released typo: both the historical model reference and historical texture use that path. The texture is now copied under its historical path; no registry ID or model reference was renamed. |
| Missing `item/cracked_brick` | FIXED | Historical item texture existed but was omitted by the Stage 4 include. It is now packaged. |
| Axe tag references `#biomemakeover:tapestries` | INTENTIONALLY DEFERRED | Tapestries are not Stage 3/4 content. The premature cross-stage tag entry is filtered from the built axe tag; no Tapestry content was activated. |
| Cactus immunity and Scuttler food reference `pink_bud` | FIXED | Source tracing shows Pink Bud is produced by `gameplay/scuttler_eating`, gates the Scuttler Tail advancement, and belongs to the released Badlands loop. Pink Bud, its dye recipe, advancement, model/texture, and 1–2 item Scuttler flower-eating output are restored under the historical ID. This is Stage 4 gap closure, not Stage 5 content. |
| Tumbleweed spawn tag references `#biomemakeover:badlands` | FIXED | The released `badlands` biome tag is legitimate Stage 4 selector infrastructure and contains vanilla `#minecraft:is_badlands` plus optional `#c:badlands`. It is now packaged. |

No reported deterministic issue remains open in source/package validation. Actual registry loading and world
creation remain STILL OPEN pending the next Prism client test.

## Additional defects detected by the improved validator

- An existing Ancient Oak door model incorrectly referenced `willow_door_bottom`; it now references the
  already-packaged `ancient_oak_door_bottom`. No Willow/Stage 5 content was restored.
- Deferred Blighted Balsa boat/chest-boat models referenced absent textures and were being packaged despite
  the corresponding IDs being intentionally deferred. They are now excluded until approved boat plumbing
  restores the complete feature.

## Validator additions

`validation/Invoke-ParityValidation.ps1` now checks built output for:

- unresolved `biomemakeover:` model and parent references;
- statically resolvable internal block/item texture references;
- the removed `minecraft:item/template_spawn_egg` parent;
- nested `minecraft:uniform` providers using the obsolete `value` structure;
- unresolved internal BM tag-to-tag references;
- direct BM block/item/entity tag members missing from current registry contracts.

The checks intentionally do not guess about dynamic resource references or declare runtime success.

## Registry and world safety

Pink Bud is the only registry addition: items increase from 181 to 182. No existing block, item, entity,
feature, sound, or other persistent ID is removed, renamed, or repurposed. All remaining changes affect
resource decoding/rendering, deterministic validation, or packaging. Existing-world compatibility remains
additive; a copied-world runtime test is still required.

## Required Prism retest

1. Confirm Minecraft reaches title and Biome Makeover initializes.
2. Create a new world and confirm dynamic registries bind successfully.
3. Capture `latest.log`, even if creation succeeds, and search for Biome Makeover decode/model/texture/tag errors.
4. Inspect Mushroom Fields configured-feature generation, Cracked Bricks models, all four current BM spawn
   eggs, Mycelium Sprouts, Tumbleweed, Cracked Brick, and Pink Bud.
5. Exercise Scuttler flower eating and confirm 1–2 Pink Bud output, food temptation/passive interaction,
   advancement chain, and save/reload.
6. Continue the existing Stage 3/4 runtime checklists only after registry loading succeeds.

Dedicated-server, multiplayer, save/reload, and existing-world-copy validation remain manual requirements.

## Second runtime checkpoint: world-load success and targeted Badlands crash

The next Prism test advanced the runtime state: client bootstrap, resource loading, dynamic registry loading, world
creation, initial-spawn generation, and entering the world all passed. Targeted fresh Badlands generation then crashed
in `SaguaroCactusBlock.generateCactus`. This document does not claim that the remediation below has passed a Prism
retest. Fresh Badlands and fresh Mushroom Fields generation remain manual P0 gates.

### Saguaro root cause and remediation

The released implementation at historical commit `2f314c0596af095a4890995a465f308f69476b4a` always places the first trunk
block at the feature origin and only requires air for subsequent trunk blocks. It then verifies that an arm's center is
the Saguaro block before applying a directional property. The Stage 4 translation instead required the origin itself to
be air, although `WORLD_SURFACE_WG` supplied the surface position. It therefore placed no trunk, later read air at the
chosen arm center, and applied `north`/`south`/`east`/`west` to that air state.

The generator now faithfully translates the complete released algorithm: inclusive height range 4–8, random north-
south/east-west axis, 80% arm chance, released one/two-arm selection, independently randomized arm starts, guarded
center-state mutation, released arm-height calculation, and recursive tall-form chance. The feature and bonemeal paths
both pass the released axis/recursion parameters. The scan of all Stage 3/4 custom feature classes found no other code
that reads a world state and immediately applies a BM-only property; Saguaro was the sole matching placement-order
pattern. Runtime confirmation is still required.

### Global recipe audit

The packaged checkpoint before remediation contained 260 BM recipes. All were inspected across shaped crafting,
shapeless crafting, smelting, smoking, campfire cooking, and stonecutting. Minecraft 1.21.10 requires the recipe result
to be an item-stack object containing `id`; the historical resources used 107 result strings and 122 `{item: ...}`
objects. All 229 historical forms are deterministically converted while preserving output counts and every other
recipe field. The remaining 28 current recipes already used the modern form.

Three accidentally packaged recipes are excluded by ownership rather than papered over: Blighted Balsa boat and chest
boat remain deferred with their historical boat infrastructure, and `white_dye_from_buttonbush` belongs to Stage 5.
The Stage 3 hanging-sign recipe remains and maps the removed 1.20.1 `minecraft:chain` input to its 1.21.10 replacement,
`minecraft:iron_chain`. The final package contains 257 recipes: 131 shaped, 12 shapeless, 3 smelting, 1 smoking,
1 campfire-cooking, and 109 stonecutting.

### Advancement audit

All 17 previously packaged BM advancements were inspected. The nine reported children failed because their common
`biomemakeover/biomemakeover/root` parent had not been packaged. A Stage 3/4-safe root is now generated with only the
released Mushroom Fields and Badlands criteria; deferred Swamp/Dark Forest criteria and the hidden `icon_item` ID are
not activated early. The Cacti predicate's released singular `item` typo is normalized to the current `items` field.
The complete-log audit also found `glowfish_save` had an explicitly empty requirements array; removing that invalid
override lets the codec derive its sole criterion requirement. This root scaffold must be reconciled with the full
historical advancement tree in the owning progression stage.

### Pink Bud texture

The generated palette image existed in the JAR, but its `permutations/buds` path was outside the directories stitched
by the 1.21.10 block atlas. The same deterministic released palette output is now generated as
`textures/item/pink_bud_overlay.png`, and the released two-layer model points to that stitched item texture. No artwork
was invented.

### Validator expansion

The validator now rejects unsupported packaged recipe types, non-object/missing-ID recipe results, empty shapeless
ingredients, empty shaped patterns/keys, missing internal advancement parents, empty advancement criteria, mismatched
requirements, and the obsolete singular advancement item-array predicate. Existing model/texture validation covers
the Pink Bud's new resolvable item path. Static validation supplements, but does not replace, targeted runtime testing.

### Complete Prism log disposition

| Log family | Disposition |
|---|---|
| Saguaro feature-placement exception | **FIXED in source; runtime verification open.** Root-cause translation above. |
| 108 `Not a JSON object` recipe errors | **FIXED in package validation; runtime verification open.** Global result conversion. |
| Empty Buttonbush dye ingredients | **INTENTIONALLY DEFERRED.** Stage 5 recipe was accidentally packaged and is now excluded. |
| Blighted Balsa boat/chest-boat unknown IDs | **INTENTIONALLY DEFERRED.** Recipes removed until faithful boat infrastructure exists. |
| Blighted Balsa hanging-sign `minecraft:chain` | **FIXED in package validation.** Mapped to vanilla 1.21.10 `minecraft:iron_chain`. |
| Nine advancement descendants missing | **FIXED in package validation; runtime verification open.** Valid Stage 3/4 root and predicates. |
| `glowfish_save` requirements mismatch | **FIXED in package validation; runtime verification open.** Found only in complete log audit. |
| Pink Bud missing texture | **FIXED in package validation; runtime verification open.** Moved deterministic overlay into stitched item atlas path. |
| JourneyMap refmap warnings | **Unrelated third-party compatibility warning.** No BM action. |
| Bare `onRegisterSpecialGuiRenderer()` warning | **Unrelated integration/launcher warning.** Surrounding initialization is third-party; no BM stack or resource. |
| Dynamic-transform UBO resize messages | **Harmless Minecraft renderer informational messages.** No BM action. |

### Mandatory next Prism gates

1. Confirm client/resource/dynamic-registry/world-creation gates still pass and capture the full log.
2. Confirm all 257 BM recipes decode without warnings and sample each recipe type.
3. Confirm all 18 BM advancements decode; exercise the Mushroom Fields/Badlands roots and Stage 3/4 child criteria.
4. Inspect Pink Bud in inventory/world and confirm both model layers render.
5. Generate fresh Badlands chunks around Saguaro features; inspect height, axes, one/two arms, collisions, and absence of
   feature-placement crashes. This is required before the crash can be called runtime-fixed.
6. Generate fresh Mushroom Fields chunks separately. No Mushroom Fields safety claim exists yet.
7. Continue entity, save/reload, dedicated-server, multiplayer, and existing-world-copy checks.

## Third runtime checkpoint: Scuttler temptation attribute

The next Prism run passed client/resource/dynamic-registry loading, world creation, initial spawn, all recipe and
advancement decoding, and application of all 33 biome modifications. Teleporting into newly located Badlands also
succeeded. Four seconds later the integrated server crashed while ticking `biomemakeover:scuttler` because its
`TemptGoal` requested absent `minecraft:tempt_range`. No earlier Saguaro exception appeared in that short interval,
but this is not enough targeted generation coverage to mark Saguaro runtime-validated.

### Historical/current source finding

Released 1.20.1 Scuttler registered `createMobAttributes()` plus 10 health and 0.25 movement speed. Its priority-one
Taniwha `TagTemptGoal` followed the `scuttler_food` tag at speed 0.7; the remaining released chain included Float,
Rattle, Panic, Breed, conditional player avoidance, flower eating/search, Follow Parent, daylight avoidance, random
stroll, player look, and random look goals.

The current local translation correctly replaced tag temptation with vanilla `TemptGoal` over the same item tag and
preserved speed 0.7. Minecraft 1.21.10 changed the contract: `TemptGoal.canUse()` calls
`getAttributeValue(Attributes.TEMPT_RANGE)`, and vanilla `Animal.createAnimalAttributes()` now extends the mob builder
with `TEMPT_RANGE = 10.0`. Scuttler retained the released `createMobAttributes()` call, so the first temptation scan
threw before ordinary AI could tick. Scuttler now starts from `createAnimalAttributes()` and retains its explicit
health/speed values. This is the current vanilla compatibility contract and preserves the historical effective
10-block temptation range; no goal was suppressed or removed.

### Restored-entity sibling audit

| Entity | Goals/base attributes reviewed | Result |
|---|---|---|
| Scuttler | Tempt, Float, Rattle, Panic, Breed, Avoid Player, Eat Flower, Follow Parent, random movement/look | `TemptGoal` was the only goal reading a separately required modern attribute. Fixed via the Animal base builder. Existing broader behavioral parity debt is unchanged. |
| Glowfish | Salmon/AbstractFish movement, schooling, panic/avoidance, bucket persistence | Uses vanilla `AbstractFish.createAttributes()` and no Tempt goal; no missing modern attribute contract found. |
| Cowboy | Pillager goals, targeting, crossbow movement, patrol/riding/passenger behavior | Registered with current `Pillager.createAttributes()`, including health, movement, attack, and follow range; no custom goal attribute gap found. |
| Tumbleweed | Non-Mob entity tick, wind movement, collision, water, lifetime | Has no AI goal selector or attribute supplier; not applicable. |
| Owl | Tempt, breeding, panic, melee/targeting, following, flight | Already explicitly registers `TEMPT_RANGE` (6.0) plus movement/flying/health/attack attributes. No Owl code or approved Mythas behavior changed. |

Current 1.21.10 bytecode inspection found no additional attribute reads in the restored Panic, Breed, Follow Parent,
Avoid Entity, Melee Attack, Nearest Target, or Follow Owner goal implementations. The validator now rejects any BM
entity that constructs `TemptGoal` without either using `createAnimalAttributes()` or explicitly adding
`Attributes.TEMPT_RANGE`.

### Runtime status after this report

- Resource, dynamic-registry, recipe, advancement, and 33-biome-modification loading: **CLIENT RUNTIME VALIDATED for
  the reported run**.
- Scuttler attribute remediation: **STATICALLY/PACKAGED VALIDATED ONLY; Prism retest required**.
- Saguaro: **STILL OPEN for adequate targeted fresh-chunk runtime verification**.
- Fresh Mushroom Fields, complete Badlands worldgen, Scuttler goals/interactions/persistence, Cowboy riding, Glowfish,
  Tumbleweed, save/reload, dedicated server, multiplayer, and existing-world copy: **MANUAL TEST REQUIRED**.

## Fourth runtime checkpoint: stable biomes and visual/behavioral parity

The reported Prism run remained stable through extended Badlands and Mushroom Fields exploration and normal saves.
Scuttler's `TEMPT_RANGE` remediation is runtime PASS. Basic generation/stability, recipe/advancement loading, and all
33 biome modifications are PASS. This does not establish complete visual, entity, persistence, server, or multiplayer
parity.

### Tumbleweed

**Historical behavior:** the released entity rendered its cross-model block on the block atlas, accumulated client
quaternion rotation from actual X/Z travel, and slerped between ticks. Physics included a per-entity 2/3–1 wind
multiplier, 0.0025 acceleration toward global wind, gravity, a 0.31–2.0 bounce from 75% of prior vertical velocity,
impact particles/sound, water buoyancy, entity collision, 1500-tick lifetime, 100-stationary-tick removal, and damage
immunity. Spawning grouped eligible players within 50 blocks and rolled 1/200 per group in the released biome tag.

**Root cause/change:** the port discarded quaternion animation and submitted a static block, omitted Tumbleweed from
CUTOUT, fixed global wind's initial vector, and omitted several physics/lifetime/collision/immunity/spawn contracts.
A dedicated render state now extracts and slerps released rotation; CUTOUT, transforms, wind variation, bounce,
particles, water response, collision, stationary/age removal, immunity, and grouped biome-gated spawning are restored.
The paired historical `tumbleweeb` model/texture typo remains intentional.

**Status:** statically validated; rendering, smooth rotation, wind, bounce, collision, water, immunity, spawning,
lifetime, and reload behavior require Prism verification.

### Saguaro dimensions

Historical `RandomUtil.randomRange(min,max)` used `nextInt(max-min)+min`, so its upper bound was exclusive. Released
trunk segments are 4–7 blocks, and arm-start/arm-height variation use the same exclusive rule. The crash remediation
made all bounds inclusive, allowing 8-block segments and taller arms; recursion amplified the difference. The helper
now exactly restores exclusive bounds, with a validator guard and no arbitrary cap. Crash stability is runtime PASS;
representative height, arms, and recursive visual distribution remain OPEN.

### Scuttler sounds

Released Scuttlers have no ordinary ambient vocalization. Non-fluid steps play `scuttler_step` at volume 0.10 and
randomized 1.25+ pitch; hurt/death use dedicated events. Rattling occurs only when a non-passive, non-submerged
Scuttler and a non-creative/non-spectator player have mutual sight, the player is 10–20 blocks away, and is not holding
Scuttler food. Its cadence follows sign changes in the released sine-driven animation.

The port instead rattled for every player within 20 blocks and used an invented eight-tick cadence. The complete threat
predicate, released cadence, and step sound are restored. Threat boundaries, sight, creative/spectator/food/water/
passive suppression, cadence, footsteps, hurt, and death require runtime verification.

### Cowboy audit

Released Badlands patrol members used the Pillager patrol predicate, spawned as finalized Cowboys riding finalized
horses, inherited Pillager crossbow AI, synchronized horse yaw, wore Cowboy Hats normally, and gave patrol leaders a
target/banner plus a visible horse Hat. Horse `Hat` and `CowboySpawned` state persisted and affected despawning. Leader
death integrated the pre-1.21 Bad Omen and Voluntary Exile behavior.

Current patrol replacement, passenger creation, Pillager AI/attributes, yaw, ordinary hat, texture, and modern spawn
reasons exist. The predicate is corrected from the custom type to historical `EntityType.PILLAGER`. Leader banner
finalization, horse hat rendering/state, horse persistence/despawn marker, exact hat drop chance, and faithful mapping
of old leader rewards to 1.21.10's changed raid-omen system remain known gaps; none were guessed here.

Cowboy behavior is UNTESTED. The next run must force a Badlands patrol and verify mount/persistence, ordinary and leader
equipment, navigation/targets, crossbow combat, yaw, despawn, death/drop/reward, save/reload, and rendering.

### Glowshroom placement and terrain

Released Orange Glowshrooms are attempted twice per chunk at `OCEAN_FLOOR`, sample a fading nine-block radius, require
water, become waterlogged, and must survive on their substrate. The observed underwater Orange is intended. Green and
Purple patches are sampled twice per chunk uniformly from Y=-30 through 60 in air subject to plant survival; Purple at
Y=-8 is intended. Underground huge Purple/Green selection uses the same band with 120 attempts, while underground
vegetation includes low-weight Green/Purple among roots, sprouts, ordinary mushrooms, and tall mushrooms.

Built configured/placed resources match the pinned released definitions, so no placement change was made. BM injects
features/spawns only; it does not alter noise routers, terrain shape, sea level, or vanilla Mushroom Fields geometry.
An ocean-heavy first `/locate` result is not evidence of a BM terrain defect. Full distribution/substrate/huge-feature
runtime parity remains OPEN.

### Glowfish

Released Glowfish was a fixed-size Salmon using the original/default model, 32x32 texture, full block light, and a
non-baby Orange Glowshroom attachment on `body_back`; the top rear fin was hidden. Minecraft 1.21.10 added randomized
small/medium/large Salmon variants after release. Glowfish inherited those new model/dimension choices even though its
texture and attachment target the historical default, and its migrated renderer stopped hiding the rear fin.

Glowfish now reports the historical default Salmon variant and the renderer hides/restores `top_back_fin` around base
submission before rendering the released full-bright attachment. Swimming/schooling/bucket behavior remains inherited;
no generic replacement was introduced. UVs, orientation, animation, fin/attachment, lighting, swimming, flop, bucket
capture/release, persistence, and baby behavior require Prism verification.

### Updated runtime matrix

| Gate | Status |
|---|---|
| Scuttler `TEMPT_RANGE` remediation | **RUNTIME PASS** |
| Badlands basic generation/stability | **PASS** |
| Mushroom Fields basic generation/stability | **PASS** |
| Recipes, advancements, 33 biome modifications | **PASS** |
| Saguaro crash remediation | **RUNTIME STABILITY PASS; VISUAL PARITY OPEN** |
| Tumbleweed rendering/movement | **FAIL OBSERVED; STATIC REMEDIATION; RETEST OPEN** |
| Scuttler sound parity | **OPEN; STATIC REMEDIATION** |
| Cowboy mob behavior | **UNTESTED; KNOWN GAPS** |
| Glowshroom placement | **SOURCE-COMPARED; REPORTED PLACEMENTS EXPECTED; FULL DISTRIBUTION OPEN** |
| Glowfish rendering/runtime | **FAIL OBSERVED; STATIC REMEDIATION; RETEST OPEN** |

Stage 3 and Stage 4 do not yet have full runtime parity.

## Fifth runtime checkpoint: remediation 2

The input Prism JAR (`B177E9E7...A52793`) was stable in both restored biomes. Resource/registry loading, recipes,
advancements, 33 biome modifications, Scuttler TEMPT_RANGE, basic Badlands/Mushroom Fields generation, Saguaro crash
stability, and the reported Mushroom Fields vegetation observations are runtime PASS. Underwater Orange Glowshrooms
and underground Purple Glowshrooms near Y=-8 remain source-confirmed released placements.

### Tumbleweed and Glowfish white geometry

**Historical behavior established:** both released renderers used the ordinary block renderer for an atlas-backed
cutout block: the Tumbleweed cross model and the Glowfish's Orange Glowshroom attachment. The historical assets,
model paths, transforms, full-bright Glowfish lighting, fin visibility, and tumble rotation are present.

**Root cause/change:** the 1.21.10 render-state translations passed `-1` as the last `submitBlock` argument, treating
it like an old tint sentinel. Current mappings establish that argument as `outlineColor`; `-1` requests opaque white.
Both submissions now use `0` (no outline). A validator rejects this migration error in attached-block renderers.

**Static status:** compiled/validator-gated after the final build. **Runtime:** Tumbleweed texture/orientation and
Glowfish body/attachment/fin/full-bright rendering remain RETEST REQUIRED. Glowfish swimming plus bucket capture,
release, and save/reload remain OPEN. The empty vanilla cod/salmon ambient-event messages are not emitted by BM code;
Glowfish defines no ambient sound override, so no suppression or unrelated sound change was made.

### Saguaro total height

**Historical behavior established:** every segment is 4-7 blocks; initial recursion is 10%, each recursive segment is
2%, and recursion starts at the actually placed trunk end. Crucially, released `RandomUtil.randomRange` used a separate
shared `RandomSource`; only branch direction and recursion rolls consumed the feature RNG.

**Root cause/change:** the first remediation put all range draws onto the feature RNG. Although the marginal segment
ranges were correct, it changed the recursion-roll call sequence for each seed and could turn ordinary historical
examples into recursively stacked examples. Shape ranges again use a separate shared RNG, preserving released RNG
ownership, bounds, call order, and uncapped recursive distribution. No arbitrary height cap was introduced.

**Static status:** source-matched. **Runtime:** crash and generation PASS; representative height/distribution RETEST.

### Scuttler animation, food, and natural spawning

**Historical behavior established:** RATTLING is synchronized, client `rattleTime` advances while true, and the model
adds `sin(rattleTime)` to tail, tail3 and rattler rotations. The released articulated tail has tail/tail2/tail3/rattler
segments. Natural spawn is CREATURE weight 4, group 1-2, 50% predicate, bright-enough, no other Scuttler in a 50-block
inflated box, with maximum spawn cluster size 1. `scuttler_food` contains Pink Bud, driving temptation, breeding and
the threat-state food-holder suppression observed in Prism.

**Root cause/change:** the simplified modern model omitted tail2/tail3 and the renderer never transferred custom
rattle time. The released hierarchy and rotations are restored through a dedicated render state. The missing 50-block
density exclusion and single-cluster override are also restored; registered weight/group values already matched.

**Runtime evidence retained:** TEMPT_RANGE, Pink Bud attraction/breeding hearts, fleeing, contextual rattle and food-
holder suppression PASS. **Runtime open:** visible tail animation and statistically representative natural spawning.

### Cowboy released contract

Released direct summons are Cowboys with the custom texture but are not guaranteed to run natural-spawn finalization;
the historical Hat render layer itself displayed the cap unconditionally. The actual system replaces Badlands patrol
members with finalized Cowboys riding finalized horses. Ordinary members receive the Cowboy Hat through equipment
finalization (historical head drop chance 25%); leaders replace it with the custom ominous banner (guaranteed drop),
set a patrol target, and mark the horse for a visible hat. Horse `Hat` and `CowboySpawned` state are serialized and the
latter controls patrol-horse despawning. The released death hook emulated pre-1.21 Bad Omen/Voluntary Exile because
the custom banner was not vanilla's raid banner.

The current patrol replacement/mount, Pillager AI, passenger yaw, entity texture, and ordinary equipment exist, but
the historical unconditional Cowboy renderer hat, exact drop chances, leader banner, horse synchronized hat/layer,
horse persistence/despawn marker, and a proven 1.21.10 raid-omen translation remain implementation gaps. They are not
papered over in this focused render/animation remediation. Deterministic Prism coverage must temporarily force the
patrol-spawner conditions in fresh Badlands (day >5, local player, patrol spawning enabled), observe ordinary and
leader mounted members, then test equipment, combat, death/drops/reward, save/reload, restart and despawning. Direct
`/summon biomemakeover:cowboy` tests only the direct entity path, not patrol finalization.

### Runtime status after remediation 2

| Contract | Status |
|---|---|
| Scuttler TEMPT_RANGE / Pink Bud attraction+breeding / flee / contextual rattle | **RUNTIME PASS** |
| Scuttler tail animation / natural spawn parity | **STATIC REMEDIATION; RETEST OPEN** |
| Saguaro crash / natural generation | **RUNTIME PASS** |
| Saguaro historical height/distribution | **STATIC REMEDIATION; RETEST OPEN** |
| Tumbleweed stability | **RUNTIME PASS** |
| Tumbleweed movement | **RUNTIME PARTIAL/PASS; FINAL CONFIRMATION OPEN** |
| Tumbleweed rendering | **STATIC REMEDIATION; RETEST OPEN** |
| Glowfish stability | **RUNTIME PASS** |
| Glowfish rendering / bucket persistence | **STATIC REMEDIATION / OPEN; RETEST REQUIRED** |
| Cowboy summon/base behavior and Hat item/player rendering | **RUNTIME PASS** |
| Cowboy patrol/mount/leader system | **OPEN** |

Stage 3 and Stage 4 remain short of full runtime parity.

## Sixth runtime checkpoint: focused Glowfish, Saguaro, and Wild Mushroom pass

The input Prism run confirms Tumbleweed rendering/rolling and the Scuttler threat, food, flee, and visible rattler
remediations as runtime PASS. Those implementations were not modified in this pass. Underwater Orange Glowshrooms
remain released behavior and their worldgen was not changed.

### Glowfish attachment transform

**Historical contract:** `GlowfishRender` registered a `RenderLayer` on the Salmon renderer. The layer ran inside the
living-entity renderer's already oriented/scaled/model-animated pose, then applied `body_back.translateAndRotate`,
translate `(0,0,0.5)`, X rotation `-90`, scale `(-0.75,-0.75,0.75)`, and translate `(-0.5,0,-0.5)`. It submitted the
Orange Glowshroom with the entity light/overlay. The non-baby visible fish used the default Salmon model, hid
`top_back_fin`, and received full block light.

**Migration defect:** the port copied the local offsets but submitted the attachment after `super.submit` returned.
In 1.21.10, `LivingEntityRenderer.submit` owns the entity rotation, Salmon swimming rotation, model animation, scaling,
and layer traversal inside a pushed pose. The later standalone submission therefore started outside those transforms;
`body_back` alone could not reconnect it to the fish.

**Change:** the attachment is again a modern `RenderLayer`, so it inherits entity/Salmon animation and then applies
the exact historical body-part/local transform. Fin suppression still surrounds the complete superclass submission,
the default variant remains pinned, and full-bright lighting, scale, block model, texture, bucket code, and persistence
are unchanged. Static/package validation can establish transform structure, not visual attachment; Prism retest is
required.

### Saguaro repeated-growth origin

The feature entry, mutable cursor, 4-7 exclusive segment range, arm placement, separate shape RNG, recursion origins,
and 10%/2% continuation logic now match the released source. The remaining defect was outside the feature method:
released `isValidBonemealTarget` additionally required the block below the growth origin to be sand or red sand.
Current code had lost that check, so any exposed ordinary vertical Saguaro segment could random-tick and generate an
entire new 4-7-block structure from itself. Repetition produced the frequent tall thin towers despite correct feature
recursion probabilities.

The exact sand/red-sand growth-origin gate is restored. Natural feature generation, recursion percentages, RNG draws,
branching, IDs, placement density, and shape are unchanged. Existing already-generated towers are not rewritten;
fresh chunks are required for meaningful runtime evidence.

### Wild Mushroom visual audit

The released and current blockstates both select, with equal unweighted alternatives, the same three
`wild_mushrooms_1/2/3` models. Each model deliberately inherits `minecraft:block/cross`, so crossed/intersecting flat
planes are the historical geometry. Model parents and texture references are semantically identical; all three PNGs
are byte-identical to the pinned released assets. The current block retains the released 12x9x12 voxel shape, CUTOUT
layer, no collision/no occlusion, random ticks, fungus sound, flammability, three-variant placement-independent state,
loot, and worldgen resources. No model rotation or directional block state existed historically.

**Result:** the reported crossed cluster appearance is source-confirmed released behavior. No Wild Mushroom code,
resource, worldgen, loot, recipe, placement, or interaction change was made. Visual confirmation remains useful, but
there is no evidence-supported migration defect to patch.

### Focused status

| Contract | Status |
|---|---|
| Tumbleweed major rendering/rolling remediation | **RUNTIME PASS; UNCHANGED** |
| Scuttler major threat/food/flee/rattler remediation | **RUNTIME PASS; UNCHANGED** |
| Glowfish attachment transform | **STATIC REMEDIATION; VISUAL/BUCKET/RELOAD RETEST REQUIRED** |
| Saguaro feature crash/generation | **RUNTIME PASS** |
| Saguaro repeated growth / fresh-chunk height distribution | **STATIC REMEDIATION; RETEST REQUIRED** |
| Wild Mushroom model/resources | **SOURCE-CONFIRMED HISTORICAL; NO CHANGE** |

Stage 3 and Stage 4 remain short of full runtime parity.
