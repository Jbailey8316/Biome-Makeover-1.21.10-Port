# Stage 4 Status — Dark Forest Mesmerite World Generation

## Added

- Data-driven configured feature: `biomemakeover:dark_forest/mesmerite_underground`
- Data-driven placed feature using the same original registry path
- Fabric biome modification restricted to `minecraft:dark_forest`
- Underground placement from the dimension bottom through Y=64
- One generation attempt per four eligible chunks
- Vanilla ore feature implementation replacing stone and deepslate ore-replaceable blocks

## Compatibility behavior

World generation only runs while a chunk is first generated. Existing chunks are not modified or retrofitted.

## Deliberate Stage 4 limits

The original 1.20 implementation used a custom large-vein feature and could attach Illunite clusters. Stage 4 uses Minecraft's vanilla ore feature as a stable 1.21.10 proof of world-generation registration. The custom fissure, boulder, and Illunite systems remain for later stages.
