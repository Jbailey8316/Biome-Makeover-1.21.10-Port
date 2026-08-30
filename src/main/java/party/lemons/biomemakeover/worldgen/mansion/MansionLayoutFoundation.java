package party.lemons.biomemakeover.worldgen.mansion;

import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;

import java.util.List;

/** Foundation constants and coordinate rules shared by later Mansion stages. */
public final class MansionLayoutFoundation {
    public static final int CELL_XZ = 12;
    public static final int CELL_Y = 7;
    public static final int GROUND_FLOOR = 0;
    public static final List<Direction> HORIZONTAL_ORDER = List.of(Direction.NORTH, Direction.EAST,
        Direction.SOUTH, Direction.WEST);

    private MansionLayoutFoundation() {}

    public static Direction randomHorizontal(RandomSource random) {
        return HORIZONTAL_ORDER.get(random.nextInt(HORIZONTAL_ORDER.size()));
    }

    public static int worldCoordinate(int origin, int cell, boolean vertical) {
        return origin + cell * (vertical ? CELL_Y : CELL_XZ);
    }
}
