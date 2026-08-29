package party.lemons.biomemakeover.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HoldingEntityRenderState;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.resources.ResourceLocation;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.entity.MushroomTraderEntity;

/** The final inner skin plus independent outer-robes texture, not appearance variants. */
public final class MushroomTraderRenderer
    extends MobRenderer<MushroomTraderEntity, VillagerRenderState, VillagerModel> {

    private static final ResourceLocation INNER = BiomeMakeover.id("textures/entity/mushrooming_trader_inner.png");

    public MushroomTraderRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
        addLayer(new OuterLayer(this, new VillagerModel(context.bakeLayer(ModelLayers.VILLAGER))));
        addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getPlayerSkinRenderCache()));
        addLayer(new CrossedArmsItemLayer<>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(VillagerRenderState state) {
        return INNER;
    }

    @Override
    public VillagerRenderState createRenderState() {
        return new VillagerRenderState();
    }

    @Override
    public void extractRenderState(MushroomTraderEntity entity, VillagerRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        HoldingEntityRenderState.extractHoldingEntityRenderState(entity, state, itemModelResolver);
        state.isUnhappy = entity.getUnhappyCounter() > 0;
    }

    @Override
    protected void scale(VillagerRenderState state, PoseStack poseStack) {
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
    }

    private static final class OuterLayer extends RenderLayer<VillagerRenderState, VillagerModel> {
        private static final ResourceLocation OUTER = BiomeMakeover.id("textures/entity/mushrooming_trader_outer.png");
        private final VillagerModel outerModel;

        private OuterLayer(RenderLayerParent<VillagerRenderState, VillagerModel> parent, VillagerModel outerModel) {
            super(parent);
            this.outerModel = outerModel;
        }

        @Override
        public void submit(
            PoseStack poseStack,
            SubmitNodeCollector output,
            int packedLight,
            VillagerRenderState state,
            float yRot,
            float xRot
        ) {
            if (!state.isInvisible) {
                coloredCutoutModelCopyLayerRender(outerModel, OUTER, poseStack, output, packedLight, state, -1, 1);
            }
        }
    }
}
