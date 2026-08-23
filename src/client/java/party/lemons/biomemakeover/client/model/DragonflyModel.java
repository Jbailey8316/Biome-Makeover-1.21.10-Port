package party.lemons.biomemakeover.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import party.lemons.biomemakeover.client.render.SwampFlyingRenderState;

public final class DragonflyModel extends EntityModel<SwampFlyingRenderState> {
    private final ModelPart rightTop, leftTop, rightBottom, leftBottom;
    public DragonflyModel(ModelPart root) { super(root); ModelPart body=root.getChild("Body"); rightTop=body.getChild("right_top"); leftTop=body.getChild("left_top"); rightBottom=body.getChild("right_bottom"); leftBottom=body.getChild("left_bottom"); }
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh=new MeshDefinition(); PartDefinition root=mesh.getRoot();
        PartDefinition body=root.addOrReplaceChild("Body",CubeListBuilder.create().texOffs(10,10).addBox(-2,-5,-4,3,2,2).texOffs(0,8).addBox(-1.5F,-5.5F,-2,2,2,3).texOffs(0,2).addBox(-1,-5,1,1,1,5),PartPose.offset(0,24,0));
        body.addOrReplaceChild("left_top",CubeListBuilder.create().texOffs(0,0).addBox(0,0,-1,6,0,2),PartPose.offsetAndRotation(.5F,-5.4F,-1,0,0,-.7854F));
        body.addOrReplaceChild("left_bottom",CubeListBuilder.create().texOffs(5,5).addBox(0,0,-1,5,0,2),PartPose.offsetAndRotation(.5F,-5.4F,-1,0,0,.2618F));
        body.addOrReplaceChild("right_top",CubeListBuilder.create().texOffs(0,0).mirror().addBox(-6,0,-1,6,0,2),PartPose.offsetAndRotation(-1.5F,-5.5F,-1,0,0,.7854F));
        body.addOrReplaceChild("right_bottom",CubeListBuilder.create().texOffs(5,5).mirror().addBox(-5,0,-1,5,0,2),PartPose.offsetAndRotation(-1.5F,-5,-1,0,0,-.2618F));
        return LayerDefinition.create(mesh,32,32);
    }
    @Override public void setupAnim(SwampFlyingRenderState state) { super.setupAnim(state); float flap=Mth.sin(state.ageInTicks*2.8F)*.436332F; rightTop.zRot=flap; leftTop.zRot=-flap; rightBottom.zRot=-flap; leftBottom.zRot=flap; }
}
