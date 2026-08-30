package party.lemons.biomemakeover.worldgen.mansion;

import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import com.mojang.serialization.MapCodec;

/** Marker substrate block; functional metadata dispatch is deferred. */
public final class MansionDirectionalDataBlock extends DirectionalBlock {
    public MansionDirectionalDataBlock(BlockBehaviour.Properties properties) { super(properties); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING);
    }
    @Override protected MapCodec<? extends DirectionalBlock> codec() { return simpleCodec(MansionDirectionalDataBlock::new); }
}
