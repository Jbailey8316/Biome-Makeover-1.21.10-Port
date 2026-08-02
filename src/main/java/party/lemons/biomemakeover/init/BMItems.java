package party.lemons.biomemakeover.init;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import party.lemons.biomemakeover.BiomeMakeover;

public final class BMItems {
    public static final Item LEAF_LITTER = register("leaf_litter");
    public static final Item OWL_EGG = register("owl_egg");

    private BMItems() {}

    private static Item register(String name) {
        ResourceLocation id = BiomeMakeover.id(name);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        return Registry.register(BuiltInRegistries.ITEM, key,
            new Item(new Item.Properties().setId(key)));
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS).register(entries -> {
            entries.accept(LEAF_LITTER);
            entries.accept(OWL_EGG);
        });
    }
}
