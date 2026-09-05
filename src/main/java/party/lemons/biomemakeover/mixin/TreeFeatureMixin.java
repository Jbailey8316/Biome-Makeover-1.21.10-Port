package party.lemons.biomemakeover.mixin;

import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import party.lemons.biomemakeover.worldgen.mansion.MansionTreeProtection;

@Mixin(TreeFeature.class)
public abstract class TreeFeatureMixin {
    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void biomemakeover$rejectMansionTree(FeaturePlaceContext<TreeConfiguration> context,
                                                  CallbackInfoReturnable<Boolean> cir) {
        if (MansionTreeProtection.rejects(context.level(), context.origin(), context.config())) cir.setReturnValue(false);
    }
}
