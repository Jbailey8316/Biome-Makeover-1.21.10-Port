package party.lemons.biomemakeover.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import party.lemons.biomemakeover.init.BMBlockEntities;

public final class LightningBugBottleBlockEntity extends BlockEntity {
    public LightningBugBottleBlockEntity(BlockPos pos, BlockState state) { super(BMBlockEntities.LIGHTNING_BUG_BOTTLE,pos,state); }
}
