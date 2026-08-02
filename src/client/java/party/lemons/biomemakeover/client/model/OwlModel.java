package party.lemons.biomemakeover.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import party.lemons.biomemakeover.client.render.state.OwlRenderState;
import net.minecraft.util.Mth;

/** Original Biome Makeover owl geometry, adapted to the 1.21.10 render-state API. */
public final class OwlModel extends EntityModel<OwlRenderState> {
    private final ModelPart chest;
    private final ModelPart tail;
    private final ModelPart head;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart eyelids;

    public OwlModel(ModelPart root) {
        super(root);
        this.chest = root.getChild("chest");
        this.tail = this.chest.getChild("tail");
        this.head = root.getChild("head_connection");
        this.leftWing = this.chest.getChild("wing_left_connection");
        this.rightWing = this.chest.getChild("wing_right_connection");
        this.leftLeg = this.chest.getChild("leg_left");
        this.rightLeg = this.chest.getChild("leg_right");
        this.eyelids = this.head.getChild("head").getChild("eyelids");
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
        // Head-only eyelid plane. This replaces whole-texture swapping so feet never blink.
        head.addOrReplaceChild("eyelids", CubeListBuilder.create().texOffs(46, 0)
            .addBox(-3.5F, -4.5F, -3.04F, 7.0F, 2.5F, 0.03F), PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(OwlRenderState state) {
        super.setupAnim(state);
        // Baby owl scaling/pose hook. Final baby geometry polish will build on this state.
        float pi = Mth.PI;
        float lean = state.lean;
        float walk = state.sleeping ? 0.0F : state.walkAnimationPos;
        float speed = state.sleeping ? 0.0F : state.walkAnimationSpeed;

        if (state.sleeping) {
            // Sleeping owls tuck their head and do not track players.
            this.head.xRot = 0.55F;
            this.head.yRot = 0.0F;
        } else {
            this.head.xRot = -0.2618F + state.xRot * 0.017453292F - 0.698132F * lean;
            this.head.yRot = state.yRot * 0.017453292F;
        }

        this.eyelids.visible = state.blinking || state.sleeping;

        // Reset pose anchors each frame so sitting can safely override them.
        this.chest.y = 14.975F;
        this.chest.xRot = 0.0F;
        this.head.y = 6.4F;
        this.tail.xRot = 0.3054F;

        if (!state.flying) {
            if (state.sitting) {
                // Perched owl pose: lower the body toward the ground/perch
                // while keeping the wings tucked naturally at the sides.
                this.leftLeg.visible = false;
                this.rightLeg.visible = false;

                // Drop the silhouette so the owl rests on body/tail instead of floating.
                this.chest.y = 17.75F;
                this.chest.xRot = 0.14F;
                this.head.y = 8.75F;
                this.tail.xRot = 0.72F;

                // Keep wings close to the same standing profile.
                this.leftWing.yRot = 0.0F;
                this.rightWing.yRot = 0.0F;
                this.leftWing.zRot = -0.03F;
                this.rightWing.zRot = 0.03F;
                this.leftWing.xRot = 0.0F;
                this.rightWing.xRot = 0.0F;

                // Occasional subtle resting flutter.
                float restFlutter = Mth.sin(state.ageInTicks * 0.08F) * 0.02F;
                this.leftWing.xRot = restFlutter;
                this.rightWing.xRot = -restFlutter;

                this.head.xRot += 0.32F;
            } else {
                this.leftLeg.visible = true;
                this.rightLeg.visible = true;
                this.leftLeg.xRot = Mth.cos(walk * 0.6662F + pi) * 1.4F * speed;
                this.rightLeg.xRot = Mth.cos(walk * 0.6662F) * 1.4F * speed;
                this.leftWing.yRot = Mth.cos(walk * 0.6662F + pi) * speed * 0.18F;
                this.rightWing.yRot = Mth.cos(walk * 0.6662F) * speed * 0.18F;
                this.leftWing.zRot = -0.10F * lean;
                this.rightWing.zRot = 0.10F * lean;
            }
        } else {
            float horizontal = state.horizontalSpeed;
            float vertical = state.verticalSpeed;
            boolean activelyFlying = horizontal > 0.035F || Math.abs(vertical) > 0.045F;
            boolean gliding = vertical < -0.035F && horizontal > 0.04F;
            float flapTime = state.ageInTicks * (0.55F + Mth.clamp(horizontal * 4.0F, 0.0F, 1.2F));

            if (gliding) {
                this.leftWing.zRot = -0.72F;
                this.rightWing.zRot = 0.72F;
                this.leftWing.yRot = -0.18F;
                this.rightWing.yRot = 0.18F;
            } else if (activelyFlying) {
                float flap = Mth.sin(flapTime) * 0.72F;
                this.leftWing.zRot = -0.42F - flap;
                this.rightWing.zRot = 0.42F + flap;
                this.leftWing.yRot = -0.12F;
                this.rightWing.yRot = 0.12F;
            } else {
                // A brief near-stationary transition, not a permanent Superman pose.
                this.leftWing.zRot = -0.20F;
                this.rightWing.zRot = 0.20F;
                this.leftWing.yRot = 0.0F;
                this.rightWing.yRot = 0.0F;
            }
            this.leftLeg.xRot = 0.35F;
            this.rightLeg.xRot = 0.35F;
        }

        if (state.blinking) this.head.xRot += 0.045F;
    }

}
