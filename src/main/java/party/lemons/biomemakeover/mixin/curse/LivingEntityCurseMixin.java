package party.lemons.biomemakeover.mixin.curse;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import party.lemons.biomemakeover.init.BMEnchantments;
import party.lemons.biomemakeover.item.enchantment.BMCurseEffects;

@Mixin(LivingEntity.class)
abstract class LivingEntityCurseMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void biomemakeover$tickCurses(CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (self.level() instanceof ServerLevel server) BMCurseEffects.tick(self, server);
    }

    @ModifyArg(method = "causeFallDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;calculateFallDamage(DF)I"), index = 0)
    private double biomemakeover$applyBuckling(double distance) {
        LivingEntity self = (LivingEntity)(Object)this;
        int level = BMEnchantments.equippedLevel(self, EquipmentSlot.LEGS, BMEnchantments.BUCKLING_CURSE);
        return BMCurseEffects.buckledDistance(distance, level);
    }

}
