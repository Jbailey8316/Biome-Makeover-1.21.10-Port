package party.lemons.biomemakeover.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.worldgen.HugeGlowshroomFeature;
import party.lemons.biomemakeover.worldgen.OrangeGlowshroomFeature;
import party.lemons.biomemakeover.worldgen.GrassPatchFeature;
import party.lemons.biomemakeover.worldgen.BalsaTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import party.lemons.biomemakeover.worldgen.PaydirtFeature;
import party.lemons.biomemakeover.worldgen.SaguaroCactusFeature;
import party.lemons.biomemakeover.worldgen.SurfaceFossilFeature;
import party.lemons.biomemakeover.level.feature.PeatFeature;
import party.lemons.biomemakeover.level.feature.ReedFeature;
import party.lemons.biomemakeover.level.feature.WaterTreeFeature;
import party.lemons.biomemakeover.level.feature.foliage.CypressTrunkPlacer;
import party.lemons.biomemakeover.level.feature.foliage.WillowTrunkPlacer;
import party.lemons.biomemakeover.level.feature.foliage.WillowFoliagePlacer;
import party.lemons.biomemakeover.level.feature.foliage.HangingLeavesDecorator;
import party.lemons.biomemakeover.level.feature.foliage.WillowingBranchDecorator;
import party.lemons.biomemakeover.level.feature.foliage.AncientOakTrunkPlacer;
import party.lemons.biomemakeover.level.feature.foliage.IvyDecorator;
import party.lemons.biomemakeover.level.feature.FissureFeature;
import party.lemons.biomemakeover.level.feature.ItchingIvyFeature;
import party.lemons.biomemakeover.level.feature.MesmeriteBoulderFeature;
import party.lemons.biomemakeover.level.feature.MesmermiteUndergroundFeature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public final class BMFeatures {
    public static final Feature<HugeMushroomFeatureConfiguration> HUGE_PURPLE_GLOWSHROOM = Registry.register(
        BuiltInRegistries.FEATURE, BiomeMakeover.id("huge_purple_glowshroom"),
        new HugeGlowshroomFeature(HugeMushroomFeatureConfiguration.CODEC, false, false));
    public static final Feature<HugeMushroomFeatureConfiguration> HUGE_GREEN_GLOWSHROOM = Registry.register(
        BuiltInRegistries.FEATURE, BiomeMakeover.id("huge_green_glowshroom"),
        new HugeGlowshroomFeature(HugeMushroomFeatureConfiguration.CODEC, true, false));
    public static final Feature<HugeMushroomFeatureConfiguration> HUGE_ORANGE_GLOWSHROOM = Registry.register(
        BuiltInRegistries.FEATURE, BiomeMakeover.id("huge_orange_glowshroom"),
        new HugeGlowshroomFeature(HugeMushroomFeatureConfiguration.CODEC, false, true));
    public static final Feature<ProbabilityFeatureConfiguration> ORANGE_GLOWSHROOM = Registry.register(
        BuiltInRegistries.FEATURE, BiomeMakeover.id("orange_glowshroom"),
        new OrangeGlowshroomFeature(ProbabilityFeatureConfiguration.CODEC));
    public static final Feature<VegetationPatchConfiguration> GRASS_PATCH = Registry.register(
        BuiltInRegistries.FEATURE, BiomeMakeover.id("grass_patch"), new GrassPatchFeature(VegetationPatchConfiguration.CODEC));
    public static final TrunkPlacerType<BalsaTrunkPlacer> BLIGHTED_BALSA_TRUNK = Registry.register(
        BuiltInRegistries.TRUNK_PLACER_TYPE, BiomeMakeover.id("blighted_balsa"), new TrunkPlacerType<>(BalsaTrunkPlacer.CODEC));
    public static final Feature<NoneFeatureConfiguration> PAYDIRT = Registry.register(BuiltInRegistries.FEATURE,
        BiomeMakeover.id("paydirt"), new PaydirtFeature(NoneFeatureConfiguration.CODEC));
    public static final Feature<NoneFeatureConfiguration> SAGUARO_CACTUS = Registry.register(BuiltInRegistries.FEATURE,
        BiomeMakeover.id("saguaro_cactus"), new SaguaroCactusFeature(NoneFeatureConfiguration.CODEC));
    public static final Feature<NoneFeatureConfiguration> SURFACE_FOSSIL = Registry.register(BuiltInRegistries.FEATURE,
        BiomeMakeover.id("surface_fossil"), new SurfaceFossilFeature(NoneFeatureConfiguration.CODEC));
    public static final Feature<NoneFeatureConfiguration> PEAT = Registry.register(BuiltInRegistries.FEATURE,
        BiomeMakeover.id("peat"), new PeatFeature(NoneFeatureConfiguration.CODEC));
    public static final Feature<NoneFeatureConfiguration> REEDS = Registry.register(BuiltInRegistries.FEATURE,
        BiomeMakeover.id("reeds"), new ReedFeature(NoneFeatureConfiguration.CODEC));
    public static final Feature<TreeConfiguration> WATER_TREE = Registry.register(BuiltInRegistries.FEATURE,
        BiomeMakeover.id("water_tree"), new WaterTreeFeature(TreeConfiguration.CODEC));
    public static final TrunkPlacerType<CypressTrunkPlacer> CYPRESS_TRUNK = Registry.register(BuiltInRegistries.TRUNK_PLACER_TYPE,
        BiomeMakeover.id("swamp_cypress"), new TrunkPlacerType<>(CypressTrunkPlacer.CODEC));
    public static final TrunkPlacerType<WillowTrunkPlacer> WILLOW_TRUNK = Registry.register(BuiltInRegistries.TRUNK_PLACER_TYPE,
        BiomeMakeover.id("willow"), new TrunkPlacerType<>(WillowTrunkPlacer.CODEC));
    public static final FoliagePlacerType<WillowFoliagePlacer> WILLOW_FOLIAGE = Registry.register(BuiltInRegistries.FOLIAGE_PLACER_TYPE,
        BiomeMakeover.id("willow_foliage"), new FoliagePlacerType<>(WillowFoliagePlacer.CODEC));
    public static final TreeDecoratorType<HangingLeavesDecorator> HANGING_LEAVES_DECORATOR = Registry.register(BuiltInRegistries.TREE_DECORATOR_TYPE,
        BiomeMakeover.id("hanging_leaves_decorator"), new TreeDecoratorType<>(HangingLeavesDecorator.CODEC));
    public static final TreeDecoratorType<WillowingBranchDecorator> WILLOWING_BRANCH_DECORATOR = Registry.register(BuiltInRegistries.TREE_DECORATOR_TYPE,
        BiomeMakeover.id("willowing_branch_decorator"), new TreeDecoratorType<>(WillowingBranchDecorator.CODEC));
    public static final TrunkPlacerType<AncientOakTrunkPlacer> ANCIENT_OAK_TRUNK = Registry.register(BuiltInRegistries.TRUNK_PLACER_TYPE,
        BiomeMakeover.id("ancient_oak"), new TrunkPlacerType<>(AncientOakTrunkPlacer.CODEC));
    public static final Feature<FissureFeature.Config> FISSURE = Registry.register(BuiltInRegistries.FEATURE,
        BiomeMakeover.id("fissure"), new FissureFeature(FissureFeature.Config.CODEC));
    public static final Feature<NoneFeatureConfiguration> ITCHING_IVY = Registry.register(BuiltInRegistries.FEATURE,
        BiomeMakeover.id("itching_ivy"), new ItchingIvyFeature(NoneFeatureConfiguration.CODEC));
    public static final Feature<BlockStateConfiguration> MESMERITE_BOULDER = Registry.register(BuiltInRegistries.FEATURE,
        BiomeMakeover.id("mesmerite_boulder"), new MesmeriteBoulderFeature(BlockStateConfiguration.CODEC));
    public static final Feature<OreConfiguration> MESMERITE_UNDERGROUND = Registry.register(BuiltInRegistries.FEATURE,
        BiomeMakeover.id("mesmerite_underground"), new MesmermiteUndergroundFeature(OreConfiguration.CODEC));
    public static final TreeDecoratorType<IvyDecorator> IVY_DECORATOR = Registry.register(BuiltInRegistries.TREE_DECORATOR_TYPE,
        BiomeMakeover.id("ivy"), new TreeDecoratorType<>(IvyDecorator.CODEC));

    private BMFeatures() {}
    public static void initialize() {}
}
