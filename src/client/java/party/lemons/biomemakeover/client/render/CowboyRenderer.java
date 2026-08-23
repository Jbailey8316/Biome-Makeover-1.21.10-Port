package party.lemons.biomemakeover.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.PillagerRenderer;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.resources.ResourceLocation;
import party.lemons.biomemakeover.BiomeMakeover;

public final class CowboyRenderer extends PillagerRenderer {
    private static final ResourceLocation TEXTURE=BiomeMakeover.id("textures/entity/cowboy.png");
    public CowboyRenderer(EntityRendererProvider.Context context){
        super(context);
        addLayer(new CowboyHatLayer<>(this,context.getModelSet(),false));
    }
    @Override public ResourceLocation getTextureLocation(IllagerRenderState state){return TEXTURE;}
}
