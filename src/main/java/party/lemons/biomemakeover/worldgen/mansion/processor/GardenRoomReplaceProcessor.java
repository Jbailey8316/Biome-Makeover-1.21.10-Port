package party.lemons.biomemakeover.worldgen.mansion.processor;

import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import party.lemons.biomemakeover.worldgen.mansion.LayoutType;
import party.lemons.biomemakeover.worldgen.mansion.RoomType;
import party.lemons.biomemakeover.worldgen.mansion.room.MansionRoom;
import party.lemons.biomemakeover.worldgen.mansion.room.NonRoofedMansionRoom;
import party.lemons.biomemakeover.worldgen.mansion.MansionMath;
import party.lemons.biomemakeover.worldgen.mansion.MansionGrid;

public class GardenRoomReplaceProcessor extends FloorRoomReplaceProcessor
{

    @Override
    public boolean isValid(RandomSource random, int floor, MansionGrid<MansionRoom> layout, MansionRoom currentRoom)
    {
        if(floor == 0 && random.nextInt(3) == 0)
        {
            boolean isSurrounded = true;
            for(Direction dir : MansionMath.HORIZONTALS)
            {
                if(!layout.contains(currentRoom.getPosition().relative(dir)))
                {
                    isSurrounded = false;
                    break;
                }
            }
            return isSurrounded;
        }

        return false;
    }

    @Override
    public MansionRoom getReplaceRoom(MansionRoom currentRoom)
    {
        NonRoofedMansionRoom gardenRoom = new NonRoofedMansionRoom(currentRoom.getPosition(), RoomType.GARDEN);
        gardenRoom.setLayoutType(LayoutType.REQUIRED);
        return gardenRoom;
    }
}