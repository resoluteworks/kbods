#!/usr/bin/env bash

set -e

VERSION=`cat version.properties | grep "version" | awk -F' *= *' '{print $2}'`
echo "Version is $VERSION"

./gradlew clean test
./gradlew publish

git tag "v${VERSION}" -m "Release v${VERSION}"
git push --tags --force

echo "Finished building version $VERSION"
