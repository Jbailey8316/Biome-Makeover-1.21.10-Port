package party.lemons.biomemakeover.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
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
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import party.lemons.biomemakeover.init.BMBlocks;
import party.lemons.biomemakeover.init.BMSounds;

import java.util.EnumSet;
import java.util.Optional;

/** Final-release hostile canopy Moth, including its historically gated light/blossom attraction. */
public final class MothEntity extends Monster {
    public boolean hasPlayedLoop;
    private int attractionCooldown = 20;
    @Nullable private BlockPos attraction;
    private float currentPitch, lastPitch;

    public MothEntity(EntityType<? extends MothEntity> type, Level level) {
        super(type, level);
        moveControl = new FlyingMoveControl(this, 20, true);
        setPathfindingMalus(PathType.DANGER_FIRE,-1); setPathfindingMalus(PathType.WATER,-1);
        setPathfindingMalus(PathType.WATER_BORDER,16); setPathfindingMalus(PathType.COCOA,-1); setPathfindingMalus(PathType.FENCE,-1);
    }
    public static AttributeSupplier.Builder createAttributes(){return createMonsterAttributes().add(Attributes.FLYING_SPEED,.6).add(Attributes.MAX_HEALTH,10).add(Attributes.MOVEMENT_SPEED,.25);}

    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new FloatGoal(this)); goalSelector.addGoal(1,new AttractionGoal());
        goalSelector.addGoal(3,new MeleeAttackGoal(this,1,false)); goalSelector.addGoal(4,new AvoidEntityGoal<>(this,OwlEntity.class,6,1,1.2));
        goalSelector.addGoal(5,new RandomFlyingGoal());
        targetSelector.addGoal(1,new NearestAttackableTargetGoal<>(this,Player.class,true)); targetSelector.addGoal(2,new HurtByTargetGoal(this));
    }
    public static boolean checkSpawnRules(EntityType<? extends MothEntity> type, ServerLevelAccessor level,
            EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return level.getDifficulty()!=Difficulty.PEACEFUL && Monster.isDarkEnoughToSpawn(level,pos,random)
            && (reason==EntitySpawnReason.SPAWNER || level.getBlockState(pos.below()).is(BlockTags.LEAVES));
    }
    @Override protected PathNavigation createNavigation(Level level){FlyingPathNavigation n=new FlyingPathNavigation(this,level);n.setCanFloat(false);n.setCanOpenDoors(false);return n;}
    @Override public float getWalkTargetValue(BlockPos pos, LevelReader level){return level.getBlockState(pos).isAir()?10+level.getLightEmission(pos):super.getWalkTargetValue(pos,level);}
    @Override protected float getBlockSpeedFactor(){
        BlockState at=level().getBlockState(blockPosition());
        BlockState below=level().getBlockState(getBlockPosBelowThatAffectsMyMovement());
        if(at.is(BMBlocks.ITCHING_IVY)||at.is(BMBlocks.MOTH_BLOSSOM)||below.is(BMBlocks.ITCHING_IVY)||below.is(BMBlocks.MOTH_BLOSSOM))return 1F;
        return super.getBlockSpeedFactor();
    }
    @Override public void aiStep(){super.aiStep();lastPitch=currentPitch;currentPitch=Math.max((float)Math.sin(tickCount/10F)/10F,currentPitch-.24F);if(!level().isClientSide()&&attractionCooldown>0)attractionCooldown--;}
    public float bodyPitch(float partial){return net.minecraft.util.Mth.lerp(partial,lastPitch,currentPitch);}
    @Override public boolean hurtServer(ServerLevel level,DamageSource source,float amount){attraction=null;return super.hurtServer(level,source,amount);}
    @Override public boolean causeFallDamage(double distance,float multiplier,DamageSource source){return false;}
    @Override protected void checkFallDamage(double y,boolean ground,BlockState state,BlockPos pos){}
    @Override protected SoundEvent getAmbientSound(){return BMSounds.MOTH_IDLE;}
    @Override protected SoundEvent getHurtSound(DamageSource source){return BMSounds.MOTH_HURT;}
    @Override protected SoundEvent getDeathSound(){return BMSounds.MOTH_DEATH;}
    @Override public boolean doHurtTarget(ServerLevel level,Entity target){boolean hit=super.doHurtTarget(level,target);if(hit)playSound(BMSounds.MOTH_BITE,1,1);return hit;}
    private boolean attractive(BlockState state){return state.getLightEmission()>10||state.is(BMBlocks.MOTH_ATTRACTIVE);}

    private final class RandomFlyingGoal extends Goal {
        private Vec3 wanted;
        RandomFlyingGoal(){setFlags(EnumSet.of(Flag.MOVE));}
        @Override public boolean canUse(){if(!getNavigation().isDone()||random.nextInt(10)!=0)return false;float a=getYRot()*(float)Math.PI/180;wanted=net.minecraft.world.entity.ai.util.AirAndWaterRandomPos.getPos(MothEntity.this,10,7,-2,Math.cos(a),0,Math.sin(a));return wanted!=null;}
        @Override public void start(){getNavigation().moveTo(wanted.x,wanted.y,wanted.z,1);}
        @Override public boolean canContinueToUse(){return !getNavigation().isDone();}
    }
    private final class AttractionGoal extends Goal {
        private int ticks;
        AttractionGoal(){setFlags(EnumSet.of(Flag.MOVE));}
        /* The attacker gate looks odd, but is the reachable final 1.20.1 source contract. */
        @Override public boolean canUse(){if(getLastHurtByMob()==null||attractionCooldown>0)return false;attraction=find().orElse(null);return attraction!=null;}
        @Override public void start(){ticks=0;}
        @Override public boolean canContinueToUse(){return getLastHurtByMob()!=null&&attraction!=null&&ticks<600&&attractive(level().getBlockState(attraction))&&(ticks<=400||random.nextFloat()<.2F);}
        @Override public void tick(){ticks++;Vec3 v=Vec3.atBottomCenterOf(attraction).add(0,.6,0);getMoveControl().setWantedPosition(v.x,v.y,v.z,.35);getLookControl().setLookAt(v.x,v.y,v.z);}
        @Override public void stop(){attraction=null;attractionCooldown=200;getNavigation().stop();}
        private Optional<BlockPos> find(){BlockPos o=blockPosition();BlockPos best=null;double d=Double.MAX_VALUE;for(BlockPos p:BlockPos.betweenClosed(o.offset(-5,-5,-5),o.offset(5,5,5)))if(attractive(level().getBlockState(p))){double n=p.distSqr(o);if(n<d){d=n;best=p.immutable();}}return Optional.ofNullable(best);}
    }
}
