#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
mkdir -p out
javac -Xlint:all -d out src/*.java
java -cp out Demo
java -cp out Tests
printf '\nThe code passed. Existing benchmark results were not changed.\n'
