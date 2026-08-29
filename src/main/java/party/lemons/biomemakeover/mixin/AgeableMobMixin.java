package party.lemons.biomemakeover.mixin;

import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import party.lemons.biomemakeover.util.extension.Stuntable;

@Mixin(AgeableMob.class)
public abstract class AgeableMobMixin implements Stuntable {
    @Unique private static final String BIOMEMAKEOVER_STUNTED_KEY = "bm_IsStunted";
    @Unique private static final int BIOMEMAKEOVER_STUNTED_AGE = -6000;
    @Unique private boolean biomemakeover$stunted;

    @Inject(method = "setAge", at = @At("HEAD"), cancellable = true)
    private void biomemakeover$holdStuntedAge(int age, CallbackInfo ci) {
        if (biomemakeover$stunted && age != BIOMEMAKEOVER_STUNTED_AGE) {
            ci.cancel();
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void biomemakeover$writeStunted(ValueOutput output, CallbackInfo ci) {
        output.putBoolean(BIOMEMAKEOVER_STUNTED_KEY, biomemakeover$stunted);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void biomemakeover$readStunted(ValueInput input, CallbackInfo ci) {
        biomemakeover$setStunted(input.getBooleanOr(BIOMEMAKEOVER_STUNTED_KEY, false));
    }

    @Override
    public boolean biomemakeover$isStunted() {
        return biomemakeover$stunted;
    }

    @Override
    public void biomemakeover$setStunted(boolean stunted) {
        AgeableMob self = (AgeableMob) (Object) this;
        if (stunted) {
            // Let vanilla update DATA_BABY_ID, dimensions and renderer state first.
            self.setAge(BIOMEMAKEOVER_STUNTED_AGE);
        }
        biomemakeover$stunted = stunted;
    }
}
