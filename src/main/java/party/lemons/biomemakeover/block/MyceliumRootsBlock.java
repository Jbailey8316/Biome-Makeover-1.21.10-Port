package party.lemons.biomemakeover.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RootsBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class MyceliumRootsBlock extends RootsBlock {
    public MyceliumRootsBlock(BlockBehaviour.Properties properties) { super(properties); }

    @Override
    protected boolean mayPlaceOn(BlockState floor, BlockGetter level, BlockPos pos) {
        return floor.is(Blocks.MYCELIUM) || super.mayPlaceOn(floor, level, pos);
    }
}
