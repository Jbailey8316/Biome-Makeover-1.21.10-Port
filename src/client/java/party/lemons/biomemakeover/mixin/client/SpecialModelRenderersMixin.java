package party.lemons.biomemakeover.mixin.client;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.client.render.MansionTapestryItemSpecialRenderer;

@Mixin(SpecialModelRenderers.class)
public abstract class SpecialModelRenderersMixin {
    @Shadow @Final private static ExtraCodecs.LateBoundIdMapper<ResourceLocation,
        MapCodec<? extends SpecialModelRenderer.Unbaked>> ID_MAPPER;

    @Inject(method = "bootstrap", at = @At("TAIL"))
    private static void biomemakeover$registerTapestry(CallbackInfo callbackInfo) {
        ID_MAPPER.put(BiomeMakeover.id("tapestry"), MansionTapestryItemSpecialRenderer.Unbaked.MAP_CODEC);
    }
}
