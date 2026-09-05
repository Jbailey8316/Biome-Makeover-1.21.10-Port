package party.lemons.biomemakeover.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.worldgen.mansion.MansionFeature;

/** Trace-only provenance hook for dynamically transformed garden collision cells. */
@Mixin(WorldGenRegion.class)
public abstract class WorldGenRegionMixin {
    @Inject(method = "setBlock", at = @At("HEAD"))
    private void biomemakeover$traceMansionTargets(BlockPos pos, BlockState state, int flags, int recursionLeft,
                                                    CallbackInfoReturnable<Boolean> cir) {
        if (!Boolean.getBoolean("bm.mansion.trace")) return;
        MansionFeature.TargetTrace target = MansionFeature.targetTraceFor(pos);
        if (target == null) return;
        StackTraceElement caller = null;
        String classification = "other";
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            String name = element.getClassName();
            if (name.contains("WorldGenRegionMixin") || name.equals(Thread.class.getName())) continue;
            if (caller == null) caller = element;
            if (name.contains("StructureTemplate")) { classification = "StructureTemplate"; caller = element; break; }
            if (name.contains("MansionFeature")) { classification = "Mansion piece"; caller = element; break; }
            if (name.contains("TreeFeature") || name.contains("TrunkPlacer") || name.contains("FoliagePlacer")) { classification = "tree feature"; caller = element; break; }
            if (name.contains("VegetationFeature") || name.contains("PlacedFeature")) { classification = "vegetation/placed feature"; caller = element; }
            if (name.contains("Processor")) classification = "processor";
            if (name.contains("Reconcile") || name.contains("reconcile")) classification = "reconciliation";
        }
        WorldGenRegion self = (WorldGenRegion) (Object) this;
        BiomeMakeover.LOGGER.info("[BM_MANSION_TARGET_BLOCK_WRITE] worldPos={} localTarget={} mansionId={} pieceId={} template={} anchor={} rotation={} mirror={} oldState={} newState={} thread={} chunk={} generationStep={} stackCaller={} stackClassification={} flags={} recursionLeft={}",
            pos, target.local(), target.mansionOrigin(), target.pieceOrdinal(), target.template(), target.anchor(), target.rotation(), target.mirror(),
            self.getBlockState(pos), state, Thread.currentThread().getName(), new net.minecraft.world.level.ChunkPos(pos),
            "unavailable-from-setBlock-hook", caller == null ? "unknown" : caller.getClassName() + "." + caller.getMethodName() + ":" + caller.getLineNumber(),
            classification, flags, recursionLeft);
    }
}
