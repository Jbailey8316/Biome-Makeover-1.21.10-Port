package party.lemons.biomemakeover.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.screen.AltarMenu;

public final class BMMenus {
    private static final ResourceKey<MenuType<?>> ALTAR_KEY = ResourceKey.create(Registries.MENU, BiomeMakeover.id("altar"));
    public static final MenuType<AltarMenu> ALTAR = Registry.register(BuiltInRegistries.MENU, ALTAR_KEY,
        new MenuType<>(AltarMenu::new, FeatureFlags.VANILLA_SET));

    private BMMenus() {}
    public static void initialize() {}
}
