package party.lemons.biomemakeover.entity.ai;

import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.ItemStack;
import party.lemons.biomemakeover.entity.StoneGolemEntity;
import java.util.EnumSet;

/** Released Stone Golem crossbow controller, adapted to current mappings. */
public final class BetterCrossbowAttackGoal<T extends Mob & CrossbowAttackMob> extends Goal {
    private static final UniformInt PATH_DELAY = TimeUtil.rangeOfSeconds(1, 2);
    private final T mob;
    private final double speed;
    private final float rangeSqr;
    private CrossbowState state = CrossbowState.UNCHARGED;
    private int seeTime;
    private int attackDelay;
    private int pathDelay;
    private boolean traceStarted;
    private CrossbowState traceState = CrossbowState.UNCHARGED;
    private Boolean lastContinueResult;

    public BetterCrossbowAttackGoal(T mob, double speed, float range) {
        this.mob = mob;
        this.speed = speed;
        this.rangeSqr = range * range;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }
    @Override public boolean canUse() { return validTarget() && mob.isHolding(Items.CROSSBOW); }
    @Override public boolean canContinueToUse() {
        boolean targetPresent = mob.getTarget() != null;
        boolean targetAlive = targetPresent && mob.getTarget().isAlive();
        boolean weaponValid = mob.isHolding(Items.CROSSBOW);
        boolean result = targetAlive && weaponValid;
        if (lastContinueResult == null || lastContinueResult != result) {
            lastContinueResult = result;
            trace("BM_GOLEM_CROSSBOW_CONTINUE result=" + result + " targetPresent=" + targetPresent
                + " targetAlive=" + targetAlive + " weaponValid=" + weaponValid
                + " navigationDone=" + mob.getNavigation().isDone() + " charging="
                + (mob instanceof StoneGolemEntity golem && golem.isChargingCrossbow())
                + " passengerCount=" + mob.getPassengers().size());
        }
        return result;
    }
    private boolean validTarget() { return mob.getTarget() != null && mob.getTarget().isAlive(); }
    @Override public void start() {
        mob.setAggressive(true);
        lastContinueResult = null;
        trace("BM_GOLEM_CROSSBOW_GOAL_START target=" + targetDescription() + " mainHand=" + mob.getMainHandItem().getItem()
            + " playerCreated=" + (mob instanceof StoneGolemEntity golem && golem.isPlayerCreated())
            + " passengers=" + mob.getPassengers().size());
    }
    @Override public void stop() {
        boolean continuationBeforeStop = canContinueToUse();
        mob.setAggressive(false); seeTime = 0; state = CrossbowState.UNCHARGED;
        mob.setTarget(null);
        if (mob.isUsingItem()) { mob.stopUsingItem(); mob.setChargingCrossbow(false); mob.getUseItem().set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY); }
        trace("BM_GOLEM_CROSSBOW_GOAL_STOP target=" + targetDescription() + " crossbow=" + mob.isHolding(Items.CROSSBOW)
            + " continuationBeforeStop=" + continuationBeforeStop);
    }
    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null) return;
        boolean visible = mob.getSensing().hasLineOfSight(target);
        if (visible != (seeTime > 0)) seeTime = 0;
        seeTime = visible ? ++seeTime : --seeTime;
        double distance = mob.distanceToSqr(target);
        boolean reposition = (distance > rangeSqr || seeTime < 5) && attackDelay == 0;
        if (reposition) {
            if (--pathDelay <= 0) { mob.getNavigation().moveTo(target, state == CrossbowState.UNCHARGED ? speed : speed * .5D); pathDelay = PATH_DELAY.sample(mob.getRandom()); }
        } else { pathDelay = 0; mob.getNavigation().stop(); }
        mob.getLookControl().setLookAt(target, 30, 30);
        if (state == CrossbowState.UNCHARGED) {
            if (!reposition) { mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(mob, Items.CROSSBOW)); state = CrossbowState.CHARGING; mob.setChargingCrossbow(true); }
        } else if (state == CrossbowState.CHARGING) {
            if (!mob.isUsingItem()) state = CrossbowState.UNCHARGED;
            else if (mob.getTicksUsingItem() >= 25) {
                mob.releaseUsingItem(); state = CrossbowState.CHARGED; attackDelay = 20 + mob.getRandom().nextInt(20); mob.setChargingCrossbow(false);
            }
        } else if (state == CrossbowState.CHARGED) {
            if (--attackDelay <= 0) state = CrossbowState.READY;
        } else if (state == CrossbowState.READY && visible) {
            mob.performRangedAttack(target, 1.0F);
            mob.getItemInHand(ProjectileUtil.getWeaponHoldingHand(mob, Items.CROSSBOW)).set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
            state = CrossbowState.UNCHARGED;
        }
        if (state != traceState) {
            traceState = state;
            trace("BM_GOLEM_CROSSBOW_GOAL_TICK state=" + state + " target=" + targetDescription()
                + " distance=" + distance + " visible=" + visible + " navigationDone=" + mob.getNavigation().isDone()
                + " passengerCount=" + mob.getPassengers().size());
        }
    }
    private String targetDescription() { return mob.getTarget() == null ? "null" : mob.getTarget().getUUID() + "/" + mob.getTarget().getType(); }
    private void trace(String message) {
        if (mob instanceof StoneGolemEntity && Boolean.getBoolean("bm.mansion.trace")) System.out.println(message + " golem=" + mob.getUUID());
    }
    private enum CrossbowState { UNCHARGED, CHARGING, CHARGED, READY }
}
