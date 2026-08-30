import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.util.valueproviders.IntProvider;
import party.lemons.biomemakeover.worldgen.FillBookshelvesProcessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Decodes the Ghost Town building processor through the same 1.21.10
 * MapCodec used by the runtime processor registry. This intentionally lives
 * in validation/ rather than production code so the audit cannot silently
 * diverge from Minecraft's dynamic-registry codec.
 */
public final class Stage10C4ProcessorCodecValidator {
    private Stage10C4ProcessorCodecValidator() {}

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: Stage10C4ProcessorCodecValidator <ghosttown_building.json>");
        }
        // IntProvider dispatches through the built-in registry. Bootstrap the
        // same vanilla registry set used by dynamic worldgen loading before
        // touching the processor codec.
        SharedConstants.tryDetectVersion();
        try {
            Class.forName("net.minecraft.server.Bootstrap").getMethod("bootStrap").invoke(null);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not bootstrap Minecraft built-in registries", exception);
        }
        Path path = Path.of(args[0]);
        JsonObject root = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
        JsonArray processors = root.getAsJsonArray("processors");
        JsonObject bookshelf = null;
        for (int i = 0; i < processors.size(); i++) {
            JsonObject candidate = processors.get(i).getAsJsonObject();
            if ("biomemakeover:fill_bookshelves".equals(candidate.get("processor_type").getAsString())) {
                if (bookshelf != null) {
                    throw new IllegalStateException("ghosttown_building contains duplicate fill_bookshelves processors");
                }
                bookshelf = candidate;
            }
        }
        if (bookshelf == null) {
            throw new IllegalStateException("ghosttown_building is missing fill_bookshelves");
        }

        DataResult<FillBookshelvesProcessor> decoded = FillBookshelvesProcessor.CODEC.codec()
            .parse(JsonOps.INSTANCE, bookshelf);
        decoded.resultOrPartial(error -> {
            throw new IllegalStateException("Minecraft 1.21.10 processor codec rejected ghosttown_building: " + error);
        }).orElseThrow(() -> new IllegalStateException("Minecraft 1.21.10 processor codec returned no value"));

        JsonObject level = bookshelf.getAsJsonObject("enchantment_level");
        if (!"minecraft:weighted_list".equals(level.get("type").getAsString())) {
            throw new IllegalStateException("enchantment_level is not a weighted_list provider");
        }
        JsonArray distribution = level.getAsJsonArray("distribution");
        int[] weights = {20, 10, 4, 1};
        int[] mins = {1, 3, 7, 20};
        int[] maxes = {5, 10, 15, 35};
        if (distribution.size() != weights.length) {
            throw new IllegalStateException("enchantment_level weighted distribution must contain four entries");
        }
        for (int i = 0; i < distribution.size(); i++) {
            JsonObject entry = distribution.get(i).getAsJsonObject();
            JsonObject provider = entry.getAsJsonObject("data");
            if (!"minecraft:uniform".equals(provider.get("type").getAsString())) {
                throw new IllegalStateException("enchantment_level entry " + i + " is not UniformInt");
            }
            if (provider.has("value")) {
                throw new IllegalStateException("enchantment_level entry " + i + " uses obsolete nested UniformInt value");
            }
            int entryIndex = i;
            DataResult<IntProvider> parsed = IntProvider.codec(0, 100).parse(JsonOps.INSTANCE, provider);
            IntProvider value = parsed.resultOrPartial(error -> {
                throw new IllegalStateException("Minecraft 1.21.10 UniformInt codec rejected entry " + entryIndex + ": " + error);
            }).orElseThrow(() -> new IllegalStateException("UniformInt codec returned no value for entry " + entryIndex));
            if (entry.get("weight").getAsInt() != weights[i] ||
                value.getMinValue() != mins[i] || value.getMaxValue() != maxes[i]) {
                throw new IllegalStateException("enchantment_level entry " + i + " does not preserve released weight/range semantics");
            }
        }
        System.out.println("STAGE 10C.4 GHOST TOWN PROCESSOR CODEC PASSED (UniformInt weighted ranges=4)");
    }
}
