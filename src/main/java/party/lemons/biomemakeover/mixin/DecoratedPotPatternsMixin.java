package party.lemons.biomemakeover.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.DecoratedPotPattern;
import net.minecraft.world.level.block.entity.DecoratedPotPatterns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import party.lemons.biomemakeover.init.BMItems;

/** Adds the released BM sherd-to-pattern mappings to vanilla's immutable map. */
@Mixin(DecoratedPotPatterns.class)
public abstract class DecoratedPotPatternsMixin {
    @Inject(method = "getPatternFromItem", at = @At("HEAD"), cancellable = true)
    private static void biomemakeover$customPatterns(Item item,
                                                      CallbackInfoReturnable<ResourceKey<DecoratedPotPattern>> cir) {
        if (item == BMItems.CRACKED_BRICK) cir.setReturnValue(BMItems.CRACKED_PATTERN);
        else if (item == BMItems.REFINED_POTTERY_SHERD) cir.setReturnValue(BMItems.REFINED_PATTERN);
        else if (item == BMItems.WORKER_POTTERY_SHERD) cir.setReturnValue(BMItems.WORKER_PATTERN);
        else if (item == BMItems.WHINNY_POTTERY_SHERD) cir.setReturnValue(BMItems.WHINNY_PATTERN);
    }
}
