package party.lemons.biomemakeover.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.client.model.BMModelLayers;
import party.lemons.biomemakeover.client.model.ScuttlerModel;
import party.lemons.biomemakeover.entity.ScuttlerEntity;

public final class ScuttlerRenderer extends MobRenderer<ScuttlerEntity,LivingEntityRenderState,ScuttlerModel> {
    private static final ResourceLocation TEXTURE=BiomeMakeover.id("textures/entity/scuttler.png");
    public ScuttlerRenderer(EntityRendererProvider.Context context){super(context,new ScuttlerModel(context.bakeLayer(BMModelLayers.SCUTTLER)),.25F);}
    @Override public LivingEntityRenderState createRenderState(){return new LivingEntityRenderState();}
    @Override public ResourceLocation getTextureLocation(LivingEntityRenderState state){return TEXTURE;}
}
