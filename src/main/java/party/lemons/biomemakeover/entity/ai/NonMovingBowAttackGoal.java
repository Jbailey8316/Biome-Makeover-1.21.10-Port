package party.lemons.biomemakeover.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Items;

import java.util.EnumSet;

/** Released Adjudicator mounted-phase bow controller; it never navigates. */
public final class NonMovingBowAttackGoal<T extends Monster & RangedAttackMob> extends Goal {
    private final T actor;
    private final int attackInterval;
    private int cooldown = -1;
    private int targetSeeingTicker;

    public NonMovingBowAttackGoal(T actor, int attackInterval, float range) {
        this.actor = actor;
        this.attackInterval = attackInterval;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override public boolean canUse() { return actor.getTarget() != null && isHoldingBow(); }
    @Override public boolean canContinueToUse() { return (canUse() || !actor.getNavigation().isDone()) && isHoldingBow(); }
    private boolean isHoldingBow() { return actor.isHolding(Items.BOW); }
    @Override public void start() { actor.setAggressive(true); }
    @Override public void stop() {
        actor.setAggressive(false); targetSeeingTicker = 0; cooldown = -1;
        actor.stopUsingItem(); actor.getNavigation().stop();
    }
    @Override public void tick() {
        LivingEntity target = actor.getTarget();
        if (target == null) return;
        boolean visible = actor.getSensing().hasLineOfSight(target);
        if (visible != (targetSeeingTicker > 0)) targetSeeingTicker = 0;
        targetSeeingTicker = visible ? ++targetSeeingTicker : --targetSeeingTicker;
        actor.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (actor.isUsingItem()) {
            if (!visible && targetSeeingTicker < -60) actor.stopUsingItem();
            else if (visible && actor.getTicksUsingItem() >= 20) {
                int useTicks = actor.getTicksUsingItem();
                actor.stopUsingItem();
                actor.performRangedAttack(target, BowItem.getPowerForTime(useTicks));
                cooldown = attackInterval;
            }
        } else if (--cooldown <= 0 && targetSeeingTicker >= -60) {
            actor.startUsingItem(ProjectileUtil.getWeaponHoldingHand(actor, Items.BOW));
        }
    }
}
