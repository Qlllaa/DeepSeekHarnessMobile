#!/bin/sh

# Gradle wrapper script
# Download gradle-wrapper.jar from GitHub releases

APP_HOME=$( cd "$( dirname "$0" )" && pwd )
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$CLASSPATH" ]; then
    echo "Downloading Gradle wrapper JAR..."
    mkdir -p "$APP_HOME/gradle/wrapper"
    wget -q -O "$CLASSPATH" "https://github.com/gradle/gradle/raw/v8.7.0/gradle/wrapper/gradle-wrapper.jar" || {
        echo "ERROR: Failed to download Gradle wrapper JAR"
        exit 1
    }
fi

# Find Java
if [ -n "$JAVA_HOME" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD=$(command -v java)
fi

exec "$JAVACMD" \
    -Xmx64m \
    -Xms64m \
    -Dorg.gradle.appname=gradlew \
    -classpath "$CLASSPATH" \
    org.gradle.wrapper.GradleWrapperMain "$@"
