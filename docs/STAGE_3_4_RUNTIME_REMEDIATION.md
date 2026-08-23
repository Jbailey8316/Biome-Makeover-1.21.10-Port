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
