# Stage 9B.1 — Final Ten-Curse Engine

Status: **IMPLEMENTED / AWAITING RUNTIME ACCEPTANCE**. Stage 9B.2 (Altar) is **NOT STARTED**.

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
| `suffocation_curse` | 3 | helmet / head | narrow max-air hook, after tick 20, values 200/100/66 and immediate clamp |
| `unwieldiness_curse` | 3 | weapons plus axes / hands | native slot-aware attack-speed attribute `-0.25L` |
| `inaccuracy_curse` | 3 | bow / main hand | bow-only launch hook; independently signed uniform pitch/yaw offsets through `1.3L` degrees |
| `buckling_curse` | 3 | leggings / legs | modifies fall-distance input by `+L` for distance at least 3, then preserves vanilla calculation |

All ten are in the vanilla `curse` and `treasure` tags and deliberately absent from enchanting-table, trade and random-loot selection tags. Ordinary curse tooltip, grindstone preservation, books, anvils and component persistence remain vanilla. `sliding_curse` remains an orphan translation only and is not defined. No curse adds a sound, particle, packet or advancement.

## Decayed integration

The existing released shield equipment path now enchants its shield through the canonical active-registry `decay_curse` holder at a uniform level 1–4. No second Decay implementation and no Decayed AI/render/loot change was introduced.

## Runtime handoff

Use `/enchant @s biomemakeover:<id> <level>` while holding a compatible item, then equip/use it. Test component persistence through save/reload, drop/pickup, containers, dimension travel, enchanted books/anvils, and verify a grindstone preserves every BM curse. Detailed numeric tests are in the implementation report handed to Prism testing.

Runtime-open: every curse effect, per-piece stacking, RNG behavior, persistence, vanilla curse interoperability, Decayed equipment, and dedicated-server bootstrap. The Altar, `BMCursed`, curse selection/upgrading, its block entity/menu/screen/renderer/packets/resources and all Stage 10+ systems remain absent.
