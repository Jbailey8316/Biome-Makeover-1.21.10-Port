# Stage 9B — Altar and Curse System Pre-Implementation Audit

Status: **AUDIT COMPLETE / IMPLEMENTATION NOT STARTED**  
Audit date: 2026-08-29  
Authority: final Biome Makeover 1.20.1-1.11.4 source at `2f314c0596af095a4890995a465f308f69476b4a`  
Port checkpoint: `94e2a25e1c147086c64e646ae12fbf53414efd06`

This document is implementation guidance, not production behavior. Final source is authoritative. All ten registered BM curses and the craftable Altar are **ACTIVE FINAL**. The untranslated `sliding_curse` language keys are an orphaned historical remnant: there is no final registration or implementation, so Sliding is **HISTORICAL/REMOVED**.

## Corrections to earlier summaries

- The Altar recipe contains **two** Crying Obsidian, not one: ` B ` / `ICI` / `MCM` (`B` Book, `I` Illunite Shard, `C` Crying Obsidian, `M` Mesmerite).
- The Altar spends no XP and offers no curse-selection UI. It automatically upgrades one eligible non-curse enchantment and adds a random compatible curse.
- Final `RandomUtil.randomRange(1, max)` excludes `max`. Direct Altar item processing therefore chooses levels 1–4 for max-5 curses and 1–2 for max-3 curses. A Book always receives level 1. This oddity is released behavior.
- The random curse pool is every registered enchantment tagged/recognized as a curse, including vanilla Binding and Vanishing, except entries in the empty final `altar_curse_excluded` tag.

## Ten-curse registry

Every curse is treasure-only, non-discoverable and non-tradeable by default, has constant enchanting costs 25–50, is stored as an ordinary item enchantment, uses vanilla curse tooltip/glint behavior, and has no custom particle, sound, packet or advancement. Configuration supplies the maximum level and those common flags/costs. Unless noted, activation is while the enchanted item is in one of its declared equipment slots.

| ID / display name | Source / target | Level and exact final effect | Runtime hook | Classification |
|---|---|---|---|---|
| `decay_curse` / Curse of Decay | `DecayCurseEnchantment`; any damageable/breakable item, all slots | Max 5. Every durability event adds the curse level to the requested damage amount. Decayed equipment also rolls levels 1–4. | `ItemStackMixin` modifies `hurtAndBreak` amount at HEAD. | ACTIVE FINAL |
| `insomnia_curse` / Curse of Insomnia | `InsomniaCurseEnchantment`; armor | Max 5. Each equipped cursed stack awards `TIME_SINCE_REST` by its level every server tick while the wearer is not sleeping. This is additional to vanilla aging and stacks across cursed armor pieces. | Generic server `LivingEntity.tick` enchantment loop. | ACTIVE FINAL |
| `conductivity_curse` / Curse of Conductivity | `ConductivityCurseEnchantment`; armor | Max 5. Each equipped cursed stack independently rolls `1 / (11000 - level*1000)` each server tick. During thunder, if raining at the wearer's on-position, it spawns a real vanilla Lightning Bolt there. | Generic server equipment tick. | ACTIVE FINAL |
| `enfeeblement_curse` / Curse of Enfeeblement | `EnfeeblementCurseEnchantment`; `VANISHABLE`, all slots | Max 5. Transient maximum-health modifier `-2 * level` for each active cursed item. Removed on unequip. | Generic equipment scan and transient attribute lifecycle. | ACTIVE FINAL |
| `depth_curse` / Curse of the Depths | `DepthsCurseEnchantment`; boots | Max 3. In water/bubble, non-flying wearers have vertical velocity reduced by `0.05 * level` per tick, clamped to at least `-0.05 * level`; active swimming is cancelled. Creative/spectator-style ability flight bypasses the sink tick. | Equipment tick plus `Entity.updateSwimming` HEAD. | ACTIVE FINAL |
| `flammability_curse` / Curse of Flammability | generic `BMEnchantment`; armor | Max 3. When a new fire duration exceeds remaining fire ticks, duration becomes `ticks + ticks*(level/2)`: 1.5×, 2× or 2.5× at levels 1–3. Equipment lookup uses the highest active level rather than summing pieces. | `Entity.setRemainingFireTicks` HEAD. | ACTIVE FINAL |
| `suffocation_curse` / Curse of Suffocation | generic `BMEnchantment`; helmet | Max 3. After entity tick 20, maximum air becomes integer `300 / (level*1.5)`: 200, 100 or 66. Current air is clamped immediately when above the new maximum. | `Entity.getMaxAirSupply` HEAD. | ACTIVE FINAL |
| `unwieldiness_curse` / Curse of Unwieldiness | `UnwieldinessCurseEnchantment`; weapons plus explicit Axe support, main/offhand | Max 3. Transient attack-speed modifier `-0.25 * level`, removed when unequipped. | Generic equipment/attribute lifecycle. | ACTIVE FINAL |
| `inaccuracy_curse` / Curse of Inaccuracy | generic `BMEnchantment`; bows, main hand | Max 3. On arrow creation, pitch and yaw each receive independently signed random offsets from zero through `level * 1.3` degrees, then the arrow is shot again with normal velocity/inaccuracy. | `BowItem.releaseUsing` injection before durability damage with captured arrow local. | ACTIVE FINAL |
| `buckling_curse` / Curse of Buckling | generic `BMEnchantment`; leggings | Max 3. For falls of at least 3 blocks, adds the equipped curse level to fall distance before vanilla `calculateFallDamage`. | `LivingEntity.causeFallDamage` argument modification. | ACTIVE FINAL |

The old generic attribute tracker has a released edge case: if multiple `TickableAttributeEnchantment`s share one stack, the first stack-level tracker entry can prevent a second attribute modifier from being added. The modern port should preserve normal observable single-curse behavior first and source-test this multi-curse edge before intentionally reproducing or correcting it.

## Altar acquisition and block contract

`biomemakeover:altar` is a normal registered block with a block item and creative visibility. Its sole survival acquisition is the shaped crafting recipe above. It is not placed by worldgen or structures and has no chest/mob loot source. The `altar` advancement is awarded when it enters inventory.

- Strength 5, correct-tool requirement, pickaxe-minable, black map color, bass-drum instrument, no occlusion and cutout rendering.
- Inactive light 1; active light 5.
- Shape: 12×2×12 base, 8×8×8 center column, 12×2×12 top.
- Boolean `active` and `waterlogged` states; placement copies source-water state. No dimension restriction.
- No random or scheduled block tick. Client `animateTick` emits five enchant particles while active and occasional dripping-obsidian-tear particles at all times.
- Comparator output uses ordinary two-slot container fullness.
- Normal break drops the Altar through `survives_explosion`; its two inventory slots spill. There is no Silk Touch distinction. No special explosion, piston or redstone behavior beyond vanilla/block-entity rules.

## Altar architecture and gameplay loop

The block owns an `altar` BlockEntityType, `altar` MenuType, a two-slot `RandomizableContainerBlockEntity`, `AltarMenu`, `AltarScreen`, `AltarRenderer`, one S2C effect packet and the looping `altar_cursing` sound.

1. Craft and place the Altar.
2. Open its server-owned menu. Slot 0 accepts one valid target. Slot 1 accepts `#biomemakeover:curse_fuel`, whose only final member is `biomemakeover:illunite_shard`.
3. Valid targets are:
   - a plain Book; or
   - a nonempty, non-Enchanted-Book item with existing enchantments, no `BMCursed` marker, and at least one non-curse enchantment with max level greater than 1 that is not in empty final `altar_cant_upgrade`. With default `strictAltarCursing=false`, its current level is not capped for selection. The final compatibility branch intended to admit curse-only items is unreachable because it checks absence while iterating present enchantments.
4. With both slots valid, the server sets `active`, sends the start-sound effect to chunk listeners and increments progress for 300 ticks (15 seconds). Removing/invalidating either input resets progress to zero. No XP, cooldown or player selection exists.
5. Book output: one Enchanted Book with one uniformly random allowed curse at level 1.
6. Item output: select one eligible non-curse enchantment uniformly and add one level; select a random compatible, absent curse (100 random attempts followed by a released brute-force fallback); add a random level with the max-exclusive behavior above; set `BMCursed=true`; set repair cost 39; preserve the item.
7. Consume one fuel. If curse selection fails, the original item is popped into the world and removed from slot 0, and fuel is still consumed.

Directly processed items cannot enter the Altar again because of `BMCursed`. Enchanted Books are always rejected as Altar input, but an Altar-produced book can transfer its curse through a vanilla anvil. Items cursed indirectly by anvil do not acquire `BMCursed`, so vanilla item/enchantment rules—not the marker—govern later handling. Multiple compatible curses can coexist through books/anvils; final source declares no BM exclusive sets.

Automation is final behavior: vertical faces expose target slot 0, horizontal faces expose fuel slot 1, and exposed items can be extracted. This permits poorly arranged hoppers to remove targets early; there is no custom automation lock.

Inventory and `Progress` serialize in block-entity NBT. The `active` flag is block state. Breaking spills contents. Menu validity requires the same BE and player distance squared at most 64. Progress is synced through one `ContainerData` integer. Save/reload resumes inventory/progress; client book animation state itself is cosmetic and reconstructed.

## Rendering, sound and networking

- Static model uses five released Altar textures; no emissive shader. Block light changes with `active`.
- Block-entity renderer displays the vanilla enchanting-table book floating/bobbing over the Altar. Inactive, it faces a player within three blocks and idles; active, it rotates and opens/turns faster. Inputs are not rendered.
- Screen uses `textures/gui/altar.png`, two slots, animated glyph/progress bars and a separately rendered enchanting book.
- Block particles are client-generated from synced block state.
- `altar_cursing.ogg` starts when work transitions inactive→active. Final networking is S2C-only `bm_effect` carrying an enum ordinal and BlockPos to chunk listeners; it starts a client tickable positional sound. There is no Altar C2S packet: menu actions and server tick processing use vanilla container networking.
- A 1.21.10 payload must use a stable effect ID rather than trusting an unbounded ordinal, execute on the client thread, and verify the loaded position/Altar before starting sound. Curse choice, costs and inventory mutation remain exclusively server-authoritative.

## Curse storage, persistence and removal

The ten curses are ordinary final-1.20.1 `Enchantment` instances, not status effects or player capabilities. Levels live in the item's enchantment NBT. `BMCursed` is separate item NBT used only to prevent direct Altar reuse. There is no entity/player curse state and no curse synchronization packet; ordinary ItemStack synchronization carries everything.

For 1.21.10, definitions must be dynamic-registry JSONs and levels must live in `DataComponents.ENCHANTMENTS` or `STORED_ENCHANTMENTS`; the Altar marker should use an intentional item component or `CUSTOM_DATA`, not obsolete raw tag calls. Repair cost uses the modern repair-cost component. Holders/ResourceKeys replace raw Enchantment singleton objects.

BM supplies no cleansing system. Milk does nothing. Grindstones follow vanilla curse behavior: non-curse enchantments are removed while curses remain. Anvils can transfer compatible cursed books and preserve enchantments/repair metadata. Normal repair and item transfer preserve the stack's curse data; ordinary crafting preserves it only where the vanilla recipe preserves input components. Dropping, containers, dimension travel and reconnect preserve the ItemStack. Death drops the cursed item normally unless keepInventory retains it or Curse of Vanishing destroys it; Curse of Binding follows vanilla equipped-item rules. Totems do not cleanse curses. Hardcore/spectator transitions have no BM-specific path.

## Configuration

Final `biomemakeover.json` contains `strictAltarCursing=false` and ten `EnchantConfig` records. Defaults:

- Decay, Insomnia, Conductivity and Enfeeblement: max 5.
- Depths, Flammability, Suffocation, Unwieldiness, Inaccuracy and Buckling: max 3.
- All: min cost 25, max cost 50, treasure-only true, discoverable false, tradeable false.

There is no Altar enable flag, curse-system enable flag, explicit per-curse enabled flag, strength setting or acquisition toggle. Editing the numeric/boolean enchantment fields changes definitions in the old class-based registry.

This is a major migration blocker: 1.21.10 definitions are datapack-driven. Hardcoding defaults would lose a released config contract, while runtime code alone cannot change tooltip/anvil/max-level definition semantics. Implementation must first prove a reload-safe dynamic-registry/bootstrap or generated built-in data-pack strategy. If that is not viable, document a deliberate default-only parity compromise before coding; do not silently pretend runtime clamps are full parity.

## Hook migration matrix

| Curse | Final 1.20.1 hook | 1.21.10 candidate | Risk |
|---|---|---|---|
| Decay | `ItemStack.hurtAndBreak` ModifyVariable | Native `minecraft:item_damage` value effect adding a linear level amount; verify all three modern overloads route through `EnchantmentHelper.processDurabilityChange` | Medium |
| Insomnia | `LivingEntity.tick` equipment scan | Native enchantment `minecraft:tick` requires a custom server entity effect, or one narrow server tick query | Medium |
| Conductivity | same equipment scan | Custom server tick enchantment effect/query; spawn real LightningBolt after exact weather/position/random checks | High (frequency and multiple pieces) |
| Enfeeblement | custom transient attribute tracker | Native `minecraft:attributes` with `add_value`, stable ID and `any` slots; verify stacking and health clamp | Medium |
| Depths | equipment tick + `Entity.updateSwimming` | Custom tick/query for velocity plus narrow `updateSwimming` HEAD hook; modern method still exists | High |
| Flammability | `Entity.setRemainingFireTicks` HEAD | Narrow modern `setRemainingFireTicks(int)` hook; avoid recursion through `igniteForSeconds` | Medium |
| Suffocation | `Entity.getMaxAirSupply` HEAD | Narrow modern `getMaxAirSupply()` hook; preserve tick>20 guard and immediate current-air clamp | Medium |
| Unwieldiness | transient attribute tracker | Native `minecraft:attributes`, `add_value`, hand slots; supported-items tag must include swords/axes exactly | Medium |
| Inaccuracy | `BowItem.releaseUsing` local capture | Investigate native `minecraft:projectile_spread` first; verify it affects a single bow arrow with the historical ±1.3°/level distribution. Otherwise inject modern `shootProjectile`/`releaseUsing` without fragile local capture | High |
| Buckling | ModifyArg into `calculateFallDamage(float,float)` | Modern signatures use doubles (`causeFallDamage(double,float,DamageSource)` / `calculateFallDamage(double,float)`); modify the distance argument narrowly | Medium |

Native 1.21.10 effect components should replace mixins only where they reproduce the released result. There are no client-only curse hooks. Tooltip red formatting/glint comes from the `minecraft:curse` enchantment tag. Put the definitions in `minecraft:curse` and `minecraft:treasure`, and omit them from enchanting-table/trade tags. Use final supported-item categories and empty BM exclusion/cannot-upgrade tags.

## Advancements and resources

Direct Stage 9B advancement:

- `biomemakeover:biomemakeover/altar`: parent `biomemakeover:biomemakeover/illunite_shard`; criterion name `get_stew`, trigger `minecraft:inventory_changed`, requiring `biomemakeover:altar`; task frame, `hidden=false`, toast/chat enabled; no reward.

There is no advancement for applying, receiving, removing or using a specific BM curse. `cursed_hat` is a later Badlands/Ghost-Town progression advancement using vanilla Binding and a historical Taniwha armor trigger; it is not Stage 9B-owned. Enchanted Totem progression is Mansion-owned.

Final Stage 9B resource inventory:

- Registries: block + block item `altar`; BlockEntityType `altar`; MenuType `altar`; SoundEvent `altar_cursing`; ten enchantments.
- Altar data/assets: one recipe, one block loot table, one advancement, one blockstate, one block model, one item model/modern item definition, five block PNGs, one GUI PNG, one OGG, sound definition and language entries.
- Tags: item `curse_fuel` containing Illunite Shard; enchantment `altar_curse_excluded` empty; enchantment `altar_cant_upgrade` empty; modern curse/treasure tag membership and supported-item tags as required.
- Ten modern enchantment JSON definitions replace the old code registrations. No custom particle, shader, curse texture, curse sound or curse-specific advancement exists.

Current 1.21.10 production/package status is **fully absent** for all of the above. Only documentation/deferred contracts mention it. There are no registry-only stubs, leaked assets, recipes, packets, translations or curse mixins. `sliding_curse` is absent too, correctly.

There is no DORMANT FINAL component inside the proposed Stage 9B scope: the Altar and all ten registered curses have final acquisition paths. The empty extension tags are active data contracts, not dormant gameplay. Sliding alone is historical/removed.

## Dependencies and reachability

Hard dependencies already restored: Mesmerite block, Illunite Shard, final item/tag/recipe infrastructure, and ordinary vanilla Book/Crying Obsidian/enchantment/anvil systems. The Altar has no dependency on Stunt Powder, Bulbus Root, Moth Scale, Nocturnal, Peat, Ectoplasm, Ghosts, structures, Mansion, Witch quests or Beach content.

Soft/deferred integrations:

- Existing Decayed final behavior is an independent acquisition path for Decay-enchanted equipment; Stage 9B definitions must reconnect that canonical holder without altering accepted Decayed AI/loot.
- Vanilla Binding obtained from the Altar can eventually feed `cursed_hat`, but its Ghost Town parent/custom armor criterion is later-owned.
- Mansion code can create stunted mobs and Enchanted Totem loot, but neither is required by Altar/curses.
- Charm Totem or other-mod death compatibility is future integration only; ordinary vanilla ItemStack semantics should compose naturally.

Reachability is complete in final release: craftable Altar → place/menu → Book or eligible enchanted item + Illunite fuel → persistent ordinary curse enchantment → effect while applicable/equipped → only vanilla curse end/removal semantics. All ten BM curses are in the active curse registry and can be produced as level-1 Altar books; they are not dormant merely because enchanting tables/trades exclude them.

## Recommended implementation split

Do **not** implement all global hooks, data migration, GUI, renderer and networking in one checkpoint.

### Stage 9B.1 — Complete ten-curse engine

- Restore config and prove the dynamic-definition strategy.
- Add all ten definitions, curse/treasure/applicability tags, holder-based lookup and complete runtime effects together.
- Reconnect Decayed's existing Decay equipment generation narrowly.
- Do not register/expose the Altar yet.

Rationale: the ten definitions share storage/config/tags and must never ship with only some effects working. Commands and controlled enchanted books can runtime-test every effect before Altar randomness obscures failures. Decay has an existing natural BM acquisition path, so this is not purely dead infrastructure. Keep 9B overall open until 9B.2.

### Stage 9B.2 — Complete Altar acquisition and processing

- Add Altar block/item/BE/menu/screen/renderer/sound, recipe/loot/advancement, fuel/exclusion tags, modern `Cursing` logic, marker/repair components and S2C sound payload.
- Validate manual and hopper operation, persistence, random curse selection, vanilla curse inclusion and all failure paths.
- Close Stage 9B only after both checkpoints pass runtime acceptance.

This two-part split isolates the high-risk global enchantment migration from inventory/render/network debugging without exposing a nonfunctional Altar. Splitting the ten curses into partial public batches would create misleading enchanted items and is not recommended.

## Future runtime matrix

### 9B.1 shared/storage tests

- Use commands only to accelerate creation of correctly enchanted test items; do not use them as evidence of Altar acquisition.
- Verify all ten IDs, red curse tooltip, levels, supported/unsupported items, books/anvil transfer, coexistence, grindstone retention, repair/component persistence, drop/pickup, container, relog and dimension travel.
- Verify config defaults and at least one non-default max/cost configuration if dynamic definitions are retained.
- Verify Decayed equipment still carries Decay without changing Decayed behavior.

### 9B.1 effect tests

- Decay: compare identical durability events at levels 1–5 and an unenchanted control.
- Insomnia: compare `TIME_SINCE_REST` growth while awake/sleeping and phantom timing; test multiple pieces.
- Conductivity: thunder/rain positive case at high level and dry/clear-sky controls; verify real lightning semantics and multiple pieces without an exception loop.
- Enfeeblement: measure max health per level, multiple items, unequip/re-equip, current-health clamp, relog/death.
- Depths: compare sinking/swimming at levels 1–3, dry-land control and ability-flight bypass.
- Flammability: ignite controlled targets at levels 1–3; measure 1.5×/2×/2.5× duration and unenchanted control.
- Suffocation: verify maximum air 200/100/66, immediate clamp, drowning/recovery and helmet removal.
- Unwieldiness: measure attack speed for sword and axe in both hands, each level, removal and unrelated item control.
- Inaccuracy: shoot a fixed distant target repeatedly at each level; verify bounded random pitch/yaw deviation and normal bow velocity/durability.
- Buckling: repeat identical ≥3-block falls with each level and a sub-3-block negative control; compare damage.
- Regression: Stuntable/Owl baby aging, Nocturnal, Decayed, armor attributes, normal durability, swimming, fire, air, bows and fall damage without curses.

### 9B.2 Altar tests

- Craft exact recipe; verify advancement, placement, shape, inactive/active light, waterlogging, break/re-place and inventory spill.
- Book + one Illunite fuel: verify 300 ticks, one fuel, random level-1 curse, sound, particles, animated book, GUI progress and no XP cost.
- Eligible enchanted item: verify one ordinary enchant gains exactly one level, one compatible random curse is added, repair cost becomes 39, `BMCursed` blocks repeat use and input metadata persists.
- Negative controls: unenchanted item, Enchanted Book, max-1-only enchantment, incompatible targets, missing/wrong fuel and interrupted progress.
- Verify vanilla Binding/Vanishing remain in the random pool and empty exclusion/cannot-upgrade tags behave as final data.
- Save/reload midway, unload/reload chunk, break while occupied, and test failed-selection item ejection/fuel consumption.
- Hopper tests for vertical target/horizontal fuel insertion and exposed extraction; comparator output.
- Multiplayer: two viewers, stale/distant menu closure, simultaneous access, server-only processing, S2C sound for chunk listeners, disconnect/reconnect and dedicated server.

## Explicit exclusions

Stage 9B does not own Witch Hat/quests, Living World integration, Antidote, Ectoplasm/Ghosts/Poltergeist, Phantom Membrane crossover, Mushroom House, Sunken Ruins, Mansion/Tapestries/Directional Data, Crude/cladding, Stone Golem, Adjudicator/Mimic, Enchanted Totem, music discs, Beach systems, boats, Toad/Tadpole, `sliding_curse`, custom curses, custom Altar recipes or any Mythas balance/behavior enhancement.

Useful future extension hooks are the curse/exclusion tags and server-authoritative Altar selection service. Mythas additions must use separate IDs/data and must not alter the final BM defaults during parity work.
