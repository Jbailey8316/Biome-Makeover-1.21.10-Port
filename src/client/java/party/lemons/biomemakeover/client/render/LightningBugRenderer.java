package party.lemons.biomemakeover.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.client.model.BMModelLayers;
import party.lemons.biomemakeover.client.model.LightningBugModel;
import party.lemons.biomemakeover.entity.LightningBugEntity;

public final class LightningBugRenderer extends MobRenderer<LightningBugEntity, SwampFlyingRenderState, LightningBugModel> {
    private static final ResourceLocation TEXTURE=BiomeMakeover.id("textures/entity/lightning_bug.png");
    public LightningBugRenderer(EntityRendererProvider.Context context){
        super(context,new LightningBugModel(context.bakeLayer(BMModelLayers.LIGHTNING_BUG)),.15F);
        addLayer(new BugLayer(this,new LightningBugModel(context.bakeLayer(BMModelLayers.LIGHTNING_BUG_INNER)),true));
        addLayer(new BugLayer(this,new LightningBugModel(context.bakeLayer(BMModelLayers.LIGHTNING_BUG_OUTER)),false));
    }
    @Override public SwampFlyingRenderState createRenderState(){return new SwampFlyingRenderState();}
    @Override public void extractRenderState(LightningBugEntity entity,SwampFlyingRenderState state,float partial){super.extractRenderState(entity,state,partial);state.alternate=entity.isAlternate();state.lightningScale=entity.advanceVisualScale(partial);state.lightningColor=entity.advanceVisualColor(partial);}
    @Override public ResourceLocation getTextureLocation(SwampFlyingRenderState state){return TEXTURE;}
    @Override protected int getBlockLightLevel(LightningBugEntity entity,net.minecraft.core.BlockPos pos){return 15;}
    @Override protected void scale(SwampFlyingRenderState state,PoseStack pose){pose.scale(state.lightningScale,state.lightningScale,state.lightningScale);state.shadowRadius=state.lightningScale/10F;}

    private static final class BugLayer extends RenderLayer<SwampFlyingRenderState,LightningBugModel>{
        private final LightningBugModel layerModel; private final boolean colored;
        private BugLayer(RenderLayerParent<SwampFlyingRenderState,LightningBugModel> parent,LightningBugModel model,boolean colored){super(parent);this.layerModel=model;this.colored=colored;}
        @Override public void submit(PoseStack pose,SubmitNodeCollector output,int light,SwampFlyingRenderState state,float yRot,float xRot){
            int color=colored?state.lightningColor:-1;
            output.order(0).submitModel(layerModel,state,pose,RenderType.entityTranslucent(TEXTURE),0x00F000F0,OverlayTexture.NO_OVERLAY,color,null,state.outlineColor,null);
        }
    }
}
