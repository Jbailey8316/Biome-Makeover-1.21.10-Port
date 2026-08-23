# Stage 0 — World Safety and Registry-ID Contract

Date: 2026-08-23
Starting commit: `8d5aabdddb0aa9268c60cced5961f854ce657bcf`
Historical target: Lemonszz/Biome-Makeover `1.20` at
`2f314c0596af095a4890995a465f308f69476b4a` (1.20.1-1.11.4)

Stage 0 establishes validation and migration rules. It registers no content,
changes no gameplay, and does not begin Stage 1.

## A. Stage 0 benchmark

| Property | Baseline |
|---|---|
| Branch | `main` |
| Starting HEAD | `8d5aabdddb0aa9268c60cced5961f854ce657bcf` |
| Upstream | `origin/main`; ahead 0, behind 0 at pre-flight |
| Initial worktree | Clean |
| Java | Oracle Java 21.0.2 LTS |
| Minecraft | 1.21.10 |
| Mappings | Official Mojang mappings; `yarn_mappings` property 1.21.10+build.2 is declared but unused |
| Fabric Loader | 0.18.4 |
| Fabric API | 0.138.4+1.21.10 |
| Fabric Loom | 1.17.10 |
| Gradle wrapper | 9.5.0 |
| Mod version | 1.21.10-0.8.5-flight-eyes-blink-fix |
| Java compilation target | 21 |

The current-port manifest, historical master audit, and Step 3 parity matrix
were present. The annotated preservation tag dereferences to
`d664cccf13ab65bddc7a3d30aa04254bb810e4f1`, as required.

## B. Registry-ID policy

Registry IDs are serialized interfaces, not cleanup opportunities.

1. Preserve every reachable released historical ID wherever technically
   possible.
2. Never remove, rename, or reuse a current 1.21.10 ID without an explicit
   existing-world migration review.
3. An absent historical ID is a target inventory entry, not authorization to
   register it before its owning restoration stage.
4. If current and historical IDs conflict, retain both facts in snapshots and
   stop for migration design. Do not automatically alias or rename.
5. Registry aliases, data fixers, fallback readers, or migration commands must
   be separately approved and tested on a world copy.
6. IDs excluded as dead or dev-only are not restoration targets.
7. Each stage updates snapshots deliberately in the same focused commit as the
   owning feature. Unexpected removals fail validation.

Classification contract:

- **A — HISTORICAL ID, ALREADY MATCHING:** same registry and path in released
  source and current port.
- **B — HISTORICAL ID, CURRENTLY ABSENT:** required target; add only in its
  approved stage.
- **C — CURRENT 1.21.10 ID REQUIRING HISTORICAL COMPARISON:** current ID or
  data key whose exact historical semantics/path remains unresolved.
- **D — CURRENT CUSTOM/MYTHAS ID:** preserve for later reconciliation; never
  count toward released parity.
- **E — HISTORICAL DEAD/UNREACHABLE ID:** document but do not restore by
  default.
- **F — HISTORICAL DEV-ONLY ID:** archive only; excluded from Stages 0–13.

This policy applies to blocks, items, entities, block entities, effects,
attributes, sounds, particles, structures/pieces/processors, configured and
placed features, recipes/serializers/types, menus, data components, and every
other persistent registry-backed system.

## C. Current registry baseline

`validation/baselines/current_registry_ids.json` is the machine-readable
contract. Static source inspection records:

| Registry/resource family | Current count |
|---|---:|
| Blocks | 28 |
| Items, including block items and Owl spawn egg | 31 |
| Entity types | 1 |
| Sound events | 8 |
| Block entities/effects/particles/structures/menus/custom serializers/components | 0 |
| Configured-feature resources | 13 |
| Placed-feature resources | 13 |
| Placed keys injected into biomes | 3 |

Matching historical IDs include most registered Ancient Oak and Mesmerite
families, Black Thistle, Foxglove, Itching Ivy, Wild Mushrooms, Leaf Litter,
Owl, and the three released Owl sounds. The committed snapshot is sorted and
diff-friendly.

The current resource tree also contains unregistered Ancient Oak sign/boat
assets and ten worldgen pairs that are not injected. Assets do not create
registry claims.

## D. Historical target registry strategy

`validation/baselines/historical_registry_targets.json` pins the source and
records counts, exact high-risk IDs, and exclusions. It contains 21 exact
entity IDs, five block-entity IDs, four particles, structure type/piece and
processor IDs, and the projectile-resistance attribute.

Blocks/items generated through Taniwha must not be guessed from translation
counts. During the owning stage, obtain an exact 1.20.1 runtime registry/tag
dump (or inspect the pinned Taniwha 5.4.4 binary), then commit the expanded
exact target list before registering content. Historical resources under
`reference/Biome-Makeover-1.20` provide the offline recipe, loot, feature,
template, tag, model and sound inventories.

## E. Known ID conflicts and review items

No proven same-registry ID collision requiring an immediate rename was found.
The following require explicit review:

| Current ID/path | Classification | Concern |
|---|---|---|
| `biomemakeover:owl_nest` block | D | Current Mythas content; not released parity |
| `biomemakeover:owl_egg` item | D | Current Mythas content; not released parity |
| Owl alert/baby/contact/hoot/takeoff sounds | D | Current additions; preserve separately |
| All 13 `dark_forest/*` feature resource paths | C/A mixture | Compare exact historical paths and semantics before enabling/replacing |
| Ancient Oak sign/boat resource IDs | C | Assets exist without current registry entries |
| `data/minecraft/tags/blocks` and `tags/items` | C | Legacy plural directories coexist with singular 1.21.10 paths; runtime membership must be checked |
| Owl entity dimensions | A ID, differing behavior | Same ID does not imply serialized/behavioral parity |

Historical Blight Bat, Mushroom Trader and Directional Data IDs are E.
Adjudicator Mimic reachability is conditional; Toad/Tadpole require runtime
reachability verification. Divergent-development IDs are F.

## F. Validation harness

Run from repository root:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File validation/Invoke-ParityValidation.ps1
```

The dependency-free script:

- re-derives block, item, entity, sound and injected-feature IDs from current
  Java and fails on removal or unexpected addition;
- compares configured/placed feature files with committed snapshots;
- parses resource JSON where PowerShell 5 can represent its keys;
- validates placed-to-configured-feature references;
- checks blockstates, item definitions and block loot, reporting known current
  omissions as warnings ready to become assertions in owning stages;
- reports legacy tag-directory risks.

PowerShell 5 cannot materialize JSON objects containing an empty-string key,
which valid multipart blockstates use. Those files are reported and delegated
to Gradle/Minecraft resource loading. Later stages add assertions rather than
replace this harness with a large testing dependency.

## G. Existing-world test protocol

**MANUAL TEST REQUIRED:** the live Mythas world is not present in this
workspace. Automation must never target the production save.

1. Record production server shutdown and make a complete, restorable backup.
2. Copy the world to a disposable test server with a distinct path/name and
   blocked public access. Verify hashes or backup manifest before starting.
3. Record the exact current mod JAR filename and SHA-256 and copy all server
   mod/config/version metadata.
4. Start once with the baseline JAR. Capture the complete startup log,
   registry warnings and final ready line.
5. Search logs for `missing`, `unknown`, `registry`, `datafix`, `serialization`,
   `codec`, `chunk`, `entity`, `block entity`, and stack traces. Preserve the
   unfiltered log too.
6. Log in with a copied player. Check inventory, Ender Chest, equipment and
   recipe/advancement state, especially any BM stacks.
7. Visit representative old chunks, bases, storage, and Dark Forest chunks.
   Check current BM blocks/items and blockstates if present.
8. Locate and inspect existing Owls if available: wild/tamed, owner, health,
   sitting, name, age, home nest and position. Record `/data get entity` output
   where safe.
9. Save all, perform an orderly shutdown, and verify the server reaches its
   completed-save/shutdown lines without errors.
10. Restart the same disposable copy, log in again, revisit the same chunks and
    Owls, and repeat log searches.
11. Compare region/entity/playerdata modification scope with expected visited
    chunks. Any missing/unknown registry warning, datafix/codec error, entity
    discard, chunk-load failure, or changed persistent stack blocks promotion.
12. Destroy or archive the disposable copy according to operator policy; never
    copy its mutated data back over production.

Repeat this protocol at every registry-, NBT-, worldgen-, structure-, or
block-entity-affecting checkpoint. New worldgen is tested in new chunks;
existing chunks must not be retro-generated automatically.

## H. Owl serialization contract

Entity registry ID: `biomemakeover:owl`.

| State | Persistence mechanism | Classification |
|---|---|---|
| `OwlState` integer enum ordinal | Explicit `ValueOutput`/`ValueInput` | HISTORICAL-COMPATIBLE; unknown ordinals fall back through `byId` |
| `StandingState` integer enum ordinal | Explicit output/input | HISTORICAL-COMPATIBLE |
| `HomeNestX/Y/Z` integers | Written only when a nest exists; read only when all three exist | CURRENT-MYTHAS |
| Synced Owl state | Tracked integer | HISTORICAL-COMPATIBLE runtime state |
| Synced standing state | Tracked integer | HISTORICAL-COMPATIBLE runtime state |
| Synced `OWL_SITTING` | Tracked boolean, not explicitly written by Owl | CURRENT-MYTHAS; reload synchronization NEEDS RUNTIME VERIFICATION |
| Synced `OWL_SLEEPING` | Tracked boolean, not explicitly written by Owl | CURRENT-MYTHAS and transient across reload unless inherited behavior incidentally restores it |
| Owner UUID/tame flag | Inherited `TamableAnimal` persistence | HISTORICAL-COMPATIBLE; verify exact 1.21.10 field output at runtime |
| Ordered sitting | Inherited tameable persistence plus current synced mirror | HISTORICAL-COMPATIBLE base, mirror UNKNOWN |
| Age, health, name, position, rotation, UUID, persistence flags | Inherited entity/living/ageable persistence | HISTORICAL-COMPATIBLE vanilla contract |
| Claimed-nest cooldown, search cooldown, disturbance, wake timers, lean values | Not serialized | TRANSIENT |
| Variant/Hedwig selection | No current serialized variant field | ABSENT; future historical comparison required |
| Tree preference/memory | No persistent tree reference found | TRANSIENT |

Explicit rule: **every Owl serialized by the current 1.21.10 port must remain
loadable throughout reconstruction.** Later changes must use tolerant readers,
retain `OwlState`, `StandingState`, and `HomeNestX/Y/Z`, and receive explicit
migration approval before changing enum meaning or field types. Stage 7 must
test existing Owl NBT captured from a disposable world copy.

## I. Build and hash baseline

Stage 0 validation tooling and documents are outside `src`, so they must not
alter production JAR content. The Step 1 known-good baseline was:

- primary: `build/libs/biomemakeover-fabric-1.21.10-0.8.5-flight-eyes-blink-fix.jar`
- SHA-256: `605E39323B6EC333467C5926F0FDB5BE618EBF6ACFD8BCBCDD53669383EDE76B`
- sources SHA-256:
  `940BCD97533CD0304EAF21E449B71AEEC057EFB1000EF9361CD0A4B9B1B39353`

Final Stage 0 build: `gradlew.bat clean build --offline`, **BUILD SUCCESSFUL**
in 8 seconds. Nine actionable tasks ran (five executed, four from cache).
There were no Java compilation errors or test failures. The project has no
test sources (`test NO-SOURCE`). Gradle reported deprecated features that will
be incompatible with Gradle 10 and emitted its incubating problems report.

The rebuilt primary and sources hashes exactly match Step 1. Stage 0 therefore
made no packaged gameplay change. Hash differences in later stages require a
packaged-JAR entry comparison before acceptance.

## J. Stage checkpoint policy

Every Stage 1–13 implementation checkpoint must finish with:

1. `git diff --check`;
2. clean offline build without dependency changes;
3. static parity validation;
4. deliberate registry snapshot comparison/update;
5. stage-specific runtime checklist;
6. existing-world risk review on a disposable copy where applicable;
7. dedicated-server safety review where applicable;
8. packaged-JAR entry/hash audit where applicable;
9. one focused local commit; and
10. no push unless explicitly instructed.

Stages 3, 4, 5 and 6 each require an independent known-good checkpoint before
the next biome starts. The 14-stage numbering does not prohibit smaller,
reviewable internal commits/checkpoints.

## K. Stage 10 subdivision policy

- **10A — Mushroom House**
- **10B — Sunken Ruins**
- **10C — Ghost Town and archaeology**

Each subdivision gets its own build, validation, runtime test and checkpoint.
10C carries the archaeology-specific very-high risk and cannot be bundled to
hide failures in the other structures.

## L. Stage 11 subdivision allowance

Stage 11 may use 11A/11B/11C checkpoints for registry/bootstrap, template and
processor reachability, and layout/integration respectively. The 228-template
pipeline warrants subdivision whenever review or deterministic tests would
otherwise become unwieldy.

## M. Stage 13 completion policy

Stage 13 is **PARITY AUDIT + GAP CLOSURE + ACCEPTANCE + FREEZE**. It is not a
backlog for ordinary content. Recipes, loot, models, tags, sounds,
advancements, particles and client resources belonging to a feature are
completed and tested in that feature's stage. Stage 13 finds accidental
omissions, cross-cutting gaps and integration defects, runs acceptance, and
creates the strict parity freeze before separately approved Mythas work.

## N. Known manual runtime tests

- Full disposable-copy world protocol in section G.
- Actual 1.21.10 registry dump from a client and dedicated server.
- Current Owl `/data` capture, save/reload and owner/sit/nest verification.
- Runtime tag membership, especially singular versus legacy plural folders.
- Dormant worldgen feature loading and active injection confirmation.
- Historical 1.20.1/Taniwha registry and tag dump.
- Dedicated-server resource/packet safety as systems are introduced.

No runtime result is claimed in Stage 0 unless actually executed and logged.

## O. Explicit Stage 1 entry criteria

Stage 1 may begin only after:

- Stage 0 is committed and reviewed;
- preservation tag still dereferences correctly;
- static validation passes with all warnings understood;
- clean offline build and JAR hashes are recorded;
- current registry snapshots are accepted;
- historical target strategy and exclusions are accepted;
- Owl loadability rule is accepted;
- live-world-copy protocol has an assigned manual operator before the first
  persistence-affecting change; and
- no uncommitted Stage 0 work remains.

Stage 1 must not begin automatically after satisfying these criteria.
