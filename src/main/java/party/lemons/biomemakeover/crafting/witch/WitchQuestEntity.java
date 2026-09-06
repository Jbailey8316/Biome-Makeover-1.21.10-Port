package party.lemons.biomemakeover.crafting.witch;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import party.lemons.biomemakeover.crafting.witch.menu.WitchMenu;
import party.lemons.biomemakeover.network.WitchQuestsPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public interface WitchQuestEntity {
    default void configureQuestGoals() {}
    default void tickQuestState(net.minecraft.server.level.ServerLevel level) {}
    default void saveQuestData(net.minecraft.world.level.storage.ValueOutput output) {}
    default void loadQuestData(net.minecraft.world.level.storage.ValueInput input) {}
    default void offerAntidote() {}
    void setCurrentCustomer(Player player);
    Player getCurrentCustomer();
    WitchQuestList getQuests();
    void setQuestsFromServer(WitchQuestList quests);
    SoundEvent getYesSound();
    boolean canInteract(Player player);
    Level getWitchLevel();
    default boolean hasCustomer() { return getCurrentCustomer() != null; }
    default void sendQuests(ServerPlayer player, Component title) {
        player.openMenu(new SimpleMenuProvider((id, inv, ignored) -> new WitchMenu(id, inv, this), title));
        if (this instanceof net.minecraft.world.entity.Entity entity) {
            party.lemons.biomemakeover.BiomeMakeover.LOGGER.info("[BM_WITCH_GUI_TRACE] SERVER_OPEN witchId={} witchUuid={} menuId={} quests={}", entity.getId(), entity.getUUID(), player.containerMenu.containerId, getQuests().size());
        }
        ServerPlayNetworking.send(player, new WitchQuestsPayload(player.containerMenu.containerId, getQuests().toTag()));
    }
    default void sendQuestUpdate(ServerPlayer player) {
        if (this instanceof net.minecraft.world.entity.Entity entity) {
            party.lemons.biomemakeover.BiomeMakeover.LOGGER.info("[BM_WITCH_GUI_TRACE] SERVER_UPDATE witchId={} witchUuid={} menuId={} quests={}", entity.getId(), entity.getUUID(), player.containerMenu.containerId, getQuests().size());
        }
        ServerPlayNetworking.send(player, new WitchQuestsPayload(player.containerMenu.containerId, getQuests().toTag()));
    }
}
