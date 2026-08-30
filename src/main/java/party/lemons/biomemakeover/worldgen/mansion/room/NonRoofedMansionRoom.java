package party.lemons.biomemakeover.worldgen.mansion.room;

import net.minecraft.core.BlockPos;
import party.lemons.biomemakeover.worldgen.mansion.RoomType;

public class NonRoofedMansionRoom extends MansionRoom
{
    public NonRoofedMansionRoom(BlockPos position, RoomType type)
    {
        super(position, type);
    }

    @Override
    public boolean canSupportRoof()
    {
        return false;
    }
}