# Stage 9 Pre-Implementation Source Audit

Audit date: 2026-08-29  
Authority: final Biome Makeover 1.20.1-1.11.4 source at `2f314c0596af095a4890995a465f308f69476b4a`  
Port checkpoint: `01bebd2e3ef91507dc851d8ffebe2b52ef26deb7`

This is an audit-only document. It changes no production code, resources, registrations, or packaged content.

## Verdict

The older roadmap defines Stage 9 broadly as functional blocks, five block entities, menus, and packets. That remains a useful umbrella, but it is not a safe single implementation checkpoint. One of the five historical block entities (`lightning_bug_bottle`) is already restored and runtime accepted in Stage 5. The four remaining block entities have different dependency owners:

- `altar` requires the complete curse engine to be meaningful.
- `tapestry` and `directional_data` are Mansion prerequisites whose normal acquisition/execution is structure-owned.
- `poltergeist` requires Ghost-derived Ectoplasm.

The next dependency-closed checkpoint should therefore be **Stage 9A — Independent Functional Utilities**, containing only:

1. Stunt Powder and the global ageable-mob stunting contract.
2. Peat Composter and the full-composter/water-drip conversion contract.

Both are active final-release parity, have all required acquisition ingredients already restored, can be tested independently, and do not activate Stage 10 structures or Stage 12 progression.

## Stage 9A dependency graph

```text
Stage 6 Illunite Shard + Stage 8 Bulbus Root
    -> biomemakeover:stunt_powder recipe (2)
    -> interact with supported baby/always-baby Stuntable
    -> persistent bm_IsStunted state
    -> permanent baby age/visual state
    -> warped-spore particles + item consumption

Stage 5 vanilla-compatible compostables
    -> vanilla composter reaches level 8
    -> water drip through pointed dripstone
    -> biomemakeover:peat_composter
    -> comparator strength 9
    -> player or downward hopper extracts one biomemakeover:peat
    -> returns to empty vanilla composter
    -> biomemakeover:peat_compost advancement trigger on player extraction
```

No recipe creates the Peat Composter. Its environmental dripstone conversion is the released acquisition path.

## Reachability table

| Component | Final ID/type | Final acquisition or entry | Final use/output | Dependencies | Classification | Current port |
|---|---|---|---|---|---|---|
| Stunt Powder | `biomemakeover:stunt_powder`, item | Shapeless Illunite Shard + Bulbus Root -> 2 | Permanently stunts an eligible baby/always-baby entity | Stage 6 Illunite; Stage 8 Bulbus Root | ACTIVE FINAL | Fully absent |
| Stuntable contract | global `AgeableMob` state plus special implementations | Invoked by Stunt Powder; Mansion markers also use it later | Persists `bm_IsStunted`; forces baby state/age | Modern age/render/save translation | ACTIVE FINAL | Fully absent |
| Peat Composter | `biomemakeover:peat_composter`, block | Level-8 vanilla composter receives a water drip | One Peat; returns to empty composter | Stage 5 Peat; vanilla dripstone/composter | ACTIVE FINAL | Fully absent |
| Peat Compost trigger | `biomemakeover:peat_compost`, criterion | Player empties Peat Composter | `create_peat` progression | Peat Composter | ACTIVE FINAL | Deferred advancement removed from current package |
| Altar | `biomemakeover:altar`, block + BE + menu | Craft: Book, 2 Illunite Shards, 2 Mesmerite, Crying Obsidian | Timed cursed-enchanting operation | Complete curse registry/engine | ACTIVE FINAL, dependency not closed | Fully absent |
| Tapestries | 17 standing + 17 wall blocks, shared `tapestry` BE | Mansion markers; Adjudicator variant from boss loot | Collectible decoration/progression | Mansion and boss acquisition | ACTIVE FINAL, later owner | Fully absent |
| Directional Data | `directional_data`, hidden block + BE + menu | Structure templates/development data | Mansion marker interpretation | Mansion structure | ACTIVE FINAL internal infrastructure | Fully absent |
| Ectoplasm Composter | `ectoplasm_composter`, stateful block | Ectoplasm converts a non-empty vanilla composter | Soul Soil at level 8 | Ghost -> Ectoplasm | ACTIVE FINAL, hard dependency missing | Fully absent |
| Poltergeist | `poltergeist`, block + BE + Possessed effect/networking | Craft consumes Ectoplasm, Phantom Membranes, Soul Soil, Cauldron | Local block interactions and possession | Ghost/Ectoplasm chain | ACTIVE FINAL, hard dependency missing | Fully absent |
| Phantom Membrane crossover | recipe | 1 Ectoplasm + 3 Moth Scales | 1 Phantom Membrane | Stage 8 Moth + Stage 10C Ghost | ACTIVE FINAL, hard dependency missing | Absent by design |
| Witch Hat | `witch_hat`, wearable item | Player-killed Witch: 5% + 5% per Looting; also Sunken Ruin loot | Gates Witch quest interaction | Witch loot; optional structure source | ACTIVE FINAL | Fully absent |
| Witch quests | `witch` menu, packets, data reloads, Witch mixin/state | Wear Witch Hat and interact with non-raiding, non-targeting Witch | Item requests -> weighted rewards | Witch Hat, data categories/rewards, networking | ACTIVE FINAL | Fully absent |
| Mansion/cladding/boss chain | many canonical IDs | Pillager/structure/boss paths | Structure progression and rewards | Stages 11–12 | ACTIVE FINAL | Fully absent |

## Stunt Powder and Illunite findings

The recipe is final and reachable: one `illunite_shard` plus one `bulbus_root` yields two `stunt_powder`. Use is server-authoritative entity interaction. It succeeds only when the target implements `Stuntable`, is a baby or reports `isAlwaysBaby()`, and is not already stunted. Success sets persistent state, consumes one powder outside creative behavior through the historical helper, emits 15 warped-spore particles at spread 0.2, and fires the entity-interact game event. It does not de-age adults.

Final 1.20.1 injects the Stuntable contract into all `AgeableMob` instances. The persisted boolean is `bm_IsStunted`; a stunted mob reports baby, retains synced baby state, and rejects normal age advancement by holding age at -6000. Tadpole has a separate implementation. The final-release-disabled Toad/Tadpole ecosystem must not be activated merely to restore the globally reachable powder.

This is an independent consumer for the Stage 6 Illunite Shard. It does not depend on the Altar or curses.

## Altar and curse boundary

The Altar is active final content, but the block cannot be split safely from cursing:

- recipe: Book, two Illunite Shards, two Mesmerite, and Crying Obsidian;
- waterlogged `active` block state, custom shape, comparator output, particles, renderer and sound;
- two-slot container: curseable item/book and `#biomemakeover:curse_fuel` (Illunite Shard);
- 300-tick operation, automation sided slots, inventory/progress persistence, menu and screen;
- output uses `Cursing`: cursed Enchanted Book or compatible enchantment upgrade plus a random compatible curse; processed items receive historical metadata/repair-cost semantics;
- `strictAltarCursing` configuration changes upgrade selection.

The ten final curses (`decay`, `insomnia`, `conductivity`, `enfeeblement`, `depth`, `flammability`, `suffocation`, `unwieldiness`, `inaccuracy`, `buckling`) depend on old enchantment registration, attributes, item NBT and numerous item/entity/projectile mixins. Minecraft 1.21.10's data-driven enchantments/components make this a high-risk semantic unit. Recommended ownership is **Stage 9B — Altar and Complete Curse Engine**, not an inert Altar foundation and not the much larger Mansion boss stage.

## Witch system findings

The Witch Hat and quests are one coherent final system, although the Hat has two acquisition paths:

- supplemental Witch loot: killed by player, base 5% plus 5% per Looting level;
- Sunken Ruin loot also contains the Hat, providing a structure-owned secondary path;
- the Hat is a wearable `HatItem` with dedicated Witch/player rendering and belongs to `biomemakeover:witch_hats`;
- a living Witch with no customer permits interaction only while it has no target, is not in an active raid, the player wears a tagged Hat, and quest categories loaded successfully;
- qualifying players are removed from the Witch's player-target predicate;
- three quests are initially populated, refill to three on a 21,000–23,999 tick schedule, and the interaction grants a 12,000-tick despawn shield;
- the quest inventory and timers persist; completion uses client/server menus and packets, a custom `witch_trade` trigger, ten data-driven request categories and four reward tables.

This is active final parity, not a dormant showcase system. The rare Witch-kill path makes the core system reachable without Sunken Ruins, while the ruin is a soft secondary acquisition/progression dependency. Restore it later as **Stage 12A — Witch Hat and Witch Quests**, independently of Mythas Living World integration.

## Peat Composter findings

The Peat Composter is active and independently reachable. It is not a block entity and has no GUI or network packet. A Taniwha `DripstoneReceiver` hook historically lets a level-8 vanilla composter receive water from pointed dripstone and replace itself with `peat_composter`. The ready block:

- exposes comparator strength 9;
- returns one Peat when used, triggers `peat_compost` for a server player, then becomes an empty vanilla composter;
- exposes one output slot only downward, so a hopper can extract one Peat and reset it;
- accepts no inputs and performs no timed processing after conversion;
- uses composter empty/fill sounds/events and ordinary wood/fire properties;
- has blockstate/model resources but no registered block item; breaking it drops a vanilla Composter. Survival acquisition is environmental conversion rather than a recipe or direct placement.

It belongs in Stage 9A, not a structure stage and not a broad Swamp rewrite.

## Mansion and Pillager progression

The final Mansion system remains a multi-stage vertical chain:

```text
Stage 11 Mansion structure/layout/templates
  -> Directional Data marker interpretation
  -> 16 ordinary Tapestries, seven loot tiers and mob pools
  -> integrated dungeon and boss room
  -> Red Rose disc from good/dungeon-good chests
  -> Adjudicator marker

Pillager/leader/outpost loot
  -> Crude Fragment/Cladding + smithing template + damaged cladded armor
  -> Cladded Stone
  -> player-created Stone Golem

Adjudicator
  -> phase-only Mimics and temporary Stone Golem
  -> Enchanted Totem + Adjudicator Tapestry
```

All are active final parity. They should not be collapsed into one implementation:

- **Stage 11A:** Mansion registry/codecs/layout/template inventory and structure placement.
- **Stage 11B:** Directional Data marker execution, Tapestries, loot tiers, mob pools, dungeon and persistence.
- **Stage 12B:** Crude/cladding/template/armor, Pillager hooks and player Stone Golem.
- **Stage 12C:** Adjudicator, Mimic phases, temporary Golem, Enchanted Totem and boss rewards.

The pinned final tree contains 168 Mansion NBT files; the older 228-file shorthand is not authoritative. The final `bonemeal` Mansion marker is commented/broken and remains dormant. Adjudicator Mimic is active only as a boss phase, never a normal-spawn entity.

## Cross-biome dependencies

| Dependency | Classification | Disposition |
|---|---|---|
| Illunite Shard + Bulbus Root -> Stunt Powder | HARD and already restored | Stage 9A ready |
| Peat output and ordinary compostables | HARD and already restored | Stage 9A ready |
| Ghost -> Ectoplasm -> Ectoplasm Composter/Poltergeist | HARD | Keep with/after Stage 10C Ghost Town |
| Ectoplasm + Moth Scales -> Phantom Membrane | HARD for recipe, soft for Moth ecosystem | Add when Ectoplasm becomes reachable; do not pull Ghost Town forward |
| Witch Hat from Witch kill | HARD core acquisition, no missing dependency | Witch system can precede Sunken Ruins |
| Witch Hat from Sunken Ruin loot | SOFT secondary acquisition | Connect in Stage 10B |
| Mansion blocks from Stage 6 Dark Forest | HARD and restored | Stage 11 ready after infrastructure |
| Tapestry and Red Rose acquisition | HARD Mansion/boss dependency | Restore with Stage 11/12 owner |
| Crude chain from Pillagers/outpost | HARD but independent of Mansion generation | Restore vertically with cladding/Golem, not as registry-only residue |

## Current-port residue

All audited remaining systems are fully absent from current production source/resources: no registry-only blocks/items, no stub classes, no copied models/textures, no menus, no packets, and no hidden recipes. The deliberate exceptions are already-restored dependencies (Illunite, Mesmerite, Bulbus Root, Moth Scales, Peat) and the Stage 5 `lightning_bug_bottle` block entity. The current package correctly excludes the deferred `create_peat`, Phantom Membrane, Altar, Witch, Tapestry, Directional Data, Ectoplasm, cladding and Mansion chains.

No dangerous partial implementation was found. Future work must preserve canonical IDs rather than introducing temporary substitutes.

## Active, dormant, and historical classification

**Active final:** Stunt Powder; Peat Composter; Altar and all ten curses; Witch Hat/quests; Ectoplasm Composter; Poltergeist; Phantom Membrane recipe; Tapestries; Mansion/dungeon; Crude/cladding; Stone Golem; Adjudicator/Mimic phase; Enchanted Totem; Red Rose disc and associated advancements/loot.

**Dormant final:** Mansion `bonemeal` marker behavior; normal spawning/acquisition for Adjudicator Mimic; standalone Mesmerite Underground/Boulder natural injections established by the Stage 7 follow-up; final-disabled Toad/Tadpole ecosystem paths.

**Historical/removed:** showcase-era Toad/Dragonfly-Wing loop and other previously catalogued disabled systems. None belongs to Stage 9 parity.

**Current port artifacts:** none found in the audited Stage 9+ families.

**Mythas enhancements:** Owl falconry, dynamic moving-bug light, Decayed shield AI, Toad revival, enhanced lily placement, Scuttler expansions/hats and Living World Witch integration remain separate.

## Stage 9A runtime test matrix

### Stunt Powder

1. Craft two powder from one Illunite Shard and one Bulbus Root; verify recipe-book behavior against the final recipe data.
2. Use on a supported baby ageable mob: consume one, emit particles, remain a genuine baby.
3. Repeat on the same mob: no consumption/state change.
4. Use on an adult and unsupported entity: no effect or consumption.
5. Save/reload and restart while stunted; verify state, dimensions and rendering persist.
6. Wait/accelerate ordinary growth; verify the stunted mob does not age.
7. Verify an unstunted control baby ages normally.
8. Test multiplayer/server authority and dispenser/non-player interaction only where final behavior supports it.

### Peat Composter

1. Fill a vanilla composter to level 8 under a valid water-dripping pointed dripstone setup.
2. Verify conversion timing/event and exact `peat_composter` state.
3. Verify comparator output 9.
4. Empty by hand: receive one Peat, trigger the advancement, and restore empty vanilla composter.
5. Repeat with a downward hopper: extract one Peat and restore empty composter without a player-only advancement.
6. Verify no side/top insertion or duplicate extraction.
7. Break it and verify it drops a vanilla Composter; confirm no nonhistorical Peat Composter item is exposed.
8. Save/reload before extraction and perform a dedicated-server boot/join/rejoin test.

## Continuation roadmap

| Stage | Proposed coherent scope | Important exclusions/dependencies |
|---|---|---|
| 9A | Stunt Powder + global stunting; Peat Composter | No Toad revival, Altar, Ectoplasm, structures or quests |
| 9B | Altar + complete ten-curse engine, menu/BE/renderer/sound/config/advancement | Must translate 1.21.10 enchantment components as one system |
| 10A | Mushroom House, structure loot, Button Mushrooms disc; recheck but do not assume Mushroom Trader reachability | Existing approved boundary |
| 10B | Sunken Ruins, loot, Swamp Jives and Witch Hat secondary acquisition | Witch quests may remain 12A |
| 10C | Ghost Town, archaeology, Ghost/Ectoplasm, Ectoplasm Composter, Phantom Membrane crossover and Poltergeist vertical chain | Processor semantics first; no generic Badlands rewrite |
| 11A | Mansion registry/codecs/layout and deterministic 168-template inventory | No boss/progression |
| 11B | Mansion marker infrastructure, Tapestries, loot/mobs, dungeon and Red Rose reward path | Directional Data remains internal, not ordinary survival content |
| 12A | Witch Hat primary loot, Witch quests/menu/network/data/rewards | No Mythas Living World integration |
| 12B | Crude/cladding/template/armor, Pillager hooks, Cladded Stone and player Stone Golem | Complete vertical chain, not dead items |
| 12C | Adjudicator/Mimic encounter, temporary Golem phase, Enchanted Totem and boss Tapestry | Requires Mansion dungeon/room bounds |
| 13 | Remaining beach ecology, cross-cutting recipes/loot/advancements/sounds/boats and parity freeze, split further if implementation risk warrants | No Mythas layer until parity tag |

This revision preserves the existing stage ownership concepts while replacing oversized shorthand with independently testable subcheckpoints.

## Explicit Stage 9A exclusions

Altar/curses, Tapestries, Directional Data, Poltergeist, Ectoplasm Composter, Ghost/Ectoplasm, Phantom Membrane crossover, Witch Hat/quests, Mansion/dungeon, Crude/cladding, Stone Golem, Adjudicator/Mimic, Enchanted Totem, music discs, structures, boats, Toad/Tadpole and every Mythas enhancement.
