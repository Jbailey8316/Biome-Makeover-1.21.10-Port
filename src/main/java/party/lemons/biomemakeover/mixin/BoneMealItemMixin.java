package party.lemons.biomemakeover.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.level.BMWorldEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

@Mixin(BoneMealItem.class)
public abstract class BoneMealItemMixin {
    private static final TagKey<Biome> SWAMP_BONEMEAL=TagKey.create(Registries.BIOME,BiomeMakeover.id("swamp_bonemeal"));
    @Inject(method="growWaterPlant",at=@At("HEAD"),cancellable=true)
    private static void biomemakeover$growSwamp(ItemStack stack,Level level,BlockPos pos,Direction direction,CallbackInfoReturnable<Boolean> cir){
        if(level.getBiome(pos).is(SWAMP_BONEMEAL)&&level.getBlockState(pos).is(Blocks.WATER)&&level.getFluidState(pos).isSource()){
            if(level instanceof ServerLevel)BMWorldEvents.handleSwampBoneMeal(level,pos,level.random);cir.setReturnValue(true);
        }
    }
}
