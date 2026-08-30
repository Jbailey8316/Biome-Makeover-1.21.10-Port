package party.lemons.biomemakeover.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import party.lemons.biomemakeover.init.BMBlocks;
import party.lemons.biomemakeover.init.BMItems;

/** Adds the released Ectoplasm input to vanilla's private composter container. */
@Mixin(targets = "net.minecraft.world.level.block.ComposterBlock$InputContainer")
abstract class InputContainerMixin_EctoCompost extends SimpleContainer {
    @Shadow @Final private BlockState state;
    @Shadow @Final private LevelAccessor level;
    @Shadow @Final private BlockPos pos;
    @Shadow private boolean changed;

    private InputContainerMixin_EctoCompost() { super(1); }

    @Inject(method = "canPlaceItemThroughFace", at = @At("RETURN"), cancellable = true)
    private void biomemakeover$allowEctoplasm(int slot, ItemStack stack, Direction side, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && stack.is(BMItems.ECTOPLASM) && state.is(Blocks.COMPOSTER)
                && state.getValue(ComposterBlock.LEVEL) > 0) cir.setReturnValue(true);
    }

    @Inject(method = "setChanged", at = @At("HEAD"))
    private void biomemakeover$consumeEctoplasm(CallbackInfo ci) {
        ItemStack stack = getItem(0);
        if (!stack.is(BMItems.ECTOPLASM) || !state.is(Blocks.COMPOSTER) || state.getValue(ComposterBlock.LEVEL) <= 0) return;
        changed = true;
        level.levelEvent(1500, pos, 1);
        removeItemNoUpdate(0);
        level.setBlock(pos, BMBlocks.ECTOPLASM_COMPOSTER.defaultBlockState().setValue(ComposterBlock.LEVEL,
            state.getValue(ComposterBlock.LEVEL)), Block.UPDATE_ALL);
    }
}
