package party.lemons.biomemakeover;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import party.lemons.biomemakeover.init.BMBlocks;
import party.lemons.biomemakeover.init.BMEntities;
import party.lemons.biomemakeover.init.BMItems;
import party.lemons.biomemakeover.init.BMWorldgen;
import party.lemons.biomemakeover.init.BMSounds;

public final class BiomeMakeover implements ModInitializer {
    public static final String MOD_ID = "biomemakeover";
    public static final Logger LOGGER = LoggerFactory.getLogger("Biome Makeover");

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        BMBlocks.initialize();
        BMItems.initialize();
        BMSounds.initialize();
        BMEntities.initialize();
        BMWorldgen.initialize();
        LOGGER.info("Biome Makeover owl nest full-test build loaded.");
    }
}
