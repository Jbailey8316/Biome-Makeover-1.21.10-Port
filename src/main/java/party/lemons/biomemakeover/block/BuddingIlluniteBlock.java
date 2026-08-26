package party.lemons.biomemakeover.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import party.lemons.biomemakeover.init.BMBlocks;

/** Released four-stage Illunite growth contract, translated onto modern cluster state. */
public final class BuddingIlluniteBlock extends Block {
    public BuddingIlluniteBlock(Properties properties) { super(properties); }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(5) != 0) return;
        Direction direction = Direction.getRandom(random);
        BlockPos target = pos.relative(direction);
        BlockState current = level.getBlockState(target);
        Block next = null;
        if (BuddingAmethystBlock.canClusterGrowAtState(current)) next = BMBlocks.SMALL_ILLUNITE_BUD;
        else if (current.is(BMBlocks.SMALL_ILLUNITE_BUD) && current.getValue(AmethystClusterBlock.FACING) == direction) next = BMBlocks.MEDIUM_ILLUNITE_BUD;
        else if (current.is(BMBlocks.MEDIUM_ILLUNITE_BUD) && current.getValue(AmethystClusterBlock.FACING) == direction) next = BMBlocks.LARGE_ILLUNITE_BUD;
        else if (current.is(BMBlocks.LARGE_ILLUNITE_BUD) && current.getValue(AmethystClusterBlock.FACING) == direction) next = BMBlocks.ILLUNITE_CLUSTER;
        if (next != null) {
            level.setBlockAndUpdate(target, next.defaultBlockState()
                .setValue(AmethystClusterBlock.FACING, direction)
                .setValue(AmethystClusterBlock.WATERLOGGED, current.getFluidState().is(Fluids.WATER)));
        }
    }
}
