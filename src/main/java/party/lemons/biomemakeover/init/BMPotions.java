package party.lemons.biomemakeover.init;

import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import party.lemons.biomemakeover.BiomeMakeover;

public final class BMPotions {
    public static final Holder<Potion> ADRENALINE = register("adrenaline", new Potion("adrenaline", new MobEffectInstance(MobEffects.STRENGTH, 2400, 1), new MobEffectInstance(MobEffects.SPEED, 2400, 1), new MobEffectInstance(MobEffects.RESISTANCE, 2400)));
    public static final Holder<Potion> ASSASSIN = register("assassin", new Potion("assassin", new MobEffectInstance(MobEffects.INVISIBILITY, 2400), new MobEffectInstance(MobEffects.SLOW_FALLING, 2400, 1), new MobEffectInstance(MobEffects.JUMP_BOOST, 2400, 2)));
    public static final Holder<Potion> DARKNESS = register("darkness", new Potion("darkness", new MobEffectInstance(MobEffects.BLINDNESS, 300), new MobEffectInstance(MobEffects.SLOWNESS, 300), new MobEffectInstance(MobEffects.WEAKNESS, 170)));
    public static final Holder<Potion> DOLPHIN_MASTER = register("dolphin_master", new Potion("dolphin_master", new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 1800), new MobEffectInstance(MobEffects.WATER_BREATHING, 1800)));
    public static final Holder<Potion> LIQUID_BREAD = register("liquid_bread", new Potion("liquid_bread", new MobEffectInstance(MobEffects.SATURATION, 1800), new MobEffectInstance(MobEffects.ABSORPTION, 1800, 4)));
    public static final Holder<Potion> PHANTOM_SPIRIT = register("phantom_spirit", new Potion("phantom_spirit", new MobEffectInstance(MobEffects.NIGHT_VISION, 2400), new MobEffectInstance(MobEffects.LEVITATION, 600), new MobEffectInstance(MobEffects.SLOW_FALLING, 1000)));
    public static final Holder<Potion> LIGHT_FOOTED = register("light_footed", new Potion("light_footed", new MobEffectInstance(MobEffects.SPEED, 1200, 1), new MobEffectInstance(MobEffects.JUMP_BOOST, 1200), new MobEffectInstance(MobEffects.SLOW_FALLING, 1200)));
    public static final Holder<Potion> MINER = register("miner", new Potion("miner", new MobEffectInstance(MobEffects.HASTE, 3600, 1), new MobEffectInstance(MobEffects.NIGHT_VISION, 4250), new MobEffectInstance(MobEffects.SPEED, 1200)));
    public static final Holder<Potion> ANTIDOTE_POT = register("actidote_pot", new Potion("antidote", new MobEffectInstance(BMEffects.ANTIDOTE, 1)));
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
