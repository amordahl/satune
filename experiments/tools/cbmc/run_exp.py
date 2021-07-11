#!/usr/bin/env python3.5

import sys,re,os
import subprocess as sp

BASE_DIR='{}/satune'.format(str(os.getenv("HOME")))
BENCH_DIR='{}/experiments/sv-comp/benchmarks/c'.format(BASE_DIR)  # TODO adjust based on where the benchmarks are downloaded.

configsFile='Configs-21opts-t3-CA.txt'

def runPart(part):
	samplesFile='{}/metadata/tasks/c-tasks-p{}.txt'.format(BASE_DIR, part)
	f1 = open(samplesFile.format(part),'r')
	samples = f1.read().split('\n')
	f1.close()
	f2 = open(configsFile,'r')
	configs = f2.read().split('\n')
	f2.close()

	f = open("results/log-ground-truth-p{}.txt".format(part), "w")
	for config in configs:
		for sample in samples:
			srcFile, probFile = ['{}/{}'.format(BENCH_DIR, item) for item in sample.split(',')]
			cmd = ['cbmc', '--propertyfile', probFile, srcFile] + config.split(' ')
			try:
				outs = sp.call(cmd, stdout=f, stderr=f)
				f.write(config + ',' + sample + ',' + str(outs)+'\n')
			except sp.TimeoutExpired:
				f.write(config + ',' + sample + ',' + str(outs)+ ',timeout\n')
			except:
				f.write(config + ',' + sample + ',' + str(outs)+ ',incomplete\n')
			f.flush()
	f.close()

if __name__ == '__main__':
    partIN=sys.argv[1]
    runPart(partIN)