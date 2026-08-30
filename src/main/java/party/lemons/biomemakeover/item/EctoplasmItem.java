package party.lemons.biomemakeover.item;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;
import party.lemons.biomemakeover.init.BMBlocks;

/** Ectoplasm fills a partially filled vanilla composter into the BM variant. */
public final class EctoplasmItem extends Item {
    public EctoplasmItem(Properties properties) { super(properties); }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if (state.is(Blocks.COMPOSTER) && state.getValue(ComposterBlock.LEVEL) > 0) {
            if (!context.getLevel().isClientSide()) {
                context.getLevel().levelEvent(1500, context.getClickedPos(), 1);
                context.getLevel().setBlock(context.getClickedPos(), BMBlocks.ECTOPLASM_COMPOSTER.defaultBlockState()
                    .setValue(ComposterBlock.LEVEL, state.getValue(ComposterBlock.LEVEL)), 3);
                if (context.getPlayer() != null && !context.getPlayer().isCreative()) context.getItemInHand().shrink(1);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }
}
