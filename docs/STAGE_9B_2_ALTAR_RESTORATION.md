# Stage 9B.2 — Complete Altar Restoration

Status: **REMEDIATED / AWAITING TARGETED RUNTIME RETEST**. Stage 9B.1 remains **COMPLETE / RUNTIME ACCEPTED**. Stage 10+ is not started.

## Authority and reachability

The authority is final Biome Makeover 1.20.1 (`1.20.1-1.11.4`, source commit `2f314c0596af095a4890995a465f308f69476b4a`). The active final chain is:

`Book + 2 Illunite Shards + 2 Crying Obsidian + 2 Mesmerite` → `biomemakeover:altar` → two-slot processing with one Illunite Shard → cursed Book or upgraded/cursed ordinary item.

The shaped pattern is exactly ` B ` / `ICI` / `MCM`. The visible block item and the advancement `biomemakeover:biomemakeover/altar` make the system normally reachable. It consumes no XP and offers no player-selected outcome.

## Final block and inventory contract

- `biomemakeover:altar` is a strength-5, correct-tool, pickaxe-mineable, bass-drum, black-map-color block and normal block item.
- The released three-part pedestal collision/outline shape, no-occlusion cutout rendering, `ACTIVE` and `WATERLOGGED` states are retained. Light is 1 while inactive and 5 while active.
- The Altar block entity has exactly two logical slots: slot 0 target, slot 1 `#biomemakeover:curse_fuel`. The final tag contains only `biomemakeover:illunite_shard`.
- Processing is server-authoritative and takes exactly 300 ticks. Inventory and `Progress` persist. Removing or invalidating either required input resets progress to zero. Chunk unload/reload and server restart retain valid in-progress state.
- A successful or failed completed attempt consumes one fuel. A failed curse search ejects the original target, empties slot 0, and does not refund fuel.
- Vertical faces expose target slot 0; horizontal faces expose fuel slot 1. Exposed slots remain extractable, matching the released `WorldlyContainer` contract. There is no processing lock.
- Comparator output is vanilla two-slot container-fullness output; processing progress does not create a separate comparator scale.
- Breaking spills contents once through the modern `BlockEntity.preRemoveSideEffects` container hook. The block only performs the vanilla post-destroy neighbor/comparator update, avoiding a duplicate historical manual drop. The released explosion-conditioned loot table returns the Altar block item.

## Target and output contract

A plain Book is accepted and becomes an Enchanted Book containing exactly one randomly chosen allowed curse at level I in `STORED_ENCHANTMENTS`. It is not marked `BMCursed`.

An ordinary item is accepted only when it is not an Enchanted Book, is unmarked, already has enchantments, and contains an eligible non-curse enchantment whose definition has a maximum level above one and is not tagged `altar_cant_upgrade`. One eligible enchantment is selected uniformly and incremented by exactly one without maximum-level clamping. One absent supported curse is then added, the output is marked `BMCursed`, and its repair cost becomes 39.

`BMCursed` is retained as the private historical boolean name inside modern `CUSTOM_DATA`. It is separate from “contains a curse.” It survives ordinary ItemStack serialization, world/container storage, drop and pickup. A plain cursed book is deliberately unmarked. Grindstone processing preserves curse components and the base stack's custom marker.

Final 1.20.1 relied on repair cost 39 plus vanilla's survival cost ceiling: every non-rename operation added at least one level and therefore had no output at cost 40 or above, while rename-only was capped to 39 and creative retained its vanilla bypass. The modern port now enforces that same effective result directly for `BMCursed` inputs so an XP-limit-relaxing mod cannot reopen enchantment transfer, upgrades, material repair, or durability combination. Rename-only and creative behavior remain source-correct; ordinary items are untouched. No cleansing was added.

## Curse selection and source quirks

The pool is the active enchantment registry's curse tag minus `biomemakeover:altar_curse_excluded`. This includes all ten accepted BM curses plus compatible vanilla Binding/Vanishing and remains extensible in the same registry-driven way as final source. Sliding Curse is absent.

For ordinary items, an initial random curse is retried up to 100 times while it is already present or unsupported. If those tries fail, the final brute-force phase walks a randomized registry order and retains the last compatible absent curse, matching the released helper's observable selection structure. If none exists, processing fails with the ejection/fuel-loss behavior above. The two empty final extension tags (`altar_curse_excluded`, `altar_cant_upgrade`) are packaged.

The released random-range upper bound is exclusive and begins at the selected definition's actual minimum. Final BM max-5 curses therefore produce I–IV and max-3 curses I–II on ordinary items. A curse whose minimum equals its maximum (including vanilla max-level-1 curses) receives that sole level without an invalid random bound. Books always receive I.

The historical `strictAltarCursing` default is false, so over-maximum upgrades such as Sharpness V → VI remain eligible. The port preserves that released default rather than introducing a new config/registry mutation system.

## 1.21.10 architecture

- Block/entity/menu use native 1.21.10 `EntityBlock`, `RandomizableContainerBlockEntity`, `WorldlyContainer`, `MenuType`, `ContainerData`, fluid-state, and item-component contracts.
- Enchantments resolve from the current server `RegistryAccess`; no holders or registry contents are cached across reloads.
- Normal items use `DataComponents.ENCHANTMENTS`; cursed books use `STORED_ENCHANTMENTS`; the marker uses `CUSTOM_DATA`; repair economics use `REPAIR_COST`.
- The final menu layout has target/fuel slots, player inventory and progress glyph only. There is no curse selection or XP control.
- The floating vanilla enchanting-table book is rendered through the modern block-entity render-state/model pipeline, including the released client animation-counter cadence. The original GUI, five block textures, models and blockstate are reused.
- The server now reproduces the historical one-shot S2C start-effect edge with a vanilla block-event packet. The client starts one positional Altar sound instance only when that authoritative event arrives; ACTIVE state continues to drive visuals and stop conditions. No custom payload or C2S gameplay path exists. The sound stops on interruption or block-entity invalidation and naturally ends with the processing-length asset.
- Client screen, renderer and sound classes remain exclusively in the client source set; common/server code contains no client class reference.

## Runtime remediation findings

- **Recipe-book discovery is final-source parity.** Final 1.20.1 packages the shaped recipe and the visible obtain-Altar advancement, but no `advancements/recipes/...` unlock resource and no generated unlock criterion. The recipe is therefore not made visible by obtaining Book, Illunite, Mesmerite, or Crying Obsidian. Successful manual crafting teaches it through vanilla recipe-award behavior. No premature unlock was added.
- **Waterlogging interaction was defective.** The block state and fluid methods existed, but 1.21.10 routes held-item interaction through `useItemOn` before the empty-hand menu hook. The menu consumed the interaction before `BucketItem` could invoke `SimpleWaterloggedBlock`. Water Bucket and empty Bucket interactions now pass to the native fluid-item path; other held items still open the menu, and no non-water fluid is accepted.
- **Altar audio startup was defective.** Registration, JSON, mono OGG, sound instance, and client registration were present, but the port had replaced final source's explicit server effect with a passive client observation of ACTIVE. Prism proved that observation never instantiated an audible sound. The authoritative start edge is restored through one server block event per cycle, matching the original event semantics without packet spam.
- **Immediate hopper extraction is original behavior.** Final 1.20.1 exposes slot 0 on vertical faces and slot 1 on horizontal faces, and `canTakeItemThroughFace` returns true without consulting validity, progress, or ACTIVE. An output hopper can therefore remove pending input immediately. That baseline is documented as parity, not a defect.

## Static validation

The Stage 9B.2 foundation contract and parity validator assert registrations, exact recipe, two logical slots, 300 ticks, persistence, sided faces, fuel tag, book/item component separation, marker/repair cost, 100-attempt fallback, exclusive curse levels, required assets/data, client-only registration and absence of later-stage leakage. Static checks cannot prove random outcome distribution, audiovisual timing, hopper timing, or menu behavior in a live integrated/dedicated server.

## Focused Prism acceptance

1. Craft the Altar with ` B ` / `ICI` / `MCM`; confirm the Altar advancement, placement, waterlogging, breaking/drop, UI and two slots.
2. Process a plain Book plus one Illunite Shard for 300 ticks. Confirm one level-I curse in an Enchanted Book, one fuel consumed and no XP cost.
3. Process an eligible ordinary item such as a Sharpness V sword. Confirm one existing eligible enchantment rises by one (Sharpness VI when selected), a compatible absent curse is added, repair cost behavior is visible, and direct repeat Altar processing is rejected.
4. Reject an unenchanted item, Enchanted Book, marked output and item with no upgradeable non-curse enchantment. Verify no invalid curse appears on incompatible gear.
5. Save/reload during processing; confirm inventory and progress resume. Remove target/fuel mid-cycle and confirm progress resets.
6. Test vertical target insertion/extraction, horizontal fuel insertion/extraction and invalid automation input. Compare comparator output for empty, one slot, both slots and larger fuel stacks.
7. During processing verify light 5, particles, floating animated book, progress glyph and positional sound. Confirm sound stops after completion, interruption, breaking and unload.
8. Smoke-test a cursed book through an anvil, a cursed item through a grindstone, item save/drop/container persistence, and representative Stage 9B.1 curse behavior. A dedicated-server join is recommended for final sidedness/authority acceptance.

No Altar runtime acceptance is claimed until this candidate passes Prism.
