#!/bin/sh
# Wrapper bootstrap: download Gradle 8.7 on first run.
set -e
DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
exec java -jar "$DIR/gradle/wrapper/gradle-wrapper.jar" "$@"
