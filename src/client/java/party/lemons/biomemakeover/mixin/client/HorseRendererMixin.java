package party.lemons.biomemakeover.mixin.client;

import net.minecraft.client.renderer.entity.HorseRenderer;
import net.minecraft.client.renderer.entity.state.HorseRenderState;
import net.minecraft.world.entity.animal.horse.Horse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import party.lemons.biomemakeover.client.render.CowboyHorseRenderState;
import party.lemons.biomemakeover.entity.HorseHat;

@Mixin(HorseRenderer.class)
public abstract class HorseRendererMixin {
    @Inject(method="extractRenderState(Lnet/minecraft/world/entity/animal/horse/Horse;Lnet/minecraft/client/renderer/entity/state/HorseRenderState;F)V",at=@At("RETURN"))
    private void biomemakeover$extractCowboyHat(Horse horse, HorseRenderState state, float tickProgress, CallbackInfo ci) {
        ((CowboyHorseRenderState)(Object)state).biomemakeover$setHasHat(((HorseHat)horse).biomemakeover$hasHat());
    }
}
