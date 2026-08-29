package party.lemons.biomemakeover.init;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import party.lemons.biomemakeover.BiomeMakeover;

import java.util.Optional;

/** Canonical, reload-safe keys for the final-release BM curse registry. */
public final class BMEnchantments {
    public static final TagKey<Enchantment> ALTAR_CURSE_EXCLUDED = TagKey.create(Registries.ENCHANTMENT, BiomeMakeover.id("altar_curse_excluded"));
    public static final TagKey<Enchantment> ALTAR_CANT_UPGRADE = TagKey.create(Registries.ENCHANTMENT, BiomeMakeover.id("altar_cant_upgrade"));
    public static final ResourceKey<Enchantment> DECAY_CURSE = key("decay_curse");
    public static final ResourceKey<Enchantment> INSOMNIA_CURSE = key("insomnia_curse");
    public static final ResourceKey<Enchantment> CONDUCTIVITY_CURSE = key("conductivity_curse");
    public static final ResourceKey<Enchantment> ENFEEBLEMENT_CURSE = key("enfeeblement_curse");
    public static final ResourceKey<Enchantment> DEPTH_CURSE = key("depth_curse");
    public static final ResourceKey<Enchantment> FLAMMABILITY_CURSE = key("flammability_curse");
    public static final ResourceKey<Enchantment> SUFFOCATION_CURSE = key("suffocation_curse");
    public static final ResourceKey<Enchantment> UNWIELDINESS_CURSE = key("unwieldiness_curse");
    public static final ResourceKey<Enchantment> INACCURACY_CURSE = key("inaccuracy_curse");
    public static final ResourceKey<Enchantment> BUCKLING_CURSE = key("buckling_curse");

    private BMEnchantments() {}

    private static ResourceKey<Enchantment> key(String path) {
        return ResourceKey.create(Registries.ENCHANTMENT, BiomeMakeover.id(path));
    }

    public static Optional<Holder.Reference<Enchantment>> holder(RegistryAccess access, ResourceKey<Enchantment> key) {
        return access.lookup(Registries.ENCHANTMENT).flatMap(registry -> registry.get(key));
    }

    public static int level(ItemStack stack, RegistryAccess access, ResourceKey<Enchantment> key) {
        return holder(access, key).map(enchantment -> EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack)).orElse(0);
    }

    public static int equippedLevel(LivingEntity entity, EquipmentSlot slot, ResourceKey<Enchantment> key) {
        return level(entity.getItemBySlot(slot), entity.registryAccess(), key);
    }
}
