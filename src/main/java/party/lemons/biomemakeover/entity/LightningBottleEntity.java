package party.lemons.biomemakeover.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import party.lemons.biomemakeover.init.BMEffects;
import party.lemons.biomemakeover.init.BMEntities;
import party.lemons.biomemakeover.init.BMItems;
import party.lemons.biomemakeover.init.BMParticles;
import party.lemons.biomemakeover.init.BMSounds;
import party.lemons.biomemakeover.mixin.LightningBoltInvoker;

public final class LightningBottleEntity extends ThrowableItemProjectile {
    public LightningBottleEntity(EntityType<? extends LightningBottleEntity> type, Level level) { super(type, level); }
    public LightningBottleEntity(Level level, LivingEntity owner, ItemStack stack) { super(BMEntities.LIGHTNING_BOTTLE, owner, level, stack); }
    public LightningBottleEntity(Level level, double x, double y, double z, ItemStack stack) { super(BMEntities.LIGHTNING_BOTTLE, x, y, z, level, stack); }

    @Override protected Item getDefaultItem() { return BMItems.LIGHTNING_BOTTLE; }
    @Override protected double getDefaultGravity() { return 0.07D; }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!(level() instanceof ServerLevel server)) return;

        server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, getItem()), getX(), getY(), getZ(),
            8, 0.15, 0.2, 0.15, 0.1);
        server.sendParticles(BMParticles.LIGHTNING_SPARK, getX(), getY() + 0.3, getZ(),
            100, 0.4, 0.5, 0.4, 0.15);
        server.playSound(null, blockPosition(), SoundEvents.SPLASH_POTION_BREAK, SoundSource.NEUTRAL, 1.0F, 0.9F + random.nextFloat() * 0.1F);
        server.playSound(null, blockPosition(), BMSounds.LIGHTNING_BOTTLE_THUNDER, SoundSource.NEUTRAL, 50F, 0.8F + random.nextFloat() * 0.2F);

        var area = getBoundingBox().inflate(4.0D, 2.0D, 4.0D);
        for (LivingEntity entity : server.getEntitiesOfClass(LivingEntity.class, area, EntitySelector.LIVING_ENTITY_STILL_ALIVE)) {
            if (distanceToSqr(entity) >= 16.0D) continue;
            int fireTicks = entity.getRemainingFireTicks();
            boolean invulnerable = entity.isInvulnerable();
            LightningBolt dummy = EntityType.LIGHTNING_BOLT.create(server, EntitySpawnReason.TRIGGERED);
            if (dummy != null) {
                dummy.setPos(entity.position());
                entity.setInvulnerable(true);
                entity.thunderHit(server, dummy);
                entity.setRemainingFireTicks(fireTicks);
                entity.setInvulnerable(invulnerable);
                dummy.discard();
            }
        }
        for (LivingEntity entity : server.getEntitiesOfClass(LivingEntity.class, area, EntitySelector.LIVING_ENTITY_STILL_ALIVE)) {
            if (distanceToSqr(entity) >= 16.0D) continue;
            server.sendParticles(BMParticles.LIGHTNING_SPARK, entity.getX(), entity.getY() + 0.3, entity.getZ(),
                100, 0.25, 0.5, 0.25, 0.1);
            MobEffectInstance old = entity.getEffect(BMEffects.SHOCKED);
            int amplifier = old == null ? 0 : Math.min(3, old.getAmplifier() + 1);
            entity.addEffect(new MobEffectInstance(BMEffects.SHOCKED, 1000, amplifier));
            entity.hurtServer(server, server.damageSources().indirectMagic(this, getOwner()), 0.0F);
            if (getOwner() instanceof LivingEntity owner) entity.setLastHurtByMob(owner);
            if (entity.getHealth() > entity.getMaxHealth()) entity.setHealth(entity.getMaxHealth());
        }

        if (hit instanceof BlockHitResult blockHit) {
            BlockPos pos = blockPosition().relative(blockHit.getDirection().getOpposite());
            var state = server.getBlockState(pos);
            if (state.is(Blocks.LIGHTNING_ROD)) ((LightningRodBlock) state.getBlock()).onLightningStrike(state, server, pos);
            LightningBoltInvoker.biomemakeover$clearCopper(server, pos);
        }
        discard();
    }
}
