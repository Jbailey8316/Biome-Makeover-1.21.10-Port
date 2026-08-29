package party.lemons.biomemakeover.init;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.world.entity.EquipmentSlot;

public final class BMAdvancements {
    public static final PlayerTrigger GLOWFISH_SAVE = CriteriaTriggers.register(
        "biomemakeover:glowfish_bucket_save", new PlayerTrigger());
    public static final PlayerTrigger PEAT_COMPOST = CriteriaTriggers.register(
        "biomemakeover:peat_compost", new PlayerTrigger());
    public static final PlayerTrigger WEAR_WITCH_HAT = CriteriaTriggers.register(
        "biomemakeover:wear_witch_hat", new PlayerTrigger());
    private BMAdvancements() {}
    public static void initialize() {
        // Narrow server-authoritative replacement for final Taniwha's wear_armor criterion.
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (var player : server.getPlayerList().getPlayers()) {
                if (player.getItemBySlot(EquipmentSlot.HEAD).is(BMItems.WITCH_HATS)) {
                    WEAR_WITCH_HAT.trigger(player);
                }
            }
        });
    }
}
