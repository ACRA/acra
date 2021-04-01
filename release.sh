#!/bin/bash

echo "Type version to release [Current version: $(./gradlew version --no-daemon --quiet --console=plain -Dorg.gradle.jvmargs=-Xmx4g)]"
read -r version
git tag -a "acra-$version" -m "Create version $version"
git push --tags