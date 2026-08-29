package party.lemons.biomemakeover.mixin.curse;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import party.lemons.biomemakeover.init.BMEnchantments;
import party.lemons.biomemakeover.item.enchantment.BMCurseEffects;

@Mixin(BowItem.class)
abstract class BowItemCurseMixin {
    @Inject(method = "shootProjectile", at = @At("HEAD"), cancellable = true)
    private void biomemakeover$applyInaccuracy(LivingEntity shooter, Projectile projectile, int index,
                                                float velocity, float inaccuracy, float angle,
                                                LivingEntity target, CallbackInfo ci) {
        ItemStack bow = shooter.getUseItem();
        int level = BMEnchantments.level(bow, shooter.registryAccess(), BMEnchantments.INACCURACY_CURSE);
        if (level <= 0) return;
        int pitchDirection = shooter.getRandom().nextBoolean() ? 1 : -1;
        int yawDirection = shooter.getRandom().nextBoolean() ? 1 : -1;
        float pitch = shooter.getXRot() + BMCurseEffects.inaccuracyDegrees(shooter.getRandom().nextFloat(), pitchDirection, level);
        float yaw = shooter.getYRot() + angle + BMCurseEffects.inaccuracyDegrees(shooter.getRandom().nextFloat(), yawDirection, level);
        projectile.shootFromRotation(shooter, pitch, yaw, 0.0F, velocity, inaccuracy);
        ci.cancel();
    }
}
