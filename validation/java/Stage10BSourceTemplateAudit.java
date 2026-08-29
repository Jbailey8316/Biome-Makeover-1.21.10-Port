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
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.TreeMap;

/** Audit-only reader for the nine final 1.20.1 Sunken Ruin templates. */
public final class Stage10BSourceTemplateAudit {
    private static final List<String> TEMPLATES = List.of(
        "sunken_1", "sunken_2", "sunken_3",
        "sunken_small_1", "sunken_small_2", "sunken_small_3",
        "sunken_small_4", "sunken_small_5", "sunken_small_6"
    );

    private Stage10BSourceTemplateAudit() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected the final 1.20.1 resource root");
        }

        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        loadBiomeMakeoverBlocksInRegistrationWindow();

        Path root = Path.of(args[0]).toAbsolutePath().normalize();
        System.out.println("id\tbytes\tsha256\tdataVersion\tsize\tpalette\tstoredBlocks\tplacedBlocks\tentities\tjigsaws\tchests\tmarkers\tblockEntities\tmarkerKinds\tblockEntityKinds\tlootTables");
        for (String name : TEMPLATES) {
            Path path = root.resolve("data/biomemakeover/structures/sunken_ruins/" + name + ".nbt");
            byte[] bytes = Files.readAllBytes(path);
            CompoundTag raw;
            try (InputStream input = Files.newInputStream(path)) {
                raw = NbtIo.readCompressed(input, NbtAccounter.unlimitedHeap());
            }

            int dataVersion = NbtUtils.getDataVersion(raw, 500);
            CompoundTag upgraded = DataFixTypes.STRUCTURE.updateToCurrentVersion(
                DataFixers.getDataFixer(), raw, dataVersion
            );
            ListTag palette = upgraded.getListOrEmpty("palette");
            ListTag blocks = upgraded.getListOrEmpty("blocks");
            ListTag entities = upgraded.getListOrEmpty("entities");
            Map<String, Integer> markerKinds = new TreeMap<>();
            Map<String, Integer> blockEntityKinds = new TreeMap<>();
            Map<String, Integer> lootTables = new TreeMap<>();
            Set<String> biomeMakeoverBlocks = new TreeSet<>();
            Set<String> blockEntityNbt = new TreeSet<>();
            int placedBlocks = 0;
            int jigsaws = 0;
            int chests = 0;
            int markers = 0;
            int blockEntities = 0;

            for (var paletteEntry : palette) {
                CompoundTag state = (CompoundTag) paletteEntry;
                ResourceLocation blockId = ResourceLocation.parse(state.getStringOr("Name", ""));
                require(BuiltInRegistries.BLOCK.containsKey(blockId), name + " has unresolved block " + blockId);
                if (blockId.getNamespace().equals("biomemakeover")) {
                    biomeMakeoverBlocks.add(blockId.toString());
                }
            }
            for (var blockEntry : blocks) {
                CompoundTag block = (CompoundTag) blockEntry;
                int stateIndex = block.getIntOr("state", -1);
                require(stateIndex >= 0 && stateIndex < palette.size(), name + " has invalid palette index " + stateIndex);
                CompoundTag state = palette.getCompoundOrEmpty(stateIndex);
                String blockId = state.getStringOr("Name", "");
                CompoundTag nbt = block.getCompoundOrEmpty("nbt");
                if (!blockId.equals("minecraft:air")
                    && !blockId.equals("minecraft:structure_void")
                    && !blockId.equals("minecraft:structure_block")) {
                    placedBlocks++;
                }
                if (blockId.equals("minecraft:jigsaw")) {
                    jigsaws++;
                } else if (blockId.equals("minecraft:chest")) {
                    chests++;
                } else if (blockId.equals("minecraft:structure_block")) {
                    markers++;
                    markerKinds.merge(nbt.getStringOr("metadata", "<none>"), 1, Integer::sum);
                }
                if (!nbt.isEmpty() && !blockId.equals("minecraft:structure_block")) {
                    blockEntities++;
                    blockEntityKinds.merge(blockId, 1, Integer::sum);
                    blockEntityNbt.add(nbt.toString());
                }
                String lootTable = nbt.getStringOr("LootTable", "");
                if (!lootTable.isEmpty()) {
                    lootTables.merge(lootTable, 1, Integer::sum);
                }
            }

            StructureTemplate template = new StructureTemplate();
            template.load(BuiltInRegistries.BLOCK, upgraded);
            var size = template.getSize();
            require(size.getX() > 0 && size.getY() > 0 && size.getZ() > 0, name + " decoded empty");
            require(template.getJigsaws(net.minecraft.core.BlockPos.ZERO, net.minecraft.world.level.block.Rotation.NONE).size() == jigsaws,
                name + " jigsaw count changed during StructureTemplate load");

            String sha256 = HexFormat.of().withUpperCase().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
            System.out.printf(
                "biomemakeover:sunken_ruins/%s\t%d\t%s\t%d\t%dx%dx%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%s\t%s\t%s%n",
                name, bytes.length, sha256, dataVersion,
                size.getX(), size.getY(), size.getZ(), palette.size(), blocks.size(), placedBlocks, entities.size(),
                jigsaws, chests, markers, blockEntities, markerKinds, blockEntityKinds, lootTables
            );
            System.out.println("  bmBlocks=" + biomeMakeoverBlocks);
            if (!blockEntityNbt.isEmpty()) {
                System.out.println("  blockEntityNbt=" + blockEntityNbt);
            }
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static void loadBiomeMakeoverBlocksInRegistrationWindow() throws Exception {
        Field frozen = MappedRegistry.class.getDeclaredField("frozen");
        Field intrusive = MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");
        frozen.setAccessible(true);
        intrusive.setAccessible(true);
        for (Registry<?> registry : BuiltInRegistries.REGISTRY) {
            if (registry instanceof MappedRegistry<?> mapped) {
                frozen.setBoolean(mapped, false);
                if ((registry == BuiltInRegistries.BLOCK || registry == BuiltInRegistries.ITEM)
                    && intrusive.get(mapped) == null) {
                    intrusive.set(mapped, new IdentityHashMap<>());
                }
            }
        }
        Class.forName("party.lemons.biomemakeover.init.BMBlocks");
    }
}
