package party.lemons.biomemakeover.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** The released 1.20.1 Mansion tapestry flag geometry. */
public final class TapestryModel extends EntityModel<TapestryModel.State> {
    private final ModelPart flag;
    private final ModelPart pole;
    private final ModelPart bar;

    public TapestryModel(ModelPart root) {
        super(root);
        flag = root.getChild("flag");
        pole = root.getChild("pole");
        bar = root.getChild("bar");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("flag", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-10.0F, 0.0F, -2.0F, 20.0F, 35.0F, 1.0F)
            .texOffs(0, 36).addBox(-10.0F, 35.0F, -2.0F, 4.0F, 5.0F, 1.0F)
            .texOffs(10, 36).addBox(-2.0F, 35.0F, -2.0F, 4.0F, 5.0F, 1.0F)
            .texOffs(20, 36).addBox(6.0F, 35.0F, -2.0F, 4.0F, 5.0F, 1.0F), PartPose.ZERO);
        root.addOrReplaceChild("pole", CubeListBuilder.create()
            .texOffs(44, 0).addBox(-1.0F, -30.0F, -1.0F, 2.0F, 42.0F, 2.0F), PartPose.ZERO);
        root.addOrReplaceChild("bar", CubeListBuilder.create()
            .texOffs(0, 42).addBox(-10.0F, -32.0F, -1.0F, 20.0F, 2.0F, 2.0F), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(State state) {
        pole.visible = state.poleVisible;
        flag.xRot = state.flagXRot;
        flag.y = state.flagY;
    }

    public static final class State extends EntityRenderState {
        public boolean poleVisible;
        public float flagXRot;
        public float flagY;
    }
}
