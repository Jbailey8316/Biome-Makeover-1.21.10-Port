package party.lemons.biomemakeover.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.client.model.BMModelLayers;
import party.lemons.biomemakeover.client.render.OwlRenderer;
import party.lemons.biomemakeover.client.render.GlowfishRenderer;
import party.lemons.biomemakeover.client.render.TumbleweedRenderer;
import party.lemons.biomemakeover.client.render.ScuttlerRenderer;
import net.minecraft.client.renderer.entity.PillagerRenderer;
import party.lemons.biomemakeover.client.render.CowboyRenderer;
import party.lemons.biomemakeover.init.BMBlocks;
import party.lemons.biomemakeover.init.BMEntities;

public final class BiomeMakeoverClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.putBlocks(ChunkSectionLayer.CUTOUT,
            BMBlocks.WILD_MUSHROOMS, BMBlocks.ITCHING_IVY, BMBlocks.BLACK_THISTLE, BMBlocks.FOXGLOVE,
            BMBlocks.ANCIENT_OAK_LEAVES, BMBlocks.ANCIENT_OAK_SAPLING,
            BMBlocks.ANCIENT_OAK_DOOR, BMBlocks.ANCIENT_OAK_TRAPDOOR);
        BlockRenderLayerMap.putBlocks(ChunkSectionLayer.CUTOUT,
            BMBlocks.PURPLE_GLOWSHROOM, BMBlocks.GREEN_GLOWSHROOM, BMBlocks.ORANGE_GLOWSHROOM,
            BMBlocks.MYCELIUM_SPROUTS, BMBlocks.MYCELIUM_ROOTS, BMBlocks.TALL_BROWN_MUSHROOM,
            BMBlocks.TALL_RED_MUSHROOM, BMBlocks.BLIGHTED_BALSA_LEAVES,
            BMBlocks.SAGUARO_CACTUS, BMBlocks.BARREL_CACTUS, BMBlocks.BARREL_CACTUS_FLOWERED,
            BMBlocks.BLIGHTED_BALSA.get("blighted_balsa_door"), BMBlocks.BLIGHTED_BALSA.get("blighted_balsa_trapdoor"));
        BMModelLayers.register();
        EntityRenderers.register(BMEntities.OWL, OwlRenderer::new);
        EntityRenderers.register(BMEntities.GLOWFISH, GlowfishRenderer::new);
        EntityRenderers.register(BMEntities.TUMBLEWEED, TumbleweedRenderer::new);
        EntityRenderers.register(BMEntities.COWBOY, CowboyRenderer::new);
        EntityRenderers.register(BMEntities.SCUTTLER, ScuttlerRenderer::new);
        BiomeMakeover.LOGGER.info("Biome Makeover client initialized.");
    }
}
