package party.lemons.biomemakeover.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;
import party.lemons.biomemakeover.init.BMBlocks;
import party.lemons.biomemakeover.init.BMParticles;

/** Physical blossom state and block-owned spread/particle behavior; Moth AI remains Stage 8. */
public final class MothBlossomBlock extends MultifaceBlock {
    public static final EnumProperty<Direction> BLOSSOM = EnumProperty.create("blossom", Direction.class);
    public MothBlossomBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(BLOSSOM, Direction.DOWN));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder); builder.add(BLOSSOM);
    }
    @Override protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean intersects) {
        entity.makeStuckInBlock(state, new Vec3(0.5D, 0.5D, 0.5D));
    }
    @Override protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        for (int y : new int[]{0, 1, -1}) for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos target = pos.offset(0, y, 0).relative(direction);
            if (!level.getBlockState(target).canBeReplaced()) continue;
            BlockState ivy = ((ItchingIvyBlock) BMBlocks.ITCHING_IVY).getStateForPlacement(BMBlocks.ITCHING_IVY.defaultBlockState(), level, target, Direction.DOWN);
            if (ivy != null) { level.setBlock(target, ivy, 3); return; }
        }
    }
    @Override public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) level.addParticle(BMParticles.BLOSSOM,
            pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, 0, .02, 0);
    }
}
