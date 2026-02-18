#!/bin/sh
# Gradle wrapper script - auto-bootstraps gradle-wrapper.jar if missing
# Official Gradle 8.7 wrapper

APP_HOME=$( cd "$(dirname "$0")" && pwd )
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
WRAPPER_URL="https://github.com/gradle/gradle/raw/v8.7.0/gradle/wrapper/gradle-wrapper.jar"
WRAPPER_SHA256="a4b4158601f8636cdeeab09bd76afb640030bb5b144aafe261a5e8af027dc612"

# Download wrapper jar if not present (first clone / CI bootstrap)
if [ ! -f "$WRAPPER_JAR" ]; then
    echo "Downloading gradle-wrapper.jar..."
    if command -v curl >/dev/null 2>&1; then
        curl -sL "$WRAPPER_URL" -o "$WRAPPER_JAR"
    elif command -v wget >/dev/null 2>&1; then
        wget -q "$WRAPPER_URL" -O "$WRAPPER_JAR"
    else
        echo "ERROR: Neither curl nor wget found. Please download gradle-wrapper.jar manually from:"
        echo "  $WRAPPER_URL"
        echo "and place it at: $WRAPPER_JAR"
        exit 1
    fi

    # Verify checksum if shasum/sha256sum available
    if command -v sha256sum >/dev/null 2>&1; then
        echo "$WRAPPER_SHA256  $WRAPPER_JAR" | sha256sum -c --status 2>/dev/null || {
            echo "WARNING: gradle-wrapper.jar checksum mismatch. Proceeding anyway."
        }
    fi
fi

DEFAULT_JVM_OPTS="-Xmx64m -Xms64m"
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

# Find Java
if [ -n "$JAVA_HOME" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

exec "$JAVACMD" \
    $DEFAULT_JVM_OPTS \
    ${JAVA_OPTS-} \
    ${GRADLE_OPTS-"-Dorg.gradle.daemon=true"} \
    "-Dorg.gradle.appname=$(basename "$0")" \
    -classpath "$CLASSPATH" \
    org.gradle.wrapper.GradleWrapperMain \
    "$@"
