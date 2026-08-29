package party.lemons.biomemakeover.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.block.entity.AltarBlockEntity;
import party.lemons.biomemakeover.screen.AltarMenu;

/** Released Altar layout with progress glyphs and the vanilla animated book. */
public final class AltarScreen extends AbstractContainerScreen<AltarMenu> {
    private static final ResourceLocation TEXTURE = BiomeMakeover.id("textures/gui/altar.png");
    private static final ResourceLocation BOOK_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/enchanting_table_book.png");
    private static final int[] GLYPH_PROGRESS = {0, 6, 11, 16, 20, 24, 29, 35, 42, 49, 54, 54, 54};
    private final RandomSource random = RandomSource.create();
    private BookModel bookModel;
    private ItemStack stack = ItemStack.EMPTY;
    private float nextPageAngle;
    private float pageAngle;
    private float approximatePageAngle;
    private float pageRotationSpeed;
    private float nextPageTurningSpeed;
    private float pageTurningSpeed;

    public AltarScreen(AltarMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        bookModel = new BookModel(minecraft.getEntityModels().bakeLayer(ModelLayers.BOOK));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);

        int progress = menu.getProgress();
        if (progress > 0) {
            int fill = (int) ((progress / (float) AltarBlockEntity.MAX_TIME) * 29.0F);
            if (fill > 0) graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 99, y + 55 - fill, 189, 29 - fill, 9, fill, 256, 256);
            int glyph = GLYPH_PROGRESS[progress / 2 % GLYPH_PROGRESS.length];
            if (glyph > 0) graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 68, y + 69 - glyph, 177, 53 - glyph, 12, glyph, 256, 256);
        }

        float open = Mth.lerp(partialTick, pageTurningSpeed, nextPageTurningSpeed);
        float flip = Mth.lerp(partialTick, pageAngle, nextPageAngle);
        graphics.submitBookModelRenderState(bookModel, BOOK_TEXTURE, 40.0F, open, flip,
            x + 14, y + 14, x + 52, y + 45);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        ItemStack current = menu.getSlot(0).getItem();
        if (!ItemStack.matches(current, stack)) {
            stack = current.copy();
            do approximatePageAngle += random.nextInt(4) - random.nextInt(4);
            while (nextPageAngle <= approximatePageAngle + 1.0F && nextPageAngle >= approximatePageAngle - 1.0F);
        }
        pageAngle = nextPageAngle;
        pageTurningSpeed = nextPageTurningSpeed;
        nextPageTurningSpeed += menu.getProgress() > 0 ? 0.2F : -0.2F;
        nextPageTurningSpeed = Mth.clamp(nextPageTurningSpeed, 0, 1);
        float rotation = Mth.clamp((approximatePageAngle - nextPageAngle) * 0.4F, -0.2F, 0.2F);
        pageRotationSpeed += (rotation - pageRotationSpeed) * 0.9F;
        nextPageAngle += pageRotationSpeed;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
