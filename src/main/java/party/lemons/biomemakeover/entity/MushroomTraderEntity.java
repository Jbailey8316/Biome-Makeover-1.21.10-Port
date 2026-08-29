package party.lemons.biomemakeover.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.LookAtTradingPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.Nullable;
import party.lemons.biomemakeover.init.BMBlocks;
import party.lemons.biomemakeover.init.BMEffects;
import party.lemons.biomemakeover.init.BMItems;

import java.util.List;
import java.util.Optional;

/** Final-release Mushroom House merchant. Natural biome spawning remains disabled. */
public final class MushroomTraderEntity extends AbstractVillager {
    public MushroomTraderEntity(EntityType<? extends AbstractVillager> type, Level level) {
        super(type, level);
        ((GroundPathNavigation) getNavigation()).setCanOpenDoors(true);
        getNavigation().setCanFloat(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes();
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        goalSelector.addGoal(1, new OpenDoorGoal(this, true));
        goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Zombie.class, 8.0F, 0.5D, 0.5D));
        goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Evoker.class, 12.0F, 0.5D, 0.5D));
        goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Vindicator.class, 8.0F, 0.5D, 0.5D));
        goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Vex.class, 8.0F, 0.5D, 0.5D));
        goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Pillager.class, 15.0F, 0.5D, 0.5D));
        goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Illusioner.class, 12.0F, 0.5D, 0.5D));
        goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Zoglin.class, 10.0F, 0.5D, 0.5D));
        goalSelector.addGoal(2, new PanicGoal(this, 0.5D));
        goalSelector.addGoal(2, new LookAtTradingPlayerGoal(this));
        goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.25D));
        goalSelector.addGoal(4, new MoveTowardsTargetGoal(this, 0.35D, 5.0F));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Mob.class, 8.0F));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!held.is(Items.VILLAGER_SPAWN_EGG) && isAlive() && !isTrading() && !isBaby()) {
            if (hand == InteractionHand.MAIN_HAND) {
                player.awardStat(Stats.TALKED_TO_VILLAGER);
            }
            if (!level().isClientSide()) {
                if (getOffers().isEmpty()) {
                    return InteractionResult.CONSUME;
                }
                setTradingPlayer(player);
                openTradingScreen(player, getDisplayName(), 1);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason reason) {
        return getY() <= 56
            && level.getEntitiesOfClass(MushroomTraderEntity.class, getBoundingBox().inflate(20.0D)).isEmpty()
            && super.checkSpawnRules(level, reason);
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 1;
    }

    @Override
    protected void rewardTradeXp(MerchantOffer offer) {
        if (offer.shouldRewardExp()) {
            level().addFreshEntity(new ExperienceOrb(level(), getX(), getY() + 0.5D, getZ(), 3 + random.nextInt(4)));
        }
    }

    @Override
    protected void updateTrades() {
        if (level().isClientSide()) {
            return;
        }
        MerchantOffers offers = getOffers();
        VillagerTrades.ItemListing[] common = commonTrades();
        addOffersFromItemListings(offers, common, 5);
        VillagerTrades.ItemListing stew = stewTrades().get(random.nextInt(stewTrades().size()));
        MerchantOffer stewOffer = stew.getOffer(this, random);
        if (stewOffer != null) {
            offers.add(stewOffer);
        }
        MerchantOffer rareOffer = standardTrade(8, BMItems.BUTTON_MUSHROOMS_MUSIC_DISK, 1, 4).getOffer(this, random);
        if (rareOffer != null) {
            offers.add(rareOffer);
        }
    }

    private static VillagerTrades.ItemListing[] commonTrades() {
        return new VillagerTrades.ItemListing[] {
            standardTrade(1, Items.BROWN_MUSHROOM, 3, 12),
            standardTrade(1, Items.RED_MUSHROOM, 3, 12),
            standardTrade(2, BMBlocks.PURPLE_GLOWSHROOM, 3, 12),
            standardTrade(2, BMBlocks.ORANGE_GLOWSHROOM, 3, 12),
            standardTrade(2, BMBlocks.GREEN_GLOWSHROOM, 3, 12),
            standardTrade(2, BMBlocks.GREEN_GLOWSHROOM_BLOCK, 2, 12),
            standardTrade(2, BMBlocks.PURPLE_GLOWSHROOM_BLOCK, 2, 12),
            standardTrade(2, BMBlocks.ORANGE_GLOWSHROOM_BLOCK, 2, 12),
            standardTrade(1, BMBlocks.TALL_BROWN_MUSHROOM, 2, 12),
            standardTrade(1, BMBlocks.TALL_RED_MUSHROOM, 2, 12),
            standardTrade(2, BMBlocks.GLOWSHROOM_STEM, 2, 12),
            standardTrade(1, Items.MUSHROOM_STEM, 2, 12),
            standardTrade(1, Items.RED_MUSHROOM_BLOCK, 2, 12),
            standardTrade(1, Items.BROWN_MUSHROOM_BLOCK, 2, 12),
            standardTrade(1, Items.GLOW_LICHEN, 3, 12),
            standardTrade(1, BMItems.GLOWFISH, 4, 12),
            standardTrade(2, BMItems.COOKED_GLOWFISH, 4, 12),
            standardTrade(3, BMItems.GLOWFISH_BUCKET, 1, 12),
            standardTrade(1, BMBlocks.BLIGHTED_BALSA_SAPLING, 3, 12),
            standardTrade(1, BMBlocks.MYCELIUM_SPROUTS, 5, 12),
            standardTrade(1, BMBlocks.MYCELIUM_ROOTS, 5, 12),
            standardTrade(1, BMBlocks.WILD_MUSHROOMS, 3, 12),
            // Source-confirmed duplicate entry; retain its effective weighting.
            standardTrade(2, BMBlocks.GLOWSHROOM_STEM, 2, 12)
        };
    }

    private static List<VillagerTrades.ItemListing> stewTrades() {
        return List.of(
            suspiciousStewTrade(2, List.of(
                effect(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE, 160),
                effect(net.minecraft.world.effect.MobEffects.BLINDNESS, 320),
                effect(net.minecraft.world.effect.MobEffects.SATURATION, 24),
                effect(net.minecraft.world.effect.MobEffects.JUMP_BOOST, 160),
                effect(net.minecraft.world.effect.MobEffects.REGENERATION, 320),
                effect(net.minecraft.world.effect.MobEffects.POISON, 480),
                effect(net.minecraft.world.effect.MobEffects.NIGHT_VISION, 200),
                effect(net.minecraft.world.effect.MobEffects.WEAKNESS, 360),
                effect(net.minecraft.world.effect.MobEffects.WITHER, 320)
            )),
            suspiciousStewTrade(4, List.of(
                effect(net.minecraft.world.effect.MobEffects.SPEED, 320),
                effect(net.minecraft.world.effect.MobEffects.SLOWNESS, 320),
                effect(net.minecraft.world.effect.MobEffects.HASTE, 320),
                effect(net.minecraft.world.effect.MobEffects.MINING_FATIGUE, 160),
                effect(net.minecraft.world.effect.MobEffects.STRENGTH, 320),
                effect(net.minecraft.world.effect.MobEffects.RESISTANCE, 480),
                effect(net.minecraft.world.effect.MobEffects.LEVITATION, 200),
                effect(net.minecraft.world.effect.MobEffects.WEAKNESS, 360),
                effect(BMEffects.NOCTURNAL, 320)
            ))
        );
    }

    private static SuspiciousStewEffects.Entry effect(net.minecraft.core.Holder<MobEffect> effect, int duration) {
        return new SuspiciousStewEffects.Entry(effect, duration);
    }

    private static VillagerTrades.ItemListing standardTrade(int emeralds, ItemLike result, int count, int maxUses) {
        return (entity, random) -> new MerchantOffer(
            new ItemCost(Items.EMERALD, emeralds),
            new ItemStack(result, count),
            maxUses,
            1,
            0.05F
        );
    }

    private static VillagerTrades.ItemListing suspiciousStewTrade(
        int emeralds,
        List<SuspiciousStewEffects.Entry> effects
    ) {
        return (entity, random) -> {
            ItemStack result = new ItemStack(Items.SUSPICIOUS_STEW);
            result.set(net.minecraft.core.component.DataComponents.SUSPICIOUS_STEW_EFFECTS,
                new SuspiciousStewEffects(List.of(effects.get(random.nextInt(effects.size())))));
            return new MerchantOffer(new ItemCost(Items.EMERALD, emeralds), Optional.empty(), result, 0, 4, 1, 0.05F, 0);
        };
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    @Override public boolean showProgressBar() { return false; }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override protected SoundEvent getTradeUpdatedSound(boolean sold) { return sold ? SoundEvents.WANDERING_TRADER_YES : SoundEvents.WANDERING_TRADER_NO; }
    @Override public SoundEvent getNotifyTradeSound() { return SoundEvents.WANDERING_TRADER_YES; }
    @Override protected SoundEvent getAmbientSound() { return isTrading() ? SoundEvents.WANDERING_TRADER_TRADE : SoundEvents.WANDERING_TRADER_AMBIENT; }
    @Nullable @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.WANDERING_TRADER_HURT; }
    @Nullable @Override protected SoundEvent getDeathSound() { return SoundEvents.WANDERING_TRADER_DEATH; }
}
