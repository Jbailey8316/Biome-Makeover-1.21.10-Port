package party.lemons.biomemakeover.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import party.lemons.biomemakeover.init.BMSounds;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Released Adjudicator entity substrate; encounter phases are restored in a later stage. */
public final class AdjudicatorEntity extends Monster implements RangedAttackMob {
    /** Staged availability gate; Mimic and Stone Golem remain deferred. */
    private static final boolean IMPLEMENTED_PHASE_EXECUTION_GATE = true;
    private static final int STATE_WAITING = 0;
    private static final int STATE_TELEPORT = 1;
    private static final int STATE_FIGHTING = 2;
    private static final int STATE_SUMMONING = 3;
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
    private boolean summonInterrupted;
    private BlockPos homePos;
    private BlockPos teleportPos;
    private AABB roomBounds;
    private List<BlockPos> arenaPositions;
    private ControllerPhase nextPhase = ControllerPhase.IDLE;
    private final List<Goal> phaseGoals = new ArrayList<>();
    private final List<Goal> phaseTargetGoals = new ArrayList<>();
    public AdjudicatorEntity(EntityType<? extends AdjudicatorEntity> type, Level level) {
        super(type, level);
        xpReward = 50;
        setPersistenceRequired();
        AdjudicatorAlliance.ensure(this);
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
            updateBossBarPlayers();
            tickController();
        }
        super.tick();
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        active = true;
        if (isSummonPhase(phase) && source.getEntity() instanceof Player) summonInterrupted = true;
        return super.hurtServer(level, source, amount);
    }

    private void initializeArena() {
        // The released encounter becomes active as soon as its first server tick
        // establishes the arena. The execution gate below only disables attacks.
        active = true;
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
    }

    private void tickController() {
        restorePhaseExecutionIfNeeded();
        if (phase == ControllerPhase.IDLE && level().getGameTime() % 4L == 0L) heal(1.0F);
        if (getTarget() != null && !isTargetInArena(getTarget())) setTarget(null);
        if ((phase == ControllerPhase.FANG_ATTACK || phase == ControllerPhase.FANG_BARRAGE)
            && getTarget() != null) {
            getNavigation().stop();
            getLookControl().setLookAt(getTarget(), 30.0F, 30.0F);
        }
        if (active && phase == ControllerPhase.IDLE) {
            beginTeleport(selectNextPhaseForStage(random));
        } else if (active && phase == ControllerPhase.TELEPORT) {
            emitTeleportParticles();
            if (++phaseTime >= TELEPORT_PHASE_TICKS) {
                teleportToArenaPosition(teleportPos);
                enterPhase(nextPhase);
            }
        } else if (active && (phase == ControllerPhase.BOW_ATTACK || phase == ControllerPhase.MELEE_ATTACK)) {
            if (++phaseTime >= ATTACK_PHASE_TICKS) beginTeleport(selectNextPhaseForStage(random));
        } else if (active && phase == ControllerPhase.FANG_ATTACK) {
            if (++phaseTime >= 20 && (phaseTime - 20) % 40 == 0) castFangs();
            if (phaseTime >= ATTACK_PHASE_TICKS) beginTeleport(selectNextPhaseForStage(random));
        } else if (active && phase == ControllerPhase.FANG_BARRAGE) {
            if (++phaseTime > 0 && phaseTime % 50 == 0) castFangBarrage();
            if (phaseTime >= FANG_BARRAGE_PHASE_TICKS) beginTeleport(selectNextPhaseForStage(random));
        } else if (active && isSummonPhase(phase)) {
            getNavigation().stop();
            if (getTarget() != null) getLookControl().setLookAt(getTarget(), 30.0F, 30.0F);
            int count = summonCount(phase);
            if (++phaseTime % (SUMMON_PHASE_TICKS / count) == 0 && summonIndex < count) {
                spawnSummonedEntity(phase);
                summonIndex++;
            }
            if (summonIndex >= count || phaseTime >= SUMMON_PHASE_TICKS)
                beginTeleport(selectNextPhaseForStage(random));
        } else if (active && phase == ControllerPhase.RAVAGER) {
            if (getVehicle() == null) beginTeleport(selectNextPhaseForStage(random));
            else if (getTarget() != null) getLookControl().setLookAt(getTarget(), 30.0F, 30.0F);
        }
        bossBar.setProgress(getHealth() / getMaxHealth());
    }

    private void beginTeleport(ControllerPhase destinationPhase) {
        exitPhase();
        nextPhase = destinationPhase;
        teleportPos = chooseArenaPosition();
        phase = ControllerPhase.TELEPORT;
        phaseTime = 0;
        setControllerState(STATE_TELEPORT);
        setControllerInvulnerable(false);
        playSound(BMSounds.ADJUDICATOR_SPELL_3, 1.0F, 1.0F);
    }

    private BlockPos chooseArenaPosition() {
        if (arenaPositions == null || arenaPositions.isEmpty()) return blockPosition();
        BlockPos selected = arenaPositions.get(random.nextInt(arenaPositions.size()));
        int safety = 0;
        while (selected.closerThan(getOnPos(), 1.0D) && safety++ < 100)
            selected = arenaPositions.get(random.nextInt(arenaPositions.size()));
        return selected;
    }

    private void enterPhase(ControllerPhase selected) {
        exitPhase();
        phase = selected;
        phaseTime = 0;
        configurePhaseExecution(selected, true);
    }

    private void configurePhaseExecution(ControllerPhase selected, boolean playEntrySound) {
        setControllerState(STATE_FIGHTING);
        selectTargetInArena();
        if (selected == ControllerPhase.BOW_ATTACK) {
            setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));
            addPhaseGoals(new RangedBowAttackGoal<>(this, 0.75F, 12, 30));
            if (playEntrySound) playSound(BMSounds.ADJUDICATOR_GRUNT, 1.0F, 1.0F);
        } else if (selected == ControllerPhase.MELEE_ATTACK) {
            setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
            addPhaseGoals(new MeleeAttackGoal(this, 1.0F, true));
            if (playEntrySound) playSound(BMSounds.ADJUDICATOR_GRUNT, 1.0F, 1.0F);
        } else if (selected == ControllerPhase.FANG_ATTACK) {
            setControllerState(STATE_SUMMONING);
            if (playEntrySound) playSound(net.minecraft.sounds.SoundEvents.EVOKER_PREPARE_ATTACK, 1.0F, 1.0F);
        } else if (selected == ControllerPhase.FANG_BARRAGE) {
            setControllerState(STATE_SUMMONING);
        } else if (isSummonPhase(selected)) {
            setControllerState(STATE_SUMMONING);
            summonIndex = 0;
            summonInterrupted = false;
            getNavigation().stop();
            if (playEntrySound) playSound(net.minecraft.sounds.SoundEvents.EVOKER_PREPARE_SUMMON, 1.0F, 1.0F);
        } else if (selected == ControllerPhase.RAVAGER) {
            setControllerState(STATE_FIGHTING);
            setControllerInvulnerable(true);
            if (!(level() instanceof ServerLevel serverLevel)) return;
            Ravager ravager = EntityType.RAVAGER.create(serverLevel, EntitySpawnReason.EVENT);
            if (ravager != null) {
                AdjudicatorAlliance.assign(ravager, this);
                ravager.snapTo(getX(), getY(), getZ(), getYRot(), getXRot());
                ravager.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(blockPosition()), EntitySpawnReason.EVENT, null);
                ravager.setTarget(getTarget());
                serverLevel.addFreshEntity(ravager);
                startRiding(ravager, true, true);
            }
            ItemStack crossbow = new ItemStack(Items.CROSSBOW);
            crossbow.enchant(level().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.MULTISHOT), 3);
            setItemInHand(InteractionHand.MAIN_HAND, crossbow);
        }
    }

    private void restorePhaseExecutionIfNeeded() {
        if (phaseGoals.isEmpty() && (phase == ControllerPhase.BOW_ATTACK || phase == ControllerPhase.MELEE_ATTACK))
            configurePhaseExecution(phase, false);
    }

    private void addPhaseGoals(Goal attackGoal) {
        Goal floating = new FloatGoal(this);
        Goal strolling = new RandomStrollGoal(this, 1.0D);
        Goal looking = new LookAtPlayerGoal(this, Player.class, 20.0F);
        Goal lookingAround = new RandomLookAroundGoal(this);
        phaseGoals.add(floating);
        phaseGoals.add(strolling);
        phaseGoals.add(attackGoal);
        phaseGoals.add(looking);
        phaseGoals.add(lookingAround);
        goalSelector.addGoal(0, floating);
        goalSelector.addGoal(1, strolling);
        goalSelector.addGoal(2, attackGoal);
        goalSelector.addGoal(3, looking);
        goalSelector.addGoal(4, lookingAround);
        Goal hurtTarget = new HurtByTargetGoal(this);
        Goal playerTarget = new NearestAttackableTargetGoal<>(this, Player.class, false);
        Goal golemTarget = new NearestAttackableTargetGoal<>(this, AbstractGolem.class, false);
        phaseTargetGoals.add(hurtTarget);
        phaseTargetGoals.add(playerTarget);
        phaseTargetGoals.add(golemTarget);
        targetSelector.addGoal(1, hurtTarget);
        targetSelector.addGoal(2, playerTarget);
        targetSelector.addGoal(3, golemTarget);
    }

    private void exitPhase() {
        phaseGoals.forEach(goalSelector::removeGoal);
        phaseTargetGoals.forEach(targetSelector::removeGoal);
        phaseGoals.clear();
        phaseTargetGoals.clear();
        if (phase == ControllerPhase.BOW_ATTACK || phase == ControllerPhase.MELEE_ATTACK || phase == ControllerPhase.RAVAGER)
            setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        if (phase == ControllerPhase.RAVAGER) {
            Entity vehicle = getVehicle();
            if (isPassenger()) stopRiding();
            if (vehicle instanceof Ravager ravager) ravager.discard();
            setControllerInvulnerable(false);
        }
        if (isSummonPhase(phase) && !summonInterrupted) {
            int count = summonCount(phase);
            while (summonIndex < count) { spawnSummonedEntity(phase); summonIndex++; }
        }
    }

    private void selectTargetInArena() {
        if (roomBounds == null) return;
        Player target = level().getEntitiesOfClass(Player.class, roomBounds, EntitySelector.NO_SPECTATORS).stream()
            .min(java.util.Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
        setTarget(target);
    }

    private void teleportToArenaPosition(BlockPos pos) {
        if (pos == null) pos = homePos;
        if (pos == null) return;
        if (level().getBlockState(pos.below()).isAir())
            level().setBlock(pos.below(), Blocks.COBBLESTONE.defaultBlockState(), 3);
        setPos(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D);
        clearTeleportArea();
        setControllerState(STATE_FIGHTING);
    }

    private void emitTeleportParticles() {
        if (level() instanceof ServerLevel serverLevel)
            serverLevel.sendParticles(ParticleTypes.PORTAL, getX(), getY() + 0.8D, getZ(),
                10, 0.5D, 0.5D, 0.5D, 0.15D);
    }

    private void clearTeleportArea() {
        if (!(level() instanceof ServerLevel serverLevel)
            || !serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) return;
        AABB hitBox = getBoundingBox();
        destroyTeleportArea(hitBox);
        if (isInWall()) destroyTeleportArea(hitBox.inflate(1.0D));
    }

    private void destroyTeleportArea(AABB hitBox) {
        BlockPos.betweenClosed(
            BlockPos.containing(hitBox.minX, hitBox.minY, hitBox.minZ),
            BlockPos.containing(hitBox.maxX, hitBox.maxY, hitBox.maxZ)).forEach(pos -> {
                if (WitherBoss.canDestroy(level().getBlockState(pos))) {
                    level().destroyBlock(pos, true, this);
                    level().levelEvent(null, 1022, pos, 0);
                }
            });
    }

    private void castFangs() {
        LivingEntity target = getTarget();
        if (!isTargetInArena(target)) return;
        double minY = Math.min(target.getY(), getY());
        double maxY = Math.max(target.getY(), getY()) + 1.0D;
        float angle = (float) Math.atan2(target.getZ() - getZ(), target.getX() - getX());
        if (distanceTo(target) < 24.0F) {
            for (int i = 0; i < 5; i++) {
                float yaw = (float) (angle + i * Math.PI * 0.4F);
                conjureFang(getX() + Math.cos(yaw) * 1.5D, getZ() + Math.sin(yaw) * 1.5D,
                    minY, maxY, yaw, 0);
            }
            for (int i = 0; i < 8; i++) {
                float yaw = (float) (angle + i * Math.PI * 2.0F / 8.0F + 1.2566371F);
                conjureFang(getX() + Math.cos(yaw) * 2.5D, getZ() + Math.sin(yaw) * 2.5D,
                    minY, maxY, yaw, 3);
            }
        } else {
            for (int i = 0; i < 16; i++) {
                double distance = 1.25D * (i + 1);
                conjureFang(getX() + Math.cos(angle) * distance, getZ() + Math.sin(angle) * distance,
                    minY, maxY, angle, i);
            }
        }
    }

    private void castFangBarrage() {
        playSound(net.minecraft.sounds.SoundEvents.EVOKER_PREPARE_SUMMON, 1.0F, 1.0F);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            for (int i = 0; i < 10; i++) {
                conjureFang(getX() + direction.getStepX() * (i + 1),
                    getZ() + direction.getStepZ() * (i + 1), 10.0D, getY(),
                    random.nextFloat() * (float) Math.PI, i);
            }
        }
    }

    private void conjureFang(double x, double z, double maxY, double y, float yaw, int warmup) {
        BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
        boolean found = false;
        double height = 0.0D;
        do {
            BlockPos below = pos.below();
            if (level().getBlockState(below).isFaceSturdy(level(), below, Direction.UP)) {
                if (!level().isEmptyBlock(pos)) {
                    var shape = level().getBlockState(pos).getCollisionShape(level(), pos);
                    if (!shape.isEmpty()) height = shape.max(net.minecraft.core.Direction.Axis.Y);
                }
                found = true;
                break;
            }
            pos = pos.below();
        } while (pos.getY() >= net.minecraft.util.Mth.floor(maxY) - 1);
        if (found) level().addFreshEntity(new EvokerFangs(level(), x, pos.getY() + height, z, yaw, warmup, this));
    }

    private static boolean isSummonPhase(ControllerPhase phase) {
        return phase == ControllerPhase.SPAWN_EVOKER || phase == ControllerPhase.SPAWN_VINDICATOR
            || phase == ControllerPhase.SPAWN_VEX || phase == ControllerPhase.SPAWN_MIX;
    }

    private static int summonCount(ControllerPhase phase) {
        return switch (phase) {
            case SPAWN_EVOKER, SPAWN_VEX -> 2;
            case SPAWN_VINDICATOR -> 6;
            case SPAWN_MIX -> 3;
            default -> 0;
        };
    }

    private EntityType<? extends LivingEntity> summonType(ControllerPhase phase) {
        return switch (phase) {
            case SPAWN_EVOKER -> EntityType.EVOKER;
            case SPAWN_VINDICATOR -> EntityType.VINDICATOR;
            case SPAWN_VEX -> EntityType.VEX;
            case SPAWN_MIX -> switch (random.nextInt(4)) {
                case 0 -> EntityType.VEX;
                case 1 -> EntityType.VINDICATOR;
                case 2 -> EntityType.EVOKER;
                default -> EntityType.PILLAGER;
            };
            default -> EntityType.EVOKER;
        };
    }

    private void spawnSummonedEntity(ControllerPhase phase) {
        BlockPos spawnPos = chooseArenaPosition();
        Entity entity = summonType(phase).create(level(), EntitySpawnReason.EVENT);
        if (entity == null) return;
        AdjudicatorAlliance.assign(entity, this);
        entity.snapTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, 0.0F, 0.0F);
        if (entity instanceof Mob mob && level() instanceof ServerLevel serverLevel) {
            mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(spawnPos), EntitySpawnReason.EVENT, null);
            mob.setTarget(getTarget());
        }
        if (level() instanceof ServerLevel serverLevel) serverLevel.addFreshEntityWithPassengers(entity);
        level().playSound(null, spawnPos, net.minecraft.sounds.SoundEvents.EVOKER_CAST_SPELL,
            net.minecraft.sounds.SoundSource.HOSTILE, 10.0F, 1.0F);
    }

    private void updateBossBarPlayers() {
        if (roomBounds == null) return;
        List<ServerPlayer> inside = level().getEntitiesOfClass(ServerPlayer.class, roomBounds, EntitySelector.NO_SPECTATORS);
        Set<UUID> current = ConcurrentHashMap.newKeySet();
        for (ServerPlayer player : inside) {
            current.add(player.getUUID());
            if (!bossBar.getPlayers().contains(player)) {
                bossBar.addPlayer(player);
            }
        }
        for (ServerPlayer player : List.copyOf(bossBar.getPlayers())) {
            if (!current.contains(player.getUUID())) {
                bossBar.removePlayer(player);
            }
        }
        bossBar.setVisible(active);
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

    private ControllerPhase selectNextPhaseForStage(RandomSource random) {
        if (!IMPLEMENTED_PHASE_EXECUTION_GATE) return selectNextPhase(random);
        List<ControllerPhase> actualPool = selectablePhases().stream()
            .filter(AdjudicatorEntity::isImplementedPhase).toList();
        ControllerPhase selected = actualPool.isEmpty() ? ControllerPhase.IDLE
            : actualPool.get(random.nextInt(actualPool.size()));
        return selected;
    }

    private List<ControllerPhase> selectablePhases() {
        List<ControllerPhase> selectable = new ArrayList<>();
        for (ControllerPhase candidate : ControllerPhase.values()) {
            if (candidate.selectable() && (!isSummonPhase(candidate) || summonPhaseEligible()))
                selectable.add(candidate);
        }
        return selectable;
    }

    private static boolean isImplementedPhase(ControllerPhase phase) {
        return phase != ControllerPhase.MIMIC && phase != ControllerPhase.STONE_GOLEM;
    }

    private int arenaMonsterCount() {
        return roomBounds == null ? 0 : level().getEntitiesOfClass(Monster.class, roomBounds,
            EntitySelector.LIVING_ENTITY_STILL_ALIVE).size();
    }

    @Override
    public void performRangedAttack(LivingEntity target, float pullProgress) {
        ItemStack arrows = Items.ARROW.getDefaultInstance();
        AbstractArrow arrow = ProjectileUtil.getMobArrow(this, arrows, pullProgress, getMainHandItem());
        double dx = target.getX() - getX();
        double dy = target.getY(0.3333333333333333D) - arrow.getY();
        double dz = target.getZ() - getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        arrow.shoot(dx, dy + horizontal * 0.2D, dz, 1.6F,
            (float)(14 - level().getDifficulty().getId() * 4));
        playSound(net.minecraft.sounds.SoundEvents.SKELETON_SHOOT, 1.0F,
            1.0F / (getRandom().nextFloat() * 0.4F + 0.8F));
        level().addFreshEntity(arrow);
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
        output.putString("NextPhase", nextPhase.id());
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
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        firstTick = input.getBooleanOr("FirstTick", true);
        active = input.getBooleanOr("BossActive", false);
        phase = ControllerPhase.byId(input.getStringOr("Phase", ControllerPhase.IDLE.id()));
        nextPhase = ControllerPhase.byId(input.getStringOr("NextPhase", ControllerPhase.IDLE.id()));
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
    }

    private static void writePosition(ValueOutput output, String prefix, BlockPos pos) {
        if (pos != null) { output.putInt(prefix + "X", pos.getX()); output.putInt(prefix + "Y", pos.getY()); output.putInt(prefix + "Z", pos.getZ()); }
    }

    private static BlockPos readPosition(ValueInput input, String prefix) {
        if (!input.getInt(prefix + "X").isPresent()) return null;
        return new BlockPos(input.getIntOr(prefix + "X", 0), input.getIntOr(prefix + "Y", 0), input.getIntOr(prefix + "Z", 0));
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
