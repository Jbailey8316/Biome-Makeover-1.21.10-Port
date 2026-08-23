package party.lemons.biomemakeover.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import party.lemons.biomemakeover.init.BMBlocks;

public final class OrangeGlowshroomFeature extends Feature<ProbabilityFeatureConfiguration> {
    public OrangeGlowshroomFeature(Codec<ProbabilityFeatureConfiguration> codec) { super(codec); }

    @Override public boolean place(FeaturePlaceContext<ProbabilityFeatureConfiguration> context) {
        var random=context.random(); var level=context.level(); var origin=context.origin();
        double angle=random.nextDouble()*Math.PI*2.0, radius=9.0*random.nextDouble();
        int x=origin.getX()+(int)Math.round(Math.cos(angle)*radius), z=origin.getZ()+(int)Math.round(Math.sin(angle)*radius);
        BlockPos pos=new BlockPos(x,level.getHeight(Heightmap.Types.OCEAN_FLOOR,x,z),z);
        if (!level.getBlockState(pos).is(Blocks.WATER)) return false;
        var state=BMBlocks.ORANGE_GLOWSHROOM.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED,true);
        if (!state.canSurvive(level,pos)) return false;
        level.setBlock(pos,state,2); return true;
    }
}
