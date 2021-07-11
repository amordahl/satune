import subprocess as sp
import re, os, string, random
from os.path import isfile, join

questions=['?']

def runWekaTest(modelFile,trainFile,predFile,predSaveFile):
	cmd = ['java','-cp','/Users/anon/weka/weka.jar','meka.classifiers.multilabel.MCC','-verbosity','3', '-l',modelFile,
			'-t',trainFile,'-T',predFile,'-predictions',predSaveFile,'-W','weka.classifiers.trees.J48']
	outs = sp.call(cmd)

def runMekaTest(modelFile,trainFile,predFile,predSaveFile):
	cmd = ['java','-cp','/Users/anon/meka/lib/*','meka.classifiers.multilabel.MCC','-verbosity','3',
		'-l',modelFile,'-t',trainFile,'-T',predFile,'-predictions',predSaveFile,'-W','weka.classifiers.trees.J48']
	outs = sp.call(cmd)

def getMetaAndDataLines(fileName):
	f = open(fileName,'r')
	lines = f.read().splitlines()
	f.close()
	dataStartIndx = lines.index('@data')
	return '\n'.join(lines[:(dataStartIndx+1)]), lines[(dataStartIndx+1):]

def readArff(arffFile,classIndex):
	metadata, dataLines = getMetaAndDataLines(arffFile)
	dataset = []
	for line in dataLines:
		features = line.split(',')#[(classIndex+1):]
		# features[11] = '1'
		dataset.append(features)# questions + ','.join(features)
	return metadata, dataset

def readFeatures(fileName):
	f = open(fileName,'r')
	lines = f.read().splitlines()[1:] #skipping header
	f.close()
	features={}
	for line in lines:
		fields=line.split(',')
		features[fields[0]]=fields[1:]
	return features
