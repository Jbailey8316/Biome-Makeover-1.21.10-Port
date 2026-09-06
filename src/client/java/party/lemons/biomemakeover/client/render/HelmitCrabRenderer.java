package party.lemons.biomemakeover.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.client.model.BMModelLayers;
import party.lemons.biomemakeover.client.model.HelmitCrabModel;
import party.lemons.biomemakeover.entity.HelmitCrabEntity;

public final class HelmitCrabRenderer extends MobRenderer<HelmitCrabEntity, HelmitCrabRenderState, HelmitCrabModel> {
    private static final ResourceLocation TEXTURE = BiomeMakeover.id("textures/entity/helmit_crab/helmit_crab.png");
    public HelmitCrabRenderer(EntityRendererProvider.Context context) { super(context, new HelmitCrabModel(context.bakeLayer(BMModelLayers.HELMIT_CRAB)), .25F); }
    @Override public HelmitCrabRenderState createRenderState() { return new HelmitCrabRenderState(); }
    @Override public void extractRenderState(HelmitCrabEntity entity, HelmitCrabRenderState state, float tickProgress) {
        super.extractRenderState(entity, state, tickProgress); state.hiding = entity.isHiding();
    }
    @Override public ResourceLocation getTextureLocation(HelmitCrabRenderState state) { return TEXTURE; }
}
