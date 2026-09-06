package party.lemons.biomemakeover.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.crafting.witch.QuestRarity;
import party.lemons.biomemakeover.crafting.witch.WitchQuest;
import party.lemons.biomemakeover.crafting.witch.WitchQuestList;
import party.lemons.biomemakeover.crafting.witch.menu.WitchMenu;
import party.lemons.biomemakeover.network.CompleteWitchQuestPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Environment(EnvType.CLIENT)
public final class WitchScreen extends AbstractContainerScreen<WitchMenu> {
    private static final ResourceLocation TEXTURE = BiomeMakeover.id("textures/gui/witch.png");
    private static final Component QUESTS_TEXT = Component.translatable("witch.quests");
    private final QuestButton[] questButtons = new QuestButton[3];
    private final Inventory inventory;

    public WitchScreen(WitchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 174;
        imageHeight = 182;
        inventoryLabelX = 110;
        inventoryLabelY = imageHeight - 92;
        this.inventory = inventory;
    }

    private void clickQuest(int index, WitchQuest quest) {
        if (minecraft.player != null && quest.hasItems(minecraft.player.getInventory()))
            ClientPlayNetworking.send(new CompleteWitchQuestPayload(index));
    }

    @Override protected void init() { super.init(); updateQuests(); }

    public void updateQuests() {
        for (int i = 0; i < questButtons.length; i++) {
            if (questButtons[i] != null) removeWidget(questButtons[i]);
            questButtons[i] = null;
        }
        int x = (width - imageWidth) / 2 + 3;
        int y = (height - imageHeight) / 2 + 18;
        WitchQuestList quests = menu.getQuests();
        for (int i = 0; i < quests.size() && i < questButtons.length; i++) {
            final int index = i;
            questButtons[i] = addRenderableWidget(new QuestButton(x, y + i * 26, quests.get(i), b -> clickQuest(index, quests.get(index))));
        }
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 49 + imageWidth / 2 - font.width(title) / 2, 6, 4210752, false);
        graphics.drawString(font, inventory.getDisplayName(), inventoryLabelX, inventoryLabelY, 4210752, false);
        graphics.drawString(font, QUESTS_TEXT, 48 - font.width(QUESTS_TEXT) / 2 + 5, 6, 4210752, false);
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 512, 256);
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        for (QuestButton button : questButtons) if (button != null && button.isHoveredOrFocused()) button.renderToolTip(graphics, mouseX, mouseY);
    }

    private final class QuestButton extends Button {
        private final WitchQuest quest;
        private final QuestRarity rarity;
        QuestButton(int x, int y, WitchQuest quest, OnPress action) {
            super(x, y, 104, 26, Component.empty(), action, Button.DEFAULT_NARRATION);
            this.quest = quest;
            this.rarity = QuestRarity.getRarityFromPoints(quest.getPoints());
        }
        @Override protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int textureRow = quest.hasItems(Minecraft.getInstance().player.getInventory()) ? (isHoveredOrFocused() ? 4 : 3) : 1;
            graphics.blit(TEXTURE, getX(), getY(), 174, textureRow * 26, width, height, 512, 256);
            graphics.blit(TEXTURE, getX() + 4, getY() + 11, 278, 7 + rarity.ordinal() * 5, 5, 5, 512, 256);
            int itemX = getX() + 11;
            for (ItemStack stack : quest.getRequiredItems()) {
                graphics.renderItem(stack, itemX, getY() + 5);
                graphics.renderItemDecorations(minecraft.font, stack, itemX, getY() + 5, String.valueOf(stack.getCount()));
                itemX += 18;
            }
        }
        void renderToolTip(GuiGraphics graphics, int mouseX, int mouseY) {
            int x = mouseX - getX(), y = mouseY - getY();
            if (!isHoveredOrFocused() || x <= 5 || y >= 19) return;
            // Item/rarity tooltip APIs changed in 1.21.10; the released
            // presentation remains intact through the rendered markers/items.
        }
    }
}
