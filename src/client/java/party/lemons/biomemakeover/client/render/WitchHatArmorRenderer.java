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
import party.lemons.biomemakeover.client.model.WitchHatModel;

/** Modern submit-node translation of final Fabric HatArmorRenderer. */
public final class WitchHatArmorRenderer implements ArmorRenderer {
    private static final ResourceLocation TEXTURE = BiomeMakeover.id("textures/misc/witch_hat.png");
    private final WitchHatModel hat;

    public WitchHatArmorRenderer(net.minecraft.client.model.geom.EntityModelSet models) {
        hat = new WitchHatModel(models.bakeLayer(BMModelLayers.WITCH_HAT));
    }

    @Override
    public boolean shouldRenderDefaultHeadItem(LivingEntity entity, ItemStack stack) {
        return false;
    }

    @Override
    public void render(PoseStack pose, SubmitNodeCollector output, ItemStack stack,
                       HumanoidRenderState state, EquipmentSlot slot, int light,
                       HumanoidModel<HumanoidRenderState> contextModel) {
        pose.pushPose();
        contextModel.root().getChild("head").translateAndRotate(pose);
        output.order(0).submitModel(hat, state, pose, RenderType.entityCutoutNoCull(TEXTURE), light,
            OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor, null);
        pose.popPose();
    }
}
