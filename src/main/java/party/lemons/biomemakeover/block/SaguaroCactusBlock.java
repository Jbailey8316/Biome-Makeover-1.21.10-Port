package party.lemons.biomemakeover.block;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import party.lemons.biomemakeover.init.BMBlocks;

public final class SaguaroCactusBlock extends Block implements BonemealableBlock {
    public static final BooleanProperty HORIZONTAL = BooleanProperty.create("horizontal");
    public static final EnumProperty<Direction> HORIZONTAL_DIRECTION = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final Map<Direction, BooleanProperty> CONNECTIONS = new EnumMap<>(Direction.class);
    static { CONNECTIONS.put(Direction.NORTH, NORTH); CONNECTIONS.put(Direction.SOUTH, SOUTH); CONNECTIONS.put(Direction.EAST, EAST); CONNECTIONS.put(Direction.WEST, WEST); }

    public SaguaroCactusBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(HORIZONTAL, false).setValue(HORIZONTAL_DIRECTION, Direction.NORTH)
            .setValue(NORTH, false).setValue(SOUTH, false).setValue(EAST, false).setValue(WEST, false));
    }

    @Nullable @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        if (face.getAxis().isHorizontal()) return defaultBlockState().setValue(HORIZONTAL, true)
            .setValue(HORIZONTAL_DIRECTION, face.getOpposite()).setValue(CONNECTIONS.get(face.getOpposite()), true);
        return defaultBlockState();
    }

    @Override protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HORIZONTAL)) return level.getBlockState(pos.relative(state.getValue(HORIZONTAL_DIRECTION))).is(this);
        return level.getBlockState(pos.below()).is(BMBlocks.SAGUARO_CACTUS_PLANTABLE) && level.getFluidState(pos.above()).isEmpty();
    }

    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = state.getValue(HORIZONTAL) ? box(4, 8, 4, 12, 16, 12) : box(4, 0, 4, 12, 16, 12);
        if (state.getValue(NORTH)) shape = Shapes.or(shape, box(4, 8, 0, 12, 16, 4));
        if (state.getValue(SOUTH)) shape = Shapes.or(shape, box(4, 8, 12, 12, 16, 16));
        if (state.getValue(WEST)) shape = Shapes.or(shape, box(0, 8, 4, 4, 16, 12));
        if (state.getValue(EAST)) shape = Shapes.or(shape, box(12, 8, 4, 16, 16, 12));
        return shape;
    }

    @Override protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean intersects) { entity.hurt(level.damageSources().cactus(), 1F); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(HORIZONTAL, HORIZONTAL_DIRECTION, NORTH, SOUTH, EAST, WEST); }
    @Override public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) { return state.equals(defaultBlockState()) && level.getBlockState(pos.above()).isAir(); }
    @Override public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) { return random.nextFloat() < .45F; }
    @Override public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) { generateCactus(this, level, random.nextBoolean(), pos, random, false); }
    @Override protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) { if (random.nextInt(10) == 0 && isValidBonemealTarget(level, pos, state)) performBonemeal(level, random, pos, state); }

    private static final Direction[] NORTH_SOUTH = {Direction.NORTH, Direction.SOUTH};
    private static final Direction[] EAST_WEST = {Direction.EAST, Direction.WEST};

    public static boolean generateCactus(Block block, WorldGenLevel level, boolean northSouth, BlockPos origin, RandomSource random, boolean isBig) {
        if (!block.defaultBlockState().canSurvive(level, origin)) return false;

        boolean hasArms = random.nextInt(10) > 1;
        boolean hasTwoArms = random.nextInt(5) != 0;
        int centerHeight = randomRange(random, 4, 8);
        BlockPos.MutableBlockPos cursor = origin.mutable();

        for (int y = 0; y < centerHeight; y++) {
            // The released generator deliberately replaces the heightmap's
            // surface position. Only subsequent trunk positions require air.
            if (y > 0 && !level.getBlockState(cursor).isAir()) break;
            level.setBlock(cursor, block.defaultBlockState(), 2);
            cursor.move(Direction.UP);
        }

        if (!hasArms) return true;

        int centerEndY = cursor.getY();
        int armStart = randomRange(random, 1, centerHeight - 2);
        Direction[] directions = northSouth ? NORTH_SOUTH : EAST_WEST;
        if (hasTwoArms) {
            for (Direction direction : directions) {
                generateArm(block, level, direction, cursor.getX(), origin.getY() + armStart, cursor.getZ(), centerEndY, random);
                armStart = randomRange(random, 1, centerHeight - 2);
            }
        } else {
            generateArm(block, level, directions[random.nextInt(directions.length)], cursor.getX(), origin.getY() + armStart, cursor.getZ(), centerEndY, random);
        }

        if ((!isBig && random.nextInt(10) == 0) || (isBig && random.nextInt(50) == 0)) {
            BlockPos next = new BlockPos(origin.getX(), centerEndY, origin.getZ());
            if (level.getBlockState(next).isAir()) generateCactus(block, level, random.nextBoolean(), next, random, true);
        }
        return true;
    }

    private static void generateArm(Block block, WorldGenLevel level, Direction direction, int centerX, int armY,
                                    int centerZ, int centerHeight, RandomSource random) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(centerX + direction.getStepX(), armY,
            centerZ + direction.getStepZ());
        if (!level.getBlockState(cursor).isAir()) return;

        BlockPos center = cursor.relative(direction.getOpposite());
        BlockState centerState = level.getBlockState(center);
        if (!centerState.is(block)) return;

        level.setBlock(center, centerState.setValue(CONNECTIONS.get(direction), true), 2);
        level.setBlock(cursor, block.defaultBlockState().setValue(HORIZONTAL, true)
            .setValue(HORIZONTAL_DIRECTION, direction.getOpposite())
            .setValue(CONNECTIONS.get(direction.getOpposite()), true), 2);

        cursor.move(Direction.UP);
        int amount = Math.max(1, centerHeight - cursor.getY() + randomRange(random, -3, -1));
        for (int i = 0; i < amount; i++) {
            if (!level.getBlockState(cursor).isAir()) return;
            level.setBlock(cursor, block.defaultBlockState(), 2);
            cursor.move(Direction.UP);
        }
    }

    private static int randomRange(RandomSource random, int minimum, int maximum) {
        return minimum + random.nextInt(maximum - minimum + 1);
    }
}
