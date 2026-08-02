# Stage 3.1 validation target

This patch addresses two test findings from Stage 3:

1. Recipes were not recognized because ingredient entries retained the older object form.
2. Mesmerite walls connected to full blocks but not each other because they were absent from `minecraft:walls`.

Expected tests:
- crafting recipes work when ingredients are manually placed;
- stonecutter lists Mesmerite outputs;
- Mesmerite and polished Mesmerite walls connect to themselves and each other;
- existing placed blocks remain intact across the upgrade.
