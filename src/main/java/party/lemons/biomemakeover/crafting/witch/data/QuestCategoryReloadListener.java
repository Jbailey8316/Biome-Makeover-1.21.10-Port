package party.lemons.biomemakeover.crafting.witch.data;

import com.google.gson.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Items;
import party.lemons.biomemakeover.crafting.witch.*;
import java.util.Map;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import party.lemons.biomemakeover.BiomeMakeover;

public final class QuestCategoryReloadListener extends SimplePreparableReloadListener<Map<ResourceLocation, JsonElement>> implements IdentifiableResourceReloadListener {
    public QuestCategoryReloadListener() {}
    @Override protected Map<ResourceLocation, JsonElement> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, JsonElement> result = new java.util.HashMap<>();
        for (var entry : FileToIdConverter.json("quest_category").listMatchingResources(manager).entrySet()) {
            try (var reader = entry.getValue().openAsReader()) { result.put(FileToIdConverter.json("quest_category").fileToId(entry.getKey()), new Gson().fromJson(reader, JsonElement.class)); }
            catch (java.io.IOException exception) { throw new IllegalStateException("Unable to read " + entry.getKey(), exception); }
        }
        return result;
    }
    @Override protected void apply(Map<ResourceLocation, JsonElement> data,ResourceManager manager,ProfilerFiller profiler){QuestCategories.clearCategories();int usable=0;StringBuilder summary=new StringBuilder();for(var entry:data.entrySet()){JsonObject object=entry.getValue().getAsJsonObject();QuestCategory category=new QuestCategory(object.get("weight").getAsInt());int requests=0;for(JsonElement request:object.getAsJsonArray("requests")){JsonObject r=request.getAsJsonObject();var item=BuiltInRegistries.ITEM.getValue(ResourceLocation.parse(r.get("item").getAsString()));if(item!=Items.AIR){category.addItem(new QuestItem(item,r.get("points").getAsFloat(),r.get("max_count").getAsInt()));requests++;}}if(requests>0)usable++;summary.append(entry.getKey()).append("=").append(requests).append(" ");QuestCategories.addCategory(category);}BiomeMakeover.LOGGER.info("[BM_WITCH_QUEST_TRACE] RELOAD categoriesLoaded={} usableCategories={} definitions={}",data.size(),usable,summary.toString().trim()); }
    @Override public ResourceLocation getFabricId() { return BiomeMakeover.id("quest_categories"); }
}
