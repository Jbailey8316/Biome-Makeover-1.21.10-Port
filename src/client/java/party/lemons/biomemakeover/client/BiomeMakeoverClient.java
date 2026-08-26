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
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.entity.EntityType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.HorseRenderState;
import net.minecraft.client.model.HorseModel;
import party.lemons.biomemakeover.client.render.CowboyHatLayer;
import party.lemons.biomemakeover.client.render.CowboyRenderer;
import party.lemons.biomemakeover.client.render.CowboyHatArmorRenderer;
import party.lemons.biomemakeover.client.render.DragonflyRenderer;
import party.lemons.biomemakeover.client.render.LightningBugRenderer;
import party.lemons.biomemakeover.client.render.DecayedRenderer;
import party.lemons.biomemakeover.init.BMBlocks;
import party.lemons.biomemakeover.init.BMEntities;
import party.lemons.biomemakeover.init.BMItems;
import party.lemons.biomemakeover.init.BMParticles;
import party.lemons.biomemakeover.client.particle.LightningSparkParticle;
import party.lemons.biomemakeover.client.particle.BlossomParticle;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import party.lemons.biomemakeover.init.BMBlockEntities;
import party.lemons.biomemakeover.client.render.LightningBugBottleRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public final class BiomeMakeoverClient implements ClientModInitializer {
    private static final TagKey<Biome> SWAMPS = TagKey.create(Registries.BIOME, BiomeMakeover.id("swamps"));
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.putBlocks(ChunkSectionLayer.CUTOUT,
            BMBlocks.WILD_MUSHROOMS, BMBlocks.IVY, BMBlocks.ITCHING_IVY, BMBlocks.MOTH_BLOSSOM, BMBlocks.BLACK_THISTLE, BMBlocks.FOXGLOVE,
            BMBlocks.ANCIENT_OAK_LEAVES, BMBlocks.ANCIENT_OAK_SAPLING,
            BMBlocks.ANCIENT_OAK_DOOR, BMBlocks.ANCIENT_OAK_TRAPDOOR,
            BMBlocks.SMALL_ILLUNITE_BUD, BMBlocks.MEDIUM_ILLUNITE_BUD, BMBlocks.LARGE_ILLUNITE_BUD, BMBlocks.ILLUNITE_CLUSTER);
        BlockRenderLayerMap.putBlocks(ChunkSectionLayer.CUTOUT,
            BMBlocks.PURPLE_GLOWSHROOM, BMBlocks.GREEN_GLOWSHROOM, BMBlocks.ORANGE_GLOWSHROOM,
            BMBlocks.MYCELIUM_SPROUTS, BMBlocks.MYCELIUM_ROOTS, BMBlocks.TALL_BROWN_MUSHROOM,
            BMBlocks.TALL_RED_MUSHROOM, BMBlocks.BLIGHTED_BALSA_LEAVES,
            BMBlocks.SAGUARO_CACTUS, BMBlocks.BARREL_CACTUS, BMBlocks.BARREL_CACTUS_FLOWERED,
            BMBlocks.TUMBLEWEED,
            BMBlocks.BLIGHTED_BALSA.get("blighted_balsa_door"), BMBlocks.BLIGHTED_BALSA.get("blighted_balsa_trapdoor"));
        BlockRenderLayerMap.putBlocks(ChunkSectionLayer.CUTOUT,
            BMBlocks.WILLOW_LEAVES, BMBlocks.SWAMP_CYPRESS_LEAVES, BMBlocks.WILLOW_SAPLING, BMBlocks.SWAMP_CYPRESS_SAPLING,
            BMBlocks.WILLOWING_BRANCHES, BMBlocks.BUTTONBUSH, BMBlocks.MARIGOLD, BMBlocks.CATTAIL, BMBlocks.REED,
            BMBlocks.SMALL_LILY_PAD, BMBlocks.WATER_LILY, BMBlocks.WILLOW.get("willow_door"), BMBlocks.WILLOW.get("willow_trapdoor"),
            BMBlocks.SWAMP_CYPRESS.get("swamp_cypress_door"), BMBlocks.SWAMP_CYPRESS.get("swamp_cypress_trapdoor"),BMBlocks.LIGHTNING_BUG_BOTTLE);
        BMModelLayers.register();
        ArmorRenderer.register(context -> new CowboyHatArmorRenderer(context.getModelSet()),BMItems.COWBOY_HAT);
        EntityRenderers.register(BMEntities.OWL, OwlRenderer::new);
        EntityRenderers.register(BMEntities.GLOWFISH, GlowfishRenderer::new);
        EntityRenderers.register(BMEntities.TUMBLEWEED, TumbleweedRenderer::new);
        EntityRenderers.register(BMEntities.COWBOY, CowboyRenderer::new);
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type,renderer,helper,context)->{
            if(type==EntityType.HORSE) {
                @SuppressWarnings("unchecked")
                RenderLayerParent<HorseRenderState,HorseModel> parent=(RenderLayerParent<HorseRenderState,HorseModel>)(Object)renderer;
                @SuppressWarnings({"rawtypes","unchecked"})
                net.minecraft.client.renderer.entity.layers.RenderLayer layer=new CowboyHatLayer<>(parent,context.getModelSet(),true);
                helper.register(layer);
            }
        });
        EntityRenderers.register(BMEntities.SCUTTLER, ScuttlerRenderer::new);
        EntityRenderers.register(BMEntities.DRAGONFLY, DragonflyRenderer::new);
        EntityRenderers.register(BMEntities.LIGHTNING_BUG, LightningBugRenderer::new);
        EntityRenderers.register(BMEntities.LIGHTNING_BUG_ALTERNATE, LightningBugRenderer::new);
        EntityRenderers.register(BMEntities.LIGHTNING_BOTTLE, ThrownItemRenderer::new);
        EntityRenderers.register(BMEntities.DECAYED, DecayedRenderer::new);
        BlockEntityRenderers.register(BMBlockEntities.LIGHTNING_BUG_BOTTLE,LightningBugBottleRenderer::new);
        ParticleFactoryRegistry.getInstance().register(BMParticles.LIGHTNING_SPARK,LightningSparkParticle.Provider::new);
        ParticleFactoryRegistry.getInstance().register(BMParticles.BLOSSOM,BlossomParticle.Provider::new);
        ColorProviderRegistry.BLOCK.register((state,world,pos,tint)->{
            int color=world!=null&&pos!=null?BiomeColors.getAverageFoliageColor(world,pos):FoliageColor.FOLIAGE_DEFAULT;
            return shiftColor(color,-20,40,-20);
        },BMBlocks.SMALL_LILY_PAD,BMBlocks.WATER_LILY);
        ColorProviderRegistry.BLOCK.register((state,world,pos,tint)->{
            int color=world!=null&&pos!=null?BiomeColors.getAverageFoliageColor(world,pos):FoliageColor.FOLIAGE_DEFAULT;
            return world instanceof ClientLevel level&&pos!=null&&level.getBiome(pos).is(SWAMPS)?shiftColor(color,-10,15,-10):color;
        },BMBlocks.WILLOW_LEAVES,BMBlocks.WILLOWING_BRANCHES);
        ColorProviderRegistry.BLOCK.register((state,world,pos,tint)->world!=null&&pos!=null?BiomeColors.getAverageFoliageColor(world,pos):0x84AB6F,BMBlocks.SWAMP_CYPRESS_LEAVES);
        ColorProviderRegistry.BLOCK.register((state,world,pos,tint)->world!=null&&pos!=null?BiomeColors.getAverageFoliageColor(world,pos):FoliageColor.FOLIAGE_DEFAULT,
            BMBlocks.ANCIENT_OAK_LEAVES,BMBlocks.IVY);
        BiomeMakeover.LOGGER.info("Biome Makeover client initialized.");
    }

    private static int shiftColor(int color,int red,int green,int blue){
        int r=Math.clamp(((color>>16)&255)+red,0,255),g=Math.clamp(((color>>8)&255)+green,0,255),b=Math.clamp((color&255)+blue,0,255);
        return (r<<16)|(g<<8)|b;
    }
}
