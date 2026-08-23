package party.lemons.biomemakeover.level;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.levelgen.Heightmap;
import party.lemons.biomemakeover.entity.TumbleweedEntity;
import party.lemons.biomemakeover.init.BMEntities;

public final class BMWorldEvents {
    public static final GameRules.Key<GameRules.BooleanValue> TUMBLEWEED_SPAWNING = GameRuleRegistry.register(
        "BMdoTumbleweedSpawning", GameRules.Category.SPAWNING, GameRuleFactory.createBooleanRule(true));
    private BMWorldEvents() {}
    public static void initialize(){ ServerTickEvents.END_WORLD_TICK.register(level->{
        WindSystem.update(level.random);
        if(!level.getGameRules().getBoolean(TUMBLEWEED_SPAWNING) || level.random.nextInt(200)!=0) return;
        for(var player:level.players()){
            if(player.isSpectator()) continue;
            int x=player.getBlockX()+20+(level.random.nextBoolean()?1:-1)*level.random.nextInt(46);
            int z=player.getBlockZ()+20+(level.random.nextBoolean()?1:-1)*level.random.nextInt(46);
            BlockPos pos=new BlockPos(x,level.getHeight(Heightmap.Types.MOTION_BLOCKING,x,z),z);
            if(!level.getBiome(pos).is(BiomeTags.IS_BADLANDS)) continue;
            TumbleweedEntity tumble=BMEntities.TUMBLEWEED.create(level, EntitySpawnReason.NATURAL);
            if(tumble!=null){ tumble.setPos(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5); level.addFreshEntity(tumble); }
            break;
        }
    }); }
}
