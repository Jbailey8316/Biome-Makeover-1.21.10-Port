package party.lemons.biomemakeover.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundSource;
import party.lemons.biomemakeover.init.BMSounds;

/** Released day/night crystal texture state; actual light is supplied by the registered stage. */
public final class IlluniteClusterBlock extends AmethystClusterBlock {
    public static final EnumProperty<Type> TYPE = EnumProperty.create("type", Type.class);
    public IlluniteClusterBlock(int height, int inset, Properties properties) {
        super(height, inset, properties);
        registerDefaultState(defaultBlockState().setValue(TYPE, Type.DAY));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { super.createBlockStateDefinition(builder); builder.add(TYPE); }
    @Override protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        schedule(level,pos,random);
    }
    @Override protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        long time=level.getDayTime()%24000L;
        Type type=level.dimensionType().hasFixedTime()?Type.UNKNOWN:(time>=13000L&&time<23000L?Type.NIGHT:Type.DAY);
        if(state.getValue(TYPE)!=type)level.setBlock(pos,state.setValue(TYPE,type),3);
        level.scheduleTick(pos,this,20+random.nextInt(150));
    }
    @Override protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
            Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if(!ticks.getBlockTicks().hasScheduledTick(pos,this))ticks.scheduleTick(pos,this,20+random.nextInt(150));
        return super.updateShape(state,level,ticks,pos,direction,neighborPos,neighborState,random);
    }
    private void schedule(ScheduledTickAccess ticks,BlockPos pos,RandomSource random){
        if(!ticks.getBlockTicks().hasScheduledTick(pos,this))ticks.scheduleTick(pos,this,20+random.nextInt(150));
    }
    @Override protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        if(!level.isClientSide()){
            BlockPos pos=hit.getBlockPos();
            level.playSound(null,pos,BMSounds.ILLUNITE_HIT,SoundSource.BLOCKS,1F,.5F+level.random.nextFloat()*1.2F);
            level.playSound(null,pos,BMSounds.ILLUNITE_STEP,SoundSource.BLOCKS,1F,.5F+level.random.nextFloat()*1.2F);
        }
    }
    public enum Type implements StringRepresentable { DAY,NIGHT,UNKNOWN; @Override public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);} }
}
