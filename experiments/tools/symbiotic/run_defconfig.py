#!/usr/bin/env python

import sys,re,os
import subprocess as sp

BASE_DIR='{}/ssatune'.format(str(os.getenv("HOME")))
BENCH_DIR='{}/experiments/sv-comp/benchmarks/c'.format(BASE_DIR)

samplesFile='{}/data/tasks/c-tasks-all.txt'.format(BASE_DIR)
f1 = open(samplesFile,'r')
programs = f1.read().splitlines()
f1.close()

f = open("results/log-defconfig.txt", "w")
for sample in programs:
        srcFile, probFile = ['{}/{}'.format(BENCH_DIR, item) for item in sample.split(',')]
        conf = '--timeout=60 --witness witness.graphml --no-integrity-check'
        probFile='--prp='+probFile
        cmd = ['symbiotic/bin/symbiotic', probFile]
        cmd.extend(conf.split(' '))
        cmd.append(srcFile)
        print(cmd)
        try:
              outs = sp.call(cmd, stdout=f, stderr=f)
              f.write(sample + ',' + str(outs)+'\n')
        except sp.TimeoutExpired:
              f.write(sample + ',' + str(outs)+ ',timeout\n')
        except:
              f.write(sample + ',' + str(outs)+ ',incomplete\n')
        f.flush()
f.close()
