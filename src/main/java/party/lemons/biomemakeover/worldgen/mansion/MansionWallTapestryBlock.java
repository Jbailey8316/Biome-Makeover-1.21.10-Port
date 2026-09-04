package party.lemons.biomemakeover.worldgen.mansion;

import com.mojang.serialization.MapCodec;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.Map;

/** Released wall-mounted tapestry form. */
public final class MansionWallTapestryBlock extends MansionTapestryBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final Map<Direction, VoxelShape> SHAPES = Maps.newEnumMap(ImmutableMap.of(
        Direction.NORTH, Block.box(0.0, 0.0, 14.0, 16.0, 12.5, 16.0),
        Direction.SOUTH, Block.box(0.0, 0.0, 0.0, 16.0, 12.5, 2.0),
        Direction.WEST, Block.box(14.0, 0.0, 0.0, 16.0, 12.5, 16.0),
        Direction.EAST, Block.box(0.0, 0.0, 0.0, 2.0, 12.5, 16.0)));

    public MansionWallTapestryBlock(BlockBehaviour.Properties properties, ResourceLocation texture) {
        super(properties, texture);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override protected MapCodec<? extends MansionTapestryBlock> codec() { return simpleCodec(p -> new MansionWallTapestryBlock(p, tapestryTexture())); }
    @Override protected VoxelShape shape(BlockState state) { return SHAPES.get(state.getValue(FACING)); }
    @Override public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.relative(state.getValue(FACING).getOpposite())).isSolid();
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        for (Direction direction : context.getNearestLookingDirections()) {
            if (!direction.getAxis().isHorizontal()) continue;
            BlockState candidate = state.setValue(FACING, direction.getOpposite());
            if (candidate.canSurvive(context.getLevel(), context.getClickedPos())) return candidate;
        }
        return null;
    }
    @Override public BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }
    @Override public BlockState mirror(BlockState state, Mirror mirror) { return state.rotate(mirror.getRotation(state.getValue(FACING))); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }
    @Override public BlockState updateShape(BlockState state, net.minecraft.world.level.LevelReader level,
                                             net.minecraft.world.level.ScheduledTickAccess ticks, BlockPos pos,
                                             Direction direction, BlockPos neighborPos, BlockState neighbor,
                                             net.minecraft.util.RandomSource random) {
        if (direction == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos)) return Blocks.AIR.defaultBlockState();
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighbor, random);
    }
}
