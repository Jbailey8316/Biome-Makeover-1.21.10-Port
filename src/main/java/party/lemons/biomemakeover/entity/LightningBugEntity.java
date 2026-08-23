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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.sounds.SoundEvents;
import party.lemons.biomemakeover.init.BMBlocks;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.Mth;

public final class LightningBugEntity extends DragonflyEntity {
    private boolean alternate;
    private float visualPhase = random.nextFloat();
    private float previousRed = -1F, previousGreen = -1F, previousBlue = -1F;
    public LightningBugEntity(EntityType<? extends LightningBugEntity> type, Level level) { super(type, level); }
    public LightningBugEntity(EntityType<? extends LightningBugEntity> type, Level level, boolean alternate) { this(type, level); this.alternate = alternate; }
    public static boolean checkLightningBugSpawn(EntityType<LightningBugEntity> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return checkFlySpawn(level, pos);
    }
    public static AttributeSupplier.Builder createAttributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,3).add(Attributes.MOVEMENT_SPEED,.25).add(Attributes.FLYING_SPEED,.6);}
    @Override protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held=player.getItemInHand(hand);
        if(held.is(Items.GLASS_BOTTLE)) {
            if(!level().isClientSide()) {
                player.setItemInHand(hand,ItemUtils.createFilledResult(held,player,new ItemStack(BMBlocks.LIGHTNING_BUG_BOTTLE)));
                discard();
                player.playSound(SoundEvents.BOTTLE_FILL,1F,1F);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player,hand);
    }
    public boolean isAlternate() { return alternate; }
    public float advanceVisualScale(float partialTick){
        visualPhase += partialTick / 10F;
        if(visualPhase > 99999F) visualPhase = 0F;
        return .9F + Mth.sin(visualPhase) / 5F;
    }
    public int advanceVisualColor(float partialTick){
        BlockPos pos=getOnPos();
        int redHash=pos.hashCode(),greenHash=(pos.getX()+pos.getY()*31)*31+pos.getZ(),blueHash=(pos.getZ()+pos.getX()*31)*31+pos.getY();
        float redTarget=(redHash%255)/255F,blueTarget=(greenHash%255)/255F,greenTarget=(blueHash%255)/255F;
        if(previousRed==-1F){previousRed=redTarget;previousGreen=greenTarget;previousBlue=blueTarget;}
        else {float step=.025F*partialTick;previousRed=Mth.approach(previousRed,redTarget,step);previousGreen=Mth.approach(previousGreen,greenTarget,step);previousBlue=Mth.approach(previousBlue,blueTarget,step);}
        int red=Math.clamp(Math.round(previousRed*255F),0,255),green=Math.clamp(Math.round(Mth.abs(previousGreen)*255F),0,255),blue=Math.clamp(Math.round(previousBlue*255F),0,255);
        return 0xFF000000|(red<<16)|(green<<8)|blue;
    }
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
