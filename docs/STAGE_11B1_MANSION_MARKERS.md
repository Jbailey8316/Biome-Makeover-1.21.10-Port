# Stage 11B.1 — Mansion Marker Infrastructure

The released `TemplateStructurePiece` directional-data hook is now connected
to `MansionFeature.Piece.postProcess`. Transformed marker positions and facing
are consumed exactly once after template placement.

Enabled released-safe behavior:

- Mansion loot markers assign the seven released tables lazily to generated
  containers with per-container seeds.
- `ivy` performs the released bounded support/variation pass.
- `shroom` places red or brown mushrooms.
- `spawner_spiders` configures generated spawners as spider or cave spider.
- `owl` creates the existing BM Owl with released stunted probability.
- `enemy`, `ranger`, `ravager`, `cow`, and `allay` use the released safe entity
  pools, persistence, structure spawn reason, and chance handling.
- `bonemeal` remains the released no-op (the original implementation had the
  effect disabled).

The structure data supplies safe vanilla pools for non-boss markers while the
Stone Golem pool remains empty. Boss, arena, golem, and full tapestry systems
remain deferred; tapestry metadata is consumed without activating gameplay.
No loot marker is re-fired on reload because processing occurs during piece
placement only.

The five Mansion loot dependencies resolve to zero missing registrations.
Rootling's `LegacyRandomSource` thread-safety defect and dungeon fluid/water
parity remain separate unresolved issues and were not changed.
