package party.lemons.biomemakeover.worldgen.mansion;

import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import com.mojang.serialization.MapCodec;

/** Palette-compatible wall tapestry substrate; rendering/gameplay is deferred. */
public final class MansionWallTapestryBlock extends HorizontalDirectionalBlock {
    public MansionWallTapestryBlock(BlockBehaviour.Properties properties) { super(properties); }
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec() { return simpleCodec(MansionWallTapestryBlock::new); }
}
