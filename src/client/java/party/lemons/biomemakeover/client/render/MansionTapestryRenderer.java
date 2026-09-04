package party.lemons.biomemakeover.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.BannerModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import party.lemons.biomemakeover.client.model.BMModelLayers;
import party.lemons.biomemakeover.worldgen.mansion.MansionStandingTapestryBlock;
import party.lemons.biomemakeover.worldgen.mansion.MansionTapestryBlock;
import party.lemons.biomemakeover.worldgen.mansion.MansionWallTapestryBlock;
import party.lemons.biomemakeover.block.entity.TapestryBlockEntity;

/** Released flag-style renderer for the Mansion tapestry family. */
public final class MansionTapestryRenderer implements BlockEntityRenderer<TapestryBlockEntity, MansionTapestryRenderer.State> {
    private final BannerModel standing;
    private final BannerModel wall;

    public MansionTapestryRenderer(BlockEntityRendererProvider.Context context) {
        standing = new BannerModel(context.bakeLayer(BMModelLayers.TAPESTRY_STANDING));
        wall = new BannerModel(context.bakeLayer(BMModelLayers.TAPESTRY_WALL));
    }

    @Override public State createRenderState() { return new State(); }

    @Override public void extractRenderState(TapestryBlockEntity entity, State state, float partialTick,
                                             Vec3 camera, ModelFeatureRenderer.CrumblingOverlay breaking) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTick, camera, breaking);
        BlockState blockState = entity.getBlockState();
        state.block = blockState.getBlock() instanceof MansionTapestryBlock tapestry ? tapestry : null;
        state.pos = entity.getBlockPos();
        state.wall = blockState.getBlock() instanceof MansionWallTapestryBlock;
        state.angle = state.wall ? -blockState.getValue(MansionWallTapestryBlock.FACING).toYRot()
            : -blockState.getValue(MansionStandingTapestryBlock.ROTATION) * 22.5F;
    }

    @Override public void submit(State state, PoseStack pose, SubmitNodeCollector output, CameraRenderState camera) {
        if (state.block == null) return;
        pose.pushPose();
        if (state.wall) {
            pose.translate(0.5F, -0.16666667F, 0.5F);
            pose.mulPose(Axis.YP.rotationDegrees(state.angle));
            pose.translate(0.0F, -0.3125F, -0.4375F);
        } else {
            pose.translate(0.5F, 0.5F, 0.5F);
            pose.mulPose(Axis.YP.rotationDegrees(state.angle));
        }
        pose.scale(0.6666667F, -0.6666667F, -0.6666667F);
        BannerModel model = state.wall ? wall : standing;
        int light = state.lightCoords;
        output.submitModel(model, Unit.INSTANCE, pose, RenderType.entitySolid(state.block.tapestryTexture()), light,
            OverlayTexture.NO_OVERLAY, -1, null, -1, state.breakProgress);
        pose.popPose();
    }

    public static final class State extends BlockEntityRenderState {
        MansionTapestryBlock block;
        BlockPos pos;
        boolean wall;
        float angle;
    }
}
