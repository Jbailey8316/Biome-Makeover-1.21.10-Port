# Stage 5 Runtime Remediation 2

## Evidence baseline

This checkpoint starts at `fef3db38e54bdee2235037a70154b83aae98f17e`. Released BM
1.20.1-1.11.4 (`2f314c0596af095a4890995a465f308f69476b4a`) remains the behavior source.
Minecraft 1.21.10 named bytecode was inspected for `LeavesBlock`, `MatchTool`, and
`BeehiveDecorator`. The Prism `latest.log` shows progressive server lag beginning after entry into
fresh Swamp chunks, no repeated exception, and no other logged failure that explains the collapse.

## Runtime observation disposition

| Observation | Disposition | Evidence and action |
|---|---|---|
| Willow canopy decay | FIXED STATICALLY; RUNTIME VERIFICATION REQUIRED | Stage 5 omitted Willow and Cypress trunks from `minecraft:logs`. Modern `LeavesBlock.getOptionalDistanceAt` recognizes distance zero only through that block tag, leaving generated foliage at distance 7. All eight trunk/wood IDs are restored to `logs` and `logs_that_burn` for blocks and items. |
| Willow leaf-block item carpet | FIXED BY REMOVING ERRONEOUS DECAY PATH; RUNTIME VERIFICATION REQUIRED | 1.21.10 natural decay calls the no-tool loot path. The packaged historical loot table still permits the leaf item only for Shears or Silk Touch; no unconditional leaf item entry exists. Correct trunk recognition prevents healthy generated foliage from invoking decay loot at all. No loot suppression or item deletion was added. |
| Progressive tick lag | ROOT CAUSE CORRELATED/REMEDIATED; RUNTIME VERIFICATION REQUIRED | The log escalates from 40 to 744 ticks behind after Swamp entry, alongside the reported mass decay/item entities. It contains no BM exception or competing pathological loop. Missing log tags cause leaf-distance scheduling, random decay, block updates, loot creation, and ItemEntity ticking across every Willow. No cleanup/performance hack was introduced. Exact post-fix entity/tick counts require Prism. The launcher host-memory warning is not evidence of this biome-specific cause. |
| Willow bee nests near ground | SOURCE-CONFIRMED / UNCHANGED | Released 1.20.1 and vanilla 1.21.10 use the same target-height rule: at least one block above the first log, constrained by the lowest foliage. Willow's low branch foliage can select that level. The observed low nest is unusual but reachable historical behavior; the 2% decorator remains. |
| Willow block tint | FIXED | Historical foliage tint uses ordinary foliage color outside Swamps and a -10/+15/-10 RGB shift inside the Swamp tag. The current provider incorrectly shifted every biome; it now follows the released biome condition. |
| Willow inventory tint | FIXED STATICALLY | Historical Taniwha simple block-with-item registration tinted both paths. With no world context, its Willow provider used the unshifted default foliage color. Minecraft 1.21.10 item tint is data-driven; the generated item definition now supplies that historical default constant. The historical texture is unchanged. Cypress receives its historical middle temperature-gradient inventory color. |
| Flat/hard terrain boundaries | NOT BM / INSUFFICIENT EVIDENCE | Stage 5 injects placed vegetation and top-layer feature blocks and removes a vanilla tree feature. It does not register biome noise, density functions, terrain shaping, or chunk blending. No BM terrain change is justified. Old/new chunk or external generation context should be checked if it recurs. |
| Lightning Bug cube appearance | SOURCE-CONFIRMED | The released base model is empty; the visible model is exactly a 2x2 inner cube inside a 4x4 outer cube. The unusual cube is authored behavior, not a missing model. |
| Lightning Bug pulse/color | FIXED STATICALLY; RUNTIME VERIFICATION REQUIRED | Remediation 1 restored geometry but replaced the per-entity randomized pulse phase with synchronized age and replaced gradual block-position color interpolation with direct hashes. It also submitted outer before inner. Random phase, historical interpolation/hash mapping, and inner-then-outer order are restored through the render state. Full brightness, translucency, particles, and geometry remain unchanged. |
| Dragonfly survival | RUNTIME VERIFICATION REQUIRED | The accepted no-fall, health, spawn, and malus remediation remains intact. The supplied log contains no Dragonfly damage/death evidence and exposes no additional static defect. No speculative immunity was added. |
| Accepted Decayed/lily/peat/branch fixes | RUNTIME PASS / UNCHANGED | Baby outer layer, underwater breathing and water seeking, loot, Faster Farmland, Water Lily and Small Lily Pad tint, peat tilling, and branches are frozen. |

## Validation additions

- Both block and item `logs`/`logs_that_burn` tags must contain every Stage 5 trunk and wood ID.
- Willow Leaves must package a modern constant item tint.
- Lightning Bug validation now requires randomized pulse state, interpolated color state, and historical layer order.

## Scope and safety

No registry ID changed. No Stage 6, Taniwha, Owl, Cowboy, Badlands, functional block, structure, boat,
Toad/Tadpole, Witch progression, Lightning Bug Bottle, or Peat Composter content was added. Changes are
additive data-contract and client-render corrections. Existing generated trees recalculate leaf distance when
neighbor/scheduled updates occur; newly generated trees receive correct support immediately. Runtime verification
must establish cleanup/stability in a fresh Swamp and behavior on save/reload.
