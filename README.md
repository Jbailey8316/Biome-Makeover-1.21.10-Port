# Stage 8.9 — Rare Canopy Owls, Sounds, Shoulder Perching

Target: Minecraft 1.21.10 / Fabric API 0.138.4+1.21.10 / Java 21.

Changes:
- Dark Forest owl spawn weight reduced from 8 to 2; groups remain 1–2.
- Natural spawning is nighttime-only and requires an exposed canopy position above logs or leaves.
- Tamed owls use Minecraft's owner-shoulder landing goal.
- Owls retain ordinary passenger/vehicle behavior, allowing boats to make wild owls persistent under normal mob rules.
- Raw rabbit: taming, temptation, breeding, and healing (4 HP).
- Raw chicken: healing only (3 HP).
- Existing subtle coo remains as the ordinary idle sound.
- New original Minecraft-style sounds: two hoots, owner contact chirp, wild alert bark/clack, baby peep, and feather-rush takeoff asset.
- Hoots are uncommon and primarily occur while perched at night.

Build: `./gradlew build` or `build-windows.bat`.

First tests:
1. Search a newly loaded Dark Forest canopy over multiple nights; expect rare singles and occasional pairs.
2. Tame with raw rabbit, leave the owl standing, and walk beneath/near it to test shoulder landing.
3. Push a wild owl into a boat, move away or wait through daylight, and confirm it persists as a passenger.
4. Damage a tame owl: both raw rabbit and raw chicken heal it; only rabbit breeds/tempts.
5. Listen at night for occasional deeper hoots while the original subtle coo remains.

This package was not compiled in the assistant environment; the Windows build is the first API validation, especially for `LandOnOwnersShoulderGoal`.
