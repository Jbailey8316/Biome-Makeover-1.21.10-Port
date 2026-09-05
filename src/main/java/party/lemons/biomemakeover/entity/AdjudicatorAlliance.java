package party.lemons.biomemakeover.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.EvokerFangs;

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
        }
    }

    public static void inheritFromOwner(Entity member, Entity owner) {
        String id = encounterId(owner);
        if (id != null && !hasEncounter(member, id)) {
            member.addTag(TAG_PREFIX + id);
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


    private static boolean hasEncounter(Entity entity, String id) {
        return id.equals(encounterId(entity));
    }

}
