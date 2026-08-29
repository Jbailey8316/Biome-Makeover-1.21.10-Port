package party.lemons.biomemakeover.client.render;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.entity.GhostEntity;
/** Client-only translucent foundation renderer; Ghost-specific model polish is deferred. */
public final class GhostRenderer extends MobRenderer<GhostEntity, ZombieRenderState, ZombieModel<ZombieRenderState>> {
    private static final ResourceLocation TEXTURE = BiomeMakeover.id("textures/entity/ghost.png");
    public GhostRenderer(EntityRendererProvider.Context context) { super(context, new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), .25f); }
    @Override public ZombieRenderState createRenderState() { return new ZombieRenderState(); }
    @Override public void extractRenderState(GhostEntity entity, ZombieRenderState state, float tickDelta) { super.extractRenderState(entity, state, tickDelta); }
    @Override public ResourceLocation getTextureLocation(ZombieRenderState state){ return TEXTURE; }
}
