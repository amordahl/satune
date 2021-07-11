#!/usr/bin/env python

import sys,re,os
import subprocess as sp

BASE_DIR='{}/satune'.format(str(os.getenv("HOME")))
BENCH_DIR='{}/experiments/sv-comp/benchmarks/java'.format(BASE_DIR)

configsFile='Configs-12opts-t3-CA.txt'
probFile='{}/properties/assert.prp'.format(BENCH_DIR)
commonJar='{}/common'.format(BENCH_DIR)

def runPart(part):
	samplesFile='{}/experiments/metadata/tasks/java-tasks-p{}.txt'.format(BASE_DIR, part)
	f1 = open(samplesFile.format(part),'r')
	samples = f1.read().split('\n')
	f1.close()
	f2 = open(configsFile,'r')
	configs = f2.read().split('\n')
	f2.close()

	f = open("results/log-ground-truth-p{}.txt".format(part), "w")
	for sample in samples:
		for config in configs:
			srcFile = BENCH_DIR +'/'+ sample.split(',')[0]
			# Update the following command for jayhorn
			cmd=['./jayhorn', '--propertyfile', probFile, commonJar, srcFile] + config.split(' ')
			try:
				outs = sp.call(cmd, stdout=f, stderr=f)
				f.write(config + ',' + sample + ',' + str(outs)+',assert.prp\n')
			except sp.TimeoutExpired:
				f.write(config + ',' + sample + ',' + str(outs)+ ',assert.prp,timeout\n')
			except:
				f.write(config + ',' + sample + ',' + str(outs)+ ',assert.prp,incomplete\n')
			f.flush()
	f.close()

if __name__ == '__main__':
    partIN=sys.argv[1]
    runPart(partIN)