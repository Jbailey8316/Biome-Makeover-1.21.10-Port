package party.lemons.biomemakeover.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import party.lemons.biomemakeover.entity.AdjudicatorAlliance;

@Mixin(Entity.class)
public abstract class AdjudicatorAllianceEntityMixin {
    @Inject(method = "isAlliedTo", at = @At("HEAD"), cancellable = true)
    private void biomemakeover$encounterAlliance(Entity other, CallbackInfoReturnable<Boolean> cir) {
        if (AdjudicatorAlliance.allied((Entity) (Object) this, other)) cir.setReturnValue(true);
    }
}
