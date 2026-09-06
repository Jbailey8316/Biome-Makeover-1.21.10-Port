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
        ServerPlayNetworking.send(player, new WitchQuestsPayload(player.containerMenu.containerId, getQuests().toTag()));
    }
    default void sendQuestUpdate(ServerPlayer player) {
        ServerPlayNetworking.send(player, new WitchQuestsPayload(player.containerMenu.containerId, getQuests().toTag()));
    }
}
