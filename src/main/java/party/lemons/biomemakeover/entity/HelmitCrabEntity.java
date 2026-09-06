package party.lemons.biomemakeover.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import party.lemons.biomemakeover.init.BMEntities;
import party.lemons.biomemakeover.init.BMBlocks;
import party.lemons.biomemakeover.init.BMItems;
import party.lemons.biomemakeover.init.BMSounds;
import java.util.EnumSet;

/** Final-release Helmit Crab behavior, adapted to the 1.21.10 entity APIs. */
public final class HelmitCrabEntity extends Animal {
    private static final EntityDataAccessor<ItemStack> SHELL = SynchedEntityData.defineId(HelmitCrabEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Boolean> HIDING = SynchedEntityData.defineId(HelmitCrabEntity.class, EntityDataSerializers.BOOLEAN);
    private int hideTime;

    public HelmitCrabEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
        getNavigation().setCanFloat(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createAnimalAttributes().add(Attributes.MAX_HEALTH, 10).add(Attributes.ATTACK_DAMAGE, 1).add(Attributes.MOVEMENT_SPEED, .25);
    }

    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new HideGoal());
        goalSelector.addGoal(2, new BreedGoal(this, 1));
        goalSelector.addGoal(3, new TemptGoal(this, 1.2, Ingredient.of(net.minecraft.core.registries.BuiltInRegistries.ITEM.getOrThrow(ItemTags.FISHES)), false));
        goalSelector.addGoal(4, new FollowParentGoal(this, 1.1));
        goalSelector.addGoal(5, new PanicGoal(this, 1.25));
        goalSelector.addGoal(6, new MeleeAttackGoal(this, 1, false));
        goalSelector.addGoal(7, new SeekShellGoal());
        goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1));
        goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 6));
        goalSelector.addGoal(10, new RandomLookAroundGoal(this));
    }

    @Override protected BodyRotationControl createBodyControl() { return new CrabBodyControl(this); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { super.defineSynchedData(builder); builder.define(SHELL, ItemStack.EMPTY); builder.define(HIDING, false); }
    public static boolean checkSpawnRules(EntityType<HelmitCrabEntity> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.below()).is(BMBlocks.CRAB_SPAWNABLE_ON) && Animal.isBrightEnoughToSpawn(level, pos);
    }
    @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        if (!isBaby() && random.nextFloat() < .6F) setShellItem(new ItemStack(Items.NAUTILUS_SHELL));
        return result;
    }
    @Override protected void customServerAiStep(ServerLevel level) { super.customServerAiStep(level); if (isHiding() && ++hideTime > 250 && random.nextInt(100) == 0) setHiding(false); }
    @Override protected void addAdditionalSaveData(ValueOutput out) { super.addAdditionalSaveData(out); out.store("Shell", ItemStack.CODEC, getShellItemStack()); out.putBoolean("Hiding", isHiding()); out.putInt("HideTime", hideTime); }
    @Override protected void readAdditionalSaveData(ValueInput in) { super.readAdditionalSaveData(in); in.read("Shell", ItemStack.CODEC).ifPresent(this::setShellItem); entityData.set(HIDING, in.getBooleanOr("Hiding", false)); hideTime = in.getIntOr("HideTime", 0); }
    @Override public boolean isFood(ItemStack stack) { return stack.is(ItemTags.FISHES); }
    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob mate) { return BMEntities.HELMIT_CRAB.create(level, EntitySpawnReason.BREEDING); }
    @Override public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (prefersShell(stack)) { if (!level().isClientSide()) { setShellItem(stack.copyWithCount(1)); if (!player.getAbilities().instabuild) stack.consume(1, player); } return InteractionResult.SUCCESS; }
        return super.mobInteract(player, hand);
    }
    @Override public boolean doHurtTarget(ServerLevel level, Entity target) { playSound(BMSounds.CRAB_SNIP, .5F, 1); return super.doHurtTarget(level, target); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return BMSounds.CRAB_HURT; }
    @Override protected SoundEvent getDeathSound() { return BMSounds.CRAB_DEATH; }
    @Override protected void playStepSound(BlockPos pos, BlockState state) { playSound(BMSounds.CRAB_SCUTTLE, .1F, 1); }
    @Override public float getWalkTargetValue(BlockPos pos, LevelReader level) { BlockState below = level.getBlockState(pos.below()); return below.is(Blocks.SAND) ? 10 : below.is(Blocks.WATER) ? 7 : level.getLightEmission(pos) - .5F; }
    public ItemStack getShellItemStack() { return entityData.get(SHELL); }
    public void setShellItem(ItemStack stack) { entityData.set(SHELL, stack); }
    public boolean isHiding() { return entityData.get(HIDING); }
    private void setHiding(boolean hiding) { entityData.set(HIDING, hiding); if (hiding) { playSound(BMSounds.CRAB_ENTER_SHELL); getNavigation().stop(); setTarget(null); hideTime = 0; } else playSound(BMSounds.CRAB_LEAVE_SHELL); }
    public boolean prefersShell(ItemStack stack) { return !isBaby() && !stack.isEmpty() && (stack.is(Items.NAUTILUS_SHELL) || stack.is(Items.SHULKER_SHELL) || stack.is(Blocks.CARVED_PUMPKIN.asItem()) || stack.is(ItemTags.HEAD_ARMOR)); }

    private final class HideGoal extends Goal {
        HideGoal() { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }
        @Override public boolean canUse() { return !isHiding() && canHide() && (isOnFire() || getLastHurtByMob() != null); }
        @Override public void start() { setHiding(true); }
        private boolean canHide() { return !getShellItemStack().isEmpty(); }
    }
    private final class SeekShellGoal extends Goal {
        private ItemEntity target;
        SeekShellGoal() { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }
        @Override public boolean canUse() { if (isHiding()) return false; target = level().getEntitiesOfClass(ItemEntity.class, getBoundingBox().inflate(8), e -> prefersShell(e.getItem())).stream().findFirst().orElse(null); return target != null; }
        @Override public boolean canContinueToUse() { return target != null && target.isAlive() && !prefersShell(getShellItemStack()); }
        @Override public void tick() { if (target != null) { getNavigation().moveTo(target, 1); getLookControl().setLookAt(target); if (distanceToSqr(target) < 2) { setShellItem(target.getItem().split(1)); target.setItem(target.getItem()); } } }
        @Override public void stop() { target = null; }
    }
    private final class CrabBodyControl extends BodyRotationControl { CrabBodyControl(Mob mob) { super(mob); } }
}
