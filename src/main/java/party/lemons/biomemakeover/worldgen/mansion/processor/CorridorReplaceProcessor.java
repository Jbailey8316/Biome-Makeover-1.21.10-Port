package party.lemons.biomemakeover.worldgen.mansion.processor;

import net.minecraft.util.RandomSource;
import party.lemons.biomemakeover.worldgen.mansion.RoomType;
import party.lemons.biomemakeover.worldgen.mansion.room.MansionRoom;
import party.lemons.biomemakeover.worldgen.mansion.MansionGrid;

public class CorridorReplaceProcessor extends FloorRoomReplaceProcessor
{
    @Override
    public boolean isValid(RandomSource random, int floor, MansionGrid<MansionRoom> grid, MansionRoom currentRoom)
    {
        return currentRoom.getRoomType() == RoomType.CORRIDOR && random.nextInt(4) == 0;
    }

    @Override
    public MansionRoom getReplaceRoom(MansionRoom currentRoom)
    {
        currentRoom.setRoomType(RoomType.ROOM);
        return currentRoom;
    }
}