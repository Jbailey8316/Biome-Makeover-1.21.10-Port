package party.lemons.biomemakeover.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import party.lemons.biomemakeover.init.BMBlocks;

public final class PeatFarmlandBlock extends FarmBlock {
    public PeatFarmlandBlock(Properties properties){super(properties);}
    @Override protected void tick(BlockState state,ServerLevel level,BlockPos pos,RandomSource random){if(!state.canSurvive(level,pos))setToPeat(state,level,pos);}
    @Override protected void randomTick(BlockState state,ServerLevel level,BlockPos pos,RandomSource random){
        int moisture=state.getValue(MOISTURE);
        if(!isWaterNearby(level,pos)&&!level.isRainingAt(pos.above())){if(moisture>0)level.setBlock(pos,state.setValue(MOISTURE,moisture-1),2);else if(!hasCrop(level,pos))setToPeat(state,level,pos);}
        else if(moisture<7)level.setBlock(pos,state.setValue(MOISTURE,7),2);
        BlockState crop=level.getBlockState(pos.above());if(crop.isRandomlyTicking())crop.randomTick(level,pos.above(),random);
    }
    @Override public void fallOn(Level level,BlockState state,BlockPos pos,Entity entity,double distance){
        boolean grief=entity instanceof Player||(level instanceof ServerLevel server&&server.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING));
        if(!level.isClientSide()&&level.random.nextFloat()<distance-.5F&&entity instanceof LivingEntity&&grief&&entity.getBbWidth()*entity.getBbWidth()*entity.getBbHeight()>.512F)setToPeat(level.getBlockState(pos),level,pos);
        entity.causeFallDamage(distance,1,level.damageSources().fall());
    }
    private static void setToPeat(BlockState state,Level level,BlockPos pos){level.setBlock(pos,pushEntitiesUp(state,BMBlocks.PEAT.defaultBlockState(),level,pos),3);}
    private static boolean isWaterNearby(Level level,BlockPos pos){for(BlockPos test:BlockPos.betweenClosed(pos.offset(-4,0,-4),pos.offset(4,1,4)))if(level.getFluidState(test).is(FluidTags.WATER))return true;return false;}
    private static boolean hasCrop(Level level,BlockPos pos){Block block=level.getBlockState(pos.above()).getBlock();return block instanceof CropBlock||block instanceof StemBlock||block instanceof AttachedStemBlock;}
}
