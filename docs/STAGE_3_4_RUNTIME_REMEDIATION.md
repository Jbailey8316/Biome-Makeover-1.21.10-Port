# Stage 3/4 Client Runtime Remediation

## Checkpoint scope

This checkpoint responds to the first real client launch of the Stage 3/4 JAR. It was reviewed against
`docs/PORTING_ENGINEERING_RULES.md`. It does not start Stage 5, change Owl behavior, restore deferred
structures/progression, add Taniwha, or upgrade dependencies.

Tested predecessor: `biomemakeover-fabric-1.21.10-0.8.5-flight-eyes-blink-fix.jar`, SHA-256
`9E9115D3F46F7AA2F8397B53E6B0D7BCFC9E53F30381816AD9F9D7F93E420AF0`.

The reported runtime state is CLIENT RUNTIME VALIDATED only through mod initialization. World creation
failed during dynamic registry loading. No statement in this document claims that the remediated JAR has
passed client runtime, world creation, dedicated-server, multiplayer, save/reload, or existing-world tests.

## Authoritative codec finding

Minecraft 1.21.10's own `data/minecraft/worldgen/configured_feature/moss_patch.json`, read from the
official mapped 1.21.10 client/common artifacts used by this build, encodes a uniform `xz_radius` as:

```json
{
  "type": "minecraft:uniform",
  "min_inclusive": 4,
  "max_inclusive": 7
}
```

The released 1.20.1 `underground_mycelium` resource instead nested those bounds under `value`. The
1.21.10 `IntProvider` codec accepts either a number or the flattened typed provider and rejects that nested
shape. A deterministic resource conversion now flattens exactly this obsolete `xz_radius` form. All built
Biome Makeover configured and placed features were scanned; `underground_mycelium` was the only restored
resource containing the obsolete typed-provider-plus-`value` structure.

## Runtime issue disposition

| Reported issue | Status | Evidence and remediation |
|---|---|---|
| `mushroom_fields/underground_mycelium` decode failure | FIXED (static/package) | Converted nested uniform provider to the exact vanilla 1.21.10 flattened shape; validator rejects recurrence. Runtime retest required. |
| Ten missing Cracked Bricks block models | FIXED | Historical models existed under `models/block/decoration/cracked_bricks/`; Stage 4's non-recursive include omitted them. The complete directory is now packaged and model references validate. |
| `minecraft:item/template_spawn_egg` | FIXED | 1.21.10 vanilla spawn eggs use generated item models backed by individual textures; the old tint-template parent is absent. Glowfish, Scuttler and Cowboy eggs now receive deterministic modern models/textures using released primary/secondary colors and the current vanilla egg silhouette. Owl already had a modern model/texture. |
| Missing `item/mycelium_sprouts` | FIXED | Historical item texture existed but Stage 3 copied only the block texture. It is now packaged. |
| Missing `block/tumbleweed`; model says `tumbleweeb` | FIXED | `tumbleweeb` is a paired released typo: both the historical model reference and historical texture use that path. The texture is now copied under its historical path; no registry ID or model reference was renamed. |
| Missing `item/cracked_brick` | FIXED | Historical item texture existed but was omitted by the Stage 4 include. It is now packaged. |
| Axe tag references `#biomemakeover:tapestries` | INTENTIONALLY DEFERRED | Tapestries are not Stage 3/4 content. The premature cross-stage tag entry is filtered from the built axe tag; no Tapestry content was activated. |
| Cactus immunity and Scuttler food reference `pink_bud` | FIXED | Source tracing shows Pink Bud is produced by `gameplay/scuttler_eating`, gates the Scuttler Tail advancement, and belongs to the released Badlands loop. Pink Bud, its dye recipe, advancement, model/texture, and 1–2 item Scuttler flower-eating output are restored under the historical ID. This is Stage 4 gap closure, not Stage 5 content. |
| Tumbleweed spawn tag references `#biomemakeover:badlands` | FIXED | The released `badlands` biome tag is legitimate Stage 4 selector infrastructure and contains vanilla `#minecraft:is_badlands` plus optional `#c:badlands`. It is now packaged. |

No reported deterministic issue remains open in source/package validation. Actual registry loading and world
creation remain STILL OPEN pending the next Prism client test.

## Additional defects detected by the improved validator

- An existing Ancient Oak door model incorrectly referenced `willow_door_bottom`; it now references the
  already-packaged `ancient_oak_door_bottom`. No Willow/Stage 5 content was restored.
- Deferred Blighted Balsa boat/chest-boat models referenced absent textures and were being packaged despite
  the corresponding IDs being intentionally deferred. They are now excluded until approved boat plumbing
  restores the complete feature.

## Validator additions

`validation/Invoke-ParityValidation.ps1` now checks built output for:

- unresolved `biomemakeover:` model and parent references;
- statically resolvable internal block/item texture references;
- the removed `minecraft:item/template_spawn_egg` parent;
- nested `minecraft:uniform` providers using the obsolete `value` structure;
- unresolved internal BM tag-to-tag references;
- direct BM block/item/entity tag members missing from current registry contracts.

The checks intentionally do not guess about dynamic resource references or declare runtime success.

## Registry and world safety

Pink Bud is the only registry addition: items increase from 181 to 182. No existing block, item, entity,
feature, sound, or other persistent ID is removed, renamed, or repurposed. All remaining changes affect
resource decoding/rendering, deterministic validation, or packaging. Existing-world compatibility remains
additive; a copied-world runtime test is still required.

## Required Prism retest

1. Confirm Minecraft reaches title and Biome Makeover initializes.
2. Create a new world and confirm dynamic registries bind successfully.
3. Capture `latest.log`, even if creation succeeds, and search for Biome Makeover decode/model/texture/tag errors.
4. Inspect Mushroom Fields configured-feature generation, Cracked Bricks models, all four current BM spawn
   eggs, Mycelium Sprouts, Tumbleweed, Cracked Brick, and Pink Bud.
5. Exercise Scuttler flower eating and confirm 1–2 Pink Bud output, food temptation/passive interaction,
   advancement chain, and save/reload.
6. Continue the existing Stage 3/4 runtime checklists only after registry loading succeeds.

Dedicated-server, multiplayer, save/reload, and existing-world-copy validation remain manual requirements.
