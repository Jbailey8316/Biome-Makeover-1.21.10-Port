package party.lemons.biomemakeover.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import party.lemons.biomemakeover.init.BMAdvancements;

public final class GlowfishBucketItem extends MobBucketItem {
    public GlowfishBucketItem(EntityType<? extends Mob> entity, Fluid fluid, SoundEvent sound, Item.Properties properties) {
        super(entity, fluid, sound, properties);
    }

    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        InteractionResult result=super.use(level,player,hand);
        if (!level.isClientSide() && result.consumesAction() && player.fallDistance >= 23.0F)
            BMAdvancements.GLOWFISH_SAVE.trigger((ServerPlayer)player);
        return result;
    }
}
