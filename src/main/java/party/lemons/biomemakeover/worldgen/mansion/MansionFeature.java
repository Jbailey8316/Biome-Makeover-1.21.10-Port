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
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.state.BlockState;
import party.lemons.biomemakeover.block.IvyBlock;
import party.lemons.biomemakeover.entity.OwlEntity;
import party.lemons.biomemakeover.util.extension.Stuntable;
import party.lemons.biomemakeover.init.BMEntities;
import net.minecraft.world.level.ChunkPos;
import party.lemons.biomemakeover.init.BMStructures;
import party.lemons.biomemakeover.init.BMBlocks;

import java.util.Optional;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import party.lemons.biomemakeover.worldgen.mansion.room.MansionRoom;
import party.lemons.biomemakeover.worldgen.mansion.RoomType;

/**
 * Foundation for the released custom Mansion structure. Physical layout and
 * templates are intentionally activated by later 11A stages.
 */
public final class MansionFeature extends Structure {
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
        private final boolean ground;
        private final boolean wall;
        private MansionDetails details;

    public Piece(StructureTemplateManager manager, ResourceLocation template, BlockPos position,
                     Rotation rotation, boolean ground, boolean wall) {
            super(BMStructures.MANSION_PIECE, 0, manager, template, template.toString(),
                settings(rotation, wall), position);
            this.ground = ground;
            this.wall = wall;
            this.details = null;
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
        }

        private static StructurePlaceSettings settings(Rotation rotation, boolean wall) {
            return new StructurePlaceSettings().setIgnoreEntities(true)
                .setKeepLiquids(false)
                .setRotation(rotation).setMirror(Mirror.NONE)
                .addProcessor(wall ? BlockIgnoreProcessor.STRUCTURE_AND_AIR : BlockIgnoreProcessor.STRUCTURE_BLOCK);
        }

        @Override protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
            super.addAdditionalSaveData(context, tag);
            tag.putString("Rotation", placeSettings.getRotation().name());
            tag.putBoolean("Ground", ground);
            tag.putBoolean("IsWall", wall);
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
            super.postProcess(level, structureManager, generator, random, bounds, chunkPos, pivot);
            for (var info : template.filterBlocks(templatePosition, placeSettings, BMBlocks.DIRECTIONAL_DATA)) {
                if (info.nbt() != null && info.state().hasProperty(DirectionalBlock.FACING)) {
                    Direction facing = info.state().getValue(DirectionalBlock.FACING);
                    handleDirectionalMetadata(info.nbt().getStringOr("metadata", ""), facing, info.pos(), level, random);
                }
            }
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
            if (random.nextInt(100) > chance) {
                world.setBlock(containerPos, Blocks.AIR.defaultBlockState(), 3);
                return;
            }
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
