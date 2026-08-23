package party.lemons.biomemakeover.init;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import party.lemons.biomemakeover.BiomeMakeover;

public final class BMParticles {
    public static final SimpleParticleType LIGHTNING_SPARK=Registry.register(BuiltInRegistries.PARTICLE_TYPE,BiomeMakeover.id("lightning_spark"),FabricParticleTypes.simple());
    private BMParticles(){}
    public static void initialize(){}
}
