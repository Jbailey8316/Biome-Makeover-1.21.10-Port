package party.lemons.biomemakeover.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.util.ProblemReporter;
import party.lemons.biomemakeover.init.BMStructureProcessors;

/** Faithful local replacement for the release's Taniwha bookshelf processor. */
public final class FillBookshelvesProcessor extends StructureProcessor {
    public static final MapCodec<FillBookshelvesProcessor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        com.mojang.serialization.Codec.FLOAT.fieldOf("replace_chance").forGetter(p -> p.replaceChance),
        com.mojang.serialization.Codec.FLOAT.fieldOf("fill_chance").forGetter(p -> p.fillChance),
        com.mojang.serialization.Codec.FLOAT.fieldOf("enchant_chance").forGetter(p -> p.enchantChance),
        IntProvider.codec(0, 6).fieldOf("book_amount").forGetter(p -> p.bookAmount),
        IntProvider.codec(0, 100).fieldOf("enchantment_level").forGetter(p -> p.enchantmentLevel)
    ).apply(instance, FillBookshelvesProcessor::new));

    private final float replaceChance, fillChance, enchantChance;
    private final IntProvider bookAmount, enchantmentLevel;

    public FillBookshelvesProcessor(float replaceChance, float fillChance, float enchantChance,
                                    IntProvider bookAmount, IntProvider enchantmentLevel) {
        this.replaceChance = replaceChance; this.fillChance = fillChance; this.enchantChance = enchantChance;
        this.bookAmount = bookAmount; this.enchantmentLevel = enchantmentLevel;
    }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader level, BlockPos origin, BlockPos pivot,
                                                              StructureTemplate.StructureBlockInfo original,
                                                              StructureTemplate.StructureBlockInfo current,
                                                              StructurePlaceSettings settings) {
        BlockState state = current.state();
        if (!state.is(Blocks.CHISELED_BOOKSHELF)) return current;
        RandomSource random = settings.getRandom(current.pos());
        if (random.nextFloat() < replaceChance)
            return new StructureTemplate.StructureBlockInfo(current.pos(), Blocks.BOOKSHELF.defaultBlockState(), null);
        if (random.nextFloat() > fillChance) return current;
        NonNullList<ItemStack> items = NonNullList.withSize(6, ItemStack.EMPTY);
        int amount = bookAmount.sample(random);
        for (int i = 0; i < amount; i++) {
            ItemStack book = new ItemStack(Items.BOOK);
            if (random.nextFloat() < enchantChance) {
                book = EnchantmentHelper.enchantItem(random, book, enchantmentLevel.sample(random),
                    level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).stream()
                        .map(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)::wrapAsHolder));
            }
            int index = random.nextInt(items.size());
            items.set(index, book);
            state = state.setValue(ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.get(index), true);
        }
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        ContainerHelper.saveAllItems(output, items, true);
        return new StructureTemplate.StructureBlockInfo(current.pos(), state, output.buildResult());
    }

    @Override
    protected StructureProcessorType<?> getType() { return BMStructureProcessors.FILL_BOOKSHELVES; }
}
