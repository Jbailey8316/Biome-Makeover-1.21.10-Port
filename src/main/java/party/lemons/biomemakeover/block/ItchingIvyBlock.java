package party.lemons.biomemakeover.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** A wall-clinging vine that scratches and slows entities moving through it. */
public class ItchingIvyBlock extends VineBlock {
    public ItchingIvyBlock(Properties properties) {
        super(properties);
    }

    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects) {
        if (entity.getType() == EntityType.BEE || entity.getType() == EntityType.FOX) {
            return;
        }

        entity.makeStuckInBlock(state, new Vec3(0.72D, 0.75D, 0.72D));
        if (level instanceof net.minecraft.server.level.ServerLevel server) {
            entity.hurtServer(server, level.damageSources().sweetBerryBush(), 1.0F);
        }
    }
}
