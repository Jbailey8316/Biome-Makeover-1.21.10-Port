package party.lemons.biomemakeover.init;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.entity.MobCategory;
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
            MobCategory.CREATURE, BMEntities.OWL, 2, 1, 2
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
