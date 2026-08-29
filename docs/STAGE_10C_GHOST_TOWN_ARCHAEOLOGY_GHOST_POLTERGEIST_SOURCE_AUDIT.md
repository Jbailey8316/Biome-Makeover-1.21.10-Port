# Stage 10C source audit — Ghost Town and paranormal systems

## Status and authority

This is an audit-only record.  The final released Biome Makeover 1.20.1
source and packaged resources under `reference/Biome-Makeover-1.20` are the
authority; older material is provenance only.  No Stage 10C production code or
resource is enabled by this audit.  At audit start the port was `e3c4425`,
`main`, clean, and 0/0 against `origin/main`; the preservation tag remains
`d664cccf13ab65bddc7a3d30aa04254bb810e4f1`.

## Active final scope

The final release contains the following connected systems:

* **Ghost Town**: a vanilla `minecraft:jigsaw` structure in the Badlands,
  with four template pools and two custom processors.
* **Archaeology**: suspicious red sand is an active BrushableBlock replacement
  used by the Ghost Town processors, with the `archaeology/ghost_town` loot
  table.
* **Ghost**: a registered flying monster, spawned by Ghost Town's
  `spawn_overrides` (and summon/spawn-egg infrastructure), dropping Ectoplasm.
* **Ectoplasm**: an item which can convert a partially filled vanilla
  Composter into the Ectoplasm Composter.
* **Ectoplasm Composter**: an active block variant which produces Soul Soil and
  fires the `ectoplasm_compost` criterion.
* **Phantom recipe**: active shapeless Ectoplasm + three Moth Scales to one
  vanilla Phantom Membrane; this is the accurate meaning of the roadmap label.
* **Poltergeist**: active redstone-sensitive block/block entity and its
  server-side behaviour/particle payload; its recipe and advancements are
  active final content.
* Ghost Town music disc, loot, and the `ghost_town`, `badlands_disc`,
  `compost_soul_soil`, `poltergeist`, and `going_ghost` advancements.

The current port has none of these Stage 10C registrations/resources.  Peat
Composter is Stage 9A and is not the Ectoplasm Composter.

## Ghost Town contract

Canonical IDs are `biomemakeover:ghost_town` (structure),
`biomemakeover:ghost_towns` (structure set),
`biomemakeover:has_structure/ghost_town` (biome tag), and
`biomemakeover:ghost_town_loot` (processor type).  The structure JSON is a
vanilla jigsaw (`size: 3`, `max_distance_from_center: 80`,
`project_start_to_heightmap: WORLD_SURFACE_WG`, `start_height: absolute 0`,
`terrain_adaptation: beard_thin`, `use_expansion_hack: true`) at
`surface_structures`.  The structure set is random-spread, linear, spacing
32, separation 12, salt 6969, weight 1, with no exclusion zone or frequency
reduction.  The biome tag replaces with `#minecraft:is_badlands` and includes
the optional `#c:badlands` convention tag.

The root pool is `biomemakeover:ghosttown/centers`; it contains seven equally
weighted rigid road elements (`roads/street_01` through `street_07`).  The
other pools are `ghosttown/roads`, `ghosttown/buildings`, and
`ghosttown/decoration`; all elements are vanilla `single_pool_element`s and
use rigid projection.  Roads and buildings use the processor lists below.

`ghosttown_building` first assigns one of three Ghost Town chest loot tables,
then fills bookshelf contents, randomises BM brick replacements, and invokes
the historical suspicious-block replacement.  `ghosttown_roads` replaces
water with oak planks, sometimes replaces dirt paths with red sand, and then
performs suspicious-block replacement.  The local 1.21.10 implementation must
replace the Taniwha processor types rather than depend on Taniwha at runtime.

The structure's monster spawn override is Ghost, weight 150, group 2–4,
piece bounding box.  There is no configuration toggle, custom terrain
predicate, or structure-specific dimension restriction.  Existing generated
chunks are unchanged; fresh worlds and unexplored chunks use normal structure
generation.

### Template inventory

All final templates are under the old source path
`data/biomemakeover/structures/ghosttown/`.  A 1.21.10 port must package the
same bytes under singular `data/biomemakeover/structure/ghosttown/`; no plural
duplicate may remain.  The complete inventory (source byte size and SHA-256)
is:

| pool | template | bytes | SHA-256 |
|---|---|---:|---|
| center | crossroads_01 | 2172 | 1BE0C70D27C2CC691E9FDE85B4B8A84CCB87DA1FF48F06724F826E53BC8712EB |
| decoration | barrel_decoration | 354 | 56152CB811AE5894949C798F3932A587196D24973B65F686AEB5A446E5D7A0B6 |
| decoration | bell_decoration_1 | 576 | DBBB13795437144640AD7F68863250F8219ED8367D5070ACA5142C7E92A62721 |
| decoration | cactus_decoration | 418 | 7CCB9F8A61236E3847A70A56FF0B0568AFC4C74B890731ABADFF84BC1721B6C5 |
| decoration | hay_decoration, hay_decoration_2 | 327, 389 | D2703E30161C671EEEFB9A37FB493204AEFCF4469F7A38C3C2476F714C270C03, 987D2BF0D700F3BD496881F7551954333CC00E4505FA52A1383C2609B40997A0 |
| decoration | hay_well_decoration | 553 | 19C1D3A7245397E9CCD68124C9668362B18064C085CF8EAFD674509C8BA52545 |
| decoration | lamp_decoration, lamp_decoration_2 | 379, 356 | 011873D3A65A504C1D12F28E0F2C2BAFFAF6F24F4FF591E3AC924C5EBB4F4655, 43BBDC60B5DCA78230136499988E2F77DD5F96C0DEC1F380733446535DE30E51 |
| decoration | tree_decoration_1, tree_decoration_2 | 415, 537 | 6AB39A76CDD903143F6DC91FF46FDB72B5C2604736914A73E88EB339433277C0, 32F530B13726EB1B1DB4B83ED94977222204FBEA7AEBCD4FB3BC0136A04B7A11 |
| decoration | trough_decoration | 365 | 56BB9423A0DCB42B53540361C1A6287607124E0E1895267D4A80ED5BAAB3F685 |
| decoration | water_tower_1, water_tower_2, water_tower_3 | 1857, 6046, 5943 | F73008BB2A02E29766B31C9E7CC7B4FD363DFAEEF1851DAD89A2AE270808861E, 1FA1E4F557CD604720EF364851236916FEA3EB1876485A12C569C6B77EF8CF48, 05F032CA7B562451F9542A0DE43331E8673E5E29C04641C186063EFE8769514A |
| decoration | well_decoration | 662 | 38C487BB184A47E0427690D4FA5C1714F982CE36A797C611F8E86FEE619BEAB5 |
| houses | house_large_01..05 | 12520, 13614, 9516, 19106, 6987 | 816CBBE5906FC5CBD3A42A98F275D5B77F0ECDA75A548EBFC5F2CDBEC9A73102; 48E625C43A88957925D4C03C2EF92E9DA7C569E8A3CABD322772C2697F0272CC; E019E90A1095D430E60C4D8475EB162DF21E6263CA7FCD94CEB874DAF42D58D4; 0E79A8F95BC8A2D5BC3797A7B92636EF57BD13E7A8D3B39EF8C0EB85367A358D; 66CD074DDB6547C200FAC2D275A64B0DF9BCF8CFBCC7A700B9B746B924104DE6 |
| houses | house_medium_01..07 | 5329, 7070, 6902, 5468, 5106, 3319, 4826 | 8C1E41B3E42DFB0F4746F51F834F06F64CE3DA4BF2202E540CD750928CA762AF; E78821A041B88EE033A970E13E2CF918E406E49C024985F705BCCF72CC863890; 90B6D8048CBBC4C53937A11A21DD117D6573FAB58AD242BDB9C471665942371E; 869DB7F1E0D77ADD39C67726B390A94C8B97D25F7A3214C48953144F67EA2034; A607B71A4885E1F0FF8E6AD6B033BB4AB70B509B756F52502ECBCC27E0333F15; 286A73F9F43B2469BDB4A79BA449EFAFEAEAE8BDA98EAF75CE653F6AE1555A87; 4F099DEB0AB6502377A424AADBEA222C9C4A1C851184D872AED6FDBB628B469E |
| houses | house_small_01..15 | 2761, 2482, 2820, 3385, 3065, 1625, 1792, 1483, 1286, 1289, 2038, 2804, 1678, 1331, 2877 | (all preserved from the final source; exact per-file hashes are recorded by the audit inventory script) |
| roads | street_01..07 | 710, 749, 728, 718, 1012, 729, 726 | 32FFF222A1C9857AE68BB6688ABA0CA32A42219EF700E80EE708EB0E25065321; 19915DBA4EAB9A699F6BABEB9D6812C7124616B23E5D9A6A96C4E1EE1DEEC22F; 9553DE551B6F38901A4A0E94172ED4E2BDEA089B3054AE1DDAC2920994590035; B8BAB68375F3FD5FAD9CF399923F387B445F91582B6CA544275B0352D53FEEA2; 55A61D7D48DD7285ADDC608968871CB2725494CA29B391C4FE68C917A54E1521; 38C27D87E4DDA153D9960E5A777D0C5FF459CAB877E8F584BEE78DF6B8325ACE; E7CBC308661EFBB24C0BFDD51B9DAB75AE837484C701A3CA6B8AEEC2221FB53A |

There are 40 templates: one center, 17 decorations, 27 houses, and seven
roads.  Source NBT versions are mixed (2580/2584/3098/3454); the future
validator must DataFix and load every template through the actual
1.21.10 `StructureTemplateManager`, checking dimensions, palettes, markers,
and block/entity compatibility rather than accepting a binary parse alone.

## Archaeology and loot

The building and road processors replace lime concrete/red sand with
`biomemakeover:suspicious_red_sand` at the audited probabilities (15% in
building lime-concrete targets, 2.5% in road red-sand targets), assigning
`biomemakeover:archaeology/ghost_town`.  The table is a one-roll archaeology
pool containing three BM pottery sherds, iron, gold, Crude Fragment, nested
horse-armour and junk tables, Ghost Town music disc, and an optionally
enchanted damaged leather boot.  It is owned by Ghost Town; it is not a
standalone Stage 10C structure.  Modern translation should use the native
BrushableBlockEntity/archaeology loot component and the falling suspicious
block path, with a local processor replacing Taniwha's suspicious-block
processor.

The three Ghost Town barrel tables are chest tables: `loot_0` (2–6 rolls),
`loot_1` (1–5), and `loot_2` (2–6), with the exact final entries/functions in
the reference JSON.  They include Ghost Town music disc, ordinary resources,
damaged/enchantable tools, and Cowboy Hat where specified.  No Ectoplasm,
Poltergeist, or Stage 12 Witch-death drop is inserted by these tables.  A
future implementation validator must check every entry, nested table, modern
potion/item component, and all BM references.

## Ghost

`biomemakeover:ghost` is a `MONSTER`, 0.6×1.95, tracking range 12,
can-spawn-far-from-player entity.  It uses Monster attributes, custom no-gravity
flight movement, Float/Poltergeist/charge/fly/look goals, hurt-by and nearest
player targeting, persistent anger and reinforcement behaviour.  It is
spawned by the Ghost Town structure override; the final source does not add a
free Badlands biome spawn.  The summon egg is visible and uses the audited
colours.  Ghost has charge/ambient/angry/hurt/death sounds, translucent
normal/angry textures and model, is immune to the final `ghost_immune_to`
damage tag, and drops 0–1 Ectoplasm plus Looting's 0–1 additional count.
Anger/home position are saved; no custom despawn contract beyond the source
Monster/structure lifecycle is added.

The Ghost's Poltergeist goal invokes `PoltergeistHandler` four times per tick
within range 10.  This is the entity-to-Poltergeist dependency; it does not
make Poltergeist an entity.

## Ectoplasm and composting

`biomemakeover:ectoplasm` is a normal stackable item.  Using it on a partially
filled vanilla Composter converts that block to `ectoplasm_composter`, keeps
the level, consumes one Ectoplasm outside creative, and plays the vanilla fill
event.  The Ectoplasm Composter accepts vanilla compostables, produces Soul
Soil at level 8, exposes it downward for extraction, resets to a vanilla
Composter, and fires `ectoplasm_compost` when emptied.  The final compostable
resource contains 41 entries; notable BM probabilities include Glowshroom and
plant items (0.7/0.8), Glowshroom blocks/stem (0.9), cactus variants
(0.15–0.4), swamp plants (0.2–0.8), and reed-thatch variants (0.266–0.8).
This is ordinary Composter registration, not Peat Composter behaviour.

## Phantom recipe

`biomemakeover:phantom_membrane` is active, shapeless, group `planks`,
ingredients Ectoplasm + three `biomemakeover:moth_scales`, output one
`minecraft:phantom_membrane`.  It has no special unlock or advancement in the
final resource.  The `phantom_spirit` potion is a separate registered potion,
not a hidden Ghost Town dependency.

## Poltergeist

Poltergeist is a block (`biomemakeover:poltergeist`) plus
`poltergeist` block entity, not an entity.  It has an `ENABLED` state (default
true), light 7 when enabled, custom cauldron-like shape, redstone toggle and
server ticker.  While enabled its block entity invokes the source
`PoltergeistHandler`, which acts on tagged doors/buttons/trapdoors and other
registered block behaviours, plays positional action/toggle sounds and sends
the narrow S2C particle effect.  Entering its interaction volume applies the
Possessed potion, with the source duration/level growth limits, and triggers
`poltergeist_yourself`; inventory acquisition triggers `poltergeist`, followed
by `going_ghost`.  The block drops itself under the normal survives-explosion
condition and has the audited recipe (three Ectoplasm, three Phantom Membrane,
Soul Soil, Cauldron).  No Altar/Stage 10B networking or client-selected action
is involved.

## Advancements

Active final advancements are `biomemakeover/ghost_town` (location inside the
structure, parent enter_badlands), `badlands_disc` (inventory Ghost Town disc),
`compost_soul_soil` (custom ectoplasm-compost criterion), `poltergeist`
(inventory block), and `going_ghost` (custom player-inside criterion).  The
Ghost Town location predicate uses the old singular `structure` key; a modern
port must translate it to plural `structures`, as Stage 10B did for Sinking
Feeling, or it silently becomes unconditional.  `going_ghost` has an empty
requirements array in the source and is custom-triggered.  Other similarly
named content (`scuttler_tail`, `cursed_hat`, Mansion) belongs to other stages.

## Current-port gap and dependency graph

Current HEAD contains no Ghost Town structure/set/pools/templates/processors,
Ghost/Ectoplasm/Poltergeist registrations, suspicious red sand, Ghost Town
loot, archaeology table, Phantom recipe, Stage 10C advancements, sounds,
particles, or related models/textures.  Existing Stage 1–10B infrastructure
supplies registries, Badlands/swamp data, Moth Scales, Phantom Membrane,
Composter/Peat Composter patterns, custom payload patterns, loot/advancement
validators, and singular structure-path lessons.  Nothing is partially
restored or colliding; no Taniwha runtime dependency is present in the port.

Recommended order is: item/block/entity registries and common tags; modern
Brushable/composter/Poltergeist behaviour; Ghost and client assets; local
structure processors; templates and jigsaw data; loot/advancements/recipe;
then packaged-template and integrated-world validation.  Witch quests,
primary Witch Hat drop, Mansion, Beach, and Stage 10C archaeology/Ghost Town
dependents outside this closure remain excluded.  Ghost Town archaeology is a
genuine Stage 10C dependency, not Stage 11.

## Historical and excluded content

Older/showcase-only Badlands fossils, Saguaro/Tumbleweeds variants, Scuttler
ambience, alternate Ghost behaviour, and experimental cave/noise systems are
not active Stage 10C parity unless the final 1.20.1 tree proves otherwise.
Likewise no Stage 10C audit authorizes Mansion templates, Witch quests,
Adjudicator systems, Beach ecology, or Mythas polish.  These are labelled
historical or future enhancement candidates, not implementation scope.

## Migration risks and mitigations

**High:** custom Structure/processor registry bootstrap, singular template
path, jigsaw/template-pool codec changes, BrushableBlockEntity/suspicious
falling persistence, Ghost translucent renderer and AI/navigation, and old
advancement predicates.  Mitigate with actual registry bootstrap tests,
StructureTemplateManager/DataFix loading of every NBT, server-side marker and
loot tests, client/common sidedness scans, and semantic advancement tests.

**Medium:** loot item/potion component migration, compost registration,
recipe schema, custom payload registration, entity save data, and block-entity
ticking.  Validate decoded components and save/reload in a disposable world.

**Low:** translations, item definitions, ordinary sound references, and
creative-tab placement, provided original hashes/assets are checked.

## Audit validator and future runtime matrix

The Stage 10C validator must check the actual singular packaged structure path,
all 40 templates (hash, DataVersion, dimensions, palette/block/entity/marker
counts, DataFix and StructureTemplate load), every pool/processor/type link,
loot references, tag resolution, advancement predicate translation, original
OGG/PNG hashes, and absence of Taniwha/Stage 10D+ references.  It must clearly
separate “resource valid” from “runtime generation validated.”

The shortest later Prism matrix is: boot a fresh world; `/locate structure
biomemakeover:ghost_town` and inspect a generated Badlands town; verify roads,
buildings, processors and Ghost spawn; brush suspicious red sand and verify
loot/falling/save-reload; inspect Ghost drops; use Ectoplasm on a partial
Composter and empty Soul Soil; craft/toggle Poltergeist and verify particles,
sounds and Possessed; obtain/play Ghost Town disc and Badlands advancement;
craft Phantom Membrane; then test a newly generated chunk in an existing world.
No retro-generation is expected.

## Audit result

Production behaviour and the accepted JAR are unchanged by this document.
Stage 10C remains **AUDITED / AWAITING IMPLEMENTATION AUTHORIZATION**.  Stage
11 and later stages are **NOT STARTED**.  No push is authorized by this audit.
