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
import party.lemons.biomemakeover.init.BMFeatures;
import party.lemons.biomemakeover.init.BMAdvancements;
import party.lemons.biomemakeover.init.BMParticles;
import party.lemons.biomemakeover.init.BMBlockEntities;
import party.lemons.biomemakeover.init.BMEffects;
import party.lemons.biomemakeover.init.BMPotions;
import party.lemons.biomemakeover.level.BMWorldEvents;

public final class BiomeMakeover implements ModInitializer {
    public static final String MOD_ID = "biomemakeover";
    public static final Logger LOGGER = LoggerFactory.getLogger("Biome Makeover");

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        BMBlocks.initialize();
        BMBlockEntities.initialize();
        BMItems.initialize();
        BMSounds.initialize();
        BMParticles.initialize();
        BMEffects.initialize();
        BMPotions.initialize();
        BMFeatures.initialize();
        BMAdvancements.initialize();
        BMEntities.initialize();
        BMWorldgen.initialize();
        BMWorldEvents.initialize();
        LOGGER.info("Biome Makeover Stage 9B.1 ten-curse engine build loaded.");
    }
}
