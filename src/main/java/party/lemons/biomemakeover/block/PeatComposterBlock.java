package party.lemons.biomemakeover.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import party.lemons.biomemakeover.init.BMAdvancements;
import party.lemons.biomemakeover.init.BMBlocks;

/** Ready-only composter state produced by water dripping into a full vanilla Composter. */
public final class PeatComposterBlock extends Block implements WorldlyContainerHolder {
    public PeatComposterBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        empty(level, pos);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            BMAdvancements.PEAT_COMPOST.trigger(serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }

    @Override protected boolean hasAnalogOutputSignal(BlockState state) { return true; }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return 9;
    }

    @Override
    public WorldlyContainer getContainer(BlockState state, LevelAccessor level, BlockPos pos) {
        return new PeatOutputContainer(level, pos);
    }

    private static void empty(Level level, BlockPos pos) {
        if (!level.isClientSide()) {
            double x = pos.getX() + level.random.nextFloat() * 0.7D + 0.15D;
            double y = pos.getY() + level.random.nextFloat() * 0.7D + 0.66D;
            double z = pos.getZ() + level.random.nextFloat() * 0.7D + 0.15D;
            ItemEntity item = new ItemEntity(level, x, y, z, new ItemStack(BMBlocks.PEAT));
            item.setDefaultPickUpDelay();
            level.addFreshEntity(item);
        }
        level.setBlock(pos, Blocks.COMPOSTER.defaultBlockState(), Block.UPDATE_ALL);
        level.playSound(null, pos, SoundEvents.COMPOSTER_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private static final class PeatOutputContainer extends SimpleContainer implements WorldlyContainer {
        private final LevelAccessor level;
        private final BlockPos pos;
        private boolean extracted;

        private PeatOutputContainer(LevelAccessor level, BlockPos pos) {
            super(new ItemStack(BMBlocks.PEAT));
            this.level = level;
            this.pos = pos;
        }

        @Override public int getMaxStackSize() { return 1; }
        @Override public int[] getSlotsForFace(Direction side) { return side == Direction.DOWN ? new int[]{0} : new int[0]; }
        @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) { return false; }
        @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
            return !extracted && side == Direction.DOWN && stack.is(BMBlocks.PEAT.asItem());
        }

        @Override
        public void setChanged() {
            extracted = true;
            level.setBlock(pos, Blocks.COMPOSTER.defaultBlockState(), Block.UPDATE_ALL);
            super.setChanged();
        }
    }
}
