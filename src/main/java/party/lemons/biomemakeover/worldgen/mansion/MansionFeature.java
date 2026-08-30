package party.lemons.biomemakeover.worldgen.mansion;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
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
import java.util.function.Consumer;
import party.lemons.biomemakeover.worldgen.mansion.room.MansionRoom;
import party.lemons.biomemakeover.worldgen.mansion.RoomType;

/**
 * Foundation for the released custom Mansion structure. Physical layout and
 * templates are intentionally activated by later 11A stages.
 */
public final class MansionFeature extends Structure {
    private static final CopyOnWriteArrayList<DelayedFluidTrace> DELAYED_FLUID_TRACES = new CopyOnWriteArrayList<>();
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
        for (DelayedFluidTrace trace : DELAYED_FLUID_TRACES) {
            if (trace.level != level) continue;
            if (trace.age == 0) {
                BiomeMakeover.LOGGER.info("[BM_DELAYED_TRACE] event=SERVER_ACCEPT template={} registrationId={}", trace.template, trace.order);
                trace.snapshot(level, "D0");
            }
            trace.age++;
            String phase = switch (trace.age) { case 1 -> "D1"; case 5 -> "D5"; case 20 -> "D20"; case 100 -> "D100"; default -> null; };
            if (phase != null) trace.snapshot(level, phase);
            if (trace.age >= 100) DELAYED_FLUID_TRACES.remove(trace);
        }
    }

    private static final class DelayedFluidTrace {
        private final ServerLevel level;
        private final String template;
        private final Rotation rotation;
        private final List<BlockPos> authoredDry;
        private final long order;
        private final java.util.Set<BlockPos> previouslyWet = new java.util.HashSet<>();
        private int age;

        private DelayedFluidTrace(ServerLevel level, String template, Rotation rotation, List<BlockPos> authoredDry, long order) {
            this.level = level; this.template = template; this.rotation = rotation; this.authoredDry = List.copyOf(authoredDry); this.order = order;
        }

        private void snapshot(ServerLevel level, String phase) {
            int water = 0, source = 0, flowing = 0;
            int newlyWet = 0;
            for (BlockPos pos : authoredDry) {
                var fluid = level.getFluidState(pos);
                if (fluid.is(Fluids.WATER)) {
                    water++;
                    if (fluid.isSource()) source++; else flowing++;
                    if (previouslyWet.add(pos) && newlyWet++ < 12) {
                        BiomeMakeover.LOGGER.info("[BM_FLUID_REENTRY] template={} local=<static-mask> world={} firstWetPhase={} fluid={} sourceOrFlowing={} neighborN={} neighborE={} neighborS={} neighborW={} neighborUp={} neighborDown={} owningDungeonPiece={} boundaryPosition={} intentionalOpening={}",
                            template, pos, phase, fluid, fluid.isSource() ? "SOURCE" : "FLOWING", level.getBlockState(pos.north()).getBlock(),
                            level.getBlockState(pos.east()).getBlock(), level.getBlockState(pos.south()).getBlock(), level.getBlockState(pos.west()).getBlock(),
                            level.getBlockState(pos.above()).getBlock(), level.getBlockState(pos.below()).getBlock(), template, true, false);
                    }
                }
            }
            BiomeMakeover.LOGGER.info("[BM_DUNGEON_FLUID_DELAYED] template={} rotation={} phase={} authoredDryPositions={} waterInAuthoredDry={} sourceWaterInAuthoredDry={} flowingWaterInAuthoredDry={} newlyWetPositions={} orderIndex={}",
                template, rotation, phase, authoredDry.size(), water, source, flowing, newlyWet, order);
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
        BlockPos origin = new BlockPos(
            context.chunkPos().getMinBlockX(),
            context.chunkGenerator().getBaseHeight(
                context.chunkPos().getMinBlockX(), context.chunkPos().getMinBlockZ(),
                Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState()),
            context.chunkPos().getMinBlockZ());
        layout.generateLayout(context.random(), origin.getY());
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
        return Optional.of(new GenerationStub(
            getLowestYIn5by5BoxOffset7Blocks(context, Rotation.NONE), Either.right(builder)));
    }

    @Override public StructureType<?> type() { return BMStructures.MANSION; }

    public MansionTemplates templates() { return templates; }
    public MansionDetails details() { return details; }

    /** Builds the released physical piece graph without activating worldgen. */
    public static void buildLayoutPieces(StructureTemplateManager manager, BlockPos origin,
                                         RandomSource random, MansionTemplates templates,
                                         MansionDetails details, StructurePiecesBuilder builder) {
        MansionLayout layout = new MansionLayout();
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
    }

    /** Serialized custom template piece; marker actions are connected later. */
    public static final class Piece extends TemplateStructurePiece {
        private static final boolean TRACE = Boolean.getBoolean("bm.mansion.trace");
        private static final AtomicLong TRACE_ORDER = new AtomicLong();
        private final boolean ground;
        private final boolean wall;
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
                BiomeMakeover.LOGGER.info("[BM_PIECE_TRACE] template={} rot={} bounds={} phase=BEGIN thread={} timestamp={} orderIndex={}",
                    diagnosticTemplate, placeSettings.getRotation(), bounds, Thread.currentThread().getName(), System.currentTimeMillis(), order);
                traceLootLifecycle(level, bounds, "T0", order);
                traceFences(level, bounds, "F0", order);
                traceFluids(level, bounds, "W0", order);
                traceFluidInterior(level, "W0", order);
            }
            List<BlockPos> staticAuthoredDry = dungeonAuthoredDryPositions();
            super.postProcess(level, structureManager, generator, random, bounds, chunkPos, pivot);
            if (TRACE) {
                BiomeMakeover.LOGGER.info("[BM_PIECE_TRACE] template={} rot={} bounds={} phase=AFTER_TEMPLATE thread={} timestamp={} orderIndex={}",
                    diagnosticTemplate, placeSettings.getRotation(), bounds, Thread.currentThread().getName(), System.currentTimeMillis(), order);
                traceLootLifecycle(level, bounds, "T1", order);
                traceFences(level, bounds, "F1", order);
                traceFluids(level, bounds, "W1", order);
                traceFluidInterior(level, "W1", order);
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
            if (TRACE) traceFluidInterior(level, "W2", order);
            if (TRACE) {
                traceLootLifecycle(level, bounds, "T4", order);
                traceFences(level, bounds, "F4", order);
                traceFluids(level, bounds, "W5", order);
                traceFluidInterior(level, "W3", order);
                if (diagnosticTemplate.contains("/dungeon/") && level.getLevel() instanceof ServerLevel serverLevel) {
                    BiomeMakeover.LOGGER.info("[BM_DELAYED_TRACE] event=REGISTER_BEGIN template={} orderIndex={} workerThread={} registrationId={}",
                        diagnosticTemplate, order, Thread.currentThread().getName(), order);
                    DELAYED_FLUID_TRACES.add(new DelayedFluidTrace(serverLevel, diagnosticTemplate,
                        placeSettings.getRotation(), staticAuthoredDry, order));
                    BiomeMakeover.LOGGER.info("[BM_DELAYED_TRACE] event=REGISTER_END template={} orderIndex={} registrationId={}",
                        diagnosticTemplate, order, order);
                }
                BiomeMakeover.LOGGER.info("[BM_PIECE_TRACE] template={} rot={} bounds={} phase=END thread={} timestamp={} orderIndex={}",
                    diagnosticTemplate, placeSettings.getRotation(), bounds, Thread.currentThread().getName(), System.currentTimeMillis(), order);
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
            if (!diagnosticTemplate.contains("/dungeon/")) return;
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
            if (!diagnosticTemplate.contains("/dungeon/")) return List.of();
            List<BlockPos> positions = new java.util.ArrayList<>();
            for (Block block : new Block[] {Blocks.AIR, Blocks.CAVE_AIR}) {
                for (var info : template.filterBlocks(templatePosition, placeSettings, block)) positions.add(info.pos());
            }
            return positions;
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
