package party.lemons.biomemakeover.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import party.lemons.biomemakeover.init.BMEntities;
import party.lemons.biomemakeover.init.BMParticles;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;

public final class LightningBugEntity extends DragonflyEntity {
    private boolean alternate;
    public LightningBugEntity(EntityType<? extends LightningBugEntity> type, Level level) { super(type, level); }
    public LightningBugEntity(EntityType<? extends LightningBugEntity> type, Level level, boolean alternate) { this(type, level); this.alternate = alternate; }
    public static boolean checkLightningBugSpawn(EntityType<LightningBugEntity> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return checkFlySpawn(level, pos);
    }
    public static AttributeSupplier.Builder createAttributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,3).add(Attributes.MOVEMENT_SPEED,.25).add(Attributes.FLYING_SPEED,.6);}
    public boolean isAlternate() { return alternate; }
    @Override public void baseTick(){
        if(firstTick&&!alternate&&!level().isClientSide()){
            int count=random.nextInt(5);
            for(int i=0;i<count;i++){var other=BMEntities.LIGHTNING_BUG_ALTERNATE.create(level(),EntitySpawnReason.MOB_SUMMONED);if(other!=null){other.setPos(getX(),getY(),getZ());level().addFreshEntity(other);}}
        }
        super.baseTick();
    }
    @Override public void aiStep(){
        super.aiStep();
        if(level().isClientSide()&&random.nextInt(200)==0)for(int i=0;i<2;i++)level().addParticle(BMParticles.LIGHTNING_SPARK,getRandomX(.5),getRandomY(),getRandomZ(.5),0,0,0);
    }
    @Override protected void addAdditionalSaveData(ValueOutput output) { super.addAdditionalSaveData(output); output.putBoolean("Alternate", alternate); }
    @Override protected void readAdditionalSaveData(ValueInput input) { super.readAdditionalSaveData(input); alternate = input.getBooleanOr("Alternate", alternate); }
    @Override protected SoundEvent getAmbientSound(){return null;}
    @Override protected SoundEvent getHurtSound(DamageSource source){return null;}
    @Override protected SoundEvent getDeathSound(){return null;}
}
