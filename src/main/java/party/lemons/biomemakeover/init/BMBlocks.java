package party.lemons.biomemakeover.init;

import java.util.Optional;
import java.util.function.Function;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
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
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.material.MapColor;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.block.OwlNestBlock;
import party.lemons.biomemakeover.block.BlackThistleBlock;
import party.lemons.biomemakeover.block.ItchingIvyBlock;
import party.lemons.biomemakeover.block.WildMushroomBlock;

public final class BMBlocks {
    private static final ResourceKey<ConfiguredFeature<?, ?>> ANCIENT_OAK_TREE = ResourceKey.create(
        Registries.CONFIGURED_FEATURE, BiomeMakeover.id("dark_forest/ancient_oak"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> ANCIENT_OAK_SMALL_TREE = ResourceKey.create(
        Registries.CONFIGURED_FEATURE, BiomeMakeover.id("dark_forest/ancient_oak_small"));
    private static final TreeGrower ANCIENT_OAK_GROWER = new TreeGrower(
        "biomemakeover:ancient_oak", Optional.of(ANCIENT_OAK_TREE), Optional.of(ANCIENT_OAK_SMALL_TREE), Optional.empty());

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
        BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).noCollision().noOcclusion().instabreak().sound(SoundType.FUNGUS));

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

    public static void initialize() {
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
        });
    }
}
