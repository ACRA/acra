#!/bin/bash

echo "Type version to release [Current version: $(git describe --abbrev=0 | sed -e "s/^acra-//")]"
read -r version
git tag -a "acra-$version" -m "Create version $version"
git push --tags