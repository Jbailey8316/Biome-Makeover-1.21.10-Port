# Stage 11B.1R.20 — Mansion Crop Parity

## Audit result

The Mansion marker dispatcher consumes `bonemeal` as a no-op (`MansionFeature.handleDirectionalMetadata`, `case "bonemeal", "tapestry"`). The marker position is cleared before dispatch, so no structure marker remains in the world. No bonemeal application, forced growth, new RNG, scheduled crop tick, or post-placement maturation is performed by Mansion code.

The released template states remain authoritative. Crop blocks and their serialized age/stage are placed by the normal template path; farmland is not rewritten by Mansion marker handling. The existing `BM_CROP_TRACE` is diagnostic-only and gated by `bm.mansion.trace`.

All 168 Mansion NBT templates were included in the audit inventory. No code or NBT change is required for crop parity in this stage. Fluid, terrain, layout, loot, fence, potted-plant, Rootling, tapestry, and Stage 12 behavior are unchanged.

## Decision

`bonemeal` is intentionally a no-op marker. Preserve the current behavior exactly; do not force crop growth.
