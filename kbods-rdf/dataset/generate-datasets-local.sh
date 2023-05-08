#!/usr/bin/env bash

set -e

# Build the fat jar first
(cd ../../ && ./gradlew clean kbods-rdf:build -x test)
VERSION=`cat ../../version.properties | grep "version" | awk -F' *= *' '{print $2}'`
JAR_PATH="$(realpath ../build/libs/kbods-rdf-${VERSION}-all.jar)"

# Clean working dir and switch to it
WORKING_DIR="temp"
rm -rf $WORKING_DIR
mkdir $WORKING_DIR
mkdir $WORKING_DIR/output
OUTPUT_DIR="$(realpath $WORKING_DIR/output)"
cd $WORKING_DIR

# Download and unpack
curl "https://oo-register-production.s3-eu-west-1.amazonaws.com/public/exports/statements.latest.jsonl.gz" > statements.latest.jsonl.gz
gunzip statements.latest.jsonl.gz

# Convert to TTL and BRF
java -jar $JAR_PATH \
    convert --input=statements.latest.jsonl --output="${OUTPUT_DIR}/bods-rdf.ttl" --plugin="uk-company-refs"

java -jar $JAR_PATH \
    convert --input=statements.latest.jsonl --output="${OUTPUT_DIR}/bods-rdf.brf" --plugin="uk-company-refs"

# Compress output files
for file in "$OUTPUT_DIR"/*
do
  echo "Compressing $file"
  gzip $file
done

rm -rf statements.latest.jsonl
