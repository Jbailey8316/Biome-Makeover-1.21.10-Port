package party.lemons.biomemakeover.util.extension;

/** Persistent final-release contract mixed into every vanilla AgeableMob. */
public interface Stuntable {
    boolean biomemakeover$isStunted();
    void biomemakeover$setStunted(boolean stunted);

    default boolean biomemakeover$isAlwaysBaby() {
        return false;
    }
}
