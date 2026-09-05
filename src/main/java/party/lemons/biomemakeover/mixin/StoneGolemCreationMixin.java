package party.lemons.biomemakeover.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import party.lemons.biomemakeover.entity.StoneGolemCreation;

@Mixin(CarvedPumpkinBlock.class)
public abstract class StoneGolemCreationMixin {
    @Inject(method = "trySpawnGolem", at = @At("HEAD"), cancellable = true)
    private static void biomemakeover$tryStoneGolem(Level level, BlockPos pos, CallbackInfo info) {
        if (StoneGolemCreation.tryCreate(level, pos)) info.cancel();
    }
}
