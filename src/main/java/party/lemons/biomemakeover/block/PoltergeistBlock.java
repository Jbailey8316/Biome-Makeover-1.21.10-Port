package party.lemons.biomemakeover.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import party.lemons.biomemakeover.block.entity.PoltergeistBlockEntity;
import party.lemons.biomemakeover.init.BMAdvancements;
import party.lemons.biomemakeover.init.BMBlockEntities;
import party.lemons.biomemakeover.init.BMEffects;
import party.lemons.biomemakeover.level.PoltergeistHandler;

/** Released cauldron-shaped, redstone-sensitive Poltergeist block. */
public final class PoltergeistBlock extends Block implements EntityBlock {
    private static final VoxelShape INSIDE = box(2.0, 4.0, 2.0, 14.0, 16.0, 14.0);
    private static final VoxelShape SUPPORT_SHAPE = Shapes.join(Shapes.block(), box(2.0, 0, 2.0, 14.0, 16.0, 14.0), BooleanOp.ONLY_FIRST);
    private static final VoxelShape SHAPE = Shapes.join(Shapes.block(), Shapes.or(
        box(0.0, 0.0, 4.0, 16.0, 3.0, 12.0), box(4.0, 0.0, 0.0, 12.0, 3.0, 16.0),
        box(2.0, 0.0, 2.0, 14.0, 3.0, 14.0), INSIDE), BooleanOp.ONLY_FIRST);
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

    public PoltergeistBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(ENABLED, true));
    }
    @Override protected MapCodec<? extends Block> codec() { return simpleCodec(PoltergeistBlock::new); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new PoltergeistBlockEntity(pos, state); }
    @Override @Nullable public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == BMBlockEntities.POLTERGEIST
            ? (tickerLevel, tickerPos, tickerState, entity) -> PoltergeistBlockEntity.tick(tickerLevel, tickerPos, tickerState, (PoltergeistBlockEntity) entity)
            : null;
    }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }
    @Override protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) { return INSIDE; }
    @Override protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) { return SUPPORT_SHAPE; }
    @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
        return defaultBlockState().setValue(ENABLED, !context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }
    @Override protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                             net.minecraft.world.level.redstone.Orientation orientation, boolean movedByPiston) {
        if (!level.isClientSide()) {
            boolean enabled = state.getValue(ENABLED);
            if (enabled == level.hasNeighborSignal(pos)) {
                if (enabled) level.scheduleTick(pos, this, 4);
                else { level.setBlock(pos, state.cycle(ENABLED), Block.UPDATE_CLIENTS); doToggleEffects(level, pos); }
            }
        }
    }
    @Override protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(ENABLED) && level.hasNeighborSignal(pos)) {
            level.setBlock(pos, state.cycle(ENABLED), Block.UPDATE_CLIENTS);
            doToggleEffects(level, pos);
        }
    }
    private static void doToggleEffects(Level level, BlockPos pos) {
        level.playSound(null, pos, party.lemons.biomemakeover.init.BMSounds.POLTERGEIST_TOGGLE, SoundSource.BLOCKS, 1F, 1F);
        PoltergeistHandler.doParticles(level, pos);
    }
    @Override protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                          net.minecraft.world.entity.InsideBlockEffectApplier effects, boolean intersects) {
        if (!state.getValue(ENABLED) || !(entity instanceof LivingEntity living)
            || entity.getY() >= pos.getY() + 0.8D || entity.getBoundingBox().maxY <= pos.getY() + 0.25D) return;
        final int maxTime = 900, maxLevel = 4;
        MobEffectInstance instance = living.getEffect(BMEffects.POSSESSED);
        if (instance == null) living.addEffect(new MobEffectInstance(BMEffects.POSSESSED, 200, 0));
        else if (instance.getDuration() < maxTime || instance.getAmplifier() < maxLevel) {
            int nextLevel = instance.getAmplifier();
            if (nextLevel <= maxLevel && living.getRandom().nextInt(100) == 0) nextLevel++;
            instance.update(new MobEffectInstance(BMEffects.POSSESSED, instance.getDuration() + 2, nextLevel));
        }
        if (living instanceof Player && living instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
            BMAdvancements.POLTERGEIST_YOURSELF.trigger(serverPlayer);
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(ENABLED); }
}
