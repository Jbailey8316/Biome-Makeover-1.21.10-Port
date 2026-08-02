package party.lemons.biomemakeover.init;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.init.BMEntities;

public final class BMWorldgen {
    public static final ResourceKey<PlacedFeature> MESMERITE_UNDERGROUND = ResourceKey.create(
        Registries.PLACED_FEATURE,
        BiomeMakeover.id("dark_forest/mesmerite_underground")
    );

    public static final ResourceKey<PlacedFeature> WILD_MUSHROOMS = ResourceKey.create(
        Registries.PLACED_FEATURE,
        BiomeMakeover.id("dark_forest/wild_mushrooms")
    );

    public static final ResourceKey<PlacedFeature> BLACK_THISTLE = ResourceKey.create(
        Registries.PLACED_FEATURE,
        BiomeMakeover.id("dark_forest/black_thistle")
    );

    private BMWorldgen() {
    }

    public static void initialize() {
        BiomeModifications.addSpawn(
            BiomeSelectors.includeByKey(Biomes.DARK_FOREST),
            MobCategory.CREATURE, BMEntities.OWL, 10, 1, 1
        );

        // Native Dark Forest wildlife additions
        BiomeModifications.addSpawn(
            BiomeSelectors.includeByKey(Biomes.DARK_FOREST),
            MobCategory.CREATURE, EntityType.FOX, 10, 1, 2
        );

        BiomeModifications.addSpawn(
            BiomeSelectors.includeByKey(Biomes.DARK_FOREST),
            MobCategory.CREATURE, EntityType.RABBIT, 8, 1, 3
        );
        BiomeModifications.addFeature(
            BiomeSelectors.includeByKey(Biomes.DARK_FOREST),
            GenerationStep.Decoration.UNDERGROUND_ORES,
            MESMERITE_UNDERGROUND
        );

        BiomeModifications.addFeature(
            BiomeSelectors.includeByKey(Biomes.DARK_FOREST),
            GenerationStep.Decoration.VEGETAL_DECORATION,
            WILD_MUSHROOMS
        );

        BiomeModifications.addFeature(
            BiomeSelectors.includeByKey(Biomes.DARK_FOREST),
            GenerationStep.Decoration.VEGETAL_DECORATION,
            BLACK_THISTLE
        );
    }
}
