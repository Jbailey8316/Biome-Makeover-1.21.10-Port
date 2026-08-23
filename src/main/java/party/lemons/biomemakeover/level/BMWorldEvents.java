package party.lemons.biomemakeover.level;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.server.level.ServerPlayer;
import java.util.ArrayList;
import java.util.List;
import party.lemons.biomemakeover.BiomeMakeover;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.levelgen.Heightmap;
import party.lemons.biomemakeover.entity.TumbleweedEntity;
import party.lemons.biomemakeover.init.BMEntities;
import party.lemons.biomemakeover.init.BMBlocks;
import party.lemons.biomemakeover.block.SmallLilyPadBlock;
import net.minecraft.world.level.Level;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class BMWorldEvents {
    private static final TagKey<Biome> SPAWNS_TUMBLEWEED = TagKey.create(Registries.BIOME,
        BiomeMakeover.id("spawns_tumbleweed"));
    public static final GameRules.Key<GameRules.BooleanValue> TUMBLEWEED_SPAWNING = GameRuleRegistry.register(
        "BMdoTumbleweedSpawning", GameRules.Category.SPAWNING, GameRuleFactory.createBooleanRule(true));
    private BMWorldEvents() {}
    public static void initialize(){ ServerTickEvents.END_WORLD_TICK.register(level->{
        WindSystem.update(level.random);
        if(!level.getGameRules().getBoolean(TUMBLEWEED_SPAWNING)) return;
        List<List<ServerPlayer>> groups=new ArrayList<>();
        for(ServerPlayer player:level.players()){
            BlockPos playerPos=player.blockPosition();
            if(player.isSpectator()||!level.isLoaded(playerPos)||!level.getBiome(playerPos).is(SPAWNS_TUMBLEWEED))continue;
            List<ServerPlayer> group=null;
            for(List<ServerPlayer> candidate:groups)if(player.distanceTo(candidate.getFirst())<=50){group=candidate;break;}
            if(group==null){group=new ArrayList<>();groups.add(group);}group.add(player);
        }
        for(List<ServerPlayer> group:groups)if(level.random.nextInt(200)==0)spawnTumbleweed(level,group);
    }); }

    private static void spawnTumbleweed(net.minecraft.server.level.ServerLevel level,List<ServerPlayer> players){
        int minX=Integer.MAX_VALUE,minZ=Integer.MAX_VALUE,maxX=Integer.MIN_VALUE,maxZ=Integer.MIN_VALUE;
        for(ServerPlayer player:players){minX=Math.min(minX,player.getBlockX()-65);minZ=Math.min(minZ,player.getBlockZ()-65);maxX=Math.max(maxX,player.getBlockX()+65);maxZ=Math.max(maxZ,player.getBlockZ()+65);}
        BlockPos candidate=null;
        for(int attempts=20;attempts>0;attempts--){
            int x=minX+level.random.nextInt(maxX-minX),z=minZ+level.random.nextInt(maxZ-minZ);
            boolean outside=true;for(ServerPlayer player:players)if(player.distanceToSqr(x,player.getY(),z)<20){outside=false;break;}
            if(outside){candidate=new BlockPos(x,0,z);break;}
        }
        if(candidate==null||!level.isLoaded(candidate))return;
        int y=level.getHeight(Heightmap.Types.MOTION_BLOCKING,candidate.getX(),candidate.getZ());
        BlockPos pos=new BlockPos(candidate.getX(),y,candidate.getZ());
        if(!level.getBiome(pos).is(BiomeTags.IS_BADLANDS))return;
        TumbleweedEntity tumble=BMEntities.TUMBLEWEED.create(level,EntitySpawnReason.NATURAL);
        if(tumble!=null){tumble.setPos(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5);level.addFreshEntity(tumble);}
    }

    public static void handleSwampBoneMeal(Level level,BlockPos origin,RandomSource random){
        start: for(int i=0;i<128;i++){
            BlockPos pos=origin; BlockState state=Blocks.SEAGRASS.defaultBlockState(); boolean requireWater=true;
            for(int j=0;j<i/16;j++){pos=pos.offset(random.nextInt(3)-1,(random.nextInt(3)-1)*random.nextInt(3)/2,random.nextInt(3)-1);if(level.getBlockState(pos).isCollisionShapeFullBlock(level,pos))continue start;}
            if(level.getBlockState(pos.above()).isAir()&&random.nextInt(4)==0){
                if(random.nextInt(5)>0)state=(random.nextInt(3)==0?BMBlocks.CATTAIL:BMBlocks.REED).defaultBlockState();
                else{pos=pos.above();requireWater=false;if(random.nextBoolean())state=BMBlocks.SMALL_LILY_PAD.defaultBlockState().setValue(SmallLilyPadBlock.PADS,random.nextInt(4));else state=random.nextInt(4)==0?BMBlocks.WATER_LILY.defaultBlockState():Blocks.LILY_PAD.defaultBlockState();}
            }
            if(!state.canSurvive(level,pos))continue; BlockState current=level.getBlockState(pos);
            if((!requireWater&&current.isAir())||(requireWater&&current.is(Blocks.WATER)&&level.getFluidState(pos).isSource())){
                if(state.getBlock() instanceof DoublePlantBlock){if(level.isEmptyBlock(pos.above()))DoublePlantBlock.placeAt(level,state,pos,2);}else level.setBlock(pos,state,2);
            }else if(current.is(Blocks.SEAGRASS)&&random.nextInt(10)==0)((BonemealableBlock)Blocks.SEAGRASS).performBonemeal((net.minecraft.server.level.ServerLevel)level,random,pos,current);
        }
    }
}
