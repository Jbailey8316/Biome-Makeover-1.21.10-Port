package party.lemons.biomemakeover.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public final class SurfaceFossilFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceLocation[] FOSSILS = {
        id("fossil/spine_1"), id("fossil/spine_2"), id("fossil/spine_3"), id("fossil/spine_4"),
        id("nether_fossils/fossil_1"), id("nether_fossils/fossil_2"), id("nether_fossils/fossil_3"), id("nether_fossils/fossil_4"),
        id("nether_fossils/fossil_5"), id("nether_fossils/fossil_6"), id("nether_fossils/fossil_7"), id("nether_fossils/fossil_8"),
        id("nether_fossils/fossil_9"), id("nether_fossils/fossil_10"), id("nether_fossils/fossil_11"), id("nether_fossils/fossil_12"),
        id("nether_fossils/fossil_13"), id("nether_fossils/fossil_14")
    };
    private static ResourceLocation id(String path) { return ResourceLocation.withDefaultNamespace(path); }
    public SurfaceFossilFeature(Codec<NoneFeatureConfiguration> codec) { super(codec); }
    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level=context.level(); Rotation rotation=Rotation.getRandom(context.random());
        int index=context.random().nextInt(FOSSILS.length);
        StructureTemplate template=level.getLevel().getServer().getStructureManager().getOrCreate(FOSSILS[index]);
        Vec3i size=template.getSize(rotation); BlockPos origin=context.origin().offset(-size.getX()/2,0,-size.getZ()/2);
        int y=context.origin().getY();
        for(int x=0;x<size.getX();x++) for(int z=0;z<size.getZ();z++) y=Math.min(y,level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG,origin.getX()+x,origin.getZ()+z));
        if(index<4) y-=Mth.randomBetweenInclusive(context.random(),1,Math.max(1,size.getY()-3));
        ChunkPos chunk=new ChunkPos(context.origin());
        BoundingBox box=new BoundingBox(chunk.getMinBlockX()-16,level.getMinY(),chunk.getMinBlockZ()-16,chunk.getMaxBlockX()+16,level.getMaxY(),chunk.getMaxBlockZ()+16);
        StructurePlaceSettings settings=new StructurePlaceSettings().addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK).setRotation(rotation).setBoundingBox(box).setRandom(context.random());
        BlockPos place=template.getZeroPositionWithTransform(origin.atY(y),Mirror.NONE,rotation);
        template.placeInWorld(level,place,place,settings,context.random(),4);
        return true;
    }
}
