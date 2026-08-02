#!/usr/bin/env bash
set -euo pipefail
java -version
./gradlew --no-daemon clean build
echo "Build complete. Check build/libs/"
