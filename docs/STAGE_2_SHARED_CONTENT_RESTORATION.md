# Stage 2 — Shared Content Restoration Contract

Date: 2026-08-23

Starting commit: `96f25b6dc9b691b4b7f4bc62ca031ab043206c2f`

## A. Scope decision

Stage 2 uses **strict ownership**. A block or item remains owned by its biome,
entity, functional system, structure or progression stage even when a later
system depends on it. Dependencies point backward to the owning stage; they do
not pull themed content into Stage 2.

> **NO GAMEPLAY CONTENT QUALIFIES FOR STAGE 2 UNDER STRICT OWNERSHIP.**

No blocks, items, foods, effects, recipes, loot, tags, entities, structures or
worldgen are registered or changed by Stage 2. This is the authoritative
content-contract and ownership checkpoint.

## B. Authoritative Taniwha family-contract evidence

The official archived source was inspected directly:

- repository: `Lemonszz/taniwha`
- tag: `1.20.0-5.4.4`
- commit: `ee029d785850d8b0ad8ba69bee4e069b03253afe`
- `common/src/main/java/party/lemons/taniwha/block/DecorationBlockFactory.java`
- `common/src/main/java/party/lemons/taniwha/block/WoodBlockFactory.java`

The tag commit was obtained with `git ls-remote` and inspected from a detached,
temporary clone. The machine-readable result is
`validation/foundations/historical_family_contracts.json`.

## C. DecorationBlockFactory contract

`DecorationBlockFactory.all()` calls `slab()`, `stair()` and `wall()`—nothing
else. The base block is registered separately by BM.

| Member | ID rule | Historical implementation |
|---|---|---|
| Slab | `<base>_slab` | `TSlabBlock`, supplied properties and modifiers |
| Stairs | `<base>_stairs` | `TStairBlock`, base default state, supplied properties and modifiers |
| Wall | `<base>_wall` | Vanilla `WallBlock`, supplied properties; factory does not apply modifiers |

Each generated block receives an ordinary same-ID block item. Later stages
must not infer any additional decorative member.

## D. WoodBlockFactory contract

`WoodBlockFactory` starts with log, stripped log and planks. `.all(boatType)`
adds all remaining members below.

Blocks—exactly 17:

1. `<base>_log`
2. `stripped_<base>_log`
3. `<base>_planks`
4. `<base>_wood`
5. `stripped_<base>_wood`
6. `<base>_slab`
7. `<base>_stairs`
8. `<base>_fence`
9. `<base>_fence_gate`
10. `<base>_pressure_plate`
11. `<base>_button`
12. `<base>_trapdoor`
13. `<base>_door`
14. `<base>_sign`
15. `<base>_wall_sign`
16. `<base>_hanging_sign`
17. `<base>_wall_hanging_sign`

Items—exactly 17:

- ordinary block items for the first 13 non-sign blocks;
- `<base>_sign`, stack size 16;
- `<base>_hanging_sign`, stack size 16;
- `<base>_boat`, stack size 1;
- `<base>_chest_boat`, stack size 1.

Sign blocks have no separate block item; their paired standing/wall blocks use
one sign item. Leaves and saplings are not factory members and are registered
separately by BM.

## E. Historical property and suffix rules

The default factory properties are strength 2/3 and Wood sound. BM supplies
bark and plank map colors. Exact member rules are preserved in the machine
contract; important distinctions include:

- log uses plank color on the Y axis and bark color horizontally, is
  flammable, and strips to stripped log;
- wood uses bark color and strips to stripped wood;
- both stripped log and, notably, stripped wood use plank color;
- logs/wood have explosion resistance 2; wood members use bass instrument
  where specified and are generally ignited by lava;
- fence and gate force solid; gate uses the factory WoodType;
- pressure plate uses EVERYTHING sensitivity; button has 30-tick wooden
  projectile activation;
- doors/trapdoors are strength 3 and cutout; trapdoors are never valid spawn
  surfaces;
- sign and hanging-sign blocks use factory WoodType and vanilla sign block
  entities; standing/wall variants follow the exact suffix rules above;
- Decoration suffixes are singular `slab`, plural `stairs`, singular `wall`.

These are historical facts, not Stage 2 registrations. Owning stages must map
them to 1.21.10 behavior and test observable equivalence.

## F. Content ownership assignments

| Candidate family/content | Owner | Stage 2 disposition |
|---|---|---|
| Blighted Balsa, mushroom/glowshroom masonry, blighted stone, Glowfish foods | Stage 3 Mushroom Fields | Deferred intact |
| Terracotta/cracked bricks, Badlands plants, archaeology materials, Cowboy/Scuttler content | Stage 4 Badlands | Deferred intact |
| Willow, Swamp Cypress, peat masonry, Reed Thatch, swamp plants/materials | Stage 5 Swamp | Deferred intact |
| Ancient Oak, Mesmerite, Dark Forest flora | Stage 6 Dark Forest | Existing slice protected; parity work deferred |
| Rootling/Moth materials and foods; Helmit Crab foods; other mob drops/buckets/eggs | Owning entity stage (principally Stage 8) | Deferred; no placeholders |
| Altar, Poltergeist, composters, Lightning Bug Bottle, tapestries and block entities | Stage 9 | Deferred intact |
| Mushroom House building use | Stage 10A, dependent on Stage 3 | Does not change ownership |
| Sunken Ruins building use | Stage 10B, dependent on Stage 5 | Does not change ownership |
| Ghost Town/archaeology use | Stage 10C, dependent on Stage 4 | Does not change ownership |
| Illunite, cladding, hats, quest/curse items, armor, progression rewards | Stage 12 | Deferred; no harmless-looking placeholders |
| Cross-cutting omission audit | Stage 13 | Not ordinary-content deferral |
| Dead/unreachable and divergent dev content | Excluded | Never automatic parity work |

Foods are owned by their ingredient/entity theme. Effects are also deferred:
Shocked belongs with Lightning Bottle, Antidote with Scuttler/Stuntable and its
advancement, Nocturnal with Moth/brewing, and Possessed with Poltergeist. None
is complete and independently testable in Stage 2.

## G. Boat and chest-boat deferral

Historical boat items are not generic cosmetic items. `TBoatItem` references
the corresponding BM/Taniwha `BoatType`; BM registers four boat types and
client rendering. Stage 2 must not approximate them with an unrelated vanilla
wood or silently reuse another entity identity. Each wood-owning stage records
the exact boat/chest-boat IDs, while actual shared boat entity/type plumbing is
introduced only when its historical serialization and rendering contract is
defined and existing-world safe.

## H. Deterministic validation

The parity validator now asserts:

- Decoration `.all()` has exactly slab/stairs/wall;
- Wood `.all()` has exactly 17 block paths, 13 ordinary block-item paths and
  four special item paths;
- leaves and saplings remain separate;
- ownership entries are unique and nonempty;
- current registry/resource/dependency contracts remain unchanged.

The manifest contains no timestamps, generated registry entries or production
resources. It is stable, sorted where set semantics apply, and diff-friendly.

## I. Registry and existing-world result

Expected and actual registry delta: zero. No existing ID is removed, renamed
or reused; no historical ID is prematurely added. No Owl, worldgen, tag or
gameplay resource changes occur, so Stage 2 adds no new existing-world risk.

## J. Validation results

Completion requires `git diff --check`, parity/family/dependency validation,
a clean offline build, registry count equality and a byte-identical production
JAR compared with Stage 1. Manual gameplay runtime testing is unnecessary for
a packaged byte-identical result, but the Stage 0 existing-world protocol
remains mandatory before future registry/content stages.

Completion result: parity validation passed deterministically; registry counts
remain 28 blocks, 31 items, one entity, eight sounds, 13 configured features,
13 placed features and three injected features. `gradlew.bat clean build
--offline` succeeded with no test sources. The production JAR SHA-256 is
`770ECF61E17B9FD5B8225341CC628D64CC9627F2DB716B4D5F1DB95F2CE52E6B`,
exactly matching Stage 1. The sources JAR also matches Stage 1 at
`1CE7E3F10F827F75EB2374A37D1AF6BA06B1631C81E9C9F0DC65392FBA59EA39`.
No validation/document file is packaged.

## K. Known unresolved items

- Boat/type serialization and modern renderer design remain deferred.
- Taniwha `suspicious_block_replacement` remains unresolved for Stage 10C.
- Historical runtime registry/tag dumps remain useful corroboration, although
  factory membership and factory properties are now source-conclusive.
- The two current plural sapling tag files remain grandfathered and unchanged;
  their correction belongs to Ancient Oak Stage 6 with runtime tag testing.

## L. Stage 3 entry criteria

Stage 3 may begin only after this contract is committed and reviewed, the
validator/build/JAR identity checks pass, the worktree is clean, and the
preservation tag remains valid. Stage 3 must establish exact Mushroom Fields
family manifests—including Blighted Balsa special members—before registering
them, complete their ordinary resources in the same stage, and retain the boat
deferral unless the boat contract is separately resolved. Stage 3 does not
begin automatically.
