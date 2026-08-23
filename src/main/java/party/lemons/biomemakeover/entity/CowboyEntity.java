package party.lemons.biomemakeover.entity;

import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.resources.ResourceLocation;
import party.lemons.biomemakeover.init.BMItems;

public final class CowboyEntity extends Pillager {
    public CowboyEntity(EntityType<? extends Pillager> type, Level level) {
        super(type,level);
        setDropChance(EquipmentSlot.HEAD,.25F);
    }
    @Override protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        super.populateDefaultEquipmentSlots(random,difficulty);
        setItemSlot(EquipmentSlot.HEAD,new ItemStack(BMItems.COWBOY_HAT));
    }
    @Override public void tick(){ super.tick(); if(isPassenger()) getVehicle().setYRot(getYRot()); }

    @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                  EntitySpawnReason reason, SpawnGroupData data) {
        SpawnGroupData result=super.finalizeSpawn(level,difficulty,reason,data);
        if(isPatrolLeader()) {
            setItemSlot(EquipmentSlot.HEAD,getOminousBanner());
            setDropChance(EquipmentSlot.HEAD,2.0F);
        }
        return result;
    }

    public ItemStack getOminousBanner() {
        var patterns=registryAccess().lookupOrThrow(Registries.BANNER_PATTERN);
        ItemStack banner=new ItemStack(Items.WHITE_BANNER);
        banner.set(DataComponents.BANNER_PATTERNS,new BannerPatternLayers.Builder()
            .addIfRegistered(patterns,BannerPatterns.RHOMBUS_MIDDLE,DyeColor.CYAN)
            .addIfRegistered(patterns,BannerPatterns.STRIPE_BOTTOM,DyeColor.RED)
            .addIfRegistered(patterns,BannerPatterns.HALF_HORIZONTAL,DyeColor.BROWN)
            .addIfRegistered(patterns,BannerPatterns.TRIANGLES_TOP,DyeColor.BLACK)
            .addIfRegistered(patterns,BannerPatterns.BORDER,DyeColor.BLACK)
            .addIfRegistered(patterns,BannerPatterns.CIRCLE_MIDDLE,DyeColor.LIGHT_GRAY)
            .addIfRegistered(patterns,BannerPatterns.STRIPE_MIDDLE,DyeColor.BROWN).build());
        banner.set(DataComponents.CUSTOM_NAME,Component.translatable("block.minecraft.ominous_banner").withStyle(ChatFormatting.GOLD));
        return banner;
    }

    @Override public boolean isCaptain() {
        return isPatrolLeader() && ItemStack.matches(getItemBySlot(EquipmentSlot.HEAD),getOminousBanner());
    }

    @Override public void die(DamageSource source) {
        if(level() instanceof ServerLevel && isCaptain()) {
            ServerPlayer player=source.getEntity() instanceof ServerPlayer direct ? direct : null;
            if(source.getEntity() instanceof Wolf wolf && wolf.isTame() && wolf.getOwner() instanceof ServerPlayer owner) player=owner;
            if(player!=null) grantVoluntaryExile(player);
        }
        super.die(source);
    }

    private static void grantVoluntaryExile(ServerPlayer player) {
        var advancement=player.level().getServer().getAdvancements().get(ResourceLocation.withDefaultNamespace("adventure/voluntary_exile"));
        if(advancement==null)return;
        var progress=player.getAdvancements().getOrStartProgress(advancement);
        if(!progress.isDone()) for(String criterion:progress.getRemainingCriteria())
            player.getAdvancements().award(advancement,criterion);
    }
}
