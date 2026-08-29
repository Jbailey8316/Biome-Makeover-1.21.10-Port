package party.lemons.biomemakeover.init;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.sounds.SoundEvents;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.item.GlowfishBucketItem;
import party.lemons.biomemakeover.item.LightningBottleItem;
import party.lemons.biomemakeover.item.StuntPowderItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.Rarity;
import net.minecraft.tags.TagKey;

public final class BMItems {
    public static final TagKey<Item> CURSE_FUEL = TagKey.create(Registries.ITEM, BiomeMakeover.id("curse_fuel"));
    public static final TagKey<Item> WITCH_HATS = TagKey.create(Registries.ITEM, BiomeMakeover.id("witch_hats"));
    public static final ResourceKey<JukeboxSong> BUTTON_MUSHROOMS_SONG = ResourceKey.create(
        Registries.JUKEBOX_SONG, BiomeMakeover.id("button_mushrooms"));
    public static final ResourceKey<JukeboxSong> SWAMP_JIVES_SONG = ResourceKey.create(
        Registries.JUKEBOX_SONG, BiomeMakeover.id("swamp_jives"));
    public static final Item LEAF_LITTER = register("leaf_litter");
    public static final Item OWL_EGG = register("owl_egg");
    private static final FoodProperties GLOWFISH_FOOD = new FoodProperties(1, 0.1F, true);
    private static final FoodProperties COOKED_GLOWFISH_FOOD = new FoodProperties(5, 0.6F, true);
    private static final Consumable GLOWFISH_CONSUMABLE = glowingFood(200, 0.5F);
    private static final Consumable STEW_CONSUMABLE = glowingFood(1200, 1.0F);
    public static final Item GLOWSHROOM_STEW = register("glowshroom_stew", p -> new Item(p.stacksTo(1)
        .usingConvertsTo(Items.BOWL).food(COOKED_GLOWFISH_FOOD, STEW_CONSUMABLE)));
    public static final Item GLOWFISH = register("glowfish", p -> new Item(p.food(GLOWFISH_FOOD, GLOWFISH_CONSUMABLE)));
    public static final Item COOKED_GLOWFISH = register("cooked_glowfish", p -> new Item(p.food(COOKED_GLOWFISH_FOOD, GLOWFISH_CONSUMABLE)));
    public static final Item GLOWFISH_BUCKET = register("glowfish_bucket", p -> new GlowfishBucketItem(
        BMEntities.GLOWFISH, Fluids.WATER, SoundEvents.BUCKET_EMPTY_FISH, p.stacksTo(1)));
    public static final Item BLIGHTED_BALSA_SIGN = register("blighted_balsa_sign", p -> new SignItem(
        BMBlocks.BLIGHTED_BALSA_SIGN, BMBlocks.BLIGHTED_BALSA_WALL_SIGN, p.stacksTo(16)));
    public static final Item BLIGHTED_BALSA_HANGING_SIGN = register("blighted_balsa_hanging_sign", p -> new HangingSignItem(
        BMBlocks.BLIGHTED_BALSA_HANGING_SIGN, BMBlocks.BLIGHTED_BALSA_WALL_HANGING_SIGN, p.stacksTo(16)));
    public static final Item WILLOW_SIGN = register("willow_sign", p -> new SignItem(
        BMBlocks.WILLOW_SIGN, BMBlocks.WILLOW_WALL_SIGN, p.stacksTo(16)));
    public static final Item WILLOW_HANGING_SIGN = register("willow_hanging_sign", p -> new HangingSignItem(
        BMBlocks.WILLOW_HANGING_SIGN, BMBlocks.WILLOW_WALL_HANGING_SIGN, p.stacksTo(16)));
    public static final Item SWAMP_CYPRESS_SIGN = register("swamp_cypress_sign", p -> new SignItem(
        BMBlocks.SWAMP_CYPRESS_SIGN, BMBlocks.SWAMP_CYPRESS_WALL_SIGN, p.stacksTo(16)));
    public static final Item SWAMP_CYPRESS_HANGING_SIGN = register("swamp_cypress_hanging_sign", p -> new HangingSignItem(
        BMBlocks.SWAMP_CYPRESS_HANGING_SIGN, BMBlocks.SWAMP_CYPRESS_WALL_HANGING_SIGN, p.stacksTo(16)));
    public static final Item ANCIENT_OAK_SIGN = register("ancient_oak_sign", p -> new SignItem(
        BMBlocks.ANCIENT_OAK_SIGN, BMBlocks.ANCIENT_OAK_WALL_SIGN, p.stacksTo(16)));
    public static final Item ANCIENT_OAK_HANGING_SIGN = register("ancient_oak_hanging_sign", p -> new HangingSignItem(
        BMBlocks.ANCIENT_OAK_HANGING_SIGN, BMBlocks.ANCIENT_OAK_WALL_HANGING_SIGN, p.stacksTo(16)));
    public static final Item SCUTTLER_TAIL = register("scuttler_tail");
    public static final Item PINK_BUD = register("pink_bud");
    public static final Item MAGENTA_BUD = register("magenta_bud");
    public static final Item COWBOY_HAT = register("cowboy_hat", p -> new Item(p
        .durability(500)
        .attributes(ItemAttributeModifiers.builder().add(Attributes.ARMOR,
            new AttributeModifier(ResourceLocation.withDefaultNamespace("armor.helmet"),2,
                AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.HEAD).build())
        .repairable(Items.LEATHER)
            .component(DataComponents.EQUIPPABLE,Equippable.builder(EquipmentSlot.HEAD)
            .setEquipSound(SoundEvents.ARMOR_EQUIP_LEATHER).build())));
    public static final Item WITCH_HAT = register("witch_hat", p -> new Item(p
        .durability(500)
        .attributes(ItemAttributeModifiers.builder().add(Attributes.ARMOR,
            new AttributeModifier(ResourceLocation.withDefaultNamespace("armor.helmet"),2,
                AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.HEAD).build())
        .repairable(Items.LEATHER)
        .component(DataComponents.EQUIPPABLE,Equippable.builder(EquipmentSlot.HEAD)
            .setEquipSound(SoundEvents.ARMOR_EQUIP_LEATHER).build())));
    public static final Item CRACKED_BRICK = register("cracked_brick");
    public static final Item LIGHTNING_BOTTLE = register("lightning_bottle", LightningBottleItem::new);
    public static final Item ILLUNITE_SHARD = register("illunite_shard");
    public static final Item BLUE_BUD = register("blue_bud");
    public static final Item BROWN_BUD = register("brown_bud");
    public static final Item CYAN_BUD = register("cyan_bud");
    public static final Item GRAY_BUD = register("gray_bud");
    public static final Item LIGHT_BLUE_BUD = register("light_blue_bud");
    public static final Item PURPLE_BUD = register("purple_bud");
    public static final Item ROOTLING_SEEDS = register("rootling_seeds", p -> new BlockItem(BMBlocks.ROOTLING_CROP, p));
    public static final Item BULBUS_ROOT = register("bulbus_root", p -> new Item(p.food(new FoodProperties(2, 0.6F, false))));
    public static final Item ROASTED_BULBUS_ROOT = register("roasted_bulbus_root", p -> new Item(p.food(new FoodProperties(5, 0.8F, false))));
    public static final Item MOTH_SCALES = register("moth_scales");
    public static final Item STUNT_POWDER = register("stunt_powder", StuntPowderItem::new);
    public static final Item BUTTON_MUSHROOMS_MUSIC_DISK = register("button_mushrooms_music_disk",
        p -> new Item(p.stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(BUTTON_MUSHROOMS_SONG)));
    public static final Item SWAMP_JIVES_MUSIC_DISK = register("swamp_jives_music_disk",
        p -> new Item(p.stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(SWAMP_JIVES_SONG)));

    private BMItems() {}

    private static Item register(String name) {
        return register(name, Item::new);
    }

    private static Item register(String name, java.util.function.Function<Item.Properties, Item> factory) {
        ResourceLocation id = BiomeMakeover.id(name);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        return Registry.register(BuiltInRegistries.ITEM, key,
            factory.apply(new Item.Properties().setId(key)));
    }

    private static Consumable glowingFood(int duration, float probability) {
        return Consumables.defaultFood()
            .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, duration), probability))
            .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.GLOWING, duration), probability))
            .build();
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS).register(entries -> {
            entries.accept(LEAF_LITTER);
            entries.accept(GLOWSHROOM_STEW); entries.accept(GLOWFISH); entries.accept(COOKED_GLOWFISH); entries.accept(GLOWFISH_BUCKET);
            entries.accept(SCUTTLER_TAIL); entries.accept(PINK_BUD); entries.accept(MAGENTA_BUD); entries.accept(COWBOY_HAT); entries.accept(WITCH_HAT); entries.accept(CRACKED_BRICK); entries.accept(LIGHTNING_BOTTLE); entries.accept(ILLUNITE_SHARD);
            entries.accept(ANCIENT_OAK_SIGN); entries.accept(ANCIENT_OAK_HANGING_SIGN);
            entries.accept(BLUE_BUD); entries.accept(BROWN_BUD); entries.accept(CYAN_BUD); entries.accept(GRAY_BUD);
            entries.accept(LIGHT_BLUE_BUD); entries.accept(PURPLE_BUD); entries.accept(ROOTLING_SEEDS);
            entries.accept(BULBUS_ROOT); entries.accept(ROASTED_BULBUS_ROOT); entries.accept(MOTH_SCALES); entries.accept(STUNT_POWDER);
            entries.accept(BUTTON_MUSHROOMS_MUSIC_DISK); entries.accept(SWAMP_JIVES_MUSIC_DISK);
        });
        LightningBottleItem.registerDispenserBehavior(LIGHTNING_BOTTLE);
    }
}
