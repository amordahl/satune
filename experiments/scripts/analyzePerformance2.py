#!/usr/bin/env python

import re,os,csv,copy, json
import Utils_Config as cu
import Utils_ML as mlu
import subprocess as sp
"""
Computes the performance results for the precise→correct approach. 
Note that, this approach is NOT applicable to JBMC and Symbiotic.
"""
HOME_DIR = str(os.getenv("HOME"))
BASE_DIR=f'{HOME_DIR}/satune/experiments'

tools={'cbmc':['c','21','0,0,0,0,0,0,0,0,0,0,0,0,100,0,0,0,sc,0,0,--round-to-nearest,0',
								'--drop-unused-functions --full-slice --no-assumptions --no-self-loops-to-assumptions --nondet-static --refine-strings --depth 100 --unwind 100 --max-nondet-tree-depth 2000 --mm tso --arrays-uf-always --round-to-plus-inf --cprover-smt2', # best performance
								'--unwind 100 --mm sc --round-to-nearest'], # best precision
		'jayhorn':['java','12','off,off,10,-1,auto,1,-1,-1,3,eldarica,1,off', 
								'-specs -bounded-heap-size 10 -heap-limit -1 -heap-mode unbounded -initial-heap-size 10 -inline-count -1 -inline-size 10 -mem-prec 4 -solver spacer -step-heap-size 1',  # best performance
								'-bounded-heap-size 10 -heap-limit -1 -heap-mode auto -initial-heap-size 1 -inline-count -1 -inline-size -1 -mem-prec 3 -solver eldarica -step-heap-size 1']}  # best precision
								#'-specs -bounded-heap-size 10 -heap-limit -1 -heap-mode unbounded -initial-heap-size 1 -inline-count 1 -inline-size -1 -mem-prec 2 -solver eldarica -step-heap-size 1 -solver-options abstractPO'],  # best precision

def getLabelClassification(result, verdict):
	label = 'UNK'
	if result == '0':
		label = 'TN' if verdict else 'FN'
	elif result == '10':
		label = 'TP' if not verdict else 'FP'
	return label


def computePerformance(tool, optsStr, lang, defconfig=None, performanceConfig=None, precisionConfig=None):
	toolPath = f'{BASE_DIR}/tools/{tool}'
	saveFile = f'{toolPath}/data/All-March18.arff'

	f = open(f'{toolPath}/data/headers.txt','r')
	headers = f.read()
	f.close()

	f = open(f'{toolPath}/results/stats-all.txt','r')
	stats = f.read().splitlines()
	f.close()

	optionNames, _ = cu.readDomFile(f'{toolPath}/Config-space-{optsStr}.txt')

	featuresFile = f'{BASE_DIR}/metadata/features/{lang}-program-features.txt'
	featureMap = mlu.readFeatures(featuresFile)
	taskLabelMap = {k : {'TP':0, 'TN':0, 'FP':0, 'FN':0, 'UNK':0} for k in featureMap.keys()}

	f = open(f'{toolPath}/CA-{optsStr}-t3.csv', 'r')
	configLines = f.read().splitlines()
	f.close()

	designMap = {}
	for cLine in configLines:
		designMap[cu.designLine2Config(tool,cLine,optionNames)] = cLine.split(',')
	
	#adding defconfig results into the datasets
	if defconfig != None:
		with open(f'{toolPath}/results/stats-defconfig.txt','r') as f:
			stats.extend(f.read().splitlines())
		designMap[cu.designLine2Config(tool,defconfig,optionNames)] = defconfig.split(',')
	configLabelMap = {k : {'TP':0, 'TN':0, 'FP':0, 'FN':0, 'UNK':0} for k in designMap.keys()}

	# book keeping the set of programs for each classification (correct/incorrect/iconclusive) for the performance and precision configurations
	performanceToCorrect = {performanceConfig:{'T':[], 'F':[], 'U':[]}, 
						precisionConfig:{'T':[], 'F':[], 'U':[]}}

	datapoints=[]
	for stat in stats:
		config, task, verdict, result, propFile, label = None, None, None, None, None, None
		if lang == 'java':
			config, task, verdict, result, propFile = stat.split(',') # java jbmc and jayhorn
			verdict = 'True' == verdict
		elif tool == 'cbmc':
			config, task, propFile, result = stat.split(',') # cbmc
			verdict = '_true-' in task
		elif tool == 'symbiotic':
			config, task, propFile, _, code = stat.split(',')
			verdict = '_true-' in task
			result = '10' if code == 'false' else ('0' if code == 'true' else ('-6' if code == 'timeout' else '-9'))
		label = getLabelClassification(result, verdict)
		taskLabelMap[task][label] = taskLabelMap[task][label] + 1
		configLabelMap[config][label] = configLabelMap[config][label] + 1

		if (config == performanceConfig or config == precisionConfig) and label[0] != 'U':
			performanceToCorrect[config][label[0]].append(task)

	# json.dump(performanceToCorrect, open(f'{toolPath}/performanceToCorrect.json', 'w'))
	# json.dump(taskLabelMap, open(f'{toolPath}/taskLabelMap.json', 'w'))
	# json.dump(configLabelMap, open(f'{toolPath}/configLabelMap.json', 'w'))

	performanceUniqueTrues = set(performanceToCorrect[performanceConfig]['T']) - set(performanceToCorrect[precisionConfig]['T'])
	performanceUniqueFalses = set(performanceToCorrect[performanceConfig]['F']) - set(performanceToCorrect[precisionConfig]['F'])
	print(f'performanceUniqueTrues: {len(performanceUniqueTrues)}, performanceUniqueFalses: {len(performanceUniqueFalses)}')
	
	preciseToCorrectTotalCorrects = len(performanceUniqueTrues) + len(set(performanceToCorrect[precisionConfig]['T']))
	preciseToCorrectTotalIncorrects = len(performanceUniqueFalses) + len(set(performanceToCorrect[precisionConfig]['F']))
	preciseToCorrectPrecision = 100 * preciseToCorrectTotalCorrects / (preciseToCorrectTotalCorrects+preciseToCorrectTotalIncorrects)
	# Following line will print the performance results presented in Table-IV
	print(f'{tool}:precise→correct:corrects/incorrects/precision: '
				f'{preciseToCorrectTotalCorrects}\t{preciseToCorrectTotalIncorrects}\t{preciseToCorrectPrecision}')

for tool,fields in tools.items():
	computePerformance(tool, fields[1]+'opts', fields[0], defconfig=fields[2], performanceConfig=fields[3], precisionConfig=fields[4])