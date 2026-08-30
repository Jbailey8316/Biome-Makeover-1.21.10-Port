package party.lemons.biomemakeover.worldgen.mansion;

import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import com.mojang.serialization.MapCodec;

/** Palette-compatible wall tapestry substrate; rendering/gameplay is deferred. */
public final class MansionWallTapestryBlock extends HorizontalDirectionalBlock {
    public MansionWallTapestryBlock(BlockBehaviour.Properties properties) { super(properties); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec() { return simpleCodec(MansionWallTapestryBlock::new); }
}
