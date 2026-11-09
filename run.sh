#!/bin/sh
set -eu
cd "$(dirname "$0")"
mkdir -p build
find src -name '*.java' > build/sources.txt
javac --release 17 -encoding UTF-8 -d build @build/sources.txt
java -cp build depgraph.Main "$@"
