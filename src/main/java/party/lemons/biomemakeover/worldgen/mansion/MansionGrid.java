package party.lemons.biomemakeover.worldgen.mansion;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Small local equivalent of the released Taniwha Grid helper. */
public final class MansionGrid<T> {
    private final Map<BlockPos, T> cells = new HashMap<>();

    public boolean contains(BlockPos pos) { return cells.containsKey(pos); }
    public T get(BlockPos pos) { return cells.get(pos); }
    public void set(BlockPos pos, T value) { cells.put(pos, value); }
    public T remove(BlockPos pos) { return cells.remove(pos); }
    public Collection<T> entries() { return Collections.unmodifiableCollection(cells.values()); }
    public int size() { return cells.size(); }
    public BlockPos neighbor(BlockPos pos, Direction direction) { return pos.relative(direction); }
}
