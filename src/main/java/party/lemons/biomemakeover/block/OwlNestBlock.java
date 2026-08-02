package party.lemons.biomemakeover.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class OwlNestBlock extends Block {
    public static final BooleanProperty CLAIMED = BooleanProperty.create("claimed");
    public static final BooleanProperty HAS_EGG = BooleanProperty.create("has_egg");
    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 5.0, 14.0);

    public OwlNestBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(CLAIMED, false).setValue(HAS_EGG, false));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CLAIMED, HAS_EGG);
    }
}
