package party.lemons.biomemakeover.client.model;

import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import party.lemons.biomemakeover.BiomeMakeover;

public final class BMModelLayers {
    public static final ModelLayerLocation OWL = new ModelLayerLocation(BiomeMakeover.id("owl"), "main");

    private BMModelLayers() {}

    public static void register() {
        EntityModelLayerRegistry.registerModelLayer(OWL, OwlModel::createBodyLayer);
    }
}
