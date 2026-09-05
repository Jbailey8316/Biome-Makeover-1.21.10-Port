package party.lemons.biomemakeover.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import party.lemons.biomemakeover.BiomeMakeover;

/** Trace-only provenance hook for the three known Mansion collision cells. */
@Mixin(WorldGenRegion.class)
public abstract class WorldGenRegionMixin {
    private static boolean target(BlockPos pos) {
        return (pos.getX() == 4694 && pos.getZ() == 316 && pos.getY() >= 65 && pos.getY() <= 67);
    }

    @Inject(method = "setBlock", at = @At("HEAD"))
    private void biomemakeover$traceMansionTargets(BlockPos pos, BlockState state, int flags, int recursionLeft,
                                                    CallbackInfoReturnable<Boolean> cir) {
        if (!Boolean.getBoolean("bm.mansion.trace") || !target(pos)) return;
        StackTraceElement caller = null;
        String classification = "unknown";
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            String name = element.getClassName();
            if (name.contains("WorldGenRegionMixin") || name.equals(Thread.class.getName())) continue;
            if (caller == null) caller = element;
            if (name.contains("StructureTemplate")) { classification = "StructureTemplate"; caller = element; break; }
            if (name.contains("TreeFeature") || name.contains("TrunkPlacer") || name.contains("FoliagePlacer")) { classification = "tree feature"; caller = element; break; }
            if (name.contains("VegetationFeature") || name.contains("PlacedFeature")) { classification = "vegetation feature"; caller = element; break; }
            if (name.contains("MansionFeature")) { classification = "MansionFeature"; caller = element; break; }
        }
        WorldGenRegion self = (WorldGenRegion) (Object) this;
        BiomeMakeover.LOGGER.info("[BM_MANSION_TARGET_BLOCK_WRITE] worldPos={} oldState={} newState={} thread={} chunk={} generationStep={} stackCaller={} stackClassification={} flags={} recursionLeft={}",
            pos, self.getBlockState(pos), state, Thread.currentThread().getName(), self.getCenter(),
            "unavailable-from-setBlock-hook", caller == null ? "unknown" : caller.getClassName() + "." + caller.getMethodName() + ":" + caller.getLineNumber(),
            classification, flags, recursionLeft);
    }
}
