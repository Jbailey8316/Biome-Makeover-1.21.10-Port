package party.lemons.biomemakeover.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import party.lemons.biomemakeover.crafting.witch.WitchQuestEntity;

@Mixin(Mob.class)
public abstract class WitchMixin_MobHooks extends LivingEntity {
    protected WitchMixin_MobHooks(EntityType<? extends LivingEntity> type, net.minecraft.world.level.Level level) { super(type, level); }
    @Inject(method = "registerGoals", at = @At("TAIL")) private void biomemakeover$registerQuestGoals(CallbackInfo ci) {
        if ((Object) this instanceof WitchQuestEntity witch) witch.configureQuestGoals();
    }
    @Inject(method = "customServerAiStep", at = @At("TAIL")) private void biomemakeover$tickQuestState(ServerLevel level, CallbackInfo ci) {
        if ((Object) this instanceof WitchQuestEntity witch) witch.tickQuestState(level);
    }
    @Inject(method = "addAdditionalSaveData", at = @At("TAIL")) private void biomemakeover$saveQuestData(ValueOutput output, CallbackInfo ci) {
        if ((Object) this instanceof WitchQuestEntity witch) witch.saveQuestData(output);
    }
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL")) private void biomemakeover$loadQuestData(ValueInput input, CallbackInfo ci) {
        if ((Object) this instanceof WitchQuestEntity witch) witch.loadQuestData(input);
    }
}
