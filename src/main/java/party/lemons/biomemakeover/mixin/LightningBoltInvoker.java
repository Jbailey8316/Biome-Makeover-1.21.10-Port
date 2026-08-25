package party.lemons.biomemakeover.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LightningBolt.class)
public interface LightningBoltInvoker {
    @Invoker("clearCopperOnLightningStrike")
    static void biomemakeover$clearCopper(Level level, BlockPos pos) {
        throw new AssertionError("mixin invoker was not applied");
    }
}
