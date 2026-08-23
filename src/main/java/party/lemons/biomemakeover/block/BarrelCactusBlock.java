package party.lemons.biomemakeover.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import party.lemons.biomemakeover.init.BMBlocks;

public final class BarrelCactusBlock extends Block {
    private static final VoxelShape SHAPE = Block.column(8.0D, 0.0D, 7.0D);
    private final boolean flowered;

    public BarrelCactusBlock(boolean flowered, BlockBehaviour.Properties properties) {
        super(properties);
        this.flowered = flowered;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).is(BMBlocks.BARREL_CACTUS_PLANTABLE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean intersects) {
        if (!entity.isSteppingCarefully() && (!(entity instanceof ItemEntity item) ||
            (entity.tickCount >= 120 && !item.getItem().is(BMBlocks.BARREL_CACTUS_IMMUNE)))) {
            entity.hurt(level.damageSources().cactus(), 1.0F);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!flowered && level.getRawBrightness(pos, 0) >= 9 && random.nextInt(7) == 0) {
            level.setBlock(pos, BMBlocks.BARREL_CACTUS_FLOWERED.defaultBlockState(), 3);
        }
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int type, int data) {
        Vec3 center = pos.getCenter();
        for (int i = 0; i < 15; i++) {
            level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state), center.x, center.y - .15, center.z,
                (level.random.nextDouble() - .5) * .2, (level.random.nextDouble() - .5) * .2,
                (level.random.nextDouble() - .5) * .2);
        }
        return true;
    }
}
