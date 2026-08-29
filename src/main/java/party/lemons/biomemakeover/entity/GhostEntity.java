package party.lemons.biomemakeover.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.state.BlockState;
import party.lemons.biomemakeover.init.BMSounds;
import party.lemons.biomemakeover.init.BMEntities;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/** Final-release Ghost foundation; structure spawning is wired by Stage 10C.4. */
public final class GhostEntity extends Monster implements NeutralMob {
    private static final UniformInt ANGER_TIME_RANGE = TimeUtil.rangeOfSeconds(20, 39);
    private int angerTime;
    private UUID targetUuid;
    private int reinforceTime;
    public GhostEntity(EntityType<? extends GhostEntity> type, Level level) {
        super(type, level);
        moveControl = new FlyingMoveControl(this, 20, true);
        setNoGravity(true);
    }
    /** The final source registered the vanilla Monster attribute set. */
    public static AttributeSupplier.Builder createAttributes() {
        // 1.20.1 inherited this through the flying movement stack; 1.21.10's
        // FlyingMoveControl reads the attribute explicitly.
        return Monster.createMonsterAttributes().add(Attributes.FLYING_SPEED, 0.6D);
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, false));
        goalSelector.addGoal(8, new RandomFlyingGoal(this));
        goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
        targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
        targetSelector.addGoal(3, new ResetUniversalAngerTargetGoal<>(this, true));
    }
    @Override protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false); nav.setCanFloat(true); return nav;
    }
    @Override public void tick() { setNoGravity(true); noPhysics = true; super.tick(); noPhysics = false; }
    @Override protected void customServerAiStep(ServerLevel serverLevel) {
        updatePersistentAnger(serverLevel, true);
        if (getTarget() != null && reinforceTime-- <= 0) { alertNearby(); reinforceTime = TimeUtil.rangeOfSeconds(4, 6).sample(random); }
        super.customServerAiStep(serverLevel);
    }
    private void alertNearby() {
        if (getTarget() == null || !getSensing().hasLineOfSight(getTarget())) return;
        double range = getAttributeValue(Attributes.FOLLOW_RANGE);
        var bounds = getBoundingBox().inflate(range, 10.0D, range);
        level().getEntitiesOfClass(GhostEntity.class, bounds).stream().filter(e -> e != this && e.getTarget() == null)
            .filter(e -> !e.isAlliedTo(getTarget())).forEach(e -> e.setTarget(getTarget()));
    }
    @Override public void setTarget(@Nullable LivingEntity target) {
        if (getTarget() == null && target != null) reinforceTime = TimeUtil.rangeOfSeconds(4, 6).sample(random);
        super.setTarget(target);
    }
    @Override public int getRemainingPersistentAngerTime() { return angerTime; }
    @Override public void setRemainingPersistentAngerTime(int time) { angerTime = time; }
    @Override @Nullable public UUID getPersistentAngerTarget() { return targetUuid; }
    @Override public void setPersistentAngerTarget(@Nullable UUID target) { targetUuid = target; }
    @Override public void startPersistentAngerTimer() { angerTime = ANGER_TIME_RANGE.sample(random); }
    @Override protected void addAdditionalSaveData(ValueOutput output) { super.addAdditionalSaveData(output); addPersistentAngerSaveData(output); }
    @Override protected void readAdditionalSaveData(ValueInput input) { super.readAdditionalSaveData(input); readPersistentAngerSaveData(level(), input); }
    @Override public boolean causeFallDamage(double d, float m, DamageSource s) { return false; }
    @Override protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {}
    @Override public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        return source.is(BMEntities.GHOST_IMMUNE_DAMAGE) || super.isInvulnerableTo(level, source);
    }
    @Override protected SoundEvent getAmbientSound() { return BMSounds.GHOST_IDLE; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return BMSounds.GHOST_HURT; }
    @Override protected SoundEvent getDeathSound() { return BMSounds.GHOST_DEATH; }
    public static boolean checkSpawnRules(EntityType<GhostEntity> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL && Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
    }
    private static final class RandomFlyingGoal extends Goal {
        private final GhostEntity ghost; RandomFlyingGoal(GhostEntity ghost){this.ghost=ghost;setFlags(java.util.EnumSet.of(Flag.MOVE));}
        @Override public boolean canUse(){return ghost.getNavigation().isDone() && ghost.random.nextInt(10)==0;}
        @Override public void start(){BlockPos p=ghost.blockPosition().offset(ghost.random.nextInt(15)-7,ghost.random.nextInt(11)-5,ghost.random.nextInt(15)-7);ghost.getNavigation().moveTo(p.getX(),p.getY(),p.getZ(),1);}
        @Override public boolean canContinueToUse(){return !ghost.getNavigation().isDone();}
    }
}
