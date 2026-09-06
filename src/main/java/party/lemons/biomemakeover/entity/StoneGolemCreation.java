package party.lemons.biomemakeover.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import party.lemons.biomemakeover.init.BMBlocks;
import party.lemons.biomemakeover.init.BMEntities;

/** The released two-form Cladded Stone creation pattern, without boss activation. */
public final class StoneGolemCreation {
    private static final BlockPattern STANDING = BlockPatternBuilder.start()
        .aisle("~^~", "###", "~#~")
        .where('^', block -> block.getState().is(Blocks.CARVED_PUMPKIN))
        .where('#', block -> block.getState().is(BMBlocks.CLADDED_STONE))
        .where('~', block -> block.getState().isAir())
        .build();

    private StoneGolemCreation() {}

    public static boolean tryCreate(Level level, BlockPos pos) {
        if (level.isClientSide()) return false;
        BlockPattern.BlockPatternMatch match = STANDING.find(level, pos);
        if (match == null) return false;
        BlockPos pumpkin = match.getBlock(1, 0, 0).getPos();
        BlockPos spawn = match.getBlock(1, 2, 0).getPos();
        StoneGolemEntity golem = BMEntities.STONE_GOLEM.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (golem == null) return false;
        golem.setPos(spawn.getX() + .5D, spawn.getY() + .05D, spawn.getZ() + .5D);
        golem.setPlayerCreated(true);
        for (int y = 0; y < 3; y++) for (int x = 0; x < 3; x++) {
            BlockPos block = match.getBlock(x, y, 0).getPos();
            if (!level.isEmptyBlock(block)) level.destroyBlock(block, false);
        }
        ((net.minecraft.server.level.ServerLevel) level).addFreshEntityWithPassengers(golem);
        level.levelEvent(2001, pumpkin, 0);
        if (Boolean.getBoolean("bm.mansion.trace")) {
            System.out.println("BM_STONE_GOLEM_PARITY_PROOF PATTERN_MATCH triggerPos=" + pumpkin + " orientation=standing");
            System.out.println("BM_STONE_GOLEM_PARITY_PROOF ENTITY_CREATE uuid=" + golem.getUUID() + " reason=MOB_SUMMONED playerCreated=" + golem.isPlayerCreated() + " health=" + golem.getHealth() + " spawnReferenceCell=" + spawn + " spawn=" + golem.position());
            System.out.println("BM_STONE_GOLEM_PARITY_PROOF ENTITY_ADD uuid=" + golem.getUUID() + " success=true");
            System.out.println("BM_STONE_GOLEM_PARITY_PROOF CREATION_COMPLETE uuid=" + golem.getUUID() + " blocksConsumed=4");
        }
        return true;
    }
}
