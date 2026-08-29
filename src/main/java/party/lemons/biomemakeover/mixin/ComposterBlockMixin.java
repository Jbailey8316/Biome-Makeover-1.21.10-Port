package party.lemons.biomemakeover.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import party.lemons.biomemakeover.init.BMBlocks;

@Mixin(ComposterBlock.class)
public abstract class ComposterBlockMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void biomemakeover$receiveWaterDrip(BlockState state, ServerLevel level, BlockPos pos,
                                                RandomSource random, CallbackInfo ci) {
        if (state.getValue(ComposterBlock.LEVEL) != ComposterBlock.READY) return;
        BlockPos tip = PointedDripstoneBlock.findStalactiteTipAboveCauldron(level, pos);
        if (tip != null && PointedDripstoneBlock.getCauldronFillFluidType(level, tip) == Fluids.WATER) {
            level.levelEvent(1500, pos, 1);
            level.setBlock(pos, BMBlocks.PEAT_COMPOSTER.defaultBlockState(), 3);
            ci.cancel();
        }
    }
}
