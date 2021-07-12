## 1. Get CBMC 5.11 from https://www.cprover.org/cbmc
```shell
cd tools/cbmc
mv cbmc cbmc-tmp
wget https://www.cprover.org/cbmc/download/cbmc-5-11-linux-64.tgz
tar -xf cbmc-5-11-linux-64.tgz
mv cbmc cbmc-binary
mv cbmc-tmp cbmc
```

## 2. Install the SMT solvers CBMC uses:
### 2.1 boolector 
Try using package manager of your OS. E.g.,
```shell
sudo apt install boolector

```
### 2.2 Yices 
We need 2.6.1. Follow the installation instructions at https://github.com/SRI-CSL/yices2 for version 2.6.1
### 2.3 Z3 
See the instructions in `jayhorn/README.md`.
### 2.4 CVC4 
We need version 1.8. Follow the installation instructions at https://github.com/CVC4/CVC4-archived.
### 2.5 MathSAT5 
We need version 20181102 (9dddce7e8e79) (Nov 20 2018 09:21:45, gmp 6.1.0, gcc 4.8.5, 64-bit). Download the executable from https://mathsat.fbk.eu/downloadall.html and move it to your home diretory and add mathsat5/bin to your path.


## 4. Test the following command under the sv-comp directory;
```shell
./cbmc --propertyfile ../../sv-comp/benchmarks/c/ReachSafety.prp ../../sv-comp/benchmarks/c/pthread-wmm/rfi010_tso.oepc_false-unreach-call.c
# This should print out "VERIFICATION FAILED" at the end.
```

Now, you can use the run_exp.pl script to run the experiments. You might need to change the paths that are defined in the script.