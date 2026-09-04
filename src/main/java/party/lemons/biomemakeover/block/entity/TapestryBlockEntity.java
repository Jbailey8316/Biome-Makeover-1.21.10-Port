package party.lemons.biomemakeover.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import party.lemons.biomemakeover.init.BMBlockEntities;

/** Released shared block entity for standing and wall tapestries. */
public final class TapestryBlockEntity extends BlockEntity {
    public TapestryBlockEntity(BlockPos pos, BlockState state) { super(BMBlockEntities.TAPESTRY, pos, state); }
}
