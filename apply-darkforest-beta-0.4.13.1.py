from pathlib import Path
import re

files = [
    Path("src/main/java/party/lemons/biomemakeover/block/BlackThistleBlock.java"),
    Path("src/main/java/party/lemons/biomemakeover/block/ItchingIvyBlock.java"),
]

for f in files:
    if not f.exists():
        raise SystemExit(f"Missing {f}")
    text = f.read_text(encoding="utf-8")
    new_text, count = re.subn(r'(?m)^[ \t]*@Override[ \t]*\r?\n', '', text)
    f.write_text(new_text, encoding="utf-8")
    print(f"{f}: removed {count} override annotation(s)")

print("Compile fix applied.")
