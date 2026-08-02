package party.lemons.biomemakeover.init;

import java.util.function.Predicate;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import party.lemons.biomemakeover.BiomeMakeover;

public final class BMWorldgen {
    public static final ResourceKey<PlacedFeature> MESMERITE_UNDERGROUND = key("dark_forest/mesmerite_underground");
    public static final ResourceKey<PlacedFeature> MESMERITE_BOULDER = key("dark_forest/mesmerite_boulder");
    public static final ResourceKey<PlacedFeature> MESMERITE_FISSURE = key("dark_forest/mesmerite_fissure");
    public static final ResourceKey<PlacedFeature> WILD_MUSHROOMS = key("dark_forest/wild_mushrooms");
    public static final ResourceKey<PlacedFeature> BLACK_THISTLE = key("dark_forest/black_thistle");
    public static final ResourceKey<PlacedFeature> ANCIENT_OAK = key("dark_forest/ancient_oak");
    public static final ResourceKey<PlacedFeature> ANCIENT_OAK_SMALL = key("dark_forest/ancient_oak_small");
    public static final ResourceKey<PlacedFeature> DARK_OAK_SMALL = key("dark_forest/dark_oak_small");
    public static final ResourceKey<PlacedFeature> FLOWERS = key("dark_forest/flowers");
    public static final ResourceKey<PlacedFeature> ITCHING_IVY = key("dark_forest/itching_ivy");
    public static final ResourceKey<PlacedFeature> TALL_GRASS = key("dark_forest/tall_grass");
    public static final ResourceKey<PlacedFeature> OWL_NEST = key("dark_forest/owl_nest");

    private BMWorldgen() {}

    private static ResourceKey<PlacedFeature> key(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, BiomeMakeover.id(name));
    }

    private static void addFeature(
        Predicate<BiomeSelectionContext> selector,
        GenerationStep.Decoration step,
        ResourceKey<PlacedFeature> feature
    ) {
        BiomeModifications.addFeature(selector, step, feature);
    }

    public static void initialize() {
        Predicate<BiomeSelectionContext> darkForest = BiomeSelectors.includeByKey(Biomes.DARK_FOREST);

        BiomeModifications.addSpawn(
            darkForest,
            MobCategory.CREATURE,
            BMEntities.OWL,
            40,
            1,
            3
        );

        addFeature(darkForest, GenerationStep.Decoration.UNDERGROUND_ORES, MESMERITE_UNDERGROUND);
        addFeature(darkForest, GenerationStep.Decoration.UNDERGROUND_ORES, MESMERITE_BOULDER);
        addFeature(darkForest, GenerationStep.Decoration.UNDERGROUND_ORES, MESMERITE_FISSURE);

        addFeature(darkForest, GenerationStep.Decoration.VEGETAL_DECORATION, WILD_MUSHROOMS);
        addFeature(darkForest, GenerationStep.Decoration.VEGETAL_DECORATION, BLACK_THISTLE);
        addFeature(darkForest, GenerationStep.Decoration.VEGETAL_DECORATION, FLOWERS);
        addFeature(darkForest, GenerationStep.Decoration.VEGETAL_DECORATION, ITCHING_IVY);
        addFeature(darkForest, GenerationStep.Decoration.VEGETAL_DECORATION, TALL_GRASS);
        addFeature(darkForest, GenerationStep.Decoration.VEGETAL_DECORATION, ANCIENT_OAK);
        addFeature(darkForest, GenerationStep.Decoration.VEGETAL_DECORATION, ANCIENT_OAK_SMALL);
        addFeature(darkForest, GenerationStep.Decoration.VEGETAL_DECORATION, DARK_OAK_SMALL);
        addFeature(darkForest, GenerationStep.Decoration.VEGETAL_DECORATION, OWL_NEST);
    }
}
