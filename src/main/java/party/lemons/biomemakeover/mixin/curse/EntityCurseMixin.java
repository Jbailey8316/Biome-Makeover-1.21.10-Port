package party.lemons.biomemakeover.mixin.curse;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import party.lemons.biomemakeover.init.BMEnchantments;
import party.lemons.biomemakeover.item.enchantment.BMCurseEffects;

@Mixin(Entity.class)
abstract class EntityCurseMixin {
    @Shadow private int remainingFireTicks;

    @Inject(method = "setRemainingFireTicks", at = @At("HEAD"), cancellable = true)
    private void biomemakeover$extendNewFire(int ticks, CallbackInfo ci) {
        if (ticks <= remainingFireTicks || !((Object)this instanceof LivingEntity living)) return;
        int level = 0;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            level = Math.max(level, BMEnchantments.equippedLevel(living, slot, BMEnchantments.FLAMMABILITY_CURSE));
        }
        if (level > 0) {
            remainingFireTicks = BMCurseEffects.extendedFireTicks(ticks, level);
            ci.cancel();
        }
    }

    @Inject(method = "updateSwimming", at = @At("HEAD"), cancellable = true)
    private void biomemakeover$preventSwimming(CallbackInfo ci) {
        if ((Object)this instanceof LivingEntity living && living.isSwimming()
                && BMEnchantments.equippedLevel(living, EquipmentSlot.FEET, BMEnchantments.DEPTH_CURSE) > 0) {
            living.setSwimming(false);
            ci.cancel();
        }
    }

}
