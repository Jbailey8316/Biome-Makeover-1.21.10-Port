package party.lemons.biomemakeover.entity.ai;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import party.lemons.biomemakeover.crafting.witch.WitchQuestEntity;
import java.util.EnumSet;

public final class WitchStopFollowingCustomerGoal extends Goal {
    private final Witch witch;
    public WitchStopFollowingCustomerGoal(Witch witch) { this.witch = witch; setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE)); }
    @Override public boolean canUse() {
        if (!witch.isAlive() || witch.isInWater() || !witch.onGround() || witch.hurtMarked) return false;
        Player player = ((WitchQuestEntity) witch).getCurrentCustomer();
        return player != null && witch.distanceToSqr(player) <= 16.0D && player.containerMenu != null;
    }
}
