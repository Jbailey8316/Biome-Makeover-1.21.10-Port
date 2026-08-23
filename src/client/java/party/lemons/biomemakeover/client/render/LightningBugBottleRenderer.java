package party.lemons.biomemakeover.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.block.LightningBugBottleBlock;
import party.lemons.biomemakeover.block.entity.LightningBugBottleBlockEntity;
import party.lemons.biomemakeover.client.model.BMModelLayers;
import party.lemons.biomemakeover.client.model.LightningBugModel;

/** Modern render-state translation of the released synthetic bug inside the luminous bottle. */
public final class LightningBugBottleRenderer implements BlockEntityRenderer<LightningBugBottleBlockEntity,LightningBugBottleRenderer.State> {
    private static final ResourceLocation TEXTURE=BiomeMakeover.id("textures/entity/lightning_bug.png");
    private final LightningBugModel inner,outer;
    public LightningBugBottleRenderer(BlockEntityRendererProvider.Context context) {
        inner=new LightningBugModel(context.bakeLayer(BMModelLayers.LIGHTNING_BUG_INNER));
        outer=new LightningBugModel(context.bakeLayer(BMModelLayers.LIGHTNING_BUG_OUTER));
    }
    @Override public State createRenderState(){return new State();}
    @Override public void extractRenderState(LightningBugBottleBlockEntity entity,State state,float partial,net.minecraft.world.phys.Vec3 camera,net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay breaking){
        BlockEntityRenderer.super.extractRenderState(entity,state,partial,camera,breaking);
        state.upper=entity.getBlockState().getValue(LightningBugBottleBlock.UPPER);
        long time=entity.getLevel()==null?0:entity.getLevel().getGameTime();
        state.scale=.9F+Mth.sin((time+partial+(entity.getBlockPos().hashCode()&255))/10F)/5F;
        int hash=entity.getBlockPos().hashCode();
        state.color=0xFF000000|((hash&255)<<16)|(((hash>>>8)&255)<<8)|((hash>>>16)&255);
    }
    @Override public void submit(State state,PoseStack pose,SubmitNodeCollector output,CameraRenderState camera){
        pose.pushPose();
        pose.translate(.5D,state.upper?.25D:0D,.5D);
        pose.scale(-state.scale,-state.scale,state.scale);
        pose.translate(0D,-1.501D,0D);
        SwampFlyingRenderState bugState=new SwampFlyingRenderState();
        output.order(0).submitModel(inner,bugState,pose,RenderType.entityTranslucent(TEXTURE),0x00F000F0,OverlayTexture.NO_OVERLAY,state.color,null,-1,state.breakProgress);
        output.order(0).submitModel(outer,bugState,pose,RenderType.entityTranslucent(TEXTURE),0x00F000F0,OverlayTexture.NO_OVERLAY,-1,null,-1,state.breakProgress);
        pose.popPose();
    }
    public static final class State extends BlockEntityRenderState { boolean upper;float scale=1F;int color=-1; }
}
