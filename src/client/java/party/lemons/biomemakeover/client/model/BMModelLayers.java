package party.lemons.biomemakeover.client.model;

import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import party.lemons.biomemakeover.BiomeMakeover;
import net.minecraft.client.model.BoatModel;

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
    public static final ModelLayerLocation TAPESTRY = new ModelLayerLocation(BiomeMakeover.id("tapestry"), "main");
    public static final ModelLayerLocation ADJUDICATOR = new ModelLayerLocation(BiomeMakeover.id("adjudicator"), "main");
    public static final ModelLayerLocation STONE_GOLEM = new ModelLayerLocation(BiomeMakeover.id("stone_golem"), "main");
    public static final ModelLayerLocation HELMIT_CRAB = new ModelLayerLocation(BiomeMakeover.id("helmit_crab"), "main");
    public static final ModelLayerLocation ANCIENT_OAK_BOAT = new ModelLayerLocation(BiomeMakeover.id("ancient_oak_boat"), "main");
    public static final ModelLayerLocation ANCIENT_OAK_CHEST_BOAT = new ModelLayerLocation(BiomeMakeover.id("ancient_oak_chest_boat"), "main");
    public static final ModelLayerLocation WILLOW_BOAT = new ModelLayerLocation(BiomeMakeover.id("willow_boat"), "main");
    public static final ModelLayerLocation WILLOW_CHEST_BOAT = new ModelLayerLocation(BiomeMakeover.id("willow_chest_boat"), "main");
    public static final ModelLayerLocation SWAMP_CYPRESS_BOAT = new ModelLayerLocation(BiomeMakeover.id("swamp_cypress_boat"), "main");
    public static final ModelLayerLocation SWAMP_CYPRESS_CHEST_BOAT = new ModelLayerLocation(BiomeMakeover.id("swamp_cypress_chest_boat"), "main");
    public static final ModelLayerLocation BLIGHTED_BALSA_BOAT = new ModelLayerLocation(BiomeMakeover.id("blighted_balsa_boat"), "main");
    public static final ModelLayerLocation BLIGHTED_BALSA_CHEST_BOAT = new ModelLayerLocation(BiomeMakeover.id("blighted_balsa_chest_boat"), "main");

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
        EntityModelLayerRegistry.registerModelLayer(TAPESTRY, TapestryModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(ADJUDICATOR, AdjudicatorModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(STONE_GOLEM, StoneGolemModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(HELMIT_CRAB, HelmitCrabModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(ANCIENT_OAK_BOAT, BoatModel::createBoatModel);
        EntityModelLayerRegistry.registerModelLayer(ANCIENT_OAK_CHEST_BOAT, BoatModel::createChestBoatModel);
        EntityModelLayerRegistry.registerModelLayer(WILLOW_BOAT, BoatModel::createBoatModel);
        EntityModelLayerRegistry.registerModelLayer(WILLOW_CHEST_BOAT, BoatModel::createChestBoatModel);
        EntityModelLayerRegistry.registerModelLayer(SWAMP_CYPRESS_BOAT, BoatModel::createBoatModel);
        EntityModelLayerRegistry.registerModelLayer(SWAMP_CYPRESS_CHEST_BOAT, BoatModel::createChestBoatModel);
        EntityModelLayerRegistry.registerModelLayer(BLIGHTED_BALSA_BOAT, BoatModel::createBoatModel);
        EntityModelLayerRegistry.registerModelLayer(BLIGHTED_BALSA_CHEST_BOAT, BoatModel::createChestBoatModel);
    }
}
