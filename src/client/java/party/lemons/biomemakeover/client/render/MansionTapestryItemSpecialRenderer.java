package party.lemons.biomemakeover.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.client.model.BMModelLayers;
import party.lemons.biomemakeover.client.model.TapestryModel;

/** Modern special-item bridge for the released block-entity tapestry model. */
public final class MansionTapestryItemSpecialRenderer implements NoDataSpecialModelRenderer {
    private final TapestryModel model;
    private final ResourceLocation texture;

    private MansionTapestryItemSpecialRenderer(TapestryModel model, String variant) {
        this.model = model;
        this.texture = BiomeMakeover.id("textures/tapestry/" + variant + "_tapestry.png");
    }

    @Override public void submit(ItemDisplayContext context, PoseStack pose, SubmitNodeCollector output,
                                  int light, int overlay, boolean glint, int seed) {
        pose.pushPose();
        pose.translate(0.5F, 0.5F, 0.5F);
        pose.scale(0.6666667F, -0.6666667F, -0.6666667F);
        TapestryModel.State state = new TapestryModel.State();
        state.poleVisible = true;
        state.flagXRot = 0.0F;
        state.flagY = -32.0F;
        model.setupAnim(state);
        RenderType renderType = RenderType.entitySolid(texture);
        output.submitModelPart(model.bar(), pose, renderType, light, overlay, null);
        output.submitModelPart(model.pole(), pose, renderType, light, overlay, null);
        output.submitModelPart(model.flag(), pose, renderType, light, overlay, null);
        pose.popPose();
    }

    @Override public void getExtents(java.util.Set<org.joml.Vector3f> extents) {
        extents.add(new org.joml.Vector3f(-0.5F, -1.0F, -0.5F));
        extents.add(new org.joml.Vector3f(0.5F, 1.0F, 0.5F));
    }

    public static final class Unbaked implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("variant").forGetter(Unbaked::variant)
        ).apply(instance, Unbaked::new));

        private final String variant;

        public Unbaked(String variant) { this.variant = variant; }
        public String variant() { return variant; }

        @Override public SpecialModelRenderer<?> bake(SpecialModelRenderer.BakingContext context) {
            return new MansionTapestryItemSpecialRenderer(
                new TapestryModel(context.entityModelSet().bakeLayer(BMModelLayers.TAPESTRY)), variant);
        }

        @Override public MapCodec<Unbaked> type() { return MAP_CODEC; }
    }
}
