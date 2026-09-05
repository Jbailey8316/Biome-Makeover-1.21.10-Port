package party.lemons.biomemakeover.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.resources.ResourceLocation;
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
        addLayer(new ItemInHandLayer<>(this));
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
}
