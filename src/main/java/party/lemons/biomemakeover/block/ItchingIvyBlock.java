package party.lemons.biomemakeover.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.phys.Vec3;

/**
 * Wall-clinging, climbable ivy with sweet-berry-bush style slowdown and damage.
 */
public class ItchingIvyBlock extends VineBlock {
    public ItchingIvyBlock(Properties properties) {
        super(properties);
    }

    protected void entityInside(Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects) {
        if (entity.getType() == EntityType.BEE || entity.getType() == EntityType.FOX) {
            return;
        }

        entity.makeStuckInBlock(level.getBlockState(pos), new Vec3(0.8D, 0.75D, 0.8D));

        Vec3 movement = entity.getDeltaMovement();
        boolean movingHorizontally =
            Math.abs(movement.x) >= 0.003D || Math.abs(movement.z) >= 0.003D;

        if (movingHorizontally && level instanceof ServerLevel serverLevel) {
            entity.hurtServer(
                serverLevel,
                level.damageSources().sweetBerryBush(),
                1.0F
            );
        }
    }
}
