package party.lemons.biomemakeover.worldgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.init.BMStructureProcessors;

/** Assigns one of the three released Ghost Town barrel loot tables. */
public final class GhostTownLootProcessor extends StructureProcessor {
    public static final MapCodec<GhostTownLootProcessor> CODEC = MapCodec.unit(GhostTownLootProcessor::new);

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader level, BlockPos origin, BlockPos pivot,
                                                              StructureTemplate.StructureBlockInfo original,
                                                              StructureTemplate.StructureBlockInfo current,
                                                              StructurePlaceSettings settings) {
        BlockState state = current.state();
        if (state.is(Blocks.BARREL)) {
            CompoundTag nbt = current.nbt() == null ? new CompoundTag() : current.nbt().copy();
            if (!nbt.contains("LootTable")) {
                nbt.putString("LootTable", BiomeMakeover.id("ghost_town/loot_" + settings.getRandom(current.pos()).nextInt(3)).toString());
            }
            return new StructureTemplate.StructureBlockInfo(current.pos(), state, nbt);
        }
        return current;
    }

    @Override
    protected StructureProcessorType<?> getType() { return BMStructureProcessors.GHOST_TOWN_LOOT; }
}
