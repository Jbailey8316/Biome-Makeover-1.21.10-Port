package party.lemons.biomemakeover.crafting.witch.data;

import net.minecraft.util.RandomSource;
import party.lemons.biomemakeover.crafting.witch.QuestCategory;
import java.util.ArrayList;
import java.util.List;

public final class QuestCategories {
    private static final List<QuestCategory> CATEGORIES=new ArrayList<>();
    public static void clearCategories(){CATEGORIES.clear();}
    public static void addCategory(QuestCategory category){if(!category.isEmpty())CATEGORIES.add(category);}
    public static QuestCategory choose(RandomSource random){int total=CATEGORIES.stream().mapToInt(QuestCategory::getWeight).sum();int n=random.nextInt(Math.max(1,total));for(QuestCategory c:CATEGORIES)if((n-=c.getWeight())<0)return c;return CATEGORIES.get(0);}
    public static boolean hasQuests(){return !CATEGORIES.isEmpty();}
    private QuestCategories(){}
}
