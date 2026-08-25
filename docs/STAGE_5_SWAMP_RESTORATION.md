# Stage 5 — Swamp Ecosystem Restoration

## A. Authority and scope

Released behavior is derived from `Lemonszz/Biome-Makeover`, branch `1.20`, commit
`2f314c0596af095a4890995a465f308f69476b4a` (BM 1.20.1-1.11.4). Generated-family
membership follows Taniwha 1.20.0-5.4.4 at
`ee029d785850d8b0ad8ba69bee4e069b03253afe`. This stage was reviewed against
`PORTING_ENGINEERING_RULES.md` before implementation.

The deterministic ownership contract is
`validation/foundations/stage_5_swamp_contract.json`. It is the authoritative
machine-readable list for this checkpoint.

## B. Released and reachable Stage 5 manifest

- Willow and Swamp Cypress wood families: log, stripped log, planks, wood,
  stripped wood, slab, stairs, fence, gate, pressure plate, button, trapdoor,
  door, sign/wall sign, hanging/wall hanging sign. Leaves and saplings remain
  separate, as required by the verified family contract.
- Fungal/wetland ecology: Willowing Branches, Buttonbush, Marigold, Cattail,
  Reed, Reed Thatch/slab/stairs, Small Lily Pad, and Water Lily.
- Peat system: Peat, Dried Peat, Mossy Peat, Peat Farmland, and the exact
  base/slab/stairs/wall memberships for Dried, Mossy Dried, and Cracked Dried
  Peat Bricks. Peat and Mossy Peat till into Peat Farmland; hydrated farmland
  gives the crop above its released additional random tick.
- Reachable entities: Decayed, Dragonfly, Lightning Bug, and the historically
  registered alternate Lightning Bug form. Natural spawns are injected with
  released weights/groups (Decayed 60/1–1, Dragonfly 20/3–8, Lightning Bug
  20/1–1). The alternate form remains non-natural.
- Worldgen: flowers, pads, peat, reeds, Willow and Swamp Cypress configured and
  placed features, tree decorators/placers, removal of vanilla swamp trees, and
  the seven released additions to swamp vegetation/top-layer generation.
- Swamp water bonemeal ecology is restored through a server-side bonemeal hook.
- Eleven released Decayed/Dragonfly sound IDs, entity renderers/models,
  translucent/cutout block layers, recipes, loot, tags, models, textures,
  translations, item definitions, and the Swamp root-advancement criterion.

## C. Reachability and deferral decisions

- `toad`, `tadpole`, their foods/bucket/eggs: released registrations existed,
  but acquisition/spawning was disabled or unreachable. They remain excluded.
- Lightning Bug Bottle and Peat Composter: functional block/item interaction
  chain is owned by Stage 9; no placeholder was registered.
- Sunken Ruins and Swamp Jives: Stage 10B structure ownership.
- Witch Hat and Witch quest: later mansion/progression ownership. No incomplete
  wearable was pulled into Stage 5.
- Willow/Cypress boat and chest-boat IDs: still deferred until the historical
  BM/Taniwha boat entity/type infrastructure is restored; vanilla substitutes
  would not be faithful.
- No dev-only content was activated.

## D. Historical behavior and 1.21.10 translations

- Exact Taniwha family suffixes were translated through the local Stage 1
  helpers; no Taniwha runtime dependency was introduced.
- Historical tree placers and decorators use modern `MapCodec` registration.
  RNG bounds, call order, water placement, hanging leaves, and Willowing Branch
  decorators follow the released algorithms.
- Recipe/item-definition/spawn-egg and configured-feature resources are emitted
  in the 1.21.10 schema while retaining historical IDs and balance.
- Restored flying mobs use modern flying navigation and render-state transfer.
  Required modern attributes are registered up front, including temptation
  range where a goal contract needs it; this avoids the Stage 4 failure mode.
- Peat tilling is a narrow Hoe `useOn` compatibility hook which mirrors vanilla
  1.21.10 sound, air-above predicate, durability, and success semantics.
- The temporary `/bmtest cowboy_patrol` command was removed. Its production
  patrol replacement/finalization path was not changed.

## E. Registry and existing-world safety

Stage 5 is additive: no existing ID was removed, renamed, or repurposed. Added
worldgen affects newly generated Swamp/Mangrove Swamp chunks only; there is no
retro-generation or automatic mutation of existing chunks. Runtime block
interactions apply normally in already-generated chunks where restored blocks
are placed. Existing Owl and accepted Stage 3/4 IDs and serialization remain
unchanged.

The one-pixel leader-horse ear extension remains an explicitly approved Mythas
render-only enhancement. It is not released parity and was not altered here.

## F. Static validation

The validator now consumes the deterministic Stage 5 contract, verifies exact
family membership, expected IDs, configured/placed features, sounds, biome
injection, resource references, and absence of the temporary Cowboy command.
Static and packaged results are recorded in the Stage 5 completion commit/report;
they are not runtime evidence.

## G. Known runtime risks and required Prism checks

Manual runtime verification remains required for custom tree codecs/shape,
waterlogged flora placement, peat spread/farmland behavior, mob animation and
render state, Decayed shield behavior, Lightning Bug illumination/variant state,
entity sound cadence, save/reload, and dedicated-server sided loading.

Focused Prism checklist:

1. Create a new test world and use `/locate biome minecraft:swamp`; generate
   fresh terrain and inspect Willow/Cypress density, peat, reeds, pads, flowers,
   mushrooms, and removal of vanilla swamp trees.
2. Place/break/craft each family; verify doors/trapdoors, signs, fences/walls,
   transparent layers, peat hoe-tilling, crop growth, and mature branch shearing.
3. Bonemeal shallow Swamp water and verify the released pad/flower ecology.
4. Observe natural Decayed, Dragonflies, and Lightning Bugs; also `/summon`
   each entity to inspect models, variants, AI, sounds, drops, and spawn eggs.
5. Save/reload and restart with restored blocks and all three reachable mobs
   loaded.
6. Boot a dedicated server, join/rejoin, generate fresh Swamp chunks, and check
   for client-class loading, registry synchronization, codec, or data errors.
7. Load a copy of the Mythas world, inspect existing BM/Owl content, generate
   only fresh Swamp chunks, then save/reload/restart. Never use the live world.

## H. Stage 6 entry criteria

Stage 5 must first pass review of this static checkpoint and the focused Prism
runtime checklist. Stage 6 Dark Forest work is not included in this commit.

## I. Runtime remediation 1 status

The first Prism run runtime-confirmed fresh Swamp/Mangrove generation, Willow/Cypress presence, flora, water bonemeal,
peat tilling, branch maturity/shearing, and natural entity reachability without a worldgen crash. The focused fixes and
source-confirmed no-change decisions are recorded in `STAGE_5_RUNTIME_REMEDIATION_1.md`. Stage 5 remains runtime-open
pending the targeted entity, loot/advancement, tint, save/reload, and server retests.

## J. Runtime remediation 2 status

Runtime Remediation 2 establishes the missing Willow/Cypress log-tag contract, correct Willow block/item tint paths,
and exact released Lightning Bug pulse/color/layer semantics. Willow decay, associated performance collapse,
inventory tint, and Lightning Bug visual behavior require a fresh Prism retest. Low Willow bee nests and cube-shaped
Lightning Bugs are source-confirmed released outcomes and were not redesigned. See
`STAGE_5_RUNTIME_REMEDIATION_2.md`.

## K. Runtime remediation 3 status

Runtime Remediation 3 translates the released Willow/Cypress leaf acquisition tables to the canonical 1.21.10 tool
predicate schema, restores the released water-targeting item class for Small Lily Pad and Water Lily, and routes both
custom leaf item tints directly through their block models. The accepted Lightning Bug and low Willow bee-nest
behavior remain unchanged. Terrain discontinuities are not attributed to BM without a controlled same-seed result;
Stage 5 adds no terrain-shaping registry or code. See `STAGE_5_RUNTIME_REMEDIATION_3.md` for evidence and the final
comprehensive Prism checklist. Stage 5 remains runtime-open.

## L. Runtime remediation 4 and final released-gameplay audit

The final-release audit moves the directly reachable Glass Bottle capture and Lightning Bug Bottle from Stage 9 into
Stage 5, restores waterlogged Willow/Cypress saplings, and completes Lily/Small Lily Pad item tint. Final source has
no moving-entity world-light implementation; the placed bottle emits level 15. Toad/Tadpole and Dragonfly Wings stay
excluded because 1.20.1 comments out natural acquisition, hides/disables entry items, and gives Dragonflies empty
loot. Older showcase behavior is version-evolution evidence, not merged parity. See
`STAGE_5_RUNTIME_REMEDIATION_4.md`. Stage 5 remains runtime-open.

## M. Runtime remediation 5

Runtime Remediation 5 restores the released air-origin Willow and water-origin Cypress sapling growth contracts,
restores Willowing Branch item tint, and completes the survival-reachable Bottle o' Lightning subsystem. The Mushroom
Fields underground chain was revalidated against final source but remains runtime-open because one spectator sweep is
insufficient evidence of a deterministic defect. See `STAGE_5_RUNTIME_REMEDIATION_5.md`.

The subsequent Prism acceptance run passed sapling growth, tint, foliage stability, restored Swamp ecology, and
natural Mushroom Fields underground generation. Bottle o' Lightning capture and transformations passed; its stacked
item art and particle-only entity-impact presentation are source-confirmed. The non-historical block-impact visual
bolt was removed pending one final focused confirmation.
