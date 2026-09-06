package party.lemons.biomemakeover.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.AbstractIllager;
import party.lemons.biomemakeover.client.render.StoneGolemRenderState;

/** Released Stone Golem mesh, adapted to the 1.21.10 render-state API. */
public final class StoneGolemModel extends EntityModel<StoneGolemRenderState> implements net.minecraft.client.model.ArmedModel, net.minecraft.client.model.HeadedModel {
    private final ModelPart body, lowerBase, armLeft, armRight, head;

    public StoneGolemModel(ModelPart root) {
        super(root);
        lowerBase = root.getChild("lower_base");
        body = root.getChild("body");
        armLeft = body.getChild("arm_left");
        armRight = body.getChild("arm_right");
        head = body.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("lower_base", CubeListBuilder.create().texOffs(0, 22)
            .addBox(-7, 0, -7, 14, 8, 14).texOffs(56, 0).addBox(-4, -8, -4, 8, 8, 8), PartPose.offset(0, 16, 0));
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0)
            .addBox(-9, -16.7F, -5, 18, 12, 10).texOffs(42, 22).addBox(-7, -4.7F, -3, 14, 5, 6), PartPose.offset(0, 9.7F, 0));
        body.addOrReplaceChild("arm_left", CubeListBuilder.create().texOffs(0, 44)
            .addBox(.5F, 1.5F, -3, 4, 26, 6).texOffs(40, 62).addBox(.5F, -2.5F, -4, 6, 8, 8), PartPose.offset(8.5F, -14.2F, 0));
        body.addOrReplaceChild("arm_right", CubeListBuilder.create().texOffs(20, 44)
            .addBox(-4.5F, 1.5F, -3, 4, 26, 6).texOffs(64, 33).addBox(-6.5F, -2.5F, -4, 6, 8, 8), PartPose.offset(-8.5F, -14.2F, 0));
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(40, 44)
            .addBox(-4, -11, -3.5F, 8, 10, 8).texOffs(60, 62).addBox(-5, -6, -5.5F, 10, 2, 2)
            .texOffs(6, 22).addBox(-1, -4, -5.5F, 2, 4, 2).texOffs(56, 16).addBox(-5, -2, -4.5F, 10, 3, 3), PartPose.offset(0, -15.7F, -3.5F));
        head.addOrReplaceChild("horn", CubeListBuilder.create().texOffs(0, 22)
            .addBox(-9.5F, -2.5F, -1, 1, 6, 2).texOffs(0, 0).addBox(-.5F, -2.5F, -1, 1, 6, 2),
            PartPose.offsetAndRotation(4.5F, -10.5F, -3.5F, .7854F, 0, 0));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override public void setupAnim(StoneGolemRenderState state) {
        super.setupAnim(state);
        head.xRot = state.xRot * Mth.DEG_TO_RAD;
        head.yRot = state.yRot * Mth.DEG_TO_RAD;
        armLeft.xRot = armRight.xRot = armLeft.yRot = armRight.yRot = armLeft.zRot = armRight.zRot = 0;
        if (state.charging) {
            armRight.xRot = armLeft.xRot = -0.97079635F;
            armRight.yRot = -0.8F;
            armLeft.yRot = 0.35F;
        } else if (state.holdingCrossbow) {
            armRight.yRot = -0.3F + head.yRot;
            armLeft.yRot = 0.6F + head.yRot;
            armRight.xRot = armLeft.xRot = -1.25F + head.xRot;
        } else if (state.attackAnimation > 0) {
            AnimationUtils.swingWeaponDown(armRight, armLeft, HumanoidArm.RIGHT, state.attackAnimation, 0);
        }
    }

    @Override public void translateToHand(net.minecraft.client.renderer.entity.state.EntityRenderState state, HumanoidArm arm, com.mojang.blaze3d.vertex.PoseStack pose) {
        (arm == HumanoidArm.LEFT ? armLeft : armRight).translateAndRotate(pose);
    }

    @Override public ModelPart getHead() { return head; }
}
