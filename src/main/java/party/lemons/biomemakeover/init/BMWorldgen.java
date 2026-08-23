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
    private static ResourceKey<PlacedFeature> mushroom(String path) {
        return ResourceKey.create(Registries.PLACED_FEATURE, BiomeMakeover.id("mushroom_fields/" + path));
    }
    public static final ResourceKey<PlacedFeature> UNDERGROUND_MYCELIUM = mushroom("underground_mycelium");
    public static final ResourceKey<PlacedFeature> BLIGHTED_BALSA_TREES = mushroom("blighted_balsa_trees");
    public static final ResourceKey<PlacedFeature> GREEN_GLOWSHROOMS = mushroom("green_glowshrooms");
    public static final ResourceKey<PlacedFeature> PURPLE_GLOWSHROOMS = mushroom("purple_glowshrooms");
    public static final ResourceKey<PlacedFeature> ORANGE_GLOWSHROOMS = mushroom("orange_glowshrooms");
    public static final ResourceKey<PlacedFeature> MYCELIUM_SPROUTS = mushroom("mycelium_sprouts");
    public static final ResourceKey<PlacedFeature> MYCELIUM_ROOTS = mushroom("mycelium_roots");
    public static final ResourceKey<PlacedFeature> UNDERGROUND_HUGE_GLOWSHROOMS = mushroom("underground_huge_glowshrooms");
    public static final ResourceKey<PlacedFeature> TALL_BROWN_MUSHROOMS = mushroom("tall_brown_mushrooms");
    public static final ResourceKey<PlacedFeature> TALL_RED_MUSHROOMS = mushroom("tall_red_mushrooms");
    public static final ResourceKey<PlacedFeature> MUSHROOM_FIELD_WILD_MUSHROOMS = mushroom("wild_mushrooms");
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
        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(Biomes.MUSHROOM_FIELDS),
            MobCategory.WATER_AMBIENT, BMEntities.GLOWFISH, 7, 2, 7);
        addMushroom(GenerationStep.Decoration.UNDERGROUND_DECORATION, UNDERGROUND_MYCELIUM);
        addMushroom(GenerationStep.Decoration.FLUID_SPRINGS, BLIGHTED_BALSA_TREES);
        addMushroom(GenerationStep.Decoration.VEGETAL_DECORATION, GREEN_GLOWSHROOMS);
        addMushroom(GenerationStep.Decoration.VEGETAL_DECORATION, PURPLE_GLOWSHROOMS);
        addMushroom(GenerationStep.Decoration.VEGETAL_DECORATION, ORANGE_GLOWSHROOMS);
        addMushroom(GenerationStep.Decoration.VEGETAL_DECORATION, MYCELIUM_SPROUTS);
        addMushroom(GenerationStep.Decoration.VEGETAL_DECORATION, MYCELIUM_ROOTS);
        addMushroom(GenerationStep.Decoration.VEGETAL_DECORATION, UNDERGROUND_HUGE_GLOWSHROOMS);
        addMushroom(GenerationStep.Decoration.VEGETAL_DECORATION, TALL_BROWN_MUSHROOMS);
        addMushroom(GenerationStep.Decoration.VEGETAL_DECORATION, TALL_RED_MUSHROOMS);
        addMushroom(GenerationStep.Decoration.VEGETAL_DECORATION, MUSHROOM_FIELD_WILD_MUSHROOMS);
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

    private static void addMushroom(GenerationStep.Decoration step, ResourceKey<PlacedFeature> feature) {
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(Biomes.MUSHROOM_FIELDS), step, feature);
    }
}
