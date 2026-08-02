package party.lemons.biomemakeover.entity;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.LandOnOwnersShoulderGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.animal.ShoulderRidingEntity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import party.lemons.biomemakeover.init.BMEntities;
import party.lemons.biomemakeover.init.BMBlocks;
import party.lemons.biomemakeover.init.BMSounds;
import party.lemons.biomemakeover.block.OwlNestBlock;

import java.util.Iterator;
import java.util.EnumSet;
import java.util.List;

/** Faithful 1.21.10 port of the original Biome Makeover owl core. */
public class OwlEntity extends ShoulderRidingEntity {
    private int wakeAnimationTicks = 0;

    private int claimedNestCooldown = 0;
    @Nullable
    private BlockPos homeNestPos;
    private int nestSearchCooldown = 0;

    private int disturbedTicks = 0;

    private int wakeCooldown = 0;

    private static final EntityDataAccessor<Integer> STANDING_STATE =
        SynchedEntityData.defineId(OwlEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> OWL_STATE =
        SynchedEntityData.defineId(OwlEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> OWL_SITTING =
        SynchedEntityData.defineId(OwlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> OWL_SLEEPING =
        SynchedEntityData.defineId(OwlEntity.class, EntityDataSerializers.BOOLEAN);

    private float leaningPitch;
    private float lastLeaningPitch;

    public OwlEntity(EntityType<? extends ShoulderRidingEntity> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 18, false);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, -1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMobAttributes()
            .add(Attributes.FLYING_SPEED, 0.8D)
            .add(Attributes.MAX_HEALTH, 6.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.4D)
            .add(Attributes.ATTACK_DAMAGE, 2.0D)
            .add(Attributes.TEMPT_RANGE, 6.0D);
    }

    public static boolean checkOwlSpawnRules(
        EntityType<OwlEntity> type,
        LevelAccessor level,
        EntitySpawnReason reason,
        BlockPos pos,
        RandomSource random
    ) {
        long time = level.getLevelData().getDayTime() % 24000L;
        boolean night = time >= 12500L && time <= 23500L;
        if (!night) return false;

        // Owls are canopy animals. They must spawn attached to existing trees,
        // never on terrain, water, or open ground.
        BlockState support = level.getBlockState(pos.below());

        boolean treeSupport =
            support.is(BlockTags.LOGS)
            || support.is(BlockTags.LEAVES);

        return treeSupport
            && level.getBlockState(pos).isAir()
            && level.getBlockState(pos.above()).isAir();
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        // Tamed, named, or otherwise persistent owls are never culled. Wild owls
        // use vanilla distance-based despawning only during daylight.
        return !this.isTame() && !this.hasCustomName() && !this.isNightTime();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(1, new WildPlayerCautionGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.15D, true));
        this.goalSelector.addGoal(4, new ReturnToTreeGoal(this));
        this.goalSelector.addGoal(5, new LandOnOwnersShoulderGoal(this));
        this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.05D, 12.0F, 4.0F));
        this.goalSelector.addGoal(7, new TemptGoal(this, 0.65D, Ingredient.of(Items.RABBIT), false));
        this.goalSelector.addGoal(8, new BreedGoal(this, 0.9D));
        this.goalSelector.addGoal(9, new PanicGoal(this, 1.2D));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(11, new ExtendedFlyOntoTree(this, 0.85D, 0.12F));
        this.goalSelector.addGoal(12, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(4, new NightChickenHuntGoal(this));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanFloat(false);
        navigation.setCanOpenDoors(false);
        return navigation;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        OwlEntity child = BMEntities.OWL.create(level, EntitySpawnReason.BREEDING);
        if (child != null) {
            LivingEntity owner = this.getOwner();
            if (owner != null) {
                child.setOwner(owner);
                child.setTame(true, true);
            }
        }
        return child;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.wakeAnimationTicks > 0) {
            this.wakeAnimationTicks--;
        }

        // Sleeping owl lock: while roosting, ignore movement/navigation and remain settled.
        if (this.isOwlSleeping()) {
            this.getNavigation().stop();
            this.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
            this.setTarget(null);
            this.yHeadRot = this.getYRot();
            this.yBodyRot = this.getYRot();
            this.setNoGravity(false);
        }
        if (!this.level().isClientSide()) {
            if (claimedNestCooldown > 0) claimedNestCooldown--;
            this.updateHomeNestBehavior();
        }
        if (this.wakeCooldown > 0) this.wakeCooldown--;
        if (this.disturbedTicks > 0) this.disturbedTicks--;
        setStandingState(onGround() || isInWater() || isOrderedToSit()
            ? StandingState.STANDING : StandingState.FLYING);

        this.lastLeaningPitch = this.leaningPitch;
        if (getStandingState() == StandingState.STANDING) {
            this.leaningPitch = Math.max(0.0F, this.leaningPitch - 2.0F);
        } else {
            this.leaningPitch = Math.min(7.0F, this.leaningPitch + 1.5F);
        }

        if (!this.level().isClientSide() && this.isAlive()) {
            if (this.isOwlSleeping() && this.isNightTime()) {
                this.setOwlSleeping(false);
            }
            if (this.isOwlSleeping()) {
                // Wake if a non-sneaking player gets close.
                for (Player player : this.level().players()) {
                    if (player.distanceTo(this) < 6.0F
                            && !player.isCrouching()
                            && !player.isSpectator()
                            && !player.isCreative()) {
                        this.setDisturbed();
                        this.wakeAnimationTicks = 20;
                        this.wakeCooldown = 40; // brief wake-up pause before flight behavior
                        break;
                    }
                }
            }
        }

        if (!this.level().isClientSide() && this.isAlive()) {
            if (this.isBaby() && this.random.nextInt(900) == 0) {
                this.playSound(BMSounds.OWL_BABY, 0.35F, 1.05F + this.random.nextFloat() * 0.12F);
            } else if (this.isTame() && this.getOwner() != null && this.distanceToSqr(this.getOwner()) < 36.0D
                && this.random.nextInt(1200) == 0) {
                this.playSound(BMSounds.OWL_CONTACT, 0.3F, 0.95F + this.random.nextFloat() * 0.1F);
            } else if (this.isNightTime() && this.onGround() && this.random.nextInt(1800) == 0) {
                this.playSound(BMSounds.OWL_HOOT, 0.7F, 0.88F + this.random.nextFloat() * 0.12F);
            }
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        Vec3 velocity = this.getDeltaMovement();
        if (!this.onGround()) {
            if (this.navigation.isDone() && this.getTarget() == null) {
                // Never hover indefinitely after reaching a perch target.
                double descent = velocity.y > -0.12D ? velocity.y - 0.035D : velocity.y;
                this.setDeltaMovement(velocity.x * 0.96D, descent, velocity.z * 0.96D);
            } else if (velocity.y < -0.18D) {
                // Gentle owl-like descent while actively flying.
                this.setDeltaMovement(velocity.x, velocity.y * 0.88D, velocity.z);
            }
        }
    }

    public float getLeanAmount(float partialTick) {
        return Mth.lerp(partialTick, this.lastLeaningPitch, this.leaningPitch);
    }

    public boolean isOwlFlying() {
        return getStandingState() == StandingState.FLYING;
    }

    @Override
    public void setTame(boolean tame, boolean applyTamingSideEffects) {
        super.setTame(tame, applyTamingSideEffects);
        var health = this.getAttribute(Attributes.MAX_HEALTH);
        var attack = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (health != null) health.setBaseValue(tame ? 20.0D : 6.0D);
        if (attack != null) attack.setBaseValue(tame ? 4.0D : 2.0D);
        if (tame) this.setHealth(this.getMaxHealth());
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (this.isTame() && stack.is(Items.CHICKEN) && this.getHealth() < this.getMaxHealth()) {
            if (!this.level().isClientSide()) {
                if (!player.getAbilities().instabuild) stack.shrink(1);
                this.heal(3.0F);
            }
            return InteractionResult.SUCCESS;
        }
        if (stack.is(Items.RABBIT)) {
            if (this.isTame() && this.getHealth() < this.getMaxHealth()) {
                if (!this.level().isClientSide()) {
                    if (!player.getAbilities().instabuild) stack.shrink(1);
                    this.heal(4.0F);
                }
                return InteractionResult.SUCCESS;
            }
            if (!this.isTame() && this.getTarget() == null) {
                if (!this.level().isClientSide()) {
                    if (!player.getAbilities().instabuild) stack.shrink(1);
                    if (this.random.nextInt(3) == 0) {
                        this.tame(player);
                        this.navigation.stop();
                        this.setTarget(null);
            this.yHeadRot = this.getYRot();
            this.yBodyRot = this.getYRot();
                        this.setOrderedToSit(true);
                        this.level().broadcastEntityEvent(this, (byte) 7);
                    } else {
                        this.level().broadcastEntityEvent(this, (byte) 6);
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }

        // Owner interaction: toggle sit/follow before vanilla ShoulderRidingEntity
        // handles the interaction. The previous order allowed the parent class to
        // consume the click before the owl toggle ran.
        if (this.isTame() && this.isOwnedBy(player) && stack.isEmpty()) {
            if (!this.level().isClientSide()) {
                this.setOwlSitting(!this.isOwlSitting());
                this.jumping = false;
                this.navigation.stop();
                this.setTarget(null);
            this.yHeadRot = this.getYRot();
            this.yBodyRot = this.getYRot();
            }
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    private void setDisturbed() {
        this.disturbedTicks = 200;
        this.setOwlSleeping(false);
        this.getNavigation().stop();
        this.getMoveControl().setWantedPosition(
            this.getX() + (this.random.nextFloat() - 0.5F) * 8,
            this.getY() + 3,
            this.getZ() + (this.random.nextFloat() - 0.5F) * 8,
            1.0F
        );
    }

    private void tryClaimNearbyNest() {
        if (this.isBaby() || this.homeNestPos != null || this.nestSearchCooldown > 0) return;
        this.nestSearchCooldown = 20;

        BlockPos center = this.blockPosition();
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-16, -8, -16), center.offset(16, 10, 16))) {
            BlockState state = this.level().getBlockState(pos);
            if (state.is(BMBlocks.OWL_NEST) && !state.getValue(OwlNestBlock.CLAIMED)) {
                double distance = pos.distSqr(center);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = pos.immutable();
                }
            }
        }

        if (best != null) {
            this.homeNestPos = best;
            BlockState state = this.level().getBlockState(best);
            this.level().setBlock(best, state.setValue(OwlNestBlock.CLAIMED, true), 3);
        }
    }

    private void updateHomeNestBehavior() {
        if (this.nestSearchCooldown > 0) this.nestSearchCooldown--;

        if (this.homeNestPos == null) {
            this.tryClaimNearbyNest();
            return;
        }

        BlockState state = this.level().getBlockState(this.homeNestPos);
        if (!state.is(BMBlocks.OWL_NEST)) {
            this.homeNestPos = null;
            this.setOwlSleeping(false);
            this.setDisturbed();
            return;
        }

        // Repair the visual claimed state after reload or block updates.
        if (!state.getValue(OwlNestBlock.CLAIMED)) {
            this.level().setBlock(
                this.homeNestPos,
                state.setValue(OwlNestBlock.CLAIMED, true),
                3
            );
        }

        if (this.isNightTime()) {
            this.setOwlSleeping(false);
            return;
        }

        if (this.disturbedTicks > 0) return;

        double targetX = this.homeNestPos.getX() + 0.5D;
        double targetY = this.homeNestPos.getY() + 0.05D;
        double targetZ = this.homeNestPos.getZ() + 0.5D;
        double distance = this.distanceToSqr(targetX, targetY, targetZ);

        if (distance > 2.25D) {
            this.setOwlSleeping(false);
            this.getNavigation().moveTo(targetX, targetY, targetZ, 1.05D);
            this.getMoveControl().setWantedPosition(targetX, targetY, targetZ, 1.05D);
        } else {
            // Put the owl squarely into the shallow nest bowl. This avoids other
            // wandering/perch goals stopping it beside or underneath the nest.
            this.getNavigation().stop();
            this.setPos(targetX, targetY, targetZ);
            this.setDeltaMovement(Vec3.ZERO);
            this.setStandingState(StandingState.STANDING);
            this.setOwlSleeping(true);
        }
    }

    public void setOwlBaby(boolean baby) {
        this.setBaby(baby);
    }

    public boolean isOwlBaby() {
        return this.isBaby();
    }

    public boolean canClaimNest() {
        return !this.isBaby();
    }

    public boolean canUseNestForSleeping() {
        return true;
    }

    
    public float getBabyScale() {
        return this.isBaby() ? 0.65F : 1.0F;
    }

    public boolean canHuntRabbit() {
        return true;
    }

    public boolean canHuntChicken() {
        return true;
    }

public boolean isOwlSleeping() {
        return this.entityData.get(OWL_SLEEPING);
    }

    public void setOwlSleeping(boolean sleeping) {
        this.entityData.set(OWL_SLEEPING, sleeping);
        if (sleeping) {
            this.setDeltaMovement(0, 0, 0);
        }
    }

    public boolean isOwlSitting() {
        return this.entityData.get(OWL_SITTING);
    }

    public void setOwlSitting(boolean sitting) {
        this.entityData.set(OWL_SITTING, sitting);
        this.setOrderedToSit(sitting);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.RABBIT);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STANDING_STATE, StandingState.STANDING.ordinal());
        builder.define(OWL_STATE, OwlState.IDLE.ordinal());
        builder.define(OWL_SITTING, false);
        builder.define(OWL_SLEEPING, false);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("OwlState", this.entityData.get(OWL_STATE));
        output.putInt("StandingState", this.entityData.get(STANDING_STATE));
        if (this.homeNestPos != null) {
            output.putInt("HomeNestX", this.homeNestPos.getX());
            output.putInt("HomeNestY", this.homeNestPos.getY());
            output.putInt("HomeNestZ", this.homeNestPos.getZ());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setOwlState(OwlState.byId(input.getInt("OwlState").orElse(OwlState.IDLE.ordinal())));
        setStandingState(StandingState.byId(input.getInt("StandingState").orElse(StandingState.STANDING.ordinal())));
        if (input.getInt("HomeNestX").isPresent()
                && input.getInt("HomeNestY").isPresent()
                && input.getInt("HomeNestZ").isPresent()) {
            this.homeNestPos = new BlockPos(
                input.getInt("HomeNestX").orElse(0),
                input.getInt("HomeNestY").orElse(0),
                input.getInt("HomeNestZ").orElse(0)
            );
        }
    }

    public void setOwlState(OwlState state) { this.entityData.set(OWL_STATE, state.ordinal()); }
    public void setStandingState(StandingState state) { this.entityData.set(STANDING_STATE, state.ordinal()); }
    public OwlState getOwlState() { return OwlState.byId(this.entityData.get(OWL_STATE)); }
    public StandingState getStandingState() { return StandingState.byId(this.entityData.get(STANDING_STATE)); }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageMultiplier, DamageSource source) {
        return false;
    }

    @Override
    public SoundEvent getAmbientSound() { return BMSounds.OWL_IDLE; }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return BMSounds.OWL_HURT; }

    @Override
    protected SoundEvent getDeathSound() { return BMSounds.OWL_DEATH; }

    @Override
    public int getAmbientSoundInterval() {
        // Owls are mostly quiet, but their calls should still be noticeable at night.
        return this.isNightTime() ? 360 : 1800;
    }

    @Override
    protected float getSoundVolume() { return 0.45F; }

    @Override
    public float getVoicePitch() { return 0.82F + this.random.nextFloat() * 0.12F; }

    public enum StandingState {
        STANDING, FLYING;
        static StandingState byId(int id) { return values()[Mth.clamp(id, 0, values().length - 1)]; }
    }

    public enum OwlState {
        IDLE, ATTACKING;
        static OwlState byId(int id) { return values()[Mth.clamp(id, 0, values().length - 1)]; }
    }

    private boolean isNightTime() {
        long time = this.level().getDayTime() % 24000L;
        return time >= 12500L && time <= 23500L;
    }

    private static final class WildPlayerCautionGoal extends Goal {
        private final OwlEntity owl;
        private Player player;
        private int cooldown;

        WildPlayerCautionGoal(OwlEntity owl) {
            this.owl = owl;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (owl.isTame() || owl.isOrderedToSit() || cooldown-- > 0) return false;
            player = owl.level().getNearestPlayer(owl, 11.0D);
            if (player == null || player.isCreative() || player.isSpectator()) return false;
            double trigger = player.isShiftKeyDown() ? 3.5D : (player.isSprinting() ? 11.0D : 7.0D);
            return owl.distanceToSqr(player) < trigger * trigger;
        }

        @Override
        public void start() {
            owl.playSound(BMSounds.OWL_ALERT, 0.45F, 0.92F + owl.getRandom().nextFloat() * 0.12F);
            Vec3 away = owl.position().subtract(player.position());
            if (away.lengthSqr() < 0.01D) away = new Vec3(1, 0, 0);
            away = away.normalize();
            double distance = player.isShiftKeyDown() ? 7.0D : 12.0D;
            Vec3 destination = owl.position().add(away.x * distance, 4.0D, away.z * distance);
            owl.navigation.moveTo(destination.x, destination.y, destination.z, 1.15D);
            cooldown = 80;
        }

        @Override
        public boolean canContinueToUse() {
            return !owl.navigation.isDone() && player != null && !owl.isTame();
        }

        @Override
        public void tick() {
            if (player != null) owl.getLookControl().setLookAt(player, 30.0F, 30.0F);
        }
    }

    private static final class NightChickenHuntGoal extends Goal {
        private final OwlEntity owl;
        private Chicken prey;
        private int cooldown = 20;

        NightChickenHuntGoal(OwlEntity owl) {
            this.owl = owl;
            this.setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (owl.isTame() || owl.isOrderedToSit() || !owl.isNightTime()) return false;
            if (cooldown > 0) {
                --cooldown;
                return false;
            }

            List<Chicken> chickens = owl.level().getEntitiesOfClass(
                Chicken.class,
                owl.getBoundingBox().inflate(24.0D, 12.0D, 24.0D),
                chicken -> chicken.isAlive() && !chicken.isBaby()
            );
            if (chickens.isEmpty()) {
                cooldown = 40;
                return false;
            }

            prey = chickens.stream()
                .min(java.util.Comparator.comparingDouble(owl::distanceToSqr))
                .orElse(null);
            return prey != null;
        }

        @Override
        public void start() {
            owl.setTarget(prey);
            owl.setOwlState(OwlState.ATTACKING);
        }

        @Override
        public boolean canContinueToUse() {
            return !owl.isTame()
                && owl.isNightTime()
                && prey != null
                && prey.isAlive()
                && owl.getTarget() == prey
                && owl.distanceToSqr(prey) < 900.0D;
        }

        @Override
        public void stop() {
            if (owl.getTarget() == prey) owl.setTarget(null);
            owl.setOwlState(OwlState.IDLE);
            prey = null;
            cooldown = 400 + owl.getRandom().nextInt(500);
        }
    }

    private static final class ReturnToTreeGoal extends Goal {
        private final OwlEntity owl;
        private Vec3 destination;
        private int cooldown;

        ReturnToTreeGoal(OwlEntity owl) {
            this.owl = owl;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (cooldown-- > 0 || owl.isOrderedToSit() || owl.getTarget() != null || !owl.onGround()) return false;
            destination = findTreePerch(owl, 10);
            cooldown = 100;
            return destination != null;
        }

        @Override
        public void start() {
            owl.navigation.moveTo(destination.x, destination.y, destination.z, 1.08D);
        }

        @Override
        public boolean canContinueToUse() {
            return destination != null && !owl.navigation.isDone() && !owl.isOrderedToSit();
        }

        @Override
        public void tick() {
            if (destination == null) return;
            double horizontal = owl.position().multiply(1.0D, 0.0D, 1.0D)
                .distanceTo(destination.multiply(1.0D, 0.0D, 1.0D));
            double verticalGap = destination.y - owl.getY();

            // Flying navigation sometimes reaches the trunk horizontally before it
            // has climbed above the canopy. Keep the target active and add a small,
            // capped lift only while directly below the selected exposed perch.
            if (horizontal < 3.25D && verticalGap > 1.0D) {
                Vec3 motion = owl.getDeltaMovement();
                double lift = Math.min(0.16D, 0.055D + verticalGap * 0.008D);
                owl.setDeltaMovement(motion.x, Math.max(motion.y, lift), motion.z);
                if (owl.navigation.isDone()) {
                    owl.navigation.moveTo(destination.x, destination.y, destination.z, 1.08D);
                }
            }
        }

        @Override
        public void stop() {
            destination = null;
        }

        private static Vec3 findTreePerch(OwlEntity owl, int radius) {
            BlockPos origin = owl.blockPosition();
            BlockPos best = null;
            double bestScore = Double.MAX_VALUE;
            for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, 0, -radius), origin.offset(radius, 12, radius))) {
                BlockState support = owl.level().getBlockState(pos.below());
                if (!(support.is(BlockTags.LOGS) || support.getBlock() instanceof LeavesBlock)) continue;
                if (!owl.level().isEmptyBlock(pos) || !owl.level().isEmptyBlock(pos.above())) continue;

                // Prefer exposed canopy tops instead of air pockets buried inside leaves.
                int openSides = 0;
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    if (owl.level().isEmptyBlock(pos.relative(direction))) ++openSides;
                }
                if (openSides < 2) continue;

                double heightBonus = (pos.getY() - origin.getY()) * 7.0D;
                double opennessBonus = openSides * 3.0D;
                double score = origin.distSqr(pos) - heightBonus - opennessBonus;
                if (score < bestScore) { bestScore = score; best = pos.immutable(); }
            }
            return best == null ? null : Vec3.atBottomCenterOf(best);
        }
    }

    private static final class ExtendedFlyOntoTree extends WaterAvoidingRandomStrollGoal {
        ExtendedFlyOntoTree(OwlEntity owl, double speed, float probability) {
            super(owl, speed, probability);
        }

        @Override
        public boolean canUse() {
            OwlEntity owl = (OwlEntity) this.mob;
            if (owl.isOrderedToSit() || owl.getTarget() != null) return false;
            int interval = owl.onGround() ? 45 : (owl.isNightTime() ? 220 : 900);
            return owl.getRandom().nextInt(interval) == 0 && super.canUse();
        }

        @Override
        protected Vec3 getPosition() {
            Vec3 target = null;
            if (this.mob.isInWater()) target = LandRandomPos.getPos(this.mob, 15, 7);
            if (this.mob.getRandom().nextFloat() >= this.probability) target = getTreeTarget();
            return target == null ? super.getPosition() : target;
        }

        private Vec3 getTreeTarget() {
            BlockPos origin = this.mob.blockPosition();
            BlockPos best = null;
            double bestScore = Double.MAX_VALUE;
            Iterable<BlockPos> positions = BlockPos.betweenClosed(
                Mth.floor(this.mob.getX() - 10.0D), Mth.floor(this.mob.getY() - 4.0D), Mth.floor(this.mob.getZ() - 10.0D),
                Mth.floor(this.mob.getX() + 10.0D), Mth.floor(this.mob.getY() + 14.0D), Mth.floor(this.mob.getZ() + 10.0D));

            for (BlockPos pos : positions) {
                if (origin.equals(pos)) continue;
                BlockState support = this.mob.level().getBlockState(pos.below());
                boolean tree = support.getBlock() instanceof LeavesBlock || support.is(BlockTags.LOGS);
                if (!tree || !this.mob.level().isEmptyBlock(pos) || !this.mob.level().isEmptyBlock(pos.above())) continue;

                int openSides = 0;
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    if (this.mob.level().isEmptyBlock(pos.relative(direction))) ++openSides;
                }
                if (openSides < 2) continue;

                double heightBonus = Math.max(0, pos.getY() - origin.getY()) * 6.0D;
                double score = origin.distSqr(pos) - heightBonus - openSides * 2.5D;
                if (score < bestScore) {
                    bestScore = score;
                    best = pos.immutable();
                }
            }
            return best == null ? null : Vec3.atBottomCenterOf(best);
        }
    }
}
