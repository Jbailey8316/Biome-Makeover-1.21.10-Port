package party.lemons.biomemakeover.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import party.lemons.biomemakeover.block.AltarBlock;
import party.lemons.biomemakeover.init.BMBlockEntities;
import party.lemons.biomemakeover.init.BMItems;
import party.lemons.biomemakeover.item.AltarCursing;
import party.lemons.biomemakeover.screen.AltarMenu;

import java.util.function.Consumer;

/** Released two-slot Altar inventory and 300-tick server processing contract. */
public final class AltarBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
    public static final int MAX_TIME = 300;
    private static final double PI = Math.PI;
    private static final double TAU = Math.PI * 2.0D;
    private static final RandomSource BOOK_RANDOM = RandomSource.create();
    private static Consumer<AltarBlockEntity> clientSoundStarter = altar -> {};

    private NonNullList<ItemStack> inventory = NonNullList.withSize(2, ItemStack.EMPTY);
    private int progress;
    private boolean clientWasActive;
    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) { return progress; }
        @Override public void set(int index, int value) { progress = value; }
        @Override public int getCount() { return 1; }
    };

    public int ticks;
    public float nextPageAngle;
    public float pageAngle;
    public float nextPageTurn;
    public float angleChange;
    public float nextPageTurningSpeed;
    public float pageTurningSpeed;
    public float currentAngle;
    public float lastAngle;
    public float nextAngle;

    public AltarBlockEntity(BlockPos pos, BlockState state) {
        super(BMBlockEntities.ALTAR, pos, state);
    }

    public static void setClientSoundStarter(Consumer<AltarBlockEntity> starter) {
        clientSoundStarter = starter == null ? altar -> {} : starter;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AltarBlockEntity altar) {
        altar.ticks++;
        if (level.isClientSide()) {
            altar.clientTick(state);
        } else {
            altar.serverTick(state);
        }
    }

    private void serverTick(BlockState state) {
        if (level == null) return;
        boolean working = canWork();
        if (!working) {
            if (progress != 0) {
                progress = 0;
                setChanged();
            }
            setActive(state, false);
            return;
        }

        setActive(state, true);
        progress++;
        setChanged();
        if (progress < MAX_TIME) {
            return;
        }

        ItemStack original = getItem(0);
        ItemStack result = AltarCursing.process(level, original, level.random);
        if (result.isEmpty()) {
            Block.popResource(level, worldPosition, original.copy());
            setItem(0, ItemStack.EMPTY);
        } else {
            setItem(0, result);
        }
        getItem(1).shrink(1);
        if (getItem(1).isEmpty()) setItem(1, ItemStack.EMPTY);
        progress = 0;
        setChanged();
        level.updateNeighbourForOutputSignal(worldPosition, state.getBlock());
    }

    private void setActive(BlockState state, boolean active) {
        if (level != null && state.getValue(AltarBlock.ACTIVE) != active) {
            level.setBlock(worldPosition, state.setValue(AltarBlock.ACTIVE, active), Block.UPDATE_ALL);
        }
    }

    private void clientTick(BlockState state) {
        boolean active = state.getValue(AltarBlock.ACTIVE);
        if (active && !clientWasActive) clientSoundStarter.accept(this);
        clientWasActive = active;
        updateBook(active);
    }

    private void updateBook(boolean active) {
        if (level == null) return;
        pageTurningSpeed = nextPageTurningSpeed;
        lastAngle = currentAngle;
        if (active) {
            nextAngle += 0.5F;
            nextPageTurningSpeed += 0.2F;
        } else {
            Player player = level.getNearestPlayer(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D, 3.0D, false);
            if (player != null) {
                double distanceX = player.getX() - (worldPosition.getX() + 0.5D);
                double distanceZ = player.getZ() - (worldPosition.getZ() + 0.5D);
                nextAngle = (float) Mth.atan2(distanceZ, distanceX);
                nextPageTurningSpeed += 0.1F;
                if (nextPageTurningSpeed < 0.5F || BOOK_RANDOM.nextInt(40) == 0) {
                    float old = nextPageTurn;
                    do nextPageTurn += BOOK_RANDOM.nextInt(4) - BOOK_RANDOM.nextInt(4); while (old == nextPageTurn);
                }
            } else {
                nextAngle += 0.02F;
                nextPageTurningSpeed -= 0.1F;
            }
        }

        while (currentAngle >= PI) currentAngle -= TAU;
        while (currentAngle < -PI) currentAngle += TAU;
        while (nextAngle >= PI) nextAngle -= TAU;
        while (nextAngle < -PI) nextAngle += TAU;
        float rotation;
        for (rotation = nextAngle - currentAngle; rotation >= PI; rotation -= TAU) {}
        while (rotation < -PI) rotation += TAU;
        currentAngle += rotation * 0.4F;
        nextPageTurningSpeed = Mth.clamp(nextPageTurningSpeed, 0, 1);
        // The released client animation advances this counter both in the
        // outer ticker and here; retain that cadence for the floating book.
        ticks++;
        pageAngle = nextPageAngle;
        float turn = Mth.clamp((nextPageTurn - nextPageAngle) * 0.4F, -0.2F, 0.2F);
        angleChange += (turn - angleChange) * 0.9F;
        nextPageAngle += angleChange;
    }

    public boolean canWork() {
        return AltarCursing.isValidTarget(inventory.get(0)) && inventory.get(1).is(BMItems.CURSE_FUEL);
    }

    public int getProgress() { return progress; }

    @Override public int[] getSlotsForFace(Direction direction) { return direction.getAxis() == Direction.Axis.Y ? new int[]{0} : new int[]{1}; }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        return slot == 0 ? AltarCursing.isValidTarget(stack) : slot == 1 && stack.is(BMItems.CURSE_FUEL);
    }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) { return true; }
    @Override public int getContainerSize() { return 2; }
    @Override protected NonNullList<ItemStack> getItems() { return inventory; }
    @Override protected void setItems(NonNullList<ItemStack> items) { inventory = items; }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, inventory);
        output.putInt("Progress", progress);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        inventory = NonNullList.withSize(2, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, inventory);
        progress = input.getIntOr("Progress", 0);
    }

    @Override protected Component getDefaultName() { return Component.translatable("block.biomemakeover.altar"); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory playerInventory) { return new AltarMenu(id, playerInventory, this, data); }
}
