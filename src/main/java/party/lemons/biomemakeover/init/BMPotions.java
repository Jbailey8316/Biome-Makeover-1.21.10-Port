package party.lemons.biomemakeover.init;

import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import party.lemons.biomemakeover.BiomeMakeover;

public final class BMPotions {
    public static final Holder<Potion> NOCTURNAL = register("nocturnal_pot", new Potion("nocturnal",
        new MobEffectInstance(BMEffects.NOCTURNAL, 72000)));
    public static final Holder<Potion> LONG_NOCTURNAL = register("long_nocturnal_pot", new Potion("nocturnal",
        new MobEffectInstance(BMEffects.NOCTURNAL, 144000)));
    private BMPotions() {}
    private static Holder<Potion> register(String name, Potion potion) {
        return Registry.registerForHolder(BuiltInRegistries.POTION,
            ResourceKey.create(Registries.POTION, BiomeMakeover.id(name)), potion);
    }
    public static void initialize() {
        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
            FabricBrewingRecipeRegistryBuilder recipes = (FabricBrewingRecipeRegistryBuilder)(Object)builder;
            recipes.registerPotionRecipe(Potions.AWKWARD, Ingredient.of(BMItems.MOTH_SCALES), NOCTURNAL);
            recipes.registerPotionRecipe(NOCTURNAL, Ingredient.of(Items.REDSTONE), LONG_NOCTURNAL);
        });
    }
}
