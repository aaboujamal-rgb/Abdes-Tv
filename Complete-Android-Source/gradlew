#!/bin/sh
#
# Abdes TV Gradle Wrapper Script (Linux / macOS / CI)
# Automatically uses system Gradle if available or bootstraps Gradle 8.7 if gradle-wrapper.jar is absent.
#
set -e

APP_HOME="$( cd "$( dirname "$0" )" && pwd )"
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
GRADLE_VERSION="8.7"

if [ -f "$WRAPPER_JAR" ]; then
    exec java -Xmx64m -Xms64m -jar "$WRAPPER_JAR" "$@"
elif command -v gradle >/dev/null 2>&1; then
    exec gradle "$@"
else
    GRADLE_CACHE_DIR="${HOME}/.gradle/abdestv-bootstrap/gradle-${GRADLE_VERSION}"
    GRADLE_BIN="${GRADLE_CACHE_DIR}/bin/gradle"
    if [ ! -x "$GRADLE_BIN" ]; then
        echo "Downloading Gradle ${GRADLE_VERSION} distribution..."
        mkdir -p "${HOME}/.gradle/abdestv-bootstrap"
        TMP_ZIP="${HOME}/.gradle/abdestv-bootstrap/gradle-${GRADLE_VERSION}-bin.zip"
        curl -fsSL "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -o "$TMP_ZIP"
        unzip -q -o "$TMP_ZIP" -d "${HOME}/.gradle/abdestv-bootstrap"
        rm -f "$TMP_ZIP"
    fi
    exec "$GRADLE_BIN" "$@"
fi
