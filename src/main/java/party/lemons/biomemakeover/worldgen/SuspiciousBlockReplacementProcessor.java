package party.lemons.biomemakeover.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.LootTable;
import party.lemons.biomemakeover.init.BMStructureProcessors;

/** Local equivalent of Taniwha's suspicious-block replacement processor. */
public final class SuspiciousBlockReplacementProcessor extends StructureProcessor {
    public static final MapCodec<SuspiciousBlockReplacementProcessor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        BuiltInRegistries.BLOCK.byNameCodec().fieldOf("target").forGetter(p -> p.target),
        BuiltInRegistries.BLOCK.byNameCodec().fieldOf("output_regular").forGetter(p -> p.outputRegular),
        BuiltInRegistries.BLOCK.byNameCodec().fieldOf("output_suspicious").forGetter(p -> p.outputSuspicious),
        Codec.FLOAT.fieldOf("suspicious_chance").forGetter(p -> p.suspiciousChance),
        LootTable.KEY_CODEC.fieldOf("loot_table").forGetter(p -> p.lootTable)
    ).apply(instance, SuspiciousBlockReplacementProcessor::new));

    private final Block target, outputRegular, outputSuspicious;
    private final float suspiciousChance;
    private final ResourceKey<LootTable> lootTable;

    public SuspiciousBlockReplacementProcessor(Block target, Block outputRegular, Block outputSuspicious,
                                               float suspiciousChance, ResourceKey<LootTable> lootTable) {
        this.target = target; this.outputRegular = outputRegular; this.outputSuspicious = outputSuspicious;
        this.suspiciousChance = suspiciousChance; this.lootTable = lootTable;
    }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader level, BlockPos origin, BlockPos pivot,
                                                              StructureTemplate.StructureBlockInfo original,
                                                              StructureTemplate.StructureBlockInfo current,
                                                              StructurePlaceSettings settings) {
        if (!current.state().is(target)) return current;
        RandomSource random = settings.getRandom(current.pos());
        if (random.nextFloat() >= suspiciousChance)
            return new StructureTemplate.StructureBlockInfo(current.pos(), outputRegular.defaultBlockState(), current.nbt());
        CompoundTag nbt = current.nbt() == null ? new CompoundTag() : current.nbt().copy();
        nbt.putString("LootTable", lootTable.location().toString());
        nbt.putLong("LootTableSeed", random.nextLong());
        return new StructureTemplate.StructureBlockInfo(current.pos(), outputSuspicious.defaultBlockState(), nbt);
    }

    @Override
    protected StructureProcessorType<?> getType() { return BMStructureProcessors.SUSPICIOUS_BLOCK_REPLACEMENT; }
}
