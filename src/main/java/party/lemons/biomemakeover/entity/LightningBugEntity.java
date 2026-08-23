package party.lemons.biomemakeover.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class LightningBugEntity extends DragonflyEntity {
    private boolean alternate;
    public LightningBugEntity(EntityType<? extends LightningBugEntity> type, Level level) { super(type, level); }
    public LightningBugEntity(EntityType<? extends LightningBugEntity> type, Level level, boolean alternate) { this(type, level); this.alternate = alternate; }
    public static boolean checkLightningBugSpawn(EntityType<LightningBugEntity> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return level.getRawBrightness(pos, 0) < 8 && checkFlySpawn(level, pos);
    }
    public boolean isAlternate() { return alternate; }
    @Override protected void addAdditionalSaveData(ValueOutput output) { super.addAdditionalSaveData(output); output.putBoolean("Alternate", alternate); }
    @Override protected void readAdditionalSaveData(ValueInput input) { super.readAdditionalSaveData(input); alternate = input.getBooleanOr("Alternate", alternate); }
}
