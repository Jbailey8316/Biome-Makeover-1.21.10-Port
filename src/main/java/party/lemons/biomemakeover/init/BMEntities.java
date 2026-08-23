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
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.entity.OwlEntity;
import party.lemons.biomemakeover.entity.GlowfishEntity;

public final class BMEntities {
    public static final EntityType<OwlEntity> OWL = registerEntity(
        "owl",
        EntityType.Builder.<OwlEntity>of(OwlEntity::new, MobCategory.CREATURE)
            .sized(0.7F, 1.4F)
            .clientTrackingRange(8)
    );

    public static final Item OWL_SPAWN_EGG = registerSpawnEgg("owl_spawn_egg", OWL);
    public static final EntityType<GlowfishEntity> GLOWFISH = registerEntity(
        "glowfish", EntityType.Builder.<GlowfishEntity>of(GlowfishEntity::new, MobCategory.WATER_AMBIENT)
            .sized(0.7F, 0.4F).clientTrackingRange(4));
    public static final Item GLOWFISH_SPAWN_EGG = registerSpawnEgg("glowfish_spawn_egg", GLOWFISH);

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
    }
}
