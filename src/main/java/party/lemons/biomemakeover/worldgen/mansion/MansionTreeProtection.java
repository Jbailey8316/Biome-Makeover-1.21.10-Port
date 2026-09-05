package party.lemons.biomemakeover.worldgen.mansion;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.trunkplacers.GiantTrunkPlacer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import party.lemons.biomemakeover.BiomeMakeover;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/** Trace-gated Mythas safety boundary for trees rooted inside Mansion geometry. */
public final class MansionTreeProtection {
    private static final Map<WorldGenLevel, List<Footprint>> FOOTPRINTS =
        Collections.synchronizedMap(new WeakHashMap<>());

    private MansionTreeProtection() { }

    public static void register(WorldGenLevel level, String mansionId, List<BoundingBox> boxes) {
        synchronized (FOOTPRINTS) {
            List<Footprint> entries = FOOTPRINTS.computeIfAbsent(level, ignored -> new ArrayList<>());
            if (entries.stream().noneMatch(existing -> existing.id.equals(mansionId)))
                entries.add(new Footprint(mansionId, List.copyOf(boxes)));
        }
    }

    public static boolean rejects(WorldGenLevel level, BlockPos origin, TreeConfiguration config) {
        List<BlockPos> roots = new ArrayList<>();
        roots.add(origin);
        if (config.trunkPlacer instanceof GiantTrunkPlacer) {
            roots.add(origin.offset(1, 0, 0));
            roots.add(origin.offset(0, 0, 1));
            roots.add(origin.offset(1, 0, 1));
        }
        synchronized (FOOTPRINTS) {
            for (Footprint footprint : FOOTPRINTS.getOrDefault(level, List.of())) {
                for (BlockPos root : roots) for (BoundingBox box : footprint.boxes) {
                    if (root.getX() >= box.minX() && root.getX() <= box.maxX()
                        && root.getZ() >= box.minZ() && root.getZ() <= box.maxZ()) {
                        if (Boolean.getBoolean("bm.mansion.trace"))
                            BiomeMakeover.LOGGER.info("[BM_MANSION_TREE_REJECTED] mansionId={} treeOrigin={} checkedRootColumns={} intersectingMansionColumn={} featureType={} thread={}",
                                footprint.id, origin, roots, root, config.trunkPlacer.getClass().getSimpleName(),
                                Thread.currentThread().getName());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private record Footprint(String id, List<BoundingBox> boxes) { }
}
