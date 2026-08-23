package party.lemons.biomemakeover.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.client.model.BMModelLayers;
import party.lemons.biomemakeover.client.model.CowboyHatModel;

/** 1.21.10 render-state translation of the released Fabric HatArmorRenderer. */
public final class CowboyHatArmorRenderer implements ArmorRenderer {
    private static final ResourceLocation TEXTURE=BiomeMakeover.id("textures/misc/cowboy_hat.png");
    private final CowboyHatModel<HumanoidRenderState> hat;

    public CowboyHatArmorRenderer(net.minecraft.client.model.geom.EntityModelSet models) {
        hat=new CowboyHatModel<>(models.bakeLayer(BMModelLayers.COWBOY_HAT));
    }

    @Override public boolean shouldRenderDefaultHeadItem(LivingEntity entity, ItemStack stack) {
        return false;
    }

    @Override public void render(PoseStack pose, SubmitNodeCollector output, ItemStack stack,
                                 HumanoidRenderState state, EquipmentSlot slot, int light,
                                 HumanoidModel<HumanoidRenderState> contextModel) {
        pose.pushPose();
        contextModel.root().getChild("head").translateAndRotate(pose);
        // HatArmorRenderer copied the animated head over the model's baked
        // +2-pixel pivot. Apply the same result in the submit-node pipeline.
        pose.scale(1.05F,1.05F,1.05F);
        pose.translate(0,-0.125F,0);
        output.order(0).submitModel(hat,state,pose,RenderType.entityCutoutNoCull(TEXTURE),light,
            OverlayTexture.NO_OVERLAY,-1,null,state.outlineColor,null);
        pose.popPose();
    }
}
