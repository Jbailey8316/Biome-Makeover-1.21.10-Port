# DarkForest Beta 0.4.11.1 - Installer Fix

Fixes the Windows `WinError 32` failure caused when the patch is extracted
directly into the repository root and the installer tries to copy each patch
file onto itself.

This retains all 0.4.11 source/resource changes.

Run:

```bash
bash apply-darkforest-beta-0.4.11.sh
./gradlew clean build
```

---

# Dark Forest Beta 0.4.11

Actual source/resource patch.

- Owl natural spawning: switches the biome spawn entry to AMBIENT for active nighttime spawning and replaces the impossible canopy-only predicate with valid surface/leaves/log support.
- Owl nests: may only generate in air directly above leaves or logs; water and ground placements are rejected.
- Black Thistle: slowdown and damage now run from the lower half that players actually intersect.
- Itching Ivy: proper VineBlock subclass, berry-bush slowdown/damage, cutout registration, original transparent texture, corrected wall-facing blockstate, and vertical wall worldgen instead of floor panels.

Ancient Oak boat/chest boat are not faked with a vanilla boat entity in this patch. They require the proper custom boat entity/type and renderer so the placed boat is genuinely Ancient Oak rather than a renamed Dark Oak boat.
