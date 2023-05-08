#!/usr/bin/env bash

set -e

# Clean working dir and switch to it
WORKING_DIR="temp"
rm -rf $WORKING_DIR
mkdir $WORKING_DIR
mkdir $WORKING_DIR/output
OUTPUT_DIR="$(realpath $WORKING_DIR/output)"
cd $WORKING_DIR

# Download BODS RDF JAR use to run the import command line
KBODS_VERSION="0.9.5"
BODS_RDF_JAR="https://repo.maven.apache.org/maven2/io/resoluteworks/kbods-rdf/${KBODS_VERSION}/kbods-rdf-${KBODS_VERSION}-all.jar"
curl $BODS_RDF_JAR > bods-rdf.jar
JAR_PATH="$(realpath bods-rdf.jar)"

# Download and unpack OpenOwnership register
curl "https://oo-register-production.s3-eu-west-1.amazonaws.com/public/exports/statements.latest.jsonl.gz" > statements.latest.jsonl.gz
echo "Unpacking statements.latest.jsonl.gz"
gunzip statements.latest.jsonl.gz

# Convert to TTL and BRF
java -jar $JAR_PATH convert --input=statements.latest.jsonl \
    --output="${OUTPUT_DIR}/bods.ttl" \
    --output="${OUTPUT_DIR}/bods.brf" \
    --plugin="uk-company-refs"

java -jar $JAR_PATH convert --input=statements.latest.jsonl --relationships-only \
    --output="${OUTPUT_DIR}/bods-relationships-only.ttl" \
    --output="${OUTPUT_DIR}/bods-relationships-only.brf"

# Compress output files
for file in "$OUTPUT_DIR"/*
do
  echo "Compressing $file"
  gzip $file
done
