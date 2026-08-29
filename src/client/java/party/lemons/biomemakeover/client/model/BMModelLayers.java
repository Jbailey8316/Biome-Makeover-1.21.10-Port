package party.lemons.biomemakeover.client.model;

import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import party.lemons.biomemakeover.BiomeMakeover;

public final class BMModelLayers {
    public static final ModelLayerLocation OWL = new ModelLayerLocation(BiomeMakeover.id("owl"), "main");
    public static final ModelLayerLocation OWL_BABY = new ModelLayerLocation(BiomeMakeover.id("owl"), "baby");
    public static final ModelLayerLocation SCUTTLER = new ModelLayerLocation(BiomeMakeover.id("scuttler"), "main");
    public static final ModelLayerLocation COWBOY_HAT = new ModelLayerLocation(BiomeMakeover.id("cowboy_hat"), "main");
    public static final ModelLayerLocation WITCH_HAT = new ModelLayerLocation(BiomeMakeover.id("witch_hat"), "main");
    public static final ModelLayerLocation DRAGONFLY = new ModelLayerLocation(BiomeMakeover.id("dragonfly"), "main");
    public static final ModelLayerLocation LIGHTNING_BUG = new ModelLayerLocation(BiomeMakeover.id("lightning_bug"), "main");
    public static final ModelLayerLocation LIGHTNING_BUG_INNER = new ModelLayerLocation(BiomeMakeover.id("lightning_bug_inner"), "main");
    public static final ModelLayerLocation LIGHTNING_BUG_OUTER = new ModelLayerLocation(BiomeMakeover.id("lightning_bug_outer"), "main");
    public static final ModelLayerLocation ROOTLING = new ModelLayerLocation(BiomeMakeover.id("rootling"), "main");
    public static final ModelLayerLocation MOTH = new ModelLayerLocation(BiomeMakeover.id("moth"), "main");
    public static final ModelLayerLocation GHOST = new ModelLayerLocation(BiomeMakeover.id("ghost"), "main");

    private BMModelLayers() {}

    public static void register() {
        EntityModelLayerRegistry.registerModelLayer(OWL, OwlModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(OWL_BABY, OwlModel::createBabyLayer);
        EntityModelLayerRegistry.registerModelLayer(SCUTTLER, ScuttlerModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(COWBOY_HAT, CowboyHatModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(WITCH_HAT, WitchHatModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(DRAGONFLY, DragonflyModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(LIGHTNING_BUG, LightningBugModel::createEmptyLayer);
        EntityModelLayerRegistry.registerModelLayer(LIGHTNING_BUG_INNER, LightningBugModel::createInnerLayer);
        EntityModelLayerRegistry.registerModelLayer(LIGHTNING_BUG_OUTER, LightningBugModel::createOuterLayer);
        EntityModelLayerRegistry.registerModelLayer(ROOTLING, RootlingModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(MOTH, MothModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(GHOST, GhostModel::createBodyLayer);
    }
}
