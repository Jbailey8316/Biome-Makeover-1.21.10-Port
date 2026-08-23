package party.lemons.biomemakeover.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import party.lemons.biomemakeover.init.BMBlocks;
import party.lemons.biomemakeover.entity.TumbleweedEntity;
import com.mojang.math.Axis;

public final class TumbleweedRenderer extends EntityRenderer<TumbleweedEntity,TumbleweedRenderState> {
    public TumbleweedRenderer(EntityRendererProvider.Context context){ super(context); shadowRadius=.35F; }
    @Override public TumbleweedRenderState createRenderState(){ return new TumbleweedRenderState(); }
    @Override public void extractRenderState(TumbleweedEntity entity, TumbleweedRenderState state, float tickProgress) {
        super.extractRenderState(entity,state,tickProgress);
        state.rotation.set(entity.previousQuaternion).slerp(entity.quaternion,tickProgress);
    }
    @Override public void submit(TumbleweedRenderState state, PoseStack pose, SubmitNodeCollector output, CameraRenderState camera){
        pose.pushPose();
        pose.translate(0,.5,0);
        pose.mulPose(state.rotation);
        pose.mulPose(Axis.YP.rotationDegrees(-90));
        pose.translate(-.5,-.5,.5);
        pose.mulPose(Axis.YP.rotationDegrees(90));
        output.submitBlock(pose,BMBlocks.TUMBLEWEED.defaultBlockState(),state.lightCoords,OverlayTexture.NO_OVERLAY,0);
        pose.popPose();
        super.submit(state,pose,output,camera);
    }
}
