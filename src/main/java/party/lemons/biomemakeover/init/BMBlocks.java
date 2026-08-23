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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
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
import party.lemons.biomemakeover.block.WildMushroomBlock;
import party.lemons.biomemakeover.block.GlowshroomPlantBlock;
import party.lemons.biomemakeover.block.UnderwaterGlowshroomBlock;
import party.lemons.biomemakeover.block.MyceliumSproutsBlock;
import party.lemons.biomemakeover.block.MyceliumRootsBlock;
import party.lemons.biomemakeover.block.TallMushroomBlock;

public final class BMBlocks {
    public static final WoodType BLIGHTED_BALSA_WOOD_TYPE = new WoodType("biomemakeover:blighted_balsa", BlockSetType.OAK);
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

    public static final Block BLACK_THISTLE = register("black_thistle", BlackThistleBlock::new,
        BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).noCollision().noOcclusion().instabreak().sound(SoundType.GRASS));
    public static final Block ITCHING_IVY = register("itching_ivy", ItchingIvyBlock::new,
        BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).replaceable().noCollision().noOcclusion().randomTicks().strength(0.2F).sound(SoundType.VINE));
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

    public static final Block ANCIENT_OAK_LEAVES = register("ancient_oak_leaves", Block::new,
        BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.2F).sound(SoundType.GRASS).noOcclusion());
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
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register(entries -> {
            entries.accept(MESMERITE); entries.accept(MESMERITE_STAIRS); entries.accept(MESMERITE_SLAB); entries.accept(MESMERITE_WALL);
            entries.accept(POLISHED_MESMERITE); entries.accept(POLISHED_MESMERITE_STAIRS); entries.accept(POLISHED_MESMERITE_SLAB); entries.accept(POLISHED_MESMERITE_WALL);
            entries.accept(ANCIENT_OAK_LOG); entries.accept(STRIPPED_ANCIENT_OAK_LOG); entries.accept(ANCIENT_OAK_WOOD); entries.accept(STRIPPED_ANCIENT_OAK_WOOD);
            entries.accept(ANCIENT_OAK_PLANKS); entries.accept(ANCIENT_OAK_STAIRS); entries.accept(ANCIENT_OAK_SLAB); entries.accept(ANCIENT_OAK_FENCE);
            entries.accept(ANCIENT_OAK_FENCE_GATE); entries.accept(ANCIENT_OAK_DOOR); entries.accept(ANCIENT_OAK_TRAPDOOR);
            entries.accept(ANCIENT_OAK_PRESSURE_PLATE); entries.accept(ANCIENT_OAK_BUTTON);
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS).register(entries -> {
            entries.accept(WILD_MUSHROOMS); entries.accept(BLACK_THISTLE); entries.accept(FOXGLOVE); entries.accept(ITCHING_IVY); entries.accept(OWL_NEST);
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
        });
    }
}
