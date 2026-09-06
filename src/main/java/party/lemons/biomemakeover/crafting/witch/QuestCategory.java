package party.lemons.biomemakeover.crafting.witch;

import java.util.ArrayList;
import java.util.List;

public final class QuestCategory {
    private final int weight;
    private final List<QuestItem> requestedItems = new ArrayList<>();
    public QuestCategory(int weight) { this.weight = weight; }
    public int getWeight() { return weight; }
    public List<QuestItem> getRequestedItemPool() { return requestedItems; }
    public void addItem(QuestItem item) { requestedItems.add(item); }
    public boolean isEmpty() { return requestedItems.isEmpty(); }
}
