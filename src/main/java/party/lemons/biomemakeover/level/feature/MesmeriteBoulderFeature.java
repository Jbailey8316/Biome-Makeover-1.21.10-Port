package party.lemons.biomemakeover.level.feature;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import party.lemons.biomemakeover.block.IlluniteClusterBlock;
import party.lemons.biomemakeover.init.BMBlocks;

/** Released dormant boulder component retained under its canonical configured-feature type. */
public final class MesmeriteBoulderFeature extends Feature<BlockStateConfiguration> {
    public MesmeriteBoulderFeature(Codec<BlockStateConfiguration> codec) { super(codec); }

    @Override
    public boolean place(FeaturePlaceContext<BlockStateConfiguration> context) {
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        WorldGenLevel level = context.level();
        while (origin.getY() > 3) {
            BlockState below = level.getBlockState(origin.below());
            if (!level.isEmptyBlock(origin.below()) && (isDirt(below) || isStone(below))) break;
            origin = origin.below();
        }
        if (origin.getY() <= 3) return false;

        List<BlockPos> rockPositions = new ArrayList<>();
        for (int pass = 0; pass < 4; pass++) {
            int xSize = random.nextInt(3), ySize = random.nextInt(4), zSize = random.nextInt(3);
            float distance = (xSize + ySize + zSize) * 0.333F + 0.5F;
            for (BlockPos candidate : BlockPos.betweenClosed(origin.offset(-xSize, -ySize, -zSize), origin.offset(xSize, ySize, zSize))) {
                if (candidate.distSqr(origin) > distance * distance) continue;
                BlockPos placePos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, candidate);
                BlockState below = level.getBlockState(placePos.below());
                int attempts = 100;
                while (attempts-- > 0 && (below.canBeReplaced() || below.is(BlockTags.LOGS) || below.is(BlockTags.LEAVES))) {
                    placePos = placePos.below();
                    below = level.getBlockState(placePos.below());
                }
                if (attempts <= 0) return false;
                if (!(below.is(BMBlocks.MESMERITE) || isDirt(below) || isStone(below) || below.is(Blocks.GRAVEL))) continue;
                level.setBlock(placePos, context.config().state, 4);
                rockPositions.add(placePos);
            }
            origin = origin.offset(-1 + random.nextInt(2), -random.nextInt(2), -1 + random.nextInt(2));
        }
        for (BlockPos rock : rockPositions) if (random.nextInt(10) == 0) {
            for (Direction direction : Direction.values()) {
                BlockPos adjacent = rock.relative(direction);
                if (level.isEmptyBlock(adjacent) && random.nextBoolean())
                    level.setBlock(adjacent, BMBlocks.ILLUNITE_CLUSTER.defaultBlockState().setValue(IlluniteClusterBlock.FACING, direction), 16);
            }
        }
        return true;
    }
}
