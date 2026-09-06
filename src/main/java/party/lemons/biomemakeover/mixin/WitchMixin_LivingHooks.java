package party.lemons.biomemakeover.mixin;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import party.lemons.biomemakeover.crafting.witch.WitchQuestEntity;

@Mixin(LivingEntity.class)
public abstract class WitchMixin_LivingHooks extends net.minecraft.world.entity.Entity {
    protected WitchMixin_LivingHooks(EntityType<?> type, Level level) { super(type, level); }
    @Inject(method = "die", at = @At("TAIL")) private void biomemakeover$clearCustomer(DamageSource source, CallbackInfo ci) {
        if ((Object) this instanceof WitchQuestEntity witch && (Object) this instanceof Witch) witch.setCurrentCustomer(null);
    }
    @Inject(method = "dropFromLootTable", at = @At("TAIL"))
    private void biomemakeover$hatLoot(ServerLevel level, DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        if (!((Object) this instanceof WitchQuestEntity) || !((Object) this instanceof Witch)) return;
        ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE, party.lemons.biomemakeover.BiomeMakeover.id("entities/witch_hat"));
        LootParams params = new LootParams(level, new ContextMap.Builder()
            .withParameter(LootContextParams.THIS_ENTITY, this)
            .withParameter(LootContextParams.ORIGIN, position())
            .withParameter(LootContextParams.DAMAGE_SOURCE, source)
            .withOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER, causedByPlayer && source.getEntity() instanceof net.minecraft.world.entity.player.Player p ? p : null)
            .create(LootTable.DEFAULT_PARAM_SET), java.util.Map.of(), 0.0F);
        level.getServer().reloadableRegistries().getLootTable(key).getRandomItems(params, stack -> ((LivingEntity)(Object)this).spawnAtLocation(level, stack));
    }
}
