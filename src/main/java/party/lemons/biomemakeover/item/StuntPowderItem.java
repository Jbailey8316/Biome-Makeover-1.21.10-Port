package party.lemons.biomemakeover.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import party.lemons.biomemakeover.util.extension.Stuntable;

/** Final 1.20.1 anti-growth interaction translated to the modern item hook. */
public final class StuntPowderItem extends Item {
    public StuntPowderItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof Stuntable stuntable)
            || (!target.isBaby() && !stuntable.biomemakeover$isAlwaysBaby())
            || stuntable.biomemakeover$isStunted()) {
            return super.interactLivingEntity(stack, player, target, hand);
        }

        if (target.level() instanceof ServerLevel serverLevel) {
            stuntable.biomemakeover$setStunted(true);
            serverLevel.sendParticles(ParticleTypes.WARPED_SPORE, target.getX(), target.getY() + target.getBbHeight() * 0.5D,
                target.getZ(), 15, 0.2D, 0.2D, 0.2D, 0.0D);
            stack.consume(1, player);
            serverLevel.gameEvent(target, GameEvent.ENTITY_INTERACT, target.position());
        }
        return InteractionResult.SUCCESS;
    }
}
