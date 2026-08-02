from pathlib import Path
import re

root = Path.cwd()

black = root / "src/main/java/party/lemons/biomemakeover/block/BlackThistleBlock.java"
ivy = root / "src/main/java/party/lemons/biomemakeover/block/ItchingIvyBlock.java"
owl = root / "src/main/java/party/lemons/biomemakeover/entity/OwlEntity.java"

for p in (black, ivy, owl):
    if not p.exists():
        raise SystemExit(f"Missing expected file: {p}")

# Remove every @Override annotation in these two small custom block classes.
# Their current collision signatures do not override the mapped 1.21.10 superclass methods.
for p in (black, ivy):
    text = p.read_text(encoding="utf-8")
    text, count = re.subn(r'(?m)^[ \t]*@Override[ \t]*\r?\n', '', text)
    p.write_text(text, encoding="utf-8")
    print(f"{p.name}: removed {count} @Override annotation(s)")

# Ensure OwlEntity imports Blocks.
text = owl.read_text(encoding="utf-8")
needed = "import net.minecraft.world.level.block.Blocks;"
if needed not in text:
    package_end = text.find("\n", text.find("package "))
    # Insert with imports, before the first existing import.
    first_import = text.find("import ")
    if first_import >= 0:
        text = text[:first_import] + needed + "\n" + text[first_import:]
    else:
        text = text[:package_end+1] + "\n" + needed + "\n" + text[package_end+1:]
    owl.write_text(text, encoding="utf-8")
    print("OwlEntity.java: added Blocks import")
else:
    print("OwlEntity.java: Blocks import already present")

# Verify the exact failing patterns are gone.
for p in (black, ivy):
    text = p.read_text(encoding="utf-8")
    if "@Override" in text:
        raise SystemExit(f"Verification failed: @Override still present in {p}")

if needed not in owl.read_text(encoding="utf-8"):
    raise SystemExit("Verification failed: Blocks import missing from OwlEntity.java")

print("0.4.11.3 compile fix applied and verified.")
