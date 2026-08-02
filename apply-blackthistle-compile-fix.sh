#!/bin/bash
# Removes the invalid override annotation from BlackThistleBlock.
# Run from repo root.

FILE="src/main/java/party/lemons/biomemakeover/block/BlackThistleBlock.java"

if [ -f "$FILE" ]; then
  sed -i '/@Override/d' "$FILE"
  echo "Removed invalid @Override from BlackThistleBlock.java"
else
  echo "ERROR: BlackThistleBlock.java not found"
  exit 1
fi
