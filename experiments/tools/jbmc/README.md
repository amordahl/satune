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
./jbmc --propertyfile 

```

Now, you can use the run_exp.pl script to run the experiments. You might need to change the paths that are defined in the script.