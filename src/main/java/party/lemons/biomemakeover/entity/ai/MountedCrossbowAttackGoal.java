package party.lemons.biomemakeover.entity.ai;

import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ChargedProjectiles;

import java.util.EnumSet;

/** Released Ravager-phase crossbow controller: mounted, stationary, LOOK-only. */
public final class MountedCrossbowAttackGoal<T extends Monster & CrossbowAttackMob> extends Goal {
    private static final UniformInt CHARGE_DELAY = TimeUtil.rangeOfSeconds(1, 2);
    private final T actor;
    private final float rangeSqr;
    private CrossbowState state = CrossbowState.UNCHARGED;
    private int seeingTargetTicker;
    private int chargedTicksLeft;

    public MountedCrossbowAttackGoal(T actor, float range) {
        this.actor = actor;
        this.rangeSqr = range * range;
        setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override public boolean canUse() { return hasAliveTarget() && isHoldingCrossbow(); }
    @Override public boolean canContinueToUse() { return canUse(); }
    private boolean hasAliveTarget() { return actor.getTarget() != null && actor.getTarget().isAlive(); }
    private boolean isHoldingCrossbow() { return actor.isHolding(Items.CROSSBOW); }

    @Override public void start() { actor.setAggressive(true); }
    @Override public void stop() {
        actor.setAggressive(false);
        actor.setTarget(null);
        seeingTargetTicker = 0;
        state = CrossbowState.UNCHARGED;
        if (actor.isUsingItem()) {
            actor.stopUsingItem();
            actor.setChargingCrossbow(false);
            actor.getUseItem().set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
        }
    }

    @Override public void tick() {
        LivingEntity target = actor.getTarget();
        if (target == null) return;
        boolean visible = actor.getSensing().hasLineOfSight(target);
        if (visible != (seeingTargetTicker > 0)) seeingTargetTicker = 0;
        seeingTargetTicker = visible ? ++seeingTargetTicker : --seeingTargetTicker;
        double distance = actor.distanceToSqr(target);
        boolean outOfRange = (distance > rangeSqr || seeingTargetTicker < 5) && chargedTicksLeft == 0;
        actor.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (state == CrossbowState.UNCHARGED) {
            if (!outOfRange) {
                actor.startUsingItem(ProjectileUtil.getWeaponHoldingHand(actor, Items.CROSSBOW));
                state = CrossbowState.CHARGING;
                actor.setChargingCrossbow(true);
            }
        } else if (state == CrossbowState.CHARGING) {
            if (!actor.isUsingItem()) state = CrossbowState.UNCHARGED;
            else if (actor.getTicksUsingItem() >= CrossbowItem.getChargeDuration(actor.getUseItem(), actor)) {
                actor.stopUsingItem();
                state = CrossbowState.CHARGED;
                chargedTicksLeft = 2 + actor.getRandom().nextInt(10);
                actor.setChargingCrossbow(false);
            }
        } else if (state == CrossbowState.CHARGED) {
            if (--chargedTicksLeft <= 0) state = CrossbowState.READY_TO_ATTACK;
        } else if (state == CrossbowState.READY_TO_ATTACK && visible) {
            actor.performCrossbowAttack(actor, 1.0F);
            actor.getItemInHand(ProjectileUtil.getWeaponHoldingHand(actor, Items.CROSSBOW))
                .set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
            state = CrossbowState.UNCHARGED;
        }
    }

    private enum CrossbowState { UNCHARGED, CHARGING, CHARGED, READY_TO_ATTACK }
}
