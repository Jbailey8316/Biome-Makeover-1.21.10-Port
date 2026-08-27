package party.lemons.biomemakeover.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import party.lemons.biomemakeover.init.BMItems;
import party.lemons.biomemakeover.init.BMSounds;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

/** Final-release Rootling lifecycle and personality, without later progression dependencies. */
public final class RootlingEntity extends Animal implements Shearable {
    private static final EntityDataAccessor<Boolean> HAS_FLOWER = SynchedEntityData.defineId(RootlingEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> FLOWER_TYPE = SynchedEntityData.defineId(RootlingEntity.class, EntityDataSerializers.INT);
    private static final int BUD_COUNT = 6;
    private int actionCooldown;
    private int growTime;
    private boolean hasAction;

    public RootlingEntity(EntityType<? extends RootlingEntity> type, Level level) {
        super(type, level);
        actionCooldown = level.random.nextInt(501);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMobAttributes().add(Attributes.MAX_HEALTH, 10).add(Attributes.MOVEMENT_SPEED, .25)
            .add(Attributes.TEMPT_RANGE, 10);
    }

    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new PanicGoal(this, 1.25));
        goalSelector.addGoal(2, new RainGoal());
        Predicate<LivingEntity> shears = e -> e.getMainHandItem().is(Items.SHEARS) || e.getOffhandItem().is(Items.SHEARS);
        goalSelector.addGoal(4, new ShearsFleeGoal(shears));
        goalSelector.addGoal(5, new TemptGoal(this, 1, Ingredient.of(Items.BONE_MEAL), false));
        goalSelector.addGoal(6, new SocialGoal(true));
        goalSelector.addGoal(7, new SocialGoal(false));
        goalSelector.addGoal(8, new FlowerInspectGoal());
        goalSelector.addGoal(9, new WaterAvoidingRandomStrollGoal(this, 1));
        goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 6));
        goalSelector.addGoal(11, new RandomLookAroundGoal(this));
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder); builder.define(HAS_FLOWER, true); builder.define(FLOWER_TYPE, 0);
    }
    public boolean hasFlower() { return entityData.get(HAS_FLOWER); }
    public int flowerType() { return entityData.get(FLOWER_TYPE); }
    public void randomizeFlower() { entityData.set(FLOWER_TYPE, random.nextInt(BUD_COUNT)); }

    @Override protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (!hasAction) actionCooldown--;
        if (growTime > 0) {
            growTime--;
            if (isInWaterOrRain() && random.nextInt(5) == 0) growTime--;
            if (growTime <= 0 && !hasFlower()) { entityData.set(HAS_FLOWER, true); randomizeFlower(); }
        }
    }

    @Override public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.SHEARS)) {
            if (!level().isClientSide() && readyForShearing()) {
                shear((ServerLevel)level(), SoundSource.PLAYERS, stack);
                stack.hurtAndBreak(1, player, hand);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.CONSUME;
        }
        if (stack.is(Items.BONE_MEAL) && !hasFlower()) {
            if (!level().isClientSide()) {
                ((ServerLevel)level()).sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                    getX(), getY() + .5, getZ(), 8, .3, .3, .3, 0);
                if (random.nextInt(3) == 0) { entityData.set(HAS_FLOWER, true); randomizeFlower(); }
                stack.consume(1, player);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override public void shear(ServerLevel level, SoundSource source, ItemStack tool) {
        level.playSound(null, this, SoundEvents.SHEEP_SHEAR, source, 1, 1);
        int count = 2 + random.nextInt(3);
        level.addFreshEntity(new ItemEntity(level, getX(), getY() + .5, getZ(), new ItemStack(bud(flowerType()), count)));
        entityData.set(HAS_FLOWER, false);
        growTime = 600 + random.nextInt(601);
    }
    @Override public boolean readyForShearing() { return isAlive() && hasFlower(); }

    @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
            EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data); randomizeFlower(); return result;
    }
    @Override protected void addAdditionalSaveData(ValueOutput out) {
        super.addAdditionalSaveData(out); out.putInt("ActionCooldown", actionCooldown); out.putInt("GrowTime", growTime);
        out.putBoolean("HasFlower", hasFlower()); out.putInt("FlowerType", flowerType());
    }
    @Override protected void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in); actionCooldown=in.getIntOr("ActionCooldown",0); growTime=in.getIntOr("GrowTime",0);
        entityData.set(HAS_FLOWER,in.getBooleanOr("HasFlower",true)); entityData.set(FLOWER_TYPE,Math.floorMod(in.getIntOr("FlowerType",0),BUD_COUNT));
    }
    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob mate) { return null; }
    @Override public boolean isFood(ItemStack stack) { return false; }
    @Override protected SoundEvent getAmbientSound() { return BMSounds.ROOTLING_IDLE; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return BMSounds.ROOTLING_HURT; }
    @Override protected SoundEvent getDeathSound() { return BMSounds.ROOTLING_DEATH; }

    private static Item bud(int index) { return switch (Math.floorMod(index, BUD_COUNT)) {
        case 0 -> BMItems.BLUE_BUD; case 1 -> BMItems.BROWN_BUD; case 2 -> BMItems.CYAN_BUD;
        case 3 -> BMItems.GRAY_BUD; case 4 -> BMItems.LIGHT_BLUE_BUD; default -> BMItems.PURPLE_BUD;
    }; }

    private List<Animal> nearbyAnimals() { return level().getEntitiesOfClass(Animal.class, getBoundingBox().inflate(8), e -> e != this && e.isAlive()); }

    private final class SocialGoal extends Goal {
        private final boolean dance; private LivingEntity partner; private int timer;
        SocialGoal(boolean dance) { this.dance=dance; setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK)); }
        @Override public boolean canUse() {
            if (hasAction || actionCooldown >= 0 || random.nextInt(10) != 0) return false;
            List<Animal> list=nearbyAnimals().stream().filter(e -> dance == (e instanceof RootlingEntity)).toList();
            if(list.isEmpty()) return false; partner=list.get(random.nextInt(list.size())); return true;
        }
        @Override public void start(){hasAction=true;actionCooldown=500;timer=0;}
        @Override public boolean canContinueToUse(){return partner!=null&&partner.isAlive()&&timer<(dance?60:120);}
        @Override public void tick(){timer++;getLookControl().setLookAt(partner);getNavigation().moveTo(partner,1);if(distanceToSqr(partner)<(dance?9:4)){getNavigation().stop();if(dance)getJumpControl().jump();}}
        @Override public void stop(){partner=null;hasAction=false;timer=0;}
    }
    private final class FlowerInspectGoal extends Goal {
        private BlockPos target; private int timer;
        FlowerInspectGoal(){setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
        @Override public boolean canUse(){if(hasAction||actionCooldown>0||random.nextInt(10)==0)return false;BlockPos o=blockPosition();for(BlockPos p:BlockPos.betweenClosed(o.offset(-2,-1,-2),o.offset(2,1,2)))if(level().getBlockState(p).is(BlockTags.FLOWERS)){target=p.immutable();return true;}return false;}
        @Override public void start(){hasAction=true;actionCooldown=500;timer=0;}
        @Override public boolean canContinueToUse(){return target!=null&&level().getBlockState(target).is(BlockTags.FLOWERS)&&timer<200;}
        @Override public void tick(){timer++;getNavigation().moveTo(target.getX()+.5,target.getY(),target.getZ()+.5,.6);getLookControl().setLookAt(target.getX()+.5,target.getY()+.5,target.getZ()+.5);}
        @Override public void stop(){target=null;hasAction=false;timer=0;}
    }
    private final class RainGoal extends Goal {
        private Vec3 target;
        RainGoal(){setFlags(EnumSet.of(Flag.MOVE));}
        @Override public boolean canUse(){if(!level().isRaining()||level().canSeeSky(blockPosition()))return false;for(int i=0;i<10;i++){Vec3 v=DefaultRandomPos.getPos(RootlingEntity.this,10,3);if(v!=null&&level().canSeeSky(BlockPos.containing(v))){target=v;return true;}}return false;}
        @Override public void start(){getNavigation().moveTo(target.x,target.y,target.z,1);}
        @Override public boolean canContinueToUse(){return !getNavigation().isDone();}
    }
    private final class ShearsFleeGoal extends AvoidEntityGoal<LivingEntity> {
        private int soundTime;
        ShearsFleeGoal(Predicate<LivingEntity> predicate){super(RootlingEntity.this,LivingEntity.class,8,1.6,1.4,predicate);}
        @Override public void start(){super.start();if(soundTime==0)playSound(BMSounds.ROOTLING_AFRAID,1,1+random.nextFloat()/10);}
        @Override public void tick(){super.tick();if(++soundTime>100){playSound(BMSounds.ROOTLING_AFRAID,1,1+random.nextFloat()/10);soundTime=0;}}
    }
}
