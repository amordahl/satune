1. Download the Symbiotic source code from Github and build.
```shell
cd tools/symbiotic
wget https://github.com/staticafi/symbiotic/archive/refs/tags/svcomp19.zip
unzip svcomp19.zip
mv symbiotic-svcomp19 symbiotic
cd symbiotic
./build.sh build-stp
```

2. Test by running following command:
```shell
symbiotic/bin/symbiotic --prp=../../sv-comp/benchmarks/c/ReachSafety.prp --timeout=60 --witness witness.graphml --no-integrity-check ../../sv-comp/benchmarks/c/array-examples/array-examples/standard_copy1_true-unreach-call_ground.i
# This benchamrk is a safe program. So the run should end with "RESULT: true" (in less than 3 seconds).
```
