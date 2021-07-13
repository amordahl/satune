1. Download the Symbiotic binaries from the SV-COMP website
```shell
cd tools/symbiotic
wget https://gitlab.com/sosy-lab/sv-comp/archives/raw/svcomp18/2018/symbiotic.zip
```

2. Test by running following command:
```shell
symbiotic/bin/symbiotic --prp=../../sv-comp/benchmarks/c/ReachSafety.prp --timeout=60 --witness witness.graphml --no-integrity-check ../../sv-comp/benchmarks/c/array-examples/sanfoundry_10_true-unreach-call_ground.i,ReachSafety.prp
# Should end with ...
```
