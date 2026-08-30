package party.lemons.biomemakeover.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.worldgen.ReplaceSelectionProcessor;
import party.lemons.biomemakeover.worldgen.GhostTownLootProcessor;
import party.lemons.biomemakeover.worldgen.FillBookshelvesProcessor;
import party.lemons.biomemakeover.worldgen.SuspiciousBlockReplacementProcessor;

public final class BMStructureProcessors {
    public static final StructureProcessorType<ReplaceSelectionProcessor> REPLACE_SELECTION = Registry.register(
        BuiltInRegistries.STRUCTURE_PROCESSOR,
        BiomeMakeover.id("replace_selection"),
        () -> ReplaceSelectionProcessor.CODEC
    );
    public static final StructureProcessorType<GhostTownLootProcessor> GHOST_TOWN_LOOT = Registry.register(
        BuiltInRegistries.STRUCTURE_PROCESSOR,
        BiomeMakeover.id("ghost_town_loot"),
        () -> GhostTownLootProcessor.CODEC
    );
    public static final StructureProcessorType<FillBookshelvesProcessor> FILL_BOOKSHELVES = Registry.register(
        BuiltInRegistries.STRUCTURE_PROCESSOR,
        BiomeMakeover.id("fill_bookshelves"),
        () -> FillBookshelvesProcessor.CODEC
    );
    public static final StructureProcessorType<SuspiciousBlockReplacementProcessor> SUSPICIOUS_BLOCK_REPLACEMENT = Registry.register(
        BuiltInRegistries.STRUCTURE_PROCESSOR,
        BiomeMakeover.id("suspicious_block_replacement"),
        () -> SuspiciousBlockReplacementProcessor.CODEC
    );

    private BMStructureProcessors() {}

    public static void initialize() {
        // Class loading performs registration.
    }
}
