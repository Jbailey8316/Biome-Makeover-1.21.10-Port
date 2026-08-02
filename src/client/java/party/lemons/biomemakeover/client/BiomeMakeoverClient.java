package party.lemons.biomemakeover.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.client.model.BMModelLayers;
import party.lemons.biomemakeover.client.render.OwlRenderer;
import party.lemons.biomemakeover.init.BMBlocks;
import party.lemons.biomemakeover.init.BMEntities;

public final class BiomeMakeoverClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.putBlocks(ChunkSectionLayer.CUTOUT, BMBlocks.WILD_MUSHROOMS, BMBlocks.BLACK_THISTLE);
        BMModelLayers.register();
        EntityRenderers.register(BMEntities.OWL, OwlRenderer::new);
        BiomeMakeover.LOGGER.info("Biome Makeover Stage 7.2 client initialized: original owl model registered.");
    }
}
