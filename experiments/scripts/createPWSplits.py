#!/usr/bin/env python
"""
Partitions a single arff file into 5 splits.
"""
import re, os, csv
import Utils_ML as mlu

HOME_DIR = str(os.getenv("HOME"))
BASE_DIR=f'{HOME_DIR}/satune/experiments'
saveFileBase = 'Aug14'

tools={'cbmc':['c','21'],'jayhorn':['java','12'], 'jbmc':['java', '27'], 'symbiotic':['c','16']}

def writeArrfFile(file,data,metadata):
	with open(file, "w") as sf:
		sf.write(metadata+'\n')
		writer = csv.writer(sf)
		writer.writerows(data)
		sf.close()

def createSplits(tool, optsStr, lang):
	toolPath = f'{BASE_DIR}/tools/{tool}'
	filePattern = f'{BASE_DIR}/metadata/splits/{lang}-testset-XYZ.txt'
	testProgs = []
	for x in range(1,6): 
		with open(filePattern.replace('XYZ',str(x)),'r') as file: 
			testProgs.append([line.split(',')[0] for line in file.read().splitlines()])

	allDataFile = f'{toolPath}/data/All-March18.arff'  # TODO change with date
	metadata, dataset = mlu.readArff(allDataFile, 0)
	testMaps = [[],[],[],[],[]]
	trainMaps = [[],[],[],[],[]]
	for dp in dataset:
		for x in range(0,5):
			if dp[-1] in testProgs[x]:
				testMaps[x].append(dp[:-1])
			else:
				trainMaps[x].append(dp[:-1])

	saveFiles = [[f'{toolPath}/data/test-{i}.arff', f'{toolPath}/data/train-{i}.arff'] for i in range(1, 6)]
	for x in range(0, 5):
		writeArrfFile(saveFiles[x][0],testMaps[x], metadata)
		writeArrfFile(saveFiles[x][1],trainMaps[x], metadata)

for tool,fields in tools.items():
	createSplits(tool, fields[1]+'opts', fields[0])