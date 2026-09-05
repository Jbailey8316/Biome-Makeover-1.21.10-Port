package party.lemons.biomemakeover.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import party.lemons.biomemakeover.entity.AdjudicatorAlliance;
import net.minecraft.world.entity.Entity;

@Mixin(LivingEntity.class)
public abstract class AdjudicatorAllianceDamageMixin {
    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void biomemakeover$blockFriendlyDamage(ServerLevel level, DamageSource source, float amount,
                                                    CallbackInfoReturnable<Boolean> cir) {
        Entity attacker = AdjudicatorAlliance.resolveAttacker(source);
        Entity victim = (Entity) (Object) this;
        if (AdjudicatorAlliance.allied(attacker, victim)) {
            AdjudicatorAlliance.friendlyDamageBlocked(attacker, victim, source);
            cir.setReturnValue(false);
        }
    }
}
