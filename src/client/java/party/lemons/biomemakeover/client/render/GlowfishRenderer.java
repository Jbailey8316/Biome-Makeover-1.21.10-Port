package party.lemons.biomemakeover.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SalmonRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.SalmonRenderState;
import net.minecraft.client.model.SalmonModel;
import net.minecraft.resources.ResourceLocation;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.init.BMBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.animal.Salmon;

public final class GlowfishRenderer extends SalmonRenderer {
    private static final ResourceLocation TEXTURE = BiomeMakeover.id("textures/entity/glow_fish.png");
    public GlowfishRenderer(EntityRendererProvider.Context context) {
        super(context);
        addLayer(new GlowshroomLayer(this));
    }
    @Override public ResourceLocation getTextureLocation(SalmonRenderState state) { return TEXTURE; }
    @Override public void extractRenderState(Salmon entity, SalmonRenderState state, float tickProgress) {
        super.extractRenderState(entity, state, tickProgress);
        state.lightCoords = 0x00F000F0;
    }
    @Override public void submit(SalmonRenderState state, PoseStack pose, SubmitNodeCollector output, CameraRenderState camera) {
        var topBackFin = model.root().getChild("body_back").getChild("top_back_fin");
        boolean finVisible = topBackFin.visible;
        if (!state.isBaby && !state.isInvisible) topBackFin.visible = false;
        super.submit(state, pose, output, camera);
        topBackFin.visible = finVisible;
    }

    private static final class GlowshroomLayer extends RenderLayer<SalmonRenderState,SalmonModel> {
        private GlowshroomLayer(RenderLayerParent<SalmonRenderState,SalmonModel> parent) { super(parent); }

        @Override public void submit(PoseStack pose, SubmitNodeCollector output, int light, SalmonRenderState state,
                                     float yRot, float xRot) {
            if (state.isBaby || state.isInvisible) return;
            pose.pushPose();
            getParentModel().root().getChild("body_back").translateAndRotate(pose);
            pose.translate(0.0, 0.0, 0.5);
            pose.mulPose(Axis.XP.rotationDegrees(-90.0F));
            pose.scale(-0.75F, -0.75F, 0.75F);
            pose.translate(-0.5, 0.0, -0.5);
            output.submitBlock(pose, BMBlocks.ORANGE_GLOWSHROOM.defaultBlockState(), light,
                LivingEntityRenderer.getOverlayCoords(state,0.0F), 0);
            pose.popPose();
        }
    }
}
