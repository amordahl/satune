#!/usr/bin/env python3.5

import sys,re,os
import subprocess as sp

BASE_DIR='{}/satune'.format(str(os.getenv("HOME")))
BENCH_DIR='{}/experiments/sv-comp/benchmarks/java'.format(BASE_DIR)

probFile='{}/properties/assert.prp'.format(BENCH_DIR)
commonJar='{}/common'.format(BENCH_DIR)

def runPart(part):
	samplesFile='{}/experiments/metadata/tasks/java-tasks-p{}.txt'.format(BASE_DIR, part)
	f1 = open(samplesFile.format(part),'r')
	samples = f1.read().split('\n')

	f = open("results/log-p{}.txt".format(part), "w")
	for sample in samples:
		srcFile = BENCH_DIR +'/'+ sample.split(',')[0]
		cmd=['./jbmc', '--propertyfile', probFile, commonJar, srcFile, '--unwind 100']
		print(cmd)
		try:
			outs = sp.call(cmd, stdout=f, stderr=f)
			f.write(sample + ',' + str(outs)+',assert.prp\n')
		except sp.TimeoutExpired:
			f.write(sample + ',' + str(outs)+ ',assert.prp,timeout\n')
		except:
			f.write(sample + ',' + str(outs)+ ',assert.prp,incomplete\n')
		f.flush()
	f.close()

if __name__ == '__main__':
    partIN=sys.argv[1]
    runPart(partIN)