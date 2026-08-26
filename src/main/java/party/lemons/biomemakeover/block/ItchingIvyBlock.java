package party.lemons.biomemakeover.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import party.lemons.biomemakeover.init.BMBlocks;

/** Released Itching Ivy: six-face ivy, half-speed contact, and downward-face blossom conversion. */
public final class ItchingIvyBlock extends IvyBlock implements BonemealableBlock {
    public ItchingIvyBlock(Properties properties) { super(properties); }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean intersects) {
        entity.makeStuckInBlock(state, new Vec3(0.5D, 0.5D, 0.5D));
    }

    @Override public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return state.getValue(MultifaceBlock.getFaceProperty(Direction.DOWN));
    }
    @Override public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) { return true; }
    @Override public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        BlockState blossom = BMBlocks.MOTH_BLOSSOM.defaultBlockState();
        for (Direction direction : Direction.values()) {
            if (state.hasProperty(MultifaceBlock.getFaceProperty(direction)))
                blossom = blossom.setValue(MultifaceBlock.getFaceProperty(direction), state.getValue(MultifaceBlock.getFaceProperty(direction)));
        }
        level.setBlock(pos, blossom.setValue(MothBlossomBlock.BLOSSOM, Direction.DOWN), 3);
    }
}
