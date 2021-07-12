1. Get latest version of z3, z3-4.8.5:
wget https://github.com/Z3Prover/z3/releases/download/Z3-4.8.5/z3-4.8.5-x64-ubuntu-16.04.zip
unzip z3-4.8.5-x64-ubuntu-16.04.zip
mv z3-4.8.5-x64-ubuntu-16.04 z3

2. add the bin to the .profile file
export LD_LIBRARY_PATH="$LD_LIBRARY_PATH:/path/to/z3/bin"

3. Get Jayhorn 0.6 source:
```shell
cd satune/experiments/tools/jayhorn
wget https://github.com/jayhorn/jayhorn/archive/v0.6.tar.gz
tar -xf v0.6.tar.gz
cd jayhorn-0.6
emacs -nw jayhorn/src/main/java/jayhorn/solver/spacer/SpacerProver.java
// comment out lines 108 109 110 and 117 and save
./gradlew jar
// should end with BUILD SUCCESFULL
cd ..
cp jayhorn-0.6/jayhorn/build/libs/jayhorn.jar jayhorn.jar
``` 

4- Edit the jayhorn script to test
```shell
emacs -nw jayhorn

// add -solver spacer to lines 47 and 54
// change -inline_size to -inline-size at the same lines
```

5 - Test
```shell
./jayhorn  --propertyfile /path/to/sv-comp/benchmarks/java/properties/assert.prp /path/to/sv-comp/benchmarks/java/common /path/to/sv-comp/benchmarks/java/jayhorn-recursive/SatAckermann01
```