package party.lemons.biomemakeover.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.RisingParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

/** Released animated translucent Poltergeist particle. */
public final class PoltergeistParticle extends RisingParticle {
    private final SpriteSet sprites;

    private PoltergeistParticle(ClientLevel level, double x, double y, double z,
                                double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z, vx, vy, vz, sprites.get(level.random));
        this.sprites = sprites;
        scale(1.5F);
        setAlpha(1.0F);
        setSpriteFromAge(sprites);
    }

    @Override public void tick() {
        super.tick();
        if (!removed) setSpriteFromAge(sprites);
    }

    @Override protected Layer getLayer() { return Layer.TRANSLUCENT; }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public Provider(SpriteSet sprites) { this.sprites = sprites; }
        @Override public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                                  double vx, double vy, double vz, RandomSource random) {
            return new PoltergeistParticle(level, x, y, z, vx, vy, vz, sprites);
        }
    }
}
