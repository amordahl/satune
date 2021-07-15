# satune
Satune: A Study-Driven Auto-Tuning Approach for Configurable Software Verification Tools.

This repository contains all the artifacts related to our ASE 2021 publication, "SATune: A Study-Driven Auto-Tuning Approach for Configurable Software Verification Tools."


# Directory Structure

## SATune

The SATune directory contains the source code of the autotuning tool, SATune.

## Experiments

The experiments directory contain all the artifacts from the empirical analysis including the datasets for training.

# Reproducing the artifacts

In this section, we will walk through running a small subset of our experiments on one tool. Running on the other tools involves very similar steps. Notable exceptions will be noted. Please let us know if we missed anything.

## Prerequisites

**These steps are only necessary if you decide not to use the VM. We strongly suggest you use the VM provided in our Zenodo artifact. If you are using the VM, please go to step 1.**

1. Clone this repository. 
2. Clone the benchmarks from the sv-comp benchmark repository under the sv-comp directory as described in  `sv-comp/README.md`.
3. Install all the dependencies for the verification tools following the instructions in the `experiments/tools/<tool>/InstallationNotes.md` files (mainly, the SMT solvers).

## Step 1: Ground-truth dataset generation experiments (start here if using the VM)

The first step of the process is to generate the ground truth datasets, as described in Section II.

4. Navigate to `/home/satune/satune/experiments/tools/cbmc`. (there is a corresponding folder for jbmc, jayhorn, and symbiotic in the tools directory, which contain similar scripts to those described below).

Ground truth generation is done by the `run_exp.py` script. It accepts a single parameter, a number in the range [1,10]. This integer tells the script which split of the dataset to use (the numbers correspond to the the task lists in `/home/satune/satune/experiments/metadata/tasks`; c-tasks-p{}.txt for C tasks, and java-tasks-p{}.txt for Java tasks, where {} should be replaced with a number). The script will run each configuration of the tool listed in the configuration file (in CBMC's case, this is `Configs-21opts-t3-CA-actual.txt`).

*Please note that, for the sake of time, we have replaced the full task lists with smaller task lists that can run quickly, and have done the same with the configuration lists. Should you want to replicate our full experiments, you need to rename `/home/satune/satune/experiments/metadata/tasks-actual` to `/home/satune/satune/experiments/metadata/tasks`. For the configurations, in each tool directory, you will see two files: Configs-XX-CA.txt, and Configs-XX-CA-actual.txt (where XX is a string that is different depending on the tool). Simply replace Configs-XX-CA.txt with Configs-XX-CA-actual.txt*.

5. We will use GNU Parallel to run all of the splits. You can run with `parallel python run_exp.sh ::: 1 2 3 4 5 6 7 8 9 10`. GNU Parallel will automatically detect the number of cores available and schedule jobs accordingly. If you are only running your virtual machine with a single core, then it will run these jobs sequentially. On our machine, each job took <1 second to run.

As a result of running this script, results are produced in the `results` directory. Each split will produce a corresponding log-ground-truth-p{}.txt file.

6. Since the logs contain both the results and the actual running logs from the experiments, we use a simple grep command to collect the information we need. From /home/satune/experiments/tools/cbmc, execute the following commands:

`cd results`
`grep -h ".prp," log-ground-truth-*.txt > stats-all.txt`

*NOTE: Should you try to generate ground truth data for JBMC or Jayhorn, replace the string grep is searching for with ",assert.prp"*

Now, `stats-all.txt` contains the actual ground truth information we need. Specifically, each line contains the configuration, the program checked, the property checked, and the result of running in terms of the tool's exit code.

#### Step 2: Main Effect Screening Study

*Note: We do not expect reviewers to replicate this step in artifact evaluation. However, we do include the scripts and description should you wish to. JMP is not available on the VM.*

Once the ground truth data is saved in `stats-all.txt`, we manually computed the aggregate counts of true positive/negative, TP/TN, false positive/negative, FP/FN for each configuration.
The full result of this manual operation is saved under `tools/<tool>/results/Configs.csv`. The numbers in Table-II can be computed from these CSV files.

At this point, we used the data analysis tool JMP [2] to conduct an effect screening study. For this, we further aggregate the counts of correct (TP+TN) and incorrect (FP + FN). Then, we fit a model using the DOE module of JMP. This will give us the analysis results with significant effects that configuration options and their settings have on the results, i.e., number of correct and incorrect verification runs, (summarized in Table-III of the paper).

We included the intermediary JMP executable scripts under the `experiment/jmp` directory (see `experiment/jmp/README.md`), which can be used to generate the information in Table-II.

#### Step 3 - Run SATune

##### 3.1 - Prepare Dataset Files

We convert the datasets to .arff format, so that they can be easily consumed by SATune.

7. The `/home/satune/satune/experiments/scripts/generateArffFromStats.py` script consumes the stats-all.txt file and generates an arff file under `tools/<tool>/data`. Simply navigate to the directory and run 

`pytonn generateArffFromStats.py`.

On our machine, this step took <1 second. The arff files are written to (...).
8. Then, the `createPWSplits.py` consumes the arff file from the previous step and partitions the datapoints into 5 cross-validation splits based on the splits defined in the files under `metadata/splits`.

On our machine, this step took <1 second.

##### 3.3  - Run SATune

9. We have included the precompiled `satune.jar` file under the `experiment/tools/` directory along with instructions in `experiments/tools/README.md`. Here is the command to run the experiments with CBMC:

```shell
cd experiments/tools
java -jar satune.jar --tool cbmc --threshold 1.0 --seed 1234 --search anneal --ml-model classification
```

For other tools, simply replace the value of the --tool parameter. Running this command will produce log files in the `experiment/tools/` directory. If you did not run the previous step, which limits the dataset for CBMC, this command will take a long time. Using the reduced examples, this step ran in less than 10 minutes on our machine.

#### Step 4 - Analyze SATune results

10. The following command will save all the result lines from the log files SATune generated:
```shell
grep "Final result:" log_anneal_cbmc_* > satune-stats/stats-cbmc.csv
```

11. At this point, we make some trivial, manual changes in `stats-cbmc.csv`. First remove the `Final result:` prefixes from each line. Then add the header (see `satune-stats/stats-paper.csv`).
At this point, `stats-cbmc.csv` should be of the same format with `satune-stats/stats-paper.csv`. We repeat this process for the other three verification tools.


We have two R scripts under the scripts directory that consume these files and produce the statistics and figures we presented in the paper.

- `analyzeTime.R` will create the Fig.3 in the paper using the time stats file under the satune-stats directory.
- `analyzePerformance.py` and `analyzePerformance2.py` will compute the performance results in Table-4

# Contact Us

Please email us at ukoc@cs.umd.edu or austin.mordahl@utdallas.edu if you have any question.

If you found bug(s), please report it/them using "GitHub Issues".
