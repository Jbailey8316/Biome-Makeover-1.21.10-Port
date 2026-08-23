package party.lemons.biomemakeover.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public final class CowboyHatModel<S extends LivingEntityRenderState> extends EntityModel<S> {
    public CowboyHatModel(ModelPart root) { super(root); }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh=new MeshDefinition();
        PartDefinition root=mesh.getRoot();
        PartDefinition head=root.addOrReplaceChild("head",CubeListBuilder.create().texOffs(32,32).mirror()
            .addBox(-4,-8,-4,8,8,8,new CubeDeformation(0)).mirror(false),PartPose.offset(0,2,0));
        head.addOrReplaceChild("left",CubeListBuilder.create().texOffs(32,46)
            .addBox(0,-2,-8,0,2,16,new CubeDeformation(0)),PartPose.offsetAndRotation(6,-6,0,0,0,.2618F));
        head.addOrReplaceChild("main",CubeListBuilder.create().texOffs(32,0)
            .addBox(-4,-4,-4,8,4,8,new CubeDeformation(0)),PartPose.offset(0,-6,0));
        head.addOrReplaceChild("right",CubeListBuilder.create().texOffs(0,46)
            .addBox(0,-2,-8,0,2,16,new CubeDeformation(0)),PartPose.offsetAndRotation(-6,-6,0,0,0,-.2618F));
        head.addOrReplaceChild("bone",CubeListBuilder.create().texOffs(0,12)
            .addBox(-8,-30,-8,16,0,16,new CubeDeformation(0)),PartPose.offset(0,24,0));
        return LayerDefinition.create(mesh,64,64);
    }
}
