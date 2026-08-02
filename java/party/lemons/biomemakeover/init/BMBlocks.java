package party.lemons.biomemakeover.init;

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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.block.WildMushroomBlock;
import party.lemons.biomemakeover.block.OwlNestBlock;

public final class BMBlocks {
    private static BlockBehaviour.Properties mesmeriteProperties() {
        return BlockBehaviour.Properties.of()
            .strength(1.5F)
            .mapColor(MapColor.ICE)
            .sound(SoundType.STONE);
    }

    public static final Block MESMERITE = register("mesmerite", Block::new, mesmeriteProperties());
    public static final Block MESMERITE_STAIRS = register(
        "mesmerite_stairs",
        properties -> new StairBlock(MESMERITE.defaultBlockState(), properties),
        mesmeriteProperties()
    );
    public static final Block MESMERITE_SLAB = register("mesmerite_slab", SlabBlock::new, mesmeriteProperties());
    public static final Block MESMERITE_WALL = register("mesmerite_wall", WallBlock::new, mesmeriteProperties());

    public static final Block POLISHED_MESMERITE = register("polished_mesmerite", Block::new, mesmeriteProperties());
    public static final Block POLISHED_MESMERITE_STAIRS = register(
        "polished_mesmerite_stairs",
        properties -> new StairBlock(POLISHED_MESMERITE.defaultBlockState(), properties),
        mesmeriteProperties()
    );
    public static final Block POLISHED_MESMERITE_SLAB = register("polished_mesmerite_slab", SlabBlock::new, mesmeriteProperties());
    public static final Block POLISHED_MESMERITE_WALL = register("polished_mesmerite_wall", WallBlock::new, mesmeriteProperties());

    public static final Block BLACK_THISTLE = register(
        "black_thistle",
        TallFlowerBlock::new,
        BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_BLACK)
            .noCollision()
            .noOcclusion()
            .instabreak()
            .sound(SoundType.GRASS)
    );

    public static final Block OWL_NEST = register(
        "owl_nest",
        OwlNestBlock::new,
        BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_BROWN)
            .noOcclusion()
            .strength(0.5F)
            .sound(SoundType.WOOD)
    );

    public static final Block WILD_MUSHROOMS = register(
        "wild_mushrooms",
        WildMushroomBlock::new,
        BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_BROWN)
            .noCollision()
            .noOcclusion()
            .instabreak()
            .sound(SoundType.FUNGUS)
    );

    private BMBlocks() {
    }

    private static Block register(
        String name,
        Function<BlockBehaviour.Properties, Block> factory,
        BlockBehaviour.Properties properties
    ) {
        ResourceLocation identifier = BiomeMakeover.id(name);
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, identifier);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, identifier);

        Block block = factory.apply(properties.setId(blockKey));
        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

        BlockItem blockItem = new BlockItem(
            block,
            new Item.Properties().setId(itemKey).useBlockDescriptionPrefix()
        );
        Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);
        return block;
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register(entries -> {
            entries.accept(MESMERITE);
            entries.accept(MESMERITE_STAIRS);
            entries.accept(MESMERITE_SLAB);
            entries.accept(MESMERITE_WALL);
            entries.accept(POLISHED_MESMERITE);
            entries.accept(POLISHED_MESMERITE_STAIRS);
            entries.accept(POLISHED_MESMERITE_SLAB);
            entries.accept(POLISHED_MESMERITE_WALL);
        });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS).register(entries -> {
            entries.accept(WILD_MUSHROOMS);
            entries.accept(BLACK_THISTLE);
            entries.accept(OWL_NEST);
        });
    }
}
