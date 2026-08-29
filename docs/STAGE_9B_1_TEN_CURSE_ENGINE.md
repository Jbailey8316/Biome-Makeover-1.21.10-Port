# Stage 9B.1 — Final Ten-Curse Engine

Status: **COMPLETE / RUNTIME ACCEPTED**. Stage 9B.2 (Altar) is implemented in its separate checkpoint and **AWAITING RUNTIME ACCEPTANCE**; none of its implementation changes the accepted curse hooks.

## Authority and architecture

Final released Biome Makeover 1.20.1 is the behavioral authority. Minecraft 1.21.10 no longer uses subclassed `Enchantment` registrations: all ten definitions are dynamic-registry JSON, classified through vanilla enchantment tags, persisted through `ENCHANTMENTS` / `STORED_ENCHANTMENTS`, and resolved at runtime through canonical `ResourceKey` values against the active `RegistryAccess`. No `Holder` is cached across bootstrap or reload.

The historical config could change max level, costs, treasure, discoverability and tradeability on class instances. Those fields now belong to frozen datapack registry definitions. Stage 9B.1 therefore ships the final released defaults (cost 25–50, treasure-only, undiscoverable, untradeable) as static server-visible definitions. Server datapacks may override them during normal registry loading; BM does not mutate a frozen registry or generate client-only definitions. This is the deliberate reload-safety migration limitation.

## Definitions and mechanisms

| ID | Max | Supported items / slot | Modern mechanism |
|---|---:|---|---|
| `decay_curse` | 5 | durability / any | native `item_damage` adds exactly the level |
| `insomnia_curse` | 5 | armor / armor | server tick, per equipped piece, awards its level to `TIME_SINCE_REST` while awake |
| `conductivity_curse` | 5 | armor / armor | server tick, independent per-piece `1/(11000-1000L)` roll; real lightning during local rain/thunder |
| `enfeeblement_curse` | 5 | vanishable / any | native slot-aware max-health attribute, `-2L` per equipped item |
| `depth_curse` | 3 | boots / feet | server tick downward velocity `0.05L`, flying bypass; swimming cancellation |
| `flammability_curse` | 3 | armor / armor | narrow new-fire-duration hook, highest equipped level, factors 1.5/2/2.5 with historical truncation |
| `suffocation_curse` | 3 | helmet / head | narrow max-air hook, after tick 20, values 200/100/66; current air clamps when vanilla next queries maximum air |
| `unwieldiness_curse` | 3 | weapons plus axes / hands | native slot-aware attack-speed attribute `-0.25L` |
| `inaccuracy_curse` | 3 | bow / main hand | bow-only launch hook; independently signed uniform pitch/yaw offsets through `1.3L` degrees |
| `buckling_curse` | 3 | leggings / legs | modifies fall-distance input by `+L` for distance at least 3, then preserves vanilla calculation |

All ten are in the vanilla `curse` and `treasure` tags and deliberately absent from enchanting-table, trade and random-loot selection tags. Ordinary curse tooltip, grindstone preservation, books, anvils and component persistence remain vanilla. `sliding_curse` remains an orphan translation only and is not defined. No curse adds a sound, particle, packet or advancement.

## Decayed integration

The existing released shield equipment path now enchants its shield through the canonical active-registry `decay_curse` holder at a uniform level 1–4. No second Decay implementation and no Decayed AI/render/loot change was introduced.

## Runtime handoff

Use `/enchant @s biomemakeover:<id> <level>` while holding a compatible item, then equip/use it. Test component persistence through save/reload, drop/pickup, containers, dimension travel, enchanted books/anvils, and verify a grindstone preserves every BM curse. Detailed numeric tests are in the implementation report handed to Prism testing.

Runtime-open: every curse effect, per-piece stacking, RNG behavior, persistence, vanilla curse interoperability, Decayed equipment, and dedicated-server bootstrap. The Altar, `BMCursed`, curse selection/upgrading, its block entity/menu/screen/renderer/packets/resources and all Stage 10+ systems remain absent.

## Runtime remediation 1 — maximum-air hook ownership

The first Prism launch exposed a mixin-owner migration error before gameplay began. In 1.21.10, `getMaxAirSupply()I` is declared by `Entity` (`class_1297.method_5748`), not `LivingEntity` (`class_1309`). The named source compiled because the method is inherited, and Loom remapped the selector correctly, but Mixin only searches the declared target class during injection. The required Suffocation injection now lives in `EntityCurseMixin`, matching the final 1.20.1 owner pattern and retaining exact 200/100/66 values plus current-air clamping whenever vanilla queries maximum air. Final source has no equipment-change callback, so equipping the helmet underwater need not rewrite the air field in that same interaction tick.

The complete curse hook set was rechecked against the actual 1.21.10 named classes and intermediary mapping table. Packaged selectors are also validated after remapping. The loader's “No refMap loaded” diagnostic is expected for this project: Loom rewrites selectors directly into the remapped production classes, as demonstrated by the failing named selector appearing at runtime as `method_5748`. Existing accepted mixins use the same architecture; no global refmap change is required.

## Runtime findings audit

Prism confirmed Decay, Insomnia, Depths, Flammability, Suffocation, Unwieldiness, Inaccuracy and Buckling execute. Enfeeblement produced the exact `20 -> 16 -> 12` level-II stacking and clean removal contract.

- **Enfeeblement held armor is final-source parity.** The released enchantment used `EnchantmentCategory.VANISHABLE`, declared every `EquipmentSlot`, and its tickable attribute framework evaluated each currently equipped slot—including main hand and offhand. Consequently a command/anvil-created cursed chestplate held in a hand applies its penalty, as does any other supported vanishable item in an active hand. Merely existing in ordinary inventory does not activate it. The 1.21.10 `supported_items: #minecraft:enchantable/vanishing`, `slots: ["any"]` definition is the faithful mapping.
- **Suffocation live-equip delay is parity-correct.** Final source performed the current-air clamp inside `getMaxAirSupply`; it did not listen for equipment changes. A full-air player equipping the helmet underwater retains that field until vanilla next queries maximum air, at which point it clamps to 200/100/66. Documentation must not call this an immediate equipment callback.
- **Conductivity remains source-correct and runtime-inconclusive only because it is random.** The server tick loops all four armor slots. Each enchanted stack independently resolves its own level and rolls `nextInt(11000 - 1000 * level)`. Only a successful roll continues through thunder and `isRainingAt(getOnPos())`; it then creates and adds a real `LIGHTNING_BOLT` at the block-bottom center. Four level-V pieces therefore use four independent 1/6000 rolls per tick. Clear weather, sheltered positions, dry biomes and dimensions without qualifying weather cannot strike. No probability or debug behavior was changed.

Shared vanilla behavior remains data-driven: curse tags preserve grindstone behavior and red tooltip semantics; normal/stored enchantment components preserve items and books through anvils, saves, containers, drops and dimension travel. The ten definitions remain omitted from enchanting-table, trading, random-loot and mob-equipment acquisition tags.

## Final runtime acceptance

Prism runtime acceptance on Minecraft 1.21.10 confirmed:

- Decay III added three durability damage to a normal one-point event (`250 -> 246`).
- Insomnia accelerated `TIME_SINCE_REST`, with multiple equipped pieces stacking.
- Enfeeblement II produced `20 -> 16 -> 12` maximum health with clean restoration; held cursed armor also activated, matching the released VANISHABLE/all-slot contract.
- Depths II forced strong downward water motion and prevented swimming only while the boots were equipped.
- Flammability III substantially extended new fire duration only while armor was equipped.
- Suffocation levels reduced air capacity; the apparent live-equip delay matches the released query-time clamp rather than an equipment callback.
- Unwieldiness III visibly reduced attack speed by its `-0.75` modifier.
- Inaccuracy III produced the released severe independent pitch/yaw scatter.
- Buckling III increased damage from a controlled five-block fall.

Conductivity's finite manual sample did not produce a strike, which is not evidence of failure for an independent 1/6000 level-V roll. Its complete per-piece server tick, exact denominator, thunder/local-rain gates and real-lightning spawn path are source-, mapping- and package-validated and are accepted without changing probability.

There are no remaining Stage 9B.1 production blockers. No Altar, `BMCursed`, debug probability, Sliding Curse, or later-stage content was introduced.
