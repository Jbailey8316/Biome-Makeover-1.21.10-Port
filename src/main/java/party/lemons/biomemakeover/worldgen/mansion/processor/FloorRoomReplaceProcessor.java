package party.lemons.biomemakeover.worldgen.mansion.processor;

import net.minecraft.util.RandomSource;
import party.lemons.biomemakeover.worldgen.mansion.room.MansionRoom;
import party.lemons.biomemakeover.worldgen.mansion.MansionGrid;


public abstract class FloorRoomReplaceProcessor
{
    public abstract boolean isValid(RandomSource random, int floor, MansionGrid<MansionRoom> grid, MansionRoom currentRoom);

    public abstract MansionRoom getReplaceRoom(MansionRoom currentRoom);
}