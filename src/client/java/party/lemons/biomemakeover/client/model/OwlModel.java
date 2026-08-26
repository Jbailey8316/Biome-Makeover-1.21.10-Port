package party.lemons.biomemakeover.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.BabyModelTransform;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import party.lemons.biomemakeover.client.render.state.OwlRenderState;
import net.minecraft.util.Mth;
import java.util.Set;

/** Original Biome Makeover owl geometry, adapted to the 1.21.10 render-state API. */
public final class OwlModel extends EntityModel<OwlRenderState> {
    private final ModelPart chest;
    private final ModelPart head;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart leftWingTip;
    private final ModelPart rightWingTip;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart leftFoot;
    private final ModelPart rightFoot;

    public OwlModel(ModelPart root) {
        super(root);
        this.chest = root.getChild("chest");
        this.head = root.getChild("head_connection");
        this.leftWing = this.chest.getChild("wing_left_connection");
        this.rightWing = this.chest.getChild("wing_right_connection");
        this.leftWingTip = this.leftWing.getChild("wing_left").getChild("wing_tip_left");
        this.rightWingTip = this.rightWing.getChild("wing_right").getChild("wing_tip_right");
        this.leftLeg = this.chest.getChild("leg_left");
        this.rightLeg = this.chest.getChild("leg_right");
        this.leftFoot = this.leftLeg.getChild("foot_left");
        this.rightFoot = this.rightLeg.getChild("foot_right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition chest = root.addOrReplaceChild("chest",
            CubeListBuilder.create().texOffs(0, 0)
                .addBox(-3.0F, -5.575F, -3.3125F, 6.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 14.975F, 0.3125F));
        chest.addOrReplaceChild("chest_lower_r1",
            CubeListBuilder.create().texOffs(21, 21)
                .addBox(-3.0F, -1.0F, -3.0F, 6.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 1.425F, 0.6875F, 0.2182F, 0.0F, 0.0F));

        PartDefinition leftConnection = chest.addOrReplaceChild("wing_left_connection", CubeListBuilder.create(), PartPose.offset(3.0F, -5.575F, 0.6875F));
        PartDefinition leftWing = leftConnection.addOrReplaceChild("wing_left", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -3.0F));
        leftWing.addOrReplaceChild("wing_upper_r1", CubeListBuilder.create().texOffs(0, 28)
            .addBox(0.0F, -1.0F, -2.0F, 2.0F, 11.0F, 7.0F), PartPose.offsetAndRotation(0.0F, 1.0F, 2.0F, 0.2618F, 0.0F, 0.0F));
        PartDefinition leftTip = leftWing.addOrReplaceChild("wing_tip_left", CubeListBuilder.create(), PartPose.offset(0.5F, 9.0F, 7.0F));
        leftTip.addOrReplaceChild("wing_tip_r1", CubeListBuilder.create().texOffs(18, 34)
            .addBox(0.0F, -1.0F, -2.0F, 1.0F, 8.0F, 4.0F), PartPose.offsetAndRotation(-0.5F, -4.0F, -2.0F, 0.5672F, 0.0F, 0.0F));

        PartDefinition rightConnection = chest.addOrReplaceChild("wing_right_connection", CubeListBuilder.create(), PartPose.offset(-3.0F, -5.575F, 0.6875F));
        PartDefinition rightWing = rightConnection.addOrReplaceChild("wing_right", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -3.0F));
        rightWing.addOrReplaceChild("wing_upper_r2", CubeListBuilder.create().texOffs(28, 0)
            .addBox(-2.0F, -1.0F, -2.0F, 2.0F, 11.0F, 7.0F), PartPose.offsetAndRotation(0.0F, 1.0F, 2.0F, 0.2618F, 0.0F, 0.0F));
        PartDefinition rightTip = rightWing.addOrReplaceChild("wing_tip_right", CubeListBuilder.create(), PartPose.offset(-0.5F, 9.0F, 7.0F));
        rightTip.addOrReplaceChild("wing_tip_r2", CubeListBuilder.create().texOffs(28, 34)
            .addBox(-1.0F, -1.0F, -2.0F, 1.0F, 8.0F, 4.0F), PartPose.offsetAndRotation(0.5F, -4.0F, -2.0F, 0.5672F, 0.0F, 0.0F));

        PartDefinition rightLeg = chest.addOrReplaceChild("leg_right", CubeListBuilder.create(), PartPose.offset(-1.5F, 6.925F, 1.6875F));
        rightLeg.addOrReplaceChild("thigh_right_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(0, 0, 0, 0.4363F, 0, 0));
        rightLeg.addOrReplaceChild("foot_right", CubeListBuilder.create().texOffs(16, 4).addBox(-1.5F, -0.5F, -3.0F, 3.0F, 0.0F, 4.0F), PartPose.offset(0.0F, 2.5F, 0.0F));
        PartDefinition leftLeg = chest.addOrReplaceChild("leg_left", CubeListBuilder.create(), PartPose.offset(1.5F, 6.925F, 1.6875F));
        leftLeg.addOrReplaceChild("thigh_left_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(0, 0, 0, 0.4363F, 0, 0));
        leftLeg.addOrReplaceChild("foot_left", CubeListBuilder.create().texOffs(16, 0).addBox(-1.5F, -0.5F, -1.0F, 3.0F, 0.0F, 4.0F), PartPose.offset(0.0F, 2.5F, -2.0F));

        PartDefinition tail = chest.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 5.925F, 5.1875F, 0.3054F, 0, 0));
        tail.addOrReplaceChild("tail_r1", CubeListBuilder.create().texOffs(38, 38).addBox(-3.0F, -0.5F, 0.0F, 6.0F, 5.0F, 0.0F), PartPose.offsetAndRotation(0, 0, 0, 0.5236F, 0, 0));

        PartDefinition headConnection = root.addOrReplaceChild("head_connection", CubeListBuilder.create(), PartPose.offset(0.0F, 6.4F, 1.0F));
        PartDefinition head = headConnection.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 16)
            .addBox(-4.0F, -6.0F, -3.0F, 8.0F, 6.0F, 6.0F)
            .texOffs(0, 5).addBox(-0.6F, -2.0F, -5.0F, 1.0F, 1.0F, 2.0F), PartPose.offset(0.0F, 3.0F, 0.0F));
        head.addOrReplaceChild("beak_lower", CubeListBuilder.create().texOffs(4, 5).addBox(-2.1F, -14.5F, -5.0F, 1.0F, 1.0F, 1.0F), PartPose.offset(1.5F, 13.5F, 1.0F));
        head.addOrReplaceChild("brow_left", CubeListBuilder.create().texOffs(11, 28).addBox(0.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F), PartPose.offset(1.0F, -4.5F, -3.5F));
        head.addOrReplaceChild("brow_right", CubeListBuilder.create().texOffs(11, 30).addBox(-4.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F), PartPose.offset(-1.0F, -4.5F, -3.5F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    /** Modern equivalent of the released AgeableListModel(true, 14, 0, 2, 2, 24) contract. */
    public static LayerDefinition createBabyLayer() {
        return createBodyLayer().apply(new BabyModelTransform(
            true, 14.0F, 0.0F, 2.0F, 2.0F, 24.0F, Set.of("head_connection")));
    }

    @Override
    public void setupAnim(OwlRenderState state) {
        super.setupAnim(state);
        float pi = Mth.PI;
        float lean = state.lean / 7.0F;
        float walk = state.walkAnimationPos;
        float speed = state.walkAnimationSpeed;
        this.head.xRot = -0.2618F + state.xRot * 0.0175F - 0.698132F * lean;
        this.head.yRot = state.yRot * 0.0175F;
        this.leftWingTip.zRot = 0.0F;
        this.rightWingTip.zRot = 0.0F;
        this.leftWing.xRot = 0.0F;
        this.rightWing.xRot = 0.0F;
        this.leftWing.zRot = 0.0F;
        this.rightWing.zRot = 0.0F;

        if (!state.flying) {
            this.rightLeg.xRot = Mth.cos(walk * 0.6662F) * 1.4F * speed;
            this.leftLeg.xRot = Mth.cos(walk * 0.6662F + pi) * 1.4F * speed;
            this.rightWing.yRot = Mth.cos(walk * 0.6662F) * 0.5F * speed;
            this.leftWing.yRot = Mth.cos(walk * 0.6662F + pi) * speed;
            if (state.sitting) {
                this.rightLeg.xRot = -1.5708F;
                this.leftLeg.xRot = -1.5708F;
            }
        } else {
            this.leftWing.yRot = Mth.cos(walk / 2.0F) / 2.0F;
            this.leftWingTip.zRot = -(Mth.sin(walk / 2.0F) / 4.0F);
            this.rightWing.yRot = -(Mth.cos(walk / 2.0F) / 2.0F);
            this.rightWingTip.zRot = Mth.sin(walk / 2.0F) / 4.0F;
            this.rightLeg.xRot = Mth.cos(walk * 0.6662F) * speed;
            this.leftLeg.xRot = Mth.cos(walk * 0.6662F + pi) * speed;
        }
        this.rightWing.getChild("wing_right").yRot = 1.22173F * lean;
        this.rightWing.getChild("wing_right").zRot = 1.5708F * lean;
        this.leftWing.getChild("wing_left").yRot = -1.22173F * lean;
        this.leftWing.getChild("wing_left").zRot = -1.5708F * lean;
        this.leftWingTip.xRot = -2.70526F * lean;
        this.rightWingTip.xRot = -2.70526F * lean;
        this.leftFoot.xRot = 1.5708F * lean;
        this.rightFoot.xRot = 1.5708F * lean;
    }

}
