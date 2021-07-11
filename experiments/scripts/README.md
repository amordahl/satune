# Scripts used to create and process data files

## Data Analysis Scripts

- `analyzeTime.R` will create the Fig.3 in the paper using the time stats file under the satune-stats directory.
- `analyzePerformance.py` and `analyzePerformance2.py` will compute the performance results in Table-4

## Data Processing Scripts
- `generateConfigsFromDesigns.py` will create actual tool configurations from covering array rows and config space files under the tools dirrectory.
- `generateArffFromStats.py` will create the Arff files from ground-truth data generation experiments which are summarized into the `results/stats-all.txt` file for each tool.
- `createPWSplits.py` will create test and training sets from the Arff file computed with `stats2arff.py`.
- `Utils_ML.py` and `Utils_Config.py` contain utility functions used by other three scripts.

Note that, there are some additional scripts to run each tool under tools directory.

### Data Processing Workflow
First, we run the ground-truth generation experiment using the `run_exp.py` scripts under each tool directory. This will run the verification tool for each program in our sample set using each configuration in the covering array. All the execution logs (including the results) will be camptures in a big log file.
We then grep the result lines from the the log file. Note that, each tool have slightly different result line, the grep regex is tool specific.
This is how the stats-all.txt file is generated.
Second, we run `stats2arff.py` to generate the Weka compatible arff file. The outcome arff file contain all the datapoints. 
Next, we run `generateArffFromStats.py` to create the cross validation partitions --which are also arff files.
SATune will use these arff files for training/testing the surrogate fitness function.