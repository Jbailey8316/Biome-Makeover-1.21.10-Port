# Stage 13H — Released Wood Boat / Chest-Boat Parity

Status: IMPLEMENTED / PRISM RUNTIME REQUIRED

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

Runtime acceptance must cover inventory rendering, placement, steering,
break/drop, save/reload, chest inventory persistence, and all four wood
families before Stage 13H can be frozen.
