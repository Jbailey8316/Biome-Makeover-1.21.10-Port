package party.lemons.biomemakeover.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import party.lemons.biomemakeover.block.entity.LightningBugBottleBlockEntity;

public final class LightningBugBottleBlock extends Block implements EntityBlock, SimpleWaterloggedBlock {
    public static final BooleanProperty UPPER = BooleanProperty.create("up");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final VoxelShape LOWER_SHAPE = Shapes.or(box(4,0,4,12,8,12),box(6,8,6,10,12,10));
    private static final VoxelShape UPPER_SHAPE = Shapes.or(box(4,4,4,12,12,12),box(6,11,6,10,16,10));

    public LightningBugBottleBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(UPPER, false).setValue(WATERLOGGED, false));
    }

    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(UPPER, context.getClickedFace() == Direction.DOWN)
            .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER));
    }
    @Override protected FluidState getFluidState(BlockState state) { return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state); }
    @Override protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, net.minecraft.util.RandomSource random) {
        if(state.getValue(WATERLOGGED)) ticks.scheduleTick(pos,Fluids.WATER,Fluids.WATER.getTickDelay(level));
        return super.updateShape(state,level,ticks,pos,direction,neighborPos,neighborState,random);
    }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return state.getValue(UPPER) ? UPPER_SHAPE : LOWER_SHAPE; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) { super.createBlockStateDefinition(builder);builder.add(UPPER,WATERLOGGED); }
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state) { return new LightningBugBottleBlockEntity(pos,state); }
}
