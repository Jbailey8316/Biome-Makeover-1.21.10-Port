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
import party.lemons.biomemakeover.init.BMEntities;
import party.lemons.biomemakeover.level.WindSystem;
import org.joml.Quaternionf;

public final class TumbleweedEntity extends Entity {
    public Quaternionf quaternion = new Quaternionf();
    public Quaternionf previousQuaternion = new Quaternionf();
    private double previousVerticalVelocity;
    private float windOffset;
    private float age;
    private int stuckX, stuckZ, staticTime;
    public TumbleweedEntity(EntityType<? extends TumbleweedEntity> type, Level level){ super(type,level); windOffset=1F-level.random.nextFloat()/3F; }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {}
    @Override public void tick(){
        if(++age>1500){ breakApart(30); return; }
        if(!isInWater()) setDeltaMovement(getDeltaMovement().add(0,-.04,0));
        super.tick();
        previousQuaternion.set(quaternion);
        previousVerticalVelocity=getDeltaMovement().y;
        double oldX=getX(), oldZ=getZ();
        move(MoverType.SELF,getDeltaMovement());
        double distanceX=getX()-oldX, distanceZ=getZ()-oldZ;
        if(!level().isClientSide()){
            double x=approach(getDeltaMovement().x, WindSystem.windX*windOffset, .0025), z=approach(getDeltaMovement().z,WindSystem.windZ*windOffset,.0025), y=getDeltaMovement().y;
            if(onGround()){ y=net.minecraft.util.Mth.clamp(Math.abs(previousVerticalVelocity)*.75,.31,2); playSound(BMSounds.TUMBLEWEED_TUMBLE,.25F,1); }
            if(isInWater()){ x*=.75; z*=.75; y=.1; }
            setDeltaMovement(x,y,z); hasImpulse=true; hurtMarked=true;
        } else {
            if(onGround()) particles(15);
            float divisor=onGround()?.25F:.6F;
            float xRotation=(float)-(distanceX/divisor), zRotation=(float)(distanceZ/divisor);
            quaternion.set(new Quaternionf().rotationXYZ(zRotation,0,xRotation).mul(quaternion));
        }
        int blockX=(int)getX(), blockZ=(int)getZ();
        if(blockX==stuckX&&blockZ==stuckZ){if(++staticTime>=100){breakApart(30);return;}}else staticTime=0;
        stuckX=blockX;stuckZ=blockZ;
    }
    @Override public boolean hurtServer(ServerLevel level, DamageSource source, float amount){
        if(!source.is(BMEntities.TUMBLEWEED_IMMUNE_DAMAGE)){playSound(BMSounds.TUMBLEWEED_BREAK,.25F,1);breakApart(30);}
        return true;
    }
    private void breakApart(int count){particles(count);discard();}
    private void particles(int count){ for(int i=0;i<count;i++) level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK,BMBlocks.TUMBLEWEED.defaultBlockState()),getRandomX(.8),getRandomY(),getRandomZ(.8),0,0,0); }
    private static double approach(double value,double target,double step){ return value<target?Math.min(value+step,target):Math.max(value-step,target); }
    @Override protected void readAdditionalSaveData(ValueInput input){} @Override protected void addAdditionalSaveData(ValueOutput output){}
    @Override public boolean isPushable(){return true;} @Override public boolean isPickable(){return !isRemoved();}
    @Override public boolean canCollideWith(Entity entity){return true;}
}
