# satune
Satune: A Study-Driven Auto-Tuning Approach for Configurable Software Verification Tools.

This repository contains all the artifacts related to our ASE 2021 publication, "SATune: A Study-Driven Auto-Tuning Approach for Configurable Software Verification Tools."


# Directory Structure

## SATune

The SATune directory contains the source code of the autotuning tool, SATune.

## Experiments

The experiments directory contain all the artifacts from the empirical analysis including the datasets for training.

# Reproducing the artifacts

## Prerequisites (Only necessary if not using the VM)

1. Clone this repository. 
2. Clone the benchmarks from the sv-comp benchmark repository under the sv-comp directory as described in  `sv-comp/README.md`.
3. Install all the dependencies for the verification tools following the instructions in the `experiments/tools/<tool>/InstallationNotes.md` files (mainly, the SMT solvers).


## Ground-truth dataset generation experiments (Section 2; start here if using the VM)

4. Each tool (i.e., each directory in ~/satune/experiments/tools/{cbmc,jbmc,jayhorn,symbiotic}) has a run_exp.py script. The script accepts an index in the range [1,10], which corresponds to one data partition in ~/satune/experiments/metadata/tasks. To run the dataset generation, navigate to a tool directory and invoke the run_exp.py script as follows: 

```shell
./run_exp.py 1

```

Note that, these scripts use a file that contain all the configs; "Configs-Xopts-t3-CA.txt". This file is generated from the covering array and the configuration space files using the scripts under the scripts directory (see `scripts/README.md` for more details instructions).

The execution logs will be saved under the `~/satune/experiments/tools/<tool>/results/` directory. 
Next part is tool specific. We manually grep these tools files to extract the verification result and save these lines into `tools/<tool>/results/stats-all.txt` file --which is the final outcome of Step 1.

#### Step 2 - Analyze the outcome of Step 1.

Using `stats-all.txt`, we compute the aggregate counts of true positive/nagtive, TP/TN, false positive/negative, FP/FN for each configuration.
The result of this manual operation is saved under 'tools/<tool>/results/Configs.csv'. The numbers in Table-II can be computed from these CSV files.

Now, we use the data analysis tool JMP [2] to conduct an effect screening study. For this, we further aggregate the counts of correct (TP+TN) and incorrect (FP + FN). Then, we fit a model using the DOE module of JMP. This will give us the Analysis results with significant effects that configuration options and their settings have on the results, i.e., number of correct and incorrect verification runs, (summarized in Table-III of the paper).

Note that, we included the intermediary JMP executable scripts under the `experiment/jmp` directory (see `experiment/jmp/README.md`).

#### Step 3 - Re-run SATune.

##### 3.1 - Prepare Dataset Files

`scripts/generateArffFromStats.py` file consumes the stats-all.txt file and generates an arff file under `tools/<tool>/data`.
Then, `createPWSplits.py` consumes the arff file form the previous step and partitions the datapoints into 5 cross-validation splits based on the splitting defined in the files under `metadata/splits`.

(Before moving forward, make sure all the dependencies are installed).

##### 3.3  - Run SATune

We have included the precompile `satune.jar` file under the `experiment/tools/` directory along with instructions in `experiments/tools/README.md`. Here is the command to run the experiments with JayHorn:

```shell
cd experiments/tools
java -jar satune.jar --tool jarhorn --threshold 1.0 --seed 1234 --search anneal --ml-model classification
```

That command will run JayHorn for each and every benchmark program in our sample set for Java. Eecution log will be saved under tools.	


#### Step 4 - Analyze SATune results

Following command will save all the result lines from the log files SATune generated:
```shell
grep "Final result:" log_anneal_jayhorn_* > satune-stats/stats-jayhorn.csv
```

Now, some trivial changes in `stats-cbmc.csv`. First remove the `Final result:` prefixes from each line. Then add the header (see `satune-stats/stats-paper.csv`).
At this point, `stats-jayhorn.csv` should be of the same format with `satune-stats/stats-paper.csv`. We repeat this process for the other three verification tools.


We have two scripts under the scripts directory:

- `analyzeTime.R` will create the Fig.3 in the paper using the time stats file under the satune-stats directory.
- `analyzePerformance.py` and `analyzePerformance2.py` will compute the performance results in Table-4


# Contact Us

Please email us at ukoc@cs.umd.edu or austin.mordahl@utdallas.edu if you have any question.

If you found bug(s), please report it/them using "GitHub Issues".