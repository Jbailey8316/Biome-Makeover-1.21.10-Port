package party.lemons.biomemakeover.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Six-face released ivy foundation. MultifaceBlock supplies support and water-state migration. */
public class IvyBlock extends MultifaceBlock {
    public IvyBlock(Properties properties) { super(properties); }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int nearby = 0;
        for (BlockPos scan : BlockPos.betweenClosed(pos.offset(-3, -3, -3), pos.offset(2, 2, 2)))
            if (level.getBlockState(scan).is(this) && ++nearby >= 7) return;
        Direction face = Direction.getRandom(random);
        Direction travel = Direction.getRandom(random);
        BlockPos target = pos.relative(travel);
        BlockState targetState = level.getBlockState(target);
        if (targetState.is(this)) return;
        if (!targetState.canBeReplaced()) return;
        BlockState placed = getStateForPlacement(defaultBlockState(), level, target, face);
        if (placed != null) level.setBlock(target, placed, 3);
    }
}
