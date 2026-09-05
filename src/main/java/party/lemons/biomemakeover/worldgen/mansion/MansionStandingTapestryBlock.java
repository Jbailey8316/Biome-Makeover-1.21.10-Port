package party.lemons.biomemakeover.worldgen.mansion;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Released floor-supported tapestry form with vanilla banner rotation semantics. */
public final class MansionStandingTapestryBlock extends MansionTapestryBlock {
    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
    private static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);

    public MansionStandingTapestryBlock(BlockBehaviour.Properties properties, ResourceLocation texture) {
        super(properties, texture);
        registerDefaultState(stateDefinition.any().setValue(ROTATION, 0));
    }

    @Override protected MapCodec<? extends MansionTapestryBlock> codec() { return simpleCodec(p -> new MansionStandingTapestryBlock(p, tapestryTexture())); }
    @Override protected VoxelShape shape(BlockState state) { return SHAPE; }
    /** Mirrors BannerBlock: player yaw is quantized into the native 16-step rotation. */
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(ROTATION, RotationSegment.convertToSegment(context.getRotation() + 180.0F));
    }
    @Override public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isSolid();
    }
    @Override public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(ROTATION, rotation.rotate(state.getValue(ROTATION), 16));
    }
    @Override public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(ROTATION, mirror.mirror(state.getValue(ROTATION), 16));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(ROTATION); }
    @Override public BlockState updateShape(BlockState state, net.minecraft.world.level.LevelReader level,
                                             net.minecraft.world.level.ScheduledTickAccess ticks, BlockPos pos,
                                             Direction direction, BlockPos neighborPos, BlockState neighbor,
                                             net.minecraft.util.RandomSource random) {
        if (direction == Direction.DOWN && !state.canSurvive(level, pos)) return Blocks.AIR.defaultBlockState();
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighbor, random);
    }
}
