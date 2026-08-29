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
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.state.BlockState;
import party.lemons.biomemakeover.init.BMSounds;

/** Final-release Ghost foundation; structure spawning is wired by Stage 10C.4. */
public final class GhostEntity extends Monster {
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
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        goalSelector.addGoal(5, new RandomFlyingGoal(this));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }
    @Override protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false); nav.setCanFloat(true); return nav;
    }
    @Override public void tick() { setNoGravity(true); noPhysics = true; super.tick(); noPhysics = false; }
    @Override public boolean causeFallDamage(double d, float m, DamageSource s) { return false; }
    @Override protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {}
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
