# Stage 5 Runtime Remediation 1

## Evidence baseline

This checkpoint starts at `f9f03a61db5e43780a6284c4979a67881ad0113d` and uses released BM
1.20.1-1.11.4 (`2f314c0596af095a4890995a465f308f69476b4a`) as its behavior source.
It was reviewed against `PORTING_ENGINEERING_RULES.md`. The first Prism run established successful fresh Swamp and
Mangrove Swamp generation, tree/flora placement, peat tilling, water bonemeal ecology, mature branch shearing, and
natural spawn reachability for all three Stage 5 mob families.

## Runtime observations and dispositions

### Dragonfly environmental deaths — remediated, runtime verification required

Released Dragonflies implemented `FlyingAnimal`, disabled fall damage and fall checking, and assigned explicit
water/fire/fence path maluses. The Stage 5 translation omitted those contracts and replaced the released flying
wander/navigation behavior with a generic modern flying goal. This exposed them to fall damage and water routes that
the released mob rejected. The no-fall hooks, path maluses, released 10 health, ground/light spawn predicate, and
flying identity are restored. The exact observed damage source was not logged, so the remediation remains a targeted
source-supported fix requiring Prism confirmation rather than a claimed runtime pass.

### Decayed water behavior — remediated, runtime verification required

Released Decayed deliberately seek water, switch between land/water navigation, and do not convert in water. Water
entry is therefore historical, not a goal-selection defect. Minecraft 1.21.10 determines underwater breathing from
`minecraft:can_breathe_under_water` on the entity type. A custom type does not inherit the vanilla Drowned ID's tag,
even when its Java class extends `Drowned`; this caused the custom Decayed type to drown. The Decayed ID is now added
to that vanilla tag. No effect, generic immunity, or AI/balance change was introduced.

### Baby Decayed vegetation layer — fixed statically

The outer layer always baked/submitted `DROWNED_OUTER_LAYER`. It now maintains both vanilla 1.21.10 adult and baby
outer models and selects from `ZombieRenderState.isBaby`, matching the released body's baby transform without changing
dimensions or hitboxes.

### Decayed loot — converted

Released drops are 0–2 rotten flesh plus Looting 0–1, and a player-kill-only slime ball at 5%, increasing by one
percentage point per Looting level. `looting_enchant` is translated to `enchanted_count_increase` with the Looting
enchantment. `random_chance_with_looting` is translated to `random_chance_with_enchanted_bonus` with unenchanted 0.05,
level-one 0.06, and +0.01 thereafter. All packaged BM loot is rejected by validation if either obsolete construct
remains.

### Faster Farmland — fixed

The historical criterion is a hoe used on a resulting Peat Farmland block. Its 1.21.10 block predicate key is
`blocks`, not `block`, and modern `match_tool` expresses an item tag as `items: "#minecraft:hoes"`, not `tag`.
Both were translated. A Decayed spawn egg no longer satisfies the criterion; actual peat tilling retains the trigger.

### Lightning Bug — renderer and released illumination restored statically

The released base model is empty. Rendering is performed by two separately baked translucent models: a colored 2px
inner cube and a white 4px outer cube, with pulsing scale and full block light. Stage 5 incorrectly merged both into a
single ordinarily textured cube, producing the blue box. Separate modern render layers, full-bright submission,
pulsing scale, historic texture, and alternate first-tick companion creation are restored.

Released "illumination" is not dynamic world lighting. It consists of full-bright entity rendering and two animated
Lightning Spark particles approximately every 200 ticks. The released `lightning_spark` particle ID, ten bolt frames,
and 1.21.10 `SingleQuadParticle` provider are restored. Client-local emission preserves the released visual result
without adding a packet whose only purpose was client particle creation.

### Willow/Cypress leaf drops — source-confirmed; unchanged

Both released loot tables drop their leaf block only with shears or Silk Touch. Ordinary breaking uses released
Fortune-dependent sapling chances (5%, 6.25%, 8.333%, 10%) and stick chances/counts. Packaged resources match those
tables. The reported leaf-block drop is historically valid when shears/Silk Touch were involved; ordinary-hand/tool
behavior remains a focused retest item.

### Water Lily and Small Lily Pad visuals — tint migration fixed

The white/pink Water Lily flower fan is the released texture. Its underlying vanilla lily-pad plane, and the Small
Lily Pad models, use tint index 0 and historically had a biome foliage provider with Swamp color shifts. Stage 5 copied
the assets but omitted that provider, producing white/light planes. Modern block color providers restore the historical
biome tint. Water Lily artwork itself was not recolored.

Small Lily Pad natural clustering is historical: its `pads=0..3` block states represent progressively larger visual
groups and are equally selected by the released weighted patch. No placement mechanic was changed in this checkpoint.

### Peat distribution — source-confirmed; unchanged

The released placed feature has in-square, ocean-floor heightmap and biome modifiers with no extra rarity/count
modifier. Its custom feature makes local peat/mossy-peat patches subject to water/ground checks. Current configured,
placed and injection resources match. A small exploration sample not finding a patch is not evidence for changing
density.

### Willow beehive placement — source-confirmed; unchanged

The released Willow tree config uses the vanilla beehive decorator at probability 0.02. Attachment candidates derive
from the generated trunk positions supplied to the decorator, so a low attachment is possible. The current config and
decorator order match; no placement tuning was made.

### Water Lily dye recipe — ownership corrected

`magenta_bud` is a registered released item, is reachable through the Water Lily recipe, and crafts into magenta dye.
It was incorrectly omitted from Stage 5. The historical ID, deterministic palette-derived
texture, item model/definition, translation, Water Lily recipe, and dye recipe are restored additively.

### Peat Composter advancement — deferred correctly

`create_peat` requires the custom `biomemakeover:peat_compost` trigger registered by the deferred Peat Composter
system. Its broad Stage 5 resource include was leakage. It is removed from the package and remains Stage 9-owned; no
placeholder trigger or block was introduced. `create_peat_farmland`, `obtain_peat`, and `obtain_dried_peat` remain.

## Validator additions

- Exact Stage 5 particle ID/resource contract.
- Recipe references to absent BM item IDs fail.
- Advancement custom triggers must be registered; obsolete `match_tool.tag` fails.
- Obsolete Looting functions/conditions fail across all packaged BM loot.
- Dragonfly no-fall/water avoidance, Decayed underwater tag, separate Lightning Bug render layers/full-bright, and
  Lightning Spark particle packaging are asserted.

## Runtime status

Static/package validation cannot close Stage 5. Prism must retest Dragonfly survival, Decayed water behavior and baby
layer, Decayed loot and Faster Farmland, Lightning Bug layered rendering/pulse/sparks, and lily/pad biome tint. Dedicated
server, save/reload, multiplayer, and existing-world-copy validation also remain open.
