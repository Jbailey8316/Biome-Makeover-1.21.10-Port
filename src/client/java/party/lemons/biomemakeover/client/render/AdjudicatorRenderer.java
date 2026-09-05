package party.lemons.biomemakeover.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.client.model.AdjudicatorModel;
import party.lemons.biomemakeover.client.model.BMModelLayers;
import party.lemons.biomemakeover.entity.AdjudicatorEntity;

public final class AdjudicatorRenderer extends MobRenderer<AdjudicatorEntity, AdjudicatorRenderState, AdjudicatorModel> {
    private static final ResourceLocation TEXTURE = BiomeMakeover.id("textures/entity/adjudicator.png");
    public AdjudicatorRenderer(EntityRendererProvider.Context context) {
        super(context, new AdjudicatorModel(context.bakeLayer(BMModelLayers.ADJUDICATOR)), .25F);
    }
    @Override public AdjudicatorRenderState createRenderState() { return new AdjudicatorRenderState(); }
    @Override public void extractRenderState(AdjudicatorEntity entity, AdjudicatorRenderState state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
    }
    @Override public ResourceLocation getTextureLocation(AdjudicatorRenderState state) { return TEXTURE; }
}
