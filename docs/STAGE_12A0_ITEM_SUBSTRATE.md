# Stage 12A.0 — Cladded / Crude Item Registration Substrate

The released source defines `cladded_helmet`, `cladded_chestplate`,
`cladded_leggings`, and `cladded_boots` as an iron-durability armor set with
iron defense values, enchantability 15, leather equip sound and repair tag,
zero toughness, and 0.07 knockback resistance. The released set includes a
helmet even though current Mansion loot tables select only the other three
pieces.

The port uses Minecraft 1.21.10's `humanoidArmor` item properties and a
canonical `ArmorMaterial` with a dedicated `cladded` equipment asset. Released
item models, icons, armor-layer textures, translations, and no acquisition
recipes are included. Modern trim-compatible equipment rendering is supplied
by the normal armor component path.

`crude_cladding` is an ordinary released `Item` with no custom use behavior in
the final source. It is registered here only as the canonical loot dependency;
Crude/cladding progression, upgrades, recipes, and broader gameplay remain
Stage 12 deferred.

Mansion loot markers remain inactive. The four previously unresolved loot item
IDs now have registrations, reducing the dependency count from 4 to 0.
Templates, Mansion layout, Stage 10C, Rootling thread-safety work, and dungeon
fluid parity are untouched.
