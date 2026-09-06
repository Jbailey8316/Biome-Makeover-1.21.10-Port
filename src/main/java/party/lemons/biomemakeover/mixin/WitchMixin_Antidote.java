package party.lemons.biomemakeover.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import party.lemons.biomemakeover.crafting.witch.WitchQuestEntity;

@Mixin(LivingEntity.class)
public abstract class WitchMixin_Antidote extends net.minecraft.world.entity.Entity {
    protected WitchMixin_Antidote(EntityType<?> type, Level level) { super(type, level); }
    @Inject(method = "aiStep", at = @At("TAIL"))
    private void biomemakeover$offerAntidote(CallbackInfo ci) {
        if ((Object) this instanceof WitchQuestEntity witch && (Object) this instanceof net.minecraft.world.entity.monster.Witch)
            witch.offerAntidote();
    }
}
