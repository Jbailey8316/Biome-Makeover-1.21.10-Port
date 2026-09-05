# Stage 11B.1R.20R.11 — Mansion Parity Closure

## Closed status

Mansion fluid parity and crop parity are closed after the accepted R20R.10
Prism runs. The accepted systems are frozen: native `LiquidSettings.IGNORE_WATERLOGGING`,
the authoritative C5–C8 lifecycle, serialized crop restoration, and D20S/D45S
retention at 400/900 server ticks.

## Fluid root cause and correction

Released Biome Makeover 1.20.1 handles Mansion DATA markers: `boss` finishes
as AIR and `arena_pos` finishes as smooth quartz. The 1.21.10 port had made
`handleDataMarker` a no-op, so natural source fluid could survive at those
metadata coordinates.

R20R.9 mapped the internal flooded source cells to the released `boss` and
`arena_pos` marker definitions. R20R.10 restored only those released marker
states. Prism confirmed 7/7 and 11/11 source-bearing marker corrections,
zero fluid-bearing markers afterward, preserved external aquifer water, and
dry boss rooms through D45S.

## Crop closure

Crop targets are collected from released serialized states across all crop
rooms and restored only after complete Mansion placement. States and ages are
not randomized or grown. Prism confirmed complete restoration with
`missingAfter=0` for independent layouts.

The Mansion fluid and crop parity threads are CLOSED. R20R.4 remains retained
as historical forensic evidence.
# Permanent Mythas safety note

The released 1.20.1 Mansion permits a latent ordering hazard: biome tree
decoration can run after Mansion structure placement and structure-spawned
mobs. The port includes a separate **MYTHAS ENHANCEMENT** that rejects tree
features whose root/trunk-base columns intersect an actively generating
Biome Makeover Mansion footprint. This preserves released Mansion templates
and mob-marker semantics, changes no NBT, and does not suppress unrelated
vegetation or trees rooted outside the footprint.
