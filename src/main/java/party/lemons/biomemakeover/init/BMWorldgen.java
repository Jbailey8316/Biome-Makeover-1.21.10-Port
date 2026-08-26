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
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.init.BMEntities;

public final class BMWorldgen {
    private static final TagKey<Biome> SWAMPS = TagKey.create(Registries.BIOME, BiomeMakeover.id("swamps"));
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
    public static final ResourceKey<PlacedFeature> BADLANDS_BARREL_CACTUS = badlands("barrel_cactus");
    public static final ResourceKey<PlacedFeature> BADLANDS_SAGUARO_CACTUS = badlands("saguaro_cactus");
    public static final ResourceKey<PlacedFeature> BADLANDS_PAYDIRT = badlands("paydirt");
    public static final ResourceKey<PlacedFeature> BADLANDS_SURFACE_FOSSIL = badlands("surface_fossil");
    public static final ResourceKey<PlacedFeature> SWAMP_BIG_MUSHROOMS = swamp("big_mushrooms");
    public static final ResourceKey<PlacedFeature> SWAMP_FLOWERS = swamp("flowers");
    public static final ResourceKey<PlacedFeature> SWAMP_PADS = swamp("pads");
    public static final ResourceKey<PlacedFeature> SWAMP_PEAT = swamp("peat");
    public static final ResourceKey<PlacedFeature> SWAMP_REEDS = swamp("reeds");
    public static final ResourceKey<PlacedFeature> SWAMP_CYPRESS_TREES = swamp("swamp_cypress_trees");
    public static final ResourceKey<PlacedFeature> SWAMP_WILLOW_TREES = swamp("willow_trees");
    public static final ResourceKey<PlacedFeature> DARK_FOREST_GRASS = darkForest("grass");
    public static final ResourceKey<PlacedFeature> DARK_FOREST_TALL_GRASS = darkForest("tall_grass");
    public static final ResourceKey<PlacedFeature> DARK_FOREST_FLOWERS = darkForest("flowers");
    public static final ResourceKey<PlacedFeature> DARK_FOREST_ITCHING_IVY = darkForest("itching_ivy");
    public static final ResourceKey<PlacedFeature> DARK_FOREST_TREES = darkForest("trees");
    public static final ResourceKey<PlacedFeature> DARK_FOREST_WILD_MUSHROOMS = darkForest("wild_mushrooms");
    public static final ResourceKey<PlacedFeature> DARK_FOREST_FISSURE = darkForest("mesmerite_fissure");

    private BMWorldgen() {
    }

    public static void initialize() {
        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(Biomes.MUSHROOM_FIELDS),
            MobCategory.WATER_AMBIENT, BMEntities.GLOWFISH, 7, 2, 7);
        BiomeModifications.addSpawn(BiomeSelectors.tag(net.minecraft.tags.BiomeTags.IS_BADLANDS),
            MobCategory.CREATURE, BMEntities.SCUTTLER, 4, 1, 2);
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
        addBadlands(GenerationStep.Decoration.VEGETAL_DECORATION, BADLANDS_BARREL_CACTUS);
        addBadlands(GenerationStep.Decoration.VEGETAL_DECORATION, BADLANDS_SAGUARO_CACTUS);
        addBadlands(GenerationStep.Decoration.UNDERGROUND_DECORATION, BADLANDS_PAYDIRT);
        addBadlands(GenerationStep.Decoration.SURFACE_STRUCTURES, BADLANDS_SURFACE_FOSSIL);
        // Released order: remove vanilla swamp trees, then install the complete BM ecology.
        BiomeModifications.create(BiomeMakeover.id("swamp/remove_vanilla_trees"))
            .add(net.fabricmc.fabric.api.biome.v1.ModificationPhase.REMOVALS, BiomeSelectors.tag(SWAMPS),
                context -> context.getGenerationSettings().removeFeature(VegetationPlacements.TREES_SWAMP));
        addSwamp(GenerationStep.Decoration.VEGETAL_DECORATION, SWAMP_BIG_MUSHROOMS);
        addSwamp(GenerationStep.Decoration.VEGETAL_DECORATION, SWAMP_FLOWERS);
        addSwamp(GenerationStep.Decoration.VEGETAL_DECORATION, SWAMP_PADS);
        addSwamp(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, SWAMP_PEAT);
        addSwamp(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, SWAMP_REEDS);
        addSwamp(GenerationStep.Decoration.VEGETAL_DECORATION, SWAMP_CYPRESS_TREES);
        addSwamp(GenerationStep.Decoration.VEGETAL_DECORATION, SWAMP_WILLOW_TREES);
        BiomeModifications.addSpawn(BiomeSelectors.tag(SWAMPS), MobCategory.MONSTER, BMEntities.DECAYED, 60, 1, 1);
        BiomeModifications.addSpawn(BiomeSelectors.tag(SWAMPS), MobCategory.AMBIENT, BMEntities.DRAGONFLY, 20, 3, 8);
        BiomeModifications.addSpawn(BiomeSelectors.tag(SWAMPS), MobCategory.AMBIENT, BMEntities.LIGHTNING_BUG, 20, 1, 1);
        BiomeModifications.addSpawn(
            BiomeSelectors.includeByKey(Biomes.DARK_FOREST),
            MobCategory.CREATURE, BMEntities.OWL, 20, 1, 4
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
        addDarkForest(GenerationStep.Decoration.VEGETAL_DECORATION, DARK_FOREST_GRASS);
        addDarkForest(GenerationStep.Decoration.VEGETAL_DECORATION, DARK_FOREST_TALL_GRASS);
        addDarkForest(GenerationStep.Decoration.VEGETAL_DECORATION, DARK_FOREST_FLOWERS);
        addDarkForest(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, DARK_FOREST_ITCHING_IVY);
        // These apparently unusual steps are the released 1.20.1 ordering contract.
        addDarkForest(GenerationStep.Decoration.UNDERGROUND_ORES, DARK_FOREST_TREES);
        addDarkForest(GenerationStep.Decoration.UNDERGROUND_ORES, DARK_FOREST_WILD_MUSHROOMS);
        addDarkForest(GenerationStep.Decoration.LOCAL_MODIFICATIONS, DARK_FOREST_FISSURE);
    }

    private static void addMushroom(GenerationStep.Decoration step, ResourceKey<PlacedFeature> feature) {
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(Biomes.MUSHROOM_FIELDS), step, feature);
    }
    private static ResourceKey<PlacedFeature> badlands(String path) { return ResourceKey.create(Registries.PLACED_FEATURE, BiomeMakeover.id("badlands/" + path)); }
    private static void addBadlands(GenerationStep.Decoration step, ResourceKey<PlacedFeature> feature) {
        BiomeModifications.addFeature(BiomeSelectors.tag(net.minecraft.tags.BiomeTags.IS_BADLANDS), step, feature);
    }
    private static ResourceKey<PlacedFeature> swamp(String path) { return ResourceKey.create(Registries.PLACED_FEATURE, BiomeMakeover.id("swamp/" + path)); }
    private static void addSwamp(GenerationStep.Decoration step, ResourceKey<PlacedFeature> feature) {
        BiomeModifications.addFeature(BiomeSelectors.tag(SWAMPS), step, feature);
    }
    private static ResourceKey<PlacedFeature> darkForest(String path) { return ResourceKey.create(Registries.PLACED_FEATURE, BiomeMakeover.id("dark_forest/" + path)); }
    private static void addDarkForest(GenerationStep.Decoration step, ResourceKey<PlacedFeature> feature) {
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(Biomes.DARK_FOREST), step, feature);
    }
}
