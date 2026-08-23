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
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

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

    private BMFeatures() {}
    public static void initialize() {}
}
