package party.lemons.biomemakeover.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;

/** Released swamp reed/cattail: a two-block plant whose lower half is waterloggable in shallow water. */
public class ReedBlock extends TallFlowerBlock implements LiquidBlockContainer {
    public ReedBlock(Properties properties) { super(properties); }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return super.mayPlaceOn(state,level,pos)||state.is(BlockTags.SAND)||state.is(Blocks.CLAY);
    }
    @Override protected boolean canSurvive(BlockState state,net.minecraft.world.level.LevelReader level,BlockPos pos){return state.getValue(HALF)!=DoubleBlockHalf.LOWER||level.getFluidState(pos).is(Fluids.WATER);}
    @Override protected FluidState getFluidState(BlockState state){return state.getValue(HALF)==DoubleBlockHalf.LOWER?Fluids.WATER.getSource(false):super.getFluidState(state);}
    @Override public boolean canPlaceLiquid(net.minecraft.world.entity.LivingEntity entity,BlockGetter level,BlockPos pos,BlockState state,Fluid fluid){return false;}
    @Override public boolean placeLiquid(LevelAccessor level,BlockPos pos,BlockState state,FluidState fluid){return false;}
}
