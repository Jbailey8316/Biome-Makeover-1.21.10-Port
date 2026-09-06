package party.lemons.biomemakeover.crafting.witch;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import party.lemons.biomemakeover.crafting.witch.data.QuestCategories;
import party.lemons.biomemakeover.crafting.witch.data.reward.RewardTables;
import java.util.ArrayList;
import java.util.List;

public final class WitchQuestHandler {
    public static WitchQuest createQuest(RandomSource random){int count=weightedCount(random);List<QuestItem> selected=new ArrayList<>();int safety=count*2;while(selected.size()<count&&safety-->0){QuestCategory category=QuestCategories.choose(random);QuestItem item=category.getRequestedItemPool().get(random.nextInt(category.getRequestedItemPool().size()));if(!selected.contains(item))selected.add(item);}return new WitchQuest(random,selected);}
    public static ItemStack getRewardFor(WitchQuest quest,RandomSource random){return RewardTables.getReward(QuestRarity.getRarityFromPoints(quest.getPoints()),random);}
    private static int weightedCount(RandomSource random){int n=random.nextInt(21);return n<5?1:n<13?2:n<17?3:n<20?4:5;}
    private WitchQuestHandler(){}
}
