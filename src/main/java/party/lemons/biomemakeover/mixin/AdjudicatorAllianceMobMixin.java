package party.lemons.biomemakeover.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import party.lemons.biomemakeover.entity.AdjudicatorAlliance;

@Mixin(Mob.class)
public abstract class AdjudicatorAllianceMobMixin {
    @Inject(method = "setTarget", at = @At("TAIL"))
    private void biomemakeover$rejectFriendlyTarget(LivingEntity target, CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        if (target != null && AdjudicatorAlliance.allied(mob, target)) {
            mob.setTarget(null);
        }
    }
}
