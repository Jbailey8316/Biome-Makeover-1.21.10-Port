package party.lemons.biomemakeover.entity;

import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import party.lemons.biomemakeover.init.BMItems;

public final class CowboyEntity extends Pillager {
    public CowboyEntity(EntityType<? extends Pillager> type, Level level) { super(type,level); }
    @Override protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        super.populateDefaultEquipmentSlots(random,difficulty);
        setItemSlot(EquipmentSlot.HEAD,new ItemStack(BMItems.COWBOY_HAT));
    }
    @Override public void tick(){ super.tick(); if(isPassenger()) getVehicle().setYRot(getYRot()); }
}
