package party.lemons.biomemakeover.worldgen.mansion;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import party.lemons.biomemakeover.block.entity.TapestryBlockEntity;

import java.util.concurrent.atomic.AtomicInteger;

/** Released Mansion tapestry substrate shared by standing and wall variants. */
public abstract class MansionTapestryBlock extends BaseEntityBlock {
    private static final AtomicInteger DROP_TRACE_COUNT = new AtomicInteger();
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

    @Override public void playerDestroy(net.minecraft.world.level.Level level, Player player, BlockPos pos,
                                        BlockState state, BlockEntity blockEntity, ItemStack tool) {
        if (Boolean.getBoolean("bm.mansion.trace") && DROP_TRACE_COUNT.get() < 16
            && level instanceof ServerLevel serverLevel && DROP_TRACE_COUNT.incrementAndGet() <= 16) {
            var drops = Block.getDrops(state, serverLevel, pos, blockEntity, player, tool);
            StringBuilder resolved = new StringBuilder();
            for (ItemStack drop : drops) {
                if (resolved.length() > 0) resolved.append(',');
                resolved.append(BuiltInRegistries.ITEM.getKey(drop.getItem())).append('x').append(drop.getCount());
            }
            String blockId = BuiltInRegistries.BLOCK.getKey(this).toString();
            String expectedItem = blockId.replace("_wall_tapestry", "_tapestry");
            party.lemons.biomemakeover.BiomeMakeover.LOGGER.info(
                "[BM_TAPESTRY_DROP] blockId={} form={} blockPos={} lootTableId={} tool={} playerBreak=true expectedItem={} resolvedDrops={} dropCount={}",
                blockId, this instanceof MansionWallTapestryBlock ? "wall" : "standing", pos,
                "biomemakeover:blocks/" + expectedItem.substring(expectedItem.indexOf(':') + 1),
                BuiltInRegistries.ITEM.getKey(tool.getItem()), expectedItem, resolved, drops.size());
        }
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }

}
