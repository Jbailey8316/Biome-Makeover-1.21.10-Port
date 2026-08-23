package party.lemons.biomemakeover.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import party.lemons.biomemakeover.client.render.ScuttlerRenderState;
import net.minecraft.util.Mth;

public final class ScuttlerModel extends EntityModel<ScuttlerRenderState> {
    private final ModelPart head,leftFront,rightFront,leftBack,rightBack,tail,tail3,rattler;
    public ScuttlerModel(ModelPart root){ super(root); head=root.getChild("head"); var body=root.getChild("body"); leftFront=body.getChild("left_front_leg");rightFront=body.getChild("right_front_leg");leftBack=body.getChild("left_back_leg");rightBack=body.getChild("right_back_leg");tail=body.getChild("tail");var tail2=tail.getChild("tail2");tail3=tail2.getChild("tail3");rattler=tail3.getChild("rattler"); }
    public static LayerDefinition createBodyLayer(){
        MeshDefinition mesh=new MeshDefinition(); PartDefinition root=mesh.getRoot();
        PartDefinition body=root.addOrReplaceChild("body",CubeListBuilder.create().texOffs(0,0).addBox(-3,-1.5F,0,6,3,12),PartPose.offsetAndRotation(0,22,-4,.2618F,0,0));
        body.addOrReplaceChild("left_front_leg",CubeListBuilder.create().texOffs(24,5).addBox(0,-1,-1,4,2,2),PartPose.offset(3,1,2));
        body.addOrReplaceChild("right_front_leg",CubeListBuilder.create().texOffs(24,5).addBox(-4,-1,-1,4,2,2),PartPose.offset(-3,1,2));
        body.addOrReplaceChild("left_back_leg",CubeListBuilder.create().texOffs(26,15).addBox(-1,-1,-1,2,4,2),PartPose.offset(3,1,9));
        body.addOrReplaceChild("right_back_leg",CubeListBuilder.create().texOffs(26,15).addBox(-1,-1,-1,2,4,2),PartPose.offset(-3,1,9));
        PartDefinition tail=body.addOrReplaceChild("tail",CubeListBuilder.create().texOffs(24,0).addBox(-2,-1,-.5F,4,2,3),PartPose.offsetAndRotation(0,0,12,.6981F,0,0));
        PartDefinition tail2=tail.addOrReplaceChild("tail2",CubeListBuilder.create().texOffs(0,0).addBox(-1.5F,-1,-1.5F,3,2,3),PartPose.offsetAndRotation(0,-.5448F,3.5642F,.4363F,0,0));
        PartDefinition tail3=tail2.addOrReplaceChild("tail3",CubeListBuilder.create().texOffs(13,25).addBox(-1,-1.4605F,-.3184F,2,2,3),PartPose.offsetAndRotation(0,.0783F,1.2687F,.7854F,0,0));
        tail3.addOrReplaceChild("rattler",CubeListBuilder.create().texOffs(15,17).addBox(-1.5F,-1.4463F,-.0312F,3,3,5),PartPose.offsetAndRotation(0,-.6005F,1.5858F,.1309F,0,0));
        root.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,15).addBox(-2.5F,-2,-4.5F,5,3,5),PartPose.offsetAndRotation(0,22,-4,-.2618F,0,0));
        return LayerDefinition.create(mesh,64,64);
    }
    @Override public void setupAnim(ScuttlerRenderState state){ super.setupAnim(state); head.xRot=-.2618F+state.xRot*Mth.DEG_TO_RAD; head.yRot=state.yRot*Mth.DEG_TO_RAD; float swing=Mth.cos(state.walkAnimationPos*.66F)*1.4F*state.walkAnimationSpeed; rightBack.xRot=swing;leftBack.xRot=-swing;rightFront.yRot=-(1.1345F+Mth.cos(state.walkAnimationPos*.66F)*state.walkAnimationSpeed)+(float)Math.PI;leftFront.yRot=1.1345F+Mth.cos(state.walkAnimationPos*.66F)*state.walkAnimationSpeed;tail.zRot=-swing*.5F;tail3.zRot=swing*(.5F/1.4F);rattler.zRot=swing*(.7F/1.4F);rattler.xRot=.1309F+swing*(.5F/1.4F);float rattle=Mth.sin(state.rattleTime);tail.zRot+=rattle;tail3.zRot-=rattle;rattler.zRot-=rattle; }
}
