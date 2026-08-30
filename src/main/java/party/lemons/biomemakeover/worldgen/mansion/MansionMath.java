package party.lemons.biomemakeover.worldgen.mansion;

import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Bounded replacement for the released Taniwha directional helpers. */
public final class MansionMath {
    public static final List<Direction> HORIZONTALS = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
    private MansionMath() {}
    public static Direction randomHorizontal(RandomSource random) { return HORIZONTALS.get(random.nextInt(4)); }
    public static List<Direction> randomOrderedHorizontals() {
        List<Direction> result = new ArrayList<>(HORIZONTALS);
        Collections.shuffle(result);
        return result;
    }
}
