#!/usr/bin/env python3.5

import sys,re,os
import subprocess as sp

BASE_DIR='{}/satune'.format(str(os.getenv("HOME")))
BENCH_DIR='{}/experiments/sv-comp/benchmarks/java'.format(BASE_DIR)

probFile='{}/properties/assert.prp'.format(BENCH_DIR)
commonJar='{}/common'.format(BENCH_DIR)

samplesFile='{}/experiments/metadata/tasks/java-tasks-all.txt'.format(BASE_DIR)
f1 = open(samplesFile,'r')
programs = f1.read().splitlines()
f1.close()

f = open("results/log-defconfig.txt", "w")
for sample in programs:
	srcFile = BENCH_DIR+'/'+sample.split(',')[0]
	cmd=['./jayhorn', '--propertyfile', probFile, commonJar, srcFile]
	try:
		outs = sp.call(cmd, stdout=f, stderr=f)
		f.write(sample + ',' + str(outs) + ',assert.prp\n')
	except:
		f.write(sample + ',' + str(outs) + ',assert.prp,incomplete\n')
	f.flush()
f.close()