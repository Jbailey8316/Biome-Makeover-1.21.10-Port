package party.lemons.biomemakeover.util.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import party.lemons.biomemakeover.init.BMItems;

import java.util.List;
import java.util.function.Consumer;

/**
 * Released BM nested loot entry.  The modern implementation is codec based,
 * while retaining the released JSON contract: {"type":"biomemakeover:loot_table",
 * "name":"..."}.  It emits the referenced table through the same context.
 */
public final class BetterLootTableReference extends LootPoolSingletonContainer {
    public static final MapCodec<BetterLootTableReference> CODEC = RecordCodecBuilder.mapCodec(i ->
        i.group(LootTable.KEY_CODEC.fieldOf("name").forGetter((BetterLootTableReference entry) -> entry.name))
            .and(singletonFields(i)).apply(i, BetterLootTableReference::new));

    private final ResourceKey<LootTable> name;

    private BetterLootTableReference(ResourceKey<LootTable> name, int weight, int quality,
                                     List<LootItemCondition> conditions, List<LootItemFunction> functions) {
        super(weight, quality, conditions, functions);
        this.name = name;
    }

    @Override public LootPoolEntryType getType() { return BMItems.BETTER_LOOTTABLE_REFERENCE; }

    @Override protected void createItemStack(Consumer<ItemStack> consumer, LootContext context) {
        LootTable table = context.getResolver().get(name).map(Holder.Reference::value).orElse(LootTable.EMPTY);
        table.getRandomItems(context, consumer);
    }

    @Override public void validate(ValidationContext context) {
        super.validate(context);
        if (!context.allowsReferences()) return;
        if (context.hasVisitedElement(name)) {
            context.reportProblem(new ValidationContext.RecursiveReferenceProblem(name));
        } else {
            context.resolver().get(name).ifPresentOrElse(holder ->
                holder.value().validate(context.enterElement(
                    new net.minecraft.util.ProblemReporter.ElementReferencePathElement(name), name)),
                () -> context.reportProblem(new ValidationContext.MissingReferenceProblem(name)));
        }
    }
}
