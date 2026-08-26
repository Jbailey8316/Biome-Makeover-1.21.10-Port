# Showcase-Informed Dark Forest Preservation Audit

Audit date: 2026-08-25

This is an audit-only checkpoint. It changes no production code, resources, registries, or stage ownership. The
strict parity authority is released Biome Makeover 1.20.1-1.11.4 at commit
`2f314c0596af095a4890995a465f308f69476b4a`. Showcase material is evidence that prompted source tracing, not a
reachability authority.

Classification vocabulary is fixed to: `PASS`, `PARITY MISSING`, `PARITY PARTIAL`, `HISTORICAL/DISABLED`,
`SHOWCASE-ONLY/UNCONFIRMED`, `DEFERRED BY EXISTING STAGE OWNERSHIP`, and `MYTHAS CANDIDATE`.

## 1. Executive findings and ownership

- Stage 6 owns Dark Forest physical content and exact worldgen: Ancient Oak, Ivy/Itching Ivy/Moth Blossom,
  Foxglove, Black Thistle, Wild Mushrooms, Mesmerite, and the Illunite formation embedded in the Mesmerite fissure.
- Stage 7 owns released Owl reconciliation. Current Mythas Owl behavior must remain preserved and untouched until
  that focused checkpoint.
- Stage 8 owns the complete Rootling and Moth ecosystems, including Rootling agriculture/foods and Moth brewing.
- Stage 9 owns the Altar, curse engine, menus, block entity, packets, and functional Tapestry foundation.
- Stage 11 owns the custom Mansion structure/layout/templates/processors.
- Stage 12 owns Mansion combat/progression: Adjudicator, Mimic, Stone Golem, cladding, Altar/curse progression,
  rewards, and related advancements.
- The final source contains no terrain-noise or carver replacement for Dark Forest. It injects features into vanilla
  `minecraft:dark_forest`.

## 2. Feature disposition matrix

| Feature | Showcase observation | Final 1.20.1 source status | Final reachability | Current 1.21.10 status | Dependencies | Classification | Action required | Notes |
|---|---|---|---|---|---|---|---|---|
| Dark Forest selector | Complete biome makeover | Tag contains vanilla `minecraft:dark_forest` | Natural | Existing partial modifier code | Fabric biome modification | PARITY PARTIAL | Stage 6 exact injection/removal audit | No custom biome/noise settings |
| Ancient Oak wood family | Major new tree/wood | Full Taniwha wood family plus leaves/sapling and boat pair | Crafting, trees, saplings | Most blocks/resources present; signs/boats and proven family behavior incomplete | Stage 2 family contract | PARITY PARTIAL | Stage 6 family completion; boats stay deferred | Leaves/sapling are separate from factory |
| Large Ancient Oak | Large canopy tree | 2x2 mega sapling feature; custom 2x2 trunk placer | Natural selector and 2x2 sapling | Config/resource and partial local trunk support exist; exact injection/runtime unproved | Custom trunk placer | PARITY PARTIAL | Restore/validate in Stage 6 | Base height 10, random additions 2 and 14; fancy foliage |
| Small Ancient Oak | 1x1 Ancient Oak | `ancient_oak_small`, fancy trunk/foliage and Ivy decorator | Natural selector and single sapling | Data present but full chain unproved | Ivy decorator | PARITY PARTIAL | Stage 6 | Selector chance 0.10 |
| Small Dark Oak | 1x1 Dark Oak | `dark_oak_small`, vanilla blocks with fancy trunk | Natural selector | Data absent/incomplete as effective chain | Vanilla Dark Oak blocks | PARITY MISSING | Restore exact configured/placed chain in Stage 6 | Selector chance 0.20 |
| Tree selector | Mixed tree silhouettes | Random selector: small Ancient 0.10, small Dark 0.20, large Ancient 0.05, default vanilla checked Dark Oak | Natural, three attempts/chunk | Current modifier does not prove exact selector parity | All tree features | PARITY PARTIAL | Exact Stage 6 chain | `OCEAN_FLOOR`, max water depth 0 |
| Ivy | Multi-face, vine-like, non-climbable | Six face booleans, full-face support, random spreading and density limit | Tree decorator, Mansion markers, player placement/spread | Block absent from current registry | Stage 6 tree/Mansion | PARITY MISSING | Restore Stage 6 block/mechanics/worldgen | It is intentionally not added to climbable tags |
| Itching Ivy | Hazardous flowering-Ivy system | Separate multi-face block, 0.5 speed factor, scheduled spreading, bonemeals into Moth Blossom | Natural top-layer feature and player propagation | Existing block is partial/inaccurate relative to final multi-face contract | Moth Blossom | PARITY PARTIAL | Source-faithful Stage 6 remediation | Final canonical ID is `itching_ivy` |
| Moth Blossom | Flowering functional Ivy | Separate six-face block with one `blossom` direction, 0.5 speed factor, blossom particles, spreads Itching Ivy | Natural feature seed, bonemeal conversion, Moth attraction | Missing | Itching Ivy, Moth | PARITY MISSING | Restore physical block in Stage 6; Moth behavior Stage 8 | Natural feature places one blossom plus ten Ivy attempts over radius 5.64 |
| Black Thistle | Two-block Weakness-associated flower | Custom tall flower with collision behavior; black dye recipe and lower-half loot | Natural flower selector and player use | Present with custom current behavior; exact source parity not runtime-proven | Flower mixture | PARITY PARTIAL | Report-only now; Stage 6 compare behavior | Do not rewrite accepted port during audit |
| Foxglove | Two-block flower | Tall flower, purple dye recipe and lower-half loot | Natural flower selector | Present; worldgen/resource parity incomplete | Flower mixture | PARITY PARTIAL | Complete Stage 6 chain | No special status effect found |
| Wild Mushrooms | Multiple crossed variants | One block with historical variant models; random patch | Natural Dark Forest and Mushroom Fields | Visual/model parity accepted; Dark Forest injection exists | Cross-biome resource | PASS | Preserve; validate Stage 6 distribution only | Do not reopen accepted geometry |
| Grass/tall grass/flower mixture | Dense forest floor | Grass, tall grass and weighted flower selector injections | Natural | Incomplete exact final modifier chain | Vanilla and BM flowers | PARITY PARTIAL | Stage 6 exact data translation | Tall grass rarity 1/3; flower noise count then rarity 1/3 |
| Mesmerite family | Crystal-rock formations | Mesmerite and polished Mesmerite, each base/slab/stairs/wall | Worldgen, mining, crafting/stonecutting | Families and underground experiment exist | Fissure/Illunite | PARITY PARTIAL | Stage 6 exact family and fissure parity | Existing underground-only injection is not the released full chain |
| Mesmerite fissure | Above/underground spire-like formation | Custom fissure at local-modification step; surface rarity 1/22; 8-15 height, offsets and 8-16 count providers | Natural Dark Forest | Missing full final feature; current underground Mesmerite is only a slice | Mesmerite/Illunite blocks | PARITY MISSING | Stage 6 P0 | Composes surface fissure, boulder and underground behavior; no noise/carver change |
| Illunite formation | Glowing crystal in Mesmerite | `illunite_block`, `budding_illunite`, four bud/cluster stages and `illunite_shard`; growing budding block | Natural fissure, mining, mansion loot | Entire family absent | Mesmerite fissure, Stage 9 Altar, Rootling Stunt Powder | PARITY MISSING | Restore formation in Stage 6; functional consumers later | Canonical spelling is **Illunite** |
| Owl | Parrot/wolf-like companion with babies/glowing eyes | Released tameable shoulder-riding flyer; concrete contract below | Natural Dark Forest, tame/breed/hunt | Extensive Mythas implementation exists and diverges | Stage 6 biome; Stage 11 marker | DEFERRED BY EXISTING STAGE OWNERSHIP | Stage 7 reconciliation only | No audit-time code changes |
| Rootling | Social sentient plant mob | Complete synchronized six-flower/shearing/social/regrowth system | Natural weight 40, groups 2-6 | Absent | Stage 6 flora; Stage 8 | DEFERRED BY EXISTING STAGE OWNERSHIP | Restore complete ecosystem in Stage 8 | Not reducible to mob plus drops |
| Rootling agriculture | Seeds become Rootling | Four-age crop on farmland, brightness >=9, bonemealable; reaching max age removes crop and spawns randomized Rootling | Seeds drop naturally from Rootlings | Absent | Rootling entity/items | DEFERRED BY EXISTING STAGE OWNERSHIP | Stage 8 vertical restoration | `getBreedOffspring` returns null; crop is reproduction path |
| Rootling buds | Shearing/dye loop | Six colors: blue, brown, cyan, gray, light blue, purple | Shearing loot then dye recipes | Absent | Rootling variants | DEFERRED BY EXISTING STAGE OWNERSHIP | Stage 8 | Final loot is 2-4 buds, contradicting showcase recollection of 1-3 |
| Bulbus Root foods | Rootling food/drop | `bulbus_root` nutrition 2/saturation 0.6; `roasted_bulbus_root` nutrition 5/saturation 0.8; furnace/smoker/campfire recipes | Rootling death drop; fire death can furnace-smelt it | Absent | Rootling | DEFERRED BY EXISTING STAGE OWNERSHIP | Stage 8 | Canonical spelling is **Bulbus**, not Bulbous |
| Stunt Powder | “Anti-aging” powder | Illunite Shard + Bulbus Root gives two `stunt_powder`; marks a baby/always-baby `Stuntable` entity as permanently stunted | Craftable when dependencies exist | Absent | Illunite, Rootling, Stuntable entities | DEFERRED BY EXISTING STAGE OWNERSHIP | Stage 12 or dependency-resolved owner | Prevents aging; does not de-age adults |
| Moth | Hostile canopy/light mob | Flying monster, hostile to Players, avoids Owls, seeks light >10 or `moth_attractive`, hugs attraction up to timeout, custom bite/flap sounds | Natural weight 90, groups 2-3; no restriction placement | Absent | Moth Blossom/Owl | DEFERRED BY EXISTING STAGE OWNERSHIP | Stage 8 | Showcase “canopy” is presentation; source spawn placement is `NO_RESTRICTIONS` |
| Moth Scales | Drop/brewing resource | Entity loot 0-2 plus Looting 0-2 | Natural Moth kills | Absent | Moth | DEFERRED BY EXISTING STAGE OWNERSHIP | Stage 8 | No player-kill condition in final loot table |
| Nocturnal effect/potion | Prevents Phantoms | `nocturnal`; every second resets player `TIME_SINCE_REST`; potion 72,000 ticks, long potion 144,000 | Awkward + Moth Scales; Redstone extension | Absent | Moth Scales | DEFERRED BY EXISTING STAGE OWNERSHIP | Stage 8/13 | It prevents insomnia-driven spawning indirectly; supports standard potion containers through vanilla brewing |
| Phantom Membrane recipe | Moth/Ectoplasm crossover | One Ectoplasm + three Moth Scales -> one Phantom Membrane | Reachable only after both ecosystems | Absent; Ectoplasm missing | Stage 8 + Stage 10C | PARITY MISSING | Restore with dependency gating, not classify dead | PARITY PRESENT BUT DEPENDENCY MISSING |
| Altar | Cursed enchanting | Craft uses book, two Illunite Shards, two Mesmerite and Crying Obsidian; inventory/menu/timed BE consumes Illunite fuel | Craftable final system | Absent | Stage 6 crystals, Stage 9 functional system | DEFERRED BY EXISTING STAGE OWNERSHIP | Stage 9 then Stage 12 acceptance | Not a vanilla enchanting-table replacement |
| Ten BM curses | Cursed enchanting expansion | Ten registered, configurable treasure curses listed below | Altar can select compatible curses; books/items supported | Absent | Altar, 1.21.10 enchantment components | DEFERRED BY EXISTING STAGE OWNERSHIP | Stage 12 | Major API migration risk |
| Dark Forest disc | Unique record | `red_rose_music_disk`, sound `red_rose`, comparator value 2, duration metadata 135; advancement present | Mansion good/dungeon-good chest loot | Absent | Stage 11 Mansion loot | DEFERRED BY EXISTING STAGE OWNERSHIP | Restore with Mansion reward chain | Not proven boss loot; packaged sound declaration/audio chain must be audited at implementation |

## 3. Exact Dark Forest worldgen contract

Final source injects seven placed features into `#biomemakeover:dark_forest`:

1. `dark_forest/grass` at `VEGETAL_DECORATION`.
2. `dark_forest/tall_grass` at `VEGETAL_DECORATION`.
3. `dark_forest/flowers` at `VEGETAL_DECORATION`.
4. `dark_forest/itching_ivy` at `TOP_LAYER_MODIFICATION`, rarity 1/4.
5. `dark_forest/trees` at the unusual `UNDERGROUND_ORES` step, count 3.
6. `dark_forest/wild_mushrooms` at `UNDERGROUND_ORES`, count 2.
7. `dark_forest/mesmerite_fissure` at `LOCAL_MODIFICATIONS`, rarity 1/22 and `WORLD_SURFACE_WG`.

There are 11 configured and 11 placed Dark Forest resources. The unusual steps and selector probabilities are part of
the final contract and must not be “cleaned up” without evidence. The final feature system does not replace Dark
Forest terrain noise, density functions, or carvers.

Ancient Oak saplings use an `AbstractMegaTreeGrower`: a 2x2 arrangement selects the large custom-trunk feature and a
single sapling selects `ancient_oak_small`. The final large tree has a 2x2 trunk, branch pairs and fancy foliage; the
small Ancient Oak and small Dark Oak use fancy trunk/foliage placers. Both tree configurations carry a 0.002 vanilla
beehive decorator probability; small Ancient Oak additionally uses the Ivy decorator.

## 4. Owl final-source discrepancy audit (report only)

| Contract | Final 1.20.1 | Current port | Disposition |
|---|---|---|---|
| Spawn | Dark Forest, weight 20, group 1-4, ON_GROUND/MOTION_BLOCKING; Grass or Leaves below; raw brightness >2; no time check | Current canopy/night-specific checks | Later Stage 7 discrepancy |
| Attributes | Flying speed 0.8, health 6, movement 0.4, attack 2; tamed health 20/attack 4 | Current port has substantial custom tuning | Compare in Stage 7 |
| Food | Any edible meat drives temptation, breeding, taming and healing | Raw Rabbit-centered; raw Chicken healing; narrower diet | Later discrepancy |
| Taming | Edible meat, no active target, 1-in-3; assigns owner and sits | Rabbit-specific 1-in-3 | Later discrepancy |
| Hunting | Untamed targets tag: Rabbit, Chicken, Silverfish, Endermite, Bat, Toad, Blight Bat, Dragonfly, both Lightning Bugs | Night Chicken focus; no wired Rabbit hunt | Later discrepancy |
| Following/perching | Custom flying owner-follow and opportunistic scan for clear space over Leaves/Logs | Remembered nest/tree/day-night behavior added | Preserve Mythas; reconcile later |
| Shoulder | Inherited `ShoulderRidingEntity`; exact runtime hook needs verification | Current shoulder renderer still historically noted incomplete | Stage 7 runtime gate |
| Babies/breeding | Vanilla ageable child; child inherits owner/tame; ordinary BreedGoal | Babies and custom work present | Compatible core, Mythas overlay |
| Nests/eggs/sleep | None in final release | Nest, egg, home, sleep and wake systems present/partial | MYTHAS enhancement; preserve separately |
| Fleeing | No wild-player avoidance | Present | MYTHAS difference |
| Eyes | Emissive eyes always rendered | Night-conditioned eyes | Later parity discrepancy |
| Blink/head tilt | No explicit blink state/eyelid; normal model head rotation | Explicit blinking and personality animation | MYTHAS enhancement |
| Sounds | Idle, hurt, death only | Eight events including hoot/contact/alert/baby/takeoff | MYTHAS enhancement |
| Drops | 1-2 Feathers plus Looting; no head | Owl drop parity absent | Stage 7 missing parity |
| Persistence | Vanilla tame/owner/age/sit plus `OwlState` and `StandingState`; ATTACKING unused | Additional nest/schedule state | Preserve fields; migration review required |

The showcase phrase “parrot/wolf hybrid” resolves to shoulder inheritance, taming/owner defense/following/sitting,
breeding, flight/perching, and wild prey targeting. It does not prove nests, egg laying, sleeping, blinking, or a
night-only spawn contract in final 1.20.1.

## 5. Complete Rootling lifecycle

Rootlings are natural Dark Forest creatures (weight 40, groups 2-6), 10 health and 0.25 movement speed. Six flower
variants are synchronized by integer index and persisted with `HasFlower`, `FlowerType`, `GrowTime`, and
`ActionCooldown`. Final colors are blue, brown, cyan, gray, light blue, and purple.

Shears operate only while flowered, cost one durability, play Sheep shear sound, set the Rootling bald, and invoke the
variant loot table. Those tables drop **2-4** matching buds. The showcase 1-3 count is not final-source parity.
Regrowth is set to a random 600-1200 ticks; water or rain gives a 1-in-5 chance each server tick to decrement a second
time. Bonemeal on a bald Rootling is consumed and has a 1-in-3 immediate regrowth chance. Regrowth randomizes the
flower. All state persists across reload.

Personality is executable AI, not cosmetic lore:

- Priority 2 seeks rain/visible sky while raining.
- Avoids any living entity holding Shears within 8 blocks, with afraid sound cadence.
- Bone Meal temptation at speed 1.
- Dance/belly-bump goal pairs with another available Rootling within 8 blocks, lasts up to 60 ticks and jumps while
  close.
- Follow goal selects a nearby non-Rootling `Animal`, follows for up to 120 ticks and stops inside two blocks.
- Flower inspection scans nearby flower-tag blocks and looks/moves toward one for up to 200 ticks.
- Actions share a 500-tick cooldown.

Rootlings do not breed conventionally (`getBreedOffspring` returns null). Their death table drops 1-2 Rootling Seeds
plus Looting and one Bulbus Root plus Looting; a burning Rootling furnace-smelts the root. Seeds place the five-state
`rootling_crop` (ages 0-4) on farmland. It requires brightness 9 for random growth, supports bonemeal through crop
behavior, and when a transition reaches maximum age the crop removes itself and spawns a Rootling with a randomized
flower and upward motion. This is the complete reproduction loop.

## 6. Moth, brewing, and cross-biome dependency

Moths are final-reachable hostile monsters: weight 90, groups 2-3, `NO_RESTRICTIONS` placement and motion-blocking
heightmap predicate. They attack Players, avoid Owls, fly, bite, and search within five blocks for either emitted light
greater than 10 or blocks in `moth_attractive` (which includes Moth Blossom). They hover/hug the attraction, change
offsets, and abandon it after extended unsuccessful hugging. Their renderer has flight animation and a client loop
flap sound.

Moth loot is 0-2 Moth Scales with Looting 0-2 and no player-kill condition. Awkward Potion + Moth Scales produces
Nocturnal for 72,000 ticks; Redstone produces 144,000 ticks. The effect resets `TIME_SINCE_REST` once per second, so
it prevents insomnia-driven Phantom spawning rather than cancelling Phantom entities or targets directly.

One Ectoplasm plus three Moth Scales crafts one Phantom Membrane. This is released parity with a missing Stage 10C
Ghost/Ectoplasm dependency, not dead content.

## 7. Mesmerite, Illunite, Stunt Powder, and cursed enchanting

The final fissure is a mixed formation, not “Mesmerite ore only.” It uses Mesmerite as depth material, noise-selected
Mesmerite/Illunite surface material, occasional Budding Illunite, and small/medium/large/full Illunite inner placements.
Illunite buds have light levels 5/7/13/15; Budding Illunite randomly grows the four-stage cluster chain. Full clusters
yield Illunite Shards under their loot contract. The shard is also present in Mansion standard/good loot.

`illunite_shard + bulbus_root -> 2 stunt_powder`. Stunt Powder works only on a `Stuntable` entity that is a baby (or
reports always-baby) and not already stunted. It sets persistent stunted state, consumes the powder and emits warped
spores. “Anti-aging” is showcase shorthand for preventing juvenile growth.

The Altar recipe is Book + two Illunite Shards + two Mesmerite + Crying Obsidian. The functional block entity accepts
Illunite Shards through `curse_fuel`, runs a timed operation, and either turns a Book into a randomly cursed Enchanted
Book or upgrades a compatible existing enchantment by one while adding a compatible random curse. It marks processed
items `BMCursed`, sets repair cost 39, respects exclusion/cannot-upgrade tags, and has a `strictAltarCursing` config.

Final BM registers ten configurable curses:

| ID | Max level | Final implementation surface |
|---|---:|---|
| `decay_curse` | 5 | Breakable equipment; durability-decay behavior through mixins |
| `insomnia_curse` | 5 | Armor; insomnia behavior on tick |
| `conductivity_curse` | 5 | Armor; conductivity/lightning-related tick behavior |
| `enfeeblement_curse` | 5 | Vanishable equipment; -2 max health per level via attribute handling |
| `depth_curse` | 3 | Boots; depth/water movement behavior |
| `flammability_curse` | 3 | Armor; fire vulnerability through entity mixin |
| `suffocation_curse` | 3 | Helmet; suffocation behavior through entity mixin |
| `unwieldiness_curse` | 3 | Weapons; -0.25 attack speed per level |
| `inaccuracy_curse` | 3 | Bows; projectile inaccuracy through Bow mixin |
| `buckling_curse` | 3 | Leggings; movement/armor behavior through mixins |

All are treasure-only, undiscoverable and untradeable by default, with cost range 25-50. Restoring this system in
1.21.10 is a high-risk semantic translation because enchantments are data/component-driven and the old item NBT,
attribute, ticking and mixin hooks cannot be copied mechanically.

## 8. Advancements tied to Dark Forest/ecology

Final authored advancements include `enter_dark_forest`, `ancient_oak`, `rootling_seeds`, `roasted_bulbus_root`,
`moth_scales`, `moth_blossom`, `nocturnal_potion`, `illunite_shard`, `altar`, and `dark_forest_disc`. The Mansion-side
advancements are catalogued in the companion Mansion audit. Current 1.21.10 has only Mesmerite recipe unlock
advancements for this theme; the released story/progression chain is missing.

## 9. Showcase vs final-release evolution

- Rootling petal quantity evolved or was narrated imprecisely: final data is 2-4, not 1-3.
- Stunt Powder does not reverse aging; it permanently prevents a supported baby from aging.
- Moth “canopy spawning” is not encoded as a canopy-only spawn predicate in final source.
- Owl showcase personality does not make current nests/eggs/sleep/blinking final parity. Those systems are absent
  from final 1.20.1 and remain Mythas additions.
- No disabled Rootling or Moth registration/spawn path was found. Both are final reachable.
- `OwlState.ATTACKING` is persisted but unused; it is historical dead state, not a missing active mechanic.

## 10. Dependency graph

```text
Stage 6 flora/worldgen
  Ancient Oak + Ivy + Moth Blossom + Mesmerite/Illunite
       |                    |                 |
       v                    v                 v
Stage 7 Owl           Stage 8 Moth      Stage 9 Altar
                            |                 |
                       Moth Scales       Stage 12 curses
                            |
        +-------------------+--------------------+
        |                                        |
  Nocturnal potion                  Ectoplasm (Stage 10C)
                                             |
                                  Phantom Membrane recipe

Stage 8 Rootling
  shearing -> six buds -> dyes
  death -> seeds -> crop -> new Rootling
  death -> Bulbus Root -> roasted food
  Bulbus Root + Illunite Shard -> Stunt Powder
```

## 11. Priority queue

### Dark Forest P0 — core biome/worldgen

1. Exact Stage 6 manifest/current-overlap inventory.
2. Ancient Oak complete safe family, leaves/sapling, tree codecs/trunk placer and selector.
3. Ivy/Itching Ivy/Moth Blossom physical block contracts.
4. Mesmerite fissure and complete Illunite formation.
5. Exact seven biome injections and all 11 configured/placed resources.
6. Flowers, groundcover and Wild Mushroom distribution.

### Dark Forest P1 — ecology/progression

1. Stage 7 released Owl parity reconciliation without deleting Mythas state.
2. Stage 8 Rootling entity, personality, shearing and crop lifecycle.
3. Stage 8 Moth entity, Moth Blossom attraction and Owl avoidance.
4. Stage 9 Altar functional chain.

### Dark Forest P2 — items/loot/brewing/advancements

1. Six Rootling buds/dyes, seeds, Bulbus foods and recipes.
2. Moth Scales, Nocturnal effect/potions and Phantom Membrane dependency.
3. Illunite loot, Stunt Powder and cross-entity persistence.
4. Theme advancement tree and recipe unlocks.

### Dark Forest P3 — presentation/minor parity

1. Exact tints, cutout layers, particles and sounds.
2. Entity animation/render-state parity and loop sound safety.
3. Potted/flora models, translations and creative ordering.

## 12. Recommended implementation order

1. Stage 6 source-derived registry/resource manifest and preservation diff.
2. Ancient Oak safe family and sapling/tree features, excluding boats.
3. Ivy/Itching Ivy/Moth Blossom and remaining flora.
4. Mesmerite/Illunite formation and exact biome injection order.
5. Stage 6 data completion and fresh-chunk runtime gate.
6. Separate Stage 7 Owl reconciliation.
7. Stage 8 Rootling vertical ecosystem, then Moth/Nocturnal.
8. Stage 9 Altar/Tapestry foundations.
9. Stage 11 Mansion structure.
10. Stage 12 cladding/construct/boss/curses and full progression.

## 13. Mythas candidates (not parity)

- Preserve existing Owl nest/egg/sleep/blink/personality work as a separately classified overlay.
- Suitable BM mob heads after parity freeze.
- Living World events using Dark Forest/Mansion systems.
- Living World integration for Witch progression.
- Modular structure infrastructure as a future Mythas tool, without changing BM structure ownership.
- Historical ecosystem revivals require separate reachability/design approval.

No Mythas candidate is authorized by this audit.
