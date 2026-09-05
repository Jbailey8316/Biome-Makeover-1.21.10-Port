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
    private boolean forcedGardenUsed;

    @Override
    public boolean isValid(RandomSource random, int floor, MansionGrid<MansionRoom> layout, MansionRoom currentRoom)
    {
        if(floor == 0 && forceRavagerGarden() && !forcedGardenUsed)
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
            if (isSurrounded) forcedGardenUsed = true;
            return isSurrounded;
        }

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

    private static boolean forceRavagerGarden()
    {
        return Boolean.getBoolean("bm.mansion.trace") && Boolean.getBoolean("bm.mansion.forceRavagerGarden");
    }

    @Override
    public MansionRoom getReplaceRoom(MansionRoom currentRoom)
    {
        NonRoofedMansionRoom gardenRoom = new NonRoofedMansionRoom(currentRoom.getPosition(), RoomType.GARDEN);
        gardenRoom.setLayoutType(LayoutType.REQUIRED);
        return gardenRoom;
    }
}
