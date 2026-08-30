package party.lemons.biomemakeover.worldgen.mansion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;

import java.util.List;

/** Codec-owned layout details; marker actions are deliberately deferred. */
public record MansionDetails(Loot loot, Mobs mobs) {
    public static final Codec<MansionDetails> CODEC = RecordCodecBuilder.create(i -> i.group(
        Loot.CODEC.fieldOf("loot").forGetter(MansionDetails::loot),
        Mobs.CODEC.fieldOf("mobs").forGetter(MansionDetails::mobs)
    ).apply(i, MansionDetails::new));

    public record Loot(net.minecraft.resources.ResourceLocation arrow,
                       net.minecraft.resources.ResourceLocation dungeonJunk,
                       net.minecraft.resources.ResourceLocation dungeonStandard,
                       net.minecraft.resources.ResourceLocation dungeonGood,
                       net.minecraft.resources.ResourceLocation junk,
                       net.minecraft.resources.ResourceLocation standard,
                       net.minecraft.resources.ResourceLocation good) {
        public static final Codec<Loot> CODEC = RecordCodecBuilder.create(i -> i.group(
            net.minecraft.resources.ResourceLocation.CODEC.fieldOf("arrow").forGetter(Loot::arrow),
            net.minecraft.resources.ResourceLocation.CODEC.fieldOf("dungeon_junk").forGetter(Loot::dungeonJunk),
            net.minecraft.resources.ResourceLocation.CODEC.fieldOf("dungeon_standard").forGetter(Loot::dungeonStandard),
            net.minecraft.resources.ResourceLocation.CODEC.fieldOf("dungeon_good").forGetter(Loot::dungeonGood),
            net.minecraft.resources.ResourceLocation.CODEC.fieldOf("junk").forGetter(Loot::junk),
            net.minecraft.resources.ResourceLocation.CODEC.fieldOf("standard").forGetter(Loot::standard),
            net.minecraft.resources.ResourceLocation.CODEC.fieldOf("good").forGetter(Loot::good)
        ).apply(i, Loot::new));
    }

    public record Mobs(List<EntityType<?>> enemies, List<EntityType<?>> rangedEnemies,
                       List<EntityType<?>> golemEnemies, List<EntityType<?>> ravagers,
                       List<EntityType<?>> cow, List<EntityType<?>> allays) {
        private static Codec<List<EntityType<?>>> entities() { return BuiltInRegistries.ENTITY_TYPE.byNameCodec().listOf(); }
        public static final Codec<Mobs> CODEC = RecordCodecBuilder.create(i -> i.group(
            entities().fieldOf("enemies").forGetter(Mobs::enemies),
            entities().fieldOf("ranged_enemies").forGetter(Mobs::rangedEnemies),
            entities().fieldOf("golem_enemies").forGetter(Mobs::golemEnemies),
            entities().fieldOf("ravagers").forGetter(Mobs::ravagers),
            entities().fieldOf("cow").forGetter(Mobs::cow),
            entities().fieldOf("allays").forGetter(Mobs::allays)
        ).apply(i, Mobs::new));
    }
}
