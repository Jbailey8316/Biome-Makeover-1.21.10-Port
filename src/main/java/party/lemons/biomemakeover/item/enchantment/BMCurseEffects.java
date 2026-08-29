package party.lemons.biomemakeover.item.enchantment;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import party.lemons.biomemakeover.init.BMEnchantments;

/** Server-authoritative effects which cannot be represented exactly by modern enchantment components. */
public final class BMCurseEffects {
    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private BMCurseEffects() {}

    public static void tick(LivingEntity entity, ServerLevel level) {
        for (EquipmentSlot slot : ARMOR) {
            int insomnia = BMEnchantments.equippedLevel(entity, slot, BMEnchantments.INSOMNIA_CURSE);
            if (insomnia > 0 && entity instanceof Player player && !player.isSleeping()) {
                player.awardStat(Stats.TIME_SINCE_REST, insomnia);
            }

            int conductivity = BMEnchantments.equippedLevel(entity, slot, BMEnchantments.CONDUCTIVITY_CURSE);
            if (conductivity > 0 && level.random.nextInt(conductivityDenominator(conductivity)) == 0 && level.isThundering()) {
                BlockPos pos = entity.getOnPos();
                if (level.isRainingAt(pos)) {
                    LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
                    if (bolt != null) {
                        bolt.setPos(Vec3.atBottomCenterOf(pos));
                        level.addFreshEntity(bolt);
                    }
                }
            }
        }

        int depth = BMEnchantments.equippedLevel(entity, EquipmentSlot.FEET, BMEnchantments.DEPTH_CURSE);
        if (depth > 0 && !(entity instanceof Player player && player.getAbilities().flying) && entity.isInWater()) {
            Vec3 velocity = entity.getDeltaMovement();
            if (velocity.y > -1.0D) {
                double force = depthForce(depth);
                entity.setDeltaMovement(velocity.x, Math.max(-force, velocity.y - force), velocity.z);
                entity.hasImpulse = true;
                entity.hurtMarked = true;
            }
        }
    }

    public static int conductivityDenominator(int level) { return 11000 - level * 1000; }
    public static double depthForce(int level) { return 0.05D * level; }
    public static int maximumAir(int level) { return (int)(300.0F / (level * 1.5F)); }
    public static int extendedFireTicks(int ticks, int level) { return ticks + (int)(ticks * (level / 2.0F)); }
    public static double buckledDistance(double distance, int level) { return distance >= 3.0D ? distance + level : distance; }
    public static float inaccuracyDegrees(float unit, int direction, int level) { return unit * (level * 1.3F) * direction; }
}
