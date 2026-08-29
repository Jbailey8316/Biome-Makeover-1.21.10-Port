package party.lemons.biomemakeover.init;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.PlayerTrigger;

public final class BMAdvancements {
    public static final PlayerTrigger GLOWFISH_SAVE = CriteriaTriggers.register(
        "biomemakeover:glowfish_bucket_save", new PlayerTrigger());
    public static final PlayerTrigger PEAT_COMPOST = CriteriaTriggers.register(
        "biomemakeover:peat_compost", new PlayerTrigger());
    private BMAdvancements() {}
    public static void initialize() {}
}
