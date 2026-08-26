package party.lemons.biomemakeover.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.RisingParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

/** Released six-frame translucent Moth Blossom particle, translated to the 1.21.10 particle API. */
public final class BlossomParticle extends RisingParticle {
    private final SpriteSet sprites;

    private BlossomParticle(ClientLevel level, double x, double y, double z,
                            double velocityX, double velocityY, double velocityZ, SpriteSet sprites) {
        super(level, x, y, z, velocityX, velocityY, velocityZ, sprites.get(level.random));
        this.sprites = sprites;
        scale(1.0F);
        setAlpha(1.0F);
        setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (!removed) setSpriteFromAge(sprites);
    }

    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double velocityX, double velocityY, double velocityZ, RandomSource random) {
            return new BlossomParticle(level, x, y, z, velocityX, velocityY, velocityZ, sprites);
        }
    }
}
