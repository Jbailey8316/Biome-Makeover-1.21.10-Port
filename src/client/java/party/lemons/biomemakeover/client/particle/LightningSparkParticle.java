package party.lemons.biomemakeover.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

public final class LightningSparkParticle extends SingleQuadParticle {
    private final SpriteSet sprites;
    private LightningSparkParticle(ClientLevel level,double x,double y,double z,double vx,double vy,double vz,SpriteSet sprites){
        super(level,x,y,z,.5-level.random.nextDouble(),vy,.5-level.random.nextDouble(),sprites.get(level.random));this.sprites=sprites;setSpriteFromAge(sprites);
        yd*=.2D;if(xd==0&&zd==0){xd*=.1D;zd*=.1D;}quadSize*=.75F;lifetime=(int)(8D/(Math.random()*.8D+.2D));hasPhysics=false;
    }
    @Override public ParticleRenderType getGroup(){return ParticleRenderType.SINGLE_QUADS;}
    @Override protected Layer getLayer(){return Layer.TRANSLUCENT;}
    @Override public void tick(){xo=x;yo=y;zo=z;if(age++>=lifetime){remove();return;}setSpriteFromAge(sprites);yd+=.004D;move(xd,yd,zd);if(y==yo){xd*=1.1D;zd*=1.1D;}xd*=.96D;yd*=.96D;zd*=.96D;if(onGround){xd*=.7D;zd*=.7D;}}
    public static final class Provider implements ParticleProvider<SimpleParticleType>{private final SpriteSet sprites;public Provider(SpriteSet sprites){this.sprites=sprites;}@Override public Particle createParticle(SimpleParticleType type,ClientLevel level,double x,double y,double z,double vx,double vy,double vz,net.minecraft.util.RandomSource random){var particle=new LightningSparkParticle(level,x,y,z,vx,vy,vz,sprites);particle.setAlpha(1);return particle;}}
}
