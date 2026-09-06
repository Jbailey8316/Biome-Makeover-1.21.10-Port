# Stage 13H — Released Wood Boat / Chest-Boat Parity

Status: RUNTIME ACCEPTED / CLEANED / FROZEN

The final released package contains four normal boats and four chest boats:
Ancient Oak, Willow, Swamp Cypress, and Blighted Balsa. The released IDs,
recipes, item models, item textures, entity textures, and the released normal
boat tag are reference-backed. Minecraft 1.21.10 has separate boat and
chest-boat entity types rather than the released Taniwha `BoatType`; the port
uses one native `Boat` or `ChestBoat` entity type per released ID and native
`BoatItem` placement. Custom model layers let the native renderer resolve the
released `textures/entity/boat/<wood>` and `<wood>_chest` textures.

The current `chest_boats` tag is a 1.21.10 compatibility representation of
the released chest-boat family. No custom boat movement, inventory, or drop
logic is introduced.

Stage 13H-R1 audit: the released entity textures are packaged at
`textures/entity/boat/<wood>.png` and `<wood>_chest.png`. The native 1.21.10
BoatRenderer derives its lookup from the model-layer model path, so renderer
layer IDs must use `boat/<wood>` and `boat/<wood>_chest`; flat IDs incorrectly
request `textures/entity/<id>.png`. R1 changes only these model-layer resource
paths and retains the released artwork and all boat behavior.

Runtime acceptance must cover inventory rendering, placement, steering,
break/drop, save/reload, chest inventory persistence, and all four wood
families. Prism acceptance confirmed those behaviors, localized entity names,
and the absence of boat texture warnings for all eight released entity IDs.

The released IDs are:
`ancient_oak_boat`, `ancient_oak_chest_boat`, `willow_boat`,
`willow_chest_boat`, `swamp_cypress_boat`, `swamp_cypress_chest_boat`,
`blighted_balsa_boat`, and `blighted_balsa_chest_boat`. Their entity names use
the released English boat item names, including `Boat with Chest` for chest
boats. Recipes, native Boat/ChestBoat behavior, and representative persistence
and drops remain unchanged.
