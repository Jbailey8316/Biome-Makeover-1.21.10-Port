package party.lemons.biomemakeover.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import net.fabricmc.loader.api.FabricLoader;
import party.lemons.biomemakeover.BiomeMakeover;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Server-authoritative Mythas configuration.  This class intentionally has no
 * dependency on a gameplay feature: it is the stable access boundary future
 * Mythas systems will use.
 */
public final class MythasConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "biomemakeover-mythas.json";
    private static final String ROOT_KEY = "mythas";
    private static final Object LOCK = new Object();
    private static volatile State state = State.ALL_OFF;

    private MythasConfig() {}

    public static void load() {
        synchronized (LOCK) {
            Path path = configPath();
            if (!Files.exists(path)) {
                state = State.ALL_OFF;
                save(path, new JsonObject(), true);
                return;
            }
            try {
                JsonObject document = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
                JsonObject mythas = object(document, ROOT_KEY);
                state = new State(readBoolean(mythas, "enabled", false),
                    readBoolean(mythas, "mansion_trial_wing", false),
                    readBoolean(mythas, "decayed_shield_upgrade", false),
                    readBoolean(mythas, "dynamic_lightning_bugs", false),
                    readBoolean(mythas, "cosmetic_polish", false));
                ensureDefaults(mythas);
                save(path, document, false);
            } catch (Exception exception) {
                state = State.ALL_OFF;
                BiomeMakeover.LOGGER.warn("Could not read {}; all Mythas enhancements remain disabled: {}", path, exception.getMessage());
            }
        }
    }

    public static void reload() { load(); }

    public static boolean isEnabled() { return state.enabled; }
    public static boolean isMansionTrialWingEnabled() { return state.enabled && state.mansionTrialWing; }
    public static boolean isDecayedShieldUpgradeEnabled() { return state.enabled && state.decayedShieldUpgrade; }
    public static boolean isDynamicLightningBugsEnabled() { return state.enabled && state.dynamicLightningBugs; }
    public static boolean isCosmeticPolishEnabled() { return state.enabled && state.cosmeticPolish; }

    // Raw values are intentionally package-private for a future config UI only.
    static boolean rawMansionTrialWing() { return state.mansionTrialWing; }
    static boolean rawDecayedShieldUpgrade() { return state.decayedShieldUpgrade; }
    static boolean rawDynamicLightningBugs() { return state.dynamicLightningBugs; }
    static boolean rawCosmeticPolish() { return state.cosmeticPolish; }

    public static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }

    private static JsonObject object(JsonObject parent, String key) {
        if (parent.has(key) && parent.get(key).isJsonObject()) return parent.getAsJsonObject(key);
        JsonObject child = new JsonObject();
        parent.add(key, child);
        return child;
    }

    private static boolean readBoolean(JsonObject object, String key, boolean fallback) {
        if (!object.has(key)) return fallback;
        if (object.get(key).isJsonPrimitive() && object.getAsJsonPrimitive(key).isBoolean())
            return object.getAsJsonPrimitive(key).getAsBoolean();
        BiomeMakeover.LOGGER.warn("Invalid Mythas config value for '{}'; using safe default OFF", ROOT_KEY + "." + key);
        return fallback;
    }

    private static void ensureDefaults(JsonObject mythas) {
        ensureBoolean(mythas, "enabled", false);
        ensureBoolean(mythas, "mansion_trial_wing", false);
        ensureBoolean(mythas, "decayed_shield_upgrade", false);
        ensureBoolean(mythas, "dynamic_lightning_bugs", false);
        ensureBoolean(mythas, "cosmetic_polish", false);
    }

    private static void ensureBoolean(JsonObject object, String key, boolean value) {
        if (!object.has(key) || !(object.get(key).isJsonPrimitive() && object.getAsJsonPrimitive(key).isBoolean()))
            object.add(key, new JsonPrimitive(value));
    }

    private static void save(Path path, JsonObject document, boolean announce) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(document), StandardCharsets.UTF_8);
            if (announce) BiomeMakeover.LOGGER.info("Created safe-off Mythas config at {}", path);
        } catch (IOException exception) {
            BiomeMakeover.LOGGER.warn("Could not write Mythas config {}; enhancements remain safe-off in memory: {}", path, exception.getMessage());
        }
    }

    private record State(boolean enabled, boolean mansionTrialWing, boolean decayedShieldUpgrade,
                         boolean dynamicLightningBugs, boolean cosmeticPolish) {
        private static final State ALL_OFF = new State(false, false, false, false, false);
    }
}
