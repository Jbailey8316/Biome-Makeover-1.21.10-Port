package party.lemons.biomemakeover.item;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import party.lemons.biomemakeover.init.BMEnchantments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Modern item-component translation of the released Altar cursing helper. */
public final class AltarCursing {
    public static final String MARKER = "BMCursed";
    public static final int REPAIR_COST = 39;
    public static final int RANDOM_ATTEMPTS = 100;

    private AltarCursing() {}

    public static boolean isValidTarget(ItemStack stack) {
        if (stack.isEmpty() || stack.is(Items.ENCHANTED_BOOK)) return false;
        if (stack.is(Items.BOOK)) return true;
        if (hasMarker(stack)) return false;

        ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (enchantments.isEmpty()) return false;
        for (Holder<Enchantment> enchantment : enchantments.keySet()) {
            if (isUpgradeable(enchantment)) return true;
        }
        return false;
    }

    private static boolean isUpgradeable(Holder<Enchantment> enchantment) {
        return !enchantment.is(BMEnchantments.ALTAR_CANT_UPGRADE)
            && !enchantment.is(EnchantmentTags.CURSE)
            && enchantment.value().definition().maxLevel() > 1;
    }

    public static boolean hasMarker(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return data.copyTag().getBooleanOr(MARKER, false);
    }

    private static void setMarker(ItemStack stack) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean(MARKER, true));
    }

    public static ItemStack process(Level level, ItemStack input, RandomSource random) {
        if (!isValidTarget(input)) return ItemStack.EMPTY;
        Registry<Enchantment> registry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

        if (input.is(Items.BOOK)) {
            Holder.Reference<Enchantment> curse = randomCurse(registry, random);
            if (curse == null) return ItemStack.EMPTY;
            ItemStack result = new ItemStack(Items.ENCHANTED_BOOK);
            ItemEnchantments.Mutable stored = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            stored.set(curse, 1);
            result.set(DataComponents.STORED_ENCHANTMENTS, stored.toImmutable());
            return result;
        }

        ItemStack result = input.copyWithCount(1);
        ItemEnchantments existing = result.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        List<Holder<Enchantment>> upgradeable = existing.keySet().stream().filter(AltarCursing::isUpgradeable).toList();
        if (upgradeable.isEmpty()) return ItemStack.EMPTY;

        Holder<Enchantment> upgraded = upgradeable.get(random.nextInt(upgradeable.size()));
        Holder.Reference<Enchantment> curse = compatibleCurse(registry, existing, result, random);
        if (curse == null) return ItemStack.EMPTY;

        ItemEnchantments.Mutable output = new ItemEnchantments.Mutable(existing);
        output.set(upgraded, existing.getLevel(upgraded) + 1);
        output.set(curse, randomCurseLevel(curse, random));
        result.set(DataComponents.ENCHANTMENTS, output.toImmutable());
        setMarker(result);
        result.set(DataComponents.REPAIR_COST, REPAIR_COST);
        return result;
    }

    private static Holder.Reference<Enchantment> compatibleCurse(Registry<Enchantment> registry,
                                                                  ItemEnchantments existing,
                                                                  ItemStack target,
                                                                  RandomSource random) {
        Holder.Reference<Enchantment> curse = randomCurse(registry, random);
        int attempts = 0;
        while (curse != null && !isCompatibleAbsentCurse(curse, existing, target)) {
            curse = randomCurse(registry, random);
            attempts++;
            if (attempts >= RANDOM_ATTEMPTS) {
                curse = null;
                break;
            }
        }
        if (curse != null) return curse;

        // Released fallback shuffled the registry with a random comparator and
        // retained the final compatible entry rather than stopping at the first.
        List<Holder.Reference<Enchantment>> shuffled = new ArrayList<>(registry.listElements().toList());
        Collections.shuffle(shuffled, new java.util.Random(random.nextLong()));
        for (Holder.Reference<Enchantment> candidate : shuffled) {
            if (isCompatibleAbsentCurse(candidate, existing, target)) curse = candidate;
        }
        return curse;
    }

    private static Holder.Reference<Enchantment> randomCurse(Registry<Enchantment> registry, RandomSource random) {
        List<Holder.Reference<Enchantment>> curses = registry.listElements()
            .filter(enchantment -> enchantment.is(EnchantmentTags.CURSE))
            .filter(enchantment -> !enchantment.is(BMEnchantments.ALTAR_CURSE_EXCLUDED))
            .toList();
        return curses.isEmpty() ? null : curses.get(random.nextInt(curses.size()));
    }

    private static boolean isCompatibleAbsentCurse(Holder<Enchantment> curse, ItemEnchantments existing, ItemStack target) {
        return curse.is(EnchantmentTags.CURSE)
            && !curse.is(BMEnchantments.ALTAR_CURSE_EXCLUDED)
            && existing.getLevel(curse) == 0
            && curse.value().definition().supportedItems().contains(target.getItemHolder());
    }

    public static int randomCurseLevel(Holder<Enchantment> curse, RandomSource random) {
        int maximum = curse.value().definition().maxLevel();
        return maximum == 1 ? 1 : 1 + random.nextInt(maximum - 1);
    }
}
