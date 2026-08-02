package party.lemons.biomemakeover.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;

/**
 * Tall thorn plant with sweet-berry-bush style slowdown and contact damage.
 */
public class BlackThistleBlock extends TallFlowerBlock {
    public BlackThistleBlock(Properties properties) {
        super(properties);
    }

    protected void entityInside(Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects) {
        if (entity.getType() == EntityType.BEE || entity.getType() == EntityType.FOX) {
            return;
        }

        entity.makeStuckInBlock(level.getBlockState(pos), new Vec3(0.8D, 0.75D, 0.8D));

        BlockState state = level.getBlockState(pos);
        Vec3 movement = entity.getDeltaMovement();
        boolean movingHorizontally =
            Math.abs(movement.x) >= 0.003D || Math.abs(movement.z) >= 0.003D;

        if (state.hasProperty(HALF)
            && state.getValue(HALF) == DoubleBlockHalf.LOWER
            && movingHorizontally
            && level instanceof ServerLevel serverLevel) {
            entity.hurtServer(
                serverLevel,
                level.damageSources().sweetBerryBush(),
                1.0F
            );
        }
    }
}
