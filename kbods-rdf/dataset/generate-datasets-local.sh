#!/usr/bin/env bash

set -e

VERSION=`cat ../../version.properties | grep "version" | awk -F' *= *' '{print $2}'`
(cd ../../ && ./gradlew clean kbods-rdf:build -x test)

OUTPUT_DIR="/Users/cosmin/temp/bods-rdf"
rm -rf $OUTPUT_DIR
mkdir -p $OUTPUT_DIR

java -jar ../build/libs/kbods-rdf-${VERSION}-all.jar \
    convert \
    --input=/Users/cosmin/temp/statements.jsonl \
    --output="$OUTPUT_DIR/bods-rdf-statements.ttl" \
    --plugin="uk-company-refs"
