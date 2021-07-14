# Metadata about the verification tasks

This directory contains meta-data about our datasets.

 - Used bechmarks are under the `tasks-actual` directory. `c-tasks-all.txt` and `java-tasks-all.txt` contain all C and Java tasks respectively. Other files partitioned versions of the same infromation (for parallelization).
   - For artifacts evaluation purpose, all the scripts/tools are poiting to the `tasks` directory which is a tiny version of `tasks-actual` for speeding up runs.
 - Cross-validation splits are under splits directory. E.g., `c-testset-1.txt` contain the tasks that are hold out of training set of the first surrogate fitness function --which is training on the other four splits.
 - The program features are under the fetures directory. First lines of `c-program-features` and `java-program-features` are the features used as the program representation for C and Java program respectively. For C programs, the features are extracted using the `feature_static_analysis` tool under the scripts directory.