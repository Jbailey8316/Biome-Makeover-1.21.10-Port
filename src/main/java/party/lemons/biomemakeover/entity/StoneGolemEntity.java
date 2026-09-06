package party.lemons.biomemakeover.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;
import party.lemons.biomemakeover.init.BMSounds;
import party.lemons.biomemakeover.entity.ai.BetterCrossbowAttackGoal;

import java.util.UUID;

/** Released independent Stone Golem; the Adjudicator mount phase is separate and gated. */
public final class StoneGolemEntity extends AbstractGolem implements CrossbowAttackMob, RangedAttackMob, NeutralMob {
    private static final String TRACE = "BM_STONE_GOLEM_PARITY_PROOF";
    private static final EntityDataAccessor<Boolean> CHARGING =
        SynchedEntityData.defineId(StoneGolemEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> PLAYER_CREATED =
        SynchedEntityData.defineId(StoneGolemEntity.class, EntityDataSerializers.BOOLEAN);
    private int angerTime;
    private UUID angryAt;

    public StoneGolemEntity(EntityType<? extends StoneGolemEntity> type, Level level) { super(type, level); }

    @Override protected void registerGoals() {
        goalSelector.addGoal(1, new BetterCrossbowAttackGoal<>(this, 1.0D, 24.0F));
        goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 5.0F, 1.0F));
        goalSelector.addGoal(3, new LookAtPlayerGoal(this, Mob.class, 5.0F));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.npc.AbstractVillager.class, false));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, StoneGolemEntity.class, true));
        targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(6, new NearestAttackableTargetGoal<>(this, Mob.class, true));
        targetSelector.addGoal(7, new ResetUniversalAngerTargetGoal<>(this, false));
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CHARGING, false);
        builder.define(PLAYER_CREATED, false);
    }

    @Override protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
    }

    @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                    EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        if (reason == EntitySpawnReason.COMMAND || reason == EntitySpawnReason.MOB_SUMMONED
            || reason == EntitySpawnReason.SPAWN_ITEM_USE || reason == EntitySpawnReason.DISPENSER)
            setPlayerCreated(true);
        else populateDefaultEquipmentSlots(level.getRandom(), difficulty);
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        trace("ENTITY_SPAWN uuid=" + getUUID() + " reason=" + reason + " playerCreated=" + isPlayerCreated() + " health=" + getHealth());
        return result;
    }

    @Override protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (isPlayerCreated()) {
            ItemStack stack = player.getItemInHand(hand);
            if (!stack.isEmpty() && stack.is(party.lemons.biomemakeover.init.BMItems.HEALS_STONE_GOLEM)) {
                float before = getHealth(); heal(15.0F);
                if (getHealth() == before) return InteractionResult.PASS;
                playSound(SoundEvents.IRON_GOLEM_REPAIR, 1.0F, 1.0F);
                if (!player.isCreative()) stack.shrink(1);
                return InteractionResult.SUCCESS;
            }
            if (!getMainHandItem().isEmpty()) {
                if (!level().isClientSide()) {
                    player.getInventory().placeItemBackInInventory(getMainHandItem().copy());
                    setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                }
                return InteractionResult.SUCCESS;
            }
            if (stack.is(Items.CROSSBOW)) {
                if (!level().isClientSide()) {
                    setItemSlot(EquipmentSlot.MAINHAND, stack.copyWithCount(1));
                    if (!player.isCreative()) stack.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, hand);
    }

    public boolean isPlayerCreated() { return entityData.get(PLAYER_CREATED); }
    public void setPlayerCreated(boolean value) { entityData.set(PLAYER_CREATED, value); }
    public boolean isAngryAt(LivingEntity entity) {
        return EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity) && !isPlayerCreated();
    }
    @Override public boolean canAttack(LivingEntity target) {
        if (isPlayerCreated() && (target instanceof Player || target instanceof net.minecraft.world.entity.npc.AbstractVillager)) return false;
        if (!isPlayerCreated() && target instanceof Monster) return false;
        return super.canAttack(target);
    }
    @Override protected int decreaseAirSupply(int air) { return air; }
    @Override protected SoundEvent getDeathSound() { return BMSounds.STONE_GOLEM_DEATH; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return BMSounds.STONE_GOLEM_HURT; }
    @Override public int getRemainingPersistentAngerTime() { return angerTime; }
    @Override public void setRemainingPersistentAngerTime(int time) { angerTime = time; }
    @Override public UUID getPersistentAngerTarget() { return angryAt; }
    @Override public void setPersistentAngerTarget(@Nullable UUID id) { angryAt = id; }
    @Override public void startPersistentAngerTimer() { angerTime = 20 * (20 + random.nextInt(20)); }
    @Override public void setChargingCrossbow(boolean charging) { entityData.set(CHARGING, charging); }
    public boolean isChargingCrossbow() { return entityData.get(CHARGING); }
    @Override public void performRangedAttack(LivingEntity target, float power) {
        trace("ATTACK uuid=" + getUUID() + " targetType=" + target.getType());
        performCrossbowAttack(this, power);
    }
    @Override public boolean canFireProjectileWeapon(ProjectileWeaponItem weapon) { return weapon == Items.CROSSBOW; }
    @Override public void onCrossbowAttackPerformed() {}

    public AbstractIllager.IllagerArmPose getState() {
        if (isChargingCrossbow()) return AbstractIllager.IllagerArmPose.CROSSBOW_CHARGE;
        if (isHolding(Items.CROSSBOW)) return AbstractIllager.IllagerArmPose.CROSSBOW_HOLD;
        return isAggressive() ? AbstractIllager.IllagerArmPose.ATTACKING : AbstractIllager.IllagerArmPose.NEUTRAL;
    }

    @Override protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput out) {
        super.addAdditionalSaveData(out);
        out.putBoolean("PlayerCreated", isPlayerCreated());
        out.putInt("AngerTime", angerTime);
        if (angryAt != null) out.putString("AngryAt", angryAt.toString());
        trace("SAVE_LOAD event=save uuid=" + getUUID() + " playerCreated=" + isPlayerCreated() + " health=" + getHealth());
    }
    @Override protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput in) {
        super.readAdditionalSaveData(in);
        setPlayerCreated(in.getBooleanOr("PlayerCreated", false));
        angerTime = in.getIntOr("AngerTime", 0);
        in.getString("AngryAt").ifPresent(value -> { try { angryAt = UUID.fromString(value); } catch (IllegalArgumentException ignored) {} });
        trace("SAVE_LOAD event=load uuid=" + getUUID() + " playerCreated=" + isPlayerCreated() + " health=" + getHealth());
    }
    public static AttributeSupplier.Builder createAttributes() {
        // 1.21.10 target goals require this inherited lookup; released BM did
        // not expose it because its older golem helper supplied the range.
        return createLivingAttributes().add(Attributes.MAX_HEALTH, 60.0D).add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    private void trace(String message) {
        if (Boolean.getBoolean("bm.mansion.trace")) System.out.println(TRACE + " " + message);
    }
}
