package party.lemons.biomemakeover.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.client.model.AdjudicatorModel;
import party.lemons.biomemakeover.client.model.BMModelLayers;
import party.lemons.biomemakeover.entity.AdjudicatorEntity;

public final class AdjudicatorRenderer extends MobRenderer<AdjudicatorEntity, AdjudicatorRenderState, AdjudicatorModel> {
    private static final ResourceLocation TEXTURE = BiomeMakeover.id("textures/entity/adjudicator.png");
    private static final ResourceLocation EYES_TEXTURE = BiomeMakeover.id("textures/entity/adjudicator_eyes.png");
    public AdjudicatorRenderer(EntityRendererProvider.Context context) {
        super(context, new AdjudicatorModel(context.bakeLayer(BMModelLayers.ADJUDICATOR)), .25F);
        this.addLayer(new EyesLayer<AdjudicatorRenderState, AdjudicatorModel>(this) {
            @Override
            public RenderType renderType() {
                return RenderType.eyes(EYES_TEXTURE);
            }
        });
    }
    @Override public AdjudicatorRenderState createRenderState() { return new AdjudicatorRenderState(); }
    @Override public void extractRenderState(AdjudicatorEntity entity, AdjudicatorRenderState state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        state.controllerState = entity.getControllerState();
        state.holdingBow = entity.getMainHandItem().is(net.minecraft.world.item.Items.BOW);
        state.holdingAxe = entity.getMainHandItem().is(net.minecraft.world.item.Items.IRON_AXE);
        state.attackAnimation = entity.getAttackAnim(tickDelta);
    }
    @Override public ResourceLocation getTextureLocation(AdjudicatorRenderState state) { return TEXTURE; }
}
