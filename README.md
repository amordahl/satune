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

#### Step 3 Re-run SATune.

We have included the precompile `satune.jar` file un the `experiment/tools/` directory along with instructions in `experiments/tools/README.md`.

TODO continue from here.
