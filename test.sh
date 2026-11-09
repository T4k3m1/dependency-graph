#!/bin/sh
set -eu
cd "$(dirname "$0")"
mkdir -p build
find src tests -name '*.java' > build/tests.txt
javac --release 17 -encoding UTF-8 -d build @build/tests.txt
java -ea -cp build depgraph.AllTests
