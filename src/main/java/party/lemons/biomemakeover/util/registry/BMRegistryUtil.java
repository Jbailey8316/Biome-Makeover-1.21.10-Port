package party.lemons.biomemakeover.util.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import party.lemons.biomemakeover.BiomeMakeover;

import java.util.Objects;
import java.util.function.Function;

/**
 * Small, project-local registration primitives for restored content.
 *
 * <p>This replaces only the registry plumbing historically supplied by
 * Taniwha's BlockHelper and ItemHelper. It deliberately does not choose block
 * properties, family membership, creative tabs, render layers, flammability,
 * or gameplay behavior.</p>
 */
public final class BMRegistryUtil {
    private BMRegistryUtil() {
    }

    public static <T extends Item> T registerItem(
        String path,
        Function<Item.Properties, T> factory,
        Item.Properties properties
    ) {
        ResourceLocation id = checkedId(path);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        T item = Objects.requireNonNull(factory.apply(properties.setId(key)), "item factory returned null for " + id);
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    public static <T extends Block> T registerBlockWithItem(
        String path,
        Function<BlockBehaviour.Properties, T> factory,
        BlockBehaviour.Properties blockProperties
    ) {
        ResourceLocation id = checkedId(path);
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
        T block = Objects.requireNonNull(factory.apply(blockProperties.setId(blockKey)), "block factory returned null for " + id);
        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
        Registry.register(
            BuiltInRegistries.ITEM,
            itemKey,
            new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix())
        );
        return block;
    }

    public static ResourceLocation checkedId(String path) {
        Objects.requireNonNull(path, "registry path");
        ResourceLocation id = BiomeMakeover.id(path);
        if (!id.getPath().equals(path)) {
            throw new IllegalArgumentException("Registry path must be an unqualified biomemakeover path: " + path);
        }
        return id;
    }
}
