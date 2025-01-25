#!/usr/bin/env bash

set -euo pipefail

WORKLOAD=$1
OUTPUT_DIR=$2

echo "Invoking optimize.sh."
echo -e "\tWorkload: ${WORKLOAD}"
echo -e "\tOutput Dir: ${OUTPUT_DIR}"

mkdir -p "${OUTPUT_DIR}"
mkdir -p input/

# Extract the workload.
tar xzf "${WORKLOAD}" --directory input/

# Feel free to add more steps here.
rm -rf stat.db;

# Replace file
mkdir -p tpch/;
cp ./input/data/*.parquet ./tpch/;
cp ./input/data/*.parquet ./tpch/;

# Load data into stat.db
./duckdb stat.db -c ".read ./input/data/schema.sql";
./duckdb stat.db -c ".read ./input/data/load.sql";

# Analyze statistics from the database
sudo apt -y install python3-pip;
pip3 install duckdb;
python3 compute_statistics.py > ./input/statistics.csv;

# Build and run the Calcite app.
cd calcite_app/
./gradlew build
./gradlew shadowJar
./gradlew --stop

for file in ../input/queries/*.sql
do
    java -Xmx4096m -jar build/libs/calcite_app-1.0-SNAPSHOT-all.jar \
        "${file}" \
        "../${OUTPUT_DIR}" \
        "../input/statistics.csv"
done

cd -
