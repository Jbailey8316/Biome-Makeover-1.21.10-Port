package party.lemons.biomemakeover.worldgen.mansion;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import party.lemons.biomemakeover.block.entity.TapestryBlockEntity;


/** Released Mansion tapestry substrate shared by standing and wall variants. */
public abstract class MansionTapestryBlock extends BaseEntityBlock {
    private final ResourceLocation texture;

    protected MansionTapestryBlock(BlockBehaviour.Properties properties, ResourceLocation texture) {
        super(properties);
        this.texture = texture;
    }

    public final ResourceLocation tapestryTexture() { return texture; }

    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TapestryBlockEntity(pos, state);
    }

    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape(state);
    }

    protected abstract VoxelShape shape(BlockState state);

    @Override public boolean isPossibleToRespawnInThis(BlockState state) { return true; }

}
