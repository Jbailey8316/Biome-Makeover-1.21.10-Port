package party.lemons.biomemakeover.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Salmon;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import party.lemons.biomemakeover.init.BMItems;

public final class GlowfishEntity extends Salmon {
    public GlowfishEntity(EntityType<? extends Salmon> type, Level level) { super(type, level); }

    @Override
    public ItemStack getBucketItemStack() { return new ItemStack(BMItems.GLOWFISH_BUCKET); }

    public static AttributeSupplier.Builder createAttributes() { return AbstractFish.createAttributes(); }
}
