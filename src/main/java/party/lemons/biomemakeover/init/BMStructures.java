package party.lemons.biomemakeover.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.worldgen.SunkenRuinStructure;
import party.lemons.biomemakeover.worldgen.mansion.MansionFeature;

/** Built-in registrations required before dynamic structure data is decoded. */
public final class BMStructures {
    public static final StructureType<SunkenRuinStructure> SUNKEN_RUIN = Registry.register(
        BuiltInRegistries.STRUCTURE_TYPE,
        BiomeMakeover.id("sunken_ruin"),
        () -> SunkenRuinStructure.CODEC
    );
    public static final StructurePieceType SUNKEN_RUIN_PIECE = Registry.register(
        BuiltInRegistries.STRUCTURE_PIECE,
        BiomeMakeover.id("sunken_ruin"),
        SunkenRuinStructure.SunkenRuinPiece::new
    );
    public static final StructureType<MansionFeature> MANSION = Registry.register(
        BuiltInRegistries.STRUCTURE_TYPE,
        BiomeMakeover.id("mansion"),
        () -> MansionFeature.CODEC
    );
    public static final StructurePieceType MANSION_PIECE = Registry.register(
        BuiltInRegistries.STRUCTURE_PIECE,
        BiomeMakeover.id("mansion"),
        MansionFeature.Piece::new
    );

    private BMStructures() {}

    public static void initialize() {
        // Class loading performs the bootstrap-safe built-in registrations.
    }
}
