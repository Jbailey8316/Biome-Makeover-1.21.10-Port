import net.minecraft.SharedConstants;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.HolderSet;
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
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import party.lemons.biomemakeover.worldgen.SunkenRuinStructure;
import party.lemons.biomemakeover.init.BMStructures;

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
    private static final Map<String, Expected> EXPECTED = Map.ofEntries(
        Map.entry("sunken_1", new Expected(7465, "CDD49475FF88926A28D6AC35C2066F9E71299F819482406C4AF304B989B6976C", 15, 9, 17, 458, 1, 3, 0)),
        Map.entry("sunken_2", new Expected(6296, "E7F21B1CE6F9DD579B2EB9384A21768DCF1F5CC27D19B27DFA9C1BD036946EB4", 15, 8, 15, 376, 1, 3, 0)),
        Map.entry("sunken_3", new Expected(7307, "C81FCBE58E52E9B8A3377104E00CB801D8883B9E64182269524F5A7B609C8777", 15, 10, 14, 786, 1, 2, 6)),
        Map.entry("sunken_small_1", new Expected(923, "884938C5EDA8CF976A54D47D45BDA3B0D243EEBAC73C8CB2FA19088CF1E96133", 7, 4, 5, 57, 0, 1, 0)),
        Map.entry("sunken_small_2", new Expected(1142, "23550DE08B5F753BC68087A6900D3C2D0253FF52F6F7B87D7C926F8C1460B3C2", 7, 5, 5, 97, 1, 1, 0)),
        Map.entry("sunken_small_3", new Expected(1252, "51E73246225271D8BADDB7B2430F4809830FD5B7E3013062AB9B8877FB707AD5", 7, 6, 5, 94, 1, 0, 1)),
        Map.entry("sunken_small_4", new Expected(1192, "0140844ADD46E93A5D3F64BA57759BACC89C7D740EE04E6257FAD0752F0FD3B1", 7, 6, 5, 70, 1, 0, 0)),
        Map.entry("sunken_small_5", new Expected(1536, "43BB2F513638D8553F323EAB3A415F0BE862639A56AAF8028B2BB7BDD9F36539", 7, 9, 5, 113, 1, 1, 0)),
        Map.entry("sunken_small_6", new Expected(1455, "2FF392E30034306DCB75DD6159A3F15FD39B536F33A72E34F9A74A1D98094F48", 7, 8, 5, 137, 1, 0, 0))
    );

    private Stage10BSourceTemplateAudit() {}

    public static void main(String[] args) throws Exception {
        if (args.length < 1 || args.length > 2) {
            throw new IllegalArgumentException("Expected resource root and optional --packaged");
        }
        boolean packaged = args.length == 2 && args[1].equals("--packaged");

        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        loadBiomeMakeoverRegistriesInRegistrationWindow();
        ResourceLocation structureId = ResourceLocation.fromNamespaceAndPath("biomemakeover", "sunken_ruin");
        require(BuiltInRegistries.STRUCTURE_TYPE.containsKey(structureId), "Sunken Ruin StructureType is not registered");
        require(BuiltInRegistries.STRUCTURE_PIECE.containsKey(structureId), "Sunken Ruin StructurePieceType is not registered");
        SunkenRuinStructure structure = new SunkenRuinStructure(
            new Structure.StructureSettings(HolderSet.empty()), 0.6F, 0.8F);
        require(structure.step() == GenerationStep.Decoration.LOCAL_MODIFICATIONS,
            "Sunken Ruin effective generation step is not LOCAL_MODIFICATIONS");
        require(structure.type() == BMStructures.SUNKEN_RUIN,
            "Sunken Ruin type() does not return its canonical registered type");

        Path root = Path.of(args[0]).toAbsolutePath().normalize();
        System.out.println("id\tbytes\tsha256\tdataVersion\tsize\tpalette\tstoredBlocks\tplacedBlocks\tentities\tjigsaws\tchests\tmarkers\tblockEntities\tmarkerKinds\tblockEntityKinds\tlootTables");
        for (String name : TEMPLATES) {
            String directory = packaged ? "structure" : "structures";
            Path path = root.resolve("data/biomemakeover/" + directory + "/sunken_ruins/" + name + ".nbt");
            if (packaged) {
                require(!Files.exists(root.resolve("data/biomemakeover/structures/sunken_ruins/" + name + ".nbt")),
                    name + " has an obsolete plural-path duplicate");
            }
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
            Expected expected = EXPECTED.get(name);
            require(bytes.length == expected.bytes && sha256.equals(expected.sha256), name + " bytes/hash changed");
            require(dataVersion == 2584, name + " DataVersion changed: " + dataVersion);
            require(size.getX() == expected.x && size.getY() == expected.y && size.getZ() == expected.z,
                name + " dimensions changed: " + size);
            require(raw.contains("palette") && !raw.contains("palettes"),
                name + " must use the final single-palette structure encoding");
            require(entities.isEmpty() && jigsaws == 0 && chests == 0, name + " gained embedded entities/jigsaws/chests");
            require(placedBlocks == expected.placedBlocks, name + " placed-block count changed: " + placedBlocks);
            require(markerKinds.getOrDefault("chest", 0) == expected.chestMarkers
                    && markerKinds.getOrDefault("witch", 0) == expected.witchMarkers
                    && markerKinds.size() == (expected.chestMarkers > 0 ? 1 : 0) + (expected.witchMarkers > 0 ? 1 : 0),
                name + " marker contract changed: " + markerKinds);
            require(blockEntityKinds.getOrDefault("biomemakeover:lightning_bug_bottle", 0) == expected.lightningBottles
                    && blockEntityKinds.size() == (expected.lightningBottles > 0 ? 1 : 0),
                name + " block-entity contract changed: " + blockEntityKinds);
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
        System.out.println(packaged ? "STAGE 10B PACKAGED TEMPLATE RUNTIME VALIDATION PASSED"
            : "STAGE 10B FINAL-SOURCE TEMPLATE AUDIT PASSED");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static void loadBiomeMakeoverRegistriesInRegistrationWindow() throws Exception {
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
        Class.forName("party.lemons.biomemakeover.init.BMStructures");
    }

    private record Expected(int bytes, String sha256, int x, int y, int z, int placedBlocks,
                            int chestMarkers, int witchMarkers, int lightningBottles) {}
}
