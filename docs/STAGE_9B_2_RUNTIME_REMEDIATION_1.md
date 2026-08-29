# Stage 9B.2 Runtime Remediation 1

Status: **REMEDIATED / AWAITING TARGETED RUNTIME RETEST**.

Authority is final released Biome Makeover 1.20.1. The earlier Stage 9B.2 Prism run accepted crafting, block/model/UI, both cursing paths, 300-tick processing, exclusive curse levels, over-maximum upgrade, marker rejection, grindstone persistence, interruption, save/reload, inventory drops, comparator, renderer, and particles. This pass changes only the demonstrated runtime defects and the separately authorized Mythas automation rule.

## Source-resolved observations

The final data pack has `recipes/altar.json` and the visible obtain-Altar advancement `advancements/biomemakeover/altar.json`. It has no recipe-unlock advancement and no generated unlock trigger. The observed recipe-book behavior - hidden until the player manually crafts and vanilla awards the recipe - is therefore source-correct and unchanged.

Final `AltarBlockEntity` exposes target slot 0 vertically and fuel slot 1 horizontally. Its `canTakeItemThroughFace` unconditionally returns true. Immediate extraction of pending input is therefore exact final behavior; active state and progress never gated extraction.

Final cursing writes `BMCursed=true` and repair cost 39. In vanilla 1.20.1, every non-rename anvil operation adds cost to that 39 and survival receives no output at 40 or higher. Rename-only is specifically capped at 39, and creative retains its ordinary bypass. This effectively blocks enchanted books, enchanted-item combination/upgrades, material repair, and durability combination with a `BMCursed` stack on either side, while preserving rename-only and creative use.

## Parity remediation

- Water Bucket and empty Bucket interactions are passed to the 1.21.10 `SimpleWaterloggedBlock`/`BucketItem` path before the empty-hand menu interaction. WATERLOGGED state, source water, scheduled fluid ticks, UI, and processing remain otherwise unchanged.
- The Altar server sends one vanilla block event when a valid processing run starts. The block delegates it to the block entity; the client-only callback starts the registered positional sound. This restores the final explicit S2C start edge without a C2S path or custom outcome data.
- A narrow `AnvilMenu.createResult()` tail hook checks `BMCursed` on either input. In survival it clears a non-rename result; rename-only and creative are preserved. This modernizes final effective behavior independently of mods that relax the XP ceiling and leaves ordinary items untouched.
- Static validation resolves `AnvilMenu.createResult()V` (`method_24928`), `onlyRenaming:Z` (`field_52566`), and the three `ItemCombinerMenu` fields against the actual 1.21.10 mapped common JAR and audits their packaged intermediary names.

## MYTHAS ENHANCEMENT - Altar automation locking

This is explicitly outside original BM parity. Final 1.20.1 allowed a hopper to pull any exposed Altar slot immediately. Mythas now makes ordinary unattended processing practical:

- Slot 0 blocks automated extraction while its stack is a valid processable Altar target.
- Successful Book and ordinary-item outputs are invalid as new direct inputs, so they become automatically extractable without a separate persisted output flag.
- Invalid target-slot garbage remains extractable.
- Slot 1 blocks real Illunite fuel extraction only while a valid target is pending/processing. It releases fuel when no valid target exists, and never traps an invalid fuel-slot stack.
- Manual menu extraction, face-specific insertion, one-shard consumption, and failed-selection pop/clear/fuel-loss behavior are unchanged.

## Runtime-only checks

Prism must confirm water Bucket placement/pickup, audible sound start/stop lifetime, the exact permitted/blocked anvil operations, and the separately documented automated extraction behavior. Static checks cannot hear audio or exercise hopper transfer timing.
