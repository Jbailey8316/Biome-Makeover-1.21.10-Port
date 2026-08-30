package party.lemons.biomemakeover.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DaylightDetectorBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DaylightDetectorBlock.class)
public interface DaylightDetectorBlockInvoker {
    @Invoker("updateSignalStrength")
    static void biomemakeover$updateSignalStrength(BlockState state, Level level, BlockPos pos) {
        throw new AssertionError();
    }
}
