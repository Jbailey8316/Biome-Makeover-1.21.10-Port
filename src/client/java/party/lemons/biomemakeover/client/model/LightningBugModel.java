package party.lemons.biomemakeover.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import party.lemons.biomemakeover.client.render.SwampFlyingRenderState;

public final class LightningBugModel extends EntityModel<SwampFlyingRenderState> {
    public LightningBugModel(ModelPart root) { super(root); }
    public static LayerDefinition createEmptyLayer(){return LayerDefinition.create(new MeshDefinition(),16,16);}
    public static LayerDefinition createInnerLayer(){MeshDefinition mesh=new MeshDefinition();mesh.getRoot().addOrReplaceChild("main",CubeListBuilder.create().texOffs(0,8).addBox(-1,-5,-1,2,2,2),PartPose.offset(0,24,0));return LayerDefinition.create(mesh,16,16);}
    public static LayerDefinition createOuterLayer(){MeshDefinition mesh=new MeshDefinition();mesh.getRoot().addOrReplaceChild("main",CubeListBuilder.create().texOffs(0,0).addBox(-2,-6,-2,4,4,4),PartPose.offset(0,24,0));return LayerDefinition.create(mesh,16,16);}
}
