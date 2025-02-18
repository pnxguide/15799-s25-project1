#!/usr/bin/env bash

set -euo pipefail

QUERY=$1
DUCKDB_PATH=$(realpath stat.db);

# Build and run the Calcite app.
cd calcite_app/
./gradlew build
./gradlew shadowJar
./gradlew --stop

java -Xmx4096m -jar build/libs/calcite_app-1.0-SNAPSHOT-all.jar \
    "../input/queries/${QUERY}.sql" \
    "../output" \
    "${DUCKDB_PATH}" || true;

cd ..;

echo 'Running on DuckDB';

echo 'pragma disable_optimizer;' > tmp.sql;
echo '.timer on' >> tmp.sql;
echo 'EXPLAIN ANALYZE' >> tmp.sql;
cat output/${QUERY}_optimized.sql >> tmp.sql;
./duckdb ./stat.db < tmp.sql;
