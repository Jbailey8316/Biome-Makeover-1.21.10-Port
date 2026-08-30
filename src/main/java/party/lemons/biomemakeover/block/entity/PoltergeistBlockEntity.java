package party.lemons.biomemakeover.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import party.lemons.biomemakeover.block.PoltergeistBlock;
import party.lemons.biomemakeover.init.BMBlockEntities;
import party.lemons.biomemakeover.level.PoltergeistHandler;

/** Stateless server ticker; enabled is the complete persistent block state. */
public final class PoltergeistBlockEntity extends BlockEntity {
    public PoltergeistBlockEntity(BlockPos pos, BlockState state) { super(BMBlockEntities.POLTERGEIST, pos, state); }
    public static void tick(Level level, BlockPos pos, BlockState state, PoltergeistBlockEntity entity) {
        if (!level.isClientSide() && state.getValue(PoltergeistBlock.ENABLED)) PoltergeistHandler.doPoltergeist(level, null, pos, 5);
    }
}
