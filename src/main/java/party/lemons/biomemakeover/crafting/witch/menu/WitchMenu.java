package party.lemons.biomemakeover.crafting.witch.menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import party.lemons.biomemakeover.crafting.witch.*;
import party.lemons.biomemakeover.init.BMMenus;
import party.lemons.biomemakeover.init.BMAdvancements;

public final class WitchMenu extends AbstractContainerMenu {
    private final WitchQuestEntity witch;
    private final SimpleContainer output = new SimpleContainer(1);
    public WitchMenu(int id, Inventory inventory) { this(id, inventory, new SimpleWitch(inventory.player)); }
    public WitchMenu(int id, Inventory inventory, WitchQuestEntity witch) {
        super(BMMenus.WITCH, id); this.witch = witch;
        addSlot(new Slot(output, 0, 131, 36) { @Override public boolean mayPlace(ItemStack stack) { return false; } });
        addStandardInventorySlots(inventory, 6, 100);
    }
    public WitchQuestList getQuests() { return witch.getQuests(); }
    public void setQuests(WitchQuestList quests) { witch.setQuestsFromServer(quests); }
    public WitchQuestEntity getWitch() { return witch; }
    @Override public boolean stillValid(Player player) { return witch.getCurrentCustomer() == player && witch.canInteract(player); }
    @Override public boolean clickMenuButton(Player player, int id) { completeQuest(player, id); return true; }
    public void completeQuest(Player player, int id) {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer) || id < 0 || id >= getQuests().size()) return;
        WitchQuest quest = getQuests().get(id);
        if (!quest.hasItems(player.getInventory())) return;
        if (!output.getItem(0).isEmpty()) player.drop(output.removeItemNoUpdate(0), true);
        ItemStack reward = WitchQuestHandler.getRewardFor(quest, player.getRandom());
        BMAdvancements.WITCH_TRADE.trigger((net.minecraft.server.level.ServerPlayer) player);
        quest.consumeItems(player.getInventory()); getQuests().remove(quest); output.setItem(0, reward);
        witch.sendQuestUpdate((net.minecraft.server.level.ServerPlayer) player);
    }
    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
    @Override public void removed(Player player) {
        super.removed(player); witch.setCurrentCustomer(null);
        if (!output.getItem(0).isEmpty() && !player.level().isClientSide()) player.getInventory().placeItemBackInInventory(output.removeItemNoUpdate(0));
    }
    private static final class SimpleWitch implements WitchQuestEntity {
        private final Player player; private WitchQuestList quests = new WitchQuestList();
        SimpleWitch(Player player) { this.player = player; }
        public void setCurrentCustomer(Player p) {} public Player getCurrentCustomer() { return player; }
        public WitchQuestList getQuests() { return quests; } public void setQuestsFromServer(WitchQuestList q) { quests = q; }
        public net.minecraft.sounds.SoundEvent getYesSound() { return net.minecraft.sounds.SoundEvents.WITCH_CELEBRATE; }
        public boolean canInteract(Player p) { return p == player; } public net.minecraft.world.level.Level getWitchLevel() { return player.level(); }
    }
}
