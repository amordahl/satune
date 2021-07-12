# satune
Satune: A Study-Driven Auto-Tuning Approach for Configurable Software Verification Tools.

This repository contain all the artifacts related to SATune ASE 2021 publication.


## SATune

SATune directory contains the source code of the autotuning tool.


## Experiments

Experiments directory contain all the artifacts from the empirical analysis including the datasets for training.

### Reproducing the artifacts

#### Step 0 - Prerequisites

1. Clone this repository. 
2. Clone the benchmarks from the sv-comp benchmark repository under the sv-comp directory as described in  `sv-comp/README.md`.
3. Install all the dependencies for the verification tools following the instructions in `experiments/tools/README.md` (mainly, the SMT solvers).


#### Step 1 - Re-run the ground-truth dataset generation experiments

After we complete all the setup as described above, we can run the study describe in Section-2 [1].

To do this, we will use `run_exp.py` scripts for each tool. To parallelize the experiments, we have divided the benchmark set into 10 partitions. As input, `run_exp.py` script take the partition index only. E.g., following command will run the experiment for the first parition benchamrks;

```shell
./run_exp.py 1

```

Note that, these scripts use a file that contain all the configs; "Configs-Xopts-t3-CA.txt". This file is generated from the covering array and the configuration space files using the scripts under the scripts directory (see `scripts/README.md` for more details instructions).

The execution logs will be saved under `tools/<tool>/results/` directory. 
Next part is tool specific. We manually grep these tools files to extract the verification result and save these lines into `tools/<tool>/results/stats-all.txt` file --which is the final outcome of Step 1.

#### Step 2 - Analyze the outcome of Step 1.

Using `stats-all.txt`, we compute the aggregate counts of true positive/nagtive, TP/TN, false positive/negative, FP/FN for each configuration.
The result of this manual operation is saved under 'tools/<tool>/results/Configs.csv'. The numbers in Table-II can be computed from these CSV files.

Now, we use the data analysis tool JMP [2] to conduct an effect screening study. For this, we further aggregate the counts of correct (TP+TN) and incorrect (FP + FN). Then, we fit a model usingthe DOE module of JMP. This will give us the Analysis results with significant effects that configuration options and their settings have on the results (i.e., number of correct and incorrect verification runs).

Note that, we included the intermediary JMP executable scripts under the `experiment/jmp` directory (see `experiment/jmp/README.md`).

#### Step 3 - Re-run SATune.

##### 3.1 - Prepare Dataset Files

`scripts/generateArffFromStats.py` file consumes the stats-all.txt file and generates an arff file under `tools/<tool>/data`.
Then, `createPWSplits.py` consumes the arff file form the previous step and partitions the datapoints into 5 cross-validation splits based on the splitting defined in the files under `metadata/splits`.

(Before moving forward, make sure all the dependencies are installed).

##### 3.3  - Run SATune

We have included the precompile `satune.jar` file un the `experiment/tools/` directory along with instructions in `experiments/tools/README.md`. Here is the command to run the experiments with CBMC:

```shell
cd experiments/tools
java -jar satune.jar --tool cbmc --threshold 1.0 --seed 1234
```

That command will run CBMC for each and every benchmark program in our sample set. Eecution log will be saved under `tools/<tool>`.	


#### Step 4 - Analyze SATune results

Following command will save all the result lines from the log files SATune generated:
```shell
grep "Final result:" log_anneal_cbmc_* > satune-stats/stats-cbmc.csv
```

Now, some trivial changes in `stats-cbmc.csv`. First remove the `Final result:` prefixes from each line. Then add the header (see satune-stats/stats-paper.csv)
At this point, `stats-cbmc.csv` should be of the same format with `satune-stats/stats-paper.csv`. We repeat this process for the other three verification tools.


We have two scripts under the scripts directory:

- `analyzeTime.R` will create the Fig.3 in the paper using the time stats file under the satune-stats directory.
- `analyzePerformance.py` and `analyzePerformance2.py` will compute the performance results in Table-4


# Contact Us

Please email us at ukoc@cs.umd.edu or austin.mordahl@utdallas.edu if you have any question.