package party.lemons.biomemakeover.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import party.lemons.biomemakeover.block.entity.AltarBlockEntity;

/** Modern render-state form of the released floating enchanting-table book. */
public final class AltarRenderer implements BlockEntityRenderer<AltarBlockEntity, AltarRenderer.State> {
    private static final Material BOOK_LOCATION = Sheets.BLOCK_ENTITIES_MAPPER.defaultNamespaceApply("enchanting_table_book");
    private final MaterialSet materials;
    private final BookModel bookModel;

    public AltarRenderer(BlockEntityRendererProvider.Context context) {
        materials = context.materials();
        bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
    }

    @Override public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(AltarBlockEntity altar, State state, float partialTick, Vec3 camera,
                                   ModelFeatureRenderer.CrumblingOverlay breaking) {
        BlockEntityRenderer.super.extractRenderState(altar, state, partialTick, camera, breaking);
        state.time = altar.ticks + partialTick;
        state.flip = Mth.lerp(partialTick, altar.pageAngle, altar.nextPageAngle);
        state.open = Mth.lerp(partialTick, altar.pageTurningSpeed, altar.nextPageTurningSpeed);
        float rotation = altar.currentAngle - altar.lastAngle;
        while (rotation >= Math.PI) rotation -= Mth.TWO_PI;
        while (rotation < -Math.PI) rotation += Mth.TWO_PI;
        state.yRot = altar.lastAngle + rotation * partialTick;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector output, CameraRenderState camera) {
        pose.pushPose();
        pose.translate(0.5F, 0.75F, 0.5F);
        pose.translate(0, 0.1F + Mth.sin(state.time * 0.1F) * 0.01F, 0);
        pose.mulPose(Axis.YP.rotation(-state.yRot));
        pose.mulPose(Axis.ZP.rotationDegrees(80));
        float left = Mth.frac(state.flip + 0.25F) * 1.6F - 0.3F;
        float right = Mth.frac(state.flip + 0.75F) * 1.6F - 0.3F;
        BookModel.State bookState = new BookModel.State(state.time, Mth.clamp(left, 0, 1), Mth.clamp(right, 0, 1), state.open);
        output.submitModel(bookModel, bookState, pose, BOOK_LOCATION.renderType(RenderType::entitySolid), state.lightCoords,
            OverlayTexture.NO_OVERLAY, -1, materials.get(BOOK_LOCATION), 0, state.breakProgress);
        pose.popPose();
    }

    public static final class State extends BlockEntityRenderState {
        float time;
        float flip;
        float open;
        float yRot;
    }
}
