package party.lemons.biomemakeover.item;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import party.lemons.biomemakeover.entity.LightningBottleEntity;
import party.lemons.biomemakeover.init.BMSounds;

public final class LightningBottleItem extends Item implements ProjectileItem {
    public LightningBottleItem(Properties properties) { super(properties); }

    @Override
    public Projectile asProjectile(Level level, Position position, ItemStack stack, Direction direction) {
        return new LightningBottleEntity(level, position.x(), position.y(), position.z(), stack.copyWithCount(1));
    }

    @Override
    public DispenseConfig createDispenseConfig() {
        return DispenseConfig.builder().uncertainty(3.0F).power(1.375F).build();
    }

    @Override
    public boolean isFoil(ItemStack stack) { return true; }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), BMSounds.LIGHTNING_BOTTLE_THROW,
            SoundSource.NEUTRAL, 0.5F, 0.4F / (level.random.nextFloat() * 0.4F + 0.8F));
        if (level instanceof ServerLevel server) {
            LightningBottleEntity bottle = new LightningBottleEntity(server, player, stack.copyWithCount(1));
            bottle.shootFromRotation(player, player.getXRot(), player.getYRot(), -20.0F, 0.7F, 1.0F);
            server.addFreshEntity(bottle);
        }
        player.getCooldowns().addCooldown(stack, 45);
        player.awardStat(Stats.ITEM_USED.get(this));
        stack.consume(1, player);
        return InteractionResult.SUCCESS;
    }

    public static void registerDispenserBehavior(Item item) { DispenserBlock.registerProjectileBehavior(item); }
}
