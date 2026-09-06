package party.lemons.biomemakeover.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import party.lemons.biomemakeover.init.BMPotions;

@Mixin(Witch.class)
public abstract class WitchMixin_Antidote extends Raider {
    @Shadow public abstract boolean isDrinkingPotion();
    @Shadow public abstract void setUsingItem(boolean using);
    @Shadow private int usingTime;
    @Shadow @Final private static AttributeModifier SPEED_MODIFIER_DRINKING;
    protected WitchMixin_Antidote(EntityType<? extends Raider> type, Level level) { super(type, level); }
    @Inject(method = "aiStep", at = @At("TAIL"))
    private void biomemakeover$offerAntidote(CallbackInfo ci) {
        if (isDrinkingPotion() || getRandom().nextFloat() >= 0.10F) return;
        boolean harmful = getActiveEffects().stream().anyMatch(effect -> effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL);
        if (!harmful) return;
        ItemStack potion = new ItemStack(Items.POTION);
        potion.set(DataComponents.POTION_CONTENTS, new PotionContents(BMPotions.ANTIDOTE_POT));
        setItemSlot(EquipmentSlot.MAINHAND, potion); usingTime = potion.getUseDuration(this); setUsingItem(true);
        if (!isSilent()) level().playSound(null, getX(), getY(), getZ(), SoundEvents.WITCH_DRINK, getSoundSource(), 1.0F, 0.8F + getRandom().nextFloat() * 0.4F);
        AttributeInstance speed = getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) { speed.removeModifier(SPEED_MODIFIER_DRINKING); speed.addTransientModifier(SPEED_MODIFIER_DRINKING); }
    }
}
