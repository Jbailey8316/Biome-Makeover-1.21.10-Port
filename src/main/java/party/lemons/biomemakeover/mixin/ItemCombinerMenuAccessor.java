package party.lemons.biomemakeover.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ResultContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Narrow common access used to preserve the final Altar anvil-cost contract. */
@Mixin(ItemCombinerMenu.class)
public interface ItemCombinerMenuAccessor {
    @Accessor("player") Player biomemakeover$getPlayer();
    @Accessor("inputSlots") Container biomemakeover$getInputSlots();
    @Accessor("resultSlots") ResultContainer biomemakeover$getResultSlots();
}
