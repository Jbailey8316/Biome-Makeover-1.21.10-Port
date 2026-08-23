# Stage 5 Runtime Remediation 3

## Evidence baseline

This checkpoint starts at `f4692d2833a2e5a34730ff9d194cf4dd61ba66bf`. Released BM
1.20.1-1.11.4 (`2f314c0596af095a4890995a465f308f69476b4a`) remains the behavior source.
The released block registration and leaf loot resources were compared with the packaged port and Minecraft
1.21.10's `PlaceOnWaterBlockItem` and canonical Oak Leaves table.

## Runtime observation disposition

| Area | Disposition | Evidence and action |
|---|---|---|
| A. Willow natural-decay drops | FIXED STATICALLY; RUNTIME VERIFICATION REQUIRED | Released Willow Leaves use the vanilla leaf contract: Shears/Silk Touch return leaves; otherwise the alternatives entry gives a Willow Sapling at 5%, 6.25%, 8.333%, or 10% with Fortune, plus the historical stick pool. The packaged table retained 1.20 item/enchantment predicates. It is now deterministically translated to 1.21.10 scalar `items` and component-based enchantment predicates. No drop was suppressed. |
| B. Cypress natural-decay drops | FIXED STATICALLY; RUNTIME VERIFICATION REQUIRED | The released Cypress table is source-identical except for leaf/sapling IDs and uses the same chances. It receives the same schema translation. Shears, Silk Touch, Fortune, sapling, and stick behavior remain intact. |
| C. Small Lily Pad placement | FIXED STATICALLY; RUNTIME VERIFICATION REQUIRED | Released `registerLilyPad` created a `PlaceOnWaterBlockItem`; Stage 5 accidentally used the generic `BlockItem`. The historical source-water targeting item contract is restored. Its released 1-4 pad block state and same-block stacking interaction remain unchanged; no Mythas redesign was added. |
| D. Water Lily placement | FIXED STATICALLY; RUNTIME VERIFICATION REQUIRED | The same released registration helper supplied `PlaceOnWaterBlockItem`. Restoring it permits the item to target valid source water while `WaterlilyBlock` retains survival/update checks. Tint, artwork, worldgen, Magenta Bud, and recipes are unchanged. |
| E. Willow/Cypress leaf item tint | FIXED STATICALLY; RUNTIME VERIFICATION REQUIRED | The first translation put a constant tint on a model routed through the legacy item-model parent. Vanilla 1.21.10 foliage definitions tint the block model directly. Willow now uses the historical no-world foliage color `-12012264`; Cypress uses its historical temperature-gradient default `-8082577`. World block color providers were not changed. |
| F. Straight terrain boundaries | NOT ATTRIBUTED TO BM / CONTROLLED COMPARISON REQUIRED | Stage 5 registers no biome, noise setting, density function, terrain router, surface rule, or chunk blending logic. `PeatFeature` is line-for-line equivalent to released code apart from registry-wrapper removal and performs small local top-layer replacement. Vegetation injection and vanilla-tree removal cannot create broad terrain-height discontinuities. No worldgen change was made; use a same-seed, same-build fresh-world comparison if this recurs. |
| G. Performance | PROVISIONALLY FIXED; FINAL SOAK REQUIRED | The prior missing-log-tag regression explained canopy-wide distance updates, decay, loot, and ItemEntity load. The latest Prism result no longer shows the item storm or progressive Swamp lag. No BM exception loop or new runaway Stage 5 tick path is evident. No cleanup/performance mechanism was introduced. Host-memory warnings remain non-causal without evidence. |
| H. Dragonfly survival | STATIC CONTRACT RETAINED; RUNTIME VERIFICATION REQUIRED | `FlyingAnimal`, no-fall behavior, 10 health, released water/lava/fire maluses, and spawn predicate remain intact. No new reported death or concrete static defect exists, so no speculative immunity or behavior change was made. |
| Lightning Bug | SHOWCASE/SOURCE-VERIFIED; UNCHANGED | Cube geometry, layered pulse/color, sparks, and brightness are accepted released behavior. No Lightning Bug production file changed. |
| Willow bee nests | SOURCE-CONFIRMED; UNCHANGED | Low eligible Willow trunk positions can produce low nests under the released 2% vanilla-style decorator. No aesthetic minimum-height rule was added. |

## Validation additions

- Both packaged leaf tables must use the 1.21.10 scalar Shears predicate and component-based Silk Touch predicate and must not retain the identified legacy forms.
- Both leaf item definitions must tint their block model directly with the source-established no-world color.
- Both released lily blocks must be registered through the water-targeting item contract.

These checks complement runtime testing; they do not claim that drop probabilities, placement interaction, or item
rendering have passed Prism.

## Registry, package, and world safety

No ID was added, removed, renamed, or repurposed. The block/item/entity/sound/particle counts remain
246/246/9/25/1. The changes affect item class construction for two existing IDs and deterministic packaged data/assets.
Existing placed blocks and serialized items keep their IDs. No retro-generation or existing-chunk mutation is added.
No Taniwha, Stage 6, Stage 9/10, Lightning Bug, Owl, Cowboy, Badlands, or Mushroom Fields code is included.

## Comprehensive Prism acceptance checklist

1. Generate fresh Swamp and Mangrove Swamp terrain; inspect Willow/Cypress trees, persistent canopies, peat/mossy peat,
   reeds/cattails/flowers, pads/lilies, bonemeal ecology, low-but-attached Willow bee nests, and any straight terrain edges.
2. Remove supporting logs from multiple Willow and Cypress trees. Confirm natural decay produces no leaf block items,
   can produce the correct saplings/sticks, and does not create an item storm. Check normal break, Shears, Silk Touch,
   and Fortune separately.
3. Place and grow both saplings. Exercise Willow/Cypress wood-family blocks, signs, doors/trapdoors, fences, recipes,
   decorative families, peat tilling, and Willowing Branch growth/shearing.
4. Place Small Lily Pads and Water Lilies on valid source water; verify invalid targets fail. Exercise the released
   Small Lily Pad stacking interaction without treating it as the deferred Mythas redesign.
5. Inspect Willow and Cypress leaf items in inventory, hotbar, hand, and as dropped ItemEntities; verify world foliage
   tint remains correct.
6. Observe adult/baby Decayed, water seeking/breathing, loot, Dragonfly movement/survival over time, and the accepted
   original Lightning Bug visuals/particles. Save/reload entities.
7. Trigger Faster Farmland by tilling peat and confirm a Decayed spawn egg does not trigger it. Verify Water Lily and
   Magenta Bud recipes and that no deferred advancement loads.
8. Remain in a Willow-heavy Swamp for several minutes, monitor item entities and tick stability, then save/reload,
   leave/re-enter, and restart. Boot/join/rejoin a dedicated server if available.
9. On a copy only, load the Mythas world, inspect existing BM content, generate fresh Swamp/Mangrove chunks, then
   save/restart and check registry/data diagnostics.

Stage 5 remains runtime-open until this checklist is executed.
