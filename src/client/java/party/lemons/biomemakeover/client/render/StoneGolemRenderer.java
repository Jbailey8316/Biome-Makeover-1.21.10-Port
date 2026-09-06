package party.lemons.biomemakeover.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.client.model.BMModelLayers;
import party.lemons.biomemakeover.client.model.StoneGolemModel;
import party.lemons.biomemakeover.entity.StoneGolemEntity;

public final class StoneGolemRenderer extends MobRenderer<StoneGolemEntity, StoneGolemRenderState, StoneGolemModel> {
    private static final ResourceLocation TEXTURE = BiomeMakeover.id("textures/entity/stone_golem/stone_golem.png");
    private final ItemModelResolver itemModelResolver;
    public StoneGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new StoneGolemModel(context.bakeLayer(BMModelLayers.STONE_GOLEM)), 1.0F);
        itemModelResolver = context.getItemModelResolver();
        addLayer(new StoneGolemItemLayer(this));
    }
    @Override public StoneGolemRenderState createRenderState() { return new StoneGolemRenderState(); }
    @Override public void extractRenderState(StoneGolemEntity entity, StoneGolemRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        ArmedEntityRenderState.extractArmedEntityRenderState(entity, state, itemModelResolver);
        state.charging = entity.isChargingCrossbow();
        state.holdingCrossbow = entity.isHolding(net.minecraft.world.item.Items.CROSSBOW);
        state.attackAnimation = entity.getAttackAnim(partialTick);
    }
    @Override public ResourceLocation getTextureLocation(StoneGolemRenderState state) { return TEXTURE; }

    /** Direct modern equivalent of released StoneGolemItemLayer. */
    private static final class StoneGolemItemLayer extends RenderLayer<StoneGolemRenderState, StoneGolemModel> {
        private StoneGolemItemLayer(RenderLayerParent<StoneGolemRenderState, StoneGolemModel> parent) { super(parent); }

        @Override public void submit(PoseStack pose, SubmitNodeCollector output, int light,
                                      StoneGolemRenderState state, float yRot, float xRot) {
            if (state.rightHandItem.isEmpty() && state.leftHandItem.isEmpty()) return;
            pose.pushPose();
            pose.translate(0.0F, 0.5F, 0.0F);
            renderItem(state.rightHandItem, HumanoidArm.RIGHT, state, pose, output, light);
            renderItem(state.leftHandItem, HumanoidArm.LEFT, state, pose, output, light);
            pose.popPose();
        }

        private void renderItem(ItemStackRenderState item, HumanoidArm arm, StoneGolemRenderState state,
                                PoseStack pose, SubmitNodeCollector output, int light) {
            if (item.isEmpty()) return;
            pose.pushPose();
            ((ArmedModel) getParentModel()).translateToHand(state, arm, pose);
            pose.mulPose(Axis.XP.rotationDegrees(-90.0F));
            pose.mulPose(Axis.YP.rotationDegrees(180.0F));
            pose.translate((arm == HumanoidArm.LEFT ? -0.7F : 0.7F) / 16.0F, 0.125D, -1.75D);
            item.submit(pose, output, light, 0, 0);
            pose.popPose();
        }
    }
}
