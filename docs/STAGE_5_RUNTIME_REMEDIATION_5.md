# Stage 5 Runtime Remediation 5

## Scope and evidence

This checkpoint uses released Biome Makeover 1.20.1-1.11.4 at commit
`2f314c0596af095a4890995a465f308f69476b4a` as the gameplay contract. It is limited to the remaining Swamp runtime
findings and the authorized Mushroom Fields underground follow-up. Stage 6, Taniwha, Owl, Cowboy, and accepted
Stage 3-5 systems are outside scope.

## Disposition

| Issue | Disposition | Evidence and result |
|---|---|---|
| A. Willow/Cypress sapling growth | FIXED STATICALLY — RUNTIME VERIFICATION REQUIRED | Minecraft 1.21.10 `TreeGrower` replaces the sapling with its fluid legacy block. Released Willow deliberately placed from air; released Cypress deliberately placed from water, including land growth. Both now retain their separate released origin state, two-stage sapling growth, and depth limits (Willow 1, Cypress 3). |
| B. Willowing Branch item color | FIXED STATICALLY — RUNTIME VERIFICATION REQUIRED | Released color registration tinted Willow Leaves and Willowing Branches through the same block-and-item provider. The port retained only the block provider for branches. The 1.21.10 item definition now uses the released no-world Willow foliage color without changing world tint. |
| C. Mushroom underground generation | INSUFFICIENT EVIDENCE | The packaged chain matches final source: underground mycelium at `UNDERGROUND_DECORATION`, huge glowshrooms at `VEGETAL_DECORATION`, exact Mushroom Fields injection, count 120, uniform absolute Y -30..60, and biome filter. Attempts sample arbitrary Y and require a valid cave floor plus volume, so one spectator sweep cannot prove failure. Density is unchanged. Use the real `/place feature` commands below before considering production changes. |
| D. Bottle o' Lightning | FIXED STATICALLY — RUNTIME VERIFICATION REQUIRED | Final source makes `biomemakeover:lightning_bottle` reachable by using an experience bottle on a Lightning Bug. The complete item/entity/effect chain is restored: capture, throwing, cooldown/consumption, dispenser behavior, radius-four lightning transformations, Shocked I-IV max-health penalty, lightning rod/copper handling, particles/sounds, rendering, persistence, and advancement. Obsolete Architectury visual packets are translated to server particle broadcasts. |
| E. Fence gates | SOURCE-CONFIRMED / UNCHANGED | Willow and Cypress gates are direct 1.21.10 vanilla `FenceGateBlock` instances. Facing, `in_wall`, placement, neighbor updates, outline/collision shapes, and adjacent-gate presentation are vanilla behavior. No BM-specific override exists. |

## Mushroom Fields verification procedure

In fresh Mushroom Fields chunks, inspect a meaningful underground area between Y -30 and 60. For deterministic
diagnosis, stand on a valid mycelium cave floor with sufficient air and run:

- `/place feature biomemakeover:mushroom_fields/huge_purple_glowshroom`
- `/place feature biomemakeover:mushroom_fields/huge_green_glowshroom`
- `/place feature biomemakeover:mushroom_fields/underground_huge_glowshrooms`

These commands invoke registered production configured/placed features; no debug registry or duplicate placement
implementation was added. The first two validate feature placement at the selected origin. The last retains the real
released random-height placement and biome filter.

## Registry and resource changes

The Lightning Bottle restoration is additive: one item, one projectile entity, one mob effect, and two sound events.
It adds the released item model/texture, effect icon, item definition, and advancement. No ID was removed, renamed, or
repurposed. Sapling and tint fixes add no registry entries.

## Runtime gate

Static/package validation is not runtime acceptance. Prism must verify the sapling matrix, Willowing Branch render
paths, Mushroom underground placement/generation, and the full Lightning Bottle interaction before Stage 5 closes.
