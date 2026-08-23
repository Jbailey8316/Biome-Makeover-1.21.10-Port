package party.lemons.biomemakeover.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.FlyingAnimal;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.tags.BlockTags;
import party.lemons.biomemakeover.init.BMSounds;

public class DragonflyEntity extends PathfinderMob implements FlyingAnimal {
    private int variant;
    public DragonflyEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level); moveControl = new FlyingMoveControl(this, 20, true); variant = historicalVariant(random);
        setPathfindingMalus(PathType.DANGER_FIRE,-1F); setPathfindingMalus(PathType.WATER,-1F);
        setPathfindingMalus(PathType.WATER_BORDER,16F); setPathfindingMalus(PathType.COCOA,-1F); setPathfindingMalus(PathType.FENCE,-1F);
    }
    public static AttributeSupplier.Builder createAttributes() { return createMobAttributes().add(Attributes.MAX_HEALTH, 10).add(Attributes.MOVEMENT_SPEED, .25).add(Attributes.FLYING_SPEED, .6); }
    public static boolean checkSpawnRules(EntityType<DragonflyEntity> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return checkFlySpawn(level, pos);
    }
    protected static boolean checkFlySpawn(ServerLevelAccessor level, BlockPos pos) { return level.getBlockState(pos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON)&&level.getRawBrightness(pos,0)>8; }
    @Override protected void registerGoals() { goalSelector.addGoal(0, new FloatGoal(this)); goalSelector.addGoal(2, new WaterAvoidingRandomFlyingGoal(this, 1)); goalSelector.addGoal(3, new RandomLookAroundGoal(this)); }
    @Override protected PathNavigation createNavigation(Level level) { FlyingPathNavigation navigation = new FlyingPathNavigation(this, level); navigation.setCanOpenDoors(false); navigation.setCanFloat(false); return navigation; }
    @Override public boolean causeFallDamage(double distance,float multiplier,DamageSource source){return false;}
    @Override protected void checkFallDamage(double distance,boolean onGround,BlockState state,BlockPos pos){}
    @Override public boolean isFlying(){return true;}
    public int getVariant() { return variant; }
    private static int historicalVariant(RandomSource random){int value=random.nextInt(5);if(value>2)value-=random.nextInt(2);else if(value<2)value+=random.nextInt(2);return value;}
    @Override protected SoundEvent getAmbientSound(){return BMSounds.DRAGONFLY_LOOP;}
    @Override protected SoundEvent getHurtSound(DamageSource source){return BMSounds.DRAGONFLY_HURT;}
    @Override protected SoundEvent getDeathSound(){return BMSounds.DRAGONFLY_DEATH;}
    @Override protected void addAdditionalSaveData(ValueOutput output) { super.addAdditionalSaveData(output); output.putInt("Variant", variant); }
    @Override protected void readAdditionalSaveData(ValueInput input) { super.readAdditionalSaveData(input); variant = Math.floorMod(input.getIntOr("Variant", 0), 5); }
}
