package party.lemons.biomemakeover.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.MyceliumBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import party.lemons.biomemakeover.init.BMBlocks;

@Mixin(MyceliumBlock.class)
public abstract class MyceliumBlockMixin extends Block implements BonemealableBlock {
    protected MyceliumBlockMixin(Properties properties) { super(properties); }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) { return true; }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        var fluid = level.getFluidState(pos.above());
        return level.isEmptyBlock(pos.above()) || fluid.is(FluidTags.WATER) && fluid.getAmount() == 8;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        BlockPos start = pos.above();
        BlockState orange = BMBlocks.ORANGE_GLOWSHROOM.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true);
        next:
        for (int range = 0; range < 128; range++) {
            BlockPos target = start;
            for (int attempt = 0; attempt < range / 16; attempt++) {
                target = target.offset(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2, random.nextInt(3) - 1);
                if (!level.getBlockState(target.below()).is(Blocks.MYCELIUM) || level.getBlockState(target).isCollisionShapeFullBlock(level, target)) continue next;
            }
            var fluid = level.getFluidState(target);
            boolean water = fluid.is(FluidTags.WATER) && fluid.getAmount() == 8;
            if (!level.isEmptyBlock(target) && !water) continue;
            boolean waterPlant = false;
            BlockState placed;
            if (random.nextInt(8) == 0) {
                if (random.nextInt(20) == 0) {
                    if (water) { placed = orange; waterPlant = true; }
                    else placed = random.nextBoolean() ? BMBlocks.PURPLE_GLOWSHROOM.defaultBlockState() : BMBlocks.GREEN_GLOWSHROOM.defaultBlockState();
                } else if (random.nextInt(4) == 0) {
                    placed = random.nextBoolean() ? BMBlocks.TALL_RED_MUSHROOM.defaultBlockState() : BMBlocks.TALL_BROWN_MUSHROOM.defaultBlockState();
                } else placed = random.nextBoolean() ? Blocks.RED_MUSHROOM.defaultBlockState() : Blocks.BROWN_MUSHROOM.defaultBlockState();
            } else placed = random.nextInt(5) == 0 ? BMBlocks.MYCELIUM_ROOTS.defaultBlockState() : BMBlocks.MYCELIUM_SPROUTS.defaultBlockState();
            if (water && !waterPlant || !placed.canSurvive(level, target)) continue;
            if (placed.getBlock() instanceof DoublePlantBlock) {
                if (level.isEmptyBlock(target.above())) DoublePlantBlock.placeAt(level, placed, target, Block.UPDATE_CLIENTS);
            } else level.setBlock(target, placed, Block.UPDATE_ALL);
        }
    }
}
