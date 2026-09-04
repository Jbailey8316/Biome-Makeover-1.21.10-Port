package party.lemons.biomemakeover.worldgen.mansion;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import party.lemons.biomemakeover.block.IvyBlock;
import party.lemons.biomemakeover.entity.OwlEntity;
import party.lemons.biomemakeover.util.extension.Stuntable;
import party.lemons.biomemakeover.init.BMEntities;
import net.minecraft.world.level.ChunkPos;
import party.lemons.biomemakeover.init.BMStructures;
import party.lemons.biomemakeover.init.BMBlocks;
import party.lemons.biomemakeover.BiomeMakeover;

import java.util.Optional;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Consumer;
import party.lemons.biomemakeover.worldgen.mansion.room.MansionRoom;
import party.lemons.biomemakeover.worldgen.mansion.RoomType;

/**
 * Foundation for the released custom Mansion structure. Physical layout and
 * templates are intentionally activated by later 11A stages.
 */
public final class MansionFeature extends Structure {
    private static final boolean ARCHAEology_TRACE = false;
    /** Native 1.21.10 Trial-Chamber liquid semantics are the production default. */
    private static final boolean VANILLA_LIQUID_PARITY = true;
    private static final Set<String> TRIAL_LIQUID_LOGGED = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private static final CopyOnWriteArrayList<DelayedFluidTrace> DELAYED_FLUID_TRACES = new CopyOnWriteArrayList<>();
    private static final ThreadLocal<BlockPos> LAYOUT_ORIGIN = new ThreadLocal<>();
    private static final ThreadLocal<List<Piece>> LAYOUT_PIECES = new ThreadLocal<>();
    private static final ThreadLocal<Integer> NEXT_PIECE_ORDINAL = new ThreadLocal<>();
    private static final Map<String, Integer> EXPECTED_PIECES = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Map<String, Set<String>> EXPECTED_ORDINALS = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Map<String, Set<String>> EXPECTED_PLACEMENTS = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Map<String, Set<String>> PLACED_PLACEMENTS = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Map<String, Integer> EXPECTED_UNION_SIZES = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Map<String, Integer> EXPECTED_BOSS_PIECES = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Set<String> RUNTIME_REGISTERED = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private static final Set<String> CROP_PHASE_LOGGED = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private static final Set<String> CROP_DISAPPEAR_LOGGED = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private static final Set<String> LATE_SUMMARY_LOGGED = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private static final Map<String, LateFinalization> LATE_FINALIZATIONS = new java.util.concurrent.ConcurrentHashMap<>();
    private static final CopyOnWriteArrayList<Piece.BossBoundaryTrace> BOSS_BOUNDARY_TRACES = new CopyOnWriteArrayList<>();
    private static final Map<String, Map<BlockPos, BlockState>> CROP_TARGETS = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Set<String> CROP_EXPECTED_MANSIONS = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private static final Map<String, BossGeometry> BOSS_GEOMETRIES = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Map<String, MarkerFluidStats> MARKER_FLUID_STATS = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Map<String, Set<BlockPos>> BOSS_C8_WATER_CELLS = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Map<String, Map<BlockPos, net.minecraft.world.level.material.FluidState>> BOSS_C8_WATER_STATES = new java.util.concurrent.ConcurrentHashMap<>();

    private static final class MarkerFluidStats {
        int markerCount, fluidBearingBefore, sourceBearingBefore, corrected, fluidBearingAfter, sourceBearingAfter;
        int emitted;
    }

    private record BossGeometry(Map<BlockPos, BlockState> serialized, Set<BlockPos> explicitAir, Set<BlockPos> solid,
                                Set<BlockPos> authoredWater, Set<BlockPos> otherSerialized, BoundingBox pieceBounds,
                                Rotation rotation, Mirror mirror, BlockPos templateOrigin) {
        private BossGeometry {
            serialized = Map.copyOf(serialized); explicitAir = Set.copyOf(explicitAir); solid = Set.copyOf(solid);
            authoredWater = Set.copyOf(authoredWater); otherSerialized = Set.copyOf(otherSerialized);
        }
    }

    private static final class LayoutMetadata {
        final String key, signature; final BlockPos origin; final Set<String> ordinals, placements; final int unionSize, bossPieces;
        LayoutMetadata(String key, String signature, BlockPos origin, Set<String> ordinals, Set<String> placements, int unionSize, int bossPieces) {
            this.key = key; this.signature = signature; this.origin = origin; this.ordinals = Set.copyOf(ordinals); this.placements = Set.copyOf(placements); this.unionSize = unionSize; this.bossPieces = bossPieces;
        }
    }
    private static final Set<String> COVERAGE_MISMATCH_LOGGED = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private static final Map<String, Set<String>> PLACED_PIECES = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Set<String> EXECUTED_MANSIONS = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private static final CopyOnWriteArrayList<EnvelopePiece> DUNGEON_ENVELOPE = new CopyOnWriteArrayList<>();
    private static volatile boolean delayedFluidTraceInstalled;

    public static void enableDelayedFluidTrace() {
        if (!delayedFluidTraceInstalled) {
            synchronized (MansionFeature.class) {
                if (!delayedFluidTraceInstalled) {
                    ServerTickEvents.END_WORLD_TICK.register(MansionFeature::tickDelayedFluidTraces);
                    delayedFluidTraceInstalled = true;
                }
            }
        }
    }

    private static void tickDelayedFluidTraces(ServerLevel level) {
        boolean tracing = Boolean.getBoolean("bm.mansion.trace");
        for (DelayedFluidTrace entry : DELAYED_FLUID_TRACES) {
            if (entry.level != level) continue;
            String mansionId = entry.mansionId();
            if (isFinalPlacementComplete(mansionId)
                && EXECUTED_MANSIONS.add(mansionId)) {
                ReconcileResult result = reconcileCompletedDungeon(level, entry.order, entry.mansionOrigin, entry.layoutSignature);
                if (result.executed()) {
                    int registeredCropTargets = cropTargetCount(level, entry.mansionOrigin, entry.layoutSignature);
                    boolean cropExpected = CROP_EXPECTED_MANSIONS.contains(mansionId);
                    boolean cropReady = !cropExpected || registeredCropTargets > 0;
                    if (tracing && cropExpected && !cropReady)
                        BiomeMakeover.LOGGER.warn("[BM_CROP_TARGET_REGISTRATION_MISSING] mansionId={} registeredCropTargets={} -- C7/C8 crop mutation suppressed", mansionId, registeredCropTargets);
                    if (tracing) BiomeMakeover.LOGGER.info("[BM_CROP_FINALIZATION_READY] mansionId={} placedPlacementCount={} expectedPlacementCount={} registeredCropTargets={} ready={}",
                        mansionId, PLACED_PLACEMENTS.getOrDefault(mansionId, Set.of()).size(), EXPECTED_PLACEMENTS.getOrDefault(mansionId, Set.of()).size(), registeredCropTargets, cropReady);
                    LateFinalization late = createLateFinalization(level, entry.mansionOrigin, entry.layoutSignature, cropReady);
                    LATE_FINALIZATIONS.putIfAbsent(mansionId, late);
                    int bossCleared = reconcileBossRoomFinalAir(level, entry.mansionOrigin, entry.layoutSignature);
                    if (tracing) BiomeMakeover.LOGGER.info("[BM_BOSS_ROOM_FINAL_RECONCILE] mansionId={} explicitAirCleared={}", entry.mansionId(), bossCleared);
                }
                if (tracing && result.executed()) {
                    MarkerFluidStats markerStats = MARKER_FLUID_STATS.get(entry.mansionId());
                    if (markerStats != null) BiomeMakeover.LOGGER.info("[BM_DATA_MARKER_FLUID_SUMMARY] mansionId={} markerCount={} fluidBearingBefore={} sourceBearingBefore={} corrected={} fluidBearingAfter={} sourceBearingAfter={}",
                        entry.mansionId(), markerStats.markerCount, markerStats.fluidBearingBefore, markerStats.sourceBearingBefore, markerStats.corrected, markerStats.fluidBearingAfter, markerStats.sourceBearingAfter);
                    BiomeMakeover.LOGGER.info("[BM_RECONCILE_LIFECYCLE] event=READY mansionId={} pieceCount={} unionPositions={}", entry.mansionId(), countMansionPieces(level, entry.mansionOrigin, entry.layoutSignature), unionSize(level, entry.mansionOrigin, entry.layoutSignature));
                    BiomeMakeover.LOGGER.info("[BM_RECONCILE_LIFECYCLE] event=EXECUTE_BEGIN mansionId={} unionPositions={}", entry.mansionId(), unionSize(level, entry.mansionOrigin, entry.layoutSignature));
                    BiomeMakeover.LOGGER.info("[BM_RECONCILE_LIFECYCLE] event=EXECUTE_END mansionId={} correctedAir={} correctedWaterlogged={} authoredWetPreserved={}",
                        entry.mansionId(), result.correctedAir(), result.correctedWaterlogged(), result.authoredWetPreserved());
                    BiomeMakeover.LOGGER.info("[BM_DUNGEON_RECONCILE] phase=R0 mansionId={} explicitDryWater={} authoredFalseNowWaterlogged={} correctedAir={} correctedWaterlogged={} authoredWetPreserved={}",
                        entry.mansionId(), result.explicitDryWater(), result.authoredFalseNowWaterlogged(), result.correctedAir(), result.correctedWaterlogged(), result.authoredWetPreserved());
                    if (tracing) analyzeHydraulicGeometry(level, entry.mansionOrigin, entry.layoutSignature);
                    entry.snapshot(level, "D0");
                    BiomeMakeover.LOGGER.info("[BM_RECONCILE_LIFECYCLE] event=REMOVE mansionId={}", entry.mansionId());
                    snapshotBossBoundary(level, entry.mansionId(), "READY");
                    emitLatePhase(level, entry.mansionOrigin, entry.layoutSignature, "C5");
                    emitLatePhase(level, entry.mansionOrigin, entry.layoutSignature, "C6");
                }
            }
            entry.age++;
            String phase = switch (entry.age) { case 1 -> "D1"; case 5 -> "D5"; case 20 -> "D20"; case 100 -> "D100"; default -> null; };
            if (phase != null) entry.snapshot(level, phase);
            if (entry.age >= 100 || (!tracing && EXECUTED_MANSIONS.contains(entry.mansionId()))) DELAYED_FLUID_TRACES.remove(entry);
        }
        for (LateFinalization late : LATE_FINALIZATIONS.values()) if (late.level == level) {
            if (!late.ready) { if (late.age++ >= 0) LATE_FINALIZATIONS.remove(late.id, late); continue; }
            if (late.age < 0) { late.age++; continue; }
            late.age++;
            if (late.age == 1) {
                int boss = 0, crops = 0;
                for (BlockPos p : late.bossAir) if (!level.getFluidState(p).isEmpty()) { level.setBlock(p, Blocks.AIR.defaultBlockState(), 2); boss++; }
                for (var e : late.crops.entrySet()) if (!level.getBlockState(e.getKey()).equals(e.getValue())) { level.setBlock(e.getKey(), e.getValue(), 2); crops++; }
                if (tracing) {
                    BiomeMakeover.LOGGER.info("[BM_BOSS_ROOM_FINAL_RECONCILE] mansionId={} phase=C7 explicitAirCleared={} cropStatesRestored={}", late.id, boss, crops);
                    emitLatePhase(level, late.origin, late.signature, "C7");
                    emitCropFinalizationResult(level, late, crops, "C7");
                    late.retention(level, "C7");
                }
            }
            if (late.age == 2 && tracing) {
                late.tickBossBoundary(level);
                emitLatePhase(level, late.origin, late.signature, "C8");
                emitCropFinalizationResult(level, late, 0, "C8");
                late.retention(level, "C8");
            }
            late.tickBossBoundary(level);
            long ageTicks = late.readyTick < 0 ? -1 : level.getGameTime() - late.readyTick;
            if (ageTicks >= 400) late.retention(level, "D20S");
            if (ageTicks >= 900) { late.retention(level, "D45S"); LATE_FINALIZATIONS.remove(late.id, late); BOSS_C8_WATER_CELLS.remove(late.id); BOSS_C8_WATER_STATES.remove(late.id); MARKER_FLUID_STATS.remove(late.id); }
        }
    }

    private static void snapshotBossBoundary(ServerLevel level, String mansionId, String phase) {
        for (Piece.BossBoundaryTrace trace : BOSS_BOUNDARY_TRACES) {
            if (trace.level == level && trace.mansionId.equals(mansionId)) {
                trace.snapshot(phase);
                if ("READY".equals(phase)) {
                    trace.ready = true;
                    trace.readyTick = level.getGameTime();
                    LateFinalization late = LATE_FINALIZATIONS.get(mansionId);
                    if (late != null) late.readyTick = trace.readyTick;
                }
            }
        }
    }


    private static boolean isFinalPlacementComplete(String mansionId) {
        Set<String> expectedPlacements = EXPECTED_PLACEMENTS.get(mansionId);
        Set<String> expectedOrdinals = EXPECTED_ORDINALS.get(mansionId);
        if (expectedPlacements == null || expectedOrdinals == null) return false;
        Set<String> placedPlacements = PLACED_PLACEMENTS.getOrDefault(mansionId, Set.of());
        Set<String> placedPieces = PLACED_PIECES.getOrDefault(mansionId, Set.of());
        return placedPlacements.size() == expectedPlacements.size()
            && placedPlacements.equals(expectedPlacements)
            && placedPieces.size() == expectedOrdinals.size()
            && placedPieces.containsAll(expectedOrdinals);
    }

    /**
     * Restores only the final serialized dry state of the dungeon union.  The
     * records are captured from transformed template palettes, so omitted
     * terrain and surrounding aquifer cells are never touched.
     */
    private record ReconcileResult(boolean executed, int correctedAir, int correctedWaterlogged, int authoredWetPreserved,
                                   int explicitDryWater, int authoredFalseNowWaterlogged) {}

    private static int unionSize(ServerLevel level, BlockPos mansionOrigin, String signature) {
        String key = level.dimension().location() + ":" + mansionOrigin + ":" + signature;
        if (EXPECTED_UNION_SIZES.containsKey(key)) return EXPECTED_UNION_SIZES.get(key);
        Set<BlockPos> positions = new java.util.HashSet<>();
        for (DelayedFluidTrace candidate : DELAYED_FLUID_TRACES) if (candidate.level == level && candidate.mansionOrigin.equals(mansionOrigin) && candidate.layoutSignature.equals(signature)) positions.addAll(candidate.authoredStates.keySet());
        return positions.size();
    }

    private static int countMansionPieces(ServerLevel level, BlockPos mansionOrigin, String signature) {
        String key = level.dimension().location() + ":" + mansionOrigin + ":" + signature;
        if (EXPECTED_PIECES.containsKey(key)) return EXPECTED_PIECES.get(key);
        int count = 0;
        for (DelayedFluidTrace candidate : DELAYED_FLUID_TRACES)
            if (candidate.level == level && candidate.mansionOrigin.equals(mansionOrigin) && candidate.layoutSignature.equals(signature)) count++;
        return count;
    }

    /** Read-only, one-shot connectivity audit over the completed structural envelope. */
    private static void analyzeHydraulicGeometry(ServerLevel level, BlockPos mansionOrigin, String signature) {
        Map<BlockPos, BlockState> union = new HashMap<>();
        for (DelayedFluidTrace candidate : DELAYED_FLUID_TRACES)
            if (candidate.level == level && candidate.mansionOrigin.equals(mansionOrigin) && candidate.layoutSignature.equals(signature)) union.putAll(candidate.authoredStates);
        if (union.isEmpty()) return;
        int minX = union.keySet().stream().mapToInt(BlockPos::getX).min().orElse(0) - 1;
        int maxX = union.keySet().stream().mapToInt(BlockPos::getX).max().orElse(0) + 1;
        int minY = union.keySet().stream().mapToInt(BlockPos::getY).min().orElse(0) - 1;
        int maxY = union.keySet().stream().mapToInt(BlockPos::getY).max().orElse(0) + 1;
        int minZ = union.keySet().stream().mapToInt(BlockPos::getZ).min().orElse(0) - 1;
        int maxZ = union.keySet().stream().mapToInt(BlockPos::getZ).max().orElse(0) + 1;
        long volume = (long)(maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
        if (volume > 600_000L) {
            BiomeMakeover.LOGGER.info("[BM_HYDRAULIC_SUMMARY] mansionId={} analysisCells=0 structuralInteriorCells=0 exteriorConnectedCells=0 interiorExteriorConnectedCells=0 leakFaces=0 sourceWaterLeakFaces=0 flowingWaterLeakFaces=0 airLeakFaces=0 waterloggedLeakFaces=0 dungeonLeakFaces=0 stairLeakFaces=0 bossLeakFaces=0 skippedVolume={}", mansionId(level, mansionOrigin, signature), volume);
            return;
        }
        Set<BlockPos> interior = new java.util.HashSet<>();
        for (var entry : union.entrySet()) if (entry.getValue().isAir()) interior.add(entry.getKey());
        Set<BlockPos> exterior = new java.util.HashSet<>();
        java.util.ArrayDeque<BlockPos> queue = new java.util.ArrayDeque<>();
        for (int x = minX; x <= maxX; x++) for (int y = minY; y <= maxY; y++) for (int z = minZ; z <= maxZ; z++) {
            if (x != minX && x != maxX && y != minY && y != maxY && z != minZ && z != maxZ) continue;
            BlockPos p = new BlockPos(x, y, z);
            if (isHydraulicPassable(level, p) && exterior.add(p)) queue.add(p);
        }
        while (!queue.isEmpty()) {
            BlockPos p = queue.remove();
            for (Direction d : Direction.values()) {
                BlockPos n = p.relative(d);
                if (n.getX() < minX || n.getX() > maxX || n.getY() < minY || n.getY() > maxY || n.getZ() < minZ || n.getZ() > maxZ) continue;
                if (isHydraulicPassable(level, n) && exterior.add(n)) queue.add(n);
            }
        }
        int leak = 0, sourceLeaks = 0, flowingLeaks = 0, airLeaks = 0, wetLeaks = 0, dungeonLeaks = 0, stairLeaks = 0, bossLeaks = 0;
        int explicitDryWater = 0, authoredWetWater = 0, omittedInteriorWater = 0, exteriorWater = 0, unknownWater = 0;
        for (BlockPos p : union.keySet()) {
            BlockState authored = union.get(p);
            var fluid = level.getFluidState(p);
            if (authored.isAir() && !fluid.isEmpty()) { explicitDryWater++; if (exterior.contains(p)) exteriorWater++; else omittedInteriorWater++; }
            else if (authored.hasProperty(BlockStateProperties.WATERLOGGED) && authored.getValue(BlockStateProperties.WATERLOGGED)) authoredWetWater++;
            if (!interior.contains(p)) continue;
            for (Direction d : Direction.values()) {
                BlockPos n = p.relative(d);
                if (!exterior.contains(n)) continue;
                leak++;
                var nf = level.getFluidState(n);
                if (nf.is(Fluids.WATER) && nf.isSource()) sourceLeaks++; else if (nf.is(Fluids.WATER)) flowingLeaks++; else airLeaks++;
                if (level.getBlockState(p).hasProperty(BlockStateProperties.WATERLOGGED)) wetLeaks++;
                String template = union.get(p).getBlock().builtInRegistryHolder().key().location().toString();
                if (template.contains("boss_room")) bossLeaks++; else if (template.contains("stair")) stairLeaks++; else dungeonLeaks++;
                if (leak <= 100) BiomeMakeover.LOGGER.info("[BM_HYDRAULIC_LEAK_FACE] mansionId={} interiorPos={} exteriorPos={} face={} interiorRuntimeState={} exteriorRuntimeState={} fluidState={} source={} nearestTemplate={} nearestPieceOrdinal={} bossRoom={} stair={}",
                    mansionId(level, mansionOrigin, signature), p, n, d, level.getBlockState(p), level.getBlockState(n), nf, nf.isSource(), template, -1, template.contains("boss_room"), template.contains("stair"));
            }
        }
        BiomeMakeover.LOGGER.info("[BM_HYDRAULIC_SUMMARY] mansionId={} analysisCells={} structuralInteriorCells={} exteriorConnectedCells={} interiorExteriorConnectedCells={} leakFaces={} sourceWaterLeakFaces={} flowingWaterLeakFaces={} airLeakFaces={} waterloggedLeakFaces={} dungeonLeakFaces={} stairLeakFaces={} bossLeakFaces={}",
            mansionId(level, mansionOrigin, signature), volume, interior.size(), exterior.size(), interior.stream().filter(exterior::contains).count(), leak, sourceLeaks, flowingLeaks, airLeaks, wetLeaks, dungeonLeaks, stairLeaks, bossLeaks);
        BiomeMakeover.LOGGER.info("[BM_HYDRAULIC_WATER_VOLUME] mansionId={} explicitDryWater={} authoredWetWater={} omittedInteriorWater={} exteriorConnectedWater={} unknownWater={}",
            mansionId(level, mansionOrigin, signature), explicitDryWater, authoredWetWater, omittedInteriorWater, exteriorWater, unknownWater);
        int bossInterior = 0, bossWater = 0, bossExterior = 0, bossLeak = 0;
        for (BlockPos p : interior) if (union.getOrDefault(p, Blocks.AIR.defaultBlockState()).getBlock().builtInRegistryHolder().key().location().toString().contains("boss_room")) { bossInterior++; if (!level.getFluidState(p).isEmpty()) bossWater++; if (exterior.contains(p)) bossExterior++; }
        BiomeMakeover.LOGGER.info("[BM_BOSS_HYDRAULIC_SUMMARY] mansionId={} bossInteriorCells={} bossWaterCells={} bossExteriorConnectedCells={} bossLeakFaces={} sourceLeakFaces={} flowingLeakFaces={} airLeakFaces={}",
            mansionId(level, mansionOrigin, signature), bossInterior, bossWater, bossExterior, bossLeak, 0, 0, 0);
    }

    private static boolean isHydraulicPassable(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || !level.getFluidState(pos).isEmpty() || state.hasProperty(BlockStateProperties.WATERLOGGED);
    }

    private static String mansionId(ServerLevel level, BlockPos origin) {
        return level.dimension().location() + ":" + origin;
    }

    private static String mansionId(ServerLevel level, BlockPos origin, String signature) {
        return mansionId(level, origin) + ":" + signature;
    }

    private static String serverKey(BlockPos origin, String signature) {
        return "minecraft:overworld:" + origin + ":" + signature;
    }

    private static void registerExpectedPieces(BlockPos origin, List<Piece> pieces) {
        String signature = layoutSignature(pieces);
        for (Piece piece : pieces) piece.layoutSignature = signature;
        String key = "minecraft:overworld:" + origin + ":" + signature;
        if (Boolean.getBoolean("bm.mansion.trace")) BiomeMakeover.LOGGER.info("[BM_MANSION_LAYOUT_INSTANCE] origin={} layoutInstanceId={} thread={} structuralPieces={} registrationReason=layout_metadata", origin, signature, Thread.currentThread().getName(), pieces.stream().filter(Piece::isDungeonStructuralTemplate).count());
        Set<String> ids = new java.util.HashSet<>();
        Map<BlockPos, BlockState> completeUnion = new HashMap<>();
        int bossPieces = 0;
        for (Piece piece : pieces) if (piece.isDungeonStructuralTemplate()) { ids.add(Integer.toString(piece.mansionPieceOrdinal)); completeUnion.putAll(piece.dungeonAuthoredStates()); if (piece.diagnosticTemplate.contains("/boss_room")) bossPieces++; }
        Set<String> placements = new java.util.HashSet<>();
        for (Piece piece : pieces) if (piece.isDungeonStructuralTemplate()) {
            BoundingBox box = piece.getBoundingBox();
            for (int chunkX = box.minX() >> 4; chunkX <= box.maxX() >> 4; chunkX++)
                for (int chunkZ = box.minZ() >> 4; chunkZ <= box.maxZ() >> 4; chunkZ++)
                    placements.add(piece.mansionPieceOrdinal + ":" + chunkX + ":" + chunkZ);
        }
        LayoutMetadata metadata = new LayoutMetadata(key, signature, origin, ids, placements, completeUnion.size(), bossPieces);
        for (Piece piece : pieces) piece.layoutMetadata = metadata;
        // Candidate construction is pure: runtime maps are populated lazily by
        // the first actual Piece.postProcess callback.
        if (Boolean.getBoolean("bm.mansion.trace")) BiomeMakeover.LOGGER.info("[BM_RECONCILE_LIFECYCLE] event=LAYOUT_COMPLETE mansionId={} structuralPieces={} expectedPlacementCount={} unionPositions={}",
            key, ids.size(), placements.size(), completeUnion.size());
    }

    private static String layoutSignature(List<Piece> pieces) {
        StringBuilder canonical = new StringBuilder();
        pieces.stream().sorted(Comparator.comparingInt(piece -> piece.mansionPieceOrdinal)).forEach(piece ->
            canonical.append(piece.diagnosticTemplate).append('|').append(piece.getBoundingBox()).append('|')
                .append(piece.mansionPieceOrdinal).append(';'));
        return Integer.toUnsignedString(canonical.toString().hashCode(), 16);
    }

    private static ReconcileResult reconcileCompletedDungeon(ServerLevel level, long order, BlockPos mansionOrigin, String signature) {
        long first = Long.MAX_VALUE;
        for (DelayedFluidTrace candidate : DELAYED_FLUID_TRACES) {
            if (candidate.level == level && candidate.mansionOrigin.equals(mansionOrigin) && candidate.layoutSignature.equals(signature) && candidate.order < first) first = candidate.order;
        }
        if (order != first) return new ReconcileResult(false, 0, 0, 0, 0, 0);
        Map<BlockPos, BlockState> union = new HashMap<>();
        for (DelayedFluidTrace candidate : DELAYED_FLUID_TRACES) {
            if (candidate.level == level && candidate.mansionOrigin.equals(mansionOrigin) && candidate.layoutSignature.equals(signature)) union.putAll(candidate.authoredStates);
        }
        int correctedAir = 0, correctedWaterlogged = 0, authoredWetPreserved = 0;
        for (var entry : union.entrySet()) {
            BlockState authored = entry.getValue();
            BlockPos pos = entry.getKey();
            if (authored.isAir()) {
                // Verification-only in R.17M. Per-placement correction owns mutation.
            } else if (authored.hasProperty(BlockStateProperties.WATERLOGGED)
                && authored.getValue(BlockStateProperties.WATERLOGGED)) {
                authoredWetPreserved++;
            } else if (authored.hasProperty(BlockStateProperties.WATERLOGGED)
                && !authored.getValue(BlockStateProperties.WATERLOGGED)) {
                // Verification-only in R.17M. Per-placement correction owns mutation.
            }
        }
        int explicitDryWater = 0, authoredFalseNowWaterlogged = 0;
        for (var entry : union.entrySet()) {
            BlockState authored = entry.getValue();
            if (authored.isAir() && !level.getFluidState(entry.getKey()).isEmpty()) explicitDryWater++;
            if (authored.hasProperty(BlockStateProperties.WATERLOGGED) && !authored.getValue(BlockStateProperties.WATERLOGGED)
                && level.getBlockState(entry.getKey()).hasProperty(BlockStateProperties.WATERLOGGED)
                && level.getBlockState(entry.getKey()).getValue(BlockStateProperties.WATERLOGGED)) authoredFalseNowWaterlogged++;
        }
        return new ReconcileResult(true, correctedAir, correctedWaterlogged, authoredWetPreserved, explicitDryWater, authoredFalseNowWaterlogged);
    }

    /** Final, boss-room-only safety net for water that re-enters after placement. */
    private static int reconcileBossRoomFinalAir(ServerLevel level, BlockPos origin, String signature) {
        int cleared = 0;
        Set<BlockPos> seen = new java.util.HashSet<>();
        for (DelayedFluidTrace candidate : DELAYED_FLUID_TRACES) {
            if (candidate.level != level || !candidate.mansionOrigin.equals(origin) || !candidate.layoutSignature.equals(signature)
                || !candidate.template.contains("/boss_room")) continue;
            for (var entry : candidate.authoredStates.entrySet()) {
                if (!entry.getValue().isAir() || !seen.add(entry.getKey())) continue;
                if (!level.getFluidState(entry.getKey()).isEmpty()) {
                    level.setBlock(entry.getKey(), Blocks.AIR.defaultBlockState(), 2);
                    cleared++;
                }
            }
        }
        return cleared;
    }

    private static LateFinalization createLateFinalization(ServerLevel level, BlockPos origin, String signature, boolean ready) {
        Map<BlockPos, BlockState> crops = cropTargets(level, origin, signature); Set<BlockPos> boss = new java.util.HashSet<>();
        for (DelayedFluidTrace c : DELAYED_FLUID_TRACES) if (c.level == level && c.mansionOrigin.equals(origin) && c.layoutSignature.equals(signature)) for (var e : c.authoredStates.entrySet()) {
            String n = BuiltInRegistries.BLOCK.getKey(e.getValue().getBlock()).toString();
            if (n.contains("wheat") || n.contains("carrot") || n.contains("potato") || n.contains("beetroot") || n.contains("melon_stem") || n.contains("pumpkin_stem")) crops.putIfAbsent(e.getKey(), e.getValue());
            if (c.template.contains("/boss_room") && e.getValue().isAir()) boss.add(e.getKey());
        }
        String id = mansionId(level, origin, signature);
        Piece.BossBoundaryTrace bossTrace = BOSS_BOUNDARY_TRACES.stream().filter(t -> t.mansionId().equals(id)).findFirst().orElse(null);
        return new LateFinalization(level, id, origin, signature, crops, boss, ready, bossTrace);
    }

    private static final class LateFinalization {
        final ServerLevel level; final String id; final BlockPos origin; final String signature; final Map<BlockPos, BlockState> crops; final Set<BlockPos> bossAir; final boolean ready; final Piece.BossBoundaryTrace bossTrace; final Set<String> phases = new java.util.HashSet<>(); final Set<String> retentionPhases = new java.util.HashSet<>(); String currentPhase = "READY"; long readyTick = -1; int age = -1;
        LateFinalization(ServerLevel level, String id, BlockPos origin, String signature, Map<BlockPos, BlockState> crops, Set<BlockPos> bossAir, boolean ready, Piece.BossBoundaryTrace bossTrace) { this.level = level; this.id = id; this.origin = origin; this.signature = signature; this.crops = Map.copyOf(crops); this.bossAir = Set.copyOf(bossAir); this.ready = ready; this.bossTrace = bossTrace; }
        void tickBossBoundary(ServerLevel currentLevel) { if (bossTrace != null) bossTrace.tick(currentLevel); }
        void retention(ServerLevel level, String phase) {
            if (retentionPhases.add(phase)) BiomeMakeover.LOGGER.info("[BM_LATE_FINALIZATION_RETENTION] mansionId={} phase={} readyTick={} currentTick={} ageTicks={} retained={}", id, phase, readyTick, level.getGameTime(), level.getGameTime() - readyTick, !"D45S".equals(phase));
        }
    }

    private static int cropTargetCount(ServerLevel level, BlockPos origin, String signature) {
        return cropTargets(level, origin, signature).size();
    }

    private static Map<BlockPos, BlockState> cropTargets(ServerLevel level, BlockPos origin, String signature) {
        String mansion = mansionId(level, origin, signature);
        Map<BlockPos, BlockState> targets = new HashMap<>(CROP_TARGETS.getOrDefault(mansion, Map.of()));
        for (DelayedFluidTrace candidate : DELAYED_FLUID_TRACES)
            if (candidate.level == level && candidate.mansionOrigin.equals(origin) && candidate.layoutSignature.equals(signature))
                for (var entry : candidate.authoredStates.entrySet()) {
                    if (isCropState(entry.getValue())) targets.putIfAbsent(entry.getKey(), entry.getValue());
                }
        return targets;
    }

    private static Set<BlockPos> legacyBossAir(ServerLevel level, String mansionId) {
        Set<BlockPos> result = new java.util.HashSet<>();
        for (DelayedFluidTrace trace : DELAYED_FLUID_TRACES)
            if (trace.level == level && trace.mansionId().equals(mansionId) && trace.template.contains("/boss_room"))
                for (var entry : trace.authoredStates.entrySet()) if (entry.getValue().isAir()) result.add(entry.getKey());
        return result;
    }

    private static boolean isCropState(BlockState state) {
        String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        return id.contains("wheat") || id.contains("carrot") || id.contains("potato") || id.contains("beetroot") || id.contains("melon_stem") || id.contains("pumpkin_stem");
    }

    private static void traceCropPhase(ServerLevel level, BlockPos origin, String signature, String phase) {
        String mansion = mansionId(level, origin, signature);
        if (LATE_SUMMARY_LOGGED.add(mansion + ":" + phase)) emitLateSummaries(level, mansion, phase, origin, signature);
        for (DelayedFluidTrace candidate : DELAYED_FLUID_TRACES) {
            if (candidate.level != level || !candidate.mansionOrigin.equals(origin) || !candidate.layoutSignature.equals(signature)) continue;
            for (var entry : candidate.authoredStates.entrySet()) {
                BlockState authored = entry.getValue();
                String id = BuiltInRegistries.BLOCK.getKey(authored.getBlock()).toString();
                if (!(id.contains("wheat") || id.contains("carrot") || id.contains("potato") || id.contains("beetroot") || id.contains("melon_stem") || id.contains("pumpkin_stem"))) continue;
                String key = mansion + ":" + phase + ":" + entry.getKey();
                BlockState runtime = level.getBlockState(entry.getKey());
                if (CROP_PHASE_LOGGED.add(key)) BiomeMakeover.LOGGER.info("[BM_CROP_RUNTIME] mansionId={} template={} localPos=<serialized> worldPos={} serializedState={} phase={} runtimeState={} supportState={} inClip=true", mansion, candidate.template, entry.getKey(), authored, phase, runtime, level.getBlockState(entry.getKey().below()));
                if (!runtime.is(authored.getBlock()) && CROP_DISAPPEAR_LOGGED.add(mansion + ":" + entry.getKey())) BiomeMakeover.LOGGER.info("[BM_CROP_DISAPPEAR] mansionId={} template={} pieceOrdinal=-1 localPos=<serialized> worldPos={} serializedState={} previousState={} newState={} phase={} supportState={} lastKnownWritingPiece=unknown lastKnownWritingTemplate=unknown", mansion, candidate.template, entry.getKey(), authored, authored, runtime, phase, level.getBlockState(entry.getKey().below()));
            }
        }
    }

    private static void emitLatePhase(ServerLevel level, BlockPos origin, String signature, String phase) {
        String id = mansionId(level, origin, signature);
        LateFinalization late = LATE_FINALIZATIONS.get(id);
        if (late == null || !late.ready || !isFinalPlacementComplete(id)) {
            emitLateContractViolation(level, id, phase, late == null ? "NONE" : late.currentPhase, late, "authoritative readiness or placement completion missing");
            return;
        }
        int ordinal = switch (phase) { case "C5" -> 1; case "C6" -> 2; case "C7" -> 3; case "C8" -> 4; default -> -1; };
        if (ordinal < 1 || !late.phases.add(phase) || ordinal != late.phases.size()) {
            emitLateContractViolation(level, id, phase, late.currentPhase, late, "duplicate or out-of-order phase");
            return;
        }
        late.currentPhase = phase;
        traceCropPhase(level, origin, signature, phase);
    }

    private static void emitLateContractViolation(ServerLevel level, String mansionId, String attemptedPhase, String currentPhase,
                                                   LateFinalization late, String reason) {
        String key = "contract:" + mansionId + ":" + attemptedPhase + ":" + reason;
        if (!CROP_DISAPPEAR_LOGGED.add(key)) return;
        BiomeMakeover.LOGGER.error("[BM_LATE_FINALIZATION_CONTRACT_VIOLATION] mansionId={} attemptedPhase={} currentPhase={} ready={} placedPlacementCount={} expectedPlacementCount={} placedPieceCount={} expectedPieceCount={} registeredCropTargets={} reason={}",
            mansionId, attemptedPhase, currentPhase, late != null && late.ready,
            PLACED_PLACEMENTS.getOrDefault(mansionId, Set.of()).size(), EXPECTED_PLACEMENTS.getOrDefault(mansionId, Set.of()).size(),
            PLACED_PIECES.getOrDefault(mansionId, Set.of()).size(), EXPECTED_ORDINALS.getOrDefault(mansionId, Set.of()).size(), late == null ? 0 : late.crops.size(), reason);
    }

    private static void emitCropFinalizationResult(ServerLevel level, LateFinalization late, int restored, String phase) {
        int present = 0, supportMissing = 0;
        for (var entry : late.crops.entrySet()) {
            if (level.getBlockState(entry.getKey()).is(entry.getValue().getBlock())) present++;
            if (!level.getBlockState(entry.getKey().below()).is(Blocks.FARMLAND)) supportMissing++;
        }
        int missing = late.crops.size() - present;
        int presentBefore = Math.max(0, present - restored);
        BiomeMakeover.LOGGER.info("[BM_CROP_FINALIZATION_RESULT] mansionId={} phase={} expected={} presentBefore={} missingBefore={} restored={} presentAfter={} missingAfter={} supportMissing={}",
            late.id, phase, late.crops.size(), presentBefore, late.crops.size() - presentBefore, restored, present, missing, supportMissing);
    }

    private static void emitLateSummaries(ServerLevel level, String mansion, String phase, BlockPos origin, String signature) {
        int expected = 0, present = 0, supportMissing = 0, air = 0, water = 0, source = 0, flowing = 0;
        String template = "serialized";
        Map<BlockPos, BlockState> registeredTargets = cropTargets(level, origin, signature);
        expected = registeredTargets.size();
        for (var e : registeredTargets.entrySet()) {
            if (level.getBlockState(e.getKey()).is(e.getValue().getBlock())) present++;
            if (!level.getBlockState(e.getKey().below()).is(Blocks.FARMLAND)) supportMissing++;
        }
        BossGeometry geometry = BOSS_GEOMETRIES.get(mansion);
        if (geometry != null) {
            Set<BlockPos> c8Water = "C8".equals(phase) ? BOSS_C8_WATER_CELLS.get(mansion) : null;
            Map<BlockPos, net.minecraft.world.level.material.FluidState> c8States = "C8".equals(phase) ? BOSS_C8_WATER_STATES.get(mansion) : null;
            for (BlockPos pos : geometry.explicitAir()) {
                air++;
                var f = c8States == null ? level.getFluidState(pos) : c8States.getOrDefault(pos, Fluids.EMPTY.defaultFluidState());
                if (!f.isEmpty()) { water++; if (f.isSource()) source++; else flowing++; }
            }
        }
        for (DelayedFluidTrace c : DELAYED_FLUID_TRACES) if (c.level == level && c.mansionOrigin.equals(origin) && c.layoutSignature.equals(signature)
            && (c.template.contains("room_big_10") || c.template.contains("room_8"))) template = c.template;
        switch (phase) {
            case "C5" -> { BiomeMakeover.LOGGER.info("[BM_BOSS_ROOM_LATE_AUDIT] mansionId={} phase=C5 explicitAirChecked={} waterFound={} sourceWaterFound={} flowingWaterFound={}", mansion, air, water, source, flowing); BiomeMakeover.LOGGER.info("[BM_CROP_LATE_SUMMARY] mansionId={} template={} phase=C5 expected={} present={} missing={} supportMissing={}", mansion, template, expected, present, expected-present, supportMissing); }
            case "C6" -> { BiomeMakeover.LOGGER.info("[BM_BOSS_ROOM_LATE_AUDIT] mansionId={} phase=C6 explicitAirChecked={} waterFound={} sourceWaterFound={} flowingWaterFound={}", mansion, air, water, source, flowing); BiomeMakeover.LOGGER.info("[BM_CROP_LATE_SUMMARY] mansionId={} template={} phase=C6 expected={} present={} missing={} supportMissing={}", mansion, template, expected, present, expected-present, supportMissing); }
            case "C7" -> { BiomeMakeover.LOGGER.info("[BM_BOSS_ROOM_LATE_AUDIT] mansionId={} phase=C7 explicitAirChecked={} waterFound={} sourceWaterFound={} flowingWaterFound={}", mansion, air, water, source, flowing); BiomeMakeover.LOGGER.info("[BM_CROP_LATE_SUMMARY] mansionId={} template={} phase=C7 expected={} present={} missing={} supportMissing={}", mansion, template, expected, present, expected-present, supportMissing); }
            case "C8" -> { BiomeMakeover.LOGGER.info("[BM_BOSS_ROOM_LATE_AUDIT] mansionId={} phase=C8 explicitAirChecked={} waterFound={} sourceWaterFound={} flowingWaterFound={}", mansion, air, water, source, flowing); BiomeMakeover.LOGGER.info("[BM_CROP_LATE_SUMMARY] mansionId={} template={} phase=C8 expected={} present={} missing={} supportMissing={}", mansion, template, expected, present, expected-present, supportMissing); }
        }
    }

    private record EnvelopePiece(ServerLevel level, String template, Set<BlockPos> authoredDry, Set<BlockPos> architecturalInterior) {
        private EnvelopePiece(ServerLevel level, String template, List<BlockPos> authoredDry, List<BlockPos> architecturalInterior) {
            this(level, template, Set.copyOf(authoredDry), Set.copyOf(architecturalInterior));
        }
    }

    private static final class DelayedFluidTrace {
        private final ServerLevel level;
        private final String template;
        private final Rotation rotation;
        private final BlockPos mansionOrigin;
        private final String layoutSignature;
        private final List<BlockPos> authoredDry;
        private final List<BlockPos> architecturalInterior;
        private final Map<BlockPos, BlockState> authoredStates;
        private final long order;
        private final java.util.Set<BlockPos> previouslyWet = new java.util.HashSet<>();
        private int age;

        private DelayedFluidTrace(ServerLevel level, String template, Rotation rotation, BlockPos mansionOrigin, List<BlockPos> authoredDry,
                                  List<BlockPos> architecturalInterior, Map<BlockPos, BlockState> authoredStates, long order, String layoutSignature) {
            this.level = level; this.template = template; this.rotation = rotation;
            this.mansionOrigin = mansionOrigin;
            this.layoutSignature = layoutSignature;
            this.authoredDry = List.copyOf(authoredDry); this.architecturalInterior = List.copyOf(architecturalInterior);
            this.authoredStates = Map.copyOf(authoredStates); this.order = order;
        }

        private String mansionId() {
            return level.dimension().location() + ":" + mansionOrigin + ":" + layoutSignature;
        }

        private void snapshot(ServerLevel level, String phase) {
            if (!ARCHAEology_TRACE) return;
            int water = 0, source = 0, flowing = 0;
            int newlyWet = 0;
            for (BlockPos pos : architecturalInterior) {
                var fluid = level.getFluidState(pos);
                if (fluid.is(Fluids.WATER)) {
                    water++;
                    if (fluid.isSource()) source++; else flowing++;
                    if (previouslyWet.add(pos) && newlyWet++ < 12) {
                        String classification = envelopeClassification(level, pos);
                        if (!authoredDry.contains(pos)) {
                            BiomeMakeover.LOGGER.info("[BM_UNTRACKED_STAIR_WATER] world={} template={} runtimeBlock={} fluid={} explicitMask=false architecturalInterior=true neighborOwnership={}",
                                pos, template, level.getBlockState(pos).getBlock(), fluid, envelopeNeighbor(level, pos));
                        }
                        BiomeMakeover.LOGGER.info("[BM_FLUID_REENTRY] template={} local=<static-mask> world={} firstWetPhase={} fluid={} sourceOrFlowing={} neighborN={} neighborE={} neighborS={} neighborW={} neighborUp={} neighborDown={} owningDungeonPiece={} boundaryPosition={} intentionalOpening={} classification={}",
                            template, pos, phase, fluid, fluid.isSource() ? "SOURCE" : "FLOWING", level.getBlockState(pos.north()).getBlock(),
                            level.getBlockState(pos.east()).getBlock(), level.getBlockState(pos.south()).getBlock(), level.getBlockState(pos.west()).getBlock(),
                            level.getBlockState(pos.above()).getBlock(), level.getBlockState(pos.below()).getBlock(), template, true, false, classification);
                        BiomeMakeover.LOGGER.info("[BM_DUNGEON_ENVELOPE] wetPos={} owner={} classification={} N={} E={} S={} W={} UP={} DOWN={}",
                            pos, template, classification, envelopeNeighbor(level, pos.north()), envelopeNeighbor(level, pos.east()),
                            envelopeNeighbor(level, pos.south()), envelopeNeighbor(level, pos.west()), envelopeNeighbor(level, pos.above()), envelopeNeighbor(level, pos.below()));
                    }
                }
            }
            BiomeMakeover.LOGGER.info("[BM_DUNGEON_FLUID_DELAYED] template={} rotation={} phase={} authoredDryPositions={} waterInArchitecturalInterior={} sourceWaterInArchitecturalInterior={} flowingWaterInArchitecturalInterior={} newlyWetPositions={} orderIndex={}",
                template, rotation, phase, authoredDry.size(), water, source, flowing, newlyWet, order);
            int transitions = 0;
            for (var entry : authoredStates.entrySet()) {
                BlockState authored = entry.getValue();
                if (!authored.hasProperty(BlockStateProperties.WATERLOGGED)) continue;
                BlockState runtime = level.getBlockState(entry.getKey());
                if (transitions++ < 24) BiomeMakeover.LOGGER.info("[BM_WATERLOG_TRANSITION] template={} local=<static-mask> world={} block={} P0={} {}={} phase={} runtimeState={} neighborFluids={} envelopeClassification={}",
                    template, entry.getKey(), authored.getBlock(), authored, "waterlogged", authored.getValue(BlockStateProperties.WATERLOGGED), phase,
                    runtime, level.getFluidState(entry.getKey()), envelopeClassification(level, entry.getKey()));
            }
            if (template.contains("/stair_")) {
                int explicitWater = 0;
                for (BlockPos pos : authoredDry) if (level.getFluidState(pos).is(Fluids.WATER)) explicitWater++;
                BiomeMakeover.LOGGER.info("[BM_STAIR_FLUID_COVERAGE] template={} rotation={} explicitDryCount={} architecturalInteriorCount={} waterInExplicitDry={} waterInArchitecturalInterior={} waterOutsideExplicitMaskButInsideInterior={} phase={}",
                    template, rotation, authoredDry.size(), architecturalInterior.size(), explicitWater, water,
                    Math.max(0, water - explicitWater), phase);
            }
        }

        private String envelopeClassification(ServerLevel level, BlockPos pos) {
            Set<BlockPos> union = new java.util.HashSet<>();
            for (EnvelopePiece piece : DUNGEON_ENVELOPE) if (piece.level() == level) union.addAll(piece.architecturalInterior());
            boolean n = union.contains(pos.north()), e = union.contains(pos.east()), s = union.contains(pos.south()),
                w = union.contains(pos.west()), up = union.contains(pos.above()), down = union.contains(pos.below());
            if (n && e && s && w && up && down) return "UNION_INTERIOR";
            if (n || e || s || w || up || down) return "INTER_PIECE_SEAM";
            return "UNION_EXTERIOR";
        }

        private String envelopeNeighbor(ServerLevel level, BlockPos pos) {
            Set<BlockPos> union = new java.util.HashSet<>();
            for (EnvelopePiece piece : DUNGEON_ENVELOPE) if (piece.level() == level) union.addAll(piece.architecturalInterior());
            return (union.contains(pos) ? "UNION_DRY" : "OUTSIDE") + ":" + level.getBlockState(pos).getBlock();
        }
    }

    public static final MapCodec<MansionFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        settingsCodec(i), MansionTemplates.CODEC.fieldOf("templates").forGetter(s -> s.templates),
        MansionDetails.CODEC.fieldOf("details").forGetter(s -> s.details)
    ).apply(i, MansionFeature::new));

    private final MansionTemplates templates;
    private final MansionDetails details;

    public MansionFeature(StructureSettings settings, MansionTemplates templates, MansionDetails details) {
        super(settings);
        this.templates = templates;
        this.details = details;
    }

    @Override public GenerationStep.Decoration step() { return GenerationStep.Decoration.SURFACE_STRUCTURES; }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        StructurePiecesBuilder builder = new StructurePiecesBuilder();
        MansionLayout layout = new MansionLayout();
        int releasedY = context.chunkGenerator().getBaseHeight(context.chunkPos().getMinBlockX(), context.chunkPos().getMinBlockZ(),
            Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        List<Integer> terrainSamples = footprintHeights(context);
        int baseY = enhancedBaseY(terrainSamples);
        int minTerrain = terrainSamples.stream().mapToInt(Integer::intValue).min().orElse(releasedY);
        int maxTerrain = terrainSamples.stream().mapToInt(Integer::intValue).max().orElse(releasedY);
        int spread = maxTerrain - minTerrain;
        int maxAbove = terrainSamples.stream().mapToInt(h -> Math.max(0, h - baseY)).max().orElse(0);
        int maxGap = terrainSamples.stream().mapToInt(h -> Math.max(0, baseY - h)).max().orElse(0);
        String rejection = spread > 40 ? "SPREAD" : maxAbove > 20 ? "TERRAIN_ABOVE_BASE" : maxGap > 20 ? "TERRAIN_GAP" : "";
        if (!rejection.isEmpty()) {
            if (Boolean.getBoolean("bm.mansion.trace")) BiomeMakeover.LOGGER.info("[BM_MANSION_SITE_REJECT] chunk={} median={} min={} max={} spread={} baseY={} maxTerrainAboveBase={} maxGapBelowBase={} reason={}",
                context.chunkPos(), terrainSamples.stream().sorted().toList().get(terrainSamples.size() / 2), minTerrain, maxTerrain, spread, baseY, maxAbove, maxGap, rejection);
            return Optional.empty();
        }
        BlockPos origin = new BlockPos(context.chunkPos().getMinBlockX(), baseY, context.chunkPos().getMinBlockZ());
        LAYOUT_ORIGIN.set(origin);
        LAYOUT_PIECES.set(new java.util.ArrayList<>());
        NEXT_PIECE_ORDINAL.set(0);
        layout.generateLayout(context.random(), origin.getY());
        if (Boolean.getBoolean("bm.mansion.trace")) traceMansionHeight(context, origin, releasedY, terrainSamples, true, "");
        Collection<MansionRoom> rooms = layout.getLayout().getEntries().stream()
            .sorted(Comparator.comparingInt(MansionRoom::getSortValue)).toList();
        for (MansionRoom room : rooms) {
            int x = origin.getX() + room.getPosition().getX() * MansionLayoutFoundation.CELL_XZ;
            int y = origin.getY() + room.getPosition().getY() * MansionLayoutFoundation.CELL_Y;
            int z = origin.getZ() + room.getPosition().getZ() * MansionLayoutFoundation.CELL_XZ;
            Rotation rotation = room.getRotation(context.random());
            BlockPos roomPos = room.getOffsetForRotation(new BlockPos(x, y, z), rotation);
            builder.addPiece(new Piece(details, context.structureTemplateManager(),
                room.getTemplate(templates, context.random()).toString(), roomPos, rotation,
                room.getPosition().getY() == 0,
                room.getRoomType() == RoomType.TOWER_MID || room.getRoomType() == RoomType.TOWER_TOP));
            room.addWalls(details, templates, context.random(), new BlockPos(x, y, z),
                context.structureTemplateManager(), layout.getLayout(), builder);
        }
        LAYOUT_ORIGIN.remove();
        registerExpectedPieces(origin, LAYOUT_PIECES.get());
        LAYOUT_PIECES.remove();
        NEXT_PIECE_ORDINAL.remove();
        return Optional.of(new GenerationStub(
            getLowestYIn5by5BoxOffset7Blocks(context, Rotation.NONE), Either.right(builder)));
    }

    private static int enhancedBaseY(List<Integer> heights) {
        heights.sort(Integer::compareTo);
        int median = heights.get(heights.size() / 2);
        int bias = Math.min(4, Math.max(0, (heights.get(heights.size() - 1) - heights.get(0)) > 25 ? 2 : 1));
        return Math.min(median + bias, median + 8);
    }

    private static List<Integer> footprintHeights(GenerationContext context) {
        List<Integer> heights = new java.util.ArrayList<>();
        for (int dx : new int[] {-96, -48, 0, 48, 96}) for (int dz : new int[] {-96, -48, 0, 48, 96}) {
            heights.add(context.chunkGenerator().getBaseHeight(context.chunkPos().getMinBlockX() + dx,
                context.chunkPos().getMinBlockZ() + dz, Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(), context.randomState()));
        }
        return heights;
    }

    private static void traceMansionHeight(GenerationContext context, BlockPos origin, int releasedY, List<Integer> heights, boolean suitable, String rejection) {
        heights.sort(Integer::compareTo);
        int sum = heights.stream().mapToInt(Integer::intValue).sum();
        int median = heights.get(heights.size() / 2);
        int bias = origin.getY() - median;
        BiomeMakeover.LOGGER.info("[BM_MANSION_HEIGHT_TRACE] structureChunk={} generationPoint={} sampleX={} sampleZ={} sampledHeight={} heightmapType={} baseY={} firstFloorY={} roofReferenceY={} dungeonTopY={} dungeonBottomY={} minSurfaceY={} maxSurfaceY={} medianSurfaceY={} meanSurfaceY={} anchorMinusMedian={} anchorMinusMin={} anchorMinusMax={}",
            context.chunkPos(), origin, origin.getX(), origin.getZ(), releasedY, Heightmap.Types.WORLD_SURFACE_WG,
            origin.getY(), origin.getY(), origin.getY() + 42, origin.getY() - 1, origin.getY() - 8,
            heights.get(0), heights.get(heights.size() - 1), median, sum / (double) heights.size(),
            releasedY - median, releasedY - heights.get(0), releasedY - heights.get(heights.size() - 1));
        BiomeMakeover.LOGGER.info("[BM_MANSION_HEIGHT_TRACE] releasedAnchorY={} enhancedBaseY={} sampleCount={} median={} mean={} min={} max={} bias={} samplesAboveBase={} samplesBelowBase={} maxTerrainAboveBase={} maxGapBelowBase={} baseDeltaFromReleased={} terrainSpread={} siteSuitable={} rejectionReason={} thresholdSpread=40 thresholdAbove=20 thresholdGap=20",
            releasedY, origin.getY(), heights.size(), median, sum / (double) heights.size(), heights.get(0), heights.get(heights.size() - 1), bias,
            heights.stream().filter(h -> h > origin.getY()).count(), heights.stream().filter(h -> h < origin.getY()).count(),
            heights.stream().mapToInt(h -> Math.max(0, h - origin.getY())).max().orElse(0),
            heights.stream().mapToInt(h -> Math.max(0, origin.getY() - h)).max().orElse(0), origin.getY() - releasedY,
            heights.get(heights.size() - 1) - heights.get(0), suitable, rejection);
    }

    @Override public StructureType<?> type() { return BMStructures.MANSION; }

    public MansionTemplates templates() { return templates; }
    public MansionDetails details() { return details; }

    /** Builds the released physical piece graph without activating worldgen. */
    public static void buildLayoutPieces(StructureTemplateManager manager, BlockPos origin,
                                         RandomSource random, MansionTemplates templates,
                                         MansionDetails details, StructurePiecesBuilder builder) {
        MansionLayout layout = new MansionLayout();
        LAYOUT_ORIGIN.set(origin);
        LAYOUT_PIECES.set(new java.util.ArrayList<>());
        NEXT_PIECE_ORDINAL.set(0);
        layout.generateLayout(random, origin.getY());
        Collection<MansionRoom> rooms = layout.getLayout().getEntries().stream()
            .sorted(Comparator.comparingInt(MansionRoom::getSortValue)).toList();
        for (MansionRoom room : rooms) {
            int x = origin.getX() + room.getPosition().getX() * MansionLayoutFoundation.CELL_XZ;
            int y = origin.getY() + room.getPosition().getY() * MansionLayoutFoundation.CELL_Y;
            int z = origin.getZ() + room.getPosition().getZ() * MansionLayoutFoundation.CELL_XZ;
            Rotation rotation = room.getRotation(random);
            BlockPos roomPos = room.getOffsetForRotation(new BlockPos(x, y, z), rotation);
            builder.addPiece(new Piece(details, manager, room.getTemplate(templates, random).toString(), roomPos,
                rotation, room.getPosition().getY() == 0,
                room.getRoomType() == RoomType.TOWER_MID || room.getRoomType() == RoomType.TOWER_TOP));
            room.addWalls(details, templates, random, new BlockPos(x, y, z), manager, layout.getLayout(), builder);
        }
        LAYOUT_ORIGIN.remove();
        registerExpectedPieces(origin, LAYOUT_PIECES.get());
        LAYOUT_PIECES.remove();
        NEXT_PIECE_ORDINAL.remove();
    }

    /** Serialized custom template piece; marker actions are connected later. */
    public static final class Piece extends TemplateStructurePiece {
        private static final boolean TRACE = Boolean.getBoolean("bm.mansion.trace");
        private static final AtomicLong TRACE_ORDER = new AtomicLong();
        private final boolean ground;
        private final boolean wall;
        private final BlockPos mansionOrigin;
        private final int mansionPieceOrdinal;
        private String layoutSignature;
        private LayoutMetadata layoutMetadata;
        private Set<String> persistedOrdinals = Set.of();
        private Set<String> persistedPlacements = Set.of();
        private int persistedUnionSize = -1;
        private int persistedBossPieces = -1;
        private MansionDetails details;
        private String diagnosticTemplate;

    public Piece(StructureTemplateManager manager, ResourceLocation template, BlockPos position,
                     Rotation rotation, boolean ground, boolean wall) {
            super(BMStructures.MANSION_PIECE, 0, manager, template, template.toString(),
                settings(rotation, wall), position);
            this.ground = ground;
            this.wall = wall;
            this.details = null;
            this.diagnosticTemplate = template.toString();
            this.mansionOrigin = LAYOUT_ORIGIN.get() == null ? position : LAYOUT_ORIGIN.get();
            this.mansionPieceOrdinal = NEXT_PIECE_ORDINAL.get() == null ? -1 : NEXT_PIECE_ORDINAL.get();
            if (NEXT_PIECE_ORDINAL.get() != null) NEXT_PIECE_ORDINAL.set(NEXT_PIECE_ORDINAL.get() + 1);
            if (LAYOUT_PIECES.get() != null) LAYOUT_PIECES.get().add(this);
    }

    public Piece(MansionDetails details, StructureTemplateManager manager, String template, BlockPos position,
                 Rotation rotation, boolean ground, boolean wall) {
        this(manager, ResourceLocation.parse(template), position, rotation, ground, wall);
        this.details = details;
    }

        public Piece(StructurePieceSerializationContext context, CompoundTag tag) {
            super(BMStructures.MANSION_PIECE, tag, context.structureTemplateManager(),
                ignored -> settings(Rotation.valueOf(tag.getStringOr("Rotation", Rotation.NONE.name())),
                    tag.getBooleanOr("IsWall", false)));
            this.ground = tag.getBooleanOr("Ground", false);
            this.wall = tag.getBooleanOr("IsWall", false);
            this.details = tag.contains("Details") ? MansionDetails.CODEC.decode(net.minecraft.nbt.NbtOps.INSTANCE, tag.get("Details")).result().map(com.mojang.datafixers.util.Pair::getFirst).orElse(null) : null;
            this.diagnosticTemplate = tag.getStringOr("Template", "<serialized>");
            this.mansionOrigin = tag.contains("MansionOriginX")
                ? new BlockPos(tag.getInt("MansionOriginX").orElse(0), tag.getInt("MansionOriginY").orElse(0), tag.getInt("MansionOriginZ").orElse(0))
                : templatePosition;
            this.mansionPieceOrdinal = tag.getInt("MansionPieceOrdinal").orElse(-1);
            this.layoutSignature = tag.getStringOr("MansionLayoutSignature", "legacy");
            this.persistedOrdinals = splitMetadataSet(tag.getStringOr("MansionExpectedOrdinals", ""));
            this.persistedPlacements = splitMetadataSet(tag.getStringOr("MansionExpectedPlacements", ""));
            this.persistedUnionSize = tag.getInt("MansionUnionSize").orElse(-1);
            this.persistedBossPieces = tag.getInt("MansionBossPieces").orElse(-1);
            this.layoutMetadata = persistedOrdinals.isEmpty() || persistedPlacements.isEmpty() || persistedUnionSize < 0
                ? null : new LayoutMetadata(serverKey(mansionOrigin, layoutSignature), layoutSignature, mansionOrigin,
                    persistedOrdinals, persistedPlacements, persistedUnionSize, Math.max(0, persistedBossPieces));
        }

        private static Set<String> splitMetadataSet(String value) {
            if (value == null || value.isEmpty()) return Set.of();
            return Set.of(value.split("\\n"));
        }

        private static StructurePlaceSettings settings(Rotation rotation, boolean wall) {
            return new StructurePlaceSettings().setIgnoreEntities(true)
            .setRotation(rotation).setMirror(Mirror.NONE)
                .setLiquidSettings(LiquidSettings.IGNORE_WATERLOGGING)
                .addProcessor(wall ? BlockIgnoreProcessor.STRUCTURE_AND_AIR : BlockIgnoreProcessor.STRUCTURE_BLOCK);
        }

        @Override protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
            super.addAdditionalSaveData(context, tag);
            tag.putString("Rotation", placeSettings.getRotation().name());
            tag.putBoolean("Ground", ground);
            tag.putBoolean("IsWall", wall);
            tag.putString("Template", diagnosticTemplate);
            tag.putInt("MansionOriginX", mansionOrigin.getX());
            tag.putInt("MansionOriginY", mansionOrigin.getY());
            tag.putInt("MansionOriginZ", mansionOrigin.getZ());
            tag.putInt("MansionPieceOrdinal", mansionPieceOrdinal);
            tag.putString("MansionLayoutSignature", layoutSignature == null ? "legacy" : layoutSignature);
            if (layoutMetadata != null) {
                tag.putString("MansionExpectedOrdinals", String.join("\n", layoutMetadata.ordinals));
                tag.putString("MansionExpectedPlacements", String.join("\n", layoutMetadata.placements));
                tag.putInt("MansionUnionSize", layoutMetadata.unionSize);
                tag.putInt("MansionBossPieces", layoutMetadata.bossPieces);
            }
            if (details != null) {
                MansionDetails.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, details).result().ifPresent(value -> tag.put("Details", value));
            }
        }

        @Override protected void handleDataMarker(String metadata, BlockPos position, ServerLevelAccessor level,
                                                   RandomSource random, BoundingBox bounds) {
            BlockState expected = switch (metadata) {
                case "boss" -> Blocks.AIR.defaultBlockState();
                case "arena_pos" -> Blocks.SMOOTH_QUARTZ.defaultBlockState();
                default -> null;
            };
            if (expected == null) return;
            BlockState before = level.getBlockState(position);
            var fluidBefore = level.getFluidState(position);
            String id = level.getLevel() instanceof ServerLevel server
                ? mansionId(server, mansionOrigin, layoutSignature) : null;
            if (id != null) {
                MarkerFluidStats stats = MARKER_FLUID_STATS.computeIfAbsent(id, ignored -> new MarkerFluidStats());
                stats.markerCount++;
                if (!fluidBefore.isEmpty()) stats.fluidBearingBefore++;
                if (fluidBefore.isSource()) stats.sourceBearingBefore++;
                level.setBlock(position, expected, 2);
                BlockState after = level.getBlockState(position);
                var fluidAfter = level.getFluidState(position);
                if (TRACE && !fluidBefore.isEmpty() && stats.emitted++ < 16)
                    BiomeMakeover.LOGGER.info("[BM_DATA_MARKER_FLUID] mansionId={} template={} markerMetadata={} worldPos={} rotation={} runtimeBefore={} fluidBefore={} sourceBefore={} releasedExpectedState={} runtimeAfter={} fluidAfter={} sourceAfter={} releasedSemanticApplied={}",
                        id, diagnosticTemplate, metadata, position, placeSettings.getRotation(), before, fluidBefore, fluidBefore.isSource(), expected,
                        after, fluidAfter, fluidAfter.isSource(), after.equals(expected));
                if (!fluidBefore.isEmpty()) stats.corrected++;
                if (!fluidAfter.isEmpty()) stats.fluidBearingAfter++;
                if (fluidAfter.isSource()) stats.sourceBearingAfter++;
            }
        }

        /**
         * Directional Data is released construction metadata.  The 1.20.1
         * TemplateStructurePiece mixin consumes these entries before they can
         * remain in the finished Mansion; gameplay marker dispatch is still
         * deferred.  Consume only this piece's transformed entries so facing
         * remains available to later marker infrastructure.
         */
        @Override
        public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                                RandomSource random, BoundingBox bounds, ChunkPos chunkPos, BlockPos pivot) {
            long order = TRACE_ORDER.incrementAndGet();
            if (level.getLevel() instanceof ServerLevel serverLevel) registerCropTargets(serverLevel);
            if (Boolean.getBoolean("bm.mansion.trace") && diagnosticTemplate.contains("/boss_room")
                && level.getLevel() instanceof ServerLevel serverLevel) {
                BossBoundaryTrace.register(serverLevel, this);
            }
            if (TRACE) {
                if (false) BiomeMakeover.LOGGER.info("[BM_PIECE_TRACE] template={} rot={} bounds={} phase=BEGIN thread={} timestamp={} orderIndex={}",
                    diagnosticTemplate, placeSettings.getRotation(), bounds, Thread.currentThread().getName(), System.currentTimeMillis(), order);
                traceLootLifecycle(level, bounds, "T0", order);
                traceFences(level, bounds, "F0", order);
                traceFluids(level, bounds, "W0", order);
                traceFluidInterior(level, "W0", order);
                traceCrops(level, "C0");
            }
            List<BlockPos> staticAuthoredDry = dungeonAuthoredDryPositions();
            Map<BlockPos, BlockState> authoredStates = dungeonAuthoredStates();
            List<BlockPos> architecturalInterior = dungeonArchitecturalInterior(bounds);
            if (TRACE && isDungeonStructuralTemplate()) traceWaterlogTransitions(level, authoredStates, "P0");
            super.postProcess(level, structureManager, generator, random, bounds, chunkPos, pivot);
            if (TRACE) {
                if (false) BiomeMakeover.LOGGER.info("[BM_PIECE_TRACE] template={} rot={} bounds={} phase=AFTER_TEMPLATE thread={} timestamp={} orderIndex={}",
                    diagnosticTemplate, placeSettings.getRotation(), bounds, Thread.currentThread().getName(), System.currentTimeMillis(), order);
                traceLootLifecycle(level, bounds, "T1", order);
                traceFences(level, bounds, "F1", order);
                traceFluids(level, bounds, "W1", order);
                traceFluidInterior(level, "W1", order);
                traceCrops(level, "C1");
                if (isDungeonStructuralTemplate()) traceWaterlogTransitions(level, authoredStates, "P1");
                for (var type : new Block[] {Blocks.CHEST, Blocks.BARREL, Blocks.TRAPPED_CHEST, Blocks.DISPENSER, Blocks.DROPPER}) {
                    for (var info : template.filterBlocks(templatePosition, placeSettings, type)) {
                        BlockEntity be = level.getBlockEntity(info.pos());
                        BiomeMakeover.LOGGER.info("[BM_CONTAINER_TRACE] template={} rot={} pos={} block={} be={} inBounds={}",
                            diagnosticTemplate, placeSettings.getRotation(), info.pos(), info.state().getBlock(),
                            be == null ? "null" : be.getClass().getSimpleName(), bounds.isInside(info.pos()));
                    }
                }
                for (var type : new Block[] {Blocks.OAK_FENCE, Blocks.SPRUCE_FENCE, Blocks.BIRCH_FENCE,
                        Blocks.JUNGLE_FENCE, Blocks.ACACIA_FENCE, Blocks.DARK_OAK_FENCE, Blocks.MANGROVE_FENCE,
                        Blocks.CHERRY_FENCE, Blocks.BAMBOO_FENCE}) {
                    for (var info : template.filterBlocks(templatePosition, placeSettings, type)) {
                        BlockState runtime = level.getBlockState(info.pos());
                        BiomeMakeover.LOGGER.info("[BM_FENCE_TRACE] template={} rot={} pos={} serialized={} runtime={} neighbors={} inBounds={}",
                            diagnosticTemplate, placeSettings.getRotation(), info.pos(), info.state(), runtime,
                            neighborSummary(level, info.pos()), bounds.isInside(info.pos()));
                    }
                }
            }
            for (var info : template.filterBlocks(templatePosition, placeSettings, BMBlocks.DIRECTIONAL_DATA)) {
                if (info.nbt() != null && info.state().hasProperty(DirectionalBlock.FACING)) {
                    Direction facing = info.state().getValue(DirectionalBlock.FACING);
                    if (TRACE && info.nbt().getStringOr("metadata", "").startsWith("loot")) {
                        traceLootMarker(level, bounds, info, facing, "T2", order);
                    }
                    if (TRACE && info.nbt().getStringOr("metadata", "").startsWith("loot")) {
                        BlockPos target = info.pos().relative(facing);
                        BlockEntity targetBe = level.getBlockEntity(target);
                        BiomeMakeover.LOGGER.info("[BM_LOOT_TRACE] template={} rot={} metadata={} marker={} facing={} target={} block={} be={} inBounds={}",
                            diagnosticTemplate, placeSettings.getRotation(), info.nbt().getStringOr("metadata", ""), info.pos(), facing,
                            target, level.getBlockState(target).getBlock(), targetBe == null ? "null" : targetBe.getClass().getSimpleName(), bounds.isInside(target));
                    }
                    handleDirectionalMetadata(info.nbt().getStringOr("metadata", ""), facing, info.pos(), level, random);
                    if (TRACE && info.nbt().getStringOr("metadata", "").startsWith("loot")) {
                        traceLootMarker(level, bounds, info, facing, "T3", order);
                    }
                }
            }
            restoreSerializedCrops(level, bounds);
            if (TRACE) traceCrops(level, "C4");
            // Modern structure placement can merge an existing source fluid into
            // waterloggable blocks even when keepLiquids(false) is set.  Released
            // Mansion dungeon templates explicitly authored these states as dry;
            // restore only those transformed template cells, leaving authored wet
            // states and all surrounding world fluid untouched.
            // Legacy R17 authored-fluid mutation is retired; native liquid settings are authoritative.
            if (VANILLA_LIQUID_PARITY && diagnosticTemplate.contains("/boss_room")) clearBossRoomAuthoredAir(level, authoredStates, bounds);
            if (VANILLA_LIQUID_PARITY && isDungeonStructuralTemplate() && level.getLevel() instanceof ServerLevel serverLevel) {
                String key = serverLevel.dimension().location() + ":" + mansionOrigin + ":" + layoutSignature;
                if (TRIAL_LIQUID_LOGGED.add(key) && TRACE) BiomeMakeover.LOGGER.info("[BM_TRIAL_LIQUID_PARITY] liquidMode=IGNORE_WATERLOGGING nativeVanillaPlacement=true customDryCorrectionEnabled=false customSourceClosureEnabled=false");
            }
            if (TRACE) traceFluidInterior(level, "W2", order);
            if (TRACE) traceCrops(level, "C2");
            if (TRACE && isDungeonStructuralTemplate()) traceWaterlogTransitions(level, authoredStates, "P2");
            if (TRACE) {
                traceLootLifecycle(level, bounds, "T4", order);
                traceFences(level, bounds, "F4", order);
                traceFluids(level, bounds, "W5", order);
                traceFluidInterior(level, "W3", order);
                traceCrops(level, "C3");
                if (isDungeonStructuralTemplate() && level.getLevel() instanceof ServerLevel serverLevel) {
                    DELAYED_FLUID_TRACES.add(new DelayedFluidTrace(serverLevel, diagnosticTemplate,
                        placeSettings.getRotation(), mansionOrigin, staticAuthoredDry, architecturalInterior, authoredStates, order, layoutSignature));
                    DUNGEON_ENVELOPE.add(new EnvelopePiece(serverLevel, diagnosticTemplate, staticAuthoredDry, architecturalInterior));
                    if (DUNGEON_ENVELOPE.size() > 512) DUNGEON_ENVELOPE.remove(0);
                }
                if (false) BiomeMakeover.LOGGER.info("[BM_PIECE_TRACE] template={} rot={} bounds={} phase=END thread={} timestamp={} orderIndex={}",
                    diagnosticTemplate, placeSettings.getRotation(), bounds, Thread.currentThread().getName(), System.currentTimeMillis(), order);
            }
            if (isDungeonStructuralTemplate() && level.getLevel() instanceof ServerLevel serverLevel) {
                if (layoutMetadata == null) {
                    String missingKey = serverLevel.dimension().location() + ":" + mansionOrigin + ":" + layoutSignature;
                    if (RUNTIME_REGISTERED.add("missing:" + missingKey)) BiomeMakeover.LOGGER.warn("[BM_MANSION_RUNTIME_METADATA_MISSING] mansionLayoutKey={} template={} ordinal={} -- skipping reconciliation bookkeeping", missingKey, diagnosticTemplate, mansionPieceOrdinal);
                    return;
                }
                String mansionId = serverLevel.dimension().location() + ":" + mansionOrigin + ":" + layoutSignature;
                if (layoutMetadata != null && RUNTIME_REGISTERED.add(mansionId)) {
                    EXPECTED_PIECES.putIfAbsent(mansionId, layoutMetadata.ordinals.size());
                    EXPECTED_ORDINALS.putIfAbsent(mansionId, layoutMetadata.ordinals);
                    EXPECTED_PLACEMENTS.putIfAbsent(mansionId, layoutMetadata.placements);
                    EXPECTED_UNION_SIZES.putIfAbsent(mansionId, layoutMetadata.unionSize);
                    EXPECTED_BOSS_PIECES.putIfAbsent(mansionId, layoutMetadata.bossPieces);
                    PLACED_PLACEMENTS.putIfAbsent(mansionId, java.util.concurrent.ConcurrentHashMap.newKeySet());
                    PLACED_PIECES.putIfAbsent(mansionId, java.util.concurrent.ConcurrentHashMap.newKeySet());
                    if (TRACE) BiomeMakeover.LOGGER.info("[BM_MANSION_RUNTIME_REGISTER] mansionLayoutKey={} structuralPieces={} expectedPlacementCount={} unionPositions={} bossRoomPieces={}", mansionId, layoutMetadata.ordinals.size(), layoutMetadata.placements.size(), layoutMetadata.unionSize, layoutMetadata.bossPieces);
                }
                String pieceId = Integer.toString(mansionPieceOrdinal);
                PLACED_PIECES.computeIfAbsent(mansionId, ignored -> java.util.concurrent.ConcurrentHashMap.newKeySet()).add(pieceId);
                String placementKey = pieceId + ":" + chunkPos.x + ":" + chunkPos.z;
                boolean newAck = PLACED_PLACEMENTS.computeIfAbsent(mansionId, ignored -> java.util.concurrent.ConcurrentHashMap.newKeySet()).add(placementKey);
                if (TRACE) BiomeMakeover.LOGGER.info("[BM_RECONCILE_LIFECYCLE] event=PIECE_PLACED mansionId={} pieceId={} placedCount={} expectedCount={}",
                    mansionId, pieceId, PLACED_PIECES.get(mansionId).size(), EXPECTED_PIECES.getOrDefault(mansionId, -1));
                if (TRACE) BiomeMakeover.LOGGER.info("[BM_RECONCILE_LIFECYCLE] event=CHUNK_PLACED mansionId={} ordinal={} template={} chunk=[{},{}] placedPlacementCount={} expectedPlacementCount={} newAck={}",
                    mansionId, mansionPieceOrdinal, diagnosticTemplate, chunkPos.x, chunkPos.z,
                    PLACED_PLACEMENTS.get(mansionId).size(), EXPECTED_PLACEMENTS.getOrDefault(mansionId, Set.of()).size(), newAck);
                Set<String> expected = EXPECTED_PLACEMENTS.getOrDefault(mansionId, Set.of());
                Set<String> placed = PLACED_PLACEMENTS.getOrDefault(mansionId, Set.of());
                if (placed.size() >= expected.size() && !placed.equals(expected) && COVERAGE_MISMATCH_LOGGED.add(mansionId) && TRACE) {
                    Set<String> missing = new java.util.HashSet<>(expected); missing.removeAll(placed);
                    Set<String> unexpected = new java.util.HashSet<>(placed); unexpected.removeAll(expected);
                    BiomeMakeover.LOGGER.info("[BM_RECONCILE_COVERAGE_MISMATCH] mansionId={} expectedCount={} placedCount={} missingCount={} unexpectedCount={} missing={} unexpected={}",
                        mansionId, expected.size(), placed.size(), missing.size(), unexpected.size(), missing.stream().limit(50).toList(), unexpected.stream().limit(50).toList());
                }
                if (TRACE && mansionPieceOrdinal == 170) BiomeMakeover.LOGGER.info("[BM_PLACEMENT_COVERAGE] ordinal=170 template={} pieceBounds={} expectedChunks={} observedChunks={} missing={} unexpected={}",
                    diagnosticTemplate, getBoundingBox(), expected.stream().filter(k -> k.startsWith("170:")).toList(), placed.stream().filter(k -> k.startsWith("170:")).toList(),
                    expected.stream().filter(k -> k.startsWith("170:") && !placed.contains(k)).toList(), placed.stream().filter(k -> k.startsWith("170:") && !expected.contains(k)).toList());
            }
            if (!TRACE && isDungeonStructuralTemplate() && level.getLevel() instanceof ServerLevel serverLevel) {
                DELAYED_FLUID_TRACES.add(new DelayedFluidTrace(serverLevel, diagnosticTemplate,
                    placeSettings.getRotation(), mansionOrigin, staticAuthoredDry, architecturalInterior, authoredStates, order, layoutSignature));
            }
        }

        private void registerCropTargets(ServerLevel level) {
            if (layoutSignature == null || layoutSignature.isEmpty() || "legacy".equals(layoutSignature)) return;
            String mansion = level.dimension().location() + ":" + mansionOrigin + ":" + layoutSignature;
            Map<BlockPos, BlockState> targets = CROP_TARGETS.computeIfAbsent(mansion, ignored -> new java.util.concurrent.ConcurrentHashMap<>());
            boolean cropTemplate = diagnosticTemplate.contains("room_big_10") || diagnosticTemplate.contains("room_8");
            for (Block crop : new Block[] {Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES, Blocks.BEETROOTS, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM})
                for (var info : template.filterBlocks(templatePosition, placeSettings, crop)) targets.putIfAbsent(info.pos(), info.state());
            if (cropTemplate && !targets.isEmpty()) CROP_EXPECTED_MANSIONS.add(mansion);
        }

        private void traceLootLifecycle(WorldGenLevel level, BoundingBox bounds, String phase, long order) {
            for (var info : template.filterBlocks(templatePosition, placeSettings, BMBlocks.DIRECTIONAL_DATA)) {
                if (info.nbt() != null && info.nbt().getStringOr("metadata", "").startsWith("loot")
                    && info.state().hasProperty(DirectionalBlock.FACING)) {
                    traceLootMarker(level, bounds, info, info.state().getValue(DirectionalBlock.FACING), phase, order);
                }
            }
        }

        private boolean isDungeonStructuralTemplate() {
            return diagnosticTemplate.contains("/dungeon/") || diagnosticTemplate.contains("/boss_room");
        }

        private static final class BossBoundaryTrace {
            private final ServerLevel level;
            private final String mansionId;
            private final String templateName;
            private final int ordinal;
            private final Rotation rotation;
            private final Mirror mirror;
            private final BlockPos templateOrigin;
            private final BoundingBox pieceBounds;
            private final BossGeometry geometry;
            private final Set<BlockPos> explicitAir;
            private final Map<BlockPos, BlockState> serialized;
            private final Set<String> snapshots = new java.util.HashSet<>();
            private final Map<BlockPos, String> readyBoundaryFluids = new HashMap<>();
            private final Map<BlockPos, Direction> readyBoundaryFaces = new HashMap<>();
            private boolean ready;
            private long readyTick = -1;

            private BossBoundaryTrace(ServerLevel level, Piece piece) {
                this.level = level;
                this.mansionId = level.dimension().location() + ":" + piece.mansionOrigin + ":" + piece.layoutSignature;
                this.templateName = piece.diagnosticTemplate;
                this.ordinal = piece.mansionPieceOrdinal;
                this.rotation = piece.placeSettings.getRotation();
                this.mirror = piece.placeSettings.getMirror();
                this.templateOrigin = piece.templatePosition;
                this.pieceBounds = piece.getBoundingBox();
                this.serialized = serializedCells(piece);
                this.geometry = bossGeometry(piece, serialized);
                this.explicitAir = geometry.explicitAir();
                BOSS_GEOMETRIES.putIfAbsent(this.mansionId, geometry);
            }

            private static void register(ServerLevel level, Piece piece) {
                String id = level.dimension().location() + ":" + piece.mansionOrigin + ":" + piece.layoutSignature;
                for (BossBoundaryTrace existing : BOSS_BOUNDARY_TRACES)
                    if (existing.level == level && existing.mansionId.equals(id)) return;
                BossBoundaryTrace trace = new BossBoundaryTrace(level, piece);
                BOSS_BOUNDARY_TRACES.add(trace);
                BiomeMakeover.LOGGER.info("[BM_BOSS_BOUNDARY_IDENTITY] mansionId={} pieceOrdinal={} template={} rotation={} mirror={} templateOrigin={} pieceBounds={} worldMin=[{},{},{}] worldMax=[{},{},{}] explicitAirCount={} solidCount={} authoredWaterCount={}",
                    trace.mansionId, trace.ordinal, trace.templateName, trace.rotation, trace.mirror, trace.templateOrigin, trace.pieceBounds,
                    trace.pieceBounds.minX(), trace.pieceBounds.minY(), trace.pieceBounds.minZ(), trace.pieceBounds.maxX(), trace.pieceBounds.maxY(), trace.pieceBounds.maxZ(),
                    trace.explicitAir.size(), trace.solidCount(), trace.authoredWaterCount());
            }

            private static Map<BlockPos, BlockState> serializedCells(Piece piece) {
                Map<BlockPos, BlockState> cells = new HashMap<>();
                for (Block block : BuiltInRegistries.BLOCK)
                    for (var info : piece.template.filterBlocks(piece.templatePosition, piece.placeSettings, block))
                        cells.put(info.pos(), info.state());
                return cells;
            }

            private static BossGeometry bossGeometry(Piece piece, Map<BlockPos, BlockState> serialized) {
                Set<BlockPos> air = new java.util.HashSet<>(), solid = new java.util.HashSet<>(), water = new java.util.HashSet<>(), other = new java.util.HashSet<>();
                for (var entry : serialized.entrySet()) {
                    if (entry.getValue().isAir()) air.add(entry.getKey());
                    else if (entry.getValue().is(Blocks.STRUCTURE_VOID)) other.add(entry.getKey());
                    else if (entry.getValue().getFluidState().is(Fluids.WATER)) water.add(entry.getKey());
                    else solid.add(entry.getKey());
                }
                return new BossGeometry(serialized, air, solid, water, other, piece.getBoundingBox(), piece.placeSettings.getRotation(), piece.placeSettings.getMirror(), piece.templatePosition);
            }

            private int solidCount() {
                int count = 0;
                for (BlockState state : serialized.values()) if (!state.isAir() && !state.getFluidState().is(Fluids.WATER)) count++;
                return count;
            }

            private int authoredWaterCount() {
                int count = 0;
                for (BlockState state : serialized.values()) if (state.getFluidState().is(Fluids.WATER)) count++;
                return count;
            }

            private String mansionId() { return mansionId; }
            private boolean finished() { return snapshots.contains("D45S"); }
            private void tick(ServerLevel currentLevel) {
                if (!ready || readyTick < 0) return;
                long age = currentLevel.getGameTime() - readyTick;
                if (age >= 2) snapshot("C8");
                if (age >= 400) snapshot("D20S");
                if (age >= 900) snapshot("D45S");
                if (finished()) BOSS_BOUNDARY_TRACES.remove(this);
            }

            private void snapshot(String phase) {
                if (!snapshots.add(phase)) return;
                Set<BlockPos> c8WaterCells = Set.of();
                if ("C8".equals(phase)) {
                    java.util.HashSet<BlockPos> captured = new java.util.HashSet<>();
                    Map<BlockPos, net.minecraft.world.level.material.FluidState> capturedStates = new HashMap<>();
                    for (BlockPos pos : geometry.explicitAir()) { var fluid = level.getFluidState(pos); if (fluid.is(Fluids.WATER)) { captured.add(pos); capturedStates.put(pos, fluid); } }
                    c8WaterCells = Set.copyOf(captured);
                    BOSS_C8_WATER_CELLS.put(mansionId, c8WaterCells);
                    BOSS_C8_WATER_STATES.put(mansionId, Map.copyOf(capturedStates));
                }
                if ("D20S".equals(phase)) BiomeMakeover.LOGGER.info("[BM_BOSS_BOUNDARY_SNAPSHOT] phase=D20S mansionId={}", mansionId);
                if ("D45S".equals(phase)) BiomeMakeover.LOGGER.info("[BM_BOSS_BOUNDARY_SNAPSHOT] phase=D45S mansionId={}", mansionId);
                Map<Direction, Integer> source = new HashMap<>(), air = new HashMap<>();
                for (Direction direction : Direction.values()) { source.put(direction, 0); air.put(direction, 0); }
                int boundary = 0, bossSolid = 0, otherMansion = 0, naturalSolid = 0, externalAir = 0, sourceWater = 0, flowing = 0, waterlogged = 0, unknown = 0;
                int opening = 0, openingMansion = 0, openingAir = 0, openingSource = 0, openingFlowing = 0;
                for (BlockPos bossAir : explicitAir) for (Direction direction : Direction.values()) {
                    BlockPos outside = bossAir.relative(direction);
                    if (explicitAir.contains(outside)) continue;
                    boundary++;
                    BlockState state = level.getBlockState(outside);
                    var fluid = level.getFluidState(outside);
                    if ("READY".equals(phase)) {
                        readyBoundaryFluids.put(outside, fluid.toString());
                        readyBoundaryFaces.put(outside, direction);
                    }
                    boolean mansion = otherMansionPosition(outside);
                    String category;
                    if (serialized.containsKey(outside) && !serialized.get(outside).isAir()) { category = "BOSS_SOLID"; bossSolid++; }
                    else if (mansion) { category = "OTHER_MANSION_BLOCK"; otherMansion++; }
                    else if (fluid.is(Fluids.WATER) && fluid.isSource()) { category = "SOURCE_WATER"; sourceWater++; source.merge(direction, 1, Integer::sum); if ("READY".equals(phase)) emitWater(phase, bossAir, outside, direction, state, true, opening); }
                    else if (fluid.is(Fluids.WATER)) { category = "FLOWING_WATER"; flowing++; openingFlowing++; }
                    else if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)) { category = "WATERLOGGED_BLOCK"; waterlogged++; }
                    else if (state.isAir()) { category = "EXTERNAL_AIR"; externalAir++; air.merge(direction, 1, Integer::sum); }
                    else { category = "NATURAL_SOLID"; naturalSolid++; }
                    if (mansion || category.equals("EXTERNAL_AIR") || category.equals("SOURCE_WATER") || category.equals("FLOWING_WATER")) {
                        opening++;
                        if (mansion) openingMansion++;
                        if (category.equals("EXTERNAL_AIR")) openingAir++;
                        if (category.equals("SOURCE_WATER")) openingSource++;
                    }
                }
                BiomeMakeover.LOGGER.info("[BM_BOSS_BOUNDARY_SUMMARY] mansionId={} phase={} rotation={} explicitAirCells={} boundaryFaces={} bossSolidFaces={} otherMansionFaces={} naturalSolidFaces={} externalAirFaces={} sourceWaterFaces={} flowingWaterFaces={} waterloggedFaces={} unknownFaces={} northSource={} southSource={} eastSource={} westSource={} upSource={} downSource={} northExternalAir={} southExternalAir={} eastExternalAir={} westExternalAir={} upExternalAir={} downExternalAir={}",
                    mansionId, phase, rotation, explicitAir.size(), boundary, bossSolid, otherMansion, naturalSolid, externalAir, sourceWater, flowing, waterlogged, unknown,
                    source.get(Direction.NORTH), source.get(Direction.SOUTH), source.get(Direction.EAST), source.get(Direction.WEST), source.get(Direction.UP), source.get(Direction.DOWN),
                    air.get(Direction.NORTH), air.get(Direction.SOUTH), air.get(Direction.EAST), air.get(Direction.WEST), air.get(Direction.UP), air.get(Direction.DOWN));
                if ("READY".equals(phase)) {
                    BiomeMakeover.LOGGER.info("[BM_BOSS_OPENING_SUMMARY] mansionId={} rotation={} openingFaces={} openingToMansion={} openingToNaturalAir={} openingToSourceWater={} openingToFlowingWater={}",
                        mansionId, rotation, opening, openingMansion, openingAir, openingSource, openingFlowing);
                }
                if ("C8".equals(phase)) { emitGeometryCompare(); emitWaterCellCompare(c8WaterCells, BOSS_C8_WATER_STATES.get(mansionId)); emitTraceSeedCheck(c8WaterCells); emitSourceCellClassify(); }
                if ("READY".equals(phase) || "D20S".equals(phase) || "D45S".equals(phase)) proximity(phase);
                if ("C5".equals(phase) || "C6".equals(phase) || "C8".equals(phase) || "D20S".equals(phase) || "D45S".equals(phase)) waterComponentTrace(phase, "C8".equals(phase) ? c8WaterCells : null);
                if ("C8".equals(phase) || "D20S".equals(phase)) {
                    for (var entry : readyBoundaryFluids.entrySet()) {
                        var current = level.getFluidState(entry.getKey()).toString();
                        if (entry.getValue().equals(current) || level.getFluidState(entry.getKey()).isEmpty()) continue;
                        BiomeMakeover.LOGGER.info("[BM_BOSS_BOUNDARY_CHANGE] mansionId={} outsidePos={} face={} readyFluid={} c8Fluid={}",
                            mansionId, entry.getKey(), readyBoundaryFaces.get(entry.getKey()), entry.getValue(), current);
                    }
                }
            }

            private void emitGeometryCompare() {
                Set<BlockPos> legacy = legacyBossAir(level, mansionId), intersection = new java.util.HashSet<>(geometry.explicitAir());
                intersection.retainAll(legacy);
                Set<BlockPos> canonicalOnly = new java.util.HashSet<>(geometry.explicitAir()); canonicalOnly.removeAll(legacy);
                Set<BlockPos> legacyOnly = new java.util.HashSet<>(legacy); legacyOnly.removeAll(geometry.explicitAir());
                BiomeMakeover.LOGGER.info("[BM_BOSS_GEOMETRY_COMPARE] mansionId={} rotation={} canonicalAir={} legacyAuditAir={} intersection={} canonicalOnly={} legacyOnly={} canonicalHash={} legacyHash={}",
                    mansionId, rotation, geometry.explicitAir().size(), legacy.size(), intersection.size(), canonicalOnly.size(), legacyOnly.size(),
                    Integer.toHexString(geometry.explicitAir().hashCode()), Integer.toHexString(legacy.hashCode()));
            }

            private void emitWaterCellCompare(Set<BlockPos> waterCells, Map<BlockPos, net.minecraft.world.level.material.FluidState> capturedStates) {
                int emitted = 0;
                for (BlockPos pos : waterCells) {
                    var fluid = capturedStates.getOrDefault(pos, Fluids.EMPTY.defaultFluidState());
                    if (emitted++ >= 32) continue;
                    BlockState canonical = geometry.serialized().get(pos);
                    BiomeMakeover.LOGGER.info("[BM_BOSS_WATER_CELL_COMPARE] mansionId={} worldPos={} localPos={} runtimeBlock={} fluidSource={} fluidAmount={} inCanonicalAir={} inLegacyAuditAir={} canonicalTemplateState={} canonicalClassification={} insidePieceBounds={} distanceToPieceEdge={}",
                        mansionId, pos, pos.subtract(templateOrigin), level.getBlockState(pos), fluid.isSource(), fluid.getAmount(), geometry.explicitAir().contains(pos), true,
                        canonical, classifyCanonical(pos), pieceBounds.isInside(pos), distanceToPieceEdge(pos));
                }
            }

            private void emitTraceSeedCheck(Set<BlockPos> audit) {
                Set<BlockPos> seeds = Set.copyOf(audit);
                Set<BlockPos> missing = new java.util.HashSet<>(audit); missing.removeAll(seeds);
                Set<BlockPos> extra = new java.util.HashSet<>(seeds); extra.removeAll(audit);
                BiomeMakeover.LOGGER.info("[BM_BOSS_TRACE_SEED_CHECK] mansionId={} auditWaterCells={} traceSeedCells={} missingFromTrace={} extraInTrace={}", mansionId, audit.size(), seeds.size(), missing.size(), extra.size());
            }

            private void emitSourceCellClassify() {
                Set<BlockPos> sources = new java.util.HashSet<>();
                for (BlockPos air : geometry.explicitAir()) for (Direction direction : Direction.values()) {
                    BlockPos one = air.relative(direction), two = one.relative(direction);
                    if (level.getFluidState(one).is(Fluids.WATER) && level.getFluidState(one).isSource()) sources.add(one);
                    if (level.getFluidState(two).is(Fluids.WATER) && level.getFluidState(two).isSource()) sources.add(two);
                }
                int emitted = 0;
                for (BlockPos pos : sources) if (emitted++ < 32) {
                    BlockState state = geometry.serialized().get(pos);
                    BiomeMakeover.LOGGER.info("[BM_BOSS_SOURCE_CELL_CLASSIFY] mansionId={} worldPos={} localPos={} insidePieceBounds={} canonicalTemplateState={} classification={} inExplicitAir={} inSolid={} inAuthoredWater={} inOther={} runtimeBlock={} runtimeFluid={} source={}",
                        mansionId, pos, pos.subtract(templateOrigin), pieceBounds.isInside(pos), state, classifyCanonical(pos), geometry.explicitAir().contains(pos), geometry.solid().contains(pos),
                        geometry.authoredWater().contains(pos), geometry.otherSerialized().contains(pos), level.getBlockState(pos), level.getFluidState(pos), level.getFluidState(pos).isSource());
                }
            }

            private String classifyCanonical(BlockPos pos) {
                BlockState state = geometry.serialized().get(pos);
                if (geometry.explicitAir().contains(pos)) return "EXPLICIT_AIR";
                if (geometry.solid().contains(pos)) return "AUTHORED_SOLID";
                if (geometry.authoredWater().contains(pos)) return "AUTHORED_WATER";
                if (geometry.otherSerialized().contains(pos)) return "STRUCTURE_VOID";
                return state == null ? "OMITTED_OR_UNSERIALIZED" : "OTHER_SERIALIZED";
            }

            private int distanceToPieceEdge(BlockPos pos) {
                return Math.min(Math.min(pos.getX() - pieceBounds.minX(), pieceBounds.maxX() - pos.getX()),
                    Math.min(Math.min(pos.getY() - pieceBounds.minY(), pieceBounds.maxY() - pos.getY()), Math.min(pos.getZ() - pieceBounds.minZ(), pieceBounds.maxZ() - pos.getZ())));
            }

            private void emitWater(String phase, BlockPos bossAir, BlockPos outside, Direction face, BlockState state, boolean source, int opening) {
                BiomeMakeover.LOGGER.info("[BM_BOSS_BOUNDARY_WATER] mansionId={} phase={} bossAirPos={} outsidePos={} face={} outsideState={} fluidSource={} distanceFromBossDoor={} nearestBossOpening={}",
                    mansionId, phase, bossAir, outside, face, state, source, "UNKNOWN", "UNKNOWN");
            }

            private void proximity(String phase) {
                Set<BlockPos> seen1 = new java.util.HashSet<>(), seen2 = new java.util.HashSet<>();
                BlockPos nearest = null; int source1 = 0, source2 = 0, flowing1 = 0, flowing2 = 0; double nearestDistance = Double.POSITIVE_INFINITY; Direction nearestDirection = null;
                int direct = (int) boundaryFaces().stream().filter(face -> "SOURCE_WATER".equals(face.category())).count();
                for (BlockPos p : explicitAir) for (Direction d : Direction.values()) {
                    BlockPos p1 = p.relative(d); if (explicitAir.contains(p1)) continue;
                    var f1 = level.getFluidState(p1); if (f1.is(Fluids.WATER)) { seen1.add(p1); if (f1.isSource()) source1++; else flowing1++; if (nearest == null) { nearest=p1; nearestDistance=1; nearestDirection=d; } }
                    BlockPos p2 = p1.relative(d); if (explicitAir.contains(p2)) continue;
                    var f2 = level.getFluidState(p2); if (f2.is(Fluids.WATER)) { seen2.add(p2); if (f2.isSource()) source2++; else flowing2++; if (nearest == null) { nearest=p2; nearestDistance=2; nearestDirection=d; } }
                }
                BiomeMakeover.LOGGER.info("[BM_BOSS_WATER_PROXIMITY] mansionId={} phase={} directSourceFaces={} sourceWithin1={} sourceWithin2={} flowingWithin1={} flowingWithin2={} nearestSourceDistance={} nearestSourcePos={} nearestSourceDirection={}",
                    mansionId, phase, direct, seen1.stream().filter(p -> level.getFluidState(p).isSource()).count(), seen2.stream().filter(p -> level.getFluidState(p).isSource()).count(), flowing1, flowing2,
                    Double.isInfinite(nearestDistance) ? -1 : nearestDistance, nearest, nearestDirection);
            }

            private void waterComponentTrace(String phase, Set<BlockPos> capturedSeeds) {
                long started = System.nanoTime();
                BoundingBox expanded = new BoundingBox(pieceBounds.minX() - 4, pieceBounds.minY() - 4, pieceBounds.minZ() - 4,
                    pieceBounds.maxX() + 4, pieceBounds.maxY() + 4, pieceBounds.maxZ() + 4);
                Set<BlockPos> visited = new java.util.HashSet<>();
                List<WaterComponent> components = new java.util.ArrayList<>();
                Set<BlockPos> seeds = capturedSeeds == null ? currentWaterSeeds() : capturedSeeds;
                for (BlockPos seed : seeds) {
                    if (!expanded.isInside(seed) || visited.contains(seed) || !level.getFluidState(seed).is(Fluids.WATER)) continue;
                    WaterComponent component = traceWaterComponent(seed, expanded, visited);
                    components.add(component);
                }
                int withSource = 0; WaterComponent nearest = null;
                for (WaterComponent component : components) if (component.sourceFound) { withSource++; if (nearest == null || component.pathLength < nearest.pathLength) nearest = component; }
                BiomeMakeover.LOGGER.info("[BM_BOSS_WATER_COMPONENT_SUMMARY] mansionId={} phase={} rotation={} interiorWaterCells={} componentCount={} componentsWithSource={} nearestSourceDistance={} nearestSource={} entryPos={} entryOutsidePos={} entryFace={} pathLength={}",
                    mansionId, phase, rotation, components.stream().mapToInt(c -> c.interiorCellCount).sum(), components.size(), withSource,
                    nearest == null ? -1 : nearest.pathLength, nearest == null ? null : nearest.sourcePos, nearest == null ? null : nearest.entryBossAir,
                    nearest == null ? null : nearest.entryOutside, nearest == null ? null : nearest.entryFace, nearest == null ? -1 : nearest.pathLength);
                int emitted = 0;
                for (WaterComponent component : components) if (emitted++ < 8)
                    BiomeMakeover.LOGGER.info("[BM_BOSS_WATER_COMPONENT] mansionId={} phase={} component={} interiorSeed={} interiorCellCount={} fluidCellCount={} sourceFound={} sourcePos={} entryBossAir={} entryOutside={} entryFace={} pathLength={}",
                        mansionId, phase, emitted, component.seed, component.interiorCellCount, component.fluidCellCount, component.sourceFound, component.sourcePos,
                        component.entryBossAir, component.entryOutside, component.entryFace, component.pathLength);
                BiomeMakeover.LOGGER.info("[BM_BOSS_TRACE_PERF] mansionId={} phase={} visitedFluidCells={} components={} elapsedMicros={}",
                    mansionId, phase, visited.size(), components.size(), (System.nanoTime() - started) / 1000L);
            }

            private Set<BlockPos> currentWaterSeeds() {
                Set<BlockPos> result = new java.util.HashSet<>();
                for (BlockPos pos : explicitAir) if (level.getFluidState(pos).is(Fluids.WATER)) result.add(pos);
                return result;
            }

            private WaterComponent traceWaterComponent(BlockPos seed, BoundingBox expanded, Set<BlockPos> visited) {
                java.util.ArrayDeque<BlockPos> queue = new java.util.ArrayDeque<>();
                Map<BlockPos, Integer> distances = new HashMap<>();
                queue.add(seed); visited.add(seed); distances.put(seed, 0);
                int interior = 0; BlockPos source = null, entryBossAir = null, entryOutside = null; Direction entryFace = null; int pathLength = -1;
                while (!queue.isEmpty()) {
                    BlockPos current = queue.remove(); int distance = distances.get(current);
                    if (explicitAir.contains(current)) interior++;
                    if (source == null && level.getFluidState(current).isSource()) { source = current; pathLength = distance; }
                    for (Direction direction : Direction.values()) {
                        BlockPos next = current.relative(direction);
                        if (explicitAir.contains(current) && !explicitAir.contains(next) && expanded.isInside(next) && level.getFluidState(next).is(Fluids.WATER) && entryOutside == null) {
                            entryBossAir = current; entryOutside = next; entryFace = direction;
                        }
                        if (!expanded.isInside(next) || visited.contains(next) || !level.getFluidState(next).is(Fluids.WATER)) continue;
                        visited.add(next); distances.put(next, distance + 1); queue.add(next);
                    }
                }
                return new WaterComponent(seed, interior, distances.size(), source != null, source, entryBossAir, entryOutside, entryFace, pathLength);
            }

            private record WaterComponent(BlockPos seed, int interiorCellCount, int fluidCellCount, boolean sourceFound, BlockPos sourcePos,
                                          BlockPos entryBossAir, BlockPos entryOutside, Direction entryFace, int pathLength) {
            }

            private List<BoundaryFace> boundaryFaces() {
                List<BoundaryFace> faces = new java.util.ArrayList<>();
                for (BlockPos bossAir : explicitAir) for (Direction direction : Direction.values()) {
                    BlockPos outside = bossAir.relative(direction);
                    if (explicitAir.contains(outside)) continue;
                    faces.add(new BoundaryFace(bossAir, outside, direction, boundaryCategory(outside)));
                }
                return faces;
            }

            private String boundaryCategory(BlockPos outside) {
                if (serialized.containsKey(outside) && !serialized.get(outside).isAir()) return "BOSS_SOLID";
                if (otherMansionPosition(outside)) return "OTHER_MANSION_BLOCK";
                var fluid = level.getFluidState(outside);
                BlockState state = level.getBlockState(outside);
                if (fluid.is(Fluids.WATER) && fluid.isSource()) return "SOURCE_WATER";
                if (fluid.is(Fluids.WATER)) return "FLOWING_WATER";
                if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)) return "WATERLOGGED_BLOCK";
                if (state.isAir()) return "EXTERNAL_AIR";
                return "NATURAL_SOLID";
            }

            private record BoundaryFace(BlockPos bossAir, BlockPos outside, Direction face, String category) {}

            private boolean otherMansionPosition(BlockPos pos) {
                for (EnvelopePiece piece : DUNGEON_ENVELOPE) if (piece.level() == level && piece.architecturalInterior().contains(pos)) return true;
                return false;
            }
        }


        private void traceLootMarker(WorldGenLevel level, BoundingBox bounds, StructureTemplate.StructureBlockInfo info,
                                     Direction facing, String phase, long order) {
            BlockPos target = info.pos().relative(facing);
            BlockEntity be = level.getBlockEntity(target);
            BlockState targetState = level.getBlockState(target);
            String loot = be instanceof RandomizableContainerBlockEntity container && container.getLootTable() != null
                ? container.getLootTable().location().toString() : "null";
            BiomeMakeover.LOGGER.info("[BM_LOOT_LIFECYCLE] template={} rotation={} markerLocal={} markerWorld={} facing={} target={} phase={} block={} blockEntity={} lootTablePresent={} lootTableId={} inPieceBounds={} owningPieceIfKnown={} orderIndex={}",
                diagnosticTemplate, placeSettings.getRotation(), info.pos().subtract(templatePosition), info.pos(), facing, target, phase,
                targetState.getBlock(), be == null ? "null" : be.getClass().getSimpleName(), !loot.equals("null"), loot,
                bounds.isInside(target), diagnosticTemplate, order);
        }

        private void traceFences(WorldGenLevel level, BoundingBox bounds, String phase, long order) {
            for (var type : new Block[] {Blocks.DARK_OAK_FENCE, BMBlocks.ANCIENT_OAK_FENCE}) {
                for (var info : template.filterBlocks(templatePosition, placeSettings, type)) {
                    BlockState state = level.getBlockState(info.pos());
                    BiomeMakeover.LOGGER.info("[BM_FENCE_LIFECYCLE] template={} rotation={} pos={} phase={} serialized={} runtime={} neighbors=N:{} E:{} S:{} W:{} inPieceBounds={} orderIndex={}",
                        diagnosticTemplate, placeSettings.getRotation(), info.pos(), phase, info.state(), state,
                        level.getBlockState(info.pos().north()).getBlock(), level.getBlockState(info.pos().east()).getBlock(),
                        level.getBlockState(info.pos().south()).getBlock(), level.getBlockState(info.pos().west()).getBlock(),
                        bounds.isInside(info.pos()), order);
                }
            }
        }

        private void traceFluids(WorldGenLevel level, BoundingBox bounds, String phase, long order) {
            if (!ARCHAEology_TRACE) return;
            int water = 0;
            BoundingBox sample = new BoundingBox(bounds.minX() - 1, bounds.minY() - 1, bounds.minZ() - 1,
                bounds.maxX() + 1, bounds.maxY() + 1, bounds.maxZ() + 1);
            for (int x = sample.minX(); x <= sample.maxX(); x++) for (int y = sample.minY(); y <= sample.maxY(); y++)
                for (int z = sample.minZ(); z <= sample.maxZ(); z++)
                    if (level.getFluidState(new BlockPos(x, y, z)).is(Fluids.WATER)) water++;
            BiomeMakeover.LOGGER.info("[BM_FLUID_LIFECYCLE] template={} rotation={} bounds={} phase={} waterBlocks={} orderIndex={}",
                diagnosticTemplate, placeSettings.getRotation(), sample, phase, water, order);
        }

        /**
         * Template-aware fluid probe. Unlike the historical bounding-box trace,
         * this only samples positions explicitly authored as air/water by the
         * dungeon template (plus its serialized waterlogged states). It is
         * diagnostics-only and never mutates world state.
         */
        private void traceFluidInterior(WorldGenLevel level, String phase, long order) {
            if (!ARCHAEology_TRACE) return;
            if (!isDungeonStructuralTemplate()) return;
            int authoredDry = 0, waterInDry = 0, sourceInDry = 0, flowingInDry = 0;
            int authoredWater = 0, wetWaterloggable = 0, dryWaterloggable = 0, openings = 0;
            int positionSamples = 0;
            for (Block block : new Block[] {Blocks.AIR, Blocks.CAVE_AIR}) {
                for (var info : template.filterBlocks(templatePosition, placeSettings, block)) {
                    authoredDry++;
                    var fluid = level.getFluidState(info.pos());
                    if (fluid.is(Fluids.WATER)) {
                        waterInDry++;
                        if (fluid.isSource()) sourceInDry++; else flowingInDry++;
                        if (positionSamples++ < 12) {
                            BiomeMakeover.LOGGER.info("[BM_FLUID_POSITION] template={} local={} world={} authoredState={} runtimeState={} fluidState={} phase={} rotation={}",
                                diagnosticTemplate, info.pos().subtract(templatePosition), info.pos(), info.state(), level.getBlockState(info.pos()), fluid, phase, placeSettings.getRotation());
                        }
                    }
                }
            }
            for (var info : template.filterBlocks(templatePosition, placeSettings, Blocks.WATER)) authoredWater++;
            Block[] waterloggable = {Blocks.OAK_STAIRS, Blocks.OAK_SLAB, Blocks.OAK_FENCE, Blocks.DARK_OAK_FENCE,
                Blocks.IRON_BARS, Blocks.OAK_TRAPDOOR, Blocks.COBBLESTONE_WALL, BMBlocks.ANCIENT_OAK_FENCE};
            for (Block block : waterloggable) {
                for (var info : template.filterBlocks(templatePosition, placeSettings, block)) {
                    if (!info.state().hasProperty(BlockStateProperties.WATERLOGGED)) continue;
                    if (info.state().getValue(BlockStateProperties.WATERLOGGED)) wetWaterloggable++; else dryWaterloggable++;
                }
            }
            BiomeMakeover.LOGGER.info("[BM_FLUID_INTERIOR] template={} rotation={} phase={} authoredDryPositions={} waterInAuthoredDry={} sourceWaterInAuthoredDry={} flowingWaterInAuthoredDry={} wetWaterloggableBlocks={} dryWaterloggableBlocks={} authoredWaterPositions={} waterAtIntentionalOpenings={} orderIndex={}",
                diagnosticTemplate, placeSettings.getRotation(), phase, authoredDry, waterInDry, sourceInDry, flowingInDry,
                wetWaterloggable, dryWaterloggable, authoredWater, openings, order);
        }

        private List<BlockPos> dungeonAuthoredDryPositions() {
            if (!isDungeonStructuralTemplate()) return List.of();
            List<BlockPos> positions = new java.util.ArrayList<>();
            for (Block block : new Block[] {Blocks.AIR, Blocks.CAVE_AIR}) {
                for (var info : template.filterBlocks(templatePosition, placeSettings, block)) positions.add(info.pos());
            }
            return positions;
        }

        private Map<BlockPos, BlockState> dungeonAuthoredStates() {
            if (!isDungeonStructuralTemplate()) return Map.of();
            Map<BlockPos, BlockState> states = new HashMap<>();
            for (Block block : BuiltInRegistries.BLOCK) {
                for (var info : template.filterBlocks(templatePosition, placeSettings, block)) states.put(info.pos(), info.state());
            }
            return states;
        }

        private void traceWaterlogTransitions(WorldGenLevel level, Map<BlockPos, BlockState> authoredStates, String phase) {
            if (!ARCHAEology_TRACE) return;
            int count = 0;
            for (var entry : authoredStates.entrySet()) {
                BlockState authored = entry.getValue();
                if (!authored.hasProperty(BlockStateProperties.WATERLOGGED) || count++ >= 24) continue;
                BlockState runtime = level.getBlockState(entry.getKey());
                BiomeMakeover.LOGGER.info("[BM_WATERLOG_TRANSITION] template={} local={} world={} block={} P0={} {}={} phase={} runtimeState={} neighborFluids={} envelopeClassification={}",
                    diagnosticTemplate, entry.getKey().subtract(templatePosition), entry.getKey(), authored.getBlock(), authored,
                    "waterlogged", authored.getValue(BlockStateProperties.WATERLOGGED), phase, runtime,
                    level.getFluidState(entry.getKey()), "piece-local");
            }
        }

        private void correctReleasedFluidStateForCurrentClip(WorldGenLevel level, Map<BlockPos, BlockState> authoredStates, BoundingBox clip) {
            int airChecked = 0, airRemoved = 0, dryChecked = 0, dryRestored = 0, wetPreserved = 0;
            for (var entry : authoredStates.entrySet()) {
                BlockState authored = entry.getValue();
                BlockPos pos = entry.getKey();
                if (!clip.isInside(pos)) continue;
                if (authored.isAir()) {
                    airChecked++;
                    if (!level.getFluidState(pos).isEmpty()) { level.setBlock(pos, authored, 3); airRemoved++; }
                    continue;
                }
                if (!authored.hasProperty(BlockStateProperties.WATERLOGGED)) continue;
                if (authored.getValue(BlockStateProperties.WATERLOGGED)) { wetPreserved++; continue; }
                dryChecked++;
                BlockState runtime = level.getBlockState(pos);
                if (runtime.is(authored.getBlock()) && runtime.hasProperty(BlockStateProperties.WATERLOGGED)
                    && runtime.getValue(BlockStateProperties.WATERLOGGED)) { level.setBlock(pos, authored, 3); dryRestored++; }
            }
            Set<BlockPos> closedSources = new java.util.HashSet<>();
            Map<String, Integer> replacementHistogram = new HashMap<>();
            int candidates = 0, dryAirRestored = 0, dryWaterloggableRestored = 0, solidRestored = 0, authoredWet = 0, voidSkipped = 0;
            for (var entry : authoredStates.entrySet()) {
                BlockState authored = entry.getValue();
                BlockPos dryPos = entry.getKey();
                if (!clip.isInside(dryPos) || !(authored.isAir() || (authored.hasProperty(BlockStateProperties.WATERLOGGED)
                    && !authored.getValue(BlockStateProperties.WATERLOGGED)))) continue;
                for (Direction face : Direction.values()) {
                    BlockPos sourcePos = dryPos.relative(face);
                    if (authoredStates.containsKey(sourcePos)) continue;
                    var fluid = level.getFluidState(sourcePos);
                    if (!fluid.is(Fluids.WATER) || !fluid.isSource()) continue;
                    candidates++;
                    if (closedSources.contains(sourcePos)) continue;
                    BlockState replacement = naturalClosureState(level, sourcePos, authoredStates);
                    if (replacement == null) continue;
                    closedSources.add(sourcePos);
                    level.setBlock(sourcePos, replacement, 3);
                    replacementHistogram.merge(replacement.getBlock().builtInRegistryHolder().key().location().toString(), 1, Integer::sum);
                    if (TRACE) BiomeMakeover.LOGGER.info("[BM_FLUID_SOURCE_BOUNDARY] mansionId={} ordinal={} template={} dryPos={} sourcePos={} face={} sourceOwnedByOtherPiece=false classification=EXTERNAL_AQUIFER_SOURCE",
                        level.getLevel().dimension().location() + ":" + mansionOrigin, mansionPieceOrdinal, diagnosticTemplate, dryPos, sourcePos, face);
                }
            }
            for (var entry : authoredStates.entrySet()) {
                if (!clip.isInside(entry.getKey())) continue;
                BlockState authored = entry.getValue();
                if (authored.isAir() && !level.getFluidState(entry.getKey()).isEmpty()) level.setBlock(entry.getKey(), authored, 3);
                else if (authored.hasProperty(BlockStateProperties.WATERLOGGED) && !authored.getValue(BlockStateProperties.WATERLOGGED)
                    && level.getBlockState(entry.getKey()).hasProperty(BlockStateProperties.WATERLOGGED)
                    && level.getBlockState(entry.getKey()).getValue(BlockStateProperties.WATERLOGGED)) level.setBlock(entry.getKey(), authored, 3);
            }
            // [BM_FLUID_SOURCE_CLOSURE] R17N baseline metrics.
            if (TRACE) BiomeMakeover.LOGGER.info("[BM_FLUID_SOURCE_CLOSURE] mansionId={} ordinal={} template={} chunk={} candidateSources={} internalOwnedSkipped={} intentionalOpeningSkipped={} externalSourcesClosed={} replacementHistogram={}",
                level.getLevel().dimension().location() + ":" + mansionOrigin, mansionPieceOrdinal, diagnosticTemplate,
                ((clip.minX() >> 4) + "," + (clip.minZ() >> 4)), candidates, 0, 0, closedSources.size(), replacementHistogram);
            if (TRACE) BiomeMakeover.LOGGER.info("[BM_PLACEMENT_FLUID_FIX] mansionId={} ordinal={} template={} chunk={} explicitAirChecked={} waterRemovedFromExplicitAir={} dryWaterloggableChecked={} waterloggedFalseRestored={} authoredWetPreserved={}",
                level.getLevel().dimension().location() + ":" + mansionOrigin, mansionPieceOrdinal, diagnosticTemplate,
                ((clip.minX() >> 4) + "," + (clip.minZ() >> 4)), airChecked, airRemoved, dryChecked, dryRestored, wetPreserved);
        }

        private BlockState naturalClosureState(WorldGenLevel level, BlockPos sourcePos, Map<BlockPos, BlockState> authoredStates) {
            Map<Block, Integer> counts = new HashMap<>();
            for (Direction direction : Direction.values()) {
                BlockPos candidatePos = sourcePos.relative(direction);
                if (authoredStates.containsKey(candidatePos) || level.getBlockEntity(candidatePos) != null) continue;
                BlockState candidate = level.getBlockState(candidatePos);
                if (!candidate.getFluidState().isEmpty() || !candidate.isCollisionShapeFullBlock(level, candidatePos)
                    || candidate.getBlock().builtInRegistryHolder().key().location().getNamespace().equals("biomemakeover")) continue;
                counts.merge(candidate.getBlock(), 1, Integer::sum);
            }
            Block selected = counts.entrySet().stream().max(Map.Entry.<Block, Integer>comparingByValue()
                .thenComparing(entry -> entry.getKey().builtInRegistryHolder().key().location().toString(), Comparator.reverseOrder()))
                .map(Map.Entry::getKey).orElse(Blocks.STONE);
            return selected.defaultBlockState();
        }

        private void clearBossRoomAuthoredAir(WorldGenLevel level, Map<BlockPos, BlockState> authoredStates, BoundingBox clip) {
            int checked = 0, cleared = 0;
            for (var entry : authoredStates.entrySet()) {
                if (!clip.isInside(entry.getKey()) || !entry.getValue().isAir()) continue;
                checked++;
                if (!level.getFluidState(entry.getKey()).isEmpty()) {
                    level.setBlock(entry.getKey(), entry.getValue(), 3);
                    cleared++;
                }
            }
            if (TRACE) BiomeMakeover.LOGGER.info("[BM_BOSS_ROOM_FLUID] template={} explicitAirChecked={} explicitAirCleared={} clip={}", diagnosticTemplate, checked, cleared, clip);
        }

        private void traceCrops(WorldGenLevel level, String phase) {
            // BM_CROP_TRACE is retained as the historical diagnostic name; runtime output is bounded.
            for (Block crop : new Block[] {Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES, Blocks.BEETROOTS,
                Blocks.MELON_STEM, Blocks.PUMPKIN_STEM}) {
                for (var info : template.filterBlocks(templatePosition, placeSettings, crop)) {
                    BlockPos support = info.pos().below();
                    BiomeMakeover.LOGGER.info("[BM_CROP_RUNTIME] template={} localPos={} worldPos={} serializedState={} phase={} runtimeState={} supportState={} inClip={}",
                        diagnosticTemplate, info.pos().subtract(templatePosition), info.pos(), info.state(), phase,
                        level.getBlockState(info.pos()), level.getBlockState(support), true);
                }
            }
        }

        private void restoreSerializedCrops(WorldGenLevel level, BoundingBox clip) {
            if (!isDungeonStructuralTemplate()) return;
            int restored = 0;
            for (Block crop : new Block[] {Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES, Blocks.BEETROOTS,
                Blocks.MELON_STEM, Blocks.PUMPKIN_STEM, Blocks.ATTACHED_MELON_STEM, Blocks.ATTACHED_PUMPKIN_STEM}) {
                for (var info : template.filterBlocks(templatePosition, placeSettings, crop)) {
                    if (!clip.isInside(info.pos())) continue;
                    BlockState runtime = level.getBlockState(info.pos());
                    if (!runtime.equals(info.state()) && level.setBlock(info.pos(), info.state(), 2)) restored++;
                }
            }
            if (TRACE && restored > 0) BiomeMakeover.LOGGER.info("[BM_CROP_PARITY] template={} restoredSerializedCrops={} clip={}", diagnosticTemplate, restored, clip);
        }

        private List<BlockPos> dungeonArchitecturalInterior(BoundingBox bounds) {
            if (!isDungeonStructuralTemplate()) return List.of();
            java.util.Set<BlockPos> solid = new java.util.HashSet<>();
            for (Block block : BuiltInRegistries.BLOCK) {
                for (var info : template.filterBlocks(templatePosition, placeSettings, block)) {
                    BlockState state = info.state();
                    if (!state.isAir() && !state.is(Blocks.STRUCTURE_VOID) && !state.getFluidState().is(Fluids.WATER)) solid.add(info.pos());
                }
            }
            java.util.Set<BlockPos> exterior = new java.util.HashSet<>();
            java.util.ArrayDeque<BlockPos> queue = new java.util.ArrayDeque<>();
            for (int x = bounds.minX(); x <= bounds.maxX(); x++) for (int y = bounds.minY(); y <= bounds.maxY(); y++) for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                if (x != bounds.minX() && x != bounds.maxX() && y != bounds.minY() && y != bounds.maxY() && z != bounds.minZ() && z != bounds.maxZ()) continue;
                BlockPos p = new BlockPos(x, y, z);
                if (!solid.contains(p) && exterior.add(p)) queue.add(p);
            }
            while (!queue.isEmpty()) {
                BlockPos p = queue.remove();
                for (Direction direction : Direction.values()) {
                    BlockPos next = p.relative(direction);
                    if (next.getX() < bounds.minX() || next.getX() > bounds.maxX() || next.getY() < bounds.minY() || next.getY() > bounds.maxY() || next.getZ() < bounds.minZ() || next.getZ() > bounds.maxZ()) continue;
                    if (!solid.contains(next) && exterior.add(next)) queue.add(next);
                }
            }
            List<BlockPos> interior = new java.util.ArrayList<>();
            for (int x = bounds.minX(); x <= bounds.maxX(); x++) for (int y = bounds.minY(); y <= bounds.maxY(); y++) for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                BlockPos p = new BlockPos(x, y, z);
                if (!solid.contains(p) && !exterior.contains(p)) interior.add(p);
            }
            return interior;
        }

        private static String neighborSummary(WorldGenLevel level, BlockPos pos) {
            return "N=" + level.getBlockState(pos.north()).getBlock()
                + ",E=" + level.getBlockState(pos.east()).getBlock()
                + ",S=" + level.getBlockState(pos.south()).getBlock()
                + ",W=" + level.getBlockState(pos.west()).getBlock();
        }

        private void handleDirectionalMetadata(String metadata, Direction facing, BlockPos position,
                                                WorldGenLevel world, RandomSource random) {
            world.setBlock(position, Blocks.AIR.defaultBlockState(), 3);
            BlockPos offset = position.relative(facing);
            BlockState offsetState = world.getBlockState(offset);
            switch (metadata) {
                case "ivy" -> generateIvy(facing, position, world, random);
                case "shroom" -> world.setBlock(position, random.nextBoolean() ? Blocks.RED_MUSHROOM.defaultBlockState() : Blocks.BROWN_MUSHROOM.defaultBlockState(), 3);
                case "spawner_spiders" -> {
                    if (offsetState.getBlock() == Blocks.SPAWNER && world.getBlockEntity(offset) instanceof SpawnerBlockEntity spawner) {
                        spawner.setEntityId(random.nextBoolean() ? EntityType.CAVE_SPIDER : EntityType.SPIDER, random);
                        spawner.setChanged();
                    }
                }
                case "owl" -> {
                    OwlEntity owl = BMEntities.OWL.create(world.getLevel(), EntitySpawnReason.STRUCTURE);
                    if (owl != null) {
                        owl.setPos(position.getX() + 0.5D, position.getY(), position.getZ() + 0.5D);
                        if (random.nextFloat() < 0.25F && owl instanceof Stuntable stuntable) stuntable.biomemakeover$setStunted(true);
                        world.addFreshEntityWithPassengers(owl);
                    }
                }
                case "bonemeal", "tapestry" -> { /* deferred cosmetic/gameplay systems */ }
                default -> { }
            }
            if (metadata.startsWith("loot")) handleLoot(metadata, position, offset, world, random);
            else if (metadata.startsWith("enemy")) handleSpawning(metadata, world, position, details == null ? List.of() : details.mobs().enemies());
            else if (metadata.startsWith("ranger")) handleSpawning(metadata, world, position, details == null ? List.of() : details.mobs().rangedEnemies());
            else if (metadata.startsWith("ravager")) handleSpawning(metadata, world, position, details == null ? List.of() : details.mobs().ravagers());
            else if (metadata.startsWith("cow")) handleSpawning(metadata, world, position, details == null ? List.of() : details.mobs().cow());
            else if (metadata.startsWith("allay")) for (int i = 0; i < 3; i++) handleSpawning(metadata, world, position, details == null ? List.of() : details.mobs().allays());
        }

        private void handleLoot(String metadata, BlockPos marker, BlockPos containerPos, WorldGenLevel world, RandomSource random) {
            if (details == null) return;
            String[] parts = metadata.split("_");
            if (parts.length < 2) return;
            int chance = parts.length < 3 ? 100 : Integer.parseInt(parts[2]);
            BlockState replacement = null;
            if (parts.length >= 4) {
                StringBuilder id = new StringBuilder();
                for (int i = 3; i < parts.length; i++) id.append(parts[i]).append('_');
                ResourceLocation replacementId = ResourceLocation.parse(id.substring(0, id.length() - 1));
                Block replacementBlock = world.registryAccess().lookupOrThrow(Registries.BLOCK).getValue(replacementId);
                replacement = replacementBlock == null ? null : replacementBlock.defaultBlockState();
            }
            if (random.nextInt(100) > chance) {
                world.setBlock(containerPos, Blocks.AIR.defaultBlockState(), 3);
            } else {
                ResourceLocation table = switch (parts[1]) {
                    case "arrow" -> details.loot().arrow();
                    case "dungeonjunk" -> details.loot().dungeonJunk();
                    case "dungeon" -> details.loot().dungeonStandard();
                    case "dungeongood" -> details.loot().dungeonGood();
                    case "junk" -> details.loot().junk();
                    case "standard", "common" -> details.loot().standard();
                    case "good", "loot" -> details.loot().good();
                    default -> null;
                };
                if (table != null && world.getBlockEntity(containerPos) instanceof RandomizableContainerBlockEntity container) {
                    container.setLootTable(net.minecraft.resources.ResourceKey.create(Registries.LOOT_TABLE, table), random.nextLong());
                }
            }
            if (replacement != null) world.setBlock(marker, replacement, 3);
        }

        private void handleSpawning(String metadata, WorldGenLevel world, BlockPos position, List<EntityType<?>> pool) {
            if (pool.isEmpty()) return;
            String[] parts = metadata.split("_");
            int chance = parts.length < 2 ? 100 : Integer.parseInt(parts[1]) / 2;
            if (world.getRandom().nextInt(100) > chance) return;
            Entity entity = pool.get(world.getRandom().nextInt(pool.size())).create(world.getLevel(), EntitySpawnReason.STRUCTURE);
            if (entity == null) return;
            entity.setPos(position.getX() + 0.5D, position.getY(), position.getZ() + 0.5D);
            if (entity instanceof Mob mob) {
                mob.finalizeSpawn(world, world.getCurrentDifficultyAt(position), EntitySpawnReason.STRUCTURE, null);
                mob.setPersistenceRequired();
            }
            world.addFreshEntityWithPassengers(entity);
        }

        private void generateIvy(Direction direction, BlockPos position, WorldGenLevel world, RandomSource random) {
            if (random.nextFloat() < 0.25F) return;
            int size = 3;
            BlockPos start = direction.getStepY() == 0 ? position.relative(direction.getClockWise(), size).above(size) : position.offset(-size, 0, -size);
            BlockPos end = direction.getStepY() == 0 ? position.relative(direction.getCounterClockWise(), size).below(size) : position.offset(size, 0, size);
            BlockPos.betweenClosed(start, end).forEach(pos -> {
                BlockState current = world.getBlockState(pos);
                if (random.nextFloat() <= 0.25F && (current.isAir() || current.is(BMBlocks.IVY))) {
                    BlockPos support = pos.relative(direction);
                    if (world.getBlockState(support).isFaceSturdy(world, support, direction.getOpposite())) {
                        world.setBlock(pos, BMBlocks.IVY.defaultBlockState().setValue(net.minecraft.world.level.block.MultifaceBlock.getFaceProperty(direction), true), 3);
                    }
                }
            });
        }
    }
}
