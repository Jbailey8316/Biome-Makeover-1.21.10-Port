package party.lemons.biomemakeover.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Vex;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import party.lemons.biomemakeover.entity.AdjudicatorAlliance;

/** Copies encounter ownership at vanilla Evoker Vex insertion, after setOwner. */
@Mixin(targets = "net.minecraft.world.entity.monster.Evoker$EvokerSummonSpellGoal")
public abstract class AdjudicatorAllianceEvokerMixin {
    @Redirect(
        method = "performSpellCasting",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V")
    )
    private void biomemakeover$inheritVexEncounter(ServerLevel level, Entity entity) {
        if (entity instanceof Vex vex && vex.getOwner() instanceof Evoker evoker)
            AdjudicatorAlliance.inheritFromOwner(vex, evoker);
        level.addFreshEntityWithPassengers(entity);
    }
}
