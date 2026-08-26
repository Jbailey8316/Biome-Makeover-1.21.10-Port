package party.lemons.biomemakeover.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.init.BMEntities;

/** Released Black Thistle contact effect translated to the 1.21.10 inside-block callback. */
public class BlackThistleBlock extends TallFlowerBlock {
    public BlackThistleBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effects, boolean isEntityInside) {
        if (!(entity instanceof LivingEntity living)
            || entity.getType() == BMEntities.OWL
            || entity.getType() == EntityType.BEE
            || BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).equals(BiomeMakeover.id("rootling"))
            || state.getValue(HALF) != DoubleBlockHalf.UPPER
            || !(level instanceof ServerLevel serverLevel)
            || living.isInvulnerableTo(serverLevel, level.damageSources().wither())) {
            return;
        }

        living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 110, 0));
    }
}
