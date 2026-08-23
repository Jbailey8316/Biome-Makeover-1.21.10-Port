# Stage 1 — Taniwha Replacement Map

Date: 2026-08-23

Historical source: Lemonszz/Biome-Makeover `1.20` at
`2f314c0596af095a4890995a465f308f69476b4a`, using Taniwha
`1.20.0-5.4.4`. Import counts below are derived from released common, Fabric
and Forge Java source. “Uses” is a lexical reference count after the import;
it measures scope, not runtime reachability.

## A. Historical Taniwha dependency inventory

The released tree imports **55 distinct Taniwha API names in 72 Java files**.
It also references four Taniwha-owned data capabilities: structure processor
types `replace_selection` and `suspicious_block_replacement`, trade listing
type `standard`, and loot-table type `generic_entity`.

The 59 APIs/capabilities collapse into 31 replacement groups:

| Classification | Groups |
|---|---:|
| VANILLA-REPLACEMENT | 9 |
| FABRIC-API-REPLACEMENT | 2 |
| SMALL-LOCAL-UTILITY | 5 |
| NO-LONGER-NEEDED | 3 |
| DEFER-TO-FEATURE-STAGE | 11 |
| UNRESOLVED | 1 |
| **Total** | **31** |

## B. Historical BM call sites and classification

Every imported API is named in this table. Feature-stage deferral means the
replacement strategy is identified but no behavior is implemented in Stage 1.

| Taniwha API(s) | Files / lexical uses | BM call sites and purpose | Observable effect | Classification / modern strategy |
|---|---:|---|---|---|
| `block.BlockHelper`, `item.ItemHelper` | 4 / 6 | `BMBlocks`, `BMItems`, main bootstrap; register blocks/items and paired block items | Persistent IDs and inventory/block identity | SMALL-LOCAL-UTILITY: `BMRegistryUtil` implements only typed vanilla registration |
| `block.DecorationBlockFactory` | 1 / 36 | `BMBlocks`; produces slab/stair/wall sets | Exact family IDs, shapes and recipes/resources | SMALL-LOCAL-UTILITY: explicit `BlockFamilyPlan`; actual members/properties deferred to Stage 2 |
| `block.WoodBlockFactory` | 3 / 18 | `BMBlocks`, `BMBoats`, `MansionFeature`; wood sets and boat association | Wood IDs, stripping, fuel/fire, signs/boats and mansion palette | SMALL-LOCAL-UTILITY: explicit membership plan now; behavior deferred to owning stages |
| `block.BlockSetHolder`, `block.WoodTypeHolder` | 1 / 3 | `PoltergeistHandler`; identify material families | Poltergeist block transformations | DEFER-TO-FEATURE-STAGE 9/12; local lookup keyed by vanilla block sets if needed |
| `block.FlammabilityRegistry` | 1 import / no direct lexical call | `BMBlocks`; imported alongside modifiers | Burn/spread behavior | FABRIC-API-REPLACEMENT: Fabric flammability API or vanilla properties in owning family stage |
| `block.modifier.BlockModifier`, `BlockWithModifiers`, `FlammableModifier`, `RTypeModifier`, `registry.ModifierContainer`, `block.rtype.RType` | 22 file-imports / 95 uses | `BMBlocks` and specialized flora/cluster/lily classes; attach render/fire modifiers | Cutout rendering, flammability and extension hooks | VANILLA-REPLACEMENT: explicit block properties, client render layers and Fabric fire hooks; defer per block |
| `block.TBlockExtension` | 3 / 3 | Black Thistle and lily pads | Taniwha modifier application | NO-LONGER-NEEDED: use direct vanilla overrides/properties; preserve feature behavior in owning stage |
| `block.types.TBlock`, `TCropBlock`, `TFarmBlock`, `TMushroomPlantBlock`, `TSaplingBlock`, `TTallFlowerBlock` | 18 / 18 | Functional blocks, crops, mushrooms, saplings and flowers | Base vanilla block behavior with Taniwha conveniences | VANILLA-REPLACEMENT: `Block`, `CropBlock`, `FarmBlock`, mushroom/sapling/tall-flower equivalents; defer constructors/behavior |
| `block.types.*` | 1 wildcard | `BMBlocks` | Imports Taniwha block subclasses including leaves/flower-pot helpers | NO-LONGER-NEEDED as a wildcard; identify each vanilla replacement when restoring its block |
| `block.DripstoneReceiver` | 1 / 1 | `ComposterBlockMixin` | Dripstone fills special composters | DEFER-TO-FEATURE-STAGE 9: local interface only if released behavior requires it |
| `hooks.block.entity.BlockEntityHooks` | 1 / 1 | `BMBlocks`; adds Suspicious Red Sand to Brushable Block entity | Archaeology block-entity compatibility | VANILLA-REPLACEMENT if 1.21.10 builder/registry supports it; otherwise a narrow mixin in Stage 10C |
| `util.BlockUtil` | 1 / 2 | `BMBlocks`; copies block properties | Family physical behavior | VANILLA-REPLACEMENT: `BlockBehaviour.Properties`/copy semantics, verified per family |
| `client.color.*`, `client.model.RenderLayerInjector` | 2 / 1 | `BiomeMakeoverClient`; colors and cutout layers | Tinting/transparency | FABRIC-API-REPLACEMENT: vanilla/Fabric color and render-layer registration; defer per client asset |
| `entity.TEntityTypeBuilder`, `mixin.spawn.SpawnPlacementsInvoker` | 2 / 33 | `BMEntities`; 21 types and 12 spawn placements | Entity dimensions/tracking/spawn predicates | VANILLA-REPLACEMENT: `EntityType.Builder` and public Fabric/vanilla spawn APIs in entity stages |
| `entity.ai.TagTemptGoal` | 1 / 1 | `ScuttlerEntity` | Tag-driven temptation | SMALL-LOCAL-UTILITY only if vanilla ingredient-based goal cannot match; defer Stage 4 |
| `entity.golem.GolemHandler`, `PlayerCreatable` | 3 / 5 | `BMEntities`, Stone Golem, Adjudicator | Golem construction and player-creator attribution | DEFER-TO-FEATURE-STAGE 12; vanilla pattern spawning plus small owner contract if required |
| `entity.effect.TMobEffect`, `mixin.brewing.PotionBrewingInvoker` | 3 / 6 | `BMPotions`, `PossessedEffect` | Effects and brewing recipes | VANILLA-REPLACEMENT: `MobEffect` and modern brewing registration; defer Stage 2/12 |
| `item.ArmorBuilder`, `item.totem.TotemItem`, `item.types.TItem`, `TItemNameBlockItem` | 5 / 21 | `BMItems`, Ectoplasm, Enchanted Totem | Items, armor attributes, totem activation, translation behavior | DEFER-TO-FEATURE-STAGE 2/12; vanilla item/components and explicit event behavior |
| `item.types.FakeItem` | 2 / 1 | icon/registration sequencing | Hidden bootstrap sentinel, no released gameplay | NO-LONGER-NEEDED: explicit initialization order replaces sentinel |
| `hooks.PotteryPatternHooks` | 1 / 4 | `BMItems`; registers four pottery patterns | Decorated-pot archaeology output | DEFER-TO-FEATURE-STAGE 4/10C; use modern decorated-pot registries/data components |
| `data.criterion.SimpleCriterion` | 1 / 14 | `BMAdvancements`; custom triggers | Advancement progression | DEFER-TO-FEATURE-STAGE 13 or owning feature stage; small local triggers over vanilla API |
| `data.trade.TradeList`, `TradeLists`, `listing.TItemListing`, `TradeTypes`; data type `taniwha:standard` | 6 Java imports / 12 uses plus 24+ JSON listings | Mushroom Trader and suspicious-stew trade codec | Data-driven trades | DEFER-TO-FEATURE-STAGE; Mushroom Trader is unreachable released content, stew/quest reachability handled with vanilla trade listings |
| `util.collections.WeightedList` | 4 / 13 | Witch quest categories/rewards/rarities | Weighted quest selection | SMALL-LOCAL-UTILITY in Stage 12; deterministic weighted selection with historical edge cases tested |
| `util.collections.Grid` | 10 / 16 | Mansion layout, rooms and processors | Mansion topology and room lookup | DEFER-TO-FEATURE-STAGE 11; local bounded grid after exact semantics are derived |
| `util.MathUtils` | 11 / 21 | Toad, renderer animation, mansion, ivy, blossom, enchantment, bookshelves | Random choice, interpolation, geometry and selection | VANILLA-REPLACEMENT where `Mth`/`RandomSource` match; otherwise tiny feature-local functions after call-site audit |
| `util.HorizontalDirection` | 3 / 10 | Ancient Oak/Cypress trunk placers and Fang Barrage | Horizontal iteration/offsets | VANILLA-REPLACEMENT: `Direction.Plane.HORIZONTAL`, preserving ordering where observable |
| `util.EntityUtil`, `util.ItemUtil` | 9 file-imports / 8 uses | Rootling, loot mixins, witch quests, Scuttler, Adjudicator, enchantment, Stunt Powder | Entity lookup, equipment/drop and item helpers | DEFER-TO-FEATURE-STAGE; use vanilla operations after each call is behaviorally specified |
| `util.TaniwhaTags` | 1 / 1 | `RootlingCropBlock` | Shared plantable/soil semantics | VANILLA-REPLACEMENT: project/vanilla tag declared in owning stage |
| `level.structure.IgnoreAirProcessor` | 1 / 1 | Surface Fossil feature | Template placement ignores air | VANILLA-REPLACEMENT: vanilla structure processor/settings if equivalent; defer Stage 4 |
| `data trade taniwha:standard`, `loot taniwha:generic_entity` | resource-only | Mushroom Trader list and six Rootling gameplay loot tables | Trade decoding and variant loot | DEFER-TO-FEATURE-STAGE; trader excluded unless reachable, Rootling loot replaced with modern vanilla loot behavior |
| processor `taniwha:replace_selection` | one processor list | Mushroom House | Random/template palette substitution | DEFER-TO-FEATURE-STAGE 10A; local processor only after exact codec/selection semantics are proven |
| processor `taniwha:suspicious_block_replacement` | two processor lists | Ghost Town roads/buildings | Archaeology block replacement and loot assignment | UNRESOLVED until Taniwha binary/runtime behavior is inspected; blocks Stage 10C portion, not Stage 1 |

## C. Required observable behavior

The dependency is relevant only where it changes released results: stable
registry IDs, exact generated family membership, physical/render/fire block
properties, spawn placement, boat identity, weighted selection, structure
layout/processing, trade decoding, special loot selection, advancement
triggers and functional interactions. Class hierarchy and helper naming are
not parity requirements.

## D. Replacement rules

Replacement order is vanilla, then Fabric API, then the smallest local
utility. A utility must have a cited historical call site, deterministic tests
and no implicit member generation. A feature stage owns gameplay properties;
Stage 1 owns only safe mechanics for ID planning/registration and validation.

## E. Implemented Stage 1 replacements

- `BMRegistryUtil`: typed vanilla item registration and paired block/item
  registration, with namespace/path validation and no creative-tab or behavior
  policy. It replaces the narrow `BlockHelper`/`ItemHelper` plumbing.
- `BlockFamilyPlan`: sorted explicit family membership with duplicate/path
  rejection. It replaces only the ID-planning aspect of WoodBlockFactory and
  DecorationBlockFactory; it does not create blocks.
- `family_membership.json`: deterministic current-family contract for Ancient
  Oak, Mesmerite and Polished Mesmerite. It registers nothing.
- `production_dependency_contract.json`: freezes dependency versions/runtime
  dependency IDs, forbids Taniwha, and prevents new legacy plural tag paths.

Existing registrations were intentionally not migrated to these helpers.

## F. Deferred feature-specific replacements

- Stage 2: exact block/item families, effects, armor/items and family data.
- Stages 3–6: plant classes, fire/render properties, tree directions,
  processors and biome-specific tags.
- Entity stages: builders, spawn placement, tag temptation and entity helpers.
- Stage 9: composter dripstone and functional block hooks.
- Stage 10: archaeology and structure processors.
- Stage 11: mansion Grid and layout semantics.
- Stage 12: weighted quests, golem/creator behavior, totem/curse helpers.
- Stage 13/owning stages: criteria and integration gap closure.

## G. Unresolved dependencies

`taniwha:suspicious_block_replacement` is unresolved. Its exact codec,
randomness, suspicious-block data and loot assignment must be derived from the
5.4.4 binary or a historical runtime before Stage 10C. No speculative local
processor was created.

Exact WoodBlockFactory/DecorationBlockFactory generated membership and some
modifier defaults also require the historical registry/tag dump mandated by
Stage 0 before Stage 2 registration.

## H. Explicitly unnecessary Taniwha functionality

No wholesale deferred registry framework, platform abstraction, wildcard
block hierarchy, fake bootstrap item, generic modifier framework, Taniwha boat
framework, trade framework, or runtime dependency is required in Stage 1.
Architectury replacement is outside this Taniwha map; the current port already
uses Fabric directly.

## I. Rules for later stages

1. Add an exact family contract before registering its members.
2. Never use an `.all()` convention unless the historical runtime dump proves
   every resulting ID.
3. Complete recipes, loot, tags, models and blockstates in the owning stage.
4. Use singular 1.21.10 tag directories (`tags/block`, `tags/item`, and the
   applicable singular registry folder). The two existing plural sapling files
   are grandfathered for regression safety and may not be copied.
5. Do not alter or remove current IDs to match a generated historical scheme.
6. Keep client-only APIs out of common/dedicated-server classloading paths.
7. Add no Taniwha dependency. Escalate any apparently unavoidable capability.
8. Preserve unresolved semantics explicitly; never approximate structure,
   loot or weighted-random behavior silently.

## Data-generation foundation

Stage 1 adopts **checked-in generated resources plus deterministic contract
manifests**. Production builds do not run a generator and require no network
or generator dependency. `validation/foundations/family_membership.json`
defines explicit sorted membership. The parity validator checks sorting,
duplicates, registration reachability, resource references and dependency
stability.

Future stages should add a family manifest first, produce ordinary checked-in
Minecraft JSON using the smallest stage-local template/script that is useful,
and run that script in check mode to prove a second run produces no diff.
Known-good hand-authored current resources are not regenerated for style.
Generated output must use stable key ordering, LF in committed content, no
timestamps or absolute paths, and IDs approved by the Stage 0 contract. The
owning stage commits the manifest, generator/template if any, generated output
and parity assertions together.

## Tag-path foundation

Static 1.21.10 evidence establishes singular registry tag directories such as
`tags/block` and `tags/item`. The validator now rejects any new legacy plural
file. The two existing plural sapling files are exact grandfathered baseline
paths: they remain untouched to avoid an untested current-content behavior
change and are reported every run. Their migration/population belongs to the
Ancient Oak restoration stage with runtime tag tests. Empty future tags are
permitted only when a released call site needs the declaration before its
members exist.

## Runtime checklist

No manual runtime test is claimed by Stage 1 unless recorded separately.

Client: launch; create and load a disposable world; place/use current blocks
and items; exercise an Owl; generate current Dark Forest chunks; inspect logs.

Dedicated server: boot; join; disconnect/rejoin; verify registry sync; check
for missing registries and client-class loading errors.

Existing-world copy: follow the Stage 0 protocol; inspect BM blocks and an
existing Owl if available; save/reload; orderly restart; compare logs and NBT.
