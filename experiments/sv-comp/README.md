# sv-comp

Get sv-benchmarks repository under here (do not check it in our repo, it is huge):
```
git clone https://github.com/sosy-lab/sv-benchmarks.git benchmarks
cd benchmarks
git checkout svcomp18
```

Not we can remove everything but the `c` directory. This directory contain all the benchmarks used in SV-COMP 2018.
We only need the ones that are listed in experiments/metadata/taks/c-tasks-all.txt. Other can be removed if disk space is a concern.

Now, we repeat the proces to get the Java benchmarks from SV-COMP 2019.

```
git clone https://github.com/sosy-lab/sv-benchmarks.git benchmarks-tmp
cd benchmarks-tmp
git checkout svcomp19
```

Remove everthing but the java directory. Then move this directory under the `benchmarks` dir:
```shell
mv benchmarks-tmp/java benchmarks/java
rm -fr benchmarks-tmp
```


Benchmarks all set.