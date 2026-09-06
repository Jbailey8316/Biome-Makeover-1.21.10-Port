package party.lemons.biomemakeover.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableWitchTargetGoal;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import party.lemons.biomemakeover.crafting.witch.*;
import party.lemons.biomemakeover.crafting.witch.data.QuestCategories;
import party.lemons.biomemakeover.entity.ai.WitchLookAtCustomerGoal;
import party.lemons.biomemakeover.entity.ai.WitchStopFollowingCustomerGoal;
import party.lemons.biomemakeover.init.BMItems;

@Mixin(Witch.class)
public abstract class WitchMixin_Quests extends Raider implements WitchQuestEntity {
    @Shadow private NearestAttackableWitchTargetGoal<Player> attackPlayersGoal;
    private WitchQuestList bmQuests;
    private Player bmCustomer;
    private int bmReplenishTime;
    private int bmDespawnShield;
    protected WitchMixin_Quests(EntityType<? extends Raider> type, Level level) { super(type, level); }
    @Inject(method = "<init>", at = @At("TAIL")) private void bmInit(EntityType<? extends Witch> type, Level level, CallbackInfo ci) { bmQuests = new WitchQuestList(); bmReplenishTime = getRandom().nextInt(24000); }
    @Inject(method = "registerGoals", at = @At("TAIL")) private void bmGoals(CallbackInfo ci) {
        targetSelector.removeGoal(attackPlayersGoal);
        attackPlayersGoal = new NearestAttackableWitchTargetGoal<>(this, Player.class, 10, true, false,
            (entity, serverLevel) -> !(entity instanceof Player player) || !canInteract(player));
        targetSelector.addGoal(3, attackPlayersGoal);
        goalSelector.addGoal(1, new WitchStopFollowingCustomerGoal((Witch)(Object)this));
        goalSelector.addGoal(1, new WitchLookAtCustomerGoal((Witch)(Object)this));
    }
    @Inject(method = "customServerAiStep", at = @At("TAIL")) private void bmServerAiStep(ServerLevel level, CallbackInfo ci) {
        if (bmDespawnShield > 0) bmDespawnShield--;
        if (bmReplenishTime > 0) bmReplenishTime--;
        else if (QuestCategories.hasQuests()) { while (getQuests().size() < 3) getQuests().add(WitchQuestHandler.createQuest(getRandom())); bmReplenishTime = 21000 + getRandom().nextInt(3000); }
    }
    @Override public boolean canInteract(Player player) { return getTarget() == null && !hasActiveRaid() && player.getItemBySlot(EquipmentSlot.HEAD).is(BMItems.WITCH_HATS) && QuestCategories.hasQuests(); }
    @Override public boolean canAttack(LivingEntity target) { return target instanceof Player player && canInteract(player) ? false : super.canAttack(target); }
    @Override public void setCurrentCustomer(Player player) { bmCustomer = player; }
    @Override public Player getCurrentCustomer() { return bmCustomer; }
    @Override public WitchQuestList getQuests() { if (bmQuests == null) bmQuests = new WitchQuestList(); return bmQuests; }
    @Override public void setQuestsFromServer(WitchQuestList quests) { bmQuests = quests; }
    @Override public SoundEvent getYesSound() { return SoundEvents.WITCH_CELEBRATE; }
    @Override public Level getWitchLevel() { return level(); }
    @Inject(method = "die", at = @At("TAIL")) private void bmDie(DamageSource source, CallbackInfo ci) { bmCustomer = null; }
    @Inject(method = "dropFromLootTable", at = @At("TAIL"))
    private void bmHatLoot(ServerLevel level, DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE, party.lemons.biomemakeover.BiomeMakeover.id("entities/witch_hat"));
        LootParams params = new LootParams(level, new net.minecraft.util.context.ContextMap.Builder()
            .withParameter(LootContextParams.THIS_ENTITY, this)
            .withParameter(LootContextParams.ORIGIN, position())
            .withParameter(LootContextParams.DAMAGE_SOURCE, source)
            .withOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER, causedByPlayer && source.getEntity() instanceof Player p ? p : null)
            .create(LootTable.DEFAULT_PARAM_SET), java.util.Map.of(), 0.0F);
        level.getServer().reloadableRegistries().getLootTable(key).getRandomItems(params, stack -> spawnAtLocation(level, stack));
    }
    @Inject(method = "addAdditionalSaveData", at = @At("TAIL")) private void bmSave(ValueOutput output, CallbackInfo ci) { output.store("Quests", CompoundTag.CODEC, getQuests().toTag()); output.putInt("DespawnShield", bmDespawnShield); output.putInt("ReplenishTime", bmReplenishTime); }
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL")) private void bmLoad(ValueInput input, CallbackInfo ci) { bmQuests = input.read("Quests", CompoundTag.CODEC).map(WitchQuestList::new).orElseGet(WitchQuestList::new); bmDespawnShield = input.getIntOr("DespawnShield", 0); bmReplenishTime = input.getIntOr("ReplenishTime", 0); }
    @Override public boolean removeWhenFarAway(double distance) { return bmDespawnShield <= 0 && super.removeWhenFarAway(distance); }
    @Override public boolean requiresCustomPersistence() { return bmDespawnShield > 0 || super.requiresCustomPersistence(); }
}
