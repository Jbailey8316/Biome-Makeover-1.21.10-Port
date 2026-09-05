package party.lemons.biomemakeover.init;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.entity.OwlEntity;
import party.lemons.biomemakeover.entity.GlowfishEntity;
import party.lemons.biomemakeover.entity.ScuttlerEntity;
import party.lemons.biomemakeover.entity.CowboyEntity;
import party.lemons.biomemakeover.entity.TumbleweedEntity;
import party.lemons.biomemakeover.entity.DecayedEntity;
import party.lemons.biomemakeover.entity.DragonflyEntity;
import party.lemons.biomemakeover.entity.LightningBugEntity;
import party.lemons.biomemakeover.entity.LightningBottleEntity;
import party.lemons.biomemakeover.entity.RootlingEntity;
import party.lemons.biomemakeover.entity.MothEntity;
import party.lemons.biomemakeover.entity.MushroomTraderEntity;
import party.lemons.biomemakeover.entity.GhostEntity;
import party.lemons.biomemakeover.entity.AdjudicatorEntity;

public final class BMEntities {
    public static final TagKey<Item> SCUTTLER_FOOD = TagKey.create(Registries.ITEM, BiomeMakeover.id("scuttler_food"));
    public static final TagKey<EntityType<?>> OWL_TARGETS = TagKey.create(Registries.ENTITY_TYPE, BiomeMakeover.id("owl_targets"));
    public static final TagKey<DamageType> TUMBLEWEED_IMMUNE_DAMAGE = TagKey.create(Registries.DAMAGE_TYPE,
        BiomeMakeover.id("tumbleweed_immune_to"));
    public static final TagKey<DamageType> GHOST_IMMUNE_DAMAGE = TagKey.create(Registries.DAMAGE_TYPE,
        BiomeMakeover.id("ghost_immune_to"));
    public static final EntityType<OwlEntity> OWL = registerEntity(
        "owl",
        EntityType.Builder.<OwlEntity>of(OwlEntity::new, MobCategory.CREATURE)
            .sized(0.7F, 0.8F)
            .clientTrackingRange(12)
    );

    public static final Item OWL_SPAWN_EGG = registerSpawnEgg("owl_spawn_egg", OWL);
    public static final EntityType<GlowfishEntity> GLOWFISH = registerEntity(
        "glowfish", EntityType.Builder.<GlowfishEntity>of(GlowfishEntity::new, MobCategory.WATER_AMBIENT)
            .sized(0.7F, 0.4F).clientTrackingRange(4));
    public static final Item GLOWFISH_SPAWN_EGG = registerSpawnEgg("glowfish_spawn_egg", GLOWFISH);
    public static final EntityType<ScuttlerEntity> SCUTTLER = registerEntity("scuttler",
        EntityType.Builder.<ScuttlerEntity>of(ScuttlerEntity::new,MobCategory.CREATURE).sized(.8F,.6F).clientTrackingRange(12));
    public static final Item SCUTTLER_SPAWN_EGG = registerSpawnEgg("scuttler_spawn_egg",SCUTTLER);
    public static final EntityType<CowboyEntity> COWBOY = registerEntity("cowboy",
        EntityType.Builder.<CowboyEntity>of(CowboyEntity::new,MobCategory.MONSTER)
            .sized(.6F,1.95F)
            .passengerAttachments(2.0F)
            .ridingOffset(-0.6F)
            .canSpawnFarFromPlayer()
            .clientTrackingRange(12));
    public static final Item COWBOY_SPAWN_EGG = registerSpawnEgg("cowboy_spawn_egg",COWBOY);
    public static final EntityType<TumbleweedEntity> TUMBLEWEED = registerEntity("tumbleweed",
        EntityType.Builder.<TumbleweedEntity>of(TumbleweedEntity::new,MobCategory.MISC).sized(.7F,.7F).clientTrackingRange(12));
    public static final EntityType<DecayedEntity> DECAYED = registerEntity("decayed",
        EntityType.Builder.<DecayedEntity>of(DecayedEntity::new, MobCategory.MONSTER).sized(.6F, 1.95F).clientTrackingRange(8));
    public static final Item DECAYED_SPAWN_EGG = registerSpawnEgg("decayed_spawn_egg", DECAYED);
    public static final EntityType<DragonflyEntity> DRAGONFLY = registerEntity("dragonfly",
        EntityType.Builder.<DragonflyEntity>of(DragonflyEntity::new, MobCategory.AMBIENT).sized(.8F, .6F).clientTrackingRange(12));
    public static final Item DRAGONFLY_SPAWN_EGG = registerSpawnEgg("dragonfly_spawn_egg", DRAGONFLY);
    public static final EntityType<LightningBugEntity> LIGHTNING_BUG = registerEntity("lightning_bug",
        EntityType.Builder.<LightningBugEntity>of(LightningBugEntity::new, MobCategory.AMBIENT).sized(.4F, .4F).clientTrackingRange(12));
    public static final EntityType<LightningBugEntity> LIGHTNING_BUG_ALTERNATE = registerEntity("lightning_bug_alternate",
        EntityType.Builder.<LightningBugEntity>of((type, level) -> new LightningBugEntity(type, level, true), MobCategory.AMBIENT).sized(.4F, .4F).clientTrackingRange(12));
    public static final Item LIGHTNING_BUG_SPAWN_EGG = registerSpawnEgg("lightning_bug_spawn_egg", LIGHTNING_BUG_ALTERNATE);
    public static final EntityType<LightningBottleEntity> LIGHTNING_BOTTLE = registerEntity("lightning_bottle",
            EntityType.Builder.<LightningBottleEntity>of(LightningBottleEntity::new, MobCategory.MISC).sized(.25F, .25F).clientTrackingRange(10).updateInterval(4));
    public static final EntityType<RootlingEntity> ROOTLING = registerEntity("rootling",
        EntityType.Builder.<RootlingEntity>of(RootlingEntity::new, MobCategory.CREATURE).sized(.6F,1.1F).clientTrackingRange(12));
    public static final EntityType<MothEntity> MOTH = registerEntity("moth",
        EntityType.Builder.<MothEntity>of(MothEntity::new, MobCategory.MONSTER).sized(.8F,1.2F).clientTrackingRange(12));
    public static final Item ROOTLING_SPAWN_EGG = registerSpawnEgg("rootling_spawn_egg", ROOTLING);
    public static final Item MOTH_SPAWN_EGG = registerSpawnEgg("moth_spawn_egg", MOTH);
    public static final EntityType<MushroomTraderEntity> MUSHROOM_TRADER = registerEntity("mushroom_trader",
        EntityType.Builder.<MushroomTraderEntity>of(MushroomTraderEntity::new, MobCategory.AMBIENT)
            .sized(0.6F, 1.95F).clientTrackingRange(12));
    public static final Item MUSHROOM_TRADER_SPAWN_EGG = registerSpawnEgg("mushroom_trader_spawn_egg", MUSHROOM_TRADER);
    public static final EntityType<GhostEntity> GHOST = registerEntity("ghost",
        EntityType.Builder.<GhostEntity>of(GhostEntity::new, MobCategory.MONSTER).sized(.6F,1.95F).canSpawnFarFromPlayer().clientTrackingRange(12));
    public static final Item GHOST_SPAWN_EGG = registerSpawnEgg("ghost_spawn_egg", GHOST);
    public static final EntityType<AdjudicatorEntity> ADJUDICATOR = registerEntity("adjudicator",
        EntityType.Builder.<AdjudicatorEntity>of(AdjudicatorEntity::new, MobCategory.MONSTER)
            .sized(.6F, 1.95F).fireImmune().noSummon().clientTrackingRange(12));

    private BMEntities() {
    }

    private static <T extends net.minecraft.world.entity.Entity> EntityType<T> registerEntity(
        String name,
        EntityType.Builder<T> builder
    ) {
        ResourceLocation id = BiomeMakeover.id(name);
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id);
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    private static Item registerSpawnEgg(String name, EntityType<?> type) {
        ResourceLocation id = BiomeMakeover.id(name);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        Item item = new SpawnEggItem(new Item.Properties().setId(key).spawnEgg(type));
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    public static void initialize() {
        FabricDefaultAttributeRegistry.register(OWL, OwlEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(GLOWFISH, GlowfishEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(SCUTTLER, ScuttlerEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(COWBOY, net.minecraft.world.entity.monster.Pillager.createAttributes());
        FabricDefaultAttributeRegistry.register(DECAYED, DecayedEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(DRAGONFLY, DragonflyEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(LIGHTNING_BUG, LightningBugEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(LIGHTNING_BUG_ALTERNATE, LightningBugEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ROOTLING, RootlingEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(MOTH, MothEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(MUSHROOM_TRADER, MushroomTraderEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(GHOST, GhostEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ADJUDICATOR, AdjudicatorEntity.createAttributes());
        SpawnPlacements.register(
            OWL,
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING,
            OwlEntity::checkOwlSpawnRules
        );
        SpawnPlacements.register(GLOWFISH, SpawnPlacementTypes.IN_WATER,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, net.minecraft.world.entity.animal.WaterAnimal::checkSurfaceWaterAnimalSpawnRules);
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS)
            .register(entries -> { entries.accept(OWL_SPAWN_EGG); entries.accept(GLOWFISH_SPAWN_EGG); });
        SpawnPlacements.register(SCUTTLER,SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,ScuttlerEntity::checkSpawnRules);
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(entries->{entries.accept(SCUTTLER_SPAWN_EGG);entries.accept(COWBOY_SPAWN_EGG);});
        SpawnPlacements.register(DECAYED, SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, DecayedEntity::checkSpawnRules);
        SpawnPlacements.register(DRAGONFLY, SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, DragonflyEntity::checkSpawnRules);
        SpawnPlacements.register(LIGHTNING_BUG, SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, LightningBugEntity::checkLightningBugSpawn);
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(entries -> {
            entries.accept(DECAYED_SPAWN_EGG); entries.accept(DRAGONFLY_SPAWN_EGG); entries.accept(LIGHTNING_BUG_SPAWN_EGG);
            entries.accept(ROOTLING_SPAWN_EGG); entries.accept(MOTH_SPAWN_EGG);
            entries.accept(MUSHROOM_TRADER_SPAWN_EGG);
            entries.accept(GHOST_SPAWN_EGG);
        });
        SpawnPlacements.register(ROOTLING, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (type, level, reason, pos, random) -> level.getBlockState(pos.below()).is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK)
                && level.getRawBrightness(pos, 0) > 2);
        SpawnPlacements.register(MOTH, SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING,
            MothEntity::checkSpawnRules);
    }
}
