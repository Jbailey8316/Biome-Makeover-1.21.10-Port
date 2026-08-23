package party.lemons.biomemakeover.block;

import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;

public class SmallLilyPadBlock extends WaterlilyBlock {
    public static final IntegerProperty PADS=IntegerProperty.create("pads",0,3);
    public SmallLilyPadBlock(Properties properties) { super(properties); registerDefaultState(stateDefinition.any().setValue(PADS,0)); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){super.createBlockStateDefinition(builder);builder.add(PADS);}
    @Override protected InteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(stack.is(this.asItem())&&state.getValue(PADS)<3){if(!level.isClientSide()){level.setBlock(pos,state.setValue(PADS,state.getValue(PADS)+1),3);if(!player.getAbilities().instabuild)stack.shrink(1);}return InteractionResult.SUCCESS;}
        return InteractionResult.PASS;
    }
}
