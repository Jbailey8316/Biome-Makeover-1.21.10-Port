package party.lemons.biomemakeover.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import party.lemons.biomemakeover.init.BMSounds;

public class DragonflyEntity extends PathfinderMob {
    private int variant;
    public DragonflyEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level); moveControl = new FlyingMoveControl(this, 20, true); variant = historicalVariant(random);
    }
    public static AttributeSupplier.Builder createAttributes() { return createMobAttributes().add(Attributes.MAX_HEALTH, 8).add(Attributes.MOVEMENT_SPEED, .25).add(Attributes.FLYING_SPEED, .6); }
    public static boolean checkSpawnRules(EntityType<DragonflyEntity> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return checkFlySpawn(level, pos);
    }
    protected static boolean checkFlySpawn(ServerLevelAccessor level, BlockPos pos) { return pos.getY() >= level.getSeaLevel() - 4 && level.getBlockState(pos).isAir(); }
    @Override protected void registerGoals() { goalSelector.addGoal(0, new FloatGoal(this)); goalSelector.addGoal(2, new WaterAvoidingRandomFlyingGoal(this, 1)); goalSelector.addGoal(3, new RandomLookAroundGoal(this)); }
    @Override protected PathNavigation createNavigation(Level level) { FlyingPathNavigation navigation = new FlyingPathNavigation(this, level); navigation.setCanOpenDoors(false); navigation.setCanFloat(false); return navigation; }
    public int getVariant() { return variant; }
    private static int historicalVariant(RandomSource random){int value=random.nextInt(5);if(value>2)value-=random.nextInt(2);else if(value<2)value+=random.nextInt(2);return value;}
    @Override protected SoundEvent getAmbientSound(){return BMSounds.DRAGONFLY_LOOP;}
    @Override protected SoundEvent getHurtSound(DamageSource source){return BMSounds.DRAGONFLY_HURT;}
    @Override protected SoundEvent getDeathSound(){return BMSounds.DRAGONFLY_DEATH;}
    @Override protected void addAdditionalSaveData(ValueOutput output) { super.addAdditionalSaveData(output); output.putInt("Variant", variant); }
    @Override protected void readAdditionalSaveData(ValueInput input) { super.readAdditionalSaveData(input); variant = Math.floorMod(input.getIntOr("Variant", 0), 5); }
}
