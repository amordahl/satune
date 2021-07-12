## 1. Get JBMC 5.11 from https://www.cprover.org/cbmc
```shell
cd tools/jbmc
mv jbmc jbmc-tmp
wget https://www.cprover.org/cbmc/download/cbmc-5-11-linux-64.tgz
tar -xf cbmc-5-11-linux-64.tgz
mv cbmc cbmc-binary
mv cbmc-tmp cbmc
```

## 2. Install the SMT solvers JBMC uses:
Follow the installation instructions for CBMC

## 4. Test the following command under the sv-comp directory;
```shell
readonly JAVA_BENCH_DIR=../../sv-comp/benchmarks/java
./jbmc --propertyfile $JAVA_BENCH_DIR/properties/assert.prp $JAVA_BENCH_DIR/common $JAVA_BENCH_DIR/MinePump/spec1-5_product1
# This benchmark is unsafe. So the run should results in "VERIFICATION FAILED"
```

Now, you can use the run_exp.pl script to run the experiments. You might need to change the paths that are defined in the script.