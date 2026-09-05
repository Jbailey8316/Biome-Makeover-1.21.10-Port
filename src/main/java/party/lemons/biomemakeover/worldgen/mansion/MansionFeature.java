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
    /** Native 1.21.10 Trial-Chamber liquid semantics are the production default. */
    private static final boolean VANILLA_LIQUID_PARITY = true;
    private static final String RELEASED_MARKER_SEMANTICS = "boss->AIR;arena_pos->SMOOTH_QUARTZ";
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
    private static final Map<String, LateFinalization> LATE_FINALIZATIONS = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Map<String, Map<BlockPos, BlockState>> CROP_TARGETS = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Set<String> CROP_EXPECTED_MANSIONS = java.util.concurrent.ConcurrentHashMap.newKeySet();

    private static final class LayoutMetadata {
        final String key, signature; final BlockPos origin; final Set<String> ordinals, placements; final int unionSize, bossPieces; final List<BoundingBox> boxes;
        LayoutMetadata(String key, String signature, BlockPos origin, Set<String> ordinals, Set<String> placements, int unionSize, int bossPieces, List<BoundingBox> boxes) {
            this.key = key; this.signature = signature; this.origin = origin; this.ordinals = Set.copyOf(ordinals); this.placements = Set.copyOf(placements); this.unionSize = unionSize; this.bossPieces = bossPieces; this.boxes = List.copyOf(boxes);
        }
    }
    private static final Set<String> COVERAGE_MISMATCH_LOGGED = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private static final Set<String> CONTRACT_VIOLATIONS_LOGGED = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private static final Map<String, Set<String>> PLACED_PIECES = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Set<String> EXECUTED_MANSIONS = java.util.concurrent.ConcurrentHashMap.newKeySet();
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
                    late.readyTick = level.getGameTime();
                    LATE_FINALIZATIONS.putIfAbsent(mansionId, late);
                    int bossCleared = reconcileBossRoomFinalAir(level, entry.mansionOrigin, entry.layoutSignature);
                    if (tracing) BiomeMakeover.LOGGER.info("[BM_BOSS_ROOM_FINAL_RECONCILE] mansionId={} explicitAirCleared={}", entry.mansionId(), bossCleared);
                }
                if (tracing && result.executed()) {
                    BiomeMakeover.LOGGER.info("[BM_RECONCILE_LIFECYCLE] event=READY mansionId={} pieceCount={} unionPositions={}", entry.mansionId(), countMansionPieces(level, entry.mansionOrigin, entry.layoutSignature), unionSize(level, entry.mansionOrigin, entry.layoutSignature));
                    BiomeMakeover.LOGGER.info("[BM_RECONCILE_LIFECYCLE] event=EXECUTE_BEGIN mansionId={} unionPositions={}", entry.mansionId(), unionSize(level, entry.mansionOrigin, entry.layoutSignature));
                    BiomeMakeover.LOGGER.info("[BM_RECONCILE_LIFECYCLE] event=EXECUTE_END mansionId={} correctedAir={} correctedWaterlogged={} authoredWetPreserved={}",
                        entry.mansionId(), result.correctedAir(), result.correctedWaterlogged(), result.authoredWetPreserved());
                    BiomeMakeover.LOGGER.info("[BM_DUNGEON_RECONCILE] phase=R0 mansionId={} explicitDryWater={} authoredFalseNowWaterlogged={} correctedAir={} correctedWaterlogged={} authoredWetPreserved={}",
                        entry.mansionId(), result.explicitDryWater(), result.authoredFalseNowWaterlogged(), result.correctedAir(), result.correctedWaterlogged(), result.authoredWetPreserved());
                    BiomeMakeover.LOGGER.info("[BM_RECONCILE_LIFECYCLE] event=REMOVE mansionId={}", entry.mansionId());
                }
            }
            entry.age++;
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
                    emitCropFinalizationResult(level, late, crops, "C7");
                    late.retention(level, "C7");
                }
            }
            if (late.age == 2 && tracing) {
                late.retention(level, "C8");
            }
            long ageTicks = late.readyTick < 0 ? -1 : level.getGameTime() - late.readyTick;
            if (ageTicks >= 400) late.retention(level, "D20S");
            if (ageTicks >= 900) { late.retention(level, "D45S"); LATE_FINALIZATIONS.remove(late.id, late); }
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
        LayoutMetadata metadata = new LayoutMetadata(key, signature, origin, ids, placements, completeUnion.size(), bossPieces,
            pieces.stream().map(Piece::getBoundingBox).toList());
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
        return new LateFinalization(level, id, origin, signature, crops, boss, ready);
    }

    private static final class LateFinalization {
        final ServerLevel level; final String id; final BlockPos origin; final String signature; final Map<BlockPos, BlockState> crops; final Set<BlockPos> bossAir; final boolean ready; final Set<String> phases = new java.util.HashSet<>(); final Set<String> retentionPhases = new java.util.HashSet<>(); String currentPhase = "READY"; long readyTick = -1; int age = -1;
        LateFinalization(ServerLevel level, String id, BlockPos origin, String signature, Map<BlockPos, BlockState> crops, Set<BlockPos> bossAir, boolean ready) { this.level = level; this.id = id; this.origin = origin; this.signature = signature; this.crops = Map.copyOf(crops); this.bossAir = Set.copyOf(bossAir); this.ready = ready; }
        void retention(ServerLevel level, String phase) {
            if (retentionPhases.add(phase)) BiomeMakeover.LOGGER.info("[BM_LATE_FINALIZATION_RETENTION] mansionId={} phase={} readyTick={} currentTick={} ageTicks={} retained={}", id, phase, readyTick, level.getGameTime(), level.getGameTime() - readyTick, !"D45S".equals(phase));
        }
    }

    private static int cropTargetCount(ServerLevel level, BlockPos origin, String signature) {
        return cropTargets(level, origin, signature).size();
    }

    private static void emitCropFinalizationResult(ServerLevel level, LateFinalization late, int restored, String phase) {
        int present = 0, supportMissing = 0;
        for (var entry : late.crops.entrySet()) {
            if (level.getBlockState(entry.getKey()).is(entry.getValue().getBlock())) present++;
            if (!level.getBlockState(entry.getKey().below()).is(Blocks.FARMLAND)) supportMissing++;
        }
        int missing = late.crops.size() - present;
        BiomeMakeover.LOGGER.info("[BM_CROP_FINALIZATION_RESULT] mansionId={} phase={} expected={} restored={} presentAfter={} missingAfter={} supportMissing={}",
            late.id, phase, late.crops.size(), restored, present, missing, supportMissing);
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

    private static final class DelayedFluidTrace {
        private final ServerLevel level;
        private final String template;
        private final Rotation rotation;
        private final BlockPos mansionOrigin;
        private final String layoutSignature;
        private final Map<BlockPos, BlockState> authoredStates;
        private final long order;
        private int age;

        private DelayedFluidTrace(ServerLevel level, String template, Rotation rotation, BlockPos mansionOrigin,
                                  Map<BlockPos, BlockState> authoredStates, long order, String layoutSignature) {
            this.level = level; this.template = template; this.rotation = rotation; this.mansionOrigin = mansionOrigin;
            this.authoredStates = Map.copyOf(authoredStates); this.order = order; this.layoutSignature = layoutSignature;
        }

        private String mansionId() { return level.dimension().location() + ":" + mansionOrigin + ":" + layoutSignature; }
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
                mansionTemplateForRoom(room, templates, context.random()).toString(), roomPos, rotation,
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

    private static ResourceLocation mansionTemplateForRoom(MansionRoom room, MansionTemplates templates, RandomSource random) {
        ResourceLocation original = room.getTemplate(templates, random);
        return original;
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
            builder.addPiece(new Piece(details, manager, mansionTemplateForRoom(room, templates, random).toString(), roomPos,
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
        private static final AtomicLong TAPESTRY_PLACEMENT_TRACE_COUNT = new AtomicLong();
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
                    persistedOrdinals, persistedPlacements, persistedUnionSize, Math.max(0, persistedBossPieces), List.of());
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
            switch (metadata) {
                case "boss" -> level.setBlock(position, Blocks.AIR.defaultBlockState(), 2);
                case "arena_pos" -> level.setBlock(position, Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);
                default -> { }
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
            if (layoutMetadata != null) MansionTreeProtection.register(level,
                level.getLevel().dimension().location() + ":" + mansionOrigin + ":" + layoutSignature,
                layoutMetadata.boxes);
            if (level.getLevel() instanceof ServerLevel serverLevel) registerCropTargets(serverLevel);
            Map<BlockPos, BlockState> authoredStates = dungeonAuthoredStates();
            super.postProcess(level, structureManager, generator, random, bounds, chunkPos, pivot);
            for (var info : template.filterBlocks(templatePosition, placeSettings, BMBlocks.DIRECTIONAL_DATA)) {
                if (info.nbt() != null && info.state().hasProperty(DirectionalBlock.FACING)) {
                    String metadata = info.nbt().getStringOr("metadata", "");
                    Direction serializedFacing = info.state().getValue(DirectionalBlock.FACING);
                    Direction transformedFacing = info.state().mirror(placeSettings.getMirror()).rotate(placeSettings.getRotation())
                        .getValue(DirectionalBlock.FACING);
                    handleDirectionalMetadata(metadata, serializedFacing,
                        "tapestry".equals(metadata) ? transformedFacing : serializedFacing, info.pos(), level, random);
                    if (TRACE && "tapestry".equals(metadata) && TAPESTRY_PLACEMENT_TRACE_COUNT.incrementAndGet() <= 16) {
                        BlockState placed = level.getBlockState(info.pos());
                        String form = placed.getBlock() instanceof MansionWallTapestryBlock ? "wall"
                            : placed.getBlock() instanceof MansionStandingTapestryBlock ? "standing" : "unknown";
                        String finalState = placed.hasProperty(MansionWallTapestryBlock.FACING)
                            ? placed.getValue(MansionWallTapestryBlock.FACING).getSerializedName()
                            : placed.hasProperty(MansionStandingTapestryBlock.ROTATION)
                                ? Integer.toString(placed.getValue(MansionStandingTapestryBlock.ROTATION)) : "none";
                        BlockPos support = placed.getBlock() instanceof MansionWallTapestryBlock
                            ? info.pos().relative(placed.getValue(MansionWallTapestryBlock.FACING).getOpposite())
                            : info.pos().below();
                        BiomeMakeover.LOGGER.info("[BM_TAPESTRY_PLACEMENT_TRACE] variant={} form={} markerPos={} finalBlockPos={} sourceDirection={} pieceRotation={} pieceMirror={} finalFacingOrRotation={} supportPos={}",
                            BuiltInRegistries.BLOCK.getKey(placed.getBlock()), form, info.pos(), info.pos(), transformedFacing.getSerializedName(),
                            placeSettings.getRotation(), placeSettings.getMirror(), finalState, support);
                    }
                }
            }
            restoreSerializedCrops(level, bounds);
            // Modern structure placement can merge an existing source fluid into
            // waterloggable blocks even when keepLiquids(false) is set.  Released
            // Mansion dungeon templates explicitly authored these states as dry;
            // restore only those transformed template cells, leaving authored wet
            // states and all surrounding world fluid untouched.
            // Legacy R17 authored-fluid mutation is retired; native liquid settings are authoritative.
            if (VANILLA_LIQUID_PARITY && diagnosticTemplate.contains("/boss_room")) clearBossRoomAuthoredAir(level, authoredStates, bounds);
            if (isDungeonStructuralTemplate() && level.getLevel() instanceof ServerLevel serverLevel)
                DELAYED_FLUID_TRACES.add(new DelayedFluidTrace(serverLevel, diagnosticTemplate, placeSettings.getRotation(), mansionOrigin, authoredStates, order, layoutSignature));

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

        private Map<BlockPos, BlockState> dungeonAuthoredStates() {
            if (!isDungeonStructuralTemplate()) return Map.of();
            Map<BlockPos, BlockState> states = new HashMap<>();
            for (Block block : BuiltInRegistries.BLOCK) {
                for (var info : template.filterBlocks(templatePosition, placeSettings, block)) states.put(info.pos(), info.state());
            }
            return states;
        }

        private boolean isDungeonStructuralTemplate() {
            return diagnosticTemplate.contains("/dungeon/") || diagnosticTemplate.contains("/boss_room");
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

        private void handleDirectionalMetadata(String metadata, Direction facing, BlockPos position,
                                                WorldGenLevel world, RandomSource random) {
            handleDirectionalMetadata(metadata, facing, facing, position, world, random);
        }

        private void handleDirectionalMetadata(String metadata, Direction serializedFacing, Direction facing,
                                                BlockPos position, WorldGenLevel world, RandomSource random) {
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
                case "tapestry" -> generateTapestry(facing, position, world, random);
                case "bonemeal" -> { /* released final source intentionally leaves this marker inert */ }
                default -> { }
            }
            if (metadata.startsWith("loot")) handleLoot(metadata, position, offset, world, random);
            else if (metadata.startsWith("enemy")) handleSpawning(metadata, world, position, details == null ? List.of() : details.mobs().enemies());
            else if (metadata.startsWith("ranger")) handleSpawning(metadata, world, position, details == null ? List.of() : details.mobs().rangedEnemies());
            else if (metadata.startsWith("ravager")) handleSpawning(metadata, world, position, details == null ? List.of() : details.mobs().ravagers());
            else if (metadata.startsWith("cow")) handleSpawning(metadata, world, position, details == null ? List.of() : details.mobs().cow());
            else if (metadata.startsWith("allay")) for (int i = 0; i < 3; i++) handleSpawning(metadata, world, position, details == null ? List.of() : details.mobs().allays());
        }

        private void generateTapestry(Direction facing, BlockPos position, WorldGenLevel level, RandomSource random) {
            Block tapestry;
            if (facing == Direction.UP || facing == Direction.DOWN) {
                tapestry = BMBlocks.MANSION_STANDING_TAPESTRIES.get(random.nextInt(BMBlocks.MANSION_STANDING_TAPESTRIES.size()));
                level.setBlock(position, tapestry.defaultBlockState()
                    .setValue(MansionStandingTapestryBlock.ROTATION, Rotation.getRandom(random).ordinal()), 3);
            } else {
                tapestry = BMBlocks.MANSION_WALL_TAPESTRIES.get(random.nextInt(BMBlocks.MANSION_WALL_TAPESTRIES.size()));
                level.setBlock(position, tapestry.defaultBlockState()
                    .setValue(MansionWallTapestryBlock.FACING, facing.getOpposite()), 3);
            }
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
