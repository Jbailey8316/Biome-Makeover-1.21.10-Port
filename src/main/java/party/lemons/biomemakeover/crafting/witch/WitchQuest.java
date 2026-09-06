package party.lemons.biomemakeover.crafting.witch;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import java.util.List;

public final class WitchQuest {
    private final ItemStack[] requiredItems; private final float rarityPoints;
    public WitchQuest(RandomSource random,List<QuestItem> items) { requiredItems=items.stream().map(i->i.createStack(random)).toArray(ItemStack[]::new); rarityPoints=items.stream().mapToDouble(QuestItem::points).sum()==0?0:(float)items.stream().mapToDouble(QuestItem::points).sum(); }
    public WitchQuest(CompoundTag tag) { rarityPoints=tag.getFloatOr("Points",0); ListTag list=tag.getListOrEmpty("Items"); requiredItems=new ItemStack[list.size()]; for(int i=0;i<list.size();i++){CompoundTag item=list.getCompoundOrEmpty(i); Item resolved=BuiltInRegistries.ITEM.getValue(ResourceLocation.parse(item.getStringOr("Item","minecraft:air"))); requiredItems[i]=new ItemStack(resolved,item.getIntOr("Count",1));} }
    public ItemStack[] getRequiredItems(){return requiredItems;}
    public float getPoints(){return rarityPoints;}
    public boolean hasItems(Container inventory){for(ItemStack stack:requiredItems)if(inventory.countItem(stack.getItem())<stack.getCount())return false;return true;}
    public void consumeItems(Inventory inventory){for(ItemStack required:requiredItems){int left=required.getCount();for(int i=0;i<inventory.getContainerSize()&&left>0;i++){ItemStack held=inventory.getItem(i);if(held.is(required.getItem())){int n=Math.min(left,held.getCount());held.shrink(n);left-=n;}}}}
    public CompoundTag toTag(){CompoundTag tag=new CompoundTag();tag.putFloat("Points",rarityPoints);ListTag list=new ListTag();for(ItemStack stack:requiredItems){CompoundTag item=new CompoundTag();item.putString("Item",BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());item.putInt("Count",stack.getCount());list.add(item);}tag.put("Items",list);return tag;}
}
