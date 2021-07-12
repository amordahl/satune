1. Get CBMC 5.11 from https://www.cprover.org/cbmc
```shell
wget https://www.cprover.org/cbmc/download/cbmc-5-11-linux-64.tgz
```

2. Place the cbmc-binary executable under cbmc directory and give it executable rights.

3. Install the SMT solver CBMC uses: ()
3.1 For boolector try using package manager of your OS. E.g.,
```shell
sudo apt install boolector

3.2  Yices 2.6.1, follow the installation instructions at https://github.com/SRI-CSL/yices2 for version 2.6.1

```
3.3 For z3 see `jayhorn/README.md`
3.4 For CVC4 version 1.8, follow the installation instructions at https://github.com/CVC4/CVC4-archived.
3.5 MathSAT5 version 20181102 (9dddce7e8e79) (Nov 20 2018 09:21:45, gmp 6.1.0, gcc 4.8.5, 64-bit). Download the executable from https://mathsat.fbk.eu/downloadall.html and move it to your home diretory and add mathsat5/bin to your path.


4. Test the following command under the sv-comp directory;
```shell
cbmc --propertyfile ../../sv-comp/benchmarks/c/ReachSafety.prp ../../sv-comp/benchmarks/c/pthread-wmm/rfi010_tso.oepc_false-unreach-call.c
# This should print out "VERIFICATION FAILED" at the end.
```

Now, you can use the run_exp.pl script to run the experiments. You might need to change the paths that are defined in the script.