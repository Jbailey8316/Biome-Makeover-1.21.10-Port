package party.lemons.biomemakeover.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import party.lemons.biomemakeover.init.BMBlocks;

public final class PaydirtFeature extends Feature<NoneFeatureConfiguration> {
    public PaydirtFeature(Codec<NoneFeatureConfiguration> codec) { super(codec); }
    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        BlockPos center = context.origin(); WorldGenLevel level = context.level();
        for (int pass = 0; pass < 3; pass++) {
            int xs=context.random().nextInt(4), ys=context.random().nextInt(4), zs=context.random().nextInt(4);
            float radius=(xs+ys+zs)/3F+.5F;
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-xs,-ys,-zs), center.offset(xs,ys,zs))) {
                BlockState state=level.getBlockState(pos);
                if (state.isAir() || state.is(Blocks.WATER) || !state.canOcclude() || pos.distSqr(center)>radius*radius) continue;
                boolean water=false;
                for (Direction direction:Direction.values()) if (level.getBlockState(pos.relative(direction)).is(Blocks.WATER)) { water=true; break; }
                if (water) level.setBlock(pos, BMBlocks.PAYDIRT.defaultBlockState(), 4);
            }
            center=center.offset(-1+context.random().nextInt(2),-context.random().nextInt(2),-1+context.random().nextInt(2));
        }
        return true;
    }
}
