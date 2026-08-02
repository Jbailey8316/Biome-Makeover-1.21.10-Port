package party.lemons.biomemakeover.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.client.model.BMModelLayers;
import party.lemons.biomemakeover.client.model.OwlModel;
import party.lemons.biomemakeover.client.render.state.OwlRenderState;
import party.lemons.biomemakeover.entity.OwlEntity;

public final class OwlRenderer extends MobRenderer<OwlEntity, OwlRenderState, OwlModel> {
    private static final ResourceLocation TEXTURE = BiomeMakeover.id("textures/entity/owl.png");
    private static final ResourceLocation EYES_TEXTURE = BiomeMakeover.id("textures/entity/owl_eyes.png");

    public OwlRenderer(EntityRendererProvider.Context context) {
        super(context, new OwlModel(context.bakeLayer(BMModelLayers.OWL)), 0.45F);
        this.addLayer(new EyesLayer<OwlRenderState, OwlModel>(this) {
            @Override
            public RenderType renderType() {
                return RenderType.eyes(EYES_TEXTURE);
            }
        });
    }

    @Override
    public OwlRenderState createRenderState() {
        return new OwlRenderState();
    }

    @Override
    public void extractRenderState(OwlEntity entity, OwlRenderState state, float tickProgress) {
        super.extractRenderState(entity, state, tickProgress);
        if (entity.isOwlSleeping()) {
            // Sleeping owls do not follow camera/player movement.
            state.yRot = entity.getYRot();
            state.xRot = 0.0F;
        }
        state.flying = entity.isOwlFlying() && !entity.isOwlSleeping() && !entity.isOwlSitting();
        state.sitting = entity.isOwlSitting() || entity.isOwlSleeping();
        state.sleeping = entity.isOwlSleeping();
        state.baby = entity.isBaby();
        state.lean = entity.isOwlSleeping() ? 0.0F : entity.getLeanAmount(tickProgress) / 7.0F;
        var velocity = entity.isOwlSleeping() ? net.minecraft.world.phys.Vec3.ZERO : entity.getDeltaMovement();
        state.horizontalSpeed = (float)Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
        state.verticalSpeed = (float)velocity.y;
        int blinkClock = (entity.tickCount + entity.getId() * 17) % 120;
        state.blinking = blinkClock < 4 || (blinkClock >= 9 && blinkClock < 12 && entity.getId() % 5 == 0);
        long time = entity.level().getDayTime() % 24000L;
        state.nightEyes = time >= 12500L && time <= 23500L;
    }

    @Override
    public ResourceLocation getTextureLocation(OwlRenderState state) {
        return TEXTURE;
    }
}


// TODO: Shoulder perch rendering hook.
// Attached owl render layer will use the same owl model at reduced scale.
