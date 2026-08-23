package party.lemons.biomemakeover.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.client.model.BMModelLayers;
import party.lemons.biomemakeover.client.model.LightningBugModel;
import party.lemons.biomemakeover.entity.LightningBugEntity;

public final class LightningBugRenderer extends MobRenderer<LightningBugEntity, SwampFlyingRenderState, LightningBugModel> {
    public LightningBugRenderer(EntityRendererProvider.Context context){ super(context,new LightningBugModel(context.bakeLayer(BMModelLayers.LIGHTNING_BUG)),.15F); }
    @Override public SwampFlyingRenderState createRenderState(){return new SwampFlyingRenderState();}
    @Override public void extractRenderState(LightningBugEntity entity,SwampFlyingRenderState state,float partial){super.extractRenderState(entity,state,partial);state.alternate=entity.isAlternate();}
    @Override public ResourceLocation getTextureLocation(SwampFlyingRenderState state){return BiomeMakeover.id("textures/entity/lightning_bug.png");}
    @Override protected int getBlockLightLevel(LightningBugEntity entity,net.minecraft.core.BlockPos pos){return 15;}
}
