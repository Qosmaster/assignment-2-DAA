#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
mkdir -p out
javac -Xlint:all -d out src/*.java
java -cp out Tests
java -Xms256m -Xmx1g -cp out Benchmark
