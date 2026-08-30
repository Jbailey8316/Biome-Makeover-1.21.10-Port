package party.lemons.biomemakeover.level;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DaylightDetectorBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;
import party.lemons.biomemakeover.init.BMParticles;
import party.lemons.biomemakeover.init.BMSounds;

/** Source-faithful random block actions shared by Poltergeist and Possessed. */
public final class PoltergeistHandler {
    private static final Map<TagKey<Block>, Behaviour> TAG_BEHAVIOUR = new HashMap<>();
    private static final Map<Block, Behaviour> BLOCK_BEHAVIOUR = new HashMap<>();

    static {
        registerBehaviour(BlockTags.DOORS, (level, source, pos, state) -> {
            BlockSetType type = ((DoorBlock) state.getBlock()).type();
            if (!type.canOpenByHand()) return false;
            BlockState next = state.cycle(DoorBlock.OPEN);
            level.setBlock(pos, next, 10);
            level.playSound(null, pos, next.getValue(DoorBlock.OPEN) ? type.doorOpen() : type.doorClose(),
                SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.1F + 0.9F);
            level.gameEvent(source, next.getValue(DoorBlock.OPEN) ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
            return true;
        });
        registerBehaviour(BlockTags.BUTTONS, (level, source, pos, state) -> {
            if (state.getValue(ButtonBlock.POWERED)) return false;
            ((ButtonBlock) state.getBlock()).press(state, level, pos, null);
            level.playSound(null, pos, SoundEvents.WOODEN_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.3F, 0.6F);
            level.gameEvent(source, GameEvent.BLOCK_ACTIVATE, pos);
            return true;
        });
        registerBehaviour(BlockTags.TRAPDOORS, (level, source, pos, state) -> {
            BlockState next = state.cycle(TrapDoorBlock.OPEN);
            level.setBlock(pos, next, 2);
            if (next.getValue(TrapDoorBlock.WATERLOGGED)) level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
            level.levelEvent(null, next.getValue(TrapDoorBlock.OPEN) ? 1007 : 1013, pos, 0);
            level.gameEvent(source, next.getValue(TrapDoorBlock.OPEN) ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
            return true;
        });
        registerBehaviour(Blocks.LEVER, (level, source, pos, state) -> {
            ((LeverBlock) state.getBlock()).pull(state, level, pos, null);
            BlockState next = level.getBlockState(pos);
            level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F,
                next.getValue(LeverBlock.POWERED) ? 0.6F : 0.5F);
            level.gameEvent(source, next.getValue(LeverBlock.POWERED) ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, pos);
            return true;
        });
        registerBehaviour(Blocks.NOTE_BLOCK, (level, source, pos, state) -> {
            if (!state.getValue(NoteBlock.INSTRUMENT).worksAboveNoteBlock() && !level.getBlockState(pos.above()).isAir()) return false;
            level.blockEvent(pos, state.getBlock(), 0, 0);
            level.gameEvent(source, GameEvent.NOTE_BLOCK_PLAY, pos);
            return true;
        });
        registerBehaviour(BlockTags.FENCE_GATES, (level, source, pos, state) -> {
            BlockState next = state.cycle(FenceGateBlock.OPEN);
            level.setBlock(pos, next, 10);
            level.levelEvent(null, next.getValue(FenceGateBlock.OPEN) ? 1008 : 1014, pos, 0);
            level.gameEvent(source, next.getValue(FenceGateBlock.OPEN) ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
            return true;
        });
        registerBehaviour(Blocks.DAYLIGHT_DETECTOR, (level, source, pos, state) -> {
            BlockState next = state.cycle(DaylightDetectorBlock.INVERTED);
            level.setBlock(pos, next, Block.UPDATE_NONE);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(source, next));
            party.lemons.biomemakeover.mixin.DaylightDetectorBlockInvoker.biomemakeover$updateSignalStrength(next, level, pos);
            return true;
        });
        registerBehaviour(Blocks.BELL, (level, source, pos, state) -> ((BellBlock) state.getBlock()).attemptToRing(source, level, pos, (Direction) null));
    }

    private PoltergeistHandler() {}

    public static void doPoltergeist(Level level, @Nullable Entity source, BlockPos origin, int range) {
        int volume = range * range * range;
        int index = level.random.nextInt(volume);
        int half = range / 2;
        int z = index % range;
        int y = (index / range) % range;
        int x = index / (range * range);
        BlockPos check = new BlockPos(origin.getX() + x - half, origin.getY() + y - half, origin.getZ() + z - half);
        if (doBehaviour(level, source, check)) {
            RandomSource random = level.random;
            doParticles(level, check);
            float pitch = random.nextFloat() * 0.4F + random.nextFloat() > 0.9F ? 0.6F : 0.0F;
            level.playSound(null, origin, BMSounds.POLTERGEIST_ACTION, SoundSource.BLOCKS, pitch, 0.6F + random.nextFloat() * 0.4F);
        }
    }

    public static void doParticles(Level level, BlockPos pos) {
        if (level instanceof ServerLevel server) {
            RandomSource random = server.random;
            server.sendParticles(BMParticles.POLTERGEIST, pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(),
                pos.getZ() + random.nextDouble(), 2, random.nextFloat() / 20.0F, 0.025F, random.nextFloat() / 20.0F, 0.0D);
        }
    }

    public static boolean doBehaviour(Level level, @Nullable Entity source, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.is(Blocks.STONE)) return false;
        Behaviour direct = BLOCK_BEHAVIOUR.get(state.getBlock());
        if (direct != null) return direct.handle(level, source, pos, state);
        for (Map.Entry<TagKey<Block>, Behaviour> entry : TAG_BEHAVIOUR.entrySet()) {
            if (state.is(entry.getKey())) return entry.getValue().handle(level, source, pos, state);
        }
        return false;
    }

    public static void registerBehaviour(TagKey<Block> tag, Behaviour behaviour) { TAG_BEHAVIOUR.put(tag, behaviour); }
    public static void registerBehaviour(Block block, Behaviour behaviour) { BLOCK_BEHAVIOUR.put(block, behaviour); }

    @FunctionalInterface
    public interface Behaviour {
        boolean handle(Level level, @Nullable Entity source, BlockPos pos, BlockState state);
    }
}
