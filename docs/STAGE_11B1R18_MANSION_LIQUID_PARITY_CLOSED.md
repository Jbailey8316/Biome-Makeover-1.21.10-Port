# Stage 11B.1R.18 — Mansion liquid parity closed

Mansion templates retain the released structure/layout and the Mythas median
terrain-placement enhancement.  Template placement now unconditionally uses
Minecraft 1.21.10's `LiquidSettings.IGNORE_WATERLOGGING`, matching the native
Trial Chamber placement path.  This prevents waterlogged state inference during
placement without converting the Mansion to jigsaw generation.

The only defensive correction retained is a bounded boss-room pass over
explicitly serialized AIR cells in the active template clip.  It does not touch
omitted cells, structure voids, neighboring geology, or non-boss pieces.

The broad R17 authored-fluid correction, source closure, union mutation, and
delayed repair paths are retired from normal Mansion generation.  They remain
available only as historical code/diagnostics and are not production authority.

Same-seed Prism validation passed for the control Mansion and two additional
Mansions: normal dungeon, stairs, and boss-room regions remained dry while
surrounding terrain fluids were preserved.  Mansion fluid parity is therefore
CLOSED.  Layout metadata bookkeeping remains a separate follow-up concern.
