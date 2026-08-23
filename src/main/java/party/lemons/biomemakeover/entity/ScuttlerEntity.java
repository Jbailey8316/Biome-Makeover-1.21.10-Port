package party.lemons.biomemakeover.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import party.lemons.biomemakeover.init.BMEntities;
import party.lemons.biomemakeover.init.BMSounds;
import party.lemons.biomemakeover.init.BMBlocks;
import party.lemons.biomemakeover.init.BMItems;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import java.util.EnumSet;

public final class ScuttlerEntity extends Animal {
    private static final EntityDataAccessor<Boolean> PASSIVE = SynchedEntityData.defineId(ScuttlerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> RATTLING = SynchedEntityData.defineId(ScuttlerEntity.class, EntityDataSerializers.BOOLEAN);
    private int rattleTicks;

    public ScuttlerEntity(EntityType<? extends Animal> type, Level level) { super(type, level); }
    public static AttributeSupplier.Builder createAttributes() { return createAnimalAttributes().add(Attributes.MAX_HEALTH,10).add(Attributes.MOVEMENT_SPEED,.25); }
    public static boolean checkSpawnRules(EntityType<ScuttlerEntity> type, LevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return random.nextBoolean() && Animal.isBrightEnoughToSpawn(level,pos);
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0,new FloatGoal(this));
        goalSelector.addGoal(1,new TemptGoal(this,.7,Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(BMEntities.SCUTTLER_FOOD)),false));
        goalSelector.addGoal(2,new RattleGoal()); goalSelector.addGoal(3,new PanicGoal(this,1.25));
        goalSelector.addGoal(4,new BreedGoal(this,1)); goalSelector.addGoal(5,new AvoidEntityGoal<>(this,Player.class,16,1.6,1.4,entity -> !isPassive()));
        goalSelector.addGoal(6,new EatFlowerGoal()); goalSelector.addGoal(7,new FollowParentGoal(this,1.1));
        goalSelector.addGoal(8,new WaterAvoidingRandomStrollGoal(this,1)); goalSelector.addGoal(9,new LookAtPlayerGoal(this,Player.class,6)); goalSelector.addGoal(10,new RandomLookAroundGoal(this));
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { super.defineSynchedData(builder); builder.define(PASSIVE,false); builder.define(RATTLING,false); }
    @Override public void tick() {
        super.tick();
        if (entityData.get(RATTLING)) {
            double direction = Math.signum(Math.sin(rattleTicks));
            rattleTicks++;
            if (direction != Math.signum(Math.sin(rattleTicks))) playSound(BMSounds.SCUTTLER_RATTLE,.25F,.75F+random.nextFloat());
        } else rattleTicks = 0;
    }
    @Override public boolean isFood(ItemStack stack) { return stack.is(BMEntities.SCUTTLER_FOOD); }
    @Override public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack=player.getItemInHand(hand);
        if(!isFood(stack)) return super.mobInteract(player,hand);
        if(level().isClientSide()) return InteractionResult.SUCCESS;
        if(!isPassive()) { if(!player.getAbilities().instabuild) stack.shrink(1); if(random.nextInt(3)==0) setPassive(true); setPersistenceRequired(); return InteractionResult.SUCCESS_SERVER; }
        if(getHealth()<getMaxHealth()) { if(!player.getAbilities().instabuild) stack.shrink(1); heal(2); return InteractionResult.SUCCESS_SERVER; }
        return super.mobInteract(player,hand);
    }
    public boolean isPassive(){ return entityData.get(PASSIVE); } public void setPassive(boolean value){ entityData.set(PASSIVE,value); }
    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) { ScuttlerEntity child=BMEntities.SCUTTLER.create(level,EntitySpawnReason.BREEDING); if(child!=null) child.setPassive(true); return child; }
    @Override protected void addAdditionalSaveData(ValueOutput output){ super.addAdditionalSaveData(output); output.putBoolean("Passive",isPassive()); }
    @Override protected void readAdditionalSaveData(ValueInput input){ super.readAdditionalSaveData(input); setPassive(input.getBooleanOr("Passive",false)); }
    @Nullable @Override protected SoundEvent getDeathSound(){ return BMSounds.SCUTTLER_DEATH; }
    @Nullable @Override protected SoundEvent getHurtSound(DamageSource source){ return BMSounds.SCUTTLER_HURT; }
    @Override protected void playStepSound(BlockPos pos, BlockState state) {
        if (state.getFluidState().isEmpty()) {
            playSound(BMSounds.SCUTTLER_STEP, .10F, 1.25F + random.nextFloat());
            spawnSprintParticle();
        }
    }

    private final class RattleGoal extends Goal {
        private Player target;
        RattleGoal(){setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));}
        @Override public boolean canUse(){
            if(isInWater() || isPassive()) return false;
            target=null;double nearest=Double.MAX_VALUE;
            for(Player player:level().players())if(isThreatInRattleBand(player)){
                double distance=distanceToSqr(player);if(distance<nearest){nearest=distance;target=player;}
            }
            return target!=null;
        }
        @Override public void start(){entityData.set(RATTLING,true);} @Override public void stop(){entityData.set(RATTLING,false);target=null;}
        @Override public void tick(){if(target!=null)getLookControl().setLookAt(target,30,30);}
        private boolean isThreatInRattleBand(Player player) {
            if (player == null || player.isCreative() || player.isSpectator() || player.isHolding(ScuttlerEntity.this::isFood)) return false;
            double distance = distanceTo(player);
            return distance >= 10 && distance < 20 && hasLineOfSight(player) && player.hasLineOfSight(ScuttlerEntity.this);
        }
        @Override public boolean canContinueToUse(){ return !isInWater() && !isPassive() && isThreatInRattleBand(target); }
    }
    private final class EatFlowerGoal extends Goal {
        private BlockPos target;
        EatFlowerGoal(){setFlags(EnumSet.of(Flag.MOVE));}
        @Override public boolean canUse(){
            if(random.nextInt(80)!=0)return false; BlockPos center=blockPosition();
            for(BlockPos pos:BlockPos.betweenClosed(center.offset(-8,-3,-8),center.offset(8,3,8))) if(level().getBlockState(pos).is(BMBlocks.BARREL_CACTUS_FLOWERED)){target=pos.immutable();return true;}
            return false;
        }
        @Override public void tick(){ if(target==null)return; getNavigation().moveTo(target.getX()+.5,target.getY(),target.getZ()+.5,1); if(blockPosition().closerThan(target,2)){level().setBlock(target,BMBlocks.BARREL_CACTUS.defaultBlockState(),3);level().blockEvent(target,BMBlocks.BARREL_CACTUS,1,0);Block.popResource(level(),target,new ItemStack(BMItems.PINK_BUD,1+random.nextInt(2)));target=null;} }
        @Override public boolean canContinueToUse(){return target!=null&&level().getBlockState(target).is(BMBlocks.BARREL_CACTUS_FLOWERED);}
    }
}
