package party.lemons.biomemakeover.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;

/** A tall thorn plant: slows entities and deals berry-bush style contact damage. */
public class BlackThistleBlock extends TallFlowerBlock {
    public BlackThistleBlock(Properties properties) { super(properties); }

    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects) {
        if (entity.getType() == EntityType.BEE || entity.getType() == EntityType.FOX) return;
        entity.makeStuckInBlock(state, new Vec3(0.72D, 0.75D, 0.72D));
        if (level instanceof net.minecraft.server.level.ServerLevel server
                && state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            entity.hurtServer(server, level.damageSources().sweetBerryBush(), 1.0F);
        }
    }
}
