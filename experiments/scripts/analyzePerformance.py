#!/usr/bin/env python
"""
Computes the results for the Table-IV of the paper only for SATune and precise→random approaches.
For the most-correct-config approach, the performance results in Table-4 and Table-2 are the same.
For the precise→correct approach, refer to analyzePerformance2.py
"""
import re,os,csv,copy
import pandas as pd
import numpy as np

HOME_DIR = str(os.getenv("HOME"))
BASE_DIR=f'{HOME_DIR}/satune/experiments'


# dictionary of subject tools. {tool: [prog_lang, default_config, exclusions_file]}
tools={'cbmc':['c','data/def_completes.txt',None],
		'jayhorn':['java','data/def_completes.txt',None],
		'jbmc':['java', 'data/def_completes.txt','data/unmodelled_skips.txt'], 
		'symbiotic':['c','data/def_completes2.txt','data/concurrency_skips.txt']}

completesMap={}
for tool,fields in tools.items():
	TOOL_DIR='{}/tools/{}'.format(BASE_DIR, tool)
	with open('{}/{}'.format(TOOL_DIR, fields[1]), 'r') as f:
		completesMap[tool] = set(f.read().splitlines())

	if fields[2] != None:
		with open('{}/{}'.format(TOOL_DIR, fields[2]), 'r') as skipsFile:
			completesMap[tool].update(skipsFile.read().splitlines())

#label,task,result,cost,score,time,configCount,revisitCount,acceptCount,bestCount,saRunCount,searchAlg,tool,pca,thresh,seed,mode,improve
summaryFile='{}/satune-stats/stats-paper.csv'.format(BASE_DIR)
stats=pd.read_csv(summaryFile)

stats.drop(stats[(stats.thresh<1.0) & (stats.thresh>-1.0)].index, inplace=True)  # only no threshold
stats.drop(stats[stats.label=='UNK'].index, inplace=True)
#stats.drop(stats[stats.searchAlg!='SATune'].index, inplace=True)
stats.drop(['result','cost','score','time','configCount','revisitCount','acceptCount','bestCount','saRunCount','pca','mode','improve'], axis=1, inplace=True)
indeces_to_drop=set()
for index, row in stats.iterrows():
		if row['task'] in completesMap[row['tool']]:
			indeces_to_drop.add(index)
stats.drop(indeces_to_drop, inplace=True)

#define function to calculate interquartile range
def find_iqr(x):
  return np.subtract(*np.percentile(x, [75, 25]))

counts = stats.groupby(['label', 'tool', 'seed', 'searchAlg']).count()


print(counts.groupby(['label', 'tool', 'searchAlg']).apply(find_iqr))
print(counts.groupby(['label', 'tool', 'searchAlg']).median())

stats.to_csv(summaryFile.replace('-paper.csv', '-improvements.csv'), index=False)