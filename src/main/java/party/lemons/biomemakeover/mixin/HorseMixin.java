package party.lemons.biomemakeover.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import party.lemons.biomemakeover.entity.HorseHat;

@Mixin(Horse.class)
public abstract class HorseMixin extends AbstractHorse implements HorseHat {
    @Unique private static final EntityDataAccessor<Boolean> BIOMEMAKEOVER_HAS_HAT =
        SynchedEntityData.defineId(Horse.class,EntityDataSerializers.BOOLEAN);
    @Unique private boolean biomemakeover$cowboySpawned;

    protected HorseMixin(EntityType<? extends AbstractHorse> type, Level level) { super(type,level); }

    @Inject(method="defineSynchedData",at=@At("TAIL"))
    private void biomemakeover$defineCowboyData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(BIOMEMAKEOVER_HAS_HAT,false);
    }

    @Inject(method="addAdditionalSaveData",at=@At("RETURN"))
    private void biomemakeover$writeCowboyData(ValueOutput output, CallbackInfo ci) {
        output.putBoolean("Hat",biomemakeover$hasHat());
        output.putBoolean("CowboySpawned",biomemakeover$cowboySpawned);
    }

    @Inject(method="readAdditionalSaveData",at=@At("RETURN"))
    private void biomemakeover$readCowboyData(ValueInput input, CallbackInfo ci) {
        Horse horse=(Horse)(Object)this;
        horse.getEntityData().set(BIOMEMAKEOVER_HAS_HAT,input.getBooleanOr("Hat",false));
        biomemakeover$cowboySpawned=input.getBooleanOr("CowboySpawned",false);
    }

    @Override public boolean removeWhenFarAway(double distanceSquared) {
        if (getControllingPassenger()==null) {
            if (!getItemBySlot(EquipmentSlot.SADDLE).isEmpty()
                || !getItemBySlot(EquipmentSlot.BODY).isEmpty() || getLeashData()!=null) {
                biomemakeover$cowboySpawned=false;
                return false;
            }
            return biomemakeover$cowboySpawned;
        }
        if (getControllingPassenger() instanceof PatrollingMonster patrol)
            return patrol.removeWhenFarAway(distanceSquared);
        return biomemakeover$cowboySpawned;
    }

    @Override public boolean biomemakeover$hasHat() {
        return ((Horse)(Object)this).getEntityData().get(BIOMEMAKEOVER_HAS_HAT);
    }
    @Override public void biomemakeover$setHat() {
        ((Horse)(Object)this).getEntityData().set(BIOMEMAKEOVER_HAS_HAT,true);
    }
    @Override public void biomemakeover$setCowboySpawned() { biomemakeover$cowboySpawned=true; }
}
