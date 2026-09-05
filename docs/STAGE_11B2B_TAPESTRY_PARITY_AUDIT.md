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

## R.1 renderer and advancement repair

The Prism wall-tapestry failure was traced to model geometry, not texture
lookup. Released 1.20.1 uses one custom 64x64 flag model with three parts:

- `flag`: cloth at `(-10, 0, -2)`, size `20x35x1`, with three lower tassels;
- `pole`: `(-1, -30, -1)`, size `2x42x2`;
- `bar`: `(-10, -32, -1)`, size `20x2x2`.

The released world transform is exact: standing form translates to
`(0.5, 0.5, 0.5)` and rotates around positive Y by
`-(ROTATION_16 * 360 / 16)` degrees. Wall form translates to
`(0.5, -0.1666666716337204, 0.5)`, rotates around positive Y by
`-FACING.toYRot()` degrees, then translates `(0, -0.3125, -0.4375)`.
Both forms then scale `(0.66, -0.66, -0.66)`. The wall form hides the pole
but retains the bar and cloth. The port previously used vanilla `BannerModel`
geometry, whose pivot/orientation contract differs from the released model;
R.1 restores the released custom geometry while retaining the released
directional math.

R.1 also restores the released `biomemakeover:biomemakeover/mansion` parent
advancement under the modern singular `advancement` resource directory. The
all-tapestries advancement retains its 17 released item predicates; the load
failure was caused by the missing parent resource, not by changing tapestry
IDs.

## R.3 spatial audit

The released marker path was compared directly with the port. The released
and current semantics are the same: directional metadata is consumed at the
marker position, the marker is replaced in place with AIR, wall tapestries use
the marker direction’s opposite as their horizontal `FACING`, standing forms
use a random `ROTATION_16`, and block placement uses update flags `3`. Piece
rotation/mirror is already applied by the transformed directional block state
before this handler receives the position and direction. No placement offset,
second position transform, or second direction rotation was found, so R.3
does not alter marker placement.

R.3 adds two trace-gated observations. At Mansion readiness,
`BM_TAPESTRY_FINAL_SUMMARY` audits the final block/support state and emits at
most 16 failing `BM_TAPESTRY_FINAL_STATE` records. On the client,
`BM_TAPESTRY_SPATIAL` applies the actual post-transform PoseStack matrix to the
released local flag normal, cloth center, pole center, and bar center. This
records the wall normal dot product and support-face distance without changing
placement or rendering. Generation-time support observations are not treated
as final-state failures.

The temporary client diagnostic is gated by `-Dbm.mansion.trace=true`, emits
at most 16 instance records, and reports the selected form, facing/rotation,
texture, transform, and support block. It is diagnostic-only and does not
alter tapestry behavior.

## R.2 texture binding and Mansion criterion repair

The R.1 PNG audit found no asset defect: all 17 released textures are 64x64,
contain non-white opaque pixels, and the inspected adjudicator, red, black, and
light-blue files match the released SHA-256 bytes. The R.1 custom model also
retained the released UV coordinates. The repair therefore keeps the released
`entitySolid(ResourceLocation)` pipeline and submits the released `flag`,
`pole`, and `bar` parts directly through the modern `SubmitNodeCollector`,
instead of relying on the deferred whole-model submission to bind the selected
variant texture. A trace-gated `BM_TAPESTRY_TEXTURE_BIND` record reports the
actual ResourceLocation, render type, UV summary, and resource presence/size.

The released 1.20.1 advancement uses a location predicate for the Mansion.
In 1.21.10 the location predicate field is `structures` (a one-element list),
not the legacy singular `structure` field. The singular field was ignored by
the newer codec, leaving an empty location predicate that granted on login.
R.2 changes only that field to `"structures": ["biomemakeover:mansion"]`.
The parent remains hidden=false, toast/chat enabled, and the all-tapestries
advancement retains its 17 released item predicates and Mansion parent.

## R.5.1 production dataflow contract

The production path carries both `serializedFacing` and the transformed
semantic `facing` into `handleDirectionalMetadata`, then into
`generateTapestry`. The wall state write derives directly from that transformed
value: `setValue(FACING, facing.getOpposite())`. Immediately after the write,
`BM_TAPESTRY_PLACED_STATE` reads the block state back from the world and records
the requested and actual facing, support block, and survival result. This closes
the raw → transformed → requested → actual-world-state contract for Prism.

## R.4 marker/support forensic result

The released NBT audit covers all 56 `metadata=tapestry` occurrences. In every
wall case, the serialized Directional Data `FACING` points toward the adjacent
architectural backing cell: 56 point toward backing, 0 point opposite, 0 have
no adjacent backing, and 0 are ambiguous. The released callback receives that
direction from the transformed Directional Data block state; the released
handler clears the marker in place, then creates the wall tapestry at the same
position with `FACING = direction.opposite()`. The current port has the same
callback source, in-place position, opposite operation, and update flag `3`.

The R.4 runtime result of 20 unsupported tapestries therefore cannot be
explained by a simple marker-direction inversion from the serialized contract.
`BM_TAPESTRY_MARKER_DIRECTION` records the callback state and all six adjacent
world blocks (capped at 24) so Prism can distinguish a transformed-state or
placement-order discrepancy from a later support-state change.

The R.3 spatial diagnostic had a unit error: model-part pixel coordinates were
passed to matrix transforms as blocks. R.4 converts the flag, pole, and bar
reference points from pixels to block units before applying the actual render
matrix. Production renderer transforms are unchanged.

## R.5 rotated-piece direction repair

R.4 runtime evidence proved that the callback paired transformed marker
positions with raw serialized `FACING`. R.5 transforms the marker state once
using the native state operation order `mirror(pieceMirror).rotate(pieceRotation)`
and reads the resulting `FACING`; it does not transform the world position or
the final tapestry state again. `FACING = transformedMarkerFacing.opposite()`
remains the released wall-tapestry rule.

Mansion pieces currently use `Mirror.NONE`; the implementation retains the
native mirror step for future mirrored pieces. The direction contract is
NONE unchanged, clockwise quarter-turn, 180-degree reversal, and
counter-clockwise quarter-turn according to the native `Rotation` operation.

The final summary separates wall and standing counts. The spatial trace
subtracts the transformed matrix origin before reporting block-relative cloth,
pole, and bar coordinates. No template or production renderer transform was
changed.
# R.6 — Survival drop parity

The final released 1.20.1 source registers each wall tapestry with
`dropsLike(standingTapestry)`. Each standing/wall pair therefore shares one
inventory item, and both forms use the standing block's block loot table. The
released resource set contains 17 `data/biomemakeover/loot_tables/blocks/*_tapestry.json`
tables, each yielding its matching tapestry item under the normal
`survives_explosion` condition; there are no separate wall tables. Support
loss follows the wall block's ordinary block removal/drop path.

The 1.21.10 port already had the shared `StandingAndWallBlockItem` and the 17
standing tables, but registered wall blocks as no-item blocks without
`dropsLike`. Consequently a survival break of a wall form resolved no wall
loot table and dropped nothing. R.6 passes the corresponding standing block to
the wall properties' `dropsLike` call. No wall loot tables, recipes, or new
drop behavior are added. A trace-gated, capped `BM_TAPESTRY_DROP` observation
records the actual block loot resolution when a tapestry is broken.
