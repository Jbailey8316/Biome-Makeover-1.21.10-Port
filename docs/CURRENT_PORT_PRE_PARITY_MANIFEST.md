# Biome Makeover 1.21.10 Current-Port Manifest

Snapshot date: 2026-08-23 (America/New_York)

Purpose: preserve and describe the current 1.21.10 port before any historical/original parity audit. This document inventories the current repository only. Presence is not evidence of correctness or parity, and no historical source comparison was performed for this manifest.

## A. Repository benchmark

- Branch: `main`
- HEAD: `d664cccf13ab65bddc7a3d30aa04254bb810e4f1`
- Upstream status at benchmark: `main...origin/main` with no ahead/behind marker
- Working tree at benchmark: clean
- Modified files: none
- Untracked files: none
- Staged files: none
- Submodules: none (`.gitmodules` is absent)
- Remote:
  - `origin` fetch: `https://github.com/Jbailey8316/Biome-Makeover-1.21.10-Port.git`
  - `origin` push: `https://github.com/Jbailey8316/Biome-Makeover-1.21.10-Port.git`
- Minecraft: `1.21.10`
- Mappings configured: official Mojang mappings; `yarn_mappings=1.21.10+build.2` is declared but is not used by `build.gradle`
- Fabric Loader: `0.18.4`
- Fabric API: `0.138.4+1.21.10`
- Fabric Loom: `1.17.10`
- Gradle wrapper: `9.5.0`
- Mod version: `1.21.10-0.8.5-flight-eyes-blink-fix`
- Archive base name: `biomemakeover-fabric`
- Java compile release/source/target: Java 21
- Fabric metadata minimum Java: 21

The repository already contained the tracked `reference/`, `porting/`, and `docs/` trees at this commit. They are included in the preservation point. The historical `reference/` tree was deliberately not inspected or compared during this Step 1 inventory.

## B. Build benchmark

Command used for the successful baseline:

```powershell
$env:GRADLE_USER_HOME='C:\Users\jbail\.gradle'
.\gradlew.bat clean build --offline --console=plain --warning-mode=all
```

- Result: success, exit code 0
- Console warnings: none emitted
- Console errors: none emitted by the successful run
- Tests: no test source files and no generated test reports were found; therefore no test suite ran
- Primary distributable JAR:
  - Path: `build/libs/biomemakeover-fabric-1.21.10-0.8.5-flight-eyes-blink-fix.jar`
  - Size: 1,107,426 bytes
  - SHA-256: `605E39323B6EC333467C5926F0FDB5BE618EBF6ACFD8BCBCDD53669383EDE76B`
- Sources JAR:
  - Path: `build/libs/biomemakeover-fabric-1.21.10-0.8.5-flight-eyes-blink-fix-sources.jar`
  - Size: 1,079,793 bytes
  - SHA-256: `940BCD97533CD0304EAF21E449B71AEEC057EFB1000EF9361CD0A4B9B1B39353`

Before the successful run, the wrapper was invoked once without an explicit Gradle user home. It failed before project configuration because the environment directed Gradle to `C:\.gradle`, where it could not create the wrapper lock parent directory. Exact exception:

```text
java.lang.RuntimeException: Could not create parent directory for lock file C:\.gradle\wrapper\dists\gradle-9.5.0-bin\bvnork1r7n8i6kp5cnkibsc9q\gradle-9.5.0-bin.zip.lck
```

The retry used the already-populated user cache at `C:\Users\jbail\.gradle`; no dependency or wrapper version was changed and the build remained offline.

## C. Current implementation inventory

### Entrypoints and registration

- Common entrypoint: `src/main/java/party/lemons/biomemakeover/BiomeMakeover.java`
- Client entrypoint: `src/client/java/party/lemons/biomemakeover/client/BiomeMakeoverClient.java`
- Registries: `src/main/java/party/lemons/biomemakeover/init/`
- Fabric metadata: `src/main/resources/fabric.mod.json`

### Biomes and world generation

- No custom biome class or biome JSON is present. Current work modifies vanilla `minecraft:dark_forest` through Fabric biome modifications.
- Runtime biome modification and spawn registration: `BMWorldgen.java`
- Thirteen configured features and thirteen corresponding placed features are stored under `src/main/resources/data/biomemakeover/worldgen/{configured_feature,placed_feature}/dark_forest/`:
  - Ancient Oak, small Ancient Oak, small Dark Oak
  - flowers, foxglove, tall grass, itching ivy, wild mushrooms, Black Thistle
  - Mesmerite boulder, fissure, and underground
  - Owl nest
- `BMWorldgen.java` currently injects only underground Mesmerite, wild mushrooms, and Black Thistle. It also adds Owl, Fox, and Rabbit creature spawns to Dark Forest.
- Ancient Oak sapling growth references the Ancient Oak configured features from `BMBlocks.java`.

### Structures/features

- Data-driven configured/placed features exist as listed above.
- No structure, structure set, template pool, or current-port structure implementation was found under `src`.

### Blocks

- Registration: `BMBlocks.java`
- Specialized classes: `block/BlackThistleBlock.java`, `ItchingIvyBlock.java`, `WildMushroomBlock.java`, and `OwlNestBlock.java`
- Registered block families/content:
  - Mesmerite and polished Mesmerite, including stairs, slabs, and walls
  - Black Thistle, Itching Ivy, Foxglove, wild mushrooms, Owl nest
  - Ancient Oak logs/woods, stripped variants, planks, stairs, slab, fence/gate, door/trapdoor, pressure plate/button, leaves, and sapling
- Blockstates, block/item models, item definitions, and textures: `src/main/resources/assets/biomemakeover/`

### Items and recipes

- Standalone item registration: `BMItems.java` (`leaf_litter`, `owl_egg`)
- Spawn egg: `BMEntities.java`
- Block items: created alongside blocks in `BMBlocks.java`
- Twenty-eight recipes: `src/main/resources/data/biomemakeover/recipe/`
  - Ancient Oak construction recipes
  - Mesmerite/polished Mesmerite crafting and stonecutting recipes
  - Owl nest, leaf litter, Black Thistle-to-black-dye recipes
- Recipe unlock advancements: seven Mesmerite-family JSON files under `data/biomemakeover/advancement/recipes/building_blocks/`

### Entities/mobs and Owl implementation

- The Owl is the only custom entity: registration, attributes, spawn placement, and spawn egg are in `BMEntities.java`.
- Server/entity behavior is concentrated in `entity/OwlEntity.java`.
- Present Owl behavior includes canopy/night spawn checks, daylight-only distance despawn for unowned unnamed wild owls, flying navigation, landing/descent changes, tree-perch seeking, wild-player caution/fleeing, owner follow/defense, shoulder-landing AI, sitting, taming, healing, breeding through vanilla animal mechanics, chicken hunting at night, nest claiming, daytime nest return/sleeping, disturbance/waking, and persistent home-nest coordinates.
- Raw rabbit is the temptation/breeding food, heals a tamed Owl by 4, and has a one-in-three taming chance for a wild Owl. Raw chicken heals a tamed Owl by 3. Wild hunting targets adult chickens; no Rabbit hunting goal is wired despite a `canHuntRabbit()` hook.
- Nest block state (`claimed`, `has_egg`) and shape: `block/OwlNestBlock.java`; models/blockstate: `assets/biomemakeover/{blockstates,models/block,textures/block}/owl_nest*`.
- The Owl egg is currently a plain registered item. No nest use interaction, egg entity/block entity, timer, incubation tick, or hatch implementation was found.
- No Owl head item/block/model or Owl-specific drop table was found.

### Sounds and particles

- Eight registered Owl sound events: idle, hurt, death, hoot, contact, alert, baby, and takeoff in `BMSounds.java`.
- Sound definitions and audio: `assets/biomemakeover/sounds.json` and `assets/biomemakeover/sounds/` (including `owl/` and a `final_owl/` asset set).
- No particle registration, particle Java implementation, or particle data was found.

### Loot, tags, and advancements

- Twenty-five block loot tables: `data/biomemakeover/loot_table/blocks/`.
- Tags: Ancient Oak log block/item tags plus Minecraft log, burnable-log, sapling, wall, and climbable integrations under `src/main/resources/data/{biomemakeover,minecraft}/tags/`.
- Note that both singular (`block`, `item`) and plural (`blocks`, `items`) Minecraft tag directory spellings exist in the current tree; this is recorded as suspicious and was not corrected.
- Seven recipe advancements exist, all for Mesmerite-family recipes. No gameplay/story advancement set was found.

### Configuration, networking, mixins, and compatibility

- Configuration: no configuration classes/files or config library wiring found.
- Networking: no custom payload, packet, receiver, or networking registration found. Owl state uses vanilla synchronized entity data.
- Mixins: no mixin classes or current-port mixin configuration found.
- Compatibility/integration: no Java compatibility modules found. A JourneyMap Owl icon exists at `assets/journeymap/icon/entity/biomemakeover/owl.png`; Minecraft/Fabric data-tag integration is present.

### Client rendering

- Client initialization/model layer and renderer registration: `client/BiomeMakeoverClient.java`, `client/model/BMModelLayers.java`
- Owl model/animation poses: `client/model/OwlModel.java`
- Owl render state and texture selection: `client/render/state/OwlRenderState.java`, `client/render/OwlRenderer.java`
- Present visuals include original-style model parts, walking/perched/flying/sleeping poses, head tracking, eyelid-plane blinking, and a night emissive eye layer.
- `OwlRenderer.java` explicitly retains a TODO for a shoulder-perch rendering hook.

### Data generation

- No data generator entrypoint, provider classes, or data-generation source set was found. Current JSON assets/data are checked in directly.

## D. Known custom/experimental implementation

These items are identified from current names, comments, project documentation, metadata, and repository history. They are preservation candidates only; this manifest does not decide whether any should remain.

### Owl work

- Mod version suffix `flight-eyes-blink-fix` explicitly marks the current build as an Owl flight/eyes/blink iteration.
- `OwlEntity.java`: custom flying move control and navigation, hover-prevention/descent behavior, exposed-canopy perch search, night/day schedule, daytime despawn rule, player caution/flee behavior, chicken hunting, owner interaction ordering fix, rabbit taming/healing, chicken healing, breeding/ownership behavior, nest claiming and sleep lock, wake/disturbance behavior, home-nest persistence, baby-scale and hunting hooks, and custom Owl sound triggers.
- `OwlModel.java`: custom perched/flying/sleeping animation logic, head tracking/tucked sleeping head, body lowering/fluff poses, wing movement, and an eyelid plane specifically added so blinking does not affect feet.
- `OwlRenderer.java` and `OwlRenderState.java`: deterministic blink timing, sleep rotation lock, motion-fed flight animation state, and night glowing-eye selection.
- Owl texture/audio variants: `assets/biomemakeover/textures/entity/owl*`, `assets/biomemakeover/sounds/owl/`, and `assets/biomemakeover/sounds/final_owl/`.
- Owl development records: `docs/owl/` contains final-system, beta-integration, sleep/nest pose, baby, wake, mechanics-fix, and full-system notes.
- Nest/egg experiments: `OwlNestBlock.java`, nest models/blockstate/recipe/loot/worldgen JSON, `BMItems.OWL_EGG`, egg item assets, `OwlEntity.homeNestPos`, and `CLAIMED`/`HAS_EGG` states.
- Explicitly absent/incomplete despite plans/hooks: actual shoulder render layer, egg placement/incubation/hatching/timers, baby-specific AI/model/commands, parent protection, natural nest injection, Rabbit hunting, Owl drops, and Owl heads.

### Dark Forest and block/data work

- Mesmerite worldgen experiments/fixes: three configured/placed feature pairs under `worldgen/*/dark_forest/mesmerite_*`; only underground Mesmerite is currently injected by `BMWorldgen.java`. Mesmerite and polished families, models/blockstates, loot, recipes, stonecutting, tags, and recipe advancements are current preservation candidates.
- Mushroom work: `WildMushroomBlock.java` is documented in source as a standalone replacement for an archived plant base and uses sturdy-face survival rules; generation JSON, multi-variant blockstate/model, loot, and texture assets are present. `BMWorldgen.java` injects it into Dark Forest.
- Black Thistle work: custom tall-flower collision slowdown/damage with Bee/Fox immunity in `BlackThistleBlock.java`, plus generation, models/textures, loot, dye recipe, and biome injection.
- Itching Ivy work: custom vine slowdown/damage and Bee/Fox immunity in `ItchingIvyBlock.java` plus climbable tag integration.
- Recipes and shape families: the checked-in recipes/models/blockstates for Mesmerite and Ancient Oak slab/wall/stair families represent prior port work to preserve.
- Wall connections: Minecraft wall-tag JSON and Mesmerite/polished Mesmerite wall blockstates/models are present. The mixed singular/plural tag paths should be audited later before assuming the connections are effective.
- Dark Forest tuning history visible in current-repository commit messages and docs includes Ancient Oak/flower weight adjustments, forest generation adjustments, Owl tests/spawn tuning, and Fox/Rabbit additions. No judgment is made here about parity.

## E. Known incomplete or suspicious areas

This list is based only on internal consistency of the current port and its own TODOs/docs, not comparison with historical Biome Makeover.

- `BMWorldgen.java` registers only 3 of 13 placed features; Ancient Oaks (except sapling growth), small Dark Oak, flowers, Foxglove, tall grass, Itching Ivy, Mesmerite boulder/fissure, and Owl nests are not injected there.
- The Owl nest has `HAS_EGG` state and egg assets, but no implemented egg interaction, timer, incubation, or hatching path.
- The Owl can breed directly through `BreedGoal`; planned nest/egg family flow is not connected to breeding.
- `canHuntRabbit()` exists but the wired hunting goal targets Chickens only.
- The shoulder-landing goal exists, but the renderer contains `TODO: Shoulder perch rendering hook.`
- Baby hooks/scaling exist, but project notes call out missing baby model/AI/commands.
- The registered takeoff sound was found, but no direct `OWL_TAKEOFF` playback call was found in the current entity inventory.
- Owl nest natural-generation JSON exists but its placed feature is not registered into Dark Forest by current Java code.
- No Owl loot/drop table and no Owl head implementation are present.
- No tests exist, so build success does not validate runtime behavior, resource loading, world generation, recipes, tags, rendering, or AI.
- Mixed tag paths (`tags/block` versus `tags/blocks`, and `tags/item` versus `tags/items`) are suspicious for current Minecraft data-pack conventions.
- `yarn_mappings` is declared in `gradle.properties`, while the build uses official Mojang mappings.
- The top-level `docs/STATUS.md` and `docs/FEATURES.md` checklists lag visible implementation and should not be treated as authoritative completion records.
- `fabric.mod.json` describes this as “Stage 7.2,” while the main logger says “owl nest full-test build”; version/phase labeling is inconsistent.
- No configuration, custom networking, mixins, particles, data generation, structures, or broad compatibility layer exists in the current port.

## F. File/class/resource index

- Core/registries: `src/main/java/party/lemons/biomemakeover/BiomeMakeover.java`; `src/main/java/party/lemons/biomemakeover/init/`
- Blocks: `src/main/java/party/lemons/biomemakeover/block/`; `src/main/resources/assets/biomemakeover/{blockstates,models,textures}/`; `src/main/resources/data/biomemakeover/loot_table/blocks/`
- Owl entity: `src/main/java/party/lemons/biomemakeover/entity/OwlEntity.java`
- Owl client: `src/client/java/party/lemons/biomemakeover/client/`
- Owl assets: `src/main/resources/assets/biomemakeover/{textures/entity,sounds,models/block,models/item,items,blockstates}/`; `src/main/resources/assets/journeymap/`
- World generation: `src/main/java/party/lemons/biomemakeover/init/BMWorldgen.java`; `src/main/resources/data/biomemakeover/worldgen/`
- Recipes/advancements: `src/main/resources/data/biomemakeover/{recipe,advancement}/`
- Tags: `src/main/resources/data/{biomemakeover,minecraft}/tags/`
- Project/Owl development notes: `docs/`, especially `docs/owl/`, and `porting/`
- Build/version metadata: `build.gradle`, `gradle.properties`, `settings.gradle`, `gradle/wrapper/gradle-wrapper.properties`, `src/main/resources/fabric.mod.json`

## G. Preservation point and recovery

All current work was already committed at benchmark time, so no worktree snapshot commit or stash was required. The preservation mechanism is a local annotated Git tag:

- Tag: `biome-makeover-1.21.10-pre-parity-reconstruction`
- Annotated tag object: `ef0b5ae9e86007da9232ba1fd3cad3cf22fe606b`
- Preserved commit: `d664cccf13ab65bddc7a3d30aa04254bb810e4f1`
- Push status: not pushed

Verify it:

```powershell
git rev-list -n 1 biome-makeover-1.21.10-pre-parity-reconstruction
```

Inspect it without changing the working tree:

```powershell
git show biome-makeover-1.21.10-pre-parity-reconstruction
```

Return to it safely on a new recovery branch (recommended; preserves later work and avoids detached HEAD):

```powershell
git switch -c recovery/biome-makeover-1.21.10-pre-parity-reconstruction biome-makeover-1.21.10-pre-parity-reconstruction
```

Alternatively, inspect the exact state detached:

```powershell
git switch --detach biome-makeover-1.21.10-pre-parity-reconstruction
```

Do not use reset or force operations to recover this snapshot. The tag remains local unless explicitly pushed later.
