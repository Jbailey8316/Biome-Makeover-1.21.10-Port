package party.lemons.biomemakeover.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import party.lemons.biomemakeover.entity.RootlingEntity;
import party.lemons.biomemakeover.init.BMEntities;
import party.lemons.biomemakeover.init.BMItems;

/** Final 1.20.1 Rootling seed lifecycle translated to the modern crop callbacks. */
public final class RootlingCropBlock extends CropBlock {
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 4);
    private static final VoxelShape[] SHAPES = {
        box(0, 0, 0, 16, 2, 16), box(0, 0, 0, 16, 6, 16),
        box(0, 0, 0, 16, 10, 16), box(0, 0, 0, 16, 16, 16), box(0, 0, 0, 16, 16, 16)
    };

    public RootlingCropBlock(Properties properties) { super(properties); }

    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[getAge(state)];
    }
    @Override public int getMaxAge() { return 4; }
    @Override protected IntegerProperty getAgeProperty() { return AGE; }
    @Override protected ItemLike getBaseSeedId() { return BMItems.ROOTLING_SEEDS; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getRawBrightness(pos, 0) < 9 || isMaxAge(state)) return;
        float speed = getGrowthSpeed(this, level, pos);
        if (random.nextInt((int)(25.0F / speed) + 1) == 0) advance(level, pos, state, 1);
    }

    @Override public void growCrops(Level level, BlockPos pos, BlockState state) {
        advance(level, pos, state, getBonemealAgeIncrease(level));
    }

    private void advance(Level level, BlockPos pos, BlockState state, int amount) {
        int age = Math.min(getMaxAge(), getAge(state) + amount);
        if (age < getMaxAge()) {
            level.setBlock(pos, getStateForAge(age), 2);
            return;
        }
        if (level.isClientSide()) return;
        level.removeBlock(pos, false);
        RootlingEntity rootling = BMEntities.ROOTLING.create(level, net.minecraft.world.entity.EntitySpawnReason.BREEDING);
        if (rootling != null) {
            rootling.snapTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, level.random.nextFloat(), level.random.nextFloat());
            rootling.setDeltaMovement(0, 0.25D, 0);
            rootling.randomizeFlower();
            level.addFreshEntity(rootling);
        }
    }
}
