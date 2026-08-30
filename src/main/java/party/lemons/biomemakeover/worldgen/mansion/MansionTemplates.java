package party.lemons.biomemakeover.worldgen.mansion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** The released Mansion template catalog. NBT assets are introduced in 11A.2. */
public record MansionTemplates(Base base, TowerRoof towerRoof, Dungeon dungeon, Other other) {
    public static final Codec<MansionTemplates> CODEC = RecordCodecBuilder.create(i -> i.group(
        Base.CODEC.fieldOf("base").forGetter(MansionTemplates::base),
        TowerRoof.CODEC.fieldOf("tower_roof").forGetter(MansionTemplates::towerRoof),
        Dungeon.CODEC.fieldOf("dungeon").forGetter(MansionTemplates::dungeon),
        Other.CODEC.fieldOf("other").forGetter(MansionTemplates::other)
    ).apply(i, MansionTemplates::new));

    private static Codec<List<ResourceLocation>> list() { return ResourceLocation.CODEC.listOf(); }

    public record Base(List<ResourceLocation> corridorStraight, List<ResourceLocation> corridorCorner,
                       List<ResourceLocation> corridorT, List<ResourceLocation> corridorCross,
                       List<ResourceLocation> rooms, List<ResourceLocation> roomsBig,
                       List<ResourceLocation> stairUp, List<ResourceLocation> stairDown,
                       List<ResourceLocation> innerWall, List<ResourceLocation> flatWall,
                       List<ResourceLocation> outerWallBase, List<ResourceLocation> outerWall,
                       List<ResourceLocation> outerWindow, List<ResourceLocation> garden,
                       List<ResourceLocation> entrance) {
        public static final Codec<Base> CODEC = RecordCodecBuilder.create(i -> i.group(
            list().fieldOf("corridor_straight").forGetter(Base::corridorStraight),
            list().fieldOf("corridor_corner").forGetter(Base::corridorCorner),
            list().fieldOf("corridor_t").forGetter(Base::corridorT),
            list().fieldOf("corridor_cross").forGetter(Base::corridorCross),
            list().fieldOf("rooms").forGetter(Base::rooms),
            list().fieldOf("rooms_big").forGetter(Base::roomsBig),
            list().fieldOf("stair_up").forGetter(Base::stairUp),
            list().fieldOf("stair_down").forGetter(Base::stairDown),
            list().fieldOf("inner_wall").forGetter(Base::innerWall),
            list().fieldOf("flat_wall").forGetter(Base::flatWall),
            list().fieldOf("outer_wall_base").forGetter(Base::outerWallBase),
            list().fieldOf("outer_wall").forGetter(Base::outerWall),
            list().fieldOf("outer_window").forGetter(Base::outerWindow),
            list().fieldOf("garden").forGetter(Base::garden),
            list().fieldOf("entrance").forGetter(Base::entrance)
        ).apply(i, Base::new));
    }

    public record TowerRoof(List<ResourceLocation> towerBase, List<ResourceLocation> towerMid,
                            List<ResourceLocation> towerTop, List<ResourceLocation> roof0,
                            List<ResourceLocation> roof1, List<ResourceLocation> roof2,
                            List<ResourceLocation> roof2Straight, List<ResourceLocation> roof3,
                            List<ResourceLocation> roof4, List<ResourceLocation> roofSplit) {
        public static final Codec<TowerRoof> CODEC = RecordCodecBuilder.create(i -> i.group(
            list().fieldOf("tower_base").forGetter(TowerRoof::towerBase),
            list().fieldOf("tower_mid").forGetter(TowerRoof::towerMid),
            list().fieldOf("tower_top").forGetter(TowerRoof::towerTop),
            list().fieldOf("roof_0").forGetter(TowerRoof::roof0),
            list().fieldOf("roof_1").forGetter(TowerRoof::roof1),
            list().fieldOf("roof_2").forGetter(TowerRoof::roof2),
            list().fieldOf("roof_2_straight").forGetter(TowerRoof::roof2Straight),
            list().fieldOf("roof_3").forGetter(TowerRoof::roof3),
            list().fieldOf("roof_4").forGetter(TowerRoof::roof4),
            list().fieldOf("roof_split").forGetter(TowerRoof::roofSplit)
        ).apply(i, TowerRoof::new));
    }

    public record Dungeon(List<ResourceLocation> door, List<ResourceLocation> wall,
                          List<ResourceLocation> room, List<ResourceLocation> stairBottom,
                          List<ResourceLocation> stairMid, List<ResourceLocation> stairTop,
                          List<ResourceLocation> bossRoom) {
        public static final Codec<Dungeon> CODEC = RecordCodecBuilder.create(i -> i.group(
            list().fieldOf("dungeon_door").forGetter(Dungeon::door),
            list().fieldOf("dungeon_wall").forGetter(Dungeon::wall),
            list().fieldOf("dungeon_room").forGetter(Dungeon::room),
            list().fieldOf("dungeon_stair_bottom").forGetter(Dungeon::stairBottom),
            list().fieldOf("dungeon_stair_mid").forGetter(Dungeon::stairMid),
            list().fieldOf("dungeon_stair_top").forGetter(Dungeon::stairTop),
            list().fieldOf("boss_room").forGetter(Dungeon::bossRoom)
        ).apply(i, Dungeon::new));
    }

    public record Other(List<ResourceLocation> cornerFillers, List<ResourceLocation> empties) {
        public static final Codec<Other> CODEC = RecordCodecBuilder.create(i -> i.group(
            list().fieldOf("corner_fillers").forGetter(Other::cornerFillers),
            list().fieldOf("empties").forGetter(Other::empties)
        ).apply(i, Other::new));
    }
}
