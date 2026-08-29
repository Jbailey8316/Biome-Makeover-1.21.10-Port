package party.lemons.biomemakeover.screen;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import party.lemons.biomemakeover.init.BMItems;
import party.lemons.biomemakeover.init.BMMenus;
import party.lemons.biomemakeover.item.AltarCursing;

/** Source-layout Altar menu: one target, one fuel, and no curse selection controls. */
public final class AltarMenu extends AbstractContainerMenu {
    private final Container inventory;
    private final ContainerData data;

    public AltarMenu(int id, Inventory playerInventory) {
        this(id, playerInventory, new SimpleContainer(2), new SimpleContainerData(1));
    }

    public AltarMenu(int id, Inventory playerInventory, Container inventory, ContainerData data) {
        super(BMMenus.ALTAR, id);
        checkContainerSize(inventory, 2);
        checkContainerDataCount(data, 1);
        this.inventory = inventory;
        this.data = data;
        addDataSlots(data);
        inventory.startOpen(playerInventory.player);

        addSlot(new TargetSlot(inventory, 0, 80, 15));
        addSlot(new FuelSlot(inventory, 1, 80, 54));
        addStandardInventorySlots(playerInventory, 8, 84);
    }

    public int getProgress() { return data.get(0); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return copy;
        ItemStack moving = slot.getItem();
        copy = moving.copy();
        if (index < 2) {
            if (!moveItemStackTo(moving, 2, 38, true)) return ItemStack.EMPTY;
        } else if (moving.is(BMItems.CURSE_FUEL)) {
            if (!moveItemStackTo(moving, 1, 2, true)) return ItemStack.EMPTY;
        } else {
            Slot target = slots.get(0);
            if (target.hasItem() || !target.mayPlace(moving)) return ItemStack.EMPTY;
            target.set(moving.split(1));
        }
        if (moving.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        if (moving.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, moving);
        return copy;
    }

    @Override public boolean stillValid(Player player) { return inventory.stillValid(player); }

    private static final class TargetSlot extends Slot {
        private TargetSlot(Container container, int index, int x, int y) { super(container, index, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return AltarCursing.isValidTarget(stack); }
        @Override public int getMaxStackSize() { return 1; }
    }

    private static final class FuelSlot extends Slot {
        private FuelSlot(Container container, int index, int x, int y) { super(container, index, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return stack.is(BMItems.CURSE_FUEL); }
    }
}
