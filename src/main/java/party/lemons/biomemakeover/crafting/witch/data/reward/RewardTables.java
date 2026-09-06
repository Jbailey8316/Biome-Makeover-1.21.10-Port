package party.lemons.biomemakeover.crafting.witch.data.reward;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import party.lemons.biomemakeover.crafting.witch.QuestRarity;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import party.lemons.biomemakeover.BiomeMakeover;

public final class RewardTables extends SimplePreparableReloadListener<Map<ResourceLocation, JsonElement>> implements IdentifiableResourceReloadListener {
    private static final RewardTables INSTANCE = new RewardTables();
    private static final Map<QuestRarity, List<Table>> TABLES = new EnumMap<>(QuestRarity.class);
    private final Gson gson = new Gson();
    private RewardTables() {}
    public static RewardTables instance() { return INSTANCE; }
    public static ItemStack getReward(QuestRarity rarity, RandomSource random) {
        List<Table> tables = TABLES.get(rarity);
        if (tables == null || tables.isEmpty()) return ItemStack.EMPTY;
        int total = tables.stream().mapToInt(t -> t.weight(rarity)).sum();
        int roll = random.nextInt(Math.max(1, total));
        for (Table table : tables) { roll -= table.weight(rarity); if (roll < 0) return table.reward(random); }
        return tables.get(0).reward(random);
    }
    @Override protected Map<ResourceLocation, JsonElement> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, JsonElement> result = new java.util.HashMap<>();
        FileToIdConverter converter = FileToIdConverter.json("quest_reward");
        for (var entry : converter.listMatchingResources(manager).entrySet()) {
            try (var reader = entry.getValue().openAsReader()) { result.put(converter.fileToId(entry.getKey()), gson.fromJson(reader, JsonElement.class)); }
            catch (IOException e) { throw new IllegalStateException("Unable to read " + entry.getKey(), e); }
        }
        return result;
    }
    @Override protected void apply(Map<ResourceLocation, JsonElement> data, ResourceManager manager, ProfilerFiller profiler) {
        TABLES.clear(); for (QuestRarity rarity : QuestRarity.values()) TABLES.put(rarity, new ArrayList<>());
        for (JsonElement element : data.values()) {
            JsonObject object = element.getAsJsonObject(); JsonObject weights = object.getAsJsonObject("weights");
            Table table = new Table(weights.get("common").getAsInt(), weights.get("uncommon").getAsInt(), weights.get("rare").getAsInt(), weights.get("epic").getAsInt(), object.getAsJsonArray("rewards"));
            for (QuestRarity rarity : QuestRarity.values()) if (table.weight(rarity) > 0) TABLES.get(rarity).add(table);
        }
        BiomeMakeover.LOGGER.info("[BM_WITCH_QUEST_TRACE] RELOAD_REWARDS tablesLoaded={} ids={} rarityTables={}", data.size(), data.keySet(), TABLES);
    }
    private record Table(int common, int uncommon, int rare, int epic, com.google.gson.JsonArray rewards) {
        int weight(QuestRarity rarity) { return switch (rarity) { case COMMON -> common; case UNCOMMON -> uncommon; case RARE -> rare; case EPIC -> epic; }; }
        ItemStack reward(RandomSource random) {
            JsonObject reward = rewards.get(random.nextInt(rewards.size())).getAsJsonObject();
            String type = reward.get("type").getAsString();
            if (type.endsWith(":item")) {
                Item item = BuiltInRegistries.ITEM.getValue(ResourceLocation.parse(reward.get("item").getAsString()));
                int min = reward.has("min") ? reward.get("min").getAsInt() : 1;
                int max = reward.has("max") ? reward.get("max").getAsInt() : min;
                return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item, min == max ? min : min + random.nextInt(max - min + 1));
            }
            if (type.endsWith(":potion")) {
                ResourceLocation id = ResourceLocation.parse(reward.get("potion").getAsString());
                var holder = BuiltInRegistries.POTION.get(ResourceKey.create(Registries.POTION, id));
                if (holder.isEmpty()) return ItemStack.EMPTY;
                Item item = random.nextInt(4) == 0 ? Items.POTION : random.nextInt(3) == 0 ? Items.LINGERING_POTION : Items.SPLASH_POTION;
                return PotionContents.createItemStack(item, holder.get());
            }
            return ItemStack.EMPTY;
        }
    }
    @Override public ResourceLocation getFabricId() { return BiomeMakeover.id("quest_rewards"); }
}
