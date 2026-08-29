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
    public static final SoundEvent DRAGONFLY_LOOP = register("entity.dragonfly.loop");
    public static final SoundEvent DRAGONFLY_HURT = register("entity.dragonfly.hurt");
    public static final SoundEvent DRAGONFLY_DEATH = register("entity.dragonfly.death");
    public static final SoundEvent DECAYED_SWIM = register("entity.decayed.swim");
    public static final SoundEvent DECAYED_STEP = register("entity.decayed.step");
    public static final SoundEvent DECAYED_HURT_WATER = register("entity.decayed.hurt_water");
    public static final SoundEvent DECAYED_HURT = register("entity.decayed.hurt");
    public static final SoundEvent DECAYED_DEATH_WATER = register("entity.decayed.death_water");
    public static final SoundEvent DECAYED_DEATH = register("entity.decayed.death");
    public static final SoundEvent DECAYED_AMBIENT_WATER = register("entity.decayed.ambient_water");
    public static final SoundEvent DECAYED_AMBIENT = register("entity.decayed.ambient");
    public static final SoundEvent LIGHTNING_BOTTLE_THROW = register("entity.lightning_bottle.throw");
    public static final SoundEvent LIGHTNING_BOTTLE_THUNDER = register("entity.lightning_bottle.thunder");
    public static final SoundEvent ILLUNITE_BREAK = register("illunite_break");
    public static final SoundEvent ILLUNITE_HIT = register("illunite_hit");
    public static final SoundEvent ILLUNITE_PLACE = register("illunite_place");
    public static final SoundEvent ILLUNITE_STEP = register("illunite_step");
    public static final SoundEvent ROOTLING_HURT = register("rootling_hurt");
    public static final SoundEvent ROOTLING_DEATH = register("rootling_death");
    public static final SoundEvent ROOTLING_AFRAID = register("rootling_afraid");
    public static final SoundEvent ROOTLING_IDLE = register("rootling_idle");
    public static final SoundEvent MOTH_IDLE = register("moth_idle");
    public static final SoundEvent MOTH_FLAP = register("moth_flap");
    public static final SoundEvent MOTH_DEATH = register("moth_death");
    public static final SoundEvent MOTH_BITE = register("moth_bite");
    public static final SoundEvent MOTH_HURT = register("moth_hurt");
    public static final SoundEvent ALTAR_CURSING = register("altar_cursing");
    public static final SoundEvent BUTTON_MUSHROOMS = register("button_mushrooms");
    public static final SoundEvent SWAMP_JIVES = register("swamp_jives");

    private BMSounds() {}

    private static SoundEvent register(String name) {
        ResourceLocation id = BiomeMakeover.id(name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void initialize() {
        // Class loading performs registration.
    }
}
