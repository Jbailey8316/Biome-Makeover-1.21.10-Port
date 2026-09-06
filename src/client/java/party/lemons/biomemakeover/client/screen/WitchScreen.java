package party.lemons.biomemakeover.client.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import party.lemons.biomemakeover.crafting.witch.WitchQuest;
import party.lemons.biomemakeover.crafting.witch.menu.WitchMenu;
import party.lemons.biomemakeover.network.CompleteWitchQuestPayload;
import party.lemons.biomemakeover.BiomeMakeover;

@Environment(EnvType.CLIENT)
public final class WitchScreen extends AbstractContainerScreen<WitchMenu> {
    public WitchScreen(WitchMenu menu, Inventory inventory, Component title) { super(menu, inventory, title); imageWidth = 176; imageHeight = 182; }
    @Override protected void init() {
        super.init();
        for (int i = 0; i < menu.getQuests().size(); i++) {
            final int index = i;
            addRenderableWidget(Button.builder(Component.translatable("witch.quest", i + 1), button -> {
                ClientPlayNetworking.send(new CompleteWitchQuestPayload(index));
            }).bounds(leftPos + 8, topPos + 16 + i * 24, 100, 20).build());
        }
    }
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BiomeMakeover.id("textures/gui/witch.png"), leftPos, topPos, 0, 0, imageWidth, imageHeight, 512, 256);
        for (int i = 0; i < menu.getQuests().size(); i++) {
            WitchQuest quest = menu.getQuests().get(i);
            int x = leftPos + 112, y = topPos + 18 + i * 24;
            for (var stack : quest.getRequiredItems()) { graphics.renderItem(stack, x, y); x += 18; }
        }
    }
}
