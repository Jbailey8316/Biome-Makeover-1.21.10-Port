package party.lemons.biomemakeover.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Released Biome Makeover death protector, adapted to the 1.21.10 hook. */
public final class EnchantedTotemItem extends Item {
    public EnchantedTotemItem(Properties properties) { super(properties); }
    @Override public boolean isFoil(ItemStack stack) { return true; }
    public static void activate(LivingEntity entity, ItemStack stack) {
        if (entity instanceof ServerPlayer player) {
            player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
            CriteriaTriggers.USED_TOTEM.trigger(player, stack);
        }
        entity.setHealth(entity.getMaxHealth() / 2.0F);
        entity.removeAllEffects();
        entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 500, 1));
        entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1200, 3));
        entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 2000, 0));
        entity.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 2000, 0));
        entity.level().broadcastEntityEvent(entity, (byte) 35);
        stack.shrink(1);
    }
}
