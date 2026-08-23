package party.lemons.biomemakeover.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import party.lemons.biomemakeover.BiomeMakeover;

public final class BMSounds {
    public static final SoundEvent OWL_IDLE = register("owl_idle");
    public static final SoundEvent OWL_HURT = register("owl_hurt");
    public static final SoundEvent OWL_DEATH = register("owl_death");
    public static final SoundEvent OWL_HOOT = register("owl_hoot");
    public static final SoundEvent OWL_CONTACT = register("owl_contact");
    public static final SoundEvent OWL_ALERT = register("owl_alert");
    public static final SoundEvent OWL_BABY = register("owl_baby");
    public static final SoundEvent OWL_TAKEOFF = register("owl_takeoff");
    public static final SoundEvent SCUTTLER_RATTLE = register("scuttler_rattle");
    public static final SoundEvent SCUTTLER_STEP = register("scuttler_step");
    public static final SoundEvent SCUTTLER_HURT = register("scuttler_hurt");
    public static final SoundEvent SCUTTLER_DEATH = register("scuttler_death");
    public static final SoundEvent TUMBLEWEED_TUMBLE = register("tumbleweed_tumble");
    public static final SoundEvent TUMBLEWEED_BREAK = register("tumbleweed_break");

    private BMSounds() {}

    private static SoundEvent register(String name) {
        ResourceLocation id = BiomeMakeover.id(name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void initialize() {
        // Class loading performs registration.
    }
}
