package party.lemons.biomemakeover.client.render;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.RenderType;
import party.lemons.biomemakeover.client.model.GhostModel;
import party.lemons.biomemakeover.client.model.BMModelLayers;
import net.minecraft.resources.ResourceLocation;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.entity.GhostEntity;
/** Client-only translucent foundation renderer; Ghost-specific model polish is deferred. */
public final class GhostRenderer extends MobRenderer<GhostEntity, GhostRenderState, GhostModel> {
    private static final ResourceLocation TEXTURE = BiomeMakeover.id("textures/entity/ghost.png");
    public GhostRenderer(EntityRendererProvider.Context context) { super(context, new GhostModel(context.bakeLayer(BMModelLayers.GHOST)), 0f); }
    @Override public GhostRenderState createRenderState() { return new GhostRenderState(); }
    @Override public void extractRenderState(GhostEntity entity, GhostRenderState state, float tickDelta) { super.extractRenderState(entity, state, tickDelta); }
    @Override public ResourceLocation getTextureLocation(GhostRenderState state){ return TEXTURE; }
    @Override protected RenderType getRenderType(GhostRenderState state, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderType.entityTranslucent(getTextureLocation(state));
    }
}
