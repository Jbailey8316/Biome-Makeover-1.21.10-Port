# Stage 9B.2 — Complete Altar Restoration

Status: **COMPLETE / RUNTIME ACCEPTED**. Stage 9B.1 remains **COMPLETE / RUNTIME ACCEPTED**. Stage 9B is complete; Stage 10+ is not started.

Known minor parity deviation: Altar waterlogging is deferred and remains nonfunctional at runtime. Its state/fluid translation is retained, but the unsuccessful interaction remediation is not treated as runtime acceptance and does not block this stage.

## Authority and reachability

The authority is final Biome Makeover 1.20.1 (`1.20.1-1.11.4`, source commit `2f314c0596af095a4890995a465f308f69476b4a`). The active final chain is:

`Book + 2 Illunite Shards + 2 Crying Obsidian + 2 Mesmerite` → `biomemakeover:altar` → two-slot processing with one Illunite Shard → cursed Book or upgraded/cursed ordinary item.

The shaped pattern is exactly ` B ` / `ICI` / `MCM`. The visible block item and the advancement `biomemakeover:biomemakeover/altar` make the system normally reachable. It consumes no XP and offers no player-selected outcome.

## Final block and inventory contract

- `biomemakeover:altar` is a strength-5, correct-tool, pickaxe-mineable, bass-drum, black-map-color block and normal block item.
- The released three-part pedestal collision/outline shape, no-occlusion cutout rendering, `ACTIVE` and `WATERLOGGED` states are retained. Light is 1 while inactive and 5 while active. Actual player waterlogging remains a known deferred runtime deviation.
- The Altar block entity has exactly two logical slots: slot 0 target, slot 1 `#biomemakeover:curse_fuel`. The final tag contains only `biomemakeover:illunite_shard`.
- Processing is server-authoritative and takes exactly 300 ticks. Inventory and `Progress` persist. Removing or invalidating either required input resets progress to zero. Chunk unload/reload and server restart retain valid in-progress state.
- A successful or failed completed attempt consumes one fuel. A failed curse search ejects the original target, empties slot 0, and does not refund fuel.
- Vertical faces expose target slot 0; horizontal faces expose fuel slot 1. Final 1.20.1 made every exposed slot immediately extractable and had no processing lock. The explicitly authorized **MYTHAS ENHANCEMENT - ALTAR AUTOMATION LOCKING** changes only automated extraction: a processable target cannot be pulled, its required fuel cannot be pulled while that target is pending/processing, and completed outputs or invalid garbage can be pulled. Manual slot interaction remains unchanged.
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
- **Waterlogging remains deferred.** The state/fluid contract and a held-bucket interaction translation are present, but targeted Prism retesting still could not waterlog the Altar. No further production attempt is made during closure. This is documented as a minor, accepted parity deviation that may be revisited in a future Minecraft migration, including 26.2.
- **Altar audio startup was defective.** Registration, JSON, mono OGG, sound instance, and client registration were present, but the port had replaced final source's explicit server effect with a passive client observation of ACTIVE. Prism proved that observation never instantiated an audible sound. The authoritative start edge is restored through one server block event per cycle, matching the original event semantics without packet spam.
- **Immediate hopper extraction is original behavior.** Final 1.20.1 exposes slot 0 on vertical faces and slot 1 on horizontal faces, and `canTakeItemThroughFace` returns true without consulting validity, progress, or ACTIVE. An output hopper can therefore remove pending input immediately. That baseline is documented as parity, not a defect.

## MYTHAS ENHANCEMENT - Altar automation locking

This is intentionally not final-source parity. Automated extraction now follows a Brewing-Stand-style lifecycle without changing the two source-owned slots or face routing:

- A processable plain Book or eligible enchanted item in target slot 0 is not hopper-extractable, whether awaiting fuel or actively processing.
- A completed Enchanted Book or marked ordinary-item result is no longer a valid Altar target and is hopper-extractable.
- An invalid item in target slot 0 remains hopper-extractable, avoiding an automation deadlock.
- Real Illunite fuel is not hopper-extractable while a valid target is pending/processing. Fuel becomes extractable when there is no valid target; invalid garbage in the fuel slot is always extractable.
- Player menu removal is unchanged because the rule is confined to `canTakeItemThroughFace`.
- The final failed-selection path remains unchanged: the original target is popped into the world, slot 0 clears, and one fuel is consumed.

## Static validation

The Stage 9B.2 foundation contract and parity validator assert registrations, exact recipe, two logical slots, 300 ticks, persistence, sided faces, fuel tag, book/item component separation, marker/repair cost, 100-attempt fallback, exclusive curse levels, required assets/data, client-only registration and absence of later-stage leakage. Static checks cannot prove random outcome distribution, audiovisual timing, hopper timing, or menu behavior in a live integrated/dedicated server.

## Runtime acceptance

Targeted Prism testing accepted the complete reachable Altar loop: source-correct manual recipe discovery and crafting, block/model/menu, plain-Book cursing, 300-tick processing, one Illunite consumption, level-I stored curse output, ordinary enchanted-item upgrading including over-maximum levels, compatible random curse application, repeat-Altar marker rejection, grindstone curse/marker persistence, Enchanted Book input rejection, save/reload, interruption/reset, inventory spills, comparator output, floating book, particles and animation.

The remediated positional processing sound is now audible as a soft cursing hum with completion feedback. The explicit survival `BMCursed` anvil restriction prevents later enchantment application as intended. The separately classified Mythas automation lock passed with hoppers retaining valid input until processing and extracting the completed result.

Waterlogging did not pass and is not claimed. Stage 9B.2 is accepted with that one documented minor deviation.
