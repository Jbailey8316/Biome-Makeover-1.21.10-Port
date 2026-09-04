# Stage 11B.1R.20R.4 - Boss Dry/Flooded Forensic Comparison

## Audit status

The referenced Prism logs are not present in this repository or its local build/workspace artifacts. Searches for `69bd9510`, `f9790208`, `4688,64,336`, `explicitAirCleared=22`, and the requested BM markers returned no runtime log files. Consequently, exact per-run rotations, bounds, chunk callback order, terrain measurements, neighbor topology, and water-proximity data cannot be recovered from local evidence.

## Evidence currently available

| Field | Dry run | Flooded B | Flooded C |
|---|---|---|---|
| Origin | `[4688,64,336]` | `[4688,64,336]` | `[4688,64,336]` |
| Layout signature | unavailable | `69bd9510` reported by stage notes | `f9790208` reported by stage notes |
| Structural pieces | unavailable | 52 (stage notes) | unavailable |
| Expected placements | unavailable | 114 (stage notes) | unavailable |
| Union positions | unavailable | 28925 (stage notes) | unavailable |
| Boss reconcile | dry, exact counters unavailable | `explicitAirCleared=22` (stage notes) | `explicitAirCleared=0` (stage notes) |
| R0 | unavailable | `explicitDryWater=22` (stage notes) | unavailable |

The local source confirms `boss_room.nbt` is a single template selected by the Mansion layout and that current native liquid placement plus the bounded boss explicit-air safeguard are the active mechanisms. It does not contain historical Prism run identities or world snapshots.

## Classification

No A-G classification is made at this forensic stage. Classification remains pending until runtime evidence is imported.

## RUNTIME EVIDENCE REQUIRED

Import one compact evidence block for each known layout. Preserve the exact field names below so the three runs can be compared directly.

### DRY - 3bf0535e

- `layoutInstanceId`
- `structuralPieces`
- `expectedPlacementCount`
- `unionPositions`
- `boss_room` rotation
- boss piece ordinal/order
- boss bounds
- boss chunk placements
- Mansion terrain/baseY data
- READY timestamp
- EXECUTE_END timestamp
- REMOVE timestamp
- `BM_BOSS_ROOM_FINAL_RECONCILE`
- `BM_BOSS_ROOM_LATE_AUDIT` if available
- `BM_HYDRAULIC_SUMMARY`
- `BM_HYDRAULIC_WATER_VOLUME`
- user visual dry/flood timestamp

### FLOODED A - 69bd9510

- `layoutInstanceId`
- `structuralPieces`
- `expectedPlacementCount`
- `unionPositions`
- `boss_room` rotation
- boss piece ordinal/order
- boss bounds
- boss chunk placements
- Mansion terrain/baseY data
- READY timestamp
- EXECUTE_END timestamp
- REMOVE timestamp
- `BM_BOSS_ROOM_FINAL_RECONCILE`
- `BM_BOSS_ROOM_LATE_AUDIT` if available
- `BM_HYDRAULIC_SUMMARY`
- `BM_HYDRAULIC_WATER_VOLUME`
- user visual dry/flood timestamp

### FLOODED B - f9790208

- `layoutInstanceId`
- `structuralPieces`
- `expectedPlacementCount`
- `unionPositions`
- `boss_room` rotation
- boss piece ordinal/order
- boss bounds
- boss chunk placements
- Mansion terrain/baseY data
- READY timestamp
- EXECUTE_END timestamp
- REMOVE timestamp
- `BM_BOSS_ROOM_FINAL_RECONCILE`
- `BM_BOSS_ROOM_LATE_AUDIT` if available
- `BM_HYDRAULIC_SUMMARY`
- `BM_HYDRAULIC_WATER_VOLUME`
- user visual dry/flood timestamp

## COMPARISON BLOCKED UNTIL EVIDENCE IMPORT

Do not infer missing runtime facts or classify categories A-G until the three evidence blocks above are populated from the extracted Prism logs.

## IMPORTED COMPACT RUNTIME EVIDENCE

### Common location and terrain

All three runs used `origin=[4688,64,336]` and `structureChunk=[293,21]`. The following values are identical across all three: `sampledHeight=65`, `baseY=64`, `firstFloorY=64`, `roofReferenceY=106`, `dungeonTopY=63`, `dungeonBottomY=56`, `minSurfaceY=56`, `maxSurfaceY=72`, `medianSurfaceY=63`, `meanSurfaceY=63.92`, `releasedAnchorY=65`, `enhancedBaseY=64`, `maxTerrainAboveBase=8`, `maxGapBelowBase=8`, `terrainSpread=16`, `siteSuitable=true`.

### DRY - 3bf0535e

- Visual: `boss room not flooded`; user confirmation `18:26:40`
- layoutInstanceId=`3bf0535e`; structuralPieces=45; expectedPlacementCount=108; unionPositions=26832; bossRoomPieces=1
- LAYOUT_COMPLETE=`18:25:55`; RUNTIME_REGISTER=`18:25:59`
- boss piece: `biomemakeover:mansion/boss_room`; ordinal=51
- boss_room rotation: `UNKNOWN FROM AVAILABLE RUNTIME EVIDENCE`
- boss bounds: not supplied; first explicit-air clip `minX=4656 minZ=320 maxX=4671 maxZ=335`
- READY=`18:26:12`, pieceCount=45, unionPositions=26832
- EXECUTE_END=`18:26:12`, correctedAir=0, correctedWaterlogged=0, authoredWetPreserved=0
- REMOVE=`18:26:12`
- BM_BOSS_ROOM_FINAL_RECONCILE=`18:26:12`, explicitAirCleared=0
- BM_BOSS_ROOM_LATE_AUDIT: not supplied
- Boss callbacks: `[291,20]=89`, `[291,19]=93`, `[290,19]=97`, `[290,20]=98`, `[290,18]=99`, `[291,18]=100`, `[289,20]=106`, `[289,19]=107`, `[289,18]=108`
- BM_HYDRAULIC_SUMMARY: analysisCells=56729; structuralInteriorCells=3747; exteriorConnectedCells=30271; interiorExteriorConnectedCells=3747; leakFaces=20639; sourceWaterLeakFaces=0; flowingWaterLeakFaces=0; airLeakFaces=20639; waterloggedLeakFaces=0; dungeonLeakFaces=20639; stairLeakFaces=0; bossLeakFaces=0
- BM_HYDRAULIC_WATER_VOLUME: explicitDryWater=0; authoredWetWater=0; omittedInteriorWater=0; exteriorConnectedWater=0; unknownWater=0
- BM_BOSS_HYDRAULIC_SUMMARY: bossInteriorCells=0; bossWaterCells=0; bossExteriorConnectedCells=0; bossLeakFaces=0 (known unreliable ownership)

### FLOODED A - 69bd9510

- Visual: `boss room flooded`; user confirmation `18:39:02`
- layoutInstanceId=`69bd9510`; structuralPieces=52; expectedPlacementCount=114; unionPositions=28925; bossRoomPieces=1
- LAYOUT_COMPLETE=`18:38:02`; RUNTIME_REGISTER=`18:38:02`
- boss piece: `biomemakeover:mansion/boss_room`; ordinal=101
- boss_room rotation: `UNKNOWN FROM AVAILABLE RUNTIME EVIDENCE`; boss bounds: not supplied
- READY=`18:38:20`, pieceCount=52, unionPositions=28925
- EXECUTE_END=`18:38:20`, correctedAir=0, correctedWaterlogged=0, authoredWetPreserved=0
- REMOVE=`18:38:20`
- BM_BOSS_ROOM_FINAL_RECONCILE=`18:38:20`, explicitAirCleared=22
- BM_BOSS_ROOM_LATE_AUDIT: not supplied
- Boss callbacks known: `[293,14]=112`, `[292,14]=113`, `[291,14]=114`; earlier callbacks are missing and not invented
- BM_HYDRAULIC_SUMMARY: analysisCells=46410; structuralInteriorCells=5068; exteriorConnectedCells=24845; interiorExteriorConnectedCells=5027; leakFaces=27581; sourceWaterLeakFaces=63; flowingWaterLeakFaces=0; airLeakFaces=27518; waterloggedLeakFaces=0; dungeonLeakFaces=27581; stairLeakFaces=0; bossLeakFaces=0
- BM_HYDRAULIC_WATER_VOLUME: explicitDryWater=0; authoredWetWater=0; omittedInteriorWater=0; exteriorConnectedWater=0; unknownWater=0

### FLOODED B - f9790208

- Visual: `boss room flooded`; user confirmation `18:47:23`
- layoutInstanceId=`f9790208`; structuralPieces=42; expectedPlacementCount=89; unionPositions=25740; bossRoomPieces=1
- LAYOUT_COMPLETE=`18:46:56`; RUNTIME_REGISTER=`18:46:57`
- boss piece: `biomemakeover:mansion/boss_room`; ordinal=107
- boss_room rotation: `UNKNOWN FROM AVAILABLE RUNTIME EVIDENCE`
- boss bounds: not supplied; first explicit-air clip `minX=4688 minZ=384 maxX=4703 maxZ=399`
- READY=`18:47:08`, pieceCount=42, unionPositions=25740
- EXECUTE_END=`18:47:08`, correctedAir=0, correctedWaterlogged=0, authoredWetPreserved=8
- REMOVE=`18:47:09`
- BM_BOSS_ROOM_FINAL_RECONCILE=`18:47:09`, phase=C7, explicitAirCleared=0, cropStatesRestored=0
- BM_BOSS_ROOM_LATE_AUDIT: not supplied
- Boss callbacks: `[293,24]=70`, `[294,24]=71`, `[292,24]=74`, `[292,25]=84`, `[293,25]=85`, `[294,25]=86`, `[292,26]=87`, `[293,26]=88`, `[294,26]=89`
- BM_HYDRAULIC_SUMMARY: analysisCells=170274; structuralInteriorCells=11830; exteriorConnectedCells=96462; interiorExteriorConnectedCells=11789; leakFaces=62260; sourceWaterLeakFaces=87; flowingWaterLeakFaces=0; airLeakFaces=62173; waterloggedLeakFaces=0; dungeonLeakFaces=62260; stairLeakFaces=0; bossLeakFaces=0
- BM_HYDRAULIC_WATER_VOLUME: explicitDryWater=0; authoredWetWater=8; omittedInteriorWater=0; exteriorConnectedWater=0; unknownWater=0

## Evidence comparison and classification

Proven differences are identical terrain/site metrics, sourceWaterLeakFaces of `0/63/87` (dry/Flooded A/Flooded B), final explicitAirCleared of `0/22/0`, and materially different piece counts, union sizes, boss ordinals, and boss chunk placements. The `0` versus `63/87` source-water counts are the strongest correlation only; no leak-face coordinates were supplied to relate them to transformed boss geometry. Flooded B also flooded with explicitAirCleared=0, and the existing Mansion-wide hydraulic boss ownership (`bossInteriorCells=0`, `bossLeakFaces=0`) is unreliable.

The source confirms `BossRoom#getRotation` derives rotation from neighboring direction (south -> counterclockwise 90, north -> clockwise 90, east -> clockwise 180, west -> none), but exact neighboring layouts and trustworthy runtime rotations are absent. Top-level terrain/elevation is not supported as the primary difference; topology, placement order, rotation, and natural-water dependence remain unresolved.

ROOT-CAUSE CLASSIFICATION: UNRESOLVED - TARGETED BOSS-BOUNDARY TRACE REQUIRED

Do not classify A-G or use G as a missing-evidence label. The smallest required diagnostic logs each source-water leak face with interior/exterior positions, face, fluid/source status, nearest template and piece ordinal, joined to transformed boss bounds, rotation, complete callback order, explicit-air mask, and first wet position/phase inside the boss mask.

## COMPARISON BLOCKED UNTIL EVIDENCE IMPORT

Evidence import is complete, but causal comparison remains blocked until source-water leak faces are spatially joined to transformed boss-room geometry.

## Required next experiment

Repeat the same-seed comparison with runtime trace enabled and preserve the complete logs for the dry and flooded instances. Capture the transformed `boss_room` origin/rotation/bounds/chunks, explicit-air target set, first external water position and phase, and neighboring Piece descriptors. That single paired capture is the smallest experiment capable of separating transform/topology/order from natural-water dependence.
