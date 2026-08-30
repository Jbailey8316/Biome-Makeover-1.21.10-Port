package party.lemons.biomemakeover.worldgen.mansion;

import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import com.mojang.serialization.MapCodec;

/** Marker substrate block; functional metadata dispatch is deferred. */
public final class MansionDirectionalDataBlock extends DirectionalBlock {
    public MansionDirectionalDataBlock(BlockBehaviour.Properties properties) { super(properties); }
    @Override protected MapCodec<? extends DirectionalBlock> codec() { return simpleCodec(MansionDirectionalDataBlock::new); }
}
