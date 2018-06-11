@echo off

rem benchmarks to include, regexp
rem SET INCLUDE=BasicEventListBenchmark.testAdd
SET INCLUDE="SeparatorListBenchmark"


rem overrides annotations for warmup-iteration, iterations and fork count
SET CONFIG=-wi 5 -i 10 -f 1

rem enabled stack profiler, show top 10 with 7 lines of stack
SET PROFILERS= -prof stack:lines=7;top=5

gradlew jmh:capsule && java -jar jmh-benchmark\build\capsule\glazedlists-benchmarks.jar %INCLUDE% %CONFIG% %PROFILERS%
