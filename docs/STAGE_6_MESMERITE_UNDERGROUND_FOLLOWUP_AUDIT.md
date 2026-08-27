# Stage 6 Mesmerite Underground Read-Only Follow-up

## Question

Runtime has confirmed Mesmerite/Illunite fissures, but no separate underground
Mesmerite deposit matching older showcase presentation has been identified.
This audit makes no production worldgen or resource change.

## Final 1.20.1 reachability

Final source registers three custom feature types and ships three configured
and placed pairs:

- `biomemakeover:fissure` / `dark_forest/mesmerite_fissure`
- `biomemakeover:mesmerite_boulder` / `dark_forest/mesmerite_boulder`
- `biomemakeover:mesmerite_underground` / `dark_forest/mesmerite_underground`

Only `dark_forest/mesmerite_fissure` is added to Dark Forest biome generation,
at `LOCAL_MODIFICATIONS`. The boulder and underground placed features are not
referenced by the final biome modifier or by another reachable configured or
placed feature. Their JSON must still decode, which is why Runtime Remediation
1 correctly registered their custom `Feature<?>` types, but packaged data and
registration do not make them naturally reachable.

The bundled pinned reference is a final source snapshot without upstream Git
history, so it cannot date exactly when the two dormant placements ceased to be
injected. Their retained implementations/resources and showcase presentation
are evidence of earlier intent, not evidence of final-release reachability.

## What the active fissure generates underground

The active placed feature starts at `WORLD_SURFACE_WG` with rarity 1/22. Each
horizontal fissure segment scans up/down five blocks for its floor, then clears
downward for that segment's sampled height. Initial height is 8–15; subsequent
segments apply the released -8..3 offsets. At increasing depth it places the
Mesmerite/Illunite wall provider around the opening and a second Mesmerite
depth layer behind that wall. Budding Illunite and oriented buds/clusters are
then placed from eligible alternate positions.

Consequently the final reachable underground component is the downward body
and lining of a surface-origin fissure. It is not a separately distributed
underground ore/deposit pass. Runtime may show deep narrow shafts and buried
wall material immediately around them while never exposing a standalone
`mesmerite_underground` formation in caves.

## Current 1.21.10 disposition

The current injection, rarity, heightmap, provider data, downward clearing,
wall/depth placement, and crystal pass are present. The implementation is
capable of producing the final fissure's underground depth. No defect was found
that selectively disables the below-surface loop while allowing the visible
surface fissure to generate.

Classification:

| Evidence | Disposition |
|---|---|
| Active fissure and its downward body | Final-release parity; present |
| Natural `mesmerite_underground` placement | Dormant/unreachable in final source |
| Natural `mesmerite_boulder` placement | Dormant/unreachable in final source |
| Showcase standalone underground presentation | Earlier/showcase behavior not proven reachable in final 1.20.1 |

No separate Stage 6 production remediation is recommended without new final
source reachability evidence or a controlled result showing the active fissure
fails to carve/place below its surface origin.
