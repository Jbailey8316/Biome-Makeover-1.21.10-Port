package party.lemons.biomemakeover;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;
import net.minecraft.resources.ResourceKey;
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
import party.lemons.biomemakeover.init.BMMenus;
import party.lemons.biomemakeover.init.BMStructureProcessors;
import party.lemons.biomemakeover.init.BMStructures;
import party.lemons.biomemakeover.worldgen.mansion.MansionFeature;
import party.lemons.biomemakeover.crafting.witch.data.QuestCategoryReloadListener;
import party.lemons.biomemakeover.crafting.witch.data.reward.RewardTables;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import party.lemons.biomemakeover.network.CompleteWitchQuestPayload;
import party.lemons.biomemakeover.network.WitchQuestsPayload;
import party.lemons.biomemakeover.crafting.witch.menu.WitchMenu;
import party.lemons.biomemakeover.config.MythasConfig;

public final class BiomeMakeover implements ModInitializer {
    public static final String MOD_ID = "biomemakeover";
    public static final Logger LOGGER = LoggerFactory.getLogger("Biome Makeover");

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        MythasConfig.load();
        BMBlocks.initialize();
        BMBlockEntities.initialize();
        BMMenus.initialize();
        BMItems.initialize();
        BMSounds.initialize();
        BMParticles.initialize();
        BMEffects.initialize();
        BMPotions.initialize();
        BMFeatures.initialize();
        BMStructureProcessors.initialize();
        BMStructures.initialize();
        BMAdvancements.initialize();
        BMEntities.initialize();
        BMWorldgen.initialize();
        BMWorldEvents.initialize();
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new QuestCategoryReloadListener());
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(RewardTables.instance());
        PayloadTypeRegistry.playC2S().register(CompleteWitchQuestPayload.TYPE, CompleteWitchQuestPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(WitchQuestsPayload.TYPE, WitchQuestsPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(CompleteWitchQuestPayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                if (context.player().containerMenu instanceof WitchMenu menu && menu.containerId == context.player().containerMenu.containerId)
                    menu.completeQuest(context.player(), payload.index());
            });
        });
        if (Boolean.getBoolean("bm.fence.trace")) {
            ServerLifecycleEvents.SERVER_STARTED.register(server -> BMBlocks.traceFenceTags());
        }
        // The server-thread callback also performs the bounded released-state
        // dungeon reconciliation when diagnostics are disabled.
        MansionFeature.enableDelayedFluidTrace();
        LOGGER.info("Biome Makeover Stage 10C.4 Ghost Town integration candidate loaded.");
    }
}
