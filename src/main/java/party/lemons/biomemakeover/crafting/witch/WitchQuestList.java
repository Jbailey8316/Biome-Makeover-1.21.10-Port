package party.lemons.biomemakeover.crafting.witch;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import party.lemons.biomemakeover.crafting.witch.data.QuestCategories;
import java.util.ArrayList;

public final class WitchQuestList extends ArrayList<WitchQuest> {
    public WitchQuestList() {}
    public WitchQuestList(CompoundTag tag){ListTag list=tag.getListOrEmpty("Quests");for(int i=0;i<list.size();i++)add(new WitchQuest(list.getCompoundOrEmpty(i)));}
    public void populate(RandomSource random){clear();if(!QuestCategories.hasQuests())return;for(int i=0;i<3;i++)add(WitchQuestHandler.createQuest(random));}
    public CompoundTag toTag(){CompoundTag tag=new CompoundTag();ListTag list=new ListTag();for(WitchQuest q:this)list.add(q.toTag());tag.put("Quests",list);return tag;}
}
