package party.lemons.biomemakeover.init;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.mobeffect.PossessedEffect;

public final class BMEffects {
    public static final Holder<MobEffect> SHOCKED = register("shocked",
        new BasicEffect(MobEffectCategory.HARMFUL, 0x6EFFFF).addAttributeModifier(
            Attributes.MAX_HEALTH, BiomeMakeover.id("effect.shocked.max_health"), -2.0,
            AttributeModifier.Operation.ADD_VALUE));
    public static final Holder<MobEffect> NOCTURNAL = register("nocturnal", new NocturnalEffect());
    public static final Holder<MobEffect> POSSESSED = register("possessed", new PossessedEffect());

    private BMEffects() {}

    private static Holder<MobEffect> register(String name, MobEffect effect) {
        ResourceLocation id = BiomeMakeover.id(name);
        ResourceKey<MobEffect> key = ResourceKey.create(net.minecraft.core.registries.Registries.MOB_EFFECT, id);
        return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, key, effect);
    }

    public static void initialize() {}

    private static final class BasicEffect extends MobEffect {
        private BasicEffect(MobEffectCategory category, int color) { super(category, color); }
    }
    private static final class NocturnalEffect extends MobEffect {
        private NocturnalEffect() { super(MobEffectCategory.BENEFICIAL, 0xBA93C7); }
        @Override public boolean applyEffectTick(net.minecraft.server.level.ServerLevel level,
                net.minecraft.world.entity.LivingEntity entity, int amplifier) {
            if (entity instanceof net.minecraft.server.level.ServerPlayer player) {
                player.resetStat(net.minecraft.stats.Stats.CUSTOM.get(net.minecraft.stats.Stats.TIME_SINCE_REST));
            }
            return true;
        }
        @Override public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) { return duration % 20 == 0; }
    }
}
