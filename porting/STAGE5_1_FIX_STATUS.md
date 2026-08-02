# Stage 5.1 Fix Status

User validation of Stage 5 found excessive density, floating/water placements, and opaque black texture backgrounds.

Changes:
- CUTOUT block render layer registered client-side.
- Added standalone WildMushroomBlock extending BushBlock.
- Added survival predicate to configured feature.
- Changed heightmap to MOTION_BLOCKING_NO_LEAVES.
- Reduced patch frequency and spread.

Existing generated chunks are not retroactively cleaned.
