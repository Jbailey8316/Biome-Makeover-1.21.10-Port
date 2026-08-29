package party.lemons.biomemakeover.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import party.lemons.biomemakeover.block.AltarBlock;
import party.lemons.biomemakeover.block.entity.AltarBlockEntity;
import party.lemons.biomemakeover.init.BMSounds;

/** One positional 300-tick Altar audio instance, stopped on invalidation. */
public final class AltarCursingSound extends AbstractTickableSoundInstance {
    private final AltarBlockEntity altar;
    private int age;

    public AltarCursingSound(AltarBlockEntity altar, RandomSource random) {
        super(BMSounds.ALTAR_CURSING, SoundSource.BLOCKS, random);
        this.altar = altar;
        x = altar.getBlockPos().getX();
        y = altar.getBlockPos().getY();
        z = altar.getBlockPos().getZ();
    }

    @Override
    public void tick() {
        age++;
        if (altar.getLevel() == null
            || !(altar.getLevel().getBlockEntity(altar.getBlockPos()) instanceof AltarBlockEntity)
            || (age > 2 && age < 280 && !altar.getLevel().getBlockState(altar.getBlockPos()).getValue(AltarBlock.ACTIVE))) {
            stop();
        }
    }
}
