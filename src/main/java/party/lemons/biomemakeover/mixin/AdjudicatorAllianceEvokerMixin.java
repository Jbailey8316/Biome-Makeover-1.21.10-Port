package party.lemons.biomemakeover.mixin;

import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import party.lemons.biomemakeover.entity.AdjudicatorAlliance;

@Mixin(Evoker.class)
public abstract class AdjudicatorAllianceEvokerMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void biomemakeover$inheritVexEncounter(CallbackInfo ci) {
        Evoker evoker = (Evoker) (Object) this;
        if (AdjudicatorAlliance.encounterId(evoker) == null || !(evoker.level() instanceof net.minecraft.server.level.ServerLevel)) return;
        for (Vex vex : evoker.level().getEntitiesOfClass(Vex.class, evoker.getBoundingBox().inflate(8.0D))) {
            if (AdjudicatorAlliance.encounterId(vex) == null) {
                // The tag is assigned through the encounter's boss tag carried by the Evoker.
                String id = AdjudicatorAlliance.encounterId(evoker);
                vex.addTag("bm_adjudicator_encounter:" + id);
                AdjudicatorAlliance.evokerVexInherited(evoker, vex);
            }
        }
    }
}
