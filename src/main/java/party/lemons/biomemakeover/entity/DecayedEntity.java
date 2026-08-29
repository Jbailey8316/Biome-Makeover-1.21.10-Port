package party.lemons.biomemakeover.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.block.state.BlockState;
import party.lemons.biomemakeover.init.BMSounds;
import party.lemons.biomemakeover.init.BMEnchantments;

/** Released amphibious zombie contract, translated onto the modern Drowned navigation implementation. */
public final class DecayedEntity extends Drowned {
    private int shieldHealth = 30;
    public DecayedEntity(EntityType<? extends Drowned> type, Level level) { super(type, level); }
    public static AttributeSupplier.Builder createAttributes() { return Monster.createMonsterAttributes().add(Attributes.FOLLOW_RANGE, 16).add(Attributes.MOVEMENT_SPEED, .23).add(Attributes.ATTACK_DAMAGE, 3).add(Attributes.ARMOR, 2).add(Attributes.SPAWN_REINFORCEMENTS_CHANCE); }
    public static boolean checkSpawnRules(EntityType<DecayedEntity> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL && random.nextInt(3) == 0 && Drowned.checkDrownedSpawnRules((EntityType)type, level, reason, pos, random);
    }
    @Override protected void populateDefaultEquipmentSlots(RandomSource random, net.minecraft.world.DifficultyInstance difficulty) {
        super.populateDefaultEquipmentSlots(random, difficulty);
        ItemStack shield = new ItemStack(Items.SHIELD);
        BMEnchantments.holder(registryAccess(), BMEnchantments.DECAY_CURSE)
            .ifPresent(decay -> shield.enchant(decay, 1 + random.nextInt(4)));
        setItemSlot(EquipmentSlot.OFFHAND, shield);
    }
    @Override protected boolean convertsInWater() { return false; }
    @Override protected SoundEvent getAmbientSound(){return isInWater()?BMSounds.DECAYED_AMBIENT_WATER:BMSounds.DECAYED_AMBIENT;}
    @Override protected SoundEvent getHurtSound(DamageSource source){return isInWater()?BMSounds.DECAYED_HURT_WATER:BMSounds.DECAYED_HURT;}
    @Override protected SoundEvent getDeathSound(){return isInWater()?BMSounds.DECAYED_DEATH_WATER:BMSounds.DECAYED_DEATH;}
    @Override protected void playStepSound(BlockPos pos,BlockState state){playSound(BMSounds.DECAYED_STEP,.15F,1F);}
    @Override protected void addAdditionalSaveData(ValueOutput output) { super.addAdditionalSaveData(output); output.putInt("ShieldHealth", shieldHealth); }
    @Override protected void readAdditionalSaveData(ValueInput input) { super.readAdditionalSaveData(input); shieldHealth = input.getIntOr("ShieldHealth", 30); }
}
