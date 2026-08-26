package party.lemons.biomemakeover.level.feature.foliage;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import party.lemons.biomemakeover.init.BMFeatures;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/** Exact released 2x2 ancient-oak trunk, buttress, hanging knob and paired branch algorithm. */
public final class AncientOakTrunkPlacer extends TrunkPlacer {
    public static final MapCodec<AncientOakTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(i -> trunkPlacerParts(i).apply(i, AncientOakTrunkPlacer::new));
    public AncientOakTrunkPlacer(int base, int first, int second) { super(base, first, second); }
    @Override protected TrunkPlacerType<?> type() { return BMFeatures.ANCIENT_OAK_TRUNK; }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blocks,
            RandomSource random, int height, BlockPos origin, TreeConfiguration config) {
        List<FoliagePlacer.FoliageAttachment> foliage = Lists.newArrayList();
        for (BlockPos dirt : List.of(origin.below(), origin.below().east(), origin.below().south(), origin.below().south().east()))
            setDirtAt(level, blocks, random, dirt, config);
        int topY = origin.getY() + height - 2;
        for (int y = 0; y < height; y++) for (int x = 0; x < 2; x++) for (int z = 0; z < 2; z++) {
            BlockPos p = origin.offset(x, y, z);
            if (TreeFeature.validTreePos(level, p)) placeLog(level, blocks, random, p, config);
        }
        foliage.add(new FoliagePlacer.FoliageAttachment(new BlockPos(origin.getX(), topY + 3, origin.getZ()), 2, true));
        for (int x = -1; x <= 2; x++) for (int z = -1; z <= 2; z++) {
            if ((x < 0 || x > 1 || z < 0 || z > 1) && random.nextInt(3) == 0) {
                int length = random.nextInt(3) + 2;
                for (int y = 0; y < length; y++) placeLog(level, blocks, random, new BlockPos(origin.getX()+x, topY-y-1, origin.getZ()+z), config);
                foliage.add(new FoliagePlacer.FoliageAttachment(new BlockPos(origin.getX()+x, topY, origin.getZ()+z), 0, false));
            }
        }
        List<BranchDirection> used = new ArrayList<>();
        for (int branch = 0; branch < 1 + random.nextInt(4); branch++)
            makePairedBranch(level, blocks, random, foliage, used, origin, height - Mth.randomBetweenInclusive(random, 8, 13), height, config);
        return foliage;
    }

    private void makePairedBranch(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blocks, RandomSource random,
            List<FoliagePlacer.FoliageAttachment> foliage, List<BranchDirection> used, BlockPos origin, int branchY, int height, TreeConfiguration config) {
        BranchDirection direction;
        do direction = BranchDirection.values()[random.nextInt(BranchDirection.values().length)]; while (used.contains(direction));
        used.add(direction); used.add(direction.opposite());
        for (BranchDirection branchDirection : new BranchDirection[]{direction, direction.opposite()}) {
            int x = origin.getX()+branchDirection.x, z = origin.getZ()+branchDirection.z, end = branchY, offsets = 0;
            for (int y = branchY; y < height - 5 + random.nextInt(4); y++) {
                end = y;
                for (int dx=0; dx<2; dx++) for (int dz=0; dz<2; dz++) placeLog(level, blocks, random, new BlockPos(x+dx, origin.getY()+y, z+dz), config);
                if (random.nextBoolean() && offsets++ <= 5) {
                    if(branchDirection.straight){x+=branchDirection.x;z+=branchDirection.z;}
                    else if((y&1)==0)x+=branchDirection.x; else z+=branchDirection.z;
                }
            }
            foliage.add(new FoliagePlacer.FoliageAttachment(new BlockPos(x, origin.getY()+end+2, z), 1, false));
        }
    }
    private enum BranchDirection {
        N(0,-1,true),NE(1,-1,false),E(1,0,true),SE(1,1,false),S(0,1,true),SW(-1,1,false),W(-1,0,true),NW(-1,-1,false);
        final int x,z; final boolean straight; BranchDirection(int x,int z,boolean straight){this.x=x;this.z=z;this.straight=straight;}
        BranchDirection opposite(){return values()[(ordinal()+4)%8];}
    }
}
