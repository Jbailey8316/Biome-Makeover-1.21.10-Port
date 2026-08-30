package party.lemons.biomemakeover.worldgen.mansion;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
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
import party.lemons.biomemakeover.init.BMStructures;

import java.util.Optional;

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
        // No template corpus is packaged in 11A.1. Keeping the registered
        // codec inert prevents incomplete Mansion worldgen from activating.
        return Optional.empty();
    }

    @Override public StructureType<?> type() { return BMStructures.MANSION; }

    public MansionTemplates templates() { return templates; }
    public MansionDetails details() { return details; }

    /** Serialized custom template piece; marker actions are connected later. */
    public static final class Piece extends TemplateStructurePiece {
        private final boolean ground;
        private final boolean wall;

        public Piece(StructureTemplateManager manager, ResourceLocation template, BlockPos position,
                     Rotation rotation, boolean ground, boolean wall) {
            super(BMStructures.MANSION_PIECE, 0, manager, template, template.toString(),
                settings(rotation, wall), position);
            this.ground = ground;
            this.wall = wall;
        }

        public Piece(StructurePieceSerializationContext context, CompoundTag tag) {
            super(BMStructures.MANSION_PIECE, tag, context.structureTemplateManager(),
                ignored -> settings(Rotation.valueOf(tag.getStringOr("Rotation", Rotation.NONE.name())),
                    tag.getBooleanOr("IsWall", false)));
            this.ground = tag.getBooleanOr("Ground", false);
            this.wall = tag.getBooleanOr("IsWall", false);
        }

        private static StructurePlaceSettings settings(Rotation rotation, boolean wall) {
            return new StructurePlaceSettings().setIgnoreEntities(true)
                .setRotation(rotation).setMirror(Mirror.NONE)
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
        }

        @Override protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
            super.addAdditionalSaveData(context, tag);
            tag.putString("Rotation", placeSettings.getRotation().name());
            tag.putBoolean("Ground", ground);
            tag.putBoolean("IsWall", wall);
        }

        @Override protected void handleDataMarker(String metadata, BlockPos position, ServerLevelAccessor level,
                                                   RandomSource random, BoundingBox bounds) {
            // Marker dispatch is deliberately deferred to 11A.2/11B.
        }
    }
}
