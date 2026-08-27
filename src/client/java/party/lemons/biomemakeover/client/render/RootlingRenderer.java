package party.lemons.biomemakeover.client.render;
import com.mojang.blaze3d.vertex.PoseStack;import net.minecraft.client.renderer.SubmitNodeCollector;import net.minecraft.client.renderer.entity.*;import net.minecraft.client.renderer.entity.layers.RenderLayer;import net.minecraft.resources.ResourceLocation;import party.lemons.biomemakeover.BiomeMakeover;import party.lemons.biomemakeover.client.model.*;import party.lemons.biomemakeover.entity.RootlingEntity;
public final class RootlingRenderer extends MobRenderer<RootlingEntity,RootlingRenderState,RootlingModel>{
 private static final ResourceLocation BASE=BiomeMakeover.id("textures/entity/rootling/rootling.png");
 public RootlingRenderer(EntityRendererProvider.Context c){super(c,new RootlingModel(c.bakeLayer(BMModelLayers.ROOTLING)),.25F);addLayer(new FlowerLayer(this,c));}
 @Override public RootlingRenderState createRenderState(){return new RootlingRenderState();}
 @Override public void extractRenderState(RootlingEntity e,RootlingRenderState s,float p){super.extractRenderState(e,s,p);s.flowered=e.hasFlower();s.flower=e.flowerType();}
 @Override public ResourceLocation getTextureLocation(RootlingRenderState s){return BASE;}
 private static final class FlowerLayer extends RenderLayer<RootlingRenderState,RootlingModel>{private static final String[] N={"blue","brown","cyan","grey","light_blue","purple"};private final RootlingModel model;FlowerLayer(RenderLayerParent<RootlingRenderState,RootlingModel> p,EntityRendererProvider.Context c){super(p);model=new RootlingModel(c.bakeLayer(BMModelLayers.ROOTLING));}@Override public void submit(PoseStack pose,SubmitNodeCollector out,int light,RootlingRenderState s,float y,float x){if(s.flowered)coloredCutoutModelCopyLayerRender(model,BiomeMakeover.id("textures/entity/rootling/rootling_flower_"+N[Math.floorMod(s.flower,N.length)]+".png"),pose,out,light,s,-1,1);}}
}
