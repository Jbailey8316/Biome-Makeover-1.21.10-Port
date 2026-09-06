package party.lemons.biomemakeover.crafting.witch;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record QuestItem(Item item, float points, int maxCount) {
    public ItemStack createStack(RandomSource random) {
        return new ItemStack(item, maxCount == 1 ? 1 : 1 + random.nextInt(maxCount - 1));
    }
}
