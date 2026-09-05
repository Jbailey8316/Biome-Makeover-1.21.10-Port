package party.lemons.biomemakeover.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import party.lemons.biomemakeover.init.BMSounds;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Released Adjudicator entity substrate; encounter phases are restored in a later stage. */
public final class AdjudicatorEntity extends Monster {
    /** Combat execution is intentionally deferred to the later phase stages. */
    private static final boolean COMBAT_PHASES_ENABLED = false;
    public static final int TELEPORT_PHASE_TICKS = 30;
    public static final int ATTACK_PHASE_TICKS = 200;
    public static final int FANG_BARRAGE_PHASE_TICKS = 100;
    public static final int SUMMON_PHASE_TICKS = 120;
    private static final EntityDataAccessor<Integer> STATE =
        SynchedEntityData.defineId(AdjudicatorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> CHARGING =
        SynchedEntityData.defineId(AdjudicatorEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> INVULNERABLE =
        SynchedEntityData.defineId(AdjudicatorEntity.class, EntityDataSerializers.BOOLEAN);

    /** Released phase inventory; execution is supplied by later stages. */
    public enum ControllerPhase {
        IDLE("idle", false), TELEPORT("teleport", false), BOW_ATTACK("bow_attack", true),
        MELEE_ATTACK("melee_attack", true), FANG_ATTACK("fang_attack", true),
        FANG_BARRAGE("fang_barrage", true), RAVAGER("ravager", true),
        SPAWN_EVOKER("spawn_evoker", true), SPAWN_VINDICATOR("spawn_vindicator", true),
        SPAWN_VEX("spawn_vex", true), SPAWN_MIX("spawn_mix", true), MIMIC("mimic", true),
        STONE_GOLEM("stone_golem", true);

        private final String id;
        private final boolean selectable;
        ControllerPhase(String id, boolean selectable) { this.id = id; this.selectable = selectable; }
        public String id() { return id; }
        public boolean selectable() { return selectable; }
        public static ControllerPhase byId(String id) {
            for (ControllerPhase phase : values()) if (phase.id.equals(id)) return phase;
            return IDLE;
        }
    }

    private final ServerBossEvent bossBar;
    private ControllerPhase phase = ControllerPhase.IDLE;
    private boolean active;
    private boolean firstTick = true;
    private int phaseTime;
    private int finishFightTime;
    private int summonIndex;
    private BlockPos homePos;
    private BlockPos teleportPos;
    private AABB roomBounds;
    private List<BlockPos> arenaPositions;
    public AdjudicatorEntity(EntityType<? extends AdjudicatorEntity> type, Level level) {
        super(type, level);
        xpReward = 50;
        setPersistenceRequired();
        bossBar = new ServerBossEvent(getDisplayName(), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.PROGRESS);
        bossBar.setVisible(false);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STATE, 0);
        builder.define(CHARGING, false);
        builder.define(INVULNERABLE, false);
    }

    @Override
    public void tick() {
        if (!level().isClientSide() && firstTick) initializeArena();
        if (!level().isClientSide()) {
            phaseTime++;
            tickController();
            updateBossBarPlayers();
        }
        super.tick();
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        active = true;
        return super.hurtServer(level, source, amount);
    }

    private void initializeArena() {
        homePos = blockPosition();
        roomBounds = new AABB(homePos.below(4)).inflate(13, 0, 13).expandTowards(0, 13, 0);
        arenaPositions = new ArrayList<>();
        arenaPositions.add(homePos.immutable());
        BlockPos.betweenClosed((int) roomBounds.minX, (int) roomBounds.minY, (int) roomBounds.minZ,
                (int) roomBounds.maxX, (int) roomBounds.maxY, (int) roomBounds.maxZ).forEach(pos -> {
            if (level().getBlockState(pos).is(Blocks.SMOOTH_QUARTZ)) {
                level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                arenaPositions.add(pos.immutable());
            }
        });
        firstTick = false;
        trace("arena_initialized", "home=" + homePos + " bounds=" + roomBounds + " positions=" + arenaPositions.size());
    }

    private void tickController() {
        if (phase == ControllerPhase.IDLE && level().getGameTime() % 4L == 0L) heal(1.0F);
        // Explicit development gate: released eligibility remains represented, but no
        // unimplemented phase is entered and no placeholder attack is executed.
        if (COMBAT_PHASES_ENABLED && phase == ControllerPhase.IDLE && active) {
            phase = selectNextPhase(random);
            phaseTime = 0;
            trace("phase_transition", "phase=" + phase.id());
        }
        bossBar.setProgress(getHealth() / getMaxHealth());
    }

    private void updateBossBarPlayers() {
        if (roomBounds == null) return;
        List<ServerPlayer> inside = level().getEntitiesOfClass(ServerPlayer.class, roomBounds, EntitySelector.NO_SPECTATORS);
        Set<UUID> current = ConcurrentHashMap.newKeySet();
        for (ServerPlayer player : inside) {
            current.add(player.getUUID());
            if (!bossBar.getPlayers().contains(player)) {
                bossBar.addPlayer(player);
                trace("player_entered", "player=" + player.getUUID());
            }
        }
        for (ServerPlayer player : List.copyOf(bossBar.getPlayers())) {
            if (!current.contains(player.getUUID())) {
                bossBar.removePlayer(player);
                trace("player_left", "player=" + player.getUUID());
            }
        }
        bossBar.setVisible(active && phase != ControllerPhase.IDLE);
    }

    public ControllerPhase getControllerPhase() { return phase; }
    public int getPhaseTime() { return phaseTime; }
    public boolean isControllerActive() { return active; }
    public BlockPos getHomePosition() { return homePos; }
    public AABB getArenaBounds() { return roomBounds; }
    public List<BlockPos> getArenaPositions() { return arenaPositions == null ? List.of() : List.copyOf(arenaPositions); }
    public boolean isChargingCrossbow() { return entityData.get(CHARGING); }
    public boolean isControllerInvulnerable() { return entityData.get(INVULNERABLE); }
    public int getControllerState() { return entityData.get(STATE); }
    public void setControllerState(int value) { entityData.set(STATE, value); }
    public void setChargingCrossbow(boolean value) { entityData.set(CHARGING, value); }
    public void setControllerInvulnerable(boolean value) { entityData.set(INVULNERABLE, value); }

    /** Released summon eligibility: fewer than four living arena Monsters. */
    public boolean summonPhaseEligible() {
        return roomBounds != null && level().getEntitiesOfClass(Monster.class, roomBounds,
            EntitySelector.LIVING_ENTITY_STILL_ALIVE).size() < 4;
    }

    /** Released selection rule: uniform choice among currently eligible phases. */
    public ControllerPhase selectNextPhase(RandomSource random) {
        List<ControllerPhase> selectable = new ArrayList<>();
        for (ControllerPhase candidate : ControllerPhase.values()) {
            if (candidate.selectable() && (!candidate.id().startsWith("spawn_") || summonPhaseEligible()))
                selectable.add(candidate);
        }
        return selectable.isEmpty() ? ControllerPhase.IDLE : selectable.get(random.nextInt(selectable.size()));
    }

    public boolean isTargetInArena(LivingEntity target) {
        return target != null && target.isAlive() && roomBounds != null && roomBounds.contains(target.position());
    }

    public void setControllerActive(boolean value) { active = value; }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("FirstTick", firstTick);
        output.putBoolean("BossActive", active);
        output.putString("Phase", phase.id());
        output.putInt("PhaseTime", phaseTime);
        output.putInt("FinishFightTime", finishFightTime);
        output.putInt("SummonIndex", summonIndex);
        output.putInt("State", entityData.get(STATE));
        output.putBoolean("Charging", entityData.get(CHARGING));
        output.putBoolean("Invulnerable", entityData.get(INVULNERABLE));
        writePosition(output, "Home", homePos);
        writePosition(output, "Teleport", teleportPos);
        if (roomBounds != null) {
            output.putDouble("RoomMinX", roomBounds.minX); output.putDouble("RoomMinY", roomBounds.minY);
            output.putDouble("RoomMinZ", roomBounds.minZ); output.putDouble("RoomMaxX", roomBounds.maxX);
            output.putDouble("RoomMaxY", roomBounds.maxY); output.putDouble("RoomMaxZ", roomBounds.maxZ);
        }
        if (arenaPositions != null) {
            output.putInt("ArenaPositionCount", arenaPositions.size());
            for (int i = 0; i < arenaPositions.size(); i++) writePosition(output, "ArenaPosition" + i, arenaPositions.get(i));
        }
        trace("controller_save", "phase=" + phase.id() + " phaseTime=" + phaseTime);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        firstTick = input.getBooleanOr("FirstTick", true);
        active = input.getBooleanOr("BossActive", false);
        phase = ControllerPhase.byId(input.getStringOr("Phase", ControllerPhase.IDLE.id()));
        phaseTime = input.getIntOr("PhaseTime", 0);
        finishFightTime = input.getIntOr("FinishFightTime", 0);
        summonIndex = input.getIntOr("SummonIndex", 0);
        entityData.set(STATE, input.getIntOr("State", 0));
        entityData.set(CHARGING, input.getBooleanOr("Charging", false));
        entityData.set(INVULNERABLE, input.getBooleanOr("Invulnerable", false));
        homePos = readPosition(input, "Home");
        teleportPos = readPosition(input, "Teleport");
        double roomMinX = input.getDoubleOr("RoomMinX", Double.NaN);
        if (!Double.isNaN(roomMinX)) {
            roomBounds = new AABB(roomMinX, input.getDoubleOr("RoomMinY", 0), input.getDoubleOr("RoomMinZ", 0),
                input.getDoubleOr("RoomMaxX", 0), input.getDoubleOr("RoomMaxY", 0), input.getDoubleOr("RoomMaxZ", 0));
        }
        int count = input.getIntOr("ArenaPositionCount", 0);
        arenaPositions = new ArrayList<>();
        for (int i = 0; i < count; i++) { BlockPos pos = readPosition(input, "ArenaPosition" + i); if (pos != null) arenaPositions.add(pos); }
        trace("controller_load", "phase=" + phase.id() + " phaseTime=" + phaseTime);
    }

    private static void writePosition(ValueOutput output, String prefix, BlockPos pos) {
        if (pos != null) { output.putInt(prefix + "X", pos.getX()); output.putInt(prefix + "Y", pos.getY()); output.putInt(prefix + "Z", pos.getZ()); }
    }

    private static BlockPos readPosition(ValueInput input, String prefix) {
        if (!input.getInt(prefix + "X").isPresent()) return null;
        return new BlockPos(input.getIntOr(prefix + "X", 0), input.getIntOr(prefix + "Y", 0), input.getIntOr(prefix + "Z", 0));
    }

    private void trace(String event, String detail) {
        if (Boolean.getBoolean("bm.mansion.trace"))
            party.lemons.biomemakeover.BiomeMakeover.LOGGER.info("[BM_ADJUDICATOR_CONTROLLER_PROOF] event={} entity={} {}", event, getUUID(), detail);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 255.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.25D)
            .add(Attributes.ATTACK_DAMAGE, 3.0D);
    }

    @Override
    public void checkDespawn() {
        noActionTime = 0;
    }

    @Override
    public void die(DamageSource source) {
        bossBar.removeAllPlayers();
        bossBar.setVisible(false);
        super.die(source);
    }

    @Override
    public void setCustomName(net.minecraft.network.chat.Component component) {
        super.setCustomName(component);
        bossBar.setName(getDisplayName());
    }

    @Override protected SoundEvent getAmbientSound() { return BMSounds.ADJUDICATOR_IDLE; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return BMSounds.ADJUDICATOR_HURT; }
    @Override protected SoundEvent getDeathSound() { return BMSounds.ADJUDICATOR_DEATH; }
}
