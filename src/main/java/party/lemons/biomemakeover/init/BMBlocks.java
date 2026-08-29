package party.lemons.biomemakeover.init;

import java.util.Optional;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PlaceOnWaterBlockItem;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.block.OwlNestBlock;
import party.lemons.biomemakeover.block.BlackThistleBlock;
import party.lemons.biomemakeover.block.ItchingIvyBlock;
import party.lemons.biomemakeover.block.IvyBlock;
import party.lemons.biomemakeover.block.MothBlossomBlock;
import party.lemons.biomemakeover.block.RootlingCropBlock;
import party.lemons.biomemakeover.block.BuddingIlluniteBlock;
import party.lemons.biomemakeover.block.IlluniteClusterBlock;
import party.lemons.biomemakeover.block.WildMushroomBlock;
import party.lemons.biomemakeover.block.GlowshroomPlantBlock;
import party.lemons.biomemakeover.block.UnderwaterGlowshroomBlock;
import party.lemons.biomemakeover.block.MyceliumSproutsBlock;
import party.lemons.biomemakeover.block.MyceliumRootsBlock;
import party.lemons.biomemakeover.block.TallMushroomBlock;
import party.lemons.biomemakeover.block.BarrelCactusBlock;
import party.lemons.biomemakeover.block.SaguaroCactusBlock;
import party.lemons.biomemakeover.block.ReedBlock;
import party.lemons.biomemakeover.block.SmallLilyPadBlock;
import party.lemons.biomemakeover.block.WaterLilyBlock;
import party.lemons.biomemakeover.block.WaterSaplingBlock;
import party.lemons.biomemakeover.block.LightningBugBottleBlock;
import party.lemons.biomemakeover.block.WillowingBranchesBlock;
import party.lemons.biomemakeover.block.PeatFarmlandBlock;
import party.lemons.biomemakeover.block.PeatComposterBlock;

public final class BMBlocks {
    public static final net.minecraft.tags.TagKey<Block> MOTH_ATTRACTIVE = net.minecraft.tags.TagKey.create(
        net.minecraft.core.registries.Registries.BLOCK, BiomeMakeover.id("moth_attractive"));
    public static final TagKey<Block> BARREL_CACTUS_PLANTABLE = blockTag("barrel_cactus_plantable_on");
    public static final TagKey<Block> SAGUARO_CACTUS_PLANTABLE = blockTag("saguaro_cactus_plantable_on");
    public static final TagKey<Block> FISSURE_NO_REPLACE = blockTag("fissure_no_replace");
    public static final TagKey<Item> BARREL_CACTUS_IMMUNE = TagKey.create(Registries.ITEM, BiomeMakeover.id("barrel_cactus_immune"));
    public static final WoodType BLIGHTED_BALSA_WOOD_TYPE = new WoodType("biomemakeover:blighted_balsa", BlockSetType.OAK);
    public static final WoodType ANCIENT_OAK_WOOD_TYPE = new WoodType("biomemakeover:ancient_oak", BlockSetType.OAK);
    private static final SoundType ILLUNITE_SOUND = new SoundType(1F, 1F, BMSounds.ILLUNITE_BREAK,
        BMSounds.ILLUNITE_STEP, BMSounds.ILLUNITE_PLACE, BMSounds.ILLUNITE_HIT, BMSounds.ILLUNITE_STEP);
    private static final ResourceKey<ConfiguredFeature<?, ?>> ANCIENT_OAK_TREE = ResourceKey.create(
        Registries.CONFIGURED_FEATURE, BiomeMakeover.id("dark_forest/ancient_oak"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> ANCIENT_OAK_SMALL_TREE = ResourceKey.create(
        Registries.CONFIGURED_FEATURE, BiomeMakeover.id("dark_forest/ancient_oak_small"));
    private static final TreeGrower ANCIENT_OAK_GROWER = new TreeGrower(
        "biomemakeover:ancient_oak", Optional.of(ANCIENT_OAK_TREE), Optional.of(ANCIENT_OAK_SMALL_TREE), Optional.empty());
    private static final ResourceKey<ConfiguredFeature<?, ?>> HUGE_PURPLE_GLOWSHROOM = configured("mushroom_fields/huge_purple_glowshroom");
    private static final ResourceKey<ConfiguredFeature<?, ?>> HUGE_GREEN_GLOWSHROOM = configured("mushroom_fields/huge_green_glowshroom");
    private static final ResourceKey<ConfiguredFeature<?, ?>> HUGE_ORANGE_GLOWSHROOM = configured("mushroom_fields/huge_orange_glowshroom");
    private static final ResourceKey<ConfiguredFeature<?, ?>> BLIGHTED_BALSA_TREE = configured("mushroom_fields/blighted_balsa");
    private static final TreeGrower BLIGHTED_BALSA_GROWER = new TreeGrower(
        "biomemakeover:blighted_balsa", Optional.of(BLIGHTED_BALSA_TREE), Optional.empty(), Optional.empty());
    private static final ResourceKey<ConfiguredFeature<?, ?>> WILLOW_TREE = configured("swamp/willow");
    private static final ResourceKey<ConfiguredFeature<?, ?>> SWAMP_CYPRESS_TREE = configured("swamp/swamp_cypress");
    private static final TreeGrower WILLOW_GROWER = new TreeGrower("biomemakeover:willow",
        Optional.of(WILLOW_TREE), Optional.empty(), Optional.empty());
    private static final TreeGrower SWAMP_CYPRESS_GROWER = new TreeGrower("biomemakeover:swamp_cypress",
        Optional.of(SWAMP_CYPRESS_TREE), Optional.empty(), Optional.empty());

    private static BlockBehaviour.Properties stoneProps() {
        return BlockBehaviour.Properties.of().strength(1.5F).mapColor(MapColor.ICE).sound(SoundType.STONE);
    }
    private static BlockBehaviour.Properties woodProps() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava();
    }

    public static final Block MESMERITE = register("mesmerite", Block::new, stoneProps());
    public static final Block MESMERITE_STAIRS = register("mesmerite_stairs", p -> new StairBlock(MESMERITE.defaultBlockState(), p), stoneProps());
    public static final Block MESMERITE_SLAB = register("mesmerite_slab", SlabBlock::new, stoneProps());
    public static final Block MESMERITE_WALL = register("mesmerite_wall", WallBlock::new, stoneProps());
    public static final Block POLISHED_MESMERITE = register("polished_mesmerite", Block::new, stoneProps());
    public static final Block POLISHED_MESMERITE_STAIRS = register("polished_mesmerite_stairs", p -> new StairBlock(POLISHED_MESMERITE.defaultBlockState(), p), stoneProps());
    public static final Block POLISHED_MESMERITE_SLAB = register("polished_mesmerite_slab", SlabBlock::new, stoneProps());
    public static final Block POLISHED_MESMERITE_WALL = register("polished_mesmerite_wall", WallBlock::new, stoneProps());
    public static final Block ILLUNITE_BLOCK = register("illunite_block", Block::new,
        BlockBehaviour.Properties.of().strength(1.5F).requiresCorrectToolForDrops().mapColor(MapColor.LAPIS).sound(ILLUNITE_SOUND));
    public static final Block BUDDING_ILLUNITE = register("budding_illunite", BuddingIlluniteBlock::new,
        BlockBehaviour.Properties.of().strength(1.5F).mapColor(MapColor.LAPIS).sound(ILLUNITE_SOUND).randomTicks());
    public static final Block SMALL_ILLUNITE_BUD = register("small_illunite_bud", p -> new IlluniteClusterBlock(3, 4, p),
        illuniteCluster(5));
    public static final Block MEDIUM_ILLUNITE_BUD = register("medium_illunite_bud", p -> new IlluniteClusterBlock(4, 3, p),
        illuniteCluster(7));
    public static final Block LARGE_ILLUNITE_BUD = register("large_illunite_bud", p -> new IlluniteClusterBlock(5, 3, p),
        illuniteCluster(13));
    public static final Block ILLUNITE_CLUSTER = register("illunite_cluster", p -> new IlluniteClusterBlock(7, 3, p),
        illuniteCluster(15));

    public static final Block BLACK_THISTLE = register("black_thistle", BlackThistleBlock::new,
        BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).noCollision().noOcclusion().instabreak().sound(SoundType.GRASS));
    public static final Block IVY = register("ivy", IvyBlock::new,
        BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).replaceable().noCollision().noOcclusion().randomTicks().strength(0.15F).sound(SoundType.VINE));
    public static final Block ITCHING_IVY = register("itching_ivy", ItchingIvyBlock::new,
        BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).replaceable().noCollision().noOcclusion().randomTicks().strength(0.15F).speedFactor(0.5F).sound(SoundType.VINE));
    public static final Block MOTH_BLOSSOM = register("moth_blossom", MothBlossomBlock::new,
        BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).replaceable().noCollision().noOcclusion().randomTicks().strength(0.25F).speedFactor(0.5F).sound(SoundType.VINE));
    public static final Block ROOTLING_CROP = registerNoItem("rootling_crop", RootlingCropBlock::new,
        BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollision().randomTicks().instabreak().sound(SoundType.CROP).pushReaction(PushReaction.DESTROY));
    public static final Block PEAT_COMPOSTER = registerNoItem("peat_composter", PeatComposterBlock::new,
        BlockBehaviour.Properties.of().strength(0.6F).instrument(NoteBlockInstrument.BASS).sound(SoundType.WOOD)
            .ignitedByLava().mapColor(MapColor.WOOD));
    public static final Block FOXGLOVE = register("foxglove", TallFlowerBlock::new,
        BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollision().noOcclusion().instabreak().sound(SoundType.GRASS));
    public static final Block OWL_NEST = register("owl_nest", OwlNestBlock::new,
        BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).noOcclusion().strength(0.5F).sound(SoundType.WOOD));
    public static final Block WILD_MUSHROOMS = register("wild_mushrooms", WildMushroomBlock::new,
        BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).noCollision().noOcclusion().instabreak().randomTicks().sound(SoundType.FUNGUS));

    public static final Block PURPLE_GLOWSHROOM = register("purple_glowshroom", p -> new GlowshroomPlantBlock(HUGE_PURPLE_GLOWSHROOM, p),
        fungusPlant(MapColor.COLOR_PINK).lightLevel(s -> 13));
    public static final Block GREEN_GLOWSHROOM = register("green_glowshroom", p -> new GlowshroomPlantBlock(HUGE_GREEN_GLOWSHROOM, p),
        fungusPlant(MapColor.GLOW_LICHEN).lightLevel(s -> 13));
    public static final Block ORANGE_GLOWSHROOM = register("orange_glowshroom", p -> new UnderwaterGlowshroomBlock(HUGE_ORANGE_GLOWSHROOM, p),
        fungusPlant(MapColor.SAND).lightLevel(s -> 13));
    public static final Block PURPLE_GLOWSHROOM_BLOCK = register("purple_glowshroom_block", HugeMushroomBlock::new,
        mushroomCap(MapColor.COLOR_PINK));
    public static final Block GREEN_GLOWSHROOM_BLOCK = register("green_glowshroom_block", HugeMushroomBlock::new,
        mushroomCap(MapColor.GLOW_LICHEN));
    public static final Block ORANGE_GLOWSHROOM_BLOCK = register("orange_glowshroom_block", HugeMushroomBlock::new,
        mushroomCap(MapColor.SAND));
    public static final Block GLOWSHROOM_STEM = register("glowshroom_stem", HugeMushroomBlock::new,
        BlockBehaviour.Properties.of().strength(0.2F).mapColor(MapColor.CLAY).instrument(NoteBlockInstrument.BASS).lightLevel(s -> 7).sound(SoundType.FUNGUS));
    public static final Block MYCELIUM_SPROUTS = register("mycelium_sprouts", MyceliumSproutsBlock::new,
        fungusPlant(MapColor.TERRACOTTA_PURPLE).replaceable().sound(SoundType.NETHER_SPROUTS));
    public static final Block MYCELIUM_ROOTS = register("mycelium_roots", MyceliumRootsBlock::new,
        fungusPlant(MapColor.TERRACOTTA_PURPLE).replaceable().sound(SoundType.ROOTS));
    public static final Block TALL_BROWN_MUSHROOM = register("tall_brown_mushroom", TallMushroomBlock::new,
        fungusPlant(MapColor.DIRT));
    public static final Block TALL_RED_MUSHROOM = register("tall_red_mushroom", TallMushroomBlock::new,
        fungusPlant(MapColor.COLOR_RED));

    public static final Map<String, Block> MUSHROOM_DECORATION = createMushroomDecoration();
    public static final Map<String, Block> BLIGHTED_BALSA = createBlightedBalsa();
    public static final Block BLIGHTED_BALSA_SIGN = registerNoItem("blighted_balsa_sign", p -> new StandingSignBlock(BLIGHTED_BALSA_WOOD_TYPE, p), signProperties());
    public static final Block BLIGHTED_BALSA_WALL_SIGN = registerNoItem("blighted_balsa_wall_sign", p -> new WallSignBlock(BLIGHTED_BALSA_WOOD_TYPE, p), signProperties());
    public static final Block BLIGHTED_BALSA_HANGING_SIGN = registerNoItem("blighted_balsa_hanging_sign", p -> new CeilingHangingSignBlock(BLIGHTED_BALSA_WOOD_TYPE, p), signProperties());
    public static final Block BLIGHTED_BALSA_WALL_HANGING_SIGN = registerNoItem("blighted_balsa_wall_hanging_sign", p -> new WallHangingSignBlock(BLIGHTED_BALSA_WOOD_TYPE, p), signProperties());
    public static final Block BLIGHTED_BALSA_LEAVES = register("blighted_balsa_leaves", p -> new TintedParticleLeavesBlock(0.0F, p),
        BlockBehaviour.Properties.of().mapColor(MapColor.ICE).strength(0.2F).randomTicks().sound(SoundType.GRASS)
            .noOcclusion().pushReaction(PushReaction.DESTROY).ignitedByLava());
    public static final Block BLIGHTED_BALSA_SAPLING = register("blighted_balsa_sapling", p -> new SaplingBlock(BLIGHTED_BALSA_GROWER, p),
        fungusPlant(MapColor.WOOL).randomTicks());
    public static final Block POTTED_MYCELIUM_ROOTS = potted("potted_mycelium_roots", MYCELIUM_ROOTS, 0);
    public static final Block POTTED_PURPLE_GLOWSHROOM = potted("potted_purple_glowshroom", PURPLE_GLOWSHROOM, 13);
    public static final Block POTTED_GREEN_GLOWSHROOM = potted("potted_green_glowshroom", GREEN_GLOWSHROOM, 13);
    public static final Block POTTED_ORANGE_GLOWSHROOM = potted("potted_orange_glowshroom", ORANGE_GLOWSHROOM, 13);
    public static final Block POTTED_BLIGHTED_BALSA_SAPLING = potted("potted_blighted_balsa_sapling", BLIGHTED_BALSA_SAPLING, 0);
    public static final Block POTTED_WILD_MUSHROOMS = potted("potted_wild_mushrooms", WILD_MUSHROOMS, 0);

    // Reachable released Badlands content. Suspicious red sand and archaeology rewards remain Stage 10C-owned.
    public static final Block PAYDIRT = register("paydirt", Block::new, BlockBehaviour.Properties.of().strength(1.4F)
        .requiresCorrectToolForDrops().sound(SoundType.GRAVEL).mapColor(MapColor.TERRACOTTA_GRAY));
    public static final Block TUMBLEWEED = registerNoItem("tumbleweed", Block::new,
        BlockBehaviour.Properties.of().strength(0).mapColor(MapColor.COLOR_YELLOW));
    public static final Block SAGUARO_CACTUS = register("saguaro_cactus", SaguaroCactusBlock::new,
        BlockBehaviour.Properties.of().strength(0.4F).mapColor(MapColor.COLOR_GREEN).pushReaction(PushReaction.DESTROY).sound(SoundType.WOOL).randomTicks().noOcclusion());
    public static final Block BARREL_CACTUS = register("barrel_cactus", p -> new BarrelCactusBlock(false, p),
        BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).pushReaction(PushReaction.DESTROY).randomTicks().sound(SoundType.WOOL).noOcclusion().instabreak().noCollision());
    public static final Block BARREL_CACTUS_FLOWERED = register("barrel_cactus_flowered", p -> new BarrelCactusBlock(true, p),
        BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).pushReaction(PushReaction.DESTROY).sound(SoundType.WOOL).noOcclusion().instabreak().noCollision());
    public static final Block POTTED_SAGUARO_CACTUS = potted("potted_saguaro_cactus", SAGUARO_CACTUS, 0);
    public static final Block POTTED_BARREL_CACTUS = potted("potted_barrel_cactus", BARREL_CACTUS, 0);
    public static final Block POTTED_FLOWERED_BARREL_CACTUS = potted("potted_flowered_barrel_cactus", BARREL_CACTUS_FLOWERED, 0);
    public static final Map<String, Block> TERRACOTTA_BRICKS = createTerracottaBricks();
    public static final Map<String, Block> CRACKED_BRICKS = createCrackedBricks();

    // Stage 5: released Swamp ecosystem. Boats/chest boats remain deferred to the shared boat infrastructure stage.
    public static final WoodType WILLOW_WOOD_TYPE = new WoodType("biomemakeover:willow", BlockSetType.OAK);
    public static final WoodType SWAMP_CYPRESS_WOOD_TYPE = new WoodType("biomemakeover:swamp_cypress", BlockSetType.OAK);
    public static final Map<String, Block> WILLOW = createSwampWood("willow", WILLOW_WOOD_TYPE,
        MapColor.TERRACOTTA_GRAY, MapColor.SAND);
    public static final Map<String, Block> SWAMP_CYPRESS = createSwampWood("swamp_cypress", SWAMP_CYPRESS_WOOD_TYPE,
        MapColor.TERRACOTTA_BROWN, MapColor.TERRACOTTA_ORANGE);
    public static final Block WILLOW_SIGN = registerNoItem("willow_sign", p -> new StandingSignBlock(WILLOW_WOOD_TYPE, p), signProperties());
    public static final Block WILLOW_WALL_SIGN = registerNoItem("willow_wall_sign", p -> new WallSignBlock(WILLOW_WOOD_TYPE, p), signProperties());
    public static final Block WILLOW_HANGING_SIGN = registerNoItem("willow_hanging_sign", p -> new CeilingHangingSignBlock(WILLOW_WOOD_TYPE, p), signProperties());
    public static final Block WILLOW_WALL_HANGING_SIGN = registerNoItem("willow_wall_hanging_sign", p -> new WallHangingSignBlock(WILLOW_WOOD_TYPE, p), signProperties());
    public static final Block SWAMP_CYPRESS_SIGN = registerNoItem("swamp_cypress_sign", p -> new StandingSignBlock(SWAMP_CYPRESS_WOOD_TYPE, p), signProperties());
    public static final Block SWAMP_CYPRESS_WALL_SIGN = registerNoItem("swamp_cypress_wall_sign", p -> new WallSignBlock(SWAMP_CYPRESS_WOOD_TYPE, p), signProperties());
    public static final Block SWAMP_CYPRESS_HANGING_SIGN = registerNoItem("swamp_cypress_hanging_sign", p -> new CeilingHangingSignBlock(SWAMP_CYPRESS_WOOD_TYPE, p), signProperties());
    public static final Block SWAMP_CYPRESS_WALL_HANGING_SIGN = registerNoItem("swamp_cypress_wall_hanging_sign", p -> new WallHangingSignBlock(SWAMP_CYPRESS_WOOD_TYPE, p), signProperties());
    public static final Block WILLOW_LEAVES = register("willow_leaves", p -> new TintedParticleLeavesBlock(0.01F, p), swampLeaves(MapColor.TERRACOTTA_LIGHT_GREEN));
    public static final Block SWAMP_CYPRESS_LEAVES = register("swamp_cypress_leaves", p -> new TintedParticleLeavesBlock(0.01F, p), swampLeaves(MapColor.TERRACOTTA_GREEN));
    public static final Block WILLOW_SAPLING = register("willow_sapling", p -> new WaterSaplingBlock(WILLOW_GROWER, WILLOW_TREE, false, 1, p), swampPlant(MapColor.PLANT).randomTicks());
    public static final Block SWAMP_CYPRESS_SAPLING = register("swamp_cypress_sapling", p -> new WaterSaplingBlock(SWAMP_CYPRESS_GROWER, SWAMP_CYPRESS_TREE, true, 3, p), swampPlant(MapColor.PLANT).randomTicks());
    public static final Block POTTED_WILLOW_SAPLING = potted("potted_willow_sapling", WILLOW_SAPLING, 0);
    public static final Block POTTED_SWAMP_CYPRESS_SAPLING = potted("potted_swamp_cypress_sapling", SWAMP_CYPRESS_SAPLING, 0);
    public static final Block WILLOWING_BRANCHES = register("willowing_branches", WillowingBranchesBlock::new,
        swampPlant(MapColor.PLANT).randomTicks().sound(SoundType.VINE).offsetType(BlockBehaviour.OffsetType.XZ).ignitedByLava());
    public static final Block PEAT = register("peat", Block::new, BlockBehaviour.Properties.of().strength(0.5F).sound(SoundType.WET_GRASS).mapColor(MapColor.TERRACOTTA_GRAY));
    public static final Block DRIED_PEAT = register("dried_peat", Block::new, BlockBehaviour.Properties.of().strength(1F).sound(SoundType.NETHERRACK).mapColor(MapColor.TERRACOTTA_BROWN));
    public static final Block MOSSY_PEAT = register("mossy_peat", Block::new, BlockBehaviour.Properties.of().strength(0.5F).randomTicks().sound(SoundType.WET_GRASS).mapColor(MapColor.TERRACOTTA_GRAY));
    public static final Block PEAT_FARMLAND = register("peat_farmland", PeatFarmlandBlock::new,
        BlockBehaviour.Properties.of().strength(0.5F).randomTicks().sound(SoundType.WET_GRASS).mapColor(MapColor.COLOR_GREEN));
    public static final Map<String, Block> PEAT_MASONRY = createPeatMasonry();
    public static final Block BUTTONBUSH = register("buttonbush", TallFlowerBlock::new, swampPlant(MapColor.WOOL));
    public static final Block MARIGOLD = register("marigold", TallFlowerBlock::new, swampPlant(MapColor.COLOR_ORANGE));
    public static final Block CATTAIL = register("cattail", ReedBlock::new, swampPlant(MapColor.GLOW_LICHEN).offsetType(BlockBehaviour.OffsetType.XZ));
    public static final Block REED = register("reed", ReedBlock::new, swampPlant(MapColor.GLOW_LICHEN).offsetType(BlockBehaviour.OffsetType.XZ));
    public static final Block REED_THATCH = register("reed_thatch", Block::new, thatchProps());
    public static final Block REED_THATCH_SLAB = register("reed_thatch_slab", SlabBlock::new, thatchProps());
    public static final Block REED_THATCH_STAIRS = register("reed_thatch_stairs", p -> new StairBlock(REED_THATCH.defaultBlockState(), p), thatchProps());
    public static final Block SMALL_LILY_PAD = registerOnWater("small_lily_pad", SmallLilyPadBlock::new,
        BlockBehaviour.Properties.of().instabreak().noCollision().sound(SoundType.LILY_PAD).mapColor(MapColor.PLANT).pushReaction(PushReaction.DESTROY));
    public static final Block WATER_LILY = registerOnWater("water_lily", WaterLilyBlock::new,
        BlockBehaviour.Properties.of().instabreak().noCollision().sound(SoundType.LILY_PAD).mapColor(MapColor.COLOR_PINK).pushReaction(PushReaction.DESTROY));
    public static final Block LIGHTNING_BUG_BOTTLE = register("lightning_bug_bottle", LightningBugBottleBlock::new,
        BlockBehaviour.Properties.of().strength(0.5F).lightLevel(state -> 15).noOcclusion().pushReaction(PushReaction.DESTROY));

    public static final Block ANCIENT_OAK_LOG = register("ancient_oak_log", RotatedPillarBlock::new, woodProps());
    public static final Block STRIPPED_ANCIENT_OAK_LOG = register("stripped_ancient_oak_log", RotatedPillarBlock::new, woodProps());
    public static final Block ANCIENT_OAK_WOOD = register("ancient_oak_wood", RotatedPillarBlock::new, woodProps());
    public static final Block STRIPPED_ANCIENT_OAK_WOOD = register("stripped_ancient_oak_wood", RotatedPillarBlock::new, woodProps());
    public static final Block ANCIENT_OAK_PLANKS = register("ancient_oak_planks", Block::new, woodProps());
    public static final Block ANCIENT_OAK_STAIRS = register("ancient_oak_stairs", p -> new StairBlock(ANCIENT_OAK_PLANKS.defaultBlockState(), p), woodProps());
    public static final Block ANCIENT_OAK_SLAB = register("ancient_oak_slab", SlabBlock::new, woodProps());
    public static final Block ANCIENT_OAK_FENCE = register("ancient_oak_fence", FenceBlock::new, woodProps());
    public static final Block ANCIENT_OAK_FENCE_GATE = register("ancient_oak_fence_gate", p -> new FenceGateBlock(WoodType.OAK, p), woodProps());
    public static final Block ANCIENT_OAK_DOOR = register("ancient_oak_door", p -> new DoorBlock(BlockSetType.OAK, p),
        woodProps().noOcclusion());
    public static final Block ANCIENT_OAK_TRAPDOOR = register("ancient_oak_trapdoor", p -> new TrapDoorBlock(BlockSetType.OAK, p),
        woodProps().noOcclusion());
    public static final Block ANCIENT_OAK_PRESSURE_PLATE = register("ancient_oak_pressure_plate", p -> new PressurePlateBlock(BlockSetType.OAK, p),
        woodProps().noCollision().strength(0.5F));
    public static final Block ANCIENT_OAK_BUTTON = register("ancient_oak_button", p -> new ButtonBlock(BlockSetType.OAK, 30, p),
        woodProps().noCollision().strength(0.5F));
    public static final Block ANCIENT_OAK_SIGN = registerNoItem("ancient_oak_sign", p -> new StandingSignBlock(ANCIENT_OAK_WOOD_TYPE, p), signProperties());
    public static final Block ANCIENT_OAK_WALL_SIGN = registerNoItem("ancient_oak_wall_sign", p -> new WallSignBlock(ANCIENT_OAK_WOOD_TYPE, p), signProperties());
    public static final Block ANCIENT_OAK_HANGING_SIGN = registerNoItem("ancient_oak_hanging_sign", p -> new CeilingHangingSignBlock(ANCIENT_OAK_WOOD_TYPE, p), signProperties());
    public static final Block ANCIENT_OAK_WALL_HANGING_SIGN = registerNoItem("ancient_oak_wall_hanging_sign", p -> new WallHangingSignBlock(ANCIENT_OAK_WOOD_TYPE, p), signProperties());

    public static final Block ANCIENT_OAK_LEAVES = register("ancient_oak_leaves", p -> new TintedParticleLeavesBlock(0.01F, p),
        BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.2F).randomTicks().sound(SoundType.GRASS)
            .noOcclusion().pushReaction(PushReaction.DESTROY).ignitedByLava());
    public static final Block ANCIENT_OAK_SAPLING = register("ancient_oak_sapling", p -> new SaplingBlock(ANCIENT_OAK_GROWER, p),
        BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollision().noOcclusion().randomTicks().instabreak().sound(SoundType.GRASS));

    private BMBlocks() {}

    private static ResourceKey<ConfiguredFeature<?, ?>> configured(String path) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, BiomeMakeover.id(path));
    }

    private static BlockBehaviour.Properties fungusPlant(MapColor color) {
        return BlockBehaviour.Properties.of().mapColor(color).noCollision().noOcclusion().instabreak()
            .pushReaction(PushReaction.DESTROY).sound(SoundType.FUNGUS);
    }

    private static BlockBehaviour.Properties mushroomCap(MapColor color) {
        return BlockBehaviour.Properties.of().mapColor(color).strength(0.2F).instrument(NoteBlockInstrument.BASS).lightLevel(s -> 15).sound(SoundType.FUNGUS);
    }

    private static BlockBehaviour.Properties illuniteCluster(int light) {
        return BlockBehaviour.Properties.of().strength(0.5F).mapColor(MapColor.LAPIS).sound(ILLUNITE_SOUND)
            .noOcclusion().randomTicks().pushReaction(PushReaction.DESTROY)
            .emissiveRendering((state, level, pos) -> true).hasPostProcess((state, level, pos) -> true)
            .lightLevel(state -> state.getValue(IlluniteClusterBlock.TYPE) == IlluniteClusterBlock.Type.NIGHT ? light : 2);
    }

    private static Map<String, Block> createMushroomDecoration() {
        Map<String, Block> blocks = new LinkedHashMap<>();
        decoration(blocks, "red_mushroom_brick", MapColor.COLOR_RED, 0.8F, SoundType.FUNGUS, 0, false);
        decoration(blocks, "brown_mushroom_brick", MapColor.COLOR_BROWN, 0.8F, SoundType.FUNGUS, 0, false);
        decoration(blocks, "purple_glowshroom_brick", MapColor.COLOR_PURPLE, 0.8F, SoundType.FUNGUS, 13, false);
        decoration(blocks, "green_glowshroom_brick", MapColor.GLOW_LICHEN, 0.8F, SoundType.FUNGUS, 13, false);
        decoration(blocks, "orange_glowshroom_brick", MapColor.COLOR_ORANGE, 0.8F, SoundType.FUNGUS, 13, false);
        decoration(blocks, "glowshroom_stem_brick", MapColor.CLAY, 0.8F, SoundType.FUNGUS, 7, false);
        decoration(blocks, "mushroom_stem_brick", MapColor.WOOL, 0.8F, SoundType.FUNGUS, 0, false);
        decoration(blocks, "blighted_cobblestone", MapColor.STONE, 2.0F, SoundType.STONE, 0, true);
        decoration(blocks, "blighted_stone_bricks", MapColor.STONE, 2.0F, SoundType.STONE, 0, true);
        return Map.copyOf(blocks);
    }

    private static void decoration(Map<String, Block> result, String base, MapColor color, float strength,
                                   SoundType sound, int light, boolean tool) {
        BlockBehaviour.Properties baseProps = decorationProps(color, strength, sound, light, tool);
        Block block = register(base, Block::new, baseProps);
        result.put(base, block);
        result.put(base + "_slab", register(base + "_slab", SlabBlock::new, decorationProps(color, strength, sound, light, tool)));
        result.put(base + "_stairs", register(base + "_stairs", p -> new StairBlock(block.defaultBlockState(), p), decorationProps(color, strength, sound, light, tool)));
        result.put(base + "_wall", register(base + "_wall", WallBlock::new, decorationProps(color, strength, sound, light, tool)));
    }

    private static BlockBehaviour.Properties decorationProps(MapColor color, float strength, SoundType sound, int light, boolean tool) {
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of().mapColor(color).strength(strength).sound(sound)
            .instrument(tool ? NoteBlockInstrument.BASEDRUM : NoteBlockInstrument.BASS);
        if (light > 0) properties = properties.lightLevel(s -> light);
        if (tool) properties = properties.requiresCorrectToolForDrops();
        return properties;
    }

    private static Map<String, Block> createBlightedBalsa() {
        Map<String, Block> blocks = new LinkedHashMap<>();
        Block planks = register("blighted_balsa_planks", Block::new, balsaPlanks());
        blocks.put("blighted_balsa_log", register("blighted_balsa_log", RotatedPillarBlock::new, balsaLog(MapColor.TERRACOTTA_GREEN)));
        blocks.put("stripped_blighted_balsa_log", register("stripped_blighted_balsa_log", RotatedPillarBlock::new, balsaLog(MapColor.WOOL)));
        blocks.put("blighted_balsa_wood", register("blighted_balsa_wood", RotatedPillarBlock::new, balsaLog(MapColor.TERRACOTTA_GREEN)));
        blocks.put("stripped_blighted_balsa_wood", register("stripped_blighted_balsa_wood", RotatedPillarBlock::new, balsaLog(MapColor.WOOL)));
        blocks.put("blighted_balsa_planks", planks);
        blocks.put("blighted_balsa_slab", register("blighted_balsa_slab", SlabBlock::new, balsaPlanks()));
        blocks.put("blighted_balsa_stairs", register("blighted_balsa_stairs", p -> new StairBlock(planks.defaultBlockState(), p), balsaPlanks()));
        blocks.put("blighted_balsa_fence", register("blighted_balsa_fence", FenceBlock::new, balsaPlanks().forceSolidOn()));
        blocks.put("blighted_balsa_fence_gate", register("blighted_balsa_fence_gate", p -> new FenceGateBlock(BLIGHTED_BALSA_WOOD_TYPE, p), balsaPlanks().forceSolidOn()));
        blocks.put("blighted_balsa_pressure_plate", register("blighted_balsa_pressure_plate", p -> new PressurePlateBlock(BlockSetType.OAK, p), balsaPlanks().noCollision().strength(0.5F)));
        blocks.put("blighted_balsa_button", register("blighted_balsa_button", p -> new ButtonBlock(BlockSetType.OAK, 30, p), balsaPlanks().noCollision().strength(0.5F)));
        blocks.put("blighted_balsa_trapdoor", register("blighted_balsa_trapdoor", p -> new TrapDoorBlock(BlockSetType.OAK, p), balsaPlanks().noOcclusion().strength(3.0F)));
        blocks.put("blighted_balsa_door", register("blighted_balsa_door", p -> new DoorBlock(BlockSetType.OAK, p), balsaPlanks().noOcclusion().strength(3.0F)));
        return Map.copyOf(blocks);
    }

    private static Map<String, Block> createTerracottaBricks() {
        Map<String, Block> blocks = new LinkedHashMap<>();
        terracottaFamily(blocks, "", MapColor.COLOR_ORANGE);
        terracottaFamily(blocks, "white_", MapColor.SNOW);
        terracottaFamily(blocks, "orange_", MapColor.COLOR_ORANGE);
        terracottaFamily(blocks, "magenta_", MapColor.COLOR_MAGENTA);
        terracottaFamily(blocks, "light_blue_", MapColor.COLOR_LIGHT_BLUE);
        terracottaFamily(blocks, "yellow_", MapColor.COLOR_YELLOW);
        terracottaFamily(blocks, "lime_", MapColor.COLOR_LIGHT_GREEN);
        terracottaFamily(blocks, "pink_", MapColor.COLOR_PINK);
        terracottaFamily(blocks, "gray_", MapColor.COLOR_GRAY);
        terracottaFamily(blocks, "light_gray_", MapColor.COLOR_LIGHT_GRAY);
        terracottaFamily(blocks, "cyan_", MapColor.COLOR_CYAN);
        terracottaFamily(blocks, "purple_", MapColor.COLOR_PURPLE);
        terracottaFamily(blocks, "blue_", MapColor.COLOR_BLUE);
        terracottaFamily(blocks, "brown_", MapColor.COLOR_BROWN);
        terracottaFamily(blocks, "green_", MapColor.COLOR_GREEN);
        terracottaFamily(blocks, "red_", MapColor.COLOR_RED);
        terracottaFamily(blocks, "black_", MapColor.COLOR_BLACK);
        return Map.copyOf(blocks);
    }

    private static void terracottaFamily(Map<String, Block> result, String prefix, MapColor color) {
        String base = prefix + "terracotta_bricks";
        String decoration = prefix + "terracotta_brick";
        Block block = register(base, Block::new, terracottaProps(color));
        result.put(base, block);
        result.put(decoration + "_slab", register(decoration + "_slab", SlabBlock::new, terracottaProps(color)));
        result.put(decoration + "_stairs", register(decoration + "_stairs", p -> new StairBlock(block.defaultBlockState(), p), terracottaProps(color)));
        result.put(decoration + "_wall", register(decoration + "_wall", WallBlock::new, terracottaProps(color)));
    }

    private static Map<String, Block> createCrackedBricks() {
        Map<String, Block> blocks = new LinkedHashMap<>();
        Block base = register("cracked_bricks", Block::new, terracottaProps(MapColor.COLOR_RED).strength(2F, 6F));
        blocks.put("cracked_bricks", base);
        blocks.put("cracked_brick_slab", register("cracked_brick_slab", SlabBlock::new, terracottaProps(MapColor.COLOR_RED).strength(2F, 6F)));
        blocks.put("cracked_brick_stairs", register("cracked_brick_stairs", p -> new StairBlock(base.defaultBlockState(), p), terracottaProps(MapColor.COLOR_RED).strength(2F, 6F)));
        blocks.put("cracked_brick_wall", register("cracked_brick_wall", WallBlock::new, terracottaProps(MapColor.COLOR_RED).strength(2F, 6F)));
        return Map.copyOf(blocks);
    }

    private static Map<String, Block> createSwampWood(String base, WoodType woodType, MapColor bark, MapColor planksColor) {
        Map<String, Block> blocks = new LinkedHashMap<>();
        Block planks = register(base + "_planks", Block::new, swampWood(planksColor));
        blocks.put(base + "_log", register(base + "_log", RotatedPillarBlock::new, swampWood(bark)));
        blocks.put("stripped_" + base + "_log", register("stripped_" + base + "_log", RotatedPillarBlock::new, swampWood(planksColor)));
        blocks.put(base + "_planks", planks);
        blocks.put(base + "_wood", register(base + "_wood", RotatedPillarBlock::new, swampWood(bark)));
        blocks.put("stripped_" + base + "_wood", register("stripped_" + base + "_wood", RotatedPillarBlock::new, swampWood(planksColor)));
        blocks.put(base + "_slab", register(base + "_slab", SlabBlock::new, swampWood(planksColor)));
        blocks.put(base + "_stairs", register(base + "_stairs", p -> new StairBlock(planks.defaultBlockState(), p), swampWood(planksColor)));
        blocks.put(base + "_fence", register(base + "_fence", FenceBlock::new, swampWood(planksColor).forceSolidOn()));
        blocks.put(base + "_fence_gate", register(base + "_fence_gate", p -> new FenceGateBlock(woodType, p), swampWood(planksColor).forceSolidOn()));
        blocks.put(base + "_pressure_plate", register(base + "_pressure_plate", p -> new PressurePlateBlock(BlockSetType.OAK, p), swampWood(planksColor).noCollision().strength(0.5F)));
        blocks.put(base + "_button", register(base + "_button", p -> new ButtonBlock(BlockSetType.OAK, 30, p), swampWood(planksColor).noCollision().strength(0.5F)));
        blocks.put(base + "_trapdoor", register(base + "_trapdoor", p -> new TrapDoorBlock(BlockSetType.OAK, p), swampWood(planksColor).noOcclusion().strength(3F)));
        blocks.put(base + "_door", register(base + "_door", p -> new DoorBlock(BlockSetType.OAK, p), swampWood(planksColor).noOcclusion().strength(3F)));
        return Map.copyOf(blocks);
    }

    private static Map<String, Block> createPeatMasonry() {
        Map<String, Block> result = new LinkedHashMap<>();
        peatFamily(result, "dried_peat_bricks", "dried_peat_bricks");
        peatFamily(result, "mossy_dried_peat_bricks", "mossy_dried_peat_brick");
        peatFamily(result, "cracked_dried_peat_bricks", "cracked_dried_peat_brick");
        return Map.copyOf(result);
    }

    private static void peatFamily(Map<String, Block> result, String baseName, String decorationName) {
        Block base = register(baseName, Block::new, peatBrickProps());
        result.put(baseName, base);
        result.put(decorationName + "_slab", register(decorationName + "_slab", SlabBlock::new, peatBrickProps()));
        result.put(decorationName + "_stairs", register(decorationName + "_stairs", p -> new StairBlock(base.defaultBlockState(), p), peatBrickProps()));
        result.put(decorationName + "_wall", register(decorationName + "_wall", WallBlock::new, peatBrickProps()));
    }

    private static BlockBehaviour.Properties swampWood(MapColor color) { return woodProps().mapColor(color); }
    private static BlockBehaviour.Properties swampLeaves(MapColor color) { return BlockBehaviour.Properties.of().mapColor(color).strength(0.2F).randomTicks().sound(SoundType.GRASS).noOcclusion().pushReaction(PushReaction.DESTROY).ignitedByLava(); }
    private static BlockBehaviour.Properties swampPlant(MapColor color) { return BlockBehaviour.Properties.of().mapColor(color).replaceable().noCollision().noOcclusion().instabreak().pushReaction(PushReaction.DESTROY).sound(SoundType.GRASS); }
    private static BlockBehaviour.Properties thatchProps() { return BlockBehaviour.Properties.of().strength(0.5F).instrument(NoteBlockInstrument.BANJO).mapColor(MapColor.PODZOL).sound(SoundType.GRASS).ignitedByLava(); }
    private static BlockBehaviour.Properties peatBrickProps() { return BlockBehaviour.Properties.of().strength(2F).instrument(NoteBlockInstrument.BASEDRUM).mapColor(MapColor.TERRACOTTA_BROWN).requiresCorrectToolForDrops().sound(SoundType.STONE); }

    private static BlockBehaviour.Properties terracottaProps(MapColor color) {
        return BlockBehaviour.Properties.of().strength(2F).mapColor(color).instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops().sound(SoundType.STONE);
    }

    private static TagKey<Block> blockTag(String path) {
        return TagKey.create(Registries.BLOCK, BiomeMakeover.id(path));
    }

    private static BlockBehaviour.Properties balsaLog(MapColor color) {
        return BlockBehaviour.Properties.of().mapColor(color).strength(2.0F, 2.0F).instrument(NoteBlockInstrument.BASS).sound(SoundType.WOOD).ignitedByLava();
    }

    private static BlockBehaviour.Properties balsaPlanks() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(2.0F, 3.0F).instrument(NoteBlockInstrument.BASS).sound(SoundType.WOOD).ignitedByLava();
    }

    private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
        ResourceLocation id = BiomeMakeover.id(name);
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
        Block block = factory.apply(properties.setId(blockKey));
        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
        Registry.register(BuiltInRegistries.ITEM, itemKey,
            new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix()));
        return block;
    }

    /** Preserves the released lily-pad item contract: target source water, then place above it. */
    private static Block registerOnWater(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
        ResourceLocation id = BiomeMakeover.id(name);
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
        Block block = factory.apply(properties.setId(blockKey));
        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
        Registry.register(BuiltInRegistries.ITEM, itemKey,
            new PlaceOnWaterBlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix()));
        return block;
    }

    private static Block potted(String name, Block plant, int light) {
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of().instabreak().noOcclusion();
        if (light > 0) properties = properties.lightLevel(s -> light);
        ResourceLocation id = BiomeMakeover.id(name);
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id);
        return Registry.register(BuiltInRegistries.BLOCK, key, new FlowerPotBlock(plant, properties.setId(key)));
    }

    private static Block registerNoItem(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, BiomeMakeover.id(name));
        return Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(properties.setId(key)));
    }

    private static BlockBehaviour.Properties signProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(1.0F).instrument(NoteBlockInstrument.BASS)
            .sound(SoundType.WOOD).noCollision().forceSolidOn().pushReaction(PushReaction.DESTROY).ignitedByLava();
    }

    public static void initialize() {
        StrippableBlockRegistry.register(BLIGHTED_BALSA.get("blighted_balsa_log"), BLIGHTED_BALSA.get("stripped_blighted_balsa_log"));
        StrippableBlockRegistry.register(BLIGHTED_BALSA.get("blighted_balsa_wood"), BLIGHTED_BALSA.get("stripped_blighted_balsa_wood"));
        BLIGHTED_BALSA.values().forEach(block -> FlammableBlockRegistry.getDefaultInstance().add(block, 5, 20));
        FlammableBlockRegistry.getDefaultInstance().add(BLIGHTED_BALSA_LEAVES, 30, 60);
        FlammableBlockRegistry.getDefaultInstance().add(WILD_MUSHROOMS, 15, 100);
        StrippableBlockRegistry.register(WILLOW.get("willow_log"), WILLOW.get("stripped_willow_log"));
        StrippableBlockRegistry.register(WILLOW.get("willow_wood"), WILLOW.get("stripped_willow_wood"));
        StrippableBlockRegistry.register(SWAMP_CYPRESS.get("swamp_cypress_log"), SWAMP_CYPRESS.get("stripped_swamp_cypress_log"));
        StrippableBlockRegistry.register(SWAMP_CYPRESS.get("swamp_cypress_wood"), SWAMP_CYPRESS.get("stripped_swamp_cypress_wood"));
        WILLOW.values().forEach(block -> FlammableBlockRegistry.getDefaultInstance().add(block, 5, 20));
        SWAMP_CYPRESS.values().forEach(block -> FlammableBlockRegistry.getDefaultInstance().add(block, 5, 20));
        FlammableBlockRegistry.getDefaultInstance().add(WILLOW_LEAVES, 30, 60);
        FlammableBlockRegistry.getDefaultInstance().add(SWAMP_CYPRESS_LEAVES, 30, 60);
        FlammableBlockRegistry.getDefaultInstance().add(WILLOWING_BRANCHES, 15, 100);
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register(entries -> {
            entries.accept(MESMERITE); entries.accept(MESMERITE_STAIRS); entries.accept(MESMERITE_SLAB); entries.accept(MESMERITE_WALL);
            entries.accept(POLISHED_MESMERITE); entries.accept(POLISHED_MESMERITE_STAIRS); entries.accept(POLISHED_MESMERITE_SLAB); entries.accept(POLISHED_MESMERITE_WALL);
            entries.accept(ANCIENT_OAK_LOG); entries.accept(STRIPPED_ANCIENT_OAK_LOG); entries.accept(ANCIENT_OAK_WOOD); entries.accept(STRIPPED_ANCIENT_OAK_WOOD);
            entries.accept(ANCIENT_OAK_PLANKS); entries.accept(ANCIENT_OAK_STAIRS); entries.accept(ANCIENT_OAK_SLAB); entries.accept(ANCIENT_OAK_FENCE);
            entries.accept(ANCIENT_OAK_FENCE_GATE); entries.accept(ANCIENT_OAK_DOOR); entries.accept(ANCIENT_OAK_TRAPDOOR);
            entries.accept(ANCIENT_OAK_PRESSURE_PLATE); entries.accept(ANCIENT_OAK_BUTTON);
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS).register(entries -> {
            entries.accept(WILD_MUSHROOMS); entries.accept(BLACK_THISTLE); entries.accept(FOXGLOVE); entries.accept(IVY); entries.accept(ITCHING_IVY); entries.accept(MOTH_BLOSSOM);
            entries.accept(ILLUNITE_BLOCK); entries.accept(BUDDING_ILLUNITE); entries.accept(SMALL_ILLUNITE_BUD); entries.accept(MEDIUM_ILLUNITE_BUD); entries.accept(LARGE_ILLUNITE_BUD); entries.accept(ILLUNITE_CLUSTER);
            entries.accept(ANCIENT_OAK_LEAVES); entries.accept(ANCIENT_OAK_SAPLING);
            entries.accept(PURPLE_GLOWSHROOM); entries.accept(GREEN_GLOWSHROOM); entries.accept(ORANGE_GLOWSHROOM);
            entries.accept(MYCELIUM_SPROUTS); entries.accept(MYCELIUM_ROOTS); entries.accept(TALL_BROWN_MUSHROOM); entries.accept(TALL_RED_MUSHROOM);
            entries.accept(BLIGHTED_BALSA_LEAVES); entries.accept(BLIGHTED_BALSA_SAPLING);
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register(entries -> {
            MUSHROOM_DECORATION.values().forEach(entries::accept);
            BLIGHTED_BALSA.values().forEach(entries::accept);
            entries.accept(PURPLE_GLOWSHROOM_BLOCK); entries.accept(GREEN_GLOWSHROOM_BLOCK);
            entries.accept(ORANGE_GLOWSHROOM_BLOCK); entries.accept(GLOWSHROOM_STEM);
            TERRACOTTA_BRICKS.values().forEach(entries::accept);
            CRACKED_BRICKS.values().forEach(entries::accept);
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS).register(entries -> {
            entries.accept(PAYDIRT); entries.accept(SAGUARO_CACTUS); entries.accept(BARREL_CACTUS); entries.accept(BARREL_CACTUS_FLOWERED);
            entries.accept(WILLOW_LEAVES); entries.accept(WILLOW_SAPLING); entries.accept(SWAMP_CYPRESS_LEAVES); entries.accept(SWAMP_CYPRESS_SAPLING);
            entries.accept(WILLOWING_BRANCHES); entries.accept(PEAT); entries.accept(MOSSY_PEAT); entries.accept(CATTAIL); entries.accept(REED);
            entries.accept(BUTTONBUSH); entries.accept(MARIGOLD); entries.accept(SMALL_LILY_PAD); entries.accept(WATER_LILY); entries.accept(LIGHTNING_BUG_BOTTLE);
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register(entries -> {
            WILLOW.values().forEach(entries::accept); SWAMP_CYPRESS.values().forEach(entries::accept);
            entries.accept(DRIED_PEAT); entries.accept(PEAT_FARMLAND); PEAT_MASONRY.values().forEach(entries::accept);
            entries.accept(REED_THATCH); entries.accept(REED_THATCH_SLAB); entries.accept(REED_THATCH_STAIRS);
        });
    }
}
