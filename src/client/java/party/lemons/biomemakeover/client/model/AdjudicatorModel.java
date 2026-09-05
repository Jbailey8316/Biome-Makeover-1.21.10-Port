package party.lemons.biomemakeover.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import party.lemons.biomemakeover.client.render.AdjudicatorRenderState;

/** Released Adjudicator base mesh; combat poses remain deferred. */
public final class AdjudicatorModel extends EntityModel<AdjudicatorRenderState> {
    public AdjudicatorModel(ModelPart root) { super(root); }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
            .texOffs(0, 20).addBox(-4, -11.25F, -3, 8, 12, 6), PartPose.offset(0, 11.25F, 0));
        body.addOrReplaceChild("robe", CubeListBuilder.create().texOffs(28, 28)
            .addBox(-4, -.5F, -3, 8, 9, 6), PartPose.offset(0, 1.25F, 0));
        body.addOrReplaceChild("arm_left", CubeListBuilder.create().texOffs(14, 38)
            .addBox(-1.2F, -2, -1.5F, 3, 12, 3).texOffs(32, 16)
            .addBox(-1.5F, -3, -2.5F, 4, 4, 5), PartPose.offset(5.5F, -9.25F, .5F));
        body.addOrReplaceChild("arm_right", CubeListBuilder.create().texOffs(26, 43)
            .addBox(-1.8F, -2, -1.5F, 3, 12, 3).texOffs(32, 16)
            .addBox(-2.5F, -3, -2.5F, 4, 4, 5), PartPose.offset(-5.5F, -9.25F, .5F));
        body.addOrReplaceChild("leg_left", CubeListBuilder.create().texOffs(32, 0)
            .addBox(-1.5F, -1, -2, 3, 12, 4), PartPose.offset(2, 1.75F, 0));
        body.addOrReplaceChild("leg_right", CubeListBuilder.create().texOffs(0, 38)
            .addBox(-1.5F, -1, -2, 3, 12, 4), PartPose.offset(-2, 1.75F, 0));
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
            .texOffs(38, 44).addBox(-4, 0, -4, 8, 4, 1).texOffs(0, 0)
            .addBox(-4, -12, -4, 8, 12, 8), PartPose.ZERO);
        head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(0, 0)
            .addBox(-1, -.5F, -2, 2, 5, 2), PartPose.offset(0, -2.5F, -4));
        return LayerDefinition.create(mesh, 64, 64);
    }
}
