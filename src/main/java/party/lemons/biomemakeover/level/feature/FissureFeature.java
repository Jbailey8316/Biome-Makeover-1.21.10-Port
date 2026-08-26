package party.lemons.biomemakeover.level.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import party.lemons.biomemakeover.init.BMBlocks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Local replacement for the released Taniwha fissure infrastructure, preserving BM's algorithm and codec. */
public final class FissureFeature extends Feature<FissureFeature.Config> {
    public FissureFeature(Codec<Config> codec) { super(codec); }

    @Override public boolean place(FeaturePlaceContext<Config> context) {
        RandomSource random=context.random(); WorldGenLevel level=context.level(); Config cfg=context.config();
        Direction startDirection=Direction.from2DDataValue(random.nextInt(4));
        Segment start=new Segment(startDirection,context.origin(),cfg.height.sample(random));
        List<Segment> segments=new ArrayList<>(); segments.add(start);
        extend(startDirection,cfg.count.sample(random),segments,start,random,cfg.heightOffset);
        extend(startDirection.getOpposite(),cfg.count.sample(random),segments,start,random,cfg.heightOffset);
        List<Segment> spread=new ArrayList<>();
        for(int i=0;i<cfg.spreadOffset.sample(random);i++) spread(segments,spread,random);
        segments.addAll(spread);
        Set<BlockPos> alternates=new HashSet<>(); boolean generated=false;
        BlockPos.MutableBlockPos cursor=new BlockPos.MutableBlockPos();
        for(Segment segment:segments){
            cursor.set(segment.pos);
            Optional<Column> column=Column.scan(level,cursor,5,s->s.isAir()||s.is(Blocks.WATER),s->!s.isAir()&&!s.is(Blocks.WATER));
            if(column.isEmpty()||column.get().getFloor().isEmpty()) continue;
            generated=true; cursor.setY(column.get().getFloor().getAsInt());
            for(int y=0;y<segment.height;y++){
                BlockState fill=cfg.fillBlock.getState(random,cursor), current=level.getBlockState(cursor);
                boolean water=current.getFluidState().isSource();
                if(water&&fill.hasProperty(BlockStateProperties.WATERLOGGED)) fill=fill.setValue(BlockStateProperties.WATERLOGGED,true);
                else if(water&&fill.isAir()) fill=Blocks.WATER.defaultBlockState();
                level.setBlock(cursor,fill,Block.UPDATE_CLIENTS);
                level.scheduleTick(cursor,fill.getFluidState().getType(),0);
                float chance=.1F+(y/5F);
                if(chance>1||random.nextFloat()<chance) setAround(cfg,alternates,fill,cursor,level,random);
                cursor.move(Direction.DOWN);
            }
        }
        for(BlockPos alternate:alternates){
            if(cfg.innerPlacementChance>random.nextFloat()||!cfg.target.test(level,alternate)) continue;
            BlockState placement=Util.getRandom(cfg.innerPlacements,random);
            for(Direction direction:Direction.values()){
                BlockState candidate=placement;
                if(candidate.hasProperty(BlockStateProperties.FACING)) candidate=candidate.setValue(BlockStateProperties.FACING,direction);
                BlockPos target=alternate.relative(direction); BlockState current=level.getBlockState(target);
                if(candidate.hasProperty(BlockStateProperties.WATERLOGGED)) candidate=candidate.setValue(BlockStateProperties.WATERLOGGED,current.getFluidState().isSource());
                if(candidate.canSurvive(level,target)&&BuddingAmethystBlock.canClusterGrowAtState(current)){level.setBlock(target,candidate,Block.UPDATE_CLIENTS);break;}
            }
        }
        return generated;
    }

    private static void setAround(Config cfg,Set<BlockPos> alternates,BlockState fill,BlockPos pos,WorldGenLevel level,RandomSource random){
        BlockPos.MutableBlockPos cursor=new BlockPos.MutableBlockPos();
        for(Direction direction:Direction.values()){
            if(direction==Direction.UP)continue;
            cursor.setWithOffset(pos,direction); BlockState current=level.getBlockState(cursor);
            if(current.isAir()||current.is(fill.getBlock()))continue;
            boolean alternate=random.nextFloat()<cfg.alternateChance;
            level.setBlock(cursor,(alternate?cfg.alternateBaseBlock:cfg.baseBlock).getState(random,cursor),Block.UPDATE_CLIENTS);
            if(alternate)alternates.add(cursor.immutable());
            cursor.move(direction); current=level.getBlockState(cursor);
            if(!current.is(BMBlocks.FISSURE_NO_REPLACE))level.setBlock(cursor,cfg.depthBlock.getState(random,cursor),Block.UPDATE_CLIENTS);
        }
    }
    private static void extend(Direction direction,int count,List<Segment> segments,Segment start,RandomSource random,IntProvider offset){
        BlockPos.MutableBlockPos pos=start.pos.relative(direction).mutable(); Segment previous=start;
        for(int i=0;i<count;i++){Segment next=new Segment(direction,pos.immutable(),previous.height+offset.sample(random));previous=next;if(!segments.contains(next))segments.add(next);if(random.nextInt(5)==0)direction=random.nextBoolean()?direction.getClockWise():direction.getCounterClockWise();pos.move(direction);}
    }
    private static void spread(List<Segment> base,List<Segment> spread,RandomSource random){
        List<Segment> source=spread.isEmpty()?base:List.copyOf(spread), additions=new ArrayList<>();
        for(Segment segment:source)for(Direction direction:new Direction[]{segment.direction.getClockWise(),segment.direction.getCounterClockWise()}){
            Segment candidate=new Segment(segment.direction,segment.pos.relative(direction),Math.max(1,segment.height-random.nextInt(2,5)));
            if(!base.contains(candidate)&&!spread.contains(candidate)&&!additions.contains(candidate))additions.add(candidate);
        } spread.addAll(additions);
    }
    private record Segment(Direction direction,BlockPos pos,int height){@Override public boolean equals(Object o){return o instanceof Segment s&&pos.equals(s.pos);}@Override public int hashCode(){return pos.hashCode();}}

    public record Config(IntProvider height,IntProvider heightOffset,IntProvider spreadOffset,IntProvider count,
            BlockStateProvider baseBlock,BlockStateProvider depthBlock,BlockStateProvider alternateBaseBlock,float alternateChance,
            List<BlockState> innerPlacements,float innerPlacementChance,BlockStateProvider fillBlock,BlockPredicate target) implements FeatureConfiguration {
        public static final Codec<Config> CODEC=RecordCodecBuilder.create(i->i.group(
            IntProvider.CODEC.fieldOf("height").forGetter(Config::height),IntProvider.CODEC.fieldOf("height_offset").forGetter(Config::heightOffset),
            IntProvider.CODEC.fieldOf("spread_offset").forGetter(Config::spreadOffset),IntProvider.CODEC.fieldOf("count").forGetter(Config::count),
            BlockStateProvider.CODEC.fieldOf("base_block").forGetter(Config::baseBlock),BlockStateProvider.CODEC.fieldOf("depth_block").forGetter(Config::depthBlock),
            BlockStateProvider.CODEC.fieldOf("alternate_base_block").forGetter(Config::alternateBaseBlock),Codec.FLOAT.fieldOf("alternate_chance").forGetter(Config::alternateChance),
            ExtraCodecs.nonEmptyList(BlockState.CODEC.listOf()).fieldOf("inner_placements").forGetter(Config::innerPlacements),
            Codec.FLOAT.fieldOf("inner_placement_chance").forGetter(Config::innerPlacementChance),BlockStateProvider.CODEC.fieldOf("fill_block").forGetter(Config::fillBlock),
            BlockPredicate.CODEC.fieldOf("inner_target").forGetter(Config::target)).apply(i,Config::new));
    }
}
