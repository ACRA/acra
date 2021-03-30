#!/bin/bash

echo "Type version to release"
read -r version
git tag -a "acra-$version" -m "Create version $version"
git push --tags