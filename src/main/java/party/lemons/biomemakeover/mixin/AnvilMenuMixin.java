package party.lemons.biomemakeover.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import party.lemons.biomemakeover.item.AltarCursing;

/**
 * Keeps the released repair-cost-39 Altar contract effective when another
 * modern mod relaxes vanilla's survival "Too Expensive" ceiling.
 */
@Mixin(AnvilMenu.class)
abstract class AnvilMenuMixin {
    @Shadow private boolean onlyRenaming;

    @Inject(method = "createResult", at = @At("RETURN"))
    private void biomemakeover$enforceAltarAnvilLimit(CallbackInfo ci) {
        ItemCombinerMenuAccessor menu = (ItemCombinerMenuAccessor) this;
        if (menu.biomemakeover$getPlayer().hasInfiniteMaterials() || onlyRenaming) return;

        Container inputs = menu.biomemakeover$getInputSlots();
        if (AltarCursing.hasMarker(inputs.getItem(0)) || AltarCursing.hasMarker(inputs.getItem(1))) {
            menu.biomemakeover$getResultSlots().setItem(0, ItemStack.EMPTY);
        }
    }
}
