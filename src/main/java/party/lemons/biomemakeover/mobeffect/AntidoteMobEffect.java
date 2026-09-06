package party.lemons.biomemakeover.mobeffect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.InstantenousMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import party.lemons.biomemakeover.init.BMAdvancements;
import party.lemons.biomemakeover.util.extension.Stuntable;

public final class AntidoteMobEffect extends InstantenousMobEffect {
    public AntidoteMobEffect() { super(MobEffectCategory.BENEFICIAL, 0xFFFFFF); }
    private void cure(LivingEntity target) {
        target.getActiveEffects().stream().filter(effect -> effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL)
            .map(effect -> effect.getEffect()).toList().forEach(target::removeEffect);
        if (target instanceof Player player && player instanceof ServerPlayer serverPlayer) BMAdvancements.ANTIDOTE.trigger(serverPlayer);
        if (target instanceof Stuntable stuntable) stuntable.biomemakeover$setStunted(false);
    }
    @Override public void applyInstantenousEffect(ServerLevel level, Entity source, Entity attacker, LivingEntity target, int amplifier, double proximity) { cure(target); }
}
