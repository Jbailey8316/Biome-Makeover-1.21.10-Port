package party.lemons.biomemakeover.worldgen;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import party.lemons.biomemakeover.init.BMFeatures;

import java.util.List;
import java.util.function.BiConsumer;

public final class BalsaTrunkPlacer extends TrunkPlacer {
    public static final MapCodec<BalsaTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(i -> trunkPlacerParts(i).apply(i, BalsaTrunkPlacer::new));
    public BalsaTrunkPlacer(int base, int first, int second) { super(base,first,second); }
    @Override protected TrunkPlacerType<?> type() { return BMFeatures.BLIGHTED_BALSA_TRUNK; }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader level,
        BiConsumer<BlockPos, BlockState> output, RandomSource random, int height, BlockPos origin, TreeConfiguration config) {
        setDirtAt(level,output,random,origin.below(),config);
        List<FoliagePlacer.FoliageAttachment> attachments=Lists.newArrayList();
        Direction bend=Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int bendStart=height-random.nextInt(4)-1, bendSteps=3-random.nextInt(3), x=origin.getX(), z=origin.getZ(), topY=origin.getY();
        int branches=random.nextInt(2);
        BlockPos.MutableBlockPos cursor=new BlockPos.MutableBlockPos();
        for(int n=0;n<height;n++) {
            int y=origin.getY()+n;
            if(n>=bendStart&&bendSteps>0){x+=bend.getStepX();z+=bend.getStepZ();bendSteps--;}
            else if(y>origin.getY()+4&&branches>0&&random.nextInt(4)==0){
                branches--; Direction branch;
                do branch=Direction.Plane.HORIZONTAL.getRandomDirection(random); while(branch==bend);
                BlockPos.MutableBlockPos bp=new BlockPos.MutableBlockPos(x,y,z);
                for(int b=0;b<random.nextInt(2,4);b++){bp.move(branch);placeLog(level,output,random,bp,config);}
                attachments.add(new FoliagePlacer.FoliageAttachment(bp.above().immutable(),1,false));
            }
            if(placeLog(level,output,random,cursor.set(x,y,z),config)) topY=y+1;
        }
        attachments.add(new FoliagePlacer.FoliageAttachment(new BlockPos(x,topY,z),2,false));
        return attachments;
    }
}
