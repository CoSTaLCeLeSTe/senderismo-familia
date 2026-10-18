#!/usr/bin/env bash
# Activa Paparazzi solo en CI y genera capturas PNG de las pantallas.
set -euo pipefail

sed -i 's|^plugins {$|plugins {\n    id("app.cash.paparazzi") version "1.3.5" apply false|' build.gradle.kts
sed -i 's|^    id("org.jetbrains.kotlin.plugin.compose")$|    id("org.jetbrains.kotlin.plugin.compose")\n    id("app.cash.paparazzi")|' app/build.gradle.kts

echo "--- build.gradle.kts"; cat build.gradle.kts
echo "--- app plugins"; head -6 app/build.gradle.kts

./gradlew :app:recordPaparazziDebug --stacktrace --no-daemon
