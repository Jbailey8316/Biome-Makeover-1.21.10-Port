# Stage 2 – Mesmerite Content Slice

This stage restores two original Biome Makeover blocks on Fabric 1.21.10:

- `biomemakeover:mesmerite`
- `biomemakeover:polished_mesmerite`

Included:

- original textures, including Mesmerite animation metadata
- original cube block models and blockstates
- current 1.21.10 item-definition JSON files
- block-item registration
- Building Blocks creative-tab placement
- self-drop loot tables moved to the current singular `loot_table` data path
- original registry identifiers

Not included yet:

- slabs, stairs, or walls
- crafting recipes
- Mesmerite world generation
- Dark Forest makeover
- entities or special mechanics

## Test checklist

1. Run `build-windows.bat`.
2. Replace the Stage-1 JAR in Prism with the Stage-2 JAR from `build/libs`.
3. Launch with Fabric API installed.
4. Create or enter a disposable creative world.
5. Search the Building Blocks tab for `Mesmerite`.
6. Place both blocks and confirm textures render.
7. Save, exit, relaunch, and verify both blocks remain.
8. Optionally switch to survival and verify each block drops itself when mined.
