#!/usr/bin/env python
"""
Generates a single arff file from the stats-all.txt file which contain the ground-truth study results.
Consumes the covering array files, configuration spaces files, headers files, and program features files.
If a default configuration is provided included the stats of the default config into the arff file.
Writes the arff file to All-<Date>.arff file under the tools/<tool>/data directory.
"""
import re, os, csv
import Utils_Config as cu
import Utils_ML as mlu
import subprocess as sp

HOME_DIR = str(os.getenv("HOME"))
BASE_DIR=f'{HOME_DIR}/satune/experiments'

# dictionary of subject tools. {tool: [prog_lang, number_of_options, default_config]}
tools={'cbmc':['c','21','0,0,0,0,0,0,0,0,0,0,0,0,100,0,0,0,sc,0,0,--round-to-nearest,0'],
		'jayhorn':['java','12','off,off,10,-1,auto,1,-1,-1,3,eldarica,1,off'], 
		'jbmc':['java', '27', '0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,100,0,0,0,0,0,0,0,0'], 
		'symbiotic':['c','16','off,off,off,off,off,off,off,off,off,off,off,off,inv,1,off,off']}


def getLabelClassification(result, verdict):
	"""
	Verification runs will end with an exist code. 
		- 0 if the verification is successful,
		- 10 if the verification is unsuccessful, i.e., the program is buggy/unsafe
	This fucntion returns a classification label based on the verification result
	and the actual verdict of the program
	"""
	label = 'UNK'
	if result == '0':
		label = 'TN' if verdict else 'FN'
	elif result == '10':
		label = 'TP' if not verdict else 'FP'
	return label


def createAllArrf(tool, optsStr, lang, defconfig=None):
	toolPath = f'{BASE_DIR}/tools/{tool}'
	saveFile = f'{toolPath}/data/All-March18.arff'  # TODO change with date

	# This file contain all the headers to be included in the arff file to be used by Weka 
	# for training the surrogate fitness function
	f = open(f'{toolPath}/data/headers.txt', 'r')
	headers = f.read()
	f.close()

	# This file contain 
	f = open(f'{toolPath}/results/stats-all.txt','r')
	stats = f.read().splitlines()
	f.close()

	spaceFile = f'{toolPath}/Config-space-{optsStr}.txt'
	optionNames, _ = cu.readDomFile(spaceFile)

	featuresFile = f'{BASE_DIR}/metadata/features/{lang}-program-features.txt'
	featureMap = mlu.readFeatures(featuresFile)

	f = open(f'{toolPath}/CA-{optsStr}-t3.csv', 'r')
	configLines = f.read().splitlines()
	f.close()

	designMap = {}
	for cLine in configLines:
		designMap[cu.designLine2Config(tool,cLine,optionNames)] = cLine.split(',')
	
	# adding defconfig results into the datasets
	if defconfig != None:
		f = open(f'{toolPath}/results/stats-defconfig.txt','r')
		stats.extend(f.read().splitlines())
		f.close()
		designMap[cu.designLine2Config(tool,defconfig,optionNames)] = defconfig.split(',')

	datapoints=[]
	for stat in stats:
		config, task, verdict, result, propFile, label= None, None, None, None, None, None
		if lang=='java':
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

		assert (config in designMap.keys()), f'Config is not in the designMap: {config}'
		assert (task in featureMap.keys()), f'Task is not in the feature map: {task}'
		dp = designMap[config] + featureMap[task] +  [label] + [propFile[:-4]] + [task]
		datapoints.append(dp)

	with open(saveFile, "w") as sf:
		sf.write(headers+'\n')
		writer = csv.writer(sf)
		writer.writerows(datapoints)

for tool, fields in tools.items():
	createAllArrf(tool, fields[1]+'opts', fields[0],defconfig=fields[2])