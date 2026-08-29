package party.lemons.biomemakeover.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Local replacement for final Taniwha DripstoneReceiver scheduling. */
@Mixin(PointedDripstoneBlock.class)
public abstract class PointedDripstoneBlockMixin {
    private static final float BIOMEMAKEOVER_WATER_TRANSFER_CHANCE = 0.17578125F;

    @Inject(method = "maybeTransferFluid", at = @At("RETURN"))
    private static void biomemakeover$scheduleFullComposter(BlockState state, ServerLevel level, BlockPos startPos,
                                                             float chance, CallbackInfo ci) {
        if (chance >= BIOMEMAKEOVER_WATER_TRANSFER_CHANCE
            || PointedDripstoneBlock.getCauldronFillFluidType(level, startPos) != Fluids.WATER) return;

        BlockPos tip = null;
        for (int distance = 0; distance <= 11; distance++) {
            BlockPos cursor = startPos.below(distance);
            BlockState cursorState = level.getBlockState(cursor);
            if (!cursorState.is(Blocks.POINTED_DRIPSTONE)
                || cursorState.getValue(PointedDripstoneBlock.TIP_DIRECTION) != Direction.DOWN) break;
            DripstoneThickness thickness = cursorState.getValue(PointedDripstoneBlock.THICKNESS);
            if (thickness == DripstoneThickness.TIP || thickness == DripstoneThickness.TIP_MERGE) {
                tip = cursor;
                break;
            }
        }
        if (tip == null) return;

        for (int distance = 1; distance <= 11; distance++) {
            BlockPos cursor = tip.below(distance);
            BlockState cursorState = level.getBlockState(cursor);
            if (cursorState.is(Blocks.COMPOSTER)
                && cursorState.getValue(ComposterBlock.LEVEL) == ComposterBlock.READY) {
                level.levelEvent(1504, tip, 0);
                level.scheduleTick(cursor, Blocks.COMPOSTER, 50 + distance);
                return;
            }
            if (!cursorState.isAir() && cursorState.isSolidRender()) return;
        }
    }
}
