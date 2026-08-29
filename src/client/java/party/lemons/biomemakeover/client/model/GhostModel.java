package party.lemons.biomemakeover.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import party.lemons.biomemakeover.client.render.GhostRenderState;

/** Faithful modern render-state port of the released Ghost model geometry. */
public final class GhostModel extends EntityModel<GhostRenderState> {
    private final ModelPart body, head, rightArm, leftArm, lower, lower2, lower3;
    public GhostModel(ModelPart root) {
        super(root);
        body = root.getChild("body"); head = root.getChild("head");
        rightArm = body.getChild("rightarm"); leftArm = body.getChild("leftarm");
        lower = body.getChild("bodylower"); lower2 = lower.getChild("bodylower2"); lower3 = lower2.getChild("bodylower3");
    }
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition(); PartDefinition root = mesh.getRoot();
        PartDefinition b = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0,19).mirror().addBox(-4,0,-3,8,15,6,new CubeDeformation(.5F)).mirror(false), PartPose.offsetAndRotation(0,0,0,.1745F,0,0));
        b.addOrReplaceChild("rightarm", CubeListBuilder.create().texOffs(28,28).mirror().addBox(-1,-2,-2,4,12,4).mirror(false), PartPose.offsetAndRotation(5,2,0,-.6545F,0,0));
        b.addOrReplaceChild("leftarm", CubeListBuilder.create().texOffs(0,40).addBox(-3,-2,-2,4,12,4), PartPose.offsetAndRotation(-5,2,0,-.829F,0,0));
        PartDefinition lo = b.addOrReplaceChild("bodylower", CubeListBuilder.create().texOffs(25,11).addBox(-3.5F,-1.1194F,-2.3649F,7,5,7), PartPose.offsetAndRotation(0,15.1194F,-.6351F,.3927F,0,0));
        PartDefinition lo2 = lo.addOrReplaceChild("bodylower2", CubeListBuilder.create().texOffs(32,0).addBox(-2.5F,2.6806F,-2.8649F,5,5,5), PartPose.offsetAndRotation(0,0,0,.3927F,0,0));
        lo2.addOrReplaceChild("bodylower3", CubeListBuilder.create().texOffs(16,40).addBox(-.5F,2.3745F,-2.8739F,3,5,3), PartPose.offsetAndRotation(-1,4.0061F,-.491F,.3927F,0,0));
        PartDefinition h = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0,0).mirror().addBox(-4,-10,-6,8,10,8).mirror(false), PartPose.offsetAndRotation(0,2,-2,.0873F,0,0));
        h.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(0,0).mirror().addBox(-1,-1,-6,2,4,2).mirror(false), PartPose.offset(0,-2,-2));
        return LayerDefinition.create(mesh,64,64);
    }
    @Override public void setupAnim(GhostRenderState s) {
        super.setupAnim(s);
        head.xRot = -.2618F + s.xRot * Mth.DEG_TO_RAD; head.yRot = s.yRot * Mth.DEG_TO_RAD;
        rightArm.xRot = -.85F + Mth.cos(s.walkAnimationPos*.6662F + Mth.PI) * .25F * s.walkAnimationSpeed;
        leftArm.xRot = -.85F + Mth.cos(s.walkAnimationPos*.6662F) * .25F * s.walkAnimationSpeed;
        lower3.xRot = .3927F + Mth.cos(s.walkAnimationPos*.6662F) * .7F * s.walkAnimationSpeed;
        lower2.zRot = .3927F + Mth.cos(s.walkAnimationPos*.6662F) * .125F * s.walkAnimationSpeed;
        lower.xRot = .3927F + Mth.cos(s.walkAnimationPos*.6662F + Mth.PI) * .05F * s.walkAnimationSpeed;
    }
}
