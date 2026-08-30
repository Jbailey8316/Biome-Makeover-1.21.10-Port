package party.lemons.biomemakeover.mobeffect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import party.lemons.biomemakeover.level.PoltergeistHandler;

/**
 * The final effect is the shared status-effect half of the Poltergeist system.
 * Its world interaction is intentionally supplied by Poltergeist in 10C.3;
 * keeping the effect itself registered here preserves duration/tick semantics
 * without creating a premature later-stage dependency.
 */
public final class PossessedEffect extends MobEffect {
    public PossessedEffect() { super(MobEffectCategory.HARMFUL, 0x20C09E); }

    @Override
    public boolean applyEffectTick(net.minecraft.server.level.ServerLevel level, LivingEntity entity, int amplifier) {
        for (int i = 0; i < Math.min(amplifier + 1, 20); i++) {
            PoltergeistHandler.doPoltergeist(level, entity, entity.blockPosition(), 4);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 10 < Math.min(amplifier + 1, 8);
    }
}
