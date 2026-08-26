package party.lemons.biomemakeover.level.feature;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import party.lemons.biomemakeover.block.IlluniteClusterBlock;
import party.lemons.biomemakeover.init.BMBlocks;

/** Released ore feature extension that decorates generated Mesmerite with full Illunite clusters. */
public final class MesmermiteUndergroundFeature extends OreFeature {
    public MesmermiteUndergroundFeature(Codec<OreConfiguration> codec) { super(codec); }

    @Override
    protected boolean doPlace(WorldGenLevel level, RandomSource random, OreConfiguration config,
                              double startX, double endX, double startZ, double endZ,
                              double startY, double endY, int minX, int minY, int minZ, int width, int height) {
        List<BlockPos> positions = new ArrayList<>();
        int placed = 0;
        BitSet occupied = new BitSet(width * height * width);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        int size = config.size;
        double[] nodes = new double[size * 4];
        for (int index = 0; index < size; index++) {
            float progress = (float) index / size;
            double x = Mth.lerp(progress, startX, endX);
            double y = Mth.lerp(progress, startY, endY);
            double z = Mth.lerp(progress, startZ, endZ);
            double variance = random.nextDouble() * size / 16.0;
            double radius = ((Mth.sin((float) Math.PI * progress) + 1.0F) * variance + 1.0) / 2.0;
            nodes[index * 4] = x; nodes[index * 4 + 1] = y; nodes[index * 4 + 2] = z; nodes[index * 4 + 3] = radius;
        }
        for (int first = 0; first < size - 1; first++) {
            if (nodes[first * 4 + 3] <= 0) continue;
            for (int second = first + 1; second < size; second++) {
                if (nodes[second * 4 + 3] <= 0) continue;
                double radiusDelta = nodes[first * 4 + 3] - nodes[second * 4 + 3];
                double xDelta = nodes[first * 4] - nodes[second * 4];
                double yDelta = nodes[first * 4 + 1] - nodes[second * 4 + 1];
                double zDelta = nodes[first * 4 + 2] - nodes[second * 4 + 2];
                if (radiusDelta * radiusDelta <= xDelta * xDelta + yDelta * yDelta + zDelta * zDelta) continue;
                if (radiusDelta > 0) nodes[second * 4 + 3] = -1; else nodes[first * 4 + 3] = -1;
            }
        }
        try (BulkSectionAccess sections = new BulkSectionAccess(level)) {
            for (int node = 0; node < size; node++) {
                double radius = nodes[node * 4 + 3];
                if (radius < 0) continue;
                double centerX = nodes[node * 4], centerY = nodes[node * 4 + 1], centerZ = nodes[node * 4 + 2];
                int lowX = Math.max(Mth.floor(centerX - radius), minX), lowY = Math.max(Mth.floor(centerY - radius), minY), lowZ = Math.max(Mth.floor(centerZ - radius), minZ);
                int highX = Math.max(Mth.floor(centerX + radius), lowX), highY = Math.max(Mth.floor(centerY + radius), lowY), highZ = Math.max(Mth.floor(centerZ + radius), lowZ);
                for (int x = lowX; x <= highX; x++) {
                    double dx = (x + 0.5 - centerX) / radius;
                    if (dx * dx >= 1) continue;
                    for (int y = lowY; y <= highY; y++) {
                        double dy = (y + 0.5 - centerY) / radius;
                        if (dx * dx + dy * dy >= 1) continue;
                        blockLoop: for (int z = lowZ; z <= highZ; z++) {
                            double dz = (z + 0.5 - centerZ) / radius;
                            int bit = x - minX + (y - minY) * width + (z - minZ) * width * height;
                            if (dx * dx + dy * dy + dz * dz >= 1 || level.isOutsideBuildHeight(y) || occupied.get(bit)) continue;
                            occupied.set(bit); mutable.set(x, y, z);
                            if (!level.ensureCanWrite(mutable)) continue;
                            LevelChunkSection section = sections.getSection(mutable);
                            if (section == null) continue;
                            int sectionX = SectionPos.sectionRelative(x), sectionY = SectionPos.sectionRelative(y), sectionZ = SectionPos.sectionRelative(z);
                            BlockState existing = section.getBlockState(sectionX, sectionY, sectionZ);
                            for (OreConfiguration.TargetBlockState target : config.targetStates) {
                                if (!OreFeature.canPlaceOre(existing, sections::getBlockState, random, config, target, mutable)) continue;
                                section.setBlockState(sectionX, sectionY, sectionZ, target.state, false);
                                positions.add(mutable.immutable()); placed++; continue blockLoop;
                            }
                        }
                    }
                }
            }
        }
        for (BlockPos position : positions) if (random.nextInt(25) == 0) {
            for (Direction direction : Direction.values()) {
                BlockPos adjacent = position.relative(direction);
                if (level.isEmptyBlock(adjacent) && random.nextBoolean())
                    level.setBlock(adjacent, BMBlocks.ILLUNITE_CLUSTER.defaultBlockState().setValue(IlluniteClusterBlock.FACING, direction), 16);
            }
        }
        return placed > 0;
    }
}
