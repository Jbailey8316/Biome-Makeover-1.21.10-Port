package party.lemons.biomemakeover.client.model;

import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import party.lemons.biomemakeover.BiomeMakeover;

public final class BMModelLayers {
    public static final ModelLayerLocation OWL = new ModelLayerLocation(BiomeMakeover.id("owl"), "main");
    public static final ModelLayerLocation SCUTTLER = new ModelLayerLocation(BiomeMakeover.id("scuttler"), "main");
    public static final ModelLayerLocation COWBOY_HAT = new ModelLayerLocation(BiomeMakeover.id("cowboy_hat"), "main");

    private BMModelLayers() {}

    public static void register() {
        EntityModelLayerRegistry.registerModelLayer(OWL, OwlModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(SCUTTLER, ScuttlerModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(COWBOY_HAT, CowboyHatModel::createBodyLayer);
    }
}
