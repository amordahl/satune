#!/usr/bin/env python3.5

import sys,re,os
import subprocess as sp

BASE_DIR='{}/satune'.format(str(os.getenv("HOME")))
BENCH_DIR='{}/experiments/sv-comp/benchmarks/c'.format(BASE_DIR)

def runPart(part):
	samplesFile='{}/experiments/metadata/tasks/c-tasks-p{}.txt'.format(BASE_DIR, part)
	f1 = open(samplesFile.format(part),'r')
	samples = f1.read().split('\n')
	f1.close()
	
	f = open("results/log-defconfig-p{}.txt".format(part), "w")
	for sample in samples:
		srcFile, probFile = [BENCH_DIR+'/'+item for item in sample.split(',')]
		cmd=['cbmc', '--propertyfile', probFile, srcFile, '--unwind', '100', '--mm', 'sc', '--round-to-nearest']
		try:
			outs = sp.call(cmd, stdout=f, stderr=f)
			f.write(sample + ',' + str(outs)+'\n')
		except sp.TimeoutExpired:
			f.write(sample +','+ str(outs) + ',timeout\n')
		except:
			f.write(sample +','+ str(outs) + ',incomplete\n')
		f.flush()
	f.close()

if __name__ == '__main__':
    partIN=sys.argv[1]
    runPart(partIN)