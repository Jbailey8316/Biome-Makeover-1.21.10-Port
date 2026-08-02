
# Owl Full System Integration Pass

This checkpoint consolidates the remaining owl feature targets.

## Existing systems to preserve
- Adult owl behavior
- Taming
- Hunting
- Shoulder perching
- Nest claiming
- Nest sleeping
- Sleeping animation lock
- Basket nest visuals

## Baby owl system
Target implementation:
- Dedicated baby state (not vanilla Age NBT)
- Debug spawning:
  /biomemakeover owl adult
  /biomemakeover owl baby
- Baby model:
  - oversized head
  - smaller body
  - shorter wings
  - fluffy appearance
- Baby does not claim nests
- Baby may sleep in nests

## Egg/nest system
Target:
- Nest has one egg maximum
- Egg visible inside nest
- Egg placement by player
- Breeding uses existing nest when available
- Nest creation when needed

## Family behavior
- Adults own nests
- Babies are guests
- Parents protect eggs briefly when disturbed
- Babies follow adults

## Testing checklist
1. Adult summon
2. Baby summon
3. Nest claim
4. Nest sleep
5. Egg placement
6. Baby nest sleep
7. Growth behavior
