package party.lemons.biomemakeover.init;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import party.lemons.biomemakeover.BiomeMakeover;

public final class BMWorldgen {
    private static ResourceKey<PlacedFeature> key(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, BiomeMakeover.id("dark_forest/" + name));
    }

    public static final ResourceKey<PlacedFeature> MESMERITE_UNDERGROUND = key("mesmerite_underground");
    public static final ResourceKey<PlacedFeature> WILD_MUSHROOMS = key("wild_mushrooms");
    public static final ResourceKey<PlacedFeature> BLACK_THISTLE = key("black_thistle");
    public static final ResourceKey<PlacedFeature> FOXGLOVE = key("foxglove");
    public static final ResourceKey<PlacedFeature> ITCHING_IVY = key("itching_ivy");
    public static final ResourceKey<PlacedFeature> OWL_NEST = key("owl_nest");
    public static final ResourceKey<PlacedFeature> ANCIENT_OAK = key("ancient_oak");
    public static final ResourceKey<PlacedFeature> ANCIENT_OAK_SMALL = key("ancient_oak_small");

    private BMWorldgen() {}

    public static void initialize() {
        // Temporary test weight until the owl spawn predicate is fully debugged.
        BiomeModifications.addSpawn(
            BiomeSelectors.includeByKey(Biomes.DARK_FOREST),
            MobCategory.AMBIENT,
            BMEntities.OWL,
            40,
            1,
            2
        );

        BiomeModifications.addFeature(
            BiomeSelectors.includeByKey(Biomes.DARK_FOREST),
            GenerationStep.Decoration.UNDERGROUND_ORES,
            MESMERITE_UNDERGROUND
        );

        for (ResourceKey<PlacedFeature> feature : new ResourceKey[]{
            WILD_MUSHROOMS,
            BLACK_THISTLE,
            FOXGLOVE,
            ITCHING_IVY,
            OWL_NEST,
            ANCIENT_OAK,
            ANCIENT_OAK_SMALL
        }) {
            BiomeModifications.addFeature(
                BiomeSelectors.includeByKey(Biomes.DARK_FOREST),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                feature
            );
        }
    }
}
