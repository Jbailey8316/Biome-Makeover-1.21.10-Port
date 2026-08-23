package party.lemons.biomemakeover.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import party.lemons.biomemakeover.block.SaguaroCactusBlock;
import party.lemons.biomemakeover.init.BMBlocks;

public final class SaguaroCactusFeature extends Feature<NoneFeatureConfiguration> {
    public SaguaroCactusFeature(Codec<NoneFeatureConfiguration> codec) { super(codec); }
    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        return SaguaroCactusBlock.generateCactus(BMBlocks.SAGUARO_CACTUS, context.level(), context.random().nextBoolean(),
            context.origin(), context.random(), false);
    }
}
