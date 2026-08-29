package party.lemons.biomemakeover.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootTable;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.init.BMStructures;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Final 1.20.1 Sunken Ruin algorithm translated to 1.21.10 structure APIs. */
public final class SunkenRuinStructure extends Structure {
    public static final MapCodec<SunkenRuinStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        settingsCodec(instance),
        Codec.floatRange(0.0F, 1.0F).fieldOf("large_probability").forGetter(structure -> structure.largeProbability),
        Codec.floatRange(0.0F, 1.0F).fieldOf("cluster_probability").forGetter(structure -> structure.clusterProbability)
    ).apply(instance, SunkenRuinStructure::new));

    private static final ResourceLocation[] LARGE_PIECES = {
        BiomeMakeover.id("sunken_ruins/sunken_1"),
        BiomeMakeover.id("sunken_ruins/sunken_2"),
        BiomeMakeover.id("sunken_ruins/sunken_3")
    };
    private static final ResourceLocation[] SMALL_PIECES = {
        BiomeMakeover.id("sunken_ruins/sunken_small_1"),
        BiomeMakeover.id("sunken_ruins/sunken_small_2"),
        BiomeMakeover.id("sunken_ruins/sunken_small_3"),
        BiomeMakeover.id("sunken_ruins/sunken_small_4"),
        BiomeMakeover.id("sunken_ruins/sunken_small_5"),
        BiomeMakeover.id("sunken_ruins/sunken_small_6")
    };
    private static final ResourceKey<LootTable> LOOT = ResourceKey.create(
        Registries.LOOT_TABLE, BiomeMakeover.id("sunken_ruin"));

    private final float largeProbability;
    private final float clusterProbability;

    public SunkenRuinStructure(StructureSettings settings, float largeProbability, float clusterProbability) {
        super(settings);
        this.largeProbability = largeProbability;
        this.clusterProbability = clusterProbability;
    }

    @Override
    public GenerationStep.Decoration step() {
        // The released JSON said surface_structures, but this class override was runtime-authoritative.
        return GenerationStep.Decoration.LOCAL_MODIFICATIONS;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        return onTopOfChunkCenter(context, Heightmap.Types.OCEAN_FLOOR_WG,
            pieces -> generatePieces(pieces, context));
    }

    private void generatePieces(StructurePiecesBuilder pieces, GenerationContext context) {
        BlockPos origin = new BlockPos(context.chunkPos().getMinBlockX(), 90, context.chunkPos().getMinBlockZ());
        addPieces(context.structureTemplateManager(), origin, Rotation.getRandom(context.random()), pieces,
            context.random(), this);
    }

    @Override
    public StructureType<?> type() {
        return BMStructures.SUNKEN_RUIN;
    }

    static void addPieces(StructureTemplateManager manager, BlockPos origin, Rotation rotation,
                          StructurePieceAccessor pieces, RandomSource random, SunkenRuinStructure structure) {
        boolean large = random.nextFloat() <= structure.largeProbability;
        // The released source calculated 0.9/0.8 here but passed integrity 1 in addPiece.
        addPiece(manager, origin, rotation, pieces, random, large);
        if (large && random.nextFloat() <= structure.clusterProbability) {
            addClusterRuins(manager, random, rotation, origin, pieces);
        }
    }

    private static void addPiece(StructureTemplateManager manager, BlockPos position, Rotation rotation,
                                 StructurePieceAccessor pieces, RandomSource random, boolean large) {
        ResourceLocation[] choices = large ? LARGE_PIECES : SMALL_PIECES;
        ResourceLocation template = choices[random.nextInt(choices.length)];
        pieces.addPiece(new SunkenRuinPiece(manager, template, position, rotation, 1.0F, large));
    }

    private static void addClusterRuins(StructureTemplateManager manager, RandomSource random, Rotation rootRotation,
                                        BlockPos origin, StructurePieceAccessor pieces) {
        BlockPos rootStart = new BlockPos(origin.getX(), 90, origin.getZ());
        BlockPos rootEnd = StructureTemplate.transform(new BlockPos(15, 0, 15), Mirror.NONE, rootRotation,
            BlockPos.ZERO).offset(rootStart);
        BoundingBox rootBox = BoundingBox.fromCorners(rootStart, rootEnd);
        BlockPos minimum = new BlockPos(Math.min(rootStart.getX(), rootEnd.getX()), rootStart.getY(),
            Math.min(rootStart.getZ(), rootEnd.getZ()));
        List<BlockPos> candidates = allPositions(random, minimum);
        int count = Mth.nextInt(random, 4, 8);
        for (int index = 0; index < count; index++) {
            if (candidates.isEmpty()) {
                break;
            }
            BlockPos candidate = candidates.remove(random.nextInt(candidates.size()));
            Rotation rotation = Rotation.getRandom(random);
            BlockPos candidateEnd = StructureTemplate.transform(new BlockPos(5, 0, 6), Mirror.NONE, rotation,
                BlockPos.ZERO).offset(candidate);
            // Final behavior checks auxiliaries only against the root, not against each other.
            if (!BoundingBox.fromCorners(candidate, candidateEnd).intersects(rootBox)) {
                addPiece(manager, candidate, rotation, pieces, random, false);
            }
        }
    }

    private static List<BlockPos> allPositions(RandomSource random, BlockPos origin) {
        List<BlockPos> positions = new ArrayList<>();
        positions.add(origin.offset(-16 + Mth.nextInt(random, 1, 8), 0, 16 + Mth.nextInt(random, 1, 7)));
        positions.add(origin.offset(-16 + Mth.nextInt(random, 1, 8), 0, Mth.nextInt(random, 1, 7)));
        positions.add(origin.offset(-16 + Mth.nextInt(random, 1, 8), 0, -16 + Mth.nextInt(random, 4, 8)));
        positions.add(origin.offset(Mth.nextInt(random, 1, 7), 0, 16 + Mth.nextInt(random, 1, 7)));
        positions.add(origin.offset(Mth.nextInt(random, 1, 7), 0, -16 + Mth.nextInt(random, 4, 6)));
        positions.add(origin.offset(16 + Mth.nextInt(random, 1, 7), 0, 16 + Mth.nextInt(random, 3, 8)));
        positions.add(origin.offset(16 + Mth.nextInt(random, 1, 7), 0, Mth.nextInt(random, 1, 7)));
        positions.add(origin.offset(16 + Mth.nextInt(random, 1, 7), 0, -16 + Mth.nextInt(random, 4, 8)));
        return positions;
    }

    public static final class SunkenRuinPiece extends TemplateStructurePiece {
        private final float integrity;
        private final boolean large;

        SunkenRuinPiece(StructureTemplateManager manager, ResourceLocation template, BlockPos position,
                        Rotation rotation, float integrity, boolean large) {
            super(BMStructures.SUNKEN_RUIN_PIECE, 0, manager, template, template.toString(),
                settings(rotation), position);
            this.integrity = integrity;
            this.large = large;
        }

        public SunkenRuinPiece(StructurePieceSerializationContext context, CompoundTag tag) {
            super(BMStructures.SUNKEN_RUIN_PIECE, tag, context.structureTemplateManager(),
                template -> settings(Rotation.valueOf(tag.getStringOr("Rot", Rotation.NONE.name()))));
            this.integrity = tag.getFloatOr("Integrity", 1.0F);
            this.large = tag.getBooleanOr("IsLarge", false);
        }

        private static StructurePlaceSettings settings(Rotation rotation) {
            return new StructurePlaceSettings().setRotation(rotation).setMirror(Mirror.NONE)
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
            super.addAdditionalSaveData(context, tag);
            tag.putString("Rot", placeSettings.getRotation().name());
            tag.putFloat("Integrity", integrity);
            tag.putBoolean("IsLarge", large);
        }

        @Override
        protected void handleDataMarker(String metadata, BlockPos position, ServerLevelAccessor level,
                                        RandomSource random, BoundingBox bounds) {
            if ("chest".equals(metadata)) {
                level.setBlock(position, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.WATERLOGGED,
                    level.getFluidState(position).is(FluidTags.WATER)), 2);
                if (level.getBlockEntity(position) instanceof ChestBlockEntity chest) {
                    chest.setLootTable(LOOT);
                    chest.setLootTableSeed(random.nextLong());
                }
            } else if ("witch".equals(metadata) && random.nextBoolean()
                       && level.getBlockState(position.above()).isAir()) {
                Witch witch = EntityType.WITCH.create(level.getLevel(), EntitySpawnReason.STRUCTURE);
                if (witch != null) {
                    witch.setPersistenceRequired();
                    witch.snapTo(position, 0.0F, 0.0F);
                    witch.finalizeSpawn(level, level.getCurrentDifficultyAt(position), EntitySpawnReason.STRUCTURE,
                        null);
                    level.addFreshEntityWithPassengers(witch);
                    level.setBlock(position, position.getY() >= level.getSeaLevel()
                        ? Blocks.AIR.defaultBlockState() : Blocks.WATER.defaultBlockState(), 2);
                }
            }
        }

        @Override
        public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                                RandomSource random, BoundingBox bounds, ChunkPos chunkPos, BlockPos pivot) {
            placeSettings.clearProcessors().addProcessor(new BlockRotProcessor(integrity))
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
            int initialY = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG,
                templatePosition.getX(), templatePosition.getZ()) - Mth.randomBetweenInclusive(random, 1, 3);
            templatePosition = new BlockPos(templatePosition.getX(), initialY, templatePosition.getZ());
            BlockPos opposite = StructureTemplate.transform(
                new BlockPos(template.getSize().getX() - 1, 0, template.getSize().getZ() - 1),
                Mirror.NONE, placeSettings.getRotation(), BlockPos.ZERO).offset(templatePosition);
            templatePosition = new BlockPos(templatePosition.getX(), fitHeight(templatePosition, level, opposite),
                templatePosition.getZ());
            super.postProcess(level, structureManager, generator, random, bounds, chunkPos, pivot);
        }

        private static int fitHeight(BlockPos start, BlockGetter level, BlockPos opposite) {
            int result = start.getY();
            int minimum = 512;
            int reference = result - 1;
            int deepColumns = 0;
            for (BlockPos position : BlockPos.betweenClosed(start, opposite)) {
                int x = position.getX();
                int z = position.getZ();
                int y = start.getY() - 1;
                BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, y, z);
                BlockState state = level.getBlockState(cursor);
                FluidState fluid = level.getFluidState(cursor);
                while ((state.isAir() || fluid.is(FluidTags.WATER) || state.is(BlockTags.ICE))
                       && y > level.getMinY() + 1) {
                    cursor.set(x, --y, z);
                    state = level.getBlockState(cursor);
                    fluid = level.getFluidState(cursor);
                }
                minimum = Math.min(minimum, y);
                if (y < reference - 2) {
                    deepColumns++;
                }
            }
            int xSpan = Math.abs(start.getX() - opposite.getX());
            if (reference - minimum > 2 && deepColumns > xSpan - 2) {
                result = minimum + 1;
            }
            return result;
        }
    }
}
