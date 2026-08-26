package party.lemons.biomemakeover.level.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import party.lemons.biomemakeover.block.MothBlossomBlock;
import party.lemons.biomemakeover.init.BMBlocks;

/** Released canopy blossom plus ten nearby downward Itching Ivy placements. */
public final class ItchingIvyFeature extends Feature<NoneFeatureConfiguration> {
    public ItchingIvyFeature(Codec<NoneFeatureConfiguration> codec) { super(codec); }
    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level=context.level(); RandomSource random=context.random();
        BlockPos surface=level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING,context.origin());
        if(!level.getBlockState(surface.below()).is(BlockTags.LEAVES)||!level.getBlockState(surface).canBeReplaced())return false;
        BlockState blossom=BMBlocks.MOTH_BLOSSOM.defaultBlockState()
            .setValue(MultifaceBlock.getFaceProperty(Direction.DOWN),true)
            .setValue(MothBlossomBlock.BLOSSOM,Direction.DOWN);
        level.setBlock(surface,blossom,3);
        for(int i=0;i<10;i++){
            BlockPos candidate=surface.offset(random.nextInt(11)-5,random.nextInt(5)-2,random.nextInt(11)-5);
            if(level.getBlockState(candidate).canBeReplaced()&&level.getBlockState(candidate.above()).is(BlockTags.LEAVES))
                level.setBlock(candidate,BMBlocks.ITCHING_IVY.defaultBlockState().setValue(MultifaceBlock.getFaceProperty(Direction.UP),true),3);
        }
        return true;
    }
}
