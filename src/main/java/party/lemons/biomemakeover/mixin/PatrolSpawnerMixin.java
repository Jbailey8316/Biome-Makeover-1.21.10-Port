package party.lemons.biomemakeover.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import party.lemons.biomemakeover.init.BMEntities;

@Mixin(PatrolSpawner.class)
public abstract class PatrolSpawnerMixin {
    @Inject(method="spawnPatrolMember",at=@At("HEAD"),cancellable=true)
    private void biomemakeover$badlandsCowboy(ServerLevel level, BlockPos pos, RandomSource random, boolean leader, CallbackInfoReturnable<Boolean> cir){
        if(!level.getBiome(pos).is(BiomeTags.IS_BADLANDS)) return;
        var state=level.getBlockState(pos);
        if(!NaturalSpawner.isValidEmptySpawnBlock(level,pos,state,state.getFluidState(),BMEntities.COWBOY)
            || !PatrollingMonster.checkPatrollingMonsterSpawnRules(BMEntities.COWBOY,level,EntitySpawnReason.PATROL,pos,random)){cir.setReturnValue(false);return;}
        var cowboy=BMEntities.COWBOY.create(level,EntitySpawnReason.PATROL); Horse horse=EntityType.HORSE.create(level,EntitySpawnReason.PATROL);
        if(cowboy==null||horse==null){cir.setReturnValue(false);return;}
        horse.setPos(pos.getX(),pos.getY(),pos.getZ()); horse.finalizeSpawn(level,level.getCurrentDifficultyAt(pos),EntitySpawnReason.PATROL,null);
        cowboy.setPos(pos.getX(),pos.getY(),pos.getZ()); cowboy.setPatrolLeader(leader); if(leader)cowboy.findPatrolTarget();
        cowboy.finalizeSpawn(level,level.getCurrentDifficultyAt(pos),EntitySpawnReason.PATROL,null); cowboy.startRiding(horse);
        level.addFreshEntityWithPassengers(horse); cir.setReturnValue(true);
    }
}
