package party.lemons.biomemakeover.mixin.client;

import net.minecraft.client.model.AbstractEquineModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.client.renderer.entity.state.HorseRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import party.lemons.biomemakeover.client.render.CowboyHorseRenderState;

@Mixin(AbstractEquineModel.class)
public abstract class HorseModelMixin {
    @Shadow protected ModelPart headParts;

    @Inject(method="setupAnim(Lnet/minecraft/client/renderer/entity/state/EquineRenderState;)V",at=@At("TAIL"))
    private void biomemakeover$exposeEarsThroughCowboyHat(EquineRenderState state, CallbackInfo ci) {
        if (!(state instanceof HorseRenderState horseState)
            || !((CowboyHorseRenderState)(Object)horseState).biomemakeover$hasHat()) return;

        ModelPart head=headParts.getChild("head");
        head.getChild("left_ear").y-=1.0F;
        head.getChild("right_ear").y-=1.0F;
    }
}
