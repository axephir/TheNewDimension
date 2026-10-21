#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
java_bin="${JAVA_HOME:+$JAVA_HOME/bin/}java"
javac_bin="${JAVA_HOME:+$JAVA_HOME/bin/}javac"
mkdir -p build/core-test
mapfile -t sources < <(find src/main/java/fr/axephir/tnd/domain src/coreTest/java -name '*.java' -print)
"$javac_bin" -encoding UTF-8 -d build/core-test "${sources[@]}"
"$java_bin" -ea -cp build/core-test fr.axephir.tnd.domain.CoreTests
