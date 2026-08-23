package party.lemons.biomemakeover.entity;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import party.lemons.biomemakeover.init.BMBlocks;
import party.lemons.biomemakeover.init.BMSounds;
import party.lemons.biomemakeover.level.WindSystem;

public final class TumbleweedEntity extends Entity {
    private float age;
    public TumbleweedEntity(EntityType<? extends TumbleweedEntity> type, Level level){ super(type,level); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {}
    @Override public void tick(){
        super.tick(); if(++age>1500){ discard(); return; }
        if(!isInWater()) setDeltaMovement(getDeltaMovement().add(0,-.04,0));
        move(MoverType.SELF,getDeltaMovement());
        if(!level().isClientSide()){
            double x=approach(getDeltaMovement().x, WindSystem.windX, .0025), z=approach(getDeltaMovement().z,WindSystem.windZ,.0025), y=getDeltaMovement().y;
            if(onGround()){ y=Math.max(.31,Math.abs(y)*.75); playSound(BMSounds.TUMBLEWEED_TUMBLE,.25F,1); }
            if(isInWater()){ x*=.75; z*=.75; y=.1; }
            setDeltaMovement(x,y,z); hasImpulse=true;
        }
    }
    @Override public boolean hurtServer(ServerLevel level, DamageSource source, float amount){ playSound(BMSounds.TUMBLEWEED_BREAK,.25F,1); particles(30); discard(); return true; }
    private void particles(int count){ for(int i=0;i<count;i++) level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK,BMBlocks.TUMBLEWEED.defaultBlockState()),getRandomX(.8),getRandomY(),getRandomZ(.8),0,0,0); }
    private static double approach(double value,double target,double step){ return value<target?Math.min(value+step,target):Math.max(value-step,target); }
    @Override protected void readAdditionalSaveData(ValueInput input){} @Override protected void addAdditionalSaveData(ValueOutput output){}
    @Override public boolean isPushable(){return true;} @Override public boolean isPickable(){return !isRemoved();}
}
