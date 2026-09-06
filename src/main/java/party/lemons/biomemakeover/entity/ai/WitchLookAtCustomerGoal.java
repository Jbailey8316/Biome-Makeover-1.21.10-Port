package party.lemons.biomemakeover.entity.ai;

import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import party.lemons.biomemakeover.crafting.witch.WitchQuestEntity;

public final class WitchLookAtCustomerGoal extends LookAtPlayerGoal {
    private final WitchQuestEntity questWitch;
    public WitchLookAtCustomerGoal(Witch witch) { super(witch, Player.class, 8.0F); questWitch = (WitchQuestEntity) witch; }
    @Override public boolean canUse() { if (!questWitch.hasCustomer()) return false; lookAt = questWitch.getCurrentCustomer(); return true; }
}
