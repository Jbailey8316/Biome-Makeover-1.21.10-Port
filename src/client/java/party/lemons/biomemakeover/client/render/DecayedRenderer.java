package party.lemons.biomemakeover.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;
import party.lemons.biomemakeover.BiomeMakeover;

public final class DecayedRenderer extends ZombieRenderer {
    private static final ResourceLocation TEXTURE=BiomeMakeover.id("textures/entity/decayed_inner_layer.png");
    public DecayedRenderer(EntityRendererProvider.Context context){super(context);addLayer(new OuterLayer(this,context.getModelSet()));}
    @Override public ResourceLocation getTextureLocation(ZombieRenderState state){return TEXTURE;}

    private static final class OuterLayer extends RenderLayer<ZombieRenderState, ZombieModel<ZombieRenderState>> {
        private static final ResourceLocation OUTER=BiomeMakeover.id("textures/entity/decayed_outer_layer.png");
        private final DrownedModel model,babyModel;
        private OuterLayer(RenderLayerParent<ZombieRenderState,ZombieModel<ZombieRenderState>> parent, EntityModelSet models){
            super(parent);model=new DrownedModel(models.bakeLayer(ModelLayers.DROWNED_OUTER_LAYER));
            babyModel=new DrownedModel(models.bakeLayer(ModelLayers.DROWNED_BABY_OUTER_LAYER));
        }
        @Override public void submit(PoseStack pose, SubmitNodeCollector output, int light, ZombieRenderState state, float yRot, float xRot){
            coloredCutoutModelCopyLayerRender(state.isBaby?babyModel:model,OUTER,pose,output,light,state,-1,1);
        }
    }
}
