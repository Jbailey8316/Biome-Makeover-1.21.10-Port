package party.lemons.biomemakeover.command;

import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import party.lemons.biomemakeover.mixin.PatrolSpawnerInvoker;

/** Temporary operator-only runtime hook. Remove after Cowboy patrol Prism acceptance. */
public final class BMDebugCommands {
    private BMDebugCommands() {}

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher,registryAccess,environment) -> dispatcher.register(
            Commands.literal("bmtest").requires(source -> source.hasPermission(2))
                .then(Commands.literal("cowboy_patrol").executes(context -> spawnCowboyPatrol(context.getSource().getPlayerOrException())))));
    }

    private static int spawnCowboyPatrol(ServerPlayer player) {
        ServerLevel level=player.level();
        BlockPos origin=player.blockPosition();
        if(!level.getBiome(origin).is(BiomeTags.IS_BADLANDS)) {
            player.sendSystemMessage(Component.literal("Stand in a Badlands biome before running /bmtest cowboy_patrol."));
            return 0;
        }

        PatrolSpawnerInvoker spawner=(PatrolSpawnerInvoker)(Object)new PatrolSpawner();
        int spawned=0;
        int[][] offsets={{3,0},{-3,0},{0,3},{0,-3},{5,2},{-5,-2},{2,-5},{-2,5},{7,0},{0,7}};
        for(int[] offset:offsets) {
            int x=origin.getX()+offset[0], z=origin.getZ()+offset[1];
            int y=level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z);
            BlockPos spawnPos=new BlockPos(x,y,z);
            if(!level.getBiome(spawnPos).is(BiomeTags.IS_BADLANDS)) continue;
            boolean leader=spawned==0;
            if(spawner.biomemakeover$spawnPatrolMember(level,spawnPos,level.random,leader)) spawned++;
            if(spawned==4) break;
        }

        int result=spawned;
        player.sendSystemMessage(Component.literal(result==4
            ? "Spawned a production-path four-member Cowboy patrol (first member is leader)."
            : "Production patrol path spawned "+result+"/4 members; move to flatter open Badlands terrain and retry."));
        return result==4 ? Command.SINGLE_SUCCESS : 0;
    }
}
