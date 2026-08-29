package party.lemons.biomemakeover.init;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.BlockEntityType;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.block.entity.LightningBugBottleBlockEntity;
import party.lemons.biomemakeover.block.entity.AltarBlockEntity;
import party.lemons.biomemakeover.mixin.BlockEntityTypeAccessor;

public final class BMBlockEntities {
    private static final ResourceKey<BlockEntityType<?>> KEY=ResourceKey.create(Registries.BLOCK_ENTITY_TYPE,BiomeMakeover.id("lightning_bug_bottle"));
    public static final BlockEntityType<LightningBugBottleBlockEntity> LIGHTNING_BUG_BOTTLE=Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,KEY,FabricBlockEntityTypeBuilder.create(LightningBugBottleBlockEntity::new,BMBlocks.LIGHTNING_BUG_BOTTLE).build());
    private static final ResourceKey<BlockEntityType<?>> ALTAR_KEY=ResourceKey.create(Registries.BLOCK_ENTITY_TYPE,BiomeMakeover.id("altar"));
    public static final BlockEntityType<AltarBlockEntity> ALTAR=Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,ALTAR_KEY,FabricBlockEntityTypeBuilder.create(AltarBlockEntity::new,BMBlocks.ALTAR).build());
    private BMBlockEntities() {}
    public static void initialize() {
        // Vanilla BrushableBlockEntity validates its BlockEntityType against a
        // closed valid-block set. Extend that set for the source-equivalent
        // custom BrushableBlock without duplicating vanilla archaeology logic.
        ((BlockEntityTypeAccessor) (Object) BlockEntityType.BRUSHABLE_BLOCK)
            .biomemakeover$getValidBlocks().add(BMBlocks.SUSPICIOUS_RED_SAND);
    }
}
