package party.lemons.biomemakeover.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Mth;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.client.model.BMModelLayers;
import party.lemons.biomemakeover.client.model.OwlModel;
import party.lemons.biomemakeover.client.render.state.OwlRenderState;
import party.lemons.biomemakeover.entity.OwlEntity;

public final class OwlRenderer extends MobRenderer<OwlEntity, OwlRenderState, OwlModel> {
    private static final ResourceLocation TEXTURE = BiomeMakeover.id("textures/entity/owl.png");
    private static final ResourceLocation HEDWIG_TEXTURE = BiomeMakeover.id("textures/entity/owl_2.png");
    private static final ResourceLocation EYES_TEXTURE = BiomeMakeover.id("textures/entity/owl_eyes.png");

    public OwlRenderer(EntityRendererProvider.Context context) {
        super(context, new OwlModel(context.bakeLayer(BMModelLayers.OWL)), 0.45F);
        this.addLayer(new EyesLayer<OwlRenderState, OwlModel>(this) {
            @Override
            public RenderType renderType() {
                return RenderType.eyes(EYES_TEXTURE);
            }
        });
    }

    @Override
    public OwlRenderState createRenderState() {
        return new OwlRenderState();
    }

    @Override
    public void extractRenderState(OwlEntity entity, OwlRenderState state, float tickProgress) {
        super.extractRenderState(entity, state, tickProgress);
        state.flying = entity.isOwlFlying();
        state.sitting = entity.isInSittingPose();
        state.lean = entity.getLeanAmount(tickProgress);
        String name = ChatFormatting.stripFormatting(entity.getName().getString());
        state.hedwig = name != null && name.equalsIgnoreCase("Hedwig");
    }

    @Override
    public ResourceLocation getTextureLocation(OwlRenderState state) {
        return state.hedwig ? HEDWIG_TEXTURE : TEXTURE;
    }

    @Override
    protected void scale(OwlRenderState state, PoseStack pose) {
        pose.scale(0.75F, 0.75F, 0.75F);
    }

    @Override
    protected void setupRotations(OwlRenderState state, PoseStack pose, float bodyRot, float scale) {
        super.setupRotations(state, pose, bodyRot, scale);
        if (state.sitting) pose.translate(0.0F, -0.1F, 0.0F);
        pose.translate(0.0F, (state.lean / 7.0F) / 2.0F, (state.lean / 7.0F) / 2.0F);
        if (state.lean > 0.0F) {
            pose.mulPose(Axis.XP.rotationDegrees(Mth.lerp(state.lean, 0.0F, -7.0F)));
        }
    }
}
