package party.lemons.biomemakeover.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.worldgen.ReplaceSelectionProcessor;

public final class BMStructureProcessors {
    public static final StructureProcessorType<ReplaceSelectionProcessor> REPLACE_SELECTION = Registry.register(
        BuiltInRegistries.STRUCTURE_PROCESSOR,
        BiomeMakeover.id("replace_selection"),
        () -> ReplaceSelectionProcessor.CODEC
    );

    private BMStructureProcessors() {}

    public static void initialize() {
        // Class loading performs registration.
    }
}
