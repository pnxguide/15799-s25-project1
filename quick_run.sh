#!/usr/bin/env bash

set -euo pipefail

QUERY=$1

# Build and run the Calcite app.
cd calcite_app/
./gradlew build
./gradlew shadowJar
./gradlew --stop

java -Xmx4096m -jar build/libs/calcite_app-1.0-SNAPSHOT-all.jar \
    "../input/queries/${QUERY}.sql" \
    "../output" \
    "../input/statistics.csv";
