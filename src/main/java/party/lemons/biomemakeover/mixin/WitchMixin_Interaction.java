package party.lemons.biomemakeover.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import party.lemons.biomemakeover.crafting.witch.WitchQuestEntity;

/** Mob.mobInteract is the current 1.21.10 declaration point for Witch interaction. */
@Mixin(Mob.class)
public abstract class WitchMixin_Interaction extends LivingEntity {
    protected WitchMixin_Interaction(EntityType<? extends LivingEntity> type, Level level) { super(type, level); }

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void bmInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if ((Object) this instanceof Witch && (Object) this instanceof WitchQuestEntity witch
                && isAlive() && !witch.hasCustomer() && witch.canInteract(player)) {
            if (!level().isClientSide() && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                witch.setCurrentCustomer(player);
                witch.sendQuests(serverPlayer, getDisplayName());
            }
            cir.setReturnValue(level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
        }
    }
}
