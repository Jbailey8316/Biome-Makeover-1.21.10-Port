# Stage 11B.0 — Released Loot Infrastructure

The final released source uses `biomemakeover:loot_table`, implemented by
`BetterLootTableReference`. It is a generic nested loot entry that resolves a
referenced table through the active loot context and emits its generated
stacks. The 1.21.10 port retains the released `name` JSON field and canonical
registry ID, using a modern `MapCodec` and `LootPoolEntryType`.

The seven released Mansion tables are now packaged under the modern singular
`data/biomemakeover/loot_table/mansion/` path: `arrows`, `dungeon`,
`dungeon_good`, `dungeon_junk`, `good`, `junk`, and `standard`.

Mansion marker activation remains blocked. The released tables depend on
items owned by later stages: `cladded_boots`, `cladded_chestplate`,
`cladded_leggings`, `crude_cladding`, and `red_rose_music_disk`. No entries
were removed or substituted. The custom loot entry is infrastructure only;
Mansion loot markers remain inactive until these dependencies receive their
source-correct registrations and resources.

## Separate runtime blockers

The Rootling `Accessing LegacyRandomSource from multiple threads` failure at
`RootlingEntity.<init>` remains a confirmed separate remediation. Dungeon
fluid/water parity also remains unresolved and untouched.

## Ownership recommendation

Implement the small Red Rose disc dependency with its own 11B.2 boundary, then
perform a bounded Stage 12 item-registration substrate for the cladded/crude
loot objects. Resume 11B.1 only after every released Mansion table resolves
without substitutions or missing IDs.

## 11B.2A dependency update

The standalone `red_rose_music_disk` item is now registered with its released
`red_rose` streamed song, 135-second duration, comparator output 2, rare
single-stack presentation, model, texture, translation, and music-disc tag.
The remaining unresolved Mansion loot dependencies are the three cladded
armor pieces and `crude_cladding`; Mansion loot markers remain inactive.
