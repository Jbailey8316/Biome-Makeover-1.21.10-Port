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
    private static final CopyOnWriteArrayList<DelayedFluidTrace> DELAYED_FLUID_TRACES = new CopyOnWriteArrayList<>();
    private static final ThreadLocal<BlockPos> LAYOUT_ORIGIN = new ThreadLocal<>();
    private static final ThreadLocal<List<Piece>> LAYOUT_PIECES = new ThreadLocal<>();
    private static final ThreadLocal<Integer> NEXT_PIECE_ORDINAL = new ThreadLocal<>();
    private static final Map<String, Integer> EXPECTED_PIECES = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Map<String, Set<String>> EXPECTED_ORDINALS = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Map<String, Set<String>> EXPECTED_PLACEMENTS = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Map<String, Set<String>> PLACED_PLACEMENTS = new java.util.concurrent.ConcurrentHashMap<>();
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
            if (EXPECTED_PIECES.containsKey(mansionId)
                && PLACED_PLACEMENTS.getOrDefault(mansionId, Set.of()).equals(EXPECTED_PLACEMENTS.get(mansionId))
                && EXECUTED_MANSIONS.add(mansionId)) {
                ReconcileResult result = reconcileCompletedDungeon(level, entry.order, entry.mansionOrigin);
                if (tracing && result.executed()) {
                    BiomeMakeover.LOGGER.info("[BM_RECONCILE_LIFECYCLE] event=READY mansionId={} pieceCount={} unionPositions={}", entry.mansionId(), countMansionPieces(level, entry.mansionOrigin), unionSize(level, entry.mansionOrigin));
                    BiomeMakeover.LOGGER.info("[BM_RECONCILE_LIFECYCLE] event=EXECUTE_BEGIN mansionId={} unionPositions={}", entry.mansionId(), unionSize(level, entry.mansionOrigin));
                    BiomeMakeover.LOGGER.info("[BM_RECONCILE_LIFECYCLE] event=EXECUTE_END mansionId={} correctedAir={} correctedWaterlogged={} authoredWetPreserved={}",
                        entry.mansionId(), result.correctedAir(), result.correctedWaterlogged(), result.authoredWetPreserved());
                    BiomeMakeover.LOGGER.info("[BM_DUNGEON_RECONCILE] phase=R0 mansionId={} explicitDryWater={} authoredFalseNowWaterlogged={} correctedAir={} correctedWaterlogged={} authoredWetPreserved={}",
                        entry.mansionId(), result.explicitDryWater(), result.authoredFalseNowWaterlogged(), result.correctedAir(), result.correctedWaterlogged(), result.authoredWetPreserved());
                    entry.snapshot(level, "D0");
                    BiomeMakeover.LOGGER.info("[BM_RECONCILE_LIFECYCLE] event=REMOVE mansionId={}", entry.mansionId());
                }
            }
            entry.age++;
            String phase = switch (entry.age) { case 1 -> "D1"; case 5 -> "D5"; case 20 -> "D20"; case 100 -> "D100"; default -> null; };
            if (phase != null) entry.snapshot(level, phase);
            if (entry.age >= 100 || (!tracing && EXECUTED_MANSIONS.contains(entry.mansionId()))) DELAYED_FLUID_TRACES.remove(entry);
        }
    }

    /**
     * Restores only the final serialized dry state of the dungeon union.  The
     * records are captured from transformed template palettes, so omitted
     * terrain and surrounding aquifer cells are never touched.
     */
    private record ReconcileResult(boolean executed, int correctedAir, int correctedWaterlogged, int authoredWetPreserved,
                                   int explicitDryWater, int authoredFalseNowWaterlogged) {}

    private static int unionSize(ServerLevel level, BlockPos mansionOrigin) {
        Set<BlockPos> positions = new java.util.HashSet<>();
        for (DelayedFluidTrace candidate : DELAYED_FLUID_TRACES) if (candidate.level == level && candidate.mansionOrigin.equals(mansionOrigin)) positions.addAll(candidate.authoredStates.keySet());
        return positions.size();
    }

    private static int countMansionPieces(ServerLevel level, BlockPos mansionOrigin) {
        int count = 0;
        for (DelayedFluidTrace candidate : DELAYED_FLUID_TRACES)
            if (candidate.level == level && candidate.mansionOrigin.equals(mansionOrigin)) count++;
        return count;
    }

    private static void registerExpectedPieces(BlockPos origin, List<Piece> pieces) {
        String key = "minecraft:overworld:" + origin;
        Set<String> ids = new java.util.HashSet<>();
        for (Piece piece : pieces) if (piece.isDungeonStructuralTemplate()) ids.add(Integer.toString(piece.mansionPieceOrdinal));
        EXPECTED_PIECES.put(key, ids.size());
        EXPECTED_ORDINALS.put(key, Set.copyOf(ids));
        Set<String> placements = new java.util.HashSet<>();
        for (Piece piece : pieces) if (piece.isDungeonStructuralTemplate()) {
            BoundingBox box = piece.getBoundingBox();
            for (int chunkX = box.minX() >> 4; chunkX <= box.maxX() >> 4; chunkX++)
                for (int chunkZ = box.minZ() >> 4; chunkZ <= box.maxZ() >> 4; chunkZ++)
                    placements.add(piece.mansionPieceOrdinal + ":" + chunkX + ":" + chunkZ);
        }
        EXPECTED_PLACEMENTS.put(key, Set.copyOf(placements));
        PLACED_PLACEMENTS.putIfAbsent(key, java.util.concurrent.ConcurrentHashMap.newKeySet());
        if (Boolean.getBoolean("bm.mansion.trace")) BiomeMakeover.LOGGER.info("[BM_RECONCILE_LIFECYCLE] event=LAYOUT_COMPLETE mansionId={} structuralPieces={} expectedPlacementCount={} unionPositions={}",
            key, ids.size(), placements.size(), pieces.stream().filter(Piece::isDungeonStructuralTemplate).mapToInt(piece -> piece.dungeonAuthoredStates().size()).sum());
        PLACED_PIECES.putIfAbsent(key, java.util.concurrent.ConcurrentHashMap.newKeySet());
    }

    private static ReconcileResult reconcileCompletedDungeon(ServerLevel level, long order, BlockPos mansionOrigin) {
        long first = Long.MAX_VALUE;
        for (DelayedFluidTrace candidate : DELAYED_FLUID_TRACES) {
            if (candidate.level == level && candidate.mansionOrigin.equals(mansionOrigin) && candidate.order < first) first = candidate.order;
        }
        if (order != first) return new ReconcileResult(false, 0, 0, 0, 0, 0);
        Map<BlockPos, BlockState> union = new HashMap<>();
        for (DelayedFluidTrace candidate : DELAYED_FLUID_TRACES) {
            if (candidate.level == level && candidate.mansionOrigin.equals(mansionOrigin)) union.putAll(candidate.authoredStates);
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
        private final List<BlockPos> authoredDry;
        private final List<BlockPos> architecturalInterior;
        private final Map<BlockPos, BlockState> authoredStates;
        private final long order;
        private final java.util.Set<BlockPos> previouslyWet = new java.util.HashSet<>();
        private int age;

        private DelayedFluidTrace(ServerLevel level, String template, Rotation rotation, BlockPos mansionOrigin, List<BlockPos> authoredDry,
                                  List<BlockPos> architecturalInterior, Map<BlockPos, BlockState> authoredStates, long order) {
            this.level = level; this.template = template; this.rotation = rotation;
            this.mansionOrigin = mansionOrigin;
            this.authoredDry = List.copyOf(authoredDry); this.architecturalInterior = List.copyOf(architecturalInterior);
            this.authoredStates = Map.copyOf(authoredStates); this.order = order;
        }

        private String mansionId() {
            return level.dimension().location() + ":" + mansionOrigin;
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
        }

        private static StructurePlaceSettings settings(Rotation rotation, boolean wall) {
            return new StructurePlaceSettings().setIgnoreEntities(true)
                .setRotation(rotation).setMirror(Mirror.NONE)
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
            if (details != null) {
                MansionDetails.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, details).result().ifPresent(value -> tag.put("Details", value));
            }
        }

        @Override protected void handleDataMarker(String metadata, BlockPos position, ServerLevelAccessor level,
                                                   RandomSource random, BoundingBox bounds) {
            // Boss markers remain deferred to Stage 12; arena markers are inert.
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
            // Modern structure placement can merge an existing source fluid into
            // waterloggable blocks even when keepLiquids(false) is set.  Released
            // Mansion dungeon templates explicitly authored these states as dry;
            // restore only those transformed template cells, leaving authored wet
            // states and all surrounding world fluid untouched.
            if (isDungeonStructuralTemplate()) correctReleasedFluidStateForCurrentClip(level, authoredStates, bounds);
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
                        placeSettings.getRotation(), mansionOrigin, staticAuthoredDry, architecturalInterior, authoredStates, order));
                    DUNGEON_ENVELOPE.add(new EnvelopePiece(serverLevel, diagnosticTemplate, staticAuthoredDry, architecturalInterior));
                    if (DUNGEON_ENVELOPE.size() > 512) DUNGEON_ENVELOPE.remove(0);
                }
                if (false) BiomeMakeover.LOGGER.info("[BM_PIECE_TRACE] template={} rot={} bounds={} phase=END thread={} timestamp={} orderIndex={}",
                    diagnosticTemplate, placeSettings.getRotation(), bounds, Thread.currentThread().getName(), System.currentTimeMillis(), order);
            }
            if (isDungeonStructuralTemplate() && level.getLevel() instanceof ServerLevel serverLevel) {
                String mansionId = serverLevel.dimension().location() + ":" + mansionOrigin;
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
                    placeSettings.getRotation(), mansionOrigin, staticAuthoredDry, architecturalInterior, authoredStates, order));
            }
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

        private void traceCrops(WorldGenLevel level, String phase) {
            for (Block crop : new Block[] {Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES, Blocks.BEETROOTS,
                Blocks.MELON_STEM, Blocks.PUMPKIN_STEM}) {
                for (var info : template.filterBlocks(templatePosition, placeSettings, crop)) {
                    BlockPos support = info.pos().below();
                    BiomeMakeover.LOGGER.info("[BM_CROP_TRACE] template={} local={} world={} serializedState={} phase={} runtimeState={} supportState={}",
                        diagnosticTemplate, info.pos().subtract(templatePosition), info.pos(), info.state(), phase,
                        level.getBlockState(info.pos()), level.getBlockState(support));
                }
            }
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
