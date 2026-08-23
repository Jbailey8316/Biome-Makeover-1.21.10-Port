package party.lemons.biomemakeover.init;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.PlayerTrigger;

public final class BMAdvancements {
    public static final PlayerTrigger GLOWFISH_SAVE = CriteriaTriggers.register(
        "biomemakeover:glowfish_bucket_save", new PlayerTrigger());
    private BMAdvancements() {}
    public static void initialize() {}
}
