package party.lemons.biomemakeover.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.feature.VegetationPatchFeature;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;

import java.util.function.Predicate;

/** The released BM patch behavior, retained as a distinct codec ID. */
public final class GrassPatchFeature extends VegetationPatchFeature {
    public GrassPatchFeature(Codec<VegetationPatchConfiguration> codec) { super(codec); }

    @Override
    protected boolean placeGround(WorldGenLevel level, VegetationPatchConfiguration config,
                                  Predicate<BlockState> replaceable, RandomSource random,
                                  BlockPos.MutableBlockPos pos, int depth) {
        for (int i=0;i<depth;i++) {
            BlockState current=level.getBlockState(pos);
            BlockState ground=config.groundState.getState(random,pos);
            if (ground.is(current.getBlock())) continue;
            if (!replaceable.test(current)) return i != 0;
            if (!canRemainGrass(ground,level,pos)) ground=Blocks.DIRT.defaultBlockState();
            level.setBlock(pos,ground,2);
            BlockPos below=pos.below();
            BlockState belowState=level.getBlockState(below);
            if (belowState.is(ground.getBlock()) && !canRemainGrass(belowState,level,below))
                level.setBlock(below,Blocks.DIRT.defaultBlockState(),2);
            pos.move(config.surface.getDirection());
        }
        return true;
    }

    private static boolean canRemainGrass(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos above=pos.above();
        BlockState aboveState=level.getBlockState(above);
        if (aboveState.is(Blocks.SNOW) && aboveState.getValue(SnowLayerBlock.LAYERS)==1) return true;
        if (aboveState.getFluidState().getAmount()==8) return false;
        int blocked=LightEngine.getLightBlockInto(state,aboveState,Direction.UP,aboveState.getLightBlock());
        return blocked < 15;
    }
}
