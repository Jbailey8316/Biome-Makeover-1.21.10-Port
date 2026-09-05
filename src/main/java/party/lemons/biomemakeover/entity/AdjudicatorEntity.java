package party.lemons.biomemakeover.entity;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import party.lemons.biomemakeover.init.BMSounds;

/** Released Adjudicator entity substrate; encounter phases are restored in a later stage. */
public final class AdjudicatorEntity extends Monster {
    public AdjudicatorEntity(EntityType<? extends AdjudicatorEntity> type, Level level) {
        super(type, level);
        xpReward = 50;
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 255.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.25D)
            .add(Attributes.ATTACK_DAMAGE, 3.0D);
    }

    @Override
    public void checkDespawn() {
        noActionTime = 0;
    }

    @Override protected SoundEvent getAmbientSound() { return BMSounds.ADJUDICATOR_IDLE; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return BMSounds.ADJUDICATOR_HURT; }
    @Override protected SoundEvent getDeathSound() { return BMSounds.ADJUDICATOR_DEATH; }
}
