package party.lemons.biomemakeover.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.EvokerFangs;
import party.lemons.biomemakeover.BiomeMakeover;

/** Encounter-local alliance support for Adjudicator summons. */
public final class AdjudicatorAlliance {
    private static final String TAG_PREFIX = "bm_adjudicator_encounter:";

    private AdjudicatorAlliance() {}

    public static void ensure(AdjudicatorEntity boss) {
        if (encounterId(boss) == null) assign(boss, boss);
    }

    public static void assign(Entity member, AdjudicatorEntity boss) {
        String id = encounterId(boss);
        if (id == null) {
            id = boss.getUUID().toString();
            boss.addTag(TAG_PREFIX + id);
        }
        if (!hasEncounter(member, id)) {
            member.addTag(TAG_PREFIX + id);
            trace("ENCOUNTER_MEMBER_ASSIGN entityType=" + member.getType()
                + " entityUUID=" + member.getUUID() + " encounterId=" + id);
        }
    }

    public static boolean allied(Entity first, Entity second) {
        if (first == null || second == null) return false;
        String firstId = encounterId(first);
        return firstId != null && firstId.equals(encounterId(second));
    }

    public static Entity resolveAttacker(DamageSource source) {
        Entity direct = source.getDirectEntity();
        if (direct instanceof Projectile projectile && projectile.getOwner() != null)
            return projectile.getOwner();
        if (direct instanceof EvokerFangs fangs && fangs.getOwner() != null)
            return fangs.getOwner();
        return source.getEntity() != null ? source.getEntity() : direct;
    }

    public static String encounterId(Entity entity) {
        if (entity == null) return null;
        for (String tag : entity.getTags()) {
            if (tag.startsWith(TAG_PREFIX)) return tag.substring(TAG_PREFIX.length());
        }
        return null;
    }

    public static void friendlyDamageBlocked(Entity attacker, Entity victim, DamageSource source) {
        trace("FRIENDLY_DAMAGE_BLOCKED attackerType=" + attacker.getType()
            + " victimType=" + victim.getType() + " damageType=" + source.type()
            + " encounterId=" + encounterId(victim));
    }

    public static void friendlyTargetRejected(Entity attacker, Entity target) {
        trace("FRIENDLY_TARGET_REJECTED attackerType=" + attacker.getType()
            + " targetType=" + target.getType() + " encounterId=" + encounterId(attacker));
    }

    public static void evokerVexInherited(Entity evoker, Entity vex) {
        trace("EVOKER_VEX_INHERIT evoker=" + evoker.getUUID() + " vex=" + vex.getUUID()
            + " encounterId=" + encounterId(evoker));
    }

    private static boolean hasEncounter(Entity entity, String id) {
        return id.equals(encounterId(entity));
    }

    private static void trace(String message) {
        if (Boolean.getBoolean("bm.mansion.trace"))
            BiomeMakeover.LOGGER.info("[BM_ADJUDICATOR_ALLIANCE_PROOF] {}", message);
    }
}
