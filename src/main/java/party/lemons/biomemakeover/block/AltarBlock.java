package party.lemons.biomemakeover.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import party.lemons.biomemakeover.block.entity.AltarBlockEntity;
import party.lemons.biomemakeover.init.BMBlockEntities;
import party.lemons.biomemakeover.util.RandomUtil;

/** Final-release two-slot curse Altar, translated to the 1.21.10 block contracts. */
public final class AltarBlock extends Block implements EntityBlock, SimpleWaterloggedBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final VoxelShape SHAPE = Shapes.or(
        box(2, 0, 2, 14, 2, 14),
        box(4, 2, 4, 12, 10, 12),
        box(2, 10, 2, 14, 12, 14));

    public AltarBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(ACTIVE, false).setValue(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return simpleCodec(AltarBlock::new);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            MenuProvider provider = getMenuProvider(state, level, pos);
            if (provider != null) player.openMenu(provider);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AltarBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == BMBlockEntities.ALTAR
            ? (tickerLevel, tickerPos, tickerState, entity) -> AltarBlockEntity.tick(tickerLevel, tickerPos, tickerState, (AltarBlockEntity) entity)
            : null;
    }

    @Override
    @Nullable
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        BlockEntity entity = level.getBlockEntity(pos);
        return entity instanceof MenuProvider provider ? provider : null;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER));
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (state.getValue(WATERLOGGED)) ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        // In 1.21.10 BlockEntity.preRemoveSideEffects spills every Container.
        // Only the neighbor/comparator update remains block-owned; manually
        // dropping here would duplicate both Altar slots.
        net.minecraft.world.Containers.updateNeighboursAfterDestroy(state, level, pos);
    }

    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }
    @Override protected boolean useShapeForLightOcclusion(BlockState state) { return true; }
    @Override protected boolean isPathfindable(BlockState state, PathComputationType type) { return false; }
    @Override protected boolean hasAnalogOutputSignal(BlockState state) { return true; }
    @Override protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE, WATERLOGGED);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(ACTIVE)) {
            for (int i = 0; i < 5; i++) {
                double xSpeed = RandomUtil.randomRange(-1.0D, 1.0D) / 0.75D;
                double zSpeed = RandomUtil.randomRange(-1.0D, 1.0D) / 0.75D;
                double ySpeed = random.nextDouble() / 0.1D;
                level.addParticle(ParticleTypes.ENCHANT, pos.getX() + 0.5D, pos.getY() + 0.75D, pos.getZ() + 0.5D,
                    xSpeed, ySpeed, zSpeed);
            }
        }
        if (random.nextInt(5) == 0) {
            Direction direction = Direction.getRandom(random);
            if (!direction.getAxis().isVertical()) {
                double x = direction.getAxis() == Direction.Axis.X
                    ? 0.5D + 0.3D * RandomUtil.randomDirection(1)
                    : RandomUtil.randomRange(2, 8) / 10.0D;
                double z = direction.getAxis() == Direction.Axis.Z
                    ? 0.5D + 0.3D * RandomUtil.randomDirection(1)
                    : RandomUtil.randomRange(2, 8) / 10.0D;
                double y = 0.2D + random.nextFloat() / 3.0D;
                level.addParticle(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, pos.getX() + x, pos.getY() + y, pos.getZ() + z, 0, 0, 0);
            }
        }
    }
}
