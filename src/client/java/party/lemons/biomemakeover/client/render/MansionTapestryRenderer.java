package party.lemons.biomemakeover.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import party.lemons.biomemakeover.client.model.BMModelLayers;
import party.lemons.biomemakeover.client.model.TapestryModel;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.worldgen.mansion.MansionStandingTapestryBlock;
import party.lemons.biomemakeover.worldgen.mansion.MansionTapestryBlock;
import party.lemons.biomemakeover.worldgen.mansion.MansionWallTapestryBlock;
import party.lemons.biomemakeover.block.entity.TapestryBlockEntity;

/** Released flag-style renderer for the Mansion tapestry family. */
public final class MansionTapestryRenderer implements BlockEntityRenderer<TapestryBlockEntity, MansionTapestryRenderer.State> {
    private static final boolean TRACE = Boolean.getBoolean("bm.mansion.trace");
    private static final java.util.Set<String> TRACE_KEYS = new java.util.HashSet<>();
    private final TapestryModel model;

    public MansionTapestryRenderer(BlockEntityRendererProvider.Context context) {
        model = new TapestryModel(context.bakeLayer(BMModelLayers.TAPESTRY));
    }

    @Override public State createRenderState() { return new State(); }

    @Override public void extractRenderState(TapestryBlockEntity entity, State state, float partialTick,
                                             Vec3 camera, ModelFeatureRenderer.CrumblingOverlay breaking) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTick, camera, breaking);
        BlockState blockState = entity.getBlockState();
        state.block = blockState.getBlock() instanceof MansionTapestryBlock tapestry ? tapestry : null;
        state.wall = blockState.getBlock() instanceof MansionWallTapestryBlock;
        state.angle = state.wall ? -blockState.getValue(MansionWallTapestryBlock.FACING).toYRot()
            : -blockState.getValue(MansionStandingTapestryBlock.ROTATION) * 22.5F;
        state.modelState.poleVisible = !state.wall;
        state.modelState.flagXRot = (-0.0125F + 0.01F * Mth.cos(Mth.TWO_PI *
            (Math.floorMod(entity.getBlockPos().getX() * 7L + entity.getBlockPos().getY() * 9L + entity.getBlockPos().getZ() * 13L +
                (entity.getLevel() == null ? 0L : entity.getLevel().getGameTime()), 100L) + partialTick) / 100.0F)) * Mth.PI;
        state.modelState.flagY = -32.0F;
        if (TRACE) trace(entity, blockState, state);
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
        model.setupAnim(state.modelState);
        int light = state.lightCoords;
        var renderType = RenderType.entitySolid(state.block.tapestryTexture());
        output.submitModelPart(model.bar(), pose, renderType, light, OverlayTexture.NO_OVERLAY, null);
        if (state.modelState.poleVisible) {
            output.submitModelPart(model.pole(), pose, renderType, light, OverlayTexture.NO_OVERLAY, null);
        }
        output.submitModelPart(model.flag(), pose, renderType, light, OverlayTexture.NO_OVERLAY, null);
        pose.popPose();
    }

    public static final class State extends BlockEntityRenderState {
        MansionTapestryBlock block;
        boolean wall;
        float angle;
        final TapestryModel.State modelState = new TapestryModel.State();
    }

    private static void trace(TapestryBlockEntity entity, BlockState state, State renderState) {
        String key = entity.getBlockPos() + ":" + state.getBlock();
        synchronized (TRACE_KEYS) {
            if (TRACE_KEYS.size() >= 16 || !TRACE_KEYS.add(key)) return;
        }
        var facing = state.hasProperty(MansionWallTapestryBlock.FACING) ? state.getValue(MansionWallTapestryBlock.FACING).toString() : "NONE";
        var support = entity.getBlockPos().relative(renderState.wall ? state.getValue(MansionWallTapestryBlock.FACING).getOpposite() : net.minecraft.core.Direction.DOWN);
        var level = entity.getLevel();
        BiomeMakeover.LOGGER.info("[BM_TAPESTRY_RENDER] form={} variant={} blockPos={} blockState={} facing={} standingRotation={} texture={} translate={} rotationAxis=Y rotationDegrees={} supportPos={} supportBlock={}",
            renderState.wall ? "wall" : "standing", state.getBlock(), entity.getBlockPos(), state, facing,
            state.hasProperty(MansionStandingTapestryBlock.ROTATION) ? state.getValue(MansionStandingTapestryBlock.ROTATION) : "NONE",
            renderState.block.tapestryTexture(), renderState.wall ? "0.5,-0.16666667,0.5;0,-0.3125,-0.4375" : "0.5,0.5,0.5",
            renderState.angle, support, level == null ? "UNKNOWN" : level.getBlockState(support));
        var texture = renderState.block.tapestryTexture();
        int textureSize = -1;
        boolean present = false;
        try {
            var resource = Minecraft.getInstance().getResourceManager().getResource(texture);
            if (resource.isPresent()) {
                present = true;
                try (var input = resource.get().open()) { textureSize = input.available(); }
            }
        } catch (Exception ignored) {
            // Diagnostic only: resource lookup must never affect rendering.
        }
        BiomeMakeover.LOGGER.info("[BM_TAPESTRY_TEXTURE_BIND] variant={} textureResource={} renderType={} vertexConsumerSource={} modelTextureWidth={} modelTextureHeight={} flagUvSummary={} textureResourcePresent={} textureResourceByteSize={}",
            state.getBlock(), texture, "entitySolid(" + texture + ")", "SubmitNodeCollector.submitModelPart", 64, 64,
            "flag=(0,0,20x35),(0,36,4x5),(10,36,4x5),(20,36,4x5);pole=(44,0,2x42);bar=(0,42,20x2)", present, textureSize);
    }
}
