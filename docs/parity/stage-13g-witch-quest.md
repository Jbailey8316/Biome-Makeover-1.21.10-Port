# Stage 13G — Witch quest/progression parity

Status: RUNTIME ACCEPTED / CLEANED / FROZEN.

The implementation follows the final released 1.20.1 architecture: ten
reloadable quest categories (`common`, `dark_forest`, `flower`, `jungle`,
`mesa`, `mushroom`, `nether`, `ocean`, `rare`, and `swamp`), four reloadable
reward tables (`items`, `multi_potions`, `potion`, and `potion_ingredients`),
three Witch-owned quests, released weighted quest sizes (1–5 with weights
5/8/4/3/1), and rarity thresholds 0/8/15/30.

Witches require a player wearing the released Witch Hat tag, have no target or
active raid, and no current customer before opening the quest menu. The server
owns quest selection, inventory checks, item consumption, reward selection,
and completion. Client display is synchronized through current 1.21.10 custom
payloads; the completion payload contains only a quest index and is validated
against the active server menu and inventory.

Quest state is Witch-owned and persisted under the released `Quests`,
`DespawnShield`, and `ReplenishTime` fields. The current port stores the quest
compound through `ValueInput`/`ValueOutput` and preserves the released item IDs,
counts, and rarity points. The four released reward tables are supplied by
the pinned reference-resource pipeline, with current-version path remapping
for advancement and loot-table directories.

Current-Minecraft adaptations are limited to the `ValueInput`/`ValueOutput`
entity API, component-based potion contents, Fabric custom payload registration,
and the current menu/screen registration APIs. The released custom Witch Hat
loot table is invoked from the current `dropFromLootTable` hook. The released
antidote Witch hook and its potion dependencies are also restored because they
are part of the final Witch mixin behavior and are required by the reward
tables.

The released 512x256 `witch.png` is byte-identical to the reference asset.
Its current-version compatibility repair is limited to using
`RenderPipelines.GUI_TEXTURED` for the panel, button, and rarity-marker blits.
The current payload/menu path carries the server-authoritative quest list and
refreshes the released screen rows after receipt. The Witch Hat loot condition
uses the current 1.21.10 schema while preserving the released probability.

Prism runtime accepted constructor-time population of three quests, Witch-owned
persistence, synchronization, the released GUI, completion, rewards, and
save/reload behavior. No temporary Stage 13G diagnostics remain.

## Prism checklist

Use a fresh disposable world and the fresh Stage 13G JAR. Wear a Witch Hat,
find or summon a normal Witch, confirm vanilla combat, open the menu, verify
quest requirements, make partial progress, save/reload and unload/reload the
Witch, then complete one quest. Confirm server-side item consumption, one
released reward, one advancement trigger, no duplicate completion, the three
quest slots/replenishment behavior, and the exact Witch Hat drop conditions.
Repeat with a second Witch/player where practical and inspect logs for Witch,
quest, menu, payload, codec, NBT, reload, reward, and registry errors.
