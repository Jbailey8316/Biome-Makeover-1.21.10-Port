# Stage 1 status

## Completed

- Converted to a single Fabric-only project.
- Targeted Minecraft 1.21.10 and Java 21.
- Removed Forge and Architectury from the active build.
- Removed private GitHub package credentials.
- Removed archived Taniwha as a binary dependency.
- Added server/common and client bootstrap entrypoints.
- Preserved original MIT license and logo.

## Deliberately not restored yet

- Blocks and items
- Entities and renderers
- World generation and biome modifications
- Structures
- Networking
- Screens and menus
- Original mixins and access widener
- Taniwha-derived helpers

## Next vertical slice

Badlands content is a likely first slice because it can exercise blocks, plants, features, entity registration, rendering, loot, and biome modification without requiring every system at once. Final selection will follow source dependency analysis.
