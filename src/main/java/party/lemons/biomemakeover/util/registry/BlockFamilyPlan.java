package party.lemons.biomemakeover.util.registry;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * An explicit, deterministic ID plan for a block family.
 *
 * <p>Unlike Taniwha's all-or-nothing factories, callers must name every
 * historical member. This prevents a modern helper from silently inventing
 * IDs. The plan registers nothing and carries no gameplay properties.</p>
 */
public final class BlockFamilyPlan {
    private final String basePath;
    private final List<String> memberPaths;

    private BlockFamilyPlan(String basePath, List<String> memberPaths) {
        this.basePath = basePath;
        this.memberPaths = memberPaths;
    }

    public static BlockFamilyPlan explicit(String basePath, String... memberPaths) {
        BMRegistryUtil.checkedId(basePath);
        Objects.requireNonNull(memberPaths, "member paths");
        Set<String> unique = new LinkedHashSet<>();
        for (String memberPath : memberPaths) {
            BMRegistryUtil.checkedId(memberPath);
            if (!unique.add(memberPath)) {
                throw new IllegalArgumentException("Duplicate family member: " + memberPath);
            }
        }
        List<String> ordered = new ArrayList<>(unique);
        Collections.sort(ordered);
        return new BlockFamilyPlan(basePath, List.copyOf(ordered));
    }

    public String basePath() {
        return basePath;
    }

    public List<String> memberPaths() {
        return memberPaths;
    }

    public List<ResourceLocation> memberIds() {
        return memberPaths.stream().map(BMRegistryUtil::checkedId).toList();
    }
}
