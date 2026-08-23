package party.lemons.biomemakeover.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HorseRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.client.model.BMModelLayers;
import party.lemons.biomemakeover.client.model.CowboyHatModel;

public final class CowboyHatLayer<S extends LivingEntityRenderState,M extends EntityModel<? super S>> extends RenderLayer<S,M> {
    private static final ResourceLocation TEXTURE=BiomeMakeover.id("textures/misc/cowboy_hat.png");
    private final CowboyHatModel<S> hat;
    private final boolean horse;

    public CowboyHatLayer(RenderLayerParent<S,M> parent, net.minecraft.client.model.geom.EntityModelSet models, boolean horse) {
        super(parent);
        this.hat=new CowboyHatModel<>(models.bakeLayer(BMModelLayers.COWBOY_HAT));
        this.horse=horse;
    }

    @Override public void submit(PoseStack pose, SubmitNodeCollector output, int light, S state, float yRot, float xRot) {
        if(horse && (!(state instanceof CowboyHorseRenderState horseState) || !horseState.biomemakeover$hasHat())) return;
        pose.pushPose();
        if(horse) {
            pose.scale(1.05F,1.05F,1.05F);
            getParentModel().root().getChild("head_parts").translateAndRotate(pose);
            pose.translate(0,-.4F,0);
            pose.mulPose(Axis.XP.rotationDegrees(-25));
        } else {
            getParentModel().root().getChild("head").translateAndRotate(pose);
            pose.translate(0,-.2F,0);
        }
        pose.scale(1.05F,1.05F,1.05F);
        renderColoredCutoutModel(hat,TEXTURE,pose,output,light,state,-1,LivingEntityRenderer.getOverlayCoords(state,0));
        pose.popPose();
    }
}
