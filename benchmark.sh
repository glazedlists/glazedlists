#!/usr/bin/env bash

# benchmarks to include, regexp
#INCLUDE="BasicEventListBenchmark.testAdd"
INCLUDE="SeparatorListBenchmark"


# overrides annotations for warmup-iteration, iterations and fork count
CONFIG="-wi 5 -i 10 -f 1"

# enabled stack profiler, show top 10 with 7 lines of stack
PROFILERS=" -prof stack:lines=7;top=5"

./gradlew :jmh-benchmark:shadowJar && java -jar jmh-benchmark/build/libs/glazedlists-benchmarks.jar $INCLUDE $CONFIG $PROFILERS
