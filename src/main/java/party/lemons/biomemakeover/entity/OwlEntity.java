package party.lemons.biomemakeover.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.animal.ShoulderRidingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import party.lemons.biomemakeover.init.BMEntities;
import party.lemons.biomemakeover.init.BMSounds;

/** Final released Biome Makeover 1.20.1 Owl behavior translated to 1.21.10. */
public class OwlEntity extends ShoulderRidingEntity {
    private static final EntityDataAccessor<Integer> STANDING_STATE =
        SynchedEntityData.defineId(OwlEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> OWL_STATE =
        SynchedEntityData.defineId(OwlEntity.class, EntityDataSerializers.INT);
    private static final EntityDimensions FLYING_DIMENSIONS = EntityDimensions.scalable(0.7F, 1.4F);

    private float leaningPitch;
    private float lastLeaningPitch;

    public OwlEntity(EntityType<? extends ShoulderRidingEntity> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 0, false);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, -1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMobAttributes()
            .add(Attributes.FLYING_SPEED, 0.8D)
            .add(Attributes.MAX_HEALTH, 6.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.4D)
            .add(Attributes.ATTACK_DAMAGE, 2.0D)
            .add(Attributes.TEMPT_RANGE, 10.0D);
    }

    public static boolean checkOwlSpawnRules(EntityType<OwlEntity> type, LevelAccessor level,
                                               EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        BlockState support = level.getBlockState(pos.below());
        return (support.is(Blocks.GRASS_BLOCK) || support.is(BlockTags.LEAVES))
            && level.getRawBrightness(pos, 0) > 2;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.2D, 10.0F, 2.0F));
        this.goalSelector.addGoal(5, new TemptGoal(this, 1.2D,
            Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(ItemTags.WOLF_FOOD)), false));
        this.goalSelector.addGoal(6, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new PanicGoal(this, 1.25D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(9, new ExtendedFlyOntoTree(this, 1.0D, 0.5F));
        this.goalSelector.addGoal(10, new RandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(11, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new NonTameRandomTargetGoal<>(
            this, LivingEntity.class, false, (target, level) -> target.getType().is(BMEntities.OWL_TARGETS)));
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
        if (child != null && this.getOwnerReference() != null) {
            child.setOwnerReference(this.getOwnerReference());
            child.setTame(true, true);
        }
        return child;
    }

    @Override
    public void tick() {
        super.tick();
        this.setStandingState(this.onGround() || this.isInWater() || this.isOrderedToSit()
            ? StandingState.STANDING : StandingState.FLYING);
        this.lastLeaningPitch = this.leaningPitch;
        if (this.getStandingState() == StandingState.STANDING) {
            this.leaningPitch = Math.max(0.0F, this.leaningPitch - 2.0F);
        } else {
            this.leaningPitch = Math.min(7.0F, this.leaningPitch + 1.5F);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        Vec3 velocity = this.getDeltaMovement();
        if (!this.onGround() && velocity.y < 0.0D) {
            this.setDeltaMovement(velocity.multiply(1.0D, 0.75D, 1.0D));
        }
    }

    public float getLeanAmount(float partialTick) {
        return Mth.rotLerp(partialTick, this.lastLeaningPitch, this.leaningPitch);
    }

    public boolean isOwlFlying() {
        return this.getStandingState() == StandingState.FLYING;
    }

    @Override
    public void setTame(boolean tame, boolean applyTamingSideEffects) {
        super.setTame(tame, applyTamingSideEffects);
        var health = this.getAttribute(Attributes.MAX_HEALTH);
        var attack = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (health != null) health.setBaseValue(tame ? 20.0D : 6.0D);
        if (attack != null) attack.setBaseValue(tame ? 4.0D : 2.0D);
        if (tame) this.setHealth(20.0F);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (this.isTame()) {
            if (this.isFood(stack) && this.getHealth() < this.getMaxHealth()) {
                if (!this.level().isClientSide()) {
                    if (!player.getAbilities().instabuild) stack.shrink(1);
                    var food = stack.getComponents().get(net.minecraft.core.component.DataComponents.FOOD);
                    this.heal(food == null ? 1.0F : food.nutrition());
                }
                return InteractionResult.SUCCESS;
            }
            InteractionResult result = super.mobInteract(player, hand);
            if ((!result.consumesAction() || this.isBaby()) && this.isOwnedBy(player)) {
                if (!this.level().isClientSide()) {
                    this.setOrderedToSit(!this.isOrderedToSit());
                    this.jumping = false;
                    this.navigation.stop();
                    this.setTarget(null);
                }
                return InteractionResult.SUCCESS;
            }
            return result;
        }
        if (this.isFood(stack) && this.getTarget() == null) {
            if (!this.level().isClientSide()) {
                if (!player.getAbilities().instabuild) stack.shrink(1);
                if (this.random.nextInt(3) == 0) {
                    this.tame(player);
                    this.navigation.stop();
                    this.setTarget(null);
                    this.setOrderedToSit(true);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                } else {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ItemTags.WOLF_FOOD);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STANDING_STATE, StandingState.STANDING.ordinal());
        builder.define(OWL_STATE, OwlState.IDLE.ordinal());
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("OwlState", this.entityData.get(OWL_STATE));
        output.putInt("StandingState", this.entityData.get(STANDING_STATE));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setOwlState(OwlState.byId(input.getInt("OwlState").orElse(0)));
        this.setStandingState(StandingState.byId(input.getInt("StandingState").orElse(0)));
    }

    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose) {
        return this.getStandingState() == StandingState.STANDING ? super.getDefaultDimensions(pose) : FLYING_DIMENSIONS;
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() { return BMSounds.OWL_IDLE; }
    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return BMSounds.OWL_HURT; }
    @Override
    protected SoundEvent getDeathSound() { return BMSounds.OWL_DEATH; }

    public void setOwlState(OwlState state) { this.entityData.set(OWL_STATE, state.ordinal()); }
    public void setStandingState(StandingState state) { this.entityData.set(STANDING_STATE, state.ordinal()); }
    public OwlState getOwlState() { return OwlState.byId(this.entityData.get(OWL_STATE)); }
    public StandingState getStandingState() { return StandingState.byId(this.entityData.get(STANDING_STATE)); }

    public enum StandingState {
        STANDING, FLYING;
        static StandingState byId(int id) { return values()[Mth.clamp(id, 0, values().length - 1)]; }
    }
    public enum OwlState {
        IDLE, ATTACKING;
        static OwlState byId(int id) { return values()[Mth.clamp(id, 0, values().length - 1)]; }
    }

    private static final class ExtendedFlyOntoTree extends WaterAvoidingRandomStrollGoal {
        ExtendedFlyOntoTree(OwlEntity owl, double speed, float probability) {
            super(owl, speed, probability);
        }

        @Override
        protected Vec3 getPosition() {
            Vec3 target = null;
            if (this.mob.isInWater()) target = LandRandomPos.getPos(this.mob, 15, 15);
            if (this.mob.getRandom().nextFloat() >= this.probability) target = this.getTreeTarget();
            return target == null ? super.getPosition() : target;
        }

        private Vec3 getTreeTarget() {
            BlockPos origin = this.mob.blockPosition();
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            BlockPos best = null;
            double bestDistance = Double.MAX_VALUE;
            for (int x = origin.getX() - 3; x <= origin.getX() + 3; ++x) {
                for (int z = origin.getZ() - 3; z <= origin.getZ() + 3; ++z) {
                    for (int y = origin.getY() - 6; y <= origin.getY() + 6; ++y) {
                        cursor.set(x, y, z);
                        BlockState support = this.mob.level().getBlockState(cursor.below());
                        if ((support.getBlock() instanceof LeavesBlock || support.is(BlockTags.LOGS))
                            && this.mob.level().isEmptyBlock(cursor)
                            && this.mob.level().isEmptyBlock(cursor.above())) {
                            double distance = origin.distSqr(cursor);
                            if (distance < bestDistance) {
                                bestDistance = distance;
                                best = cursor.immutable();
                            }
                        }
                    }
                }
            }
            return best == null ? null : Vec3.atBottomCenterOf(best);
        }
    }
}
