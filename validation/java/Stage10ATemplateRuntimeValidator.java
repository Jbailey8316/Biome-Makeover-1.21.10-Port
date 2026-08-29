import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;

/**
 * Loads the Stage 10A template through the 1.21.10 resource-path, NBT,
 * data-fixer and StructureTemplate codecs. This catches the singular
 * data/&lt;namespace&gt;/structure migration that JSON-only checks cannot see.
 */
public final class Stage10ATemplateRuntimeValidator {
    private static final ResourceLocation TEMPLATE_ID =
        ResourceLocation.fromNamespaceAndPath("biomemakeover", "mushroom_house/house/house_1");
    private static final ResourceLocation EXPECTED_FILE_ID =
        ResourceLocation.fromNamespaceAndPath("biomemakeover", "structure/mushroom_house/house/house_1.nbt");

    private Stage10ATemplateRuntimeValidator() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected the source resource root");
        }

        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        loadBiomeMakeoverBlocksInRegistrationWindow();

        FileToIdConverter converter = new FileToIdConverter("structure", ".nbt");
        ResourceLocation fileId = converter.idToFile(TEMPLATE_ID);
        require(fileId.equals(EXPECTED_FILE_ID), "1.21.10 structure template converter path changed: " + fileId);

        Path resourceRoot = Path.of(args[0]).toAbsolutePath().normalize();
        PackLocationInfo info = new PackLocationInfo(
            "stage10a-validator",
            Component.literal("Stage 10A validator"),
            PackSource.DEFAULT,
            Optional.empty()
        );

        CompoundTag raw;
        try (MultiPackResourceManager resources = new MultiPackResourceManager(
            PackType.SERVER_DATA,
            List.of(new PathPackResources(info, resourceRoot))
        ); InputStream input = resources.open(fileId)) {
            raw = NbtIo.readCompressed(input, NbtAccounter.unlimitedHeap());
        }

        int originalDataVersion = NbtUtils.getDataVersion(raw, 500);
        require(originalDataVersion == 3337, "Unexpected original template DataVersion: " + originalDataVersion);
        CompoundTag upgraded = DataFixTypes.STRUCTURE.updateToCurrentVersion(
            DataFixers.getDataFixer(), raw, originalDataVersion
        );

        for (var paletteEntry : upgraded.getListOrEmpty("palette")) {
            CompoundTag state = (CompoundTag) paletteEntry;
            ResourceLocation blockId = ResourceLocation.parse(state.getStringOr("Name", ""));
            require(BuiltInRegistries.BLOCK.containsKey(blockId), "Unresolved template block: " + blockId);
        }

        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK, upgraded);
        Vec3i size = template.getSize();
        require(size.getX() == 11 && size.getY() == 10 && size.getZ() == 11,
            "Decoded template dimensions differ: " + size);
        require(template.getJigsaws(BlockPos.ZERO, net.minecraft.world.level.block.Rotation.NONE).isEmpty(),
            "Final Mushroom House unexpectedly gained jigsaw connectors");
        require(template.filterBlocks(BlockPos.ZERO, new StructurePlaceSettings(), Blocks.CHEST, false).size() == 1,
            "Decoded template must contain exactly one chest");
        require(template.filterBlocks(BlockPos.ZERO, new StructurePlaceSettings(), Blocks.FLOWER_POT, false).size() == 4,
            "Decoded template must contain exactly four flower-pot processor targets");

        ListTag entities = upgraded.getListOrEmpty("entities");
        require(entities.size() == 1, "Decoded template must contain exactly one entity");
        CompoundTag trader = entities.getCompoundOrEmpty(0).getCompoundOrEmpty("nbt");
        require(trader.getStringOr("id", "").equals("biomemakeover:mushroom_trader"),
            "Decoded template entity is not the Mushroom Trader");
        ListTag offers = trader.getCompoundOrEmpty("Offers").getListOrEmpty("Recipes");
        require(offers.size() == 6, "Embedded Mushroom Trader must retain six offers");
        require(offers.toString().contains("biomemakeover:button_mushrooms_music_disk"),
            "Embedded Mushroom Trader lost the Button Mushrooms offer during data fixing");

        System.out.printf(
            "STAGE 10A TEMPLATE RUNTIME VALIDATION PASSED path=%s dataVersion=%d size=%dx%dx%d blocks=%d entities=%d%n",
            fileId, originalDataVersion, size.getX(), size.getY(), size.getZ(),
            upgraded.getListOrEmpty("blocks").size(), entities.size()
        );
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static void loadBiomeMakeoverBlocksInRegistrationWindow() throws Exception {
        // The standalone validator does not run through Knot/Fabric's normal
        // registration window. Reopen the already-bootstrapped built-ins only
        // in this isolated validation JVM, then load BM's real block classes.
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
