import net.minecraft.SharedConstants;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.IdentityHashMap;
import java.util.List;

/** DataFix + StructureTemplate.load validation for the complete Ghost Town graph. */
public final class Stage10C4TemplateRuntimeValidator {
    private static final List<String> TEMPLATES = List.of(
        "ghosttown/centers/crossroads_01",
        "ghosttown/decoration/barrel_decoration", "ghosttown/decoration/bell_decoration_1", "ghosttown/decoration/cactus_decoration",
        "ghosttown/decoration/hay_decoration", "ghosttown/decoration/hay_decoration_2", "ghosttown/decoration/hay_well_decoration",
        "ghosttown/decoration/lamp_decoration", "ghosttown/decoration/lamp_decoration_2", "ghosttown/decoration/tree_decoration_1",
        "ghosttown/decoration/tree_decoration_2", "ghosttown/decoration/trough_decoration", "ghosttown/decoration/water_tower_1",
        "ghosttown/decoration/water_tower_2", "ghosttown/decoration/water_tower_3", "ghosttown/decoration/well_decoration",
        "ghosttown/houses/house_large_01", "ghosttown/houses/house_large_02", "ghosttown/houses/house_large_03", "ghosttown/houses/house_large_04", "ghosttown/houses/house_large_05",
        "ghosttown/houses/house_medium_01", "ghosttown/houses/house_medium_02", "ghosttown/houses/house_medium_03", "ghosttown/houses/house_medium_04", "ghosttown/houses/house_medium_05", "ghosttown/houses/house_medium_06", "ghosttown/houses/house_medium_07",
        "ghosttown/houses/house_small_01", "ghosttown/houses/house_small_02", "ghosttown/houses/house_small_03", "ghosttown/houses/house_small_04", "ghosttown/houses/house_small_05", "ghosttown/houses/house_small_06", "ghosttown/houses/house_small_07", "ghosttown/houses/house_small_08", "ghosttown/houses/house_small_09", "ghosttown/houses/house_small_10", "ghosttown/houses/house_small_11", "ghosttown/houses/house_small_12", "ghosttown/houses/house_small_13", "ghosttown/houses/house_small_14", "ghosttown/houses/house_small_15",
        "ghosttown/roads/street_01", "ghosttown/roads/street_02", "ghosttown/roads/street_03", "ghosttown/roads/street_04", "ghosttown/roads/street_05", "ghosttown/roads/street_06", "ghosttown/roads/street_07"
    );

    private Stage10C4TemplateRuntimeValidator() {}

    public static void main(String[] args) throws Exception {
        if (args.length < 1 || args.length > 2) throw new IllegalArgumentException("Expected resource root and optional --packaged");
        boolean packaged = args.length == 2 && args[1].equals("--packaged");
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        loadBiomeMakeoverBlocks();
        Path root = Path.of(args[0]).toAbsolutePath().normalize();
        int totalBlocks = 0;
        for (String id : TEMPLATES) {
            Path path = root.resolve("data/biomemakeover/structure/" + id + ".nbt");
            require(Files.isRegularFile(path), "Missing template " + id);
            CompoundTag raw;
            try (InputStream input = Files.newInputStream(path)) {
                raw = NbtIo.readCompressed(input, NbtAccounter.unlimitedHeap());
            }
            int dataVersion = NbtUtils.getDataVersion(raw, 500);
            CompoundTag upgraded = DataFixTypes.STRUCTURE.updateToCurrentVersion(DataFixers.getDataFixer(), raw, dataVersion);
            ListTag palette = upgraded.getListOrEmpty("palette");
            ListTag blocks = upgraded.getListOrEmpty("blocks");
            require(!palette.isEmpty() && !blocks.isEmpty(), id + " decoded without palette/blocks");
            for (var paletteEntry : palette) {
                CompoundTag state = (CompoundTag) paletteEntry;
                String blockName = state.getStringOr("Name", "");
                require(!blockName.isEmpty() && BuiltInRegistries.BLOCK.containsKey(ResourceLocation.parse(blockName)),
                    id + " has unresolved block " + blockName);
            }
            for (var blockEntry : blocks) {
                CompoundTag block = (CompoundTag) blockEntry;
                int state = block.getIntOr("state", -1);
                require(state >= 0 && state < palette.size(), id + " has invalid palette index " + state);
            }
            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK, upgraded);
            require(template.getSize().getX() > 0 && template.getSize().getY() > 0 && template.getSize().getZ() > 0,
                id + " loaded an empty StructureTemplate");
            totalBlocks += blocks.size();
            System.out.printf("%s dataVersion=%d size=%s palette=%d blocks=%d entities=%d%n",
                id, dataVersion, template.getSize(), palette.size(), blocks.size(), upgraded.getListOrEmpty("entities").size());
        }
        System.out.printf("STAGE 10C.4 %s TEMPLATE DATAFIX/LOAD PASSED (templates=%d blocks=%d)%n",
            packaged ? "PACKAGED" : "SOURCE", TEMPLATES.size(), totalBlocks);
    }

    private static void loadBiomeMakeoverBlocks() throws Exception {
        Field frozen = MappedRegistry.class.getDeclaredField("frozen");
        Field intrusive = MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");
        frozen.setAccessible(true);
        intrusive.setAccessible(true);
        for (Registry<?> registry : BuiltInRegistries.REGISTRY) {
            if (registry instanceof MappedRegistry<?> mapped) {
                frozen.setBoolean(mapped, false);
                if ((registry == BuiltInRegistries.BLOCK || registry == BuiltInRegistries.ITEM) && intrusive.get(mapped) == null) {
                    intrusive.set(mapped, new IdentityHashMap<>());
                }
            }
        }
        Class.forName("party.lemons.biomemakeover.init.BMBlocks");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
