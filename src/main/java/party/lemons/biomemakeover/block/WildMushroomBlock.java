package party.lemons.biomemakeover.block;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Standalone replacement for the archived Taniwha mushroom plant base.
 * It uses normal plant survival rules so worldgen cannot leave mushrooms
 * floating or supported by fluids.
 */
public final class WildMushroomBlock extends MushroomBlock {
    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 9.0D, 14.0D);

    public WildMushroomBlock(BlockBehaviour.Properties properties) {
        super((ResourceKey<net.minecraft.world.level.levelgen.feature.ConfiguredFeature<?, ?>>) null, properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
