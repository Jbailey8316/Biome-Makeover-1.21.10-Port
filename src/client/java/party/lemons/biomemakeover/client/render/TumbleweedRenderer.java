package party.lemons.biomemakeover.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import party.lemons.biomemakeover.init.BMBlocks;

public final class TumbleweedRenderer extends EntityRenderer<Entity,EntityRenderState> {
    public TumbleweedRenderer(EntityRendererProvider.Context context){ super(context); shadowRadius=.35F; }
    @Override public EntityRenderState createRenderState(){ return new EntityRenderState(); }
    @Override public void submit(EntityRenderState state, PoseStack pose, SubmitNodeCollector output, CameraRenderState camera){
        pose.pushPose(); pose.translate(-.5,0,-.5); output.submitBlock(pose,BMBlocks.TUMBLEWEED.defaultBlockState(),state.lightCoords,OverlayTexture.NO_OVERLAY,-1); pose.popPose();
        super.submit(state,pose,output,camera);
    }
}
