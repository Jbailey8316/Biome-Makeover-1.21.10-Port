# DarkForest Beta 0.4.11.3 — Guaranteed Compile Fix

This version removes **all** `@Override` annotations from the two affected
custom block classes instead of trying to match a specific method name.

It also inserts and verifies the missing `Blocks` import in `OwlEntity`.

Apply:

```bash
bash apply-darkforest-beta-0.4.11.3.sh
./gradlew clean build
```
