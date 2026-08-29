# Stage 9A — Independent Functional Utilities

Status: **COMPLETE / RUNTIME ACCEPTED**

Authority is the final released Biome Makeover 1.20.1 source. This checkpoint restores only Stunt Powder with the global ageable-mob `Stuntable` contract and the Peat Composter. Altar/curses, Witch progression, structures, Ghost/Ectoplasm, Mansion progression and all Stage 9B+ content remain excluded.

## Stunt Powder contract

- Canonical item: `biomemakeover:stunt_powder`.
- Shapeless recipe: one `biomemakeover:illunite_shard` plus one `biomemakeover:bulbus_root` produces two powders.
- Final source integrates `Stuntable` into `AgeableMob`, so normal ageable vanilla and BM mobs are eligible. An eligible target must currently be a baby (or implement the historical always-baby exception) and must not already be stunted. Disabled Toad/Tadpole content is not activated.
- A successful server-authoritative use sets persistent `bm_IsStunted`, holds age at `-6000`, emits 15 warped-spore particles with 0.2 spread, consumes one item under vanilla survival/creative semantics, and emits the entity-interact game event. Final source adds no sound or advancement.
- Adults and already-stunted targets fall through without consumption or duplicate effects.

The 1.21.10 translation mixes into `AgeableMob.setAge` rather than replacing aging. The first application calls vanilla `setAge(-6000)` before the stunted flag is latched, allowing vanilla to synchronize baby state and refresh dimensions/render state. Later age changes are rejected while stunted. The flag is serialized through modern `ValueInput`/`ValueOutput`. Normal babies, breeding cooldowns and the dedicated Owl baby model pipeline remain untouched.

## Peat Composter contract

- Canonical block: `biomemakeover:peat_composter`, deliberately registered without a block item.
- It has no recipe, block entity, menu, inventory or packet system.
- A full vanilla Composter (`level=8`) beneath a water-fed downward pointed-dripstone chain is scheduled through the vanilla dripstone transfer cadence, then becomes the Peat Composter. Rain, direct water and ordinary contact do not convert it.
- Comparator output is 9.
- Player use ejects exactly one Peat into the world, resets the position to an empty vanilla Composter, plays the composter-empty sound and triggers `biomemakeover:peat_compost` for the player. The `biomemakeover:biomemakeover/create_peat` advancement is translated to the 1.21.10 item-stack icon schema.
- Hopper extraction is exposed only downward through the block's transient sided-container contract. A successful extraction returns one Peat and resets the block; it does not trigger the player advancement.
- Breaking returns one vanilla Composter. It never drops Peat or an unobtainable Peat Composter item.

Final BM delegated water-drip scheduling to Taniwha. The port supplies only the required local mixins around vanilla pointed-dripstone scheduling and the Composter scheduled tick; it introduces no Taniwha dependency.

## Validation guardrails

The parity validator now asserts the Stage 9A registry delta, exact Stunt Powder recipe, persistent key and held age, server interaction/particle/consumption wiring, itemless and recipe-less Peat Composter, comparator/reset/extraction contracts, water-drip hooks, vanilla-Composter loot, advancement trigger, mixin wiring and absence of representative Stage 9B+ registries.

## Prism acceptance checklist

### Stunt Powder

1. Craft one Illunite Shard plus one Bulbus Root in any arrangement; verify two Stunt Powder.
2. Use it on a supported vanilla baby; verify one powder is consumed in survival and warped-spore particles appear.
3. Try the same target again; verify no consumption or duplicate effect.
4. Try an adult; verify it does not become a baby and powder is not consumed.
5. Keep an unstunted baby control nearby. Save/reload, then wait or accelerate aging: the control must grow while the stunted target stays logically, visually and physically a baby.
6. In creative, verify successful use does not shrink the stack.
7. Breed Owls and confirm the ordinary Owl baby model, dimensions and aging still work.

### Peat Composter

1. Fill a vanilla Composter to level 8. Above it, leave the drip path clear and place a downward pointed-dripstone stalactite fed by water on the supporting block. Wait for a natural drip; verify conversion.
2. Read the converted block with a comparator; verify signal 9.
3. Use it in survival; verify exactly one dropped Peat, reset to empty vanilla Composter and the create-peat advancement where applicable.
4. Convert another, break it, and verify exactly one vanilla Composter with no Peat/Peat-Composter item.
5. Convert another with a hopper below; verify exactly one Peat transfers and the block resets. Verify side automation cannot extract.
6. Save/reload while a converted block exists and repeat extraction.

## Runtime acceptance

Prism testing used Minecraft 1.21.10, Fabric Loader 0.19.3, Fabric API 0.138.4+1.21.10 and Biome Makeover 1.21.10-0.8.5. The client and integrated server loaded normally with no Stage 9A startup or blocking runtime error.

- Stunt Powder applied successfully to an eligible baby cow. The treated cow remained a baby while an untreated control aged normally, proving the global mixin does not freeze ordinary babies. Adult and repeated-use rejection, synchronized baby rendering/physical state and save/reload persistence passed.
- Natural water/dripstone conversion of a full vanilla Composter passed. Manual extraction returned exactly one Peat and reset the block. Breaking returned the vanilla Composter. Downward hopper extraction returned Peat from an already converted block.
- `Re-Peat` and `For Peat's Sake` fired during the Peat test. This matches final data: player extraction triggers `peat_compost`/`create_peat`, while acquiring the resulting Peat satisfies the existing inventory criterion.
- An active hopper beneath the *vanilla* full Composter can extract its vanilla Bone Meal before a water drip converts it. Final 1.20.1 adds no hopper lock or interception to the vanilla pre-conversion block, so this race is expected parity behavior. Automation must lock the hopper until conversion; no convenience override was added.

The accepted Stage 7 Owl renderer still selects its dedicated adult/baby model pair from `entity.isBaby()`. Stage 9A changed no Owl source or renderer. Vanilla age mutation remains untouched for every non-stunted Owl, including baby growth and breeding cooldowns; only entities whose persistent stunted flag is true reject subsequent age changes.

## Static checkpoint results

- Clean offline Gradle build: PASS; Java test task reports `NO-SOURCE` (the repository has no automated Java tests).
- Parity/package validator: PASS at 261 blocks, 272 items, 12 entities, 1 block entity, 40 sounds and 2 particles; worldgen remains unchanged at 38 configured, 37 placed and 31 injected features.
- Packaged totals: 2,877 entries, 278 blockstates, 600 block models, 272 item definitions, 274 item models, 286 PNG textures, 329 recipes, 271 loot tables, 33 advancements and 78 tag files.
- Texture-count reconciliation: the Stage 8 total of 285 counted PNG files. The initial Stage 9A figure of 315 counted every JAR entry below `textures/`: 286 PNGs, 14 animation `.mcmeta` files and 15 directory entries. Stage 9A added exactly one PNG (`stunt_powder.png`); no unrelated or duplicate texture was introduced.
- Dedicated-server bootstrap: attempted offline, but Loom could not resolve uncached `net.fabricmc:fabric-log4j-util:1.0.2`; no dependency or Gradle change was made. Dedicated-server gameplay was not exercised in this acceptance pass; the successful integrated-server test is the available runtime evidence.
