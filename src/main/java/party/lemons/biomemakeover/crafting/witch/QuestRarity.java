package party.lemons.biomemakeover.crafting.witch;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Rarity;
import java.util.Locale;

public enum QuestRarity {
    COMMON(Rarity.COMMON, 0), UNCOMMON(Rarity.UNCOMMON, 8), RARE(Rarity.RARE, 15), EPIC(Rarity.EPIC, 30);
    private final Rarity itemRarity; private final int requiredPoints;
    QuestRarity(Rarity rarity, int points) { itemRarity=rarity; requiredPoints=points; }
    public Component getTooltipText() { return Component.translatable("tooltip."+name().toLowerCase(Locale.ROOT)).withStyle(itemRarity.color()); }
    public static QuestRarity getRarityFromPoints(float points) { QuestRarity result=COMMON; for(QuestRarity r:values()) if(points>=r.requiredPoints) result=r; return result; }
}
