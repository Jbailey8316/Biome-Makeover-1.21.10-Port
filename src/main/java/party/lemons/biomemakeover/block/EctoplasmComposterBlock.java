package party.lemons.biomemakeover.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import party.lemons.biomemakeover.init.BMAdvancements;

/** The released Ectoplasm Composter: a vanilla composter variant whose ready product is Soul Soil. */
public final class EctoplasmComposterBlock extends ComposterBlock {
    public EctoplasmComposterBlock(Properties properties) { super(properties); }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.getValue(LEVEL) == READY) {
            emptyFullComposter(level, pos, new ItemStack(Blocks.SOUL_SOIL));
            if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) BMAdvancements.ECTOPLASM_COMPOST.trigger(serverPlayer);
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }

    @Override
    public WorldlyContainer getContainer(BlockState state, LevelAccessor level, BlockPos pos) {
        return state.getValue(LEVEL) == READY
            ? new FullComposterContainer(level, pos, new ItemStack(Items.SOUL_SOIL))
            : super.getContainer(state, level, pos);
    }

    public static void emptyFullComposter(Level level, BlockPos pos, ItemStack stack) {
        if (!level.isClientSide()) {
            double x = pos.getX() + level.random.nextFloat() * 0.7D + 0.15D;
            double y = pos.getY() + level.random.nextFloat() * 0.7D + 0.66D;
            double z = pos.getZ() + level.random.nextFloat() * 0.7D + 0.15D;
            ItemEntity item = new ItemEntity(level, x, y, z, stack);
            item.setDefaultPickUpDelay();
            level.addFreshEntity(item);
        }
        level.setBlock(pos, Blocks.COMPOSTER.defaultBlockState(), Block.UPDATE_ALL);
        level.playSound(null, pos, SoundEvents.COMPOSTER_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    static final class FullComposterContainer extends SimpleContainer implements WorldlyContainer {
        private final LevelAccessor level;
        private final BlockPos pos;
        private final Item item;
        private boolean dirty;

        FullComposterContainer(LevelAccessor level, BlockPos pos, ItemStack stack) {
            super(stack);
            this.level = level;
            this.pos = pos;
            this.item = stack.getItem();
        }

        @Override public int getMaxStackSize() { return 1; }
        @Override public int[] getSlotsForFace(Direction side) { return side == Direction.DOWN ? new int[]{0} : new int[0]; }
        @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) { return false; }
        @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
            return !dirty && side == Direction.DOWN && stack.getItem() == item;
        }
        @Override public void setChanged() {
            dirty = true;
            level.setBlock(pos, Blocks.COMPOSTER.defaultBlockState(), Block.UPDATE_ALL);
            super.setChanged();
        }
    }
}
