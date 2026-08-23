package party.lemons.biomemakeover.mixin.client;

import net.minecraft.client.renderer.entity.state.HorseRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import party.lemons.biomemakeover.client.render.CowboyHorseRenderState;

@Mixin(HorseRenderState.class)
public abstract class HorseRenderStateMixin implements CowboyHorseRenderState {
    @Unique private boolean biomemakeover$hasHat;
    @Override public boolean biomemakeover$hasHat(){ return biomemakeover$hasHat; }
    @Override public void biomemakeover$setHasHat(boolean value){ biomemakeover$hasHat=value; }
}
