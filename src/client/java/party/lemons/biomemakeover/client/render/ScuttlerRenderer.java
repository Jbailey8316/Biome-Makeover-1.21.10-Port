package party.lemons.biomemakeover.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.client.model.BMModelLayers;
import party.lemons.biomemakeover.client.model.ScuttlerModel;
import party.lemons.biomemakeover.entity.ScuttlerEntity;

public final class ScuttlerRenderer extends MobRenderer<ScuttlerEntity,ScuttlerRenderState,ScuttlerModel> {
    private static final ResourceLocation TEXTURE=BiomeMakeover.id("textures/entity/scuttler.png");
    public ScuttlerRenderer(EntityRendererProvider.Context context){super(context,new ScuttlerModel(context.bakeLayer(BMModelLayers.SCUTTLER)),.25F);}
    @Override public ScuttlerRenderState createRenderState(){return new ScuttlerRenderState();}
    @Override public void extractRenderState(ScuttlerEntity entity, ScuttlerRenderState state, float tickProgress) {
        super.extractRenderState(entity,state,tickProgress);
        state.rattleTime=entity.getRattleTime(tickProgress);
    }
    @Override public ResourceLocation getTextureLocation(ScuttlerRenderState state){return TEXTURE;}
}
