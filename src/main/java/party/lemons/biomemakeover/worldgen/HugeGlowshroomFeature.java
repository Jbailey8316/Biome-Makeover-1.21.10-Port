package party.lemons.biomemakeover.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.AbstractHugeMushroomFeature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;

/** Released crooked-stem glowshroom generator. Green uses its layered cap; purple/orange use the broad cap. */
public final class HugeGlowshroomFeature extends AbstractHugeMushroomFeature {
    private final boolean green;
    private final boolean underwater;

    public HugeGlowshroomFeature(Codec<HugeMushroomFeatureConfiguration> codec, boolean green, boolean underwater) {
        super(codec); this.green = green; this.underwater = underwater;
    }

    @Override
    public boolean place(FeaturePlaceContext<HugeMushroomFeatureConfiguration> context) {
        int height = getTreeHeight(context.random());
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        if (!(underwater ? isValidUnderwater(context.level(), context.origin(), height, cursor, context.config())
            : isValidPosition(context.level(), context.origin(), height, cursor, context.config()))) return false;
        BlockPos cap = crookedStem(context.level(), context.random(), context.origin(), context.config(), height, cursor);
        makeCap(context.level(), context.random(), cap, 0, cursor, context.config());
        return true;
    }

    private boolean isValidUnderwater(LevelAccessor level, BlockPos origin, int height, BlockPos.MutableBlockPos cursor,
                                      HugeMushroomFeatureConfiguration config) {
        if (origin.getY() < level.getMinY() + 1 || origin.getY() + height + 1 >= level.getMaxY()) return false;
        BlockState floor=level.getBlockState(origin.below());
        if (!isDirt(floor) && !floor.is(BlockTags.MUSHROOM_GROW_BLOCK)) return false;
        for(int y=0;y<=height;y++) {
            int radius=getTreeRadiusForHeight(-1,-1,config.foliageRadius,y);
            for(int x=-radius;x<=radius;x++) for(int z=-radius;z<=radius;z++) {
                BlockState state=level.getBlockState(cursor.setWithOffset(origin,x,y,z));
                if(!state.isAir()&&!level.getFluidState(cursor).is(FluidTags.WATER)&&!state.is(BlockTags.LEAVES)) return false;
            }
        }
        return true;
    }

    private BlockPos crookedStem(LevelAccessor level, RandomSource random, BlockPos origin,
                                 HugeMushroomFeatureConfiguration config, int height, BlockPos.MutableBlockPos cursor) {
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        cursor.set(origin);
        for (int i = 0; i < height; i++) {
            if (!level.getBlockState(cursor).isSolidRender()) setBlock(level, cursor, config.stemProvider.getState(random, origin));
            if (random.nextInt(2) == 0) { cursor.move(direction); setBlock(level, cursor, config.stemProvider.getState(random, origin)); }
            cursor.move(Direction.UP);
        }
        return cursor.immutable();
    }

    @Override protected int getTreeHeight(RandomSource random) {
        int height = random.nextInt(3) + 6;
        return random.nextInt(12) == 0 ? height * 2 : height;
    }

    @Override protected int getTreeRadiusForHeight(int a, int b, int c, int height) { return green ? 1 : height < 3 ? 0 : 2; }

    @Override
    protected void makeCap(LevelAccessor level, RandomSource random, BlockPos start, int y,
                           BlockPos.MutableBlockPos cursor, HugeMushroomFeatureConfiguration config) {
        if (green) { makeGreenCap(level, random, start, y, cursor, config); return; }
        int size = config.foliageRadius + Mth.randomBetweenInclusive(random, 1, 2);
        for (int x = -size; x <= size; x++) for (int z = -size; z <= size; z++) {
            boolean minX=x==-size, maxX=x==size, minZ=z==-size, maxZ=z==size, edgeX=minX||maxX, edgeZ=minZ||maxZ;
            if (edgeX && edgeZ) continue;
            cursor.setWithOffset(start, x, y, z);
            if (level.getBlockState(cursor).isSolidRender()) continue;
            boolean west=minX||edgeZ&&x==1-size, east=maxX||edgeZ&&x==size-1;
            boolean north=minZ||edgeX&&z==1-size, south=maxZ||edgeX&&z==size-1;
            boolean middle=x>-size&&x<size&&z>-size&&z<size;
            BlockState cap = cap(config, random, start, west, east, north, south).setValue(HugeMushroomBlock.DOWN, false);
            level.setBlock(cursor, cap, 3);
            level.setBlock(middle ? cursor.above() : cursor.below(), middle ? config.capProvider.getState(random,start) : cap, 3);
        }
        cursor.setWithOffset(start,0,y-2,0);
        level.setBlock(cursor.west(size),config.capProvider.getState(random,start).setValue(HugeMushroomBlock.EAST,false),3);
        level.setBlock(cursor.east(size),config.capProvider.getState(random,start).setValue(HugeMushroomBlock.WEST,false),3);
        level.setBlock(cursor.north(size),config.capProvider.getState(random,start).setValue(HugeMushroomBlock.SOUTH,false),3);
        level.setBlock(cursor.south(size),config.capProvider.getState(random,start).setValue(HugeMushroomBlock.NORTH,false),3);
    }

    private void makeGreenCap(LevelAccessor level, RandomSource random, BlockPos start, int y,
                              BlockPos.MutableBlockPos cursor, HugeMushroomFeatureConfiguration config) {
        for (int yy=y-3; yy<=y; yy++) {
            int size=yy<y?config.foliageRadius:config.foliageRadius-1, inner=config.foliageRadius-2;
            for (int x=-size;x<=size;x++) for(int z=-size;z<=size;z++) {
                boolean ex=x==-size||x==size, ez=z==-size||z==size, middle=x>-size&&x<size&&z>-size&&z<size;
                if (yy<y && ex==ez && (yy!=y-3 || middle)) continue;
                cursor.setWithOffset(start,x,yy,z);
                if (!level.getBlockState(cursor).isSolidRender()) setBlock(level,cursor,cap(config,random,start,x < -inner,x > inner,z < -inner,z > inner));
            }
        }
        cursor.setWithOffset(start,0,y,0);
        if (level.isEmptyBlock(cursor.above())) level.setBlock(cursor.above(),config.capProvider.getState(random,start),3);
        boolean top=true;
        int offset=config.foliageRadius;
        for(int i=0;i<2;i++) {
            level.setBlock(cursor.west(offset),config.capProvider.getState(random,start).setValue(HugeMushroomBlock.EAST,top),3);
            level.setBlock(cursor.east(offset),config.capProvider.getState(random,start).setValue(HugeMushroomBlock.WEST,top),3);
            level.setBlock(cursor.north(offset),config.capProvider.getState(random,start).setValue(HugeMushroomBlock.SOUTH,top),3);
            level.setBlock(cursor.south(offset),config.capProvider.getState(random,start).setValue(HugeMushroomBlock.NORTH,top),3);
            top=false;
            cursor.setWithOffset(start,0,y-4,0);
        }
    }

    private static BlockState cap(HugeMushroomFeatureConfiguration config, RandomSource random, BlockPos pos,
                                  boolean west, boolean east, boolean north, boolean south) {
        return config.capProvider.getState(random,pos).setValue(HugeMushroomBlock.WEST,west)
            .setValue(HugeMushroomBlock.EAST,east).setValue(HugeMushroomBlock.NORTH,north).setValue(HugeMushroomBlock.SOUTH,south);
    }
}
