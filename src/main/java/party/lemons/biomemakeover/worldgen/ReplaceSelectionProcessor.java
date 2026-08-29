package party.lemons.biomemakeover.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import party.lemons.biomemakeover.init.BMStructureProcessors;

/**
 * The narrow local equivalent of the final release's Taniwha
 * {@code replace_selection} processor. Mushroom House flower pots are the
 * only released BM resource that needs this target + state-provider contract.
 */
public final class ReplaceSelectionProcessor extends StructureProcessor {
    public static final MapCodec<ReplaceSelectionProcessor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        BuiltInRegistries.BLOCK.byNameCodec().fieldOf("target").forGetter(processor -> processor.target),
        BlockStateProvider.CODEC.fieldOf("output").forGetter(processor -> processor.output)
    ).apply(instance, ReplaceSelectionProcessor::new));

    private final Block target;
    private final BlockStateProvider output;

    public ReplaceSelectionProcessor(Block target, BlockStateProvider output) {
        this.target = target;
        this.output = output;
    }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(
        LevelReader level,
        BlockPos structureOrigin,
        BlockPos pivot,
        StructureTemplate.StructureBlockInfo original,
        StructureTemplate.StructureBlockInfo current,
        StructurePlaceSettings settings
    ) {
        if (!current.state().is(target)) {
            return current;
        }
        return new StructureTemplate.StructureBlockInfo(
            current.pos(),
            output.getState(settings.getRandom(current.pos()), current.pos()),
            current.nbt()
        );
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return BMStructureProcessors.REPLACE_SELECTION;
    }
}
