package party.lemons.biomemakeover.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.client.model.BMModelLayers;
import party.lemons.biomemakeover.client.model.DragonflyModel;
import party.lemons.biomemakeover.entity.DragonflyEntity;

public final class DragonflyRenderer extends MobRenderer<DragonflyEntity, SwampFlyingRenderState, DragonflyModel> {
    public DragonflyRenderer(EntityRendererProvider.Context context) { super(context,new DragonflyModel(context.bakeLayer(BMModelLayers.DRAGONFLY)),.2F); }
    @Override public SwampFlyingRenderState createRenderState(){ return new SwampFlyingRenderState(); }
    @Override public void extractRenderState(DragonflyEntity entity, SwampFlyingRenderState state, float partial){ super.extractRenderState(entity,state,partial); state.variant=entity.getVariant(); }
    @Override public ResourceLocation getTextureLocation(SwampFlyingRenderState state){ return BiomeMakeover.id("textures/entity/dragonfly/dragonfly_"+state.variant+".png"); }
}
