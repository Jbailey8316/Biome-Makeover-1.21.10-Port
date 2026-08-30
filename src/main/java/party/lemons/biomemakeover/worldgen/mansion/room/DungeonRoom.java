package party.lemons.biomemakeover.worldgen.mansion.room;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import party.lemons.biomemakeover.worldgen.mansion.MansionFeature;
import party.lemons.biomemakeover.worldgen.mansion.MansionTemplateType;
import party.lemons.biomemakeover.worldgen.mansion.MansionTemplates;
import party.lemons.biomemakeover.worldgen.mansion.RoomType;
import party.lemons.biomemakeover.worldgen.mansion.MansionGrid;

public class DungeonRoom extends MansionRoom
{
    public DungeonRoom(BlockPos position, RoomType type)
    {
        super(position, type);
    }

    @Override
    public String getInnerWall(MansionTemplates templates, RandomSource random)
    {
        return MansionTemplateType.DUNGEON_DOOR.getRandomTemplate(templates, random).toString();
    }

    @Override
    public String getFlatWall(MansionTemplates templates, RandomSource random)
    {
        return MansionTemplateType.DUNGEON_WAll.getRandomTemplate(templates, random).toString();
    }

    @Override
    public String getOuterWall(MansionTemplates templates, Direction dir, MansionGrid<MansionRoom> roomGrid, RandomSource random)
    {
        return MansionTemplateType.DUNGEON_WAll.getRandomTemplate(templates, random).toString();
    }

    @Override
    public boolean hasGroundModifications() {
        return false;
    }
}