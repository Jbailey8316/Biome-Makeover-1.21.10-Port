# Stage 11B.2B — Released Mansion Tapestry Parity Audit

Status: released-source audit complete; implementation is authorized only for
the released contract recorded here. This is not a Mythas Enhancement and does
not activate Stage 12 boss gameplay.

## Authority and inventory

The authority for this audit is the final released source/resources under
`reference/Biome-Makeover-1.20`. Supporting repository audits are
[the Mansion source audit](STAGE_11A_MANSION_SOURCE_AUDIT.md) and [the Mansion
marker audit](STAGE_11B1_MANSION_MARKERS.md).

The released family contains 17 tapestry variants: the 16 `DyeColor` names
plus `adjudicator`. Each variant has a standing block and a wall block, for 34
blocks total, and one BlockItem per variant, for 17 tapestry items total.

Released registry IDs are:

```text
white_tapestry, orange_tapestry, magenta_tapestry, light_blue_tapestry,
yellow_tapestry, lime_tapestry, pink_tapestry, gray_tapestry,
light_gray_tapestry, cyan_tapestry, purple_tapestry, blue_tapestry,
brown_tapestry, green_tapestry, red_tapestry, black_tapestry,
adjudicator_tapestry
```

The corresponding wall IDs add `_wall` before `_tapestry`. The shared block
entity ID is `biomemakeover:tapestry`.

## Released implementation contract

Each standing and wall block is a `BaseEntityBlock` with a
`TapestryBlockEntity`. The client registers one `TapestryRenderer`; the item
renderer also uses a temporary tapestry block entity so the item uses the
same modeled flag renderer. The blocks are model-backed through the shared
`block/tapestry` model and use the variant texture selected by block class.

Standing tapestries:

- expose `BlockStateProperties.ROTATION_16`;
- are placed on a solid block below the tapestry;
- use an 8-by-16-by-8 voxel shape (`4..12`, `0..16`, `4..12`);
- choose the 16-step rotation from the placement rotation;
- rotate and mirror the `ROTATION_16` property with vanilla `Rotation` and
  `Mirror`;
- break when the supporting block below is removed;
- have no collision, strength 1, wood sound, and no special interaction.

Wall tapestries:

- expose horizontal `FACING` only;
- select the opposite horizontal face of the clicked direction when placed;
- survive only when the block behind `FACING.getOpposite()` is solid;
- break when that supporting wall block is removed;
- use the released directional shapes: a two-block-deep wall strip with
  height `0..12.5` and full width on the corresponding wall face;
- rotate and mirror `FACING` through vanilla horizontal direction transforms;
- have no collision, strength 1, wood sound, and no special interaction.

The color variants are mechanically identical apart from texture and item
rarity. The Adjudicator variant uses the same mechanics but is `EPIC` rather
than `UNCOMMON`, and its blocks use `forceSolidOn()`. Wall blocks drop the
standing item through `dropsLike`; the released block loot tables drop one
corresponding tapestry item on `survives_explosion`. No Silk Touch, tool
requirement, waterlogging, redstone, comparator, or custom right-click
behavior is present.

The 17 items stack to 16 and are `StandingAndWallBlockItem`s, with the
standing block as the default placement and the wall block selected for wall
placement. All 17 are appended to the released Biome Makeover creative tab.
No authored crafting recipes were found. Ordinary color tapestries are
Mansion marker rewards; the Adjudicator tapestry is boss-owned in the
released source. The released `all_tapestries` advancement requires all 16
colors plus Adjudicator.

## Released Mansion integration

The 168 released Mansion NBT files and the ported 168 files have matching
inventory structure and matching tapestry marker usage. Template records use
`biomemakeover:directional_data` with `metadata=tapestry`, not direct final
tapestry block states. The audit found 56 such marker occurrences in 12
templates:

| Template | Marker count |
| --- | ---: |
| `corridor/t/corridor_t_1.nbt` | 2 |
| `dungeon/door_3.nbt` | 2 |
| `dungeon/room_12.nbt` | 4 |
| `dungeon/room_6.nbt` | 4 |
| `room/big/room_big_1.nbt` | 3 |
| `room/big/room_big_11.nbt` | 2 |
| `room/big/room_big_2.nbt` | 6 |
| `room/big/room_big_6.nbt` | 10 |
| `room/big/room_big_7.nbt` | 11 |
| `room/big/room_big_8.nbt` | 4 |
| `room/big/room_big_9.nbt` | 2 |
| `room/room_10.nbt` | 6 |

For a marker whose transformed facing is vertical (`UP` or `DOWN`), released
Mansion code randomly chooses one of the 17 standing blocks and assigns a
random vanilla 16-step rotation. For a horizontal facing, it randomly chooses
one of the 17 wall blocks and assigns `facing=direction.getOpposite()`.
The marker position is cleared before dispatch, then the selected tapestry is
written with block-update flags 3. Thus the final variant is generated at
placement time, rather than serialized as a fixed color or selected by a
processor. This behavior is rotation-aware because the transformed
Directional Data facing is used by the handler.

The current 1.21.10 templates are byte-identical in the tapestry marker
records and no NBT modification is required.

## Current-port comparison

| Released feature | Current port at audit start | Classification |
| --- | --- | --- |
| 17 standing + 17 wall blocks | 16 wall palette blocks only | MISSING |
| 17 tapestry BlockItems | no tapestry items | MISSING |
| `biomemakeover:tapestry` BlockEntity | absent | MISSING |
| modeled flag renderer and item renderer | absent | MISSING |
| textures, item models, loot, tags, advancement, translations | absent from port | MISSING |
| horizontal `FACING` property | present on 16 wall substrates | PARTIAL |
| wall support and neighbor survival | absent | MISSING |
| vanilla rotation/mirror transforms | inherited property only; no released tapestry implementation | PARTIAL |
| Mansion marker dispatch | marker is cleared but `tapestry` is inert | MISSING |
| 168 template marker records | present and unchanged | PASS |
| Mansion rotation/mirror source data | preserved | PASS |
| save/reload tapestry state | not applicable to the current marker-only substrate | MISSING |

The existing 16 wall registrations/assets are therefore structural palette
dependencies, not gameplay parity. No Mythas Enhancement is part of the
implementation scope.

## Authorized implementation boundary

The smallest released-parity implementation is to restore the released
tapestry block/item family, shared block entity and renderer, released
resources/data, and the `tapestry` Mansion marker dispatch. It must not alter
the 168 NBT files or any accepted fluid, crop, terrain, layout, loot, fence,
potted-plant, runtime-metadata, or Mythas design behavior.

The temporary Prism diagnostic, if retained during testing, is trace-gated
and limited to 16 representative placements per Mansion. It is not a gameplay
contract and must be removed or reduced to a negligible guard after runtime
acceptance.
