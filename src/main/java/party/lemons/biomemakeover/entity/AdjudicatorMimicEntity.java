package party.lemons.biomemakeover.entity;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import party.lemons.biomemakeover.init.BMSounds;

/** Released Adjudicator phase-only Mimic. */
public final class AdjudicatorMimicEntity extends Monster implements RangedAttackMob {
    public AdjudicatorMimicEntity(EntityType<? extends AdjudicatorMimicEntity> type, Level level) {
        super(type, level);
    }

    @Override protected void registerGoals() {
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        goalSelector.addGoal(4, new RangedAttackGoal(this, 1.0D, 12, 15.0F));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        super.populateDefaultEquipmentSlots(random, difficulty);
        setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        setDropChance(net.minecraft.world.entity.EquipmentSlot.MAINHAND, 0.0F);
        setDropChance(net.minecraft.world.entity.EquipmentSlot.OFFHAND, 0.0F);
    }

    @Override public void performRangedAttack(LivingEntity target, float pullProgress) {
        ItemStack arrowStack = getProjectile(getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, Items.BOW)));
        AbstractArrow arrow = ProjectileUtil.getMobArrow(this, arrowStack, pullProgress, getMainHandItem());
        double dx = target.getX() - getX();
        double dy = target.getY(0.3333333333333333D) - arrow.getY();
        double dz = target.getZ() - getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        arrow.shoot(dx, dy + horizontal * 0.20000000298023224D, dz, 1.6F,
            (float) (14 - level().getDifficulty().getId() * 4));
        playSound(SoundEvents.SKELETON_SHOOT, 1.0F,
            1.0F / (getRandom().nextFloat() * 0.4F + 0.8F));
        level().addFreshEntity(arrow);
    }

    @Override protected SoundEvent getAmbientSound() { return BMSounds.ADJUDICATOR_LAUGH; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return BMSounds.ADJUDICATOR_HURT; }
    @Override protected SoundEvent getDeathSound() { return BMSounds.ADJUDICATOR_NO; }

    public static AttributeSupplier.Builder createAttributes() {
        return createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 1.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.25D)
            .add(Attributes.ATTACK_DAMAGE, 3.0D);
    }
}
